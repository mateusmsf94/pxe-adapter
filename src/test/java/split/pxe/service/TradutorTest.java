package split.pxe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static split.pxe.service.Exemplos.AGORA;
import static split.pxe.service.Exemplos.E2E;
import static split.pxe.service.Exemplos.ISPB_INDIRETO;
import static split.pxe.service.Exemplos.ISPB_NOS;
import static split.pxe.service.Exemplos.PJ_PAGADOR;
import static split.pxe.service.Exemplos.PJ_RECEBEDOR;
import static split.pxe.service.Exemplos.RAIZ_INDIRETO;
import static split.pxe.service.Exemplos.RAIZ_NOS;
import static split.pxe.service.Exemplos.RAIZ_OUTRO_BANCO;
import static split.pxe.service.Exemplos.preliminar;
import static split.pxe.service.Exemplos.preliminarValido;
import static split.pxe.service.Exemplos.segregacao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import split.pxe.domain.FormaIniciacao;
import split.pxe.domain.LiquidacaoPixEstatico;
import split.pxe.domain.PixEstaticoIniciado;
import split.pxe.domain.ResultadoLiquidacao.Liquidado;
import split.pxe.domain.ResultadoLiquidacao.Rejeitado;
import split.pxe.domain.tipos.CampoFiscal.Ausente;
import split.pxe.domain.tipos.CampoFiscal.Invalido;
import split.pxe.domain.tipos.CampoFiscal.Presente;
import split.pxe.domain.tipos.CamposFiscais;
import split.pxe.domain.tipos.Centavos;
import split.pxe.domain.tipos.Cnpj;
import split.pxe.domain.tipos.DocumentoFiscal;
import split.pxe.domain.tipos.E2eId;
import split.pxe.service.MensagemPixEstatico.Preliminar;
import split.pxe.service.MensagemPixEstatico.Segregacao;
import split.pxe.service.Traducao.Aceito;
import split.pxe.service.Traducao.Ignorado;
import split.pxe.service.Traducao.Malformado;
import split.pxe.service.Traducao.MotivoIgnorado.StatusNaoFinal;

class TradutorTest {

    private final Tradutor tradutor = Exemplos.tradutor();

    // ── Preliminar ──

    @Test
    void preliminarValidoComRecebedorNoProprioPsp() {
        var iniciado = iniciado(tradutor.traduzir(new Preliminar(preliminarValido())));

        assertEquals(new PixEstaticoIniciado(
                new E2eId(E2E),
                FormaIniciacao.QRES,
                new Centavos(100_000),
                Instant.parse("2026-10-27T13:15:03Z"),
                new Cnpj(PJ_PAGADOR),
                new Cnpj(PJ_RECEBEDOR),
                Optional.empty(),
                RAIZ_OUTRO_BANCO,
                RAIZ_NOS,
                Optional.empty(),
                new CamposFiscais(
                        new Presente<>(new Centavos(9_250)),
                        new Presente<>(new Centavos(17_700)),
                        new Presente<>(new DocumentoFiscal("NFE-35261012ABC34501DE3555"))),
                AGORA), iniciado);
    }

    @Test
    void preliminarValidoComPspRecebedorIndireto() {
        var iniciado = iniciado(tradutor.traduzir(new Preliminar(
                preliminar("DICT", ISPB_INDIRETO, "46.25", "88.50", "NFSE-0001"))));

        assertEquals(FormaIniciacao.DICT, iniciado.formaIniciacao());
        assertEquals(RAIZ_NOS, iniciado.pspRecebedorDireto());
        assertEquals(Optional.of(RAIZ_INDIRETO), iniciado.pspRecebedorIndireto());
    }

    @Test
    void camposFiscaisInvalidosContinuamAceitos() {
        var documentoGrandeDemais = "X".repeat(51);
        var iniciado = iniciado(tradutor.traduzir(new Preliminar(
                preliminar("MANU", ISPB_NOS.valor(), "-10.00", "53,10", documentoGrandeDemais))));

        var fiscais = iniciado.camposFiscais();
        assertEquals("-10.00", assertInstanceOf(Invalido.class, fiscais.cbs()).bruto());
        assertEquals("53,10", assertInstanceOf(Invalido.class, fiscais.ibs()).bruto());
        assertEquals(documentoGrandeDemais, assertInstanceOf(Invalido.class, fiscais.documentoFiscal()).bruto());
    }

    @Test
    void camposFiscaisQueNaoVieramFicamAusentes() {
        var iniciado = iniciado(tradutor.traduzir(new Preliminar(
                preliminar("APES", ISPB_NOS.valor(), null, "", "   "))));

        assertEquals(new CamposFiscais(new Ausente<>(), new Ausente<>(), new Ausente<>()), iniciado.camposFiscais());
    }

    @Test
    void formaQueNaoEPixEstaticoEMalformado() {
        var traducao = tradutor.traduzir(new Preliminar(preliminar("QRDN", ISPB_NOS.valor(), null, null, null)));

        assertEquals(new Malformado(E2E, List.of("formaIniciacao: QRDN chegou ao pxe-adapter, mas não é Pix Estático")),
                traducao);
    }

    @Test
    void preliminarQuebradoListaTodosOsErrosJuntos() {
        var quebrado = new PayloadPreliminar(
                null,                    // e2eId ausente
                "XPTO",                  // forma desconhecida
                null,                    // valor ausente
                "ontem",                 // data ilegível
                PJ_PAGADOR,
                "77777777",              // PSP pagador fora do cadastro
                "1234567890123",         // 13 caracteres
                ISPB_NOS.valor(),
                null,
                "1.00", "1.00", "NFE-1");

        var malformado = assertInstanceOf(Malformado.class, tradutor.traduzir(new Preliminar(quebrado)));

        assertEquals("(sem e2eId)", malformado.referencia());
        assertEquals(List.of("e2eId", "formaIniciacao", "valor", "dataHoraPagamento", "documentoRecebedor", "ispbPspPagador"),
                campos(malformado));
        assertEquals("ispbPspPagador: ISPB 77777777 fora do cadastro de participantes", malformado.erros().getLast());
    }

    // ── Segregação ──

    @Test
    void acscViraLiquidado() {
        var traducao = tradutor.traduzir(new Segregacao(segregacao("ACSC", "2026-10-27T10:15:04-03:00")));

        assertEquals(new Aceito(new LiquidacaoPixEstatico(
                new E2eId(E2E), AGORA, new Liquidado(Instant.parse("2026-10-27T13:15:04Z")))), traducao);
    }

    @Test
    void rjctViraRejeitado() {
        var traducao = tradutor.traduzir(new Segregacao(segregacao("RJCT", null)));

        assertEquals(new Aceito(new LiquidacaoPixEstatico(new E2eId(E2E), AGORA, new Rejeitado("RJCT"))), traducao);
    }

    @Test
    void statusNaoFinalEIgnorado() {
        var traducao = tradutor.traduzir(new Segregacao(segregacao("PDNG", null)));

        assertEquals(new Ignorado(new StatusNaoFinal("PDNG")), traducao);
    }

    @Test
    void liquidadoSemDataEMalformado() {
        var malformado = assertInstanceOf(Malformado.class,
                tradutor.traduzir(new Segregacao(segregacao("ACSC", null))));

        assertEquals(List.of("dataHoraLiquidacao: ausente"), malformado.erros());
    }

    @Test
    void segregacaoSemStatusEMalformado() {
        var malformado = assertInstanceOf(Malformado.class,
                tradutor.traduzir(new Segregacao(segregacao(" ", null))));

        assertEquals(List.of("status: ausente"), malformado.erros());
    }

    private static PixEstaticoIniciado iniciado(Traducao traducao) {
        return assertInstanceOf(PixEstaticoIniciado.class, assertInstanceOf(Aceito.class, traducao).fato());
    }

    /** O nome do campo de cada erro, na ordem. */
    private static List<String> campos(Malformado malformado) {
        return malformado.erros().stream().map(erro -> erro.substring(0, erro.indexOf(':'))).toList();
    }
}
