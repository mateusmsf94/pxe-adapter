package split.pxe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static split.pxe.service.Exemplos.E2E;
import static split.pxe.service.Exemplos.ISPB_NOS;
import static split.pxe.service.Exemplos.preliminar;
import static split.pxe.service.Exemplos.preliminarValido;
import static split.pxe.service.Exemplos.segregacao;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import split.pxe.domain.LiquidacaoPixEstatico;
import split.pxe.domain.PixEstaticoIniciado;
import split.pxe.service.MensagemPixEstatico.Preliminar;
import split.pxe.service.MensagemPixEstatico.Segregacao;
import split.pxe.service.Traducao.Aceito;
import split.pxe.service.Traducao.MotivoIgnorado;
import split.pxe.service.Traducao.MotivoIgnorado.StatusNaoFinal;

class ProcessarMensagemPixEstaticoTest {

    /** Grava, em ordem, o que chegou em cada porta. */
    private final List<Object> chamadas = new ArrayList<>();

    private final ProcessarMensagemPixEstatico processar = new ProcessarMensagemPixEstatico(
            Exemplos.tradutor(),
            new RegistroPixEstatico() {
                public void registrarIniciado(PixEstaticoIniciado iniciado) { chamadas.add(iniciado); }
                public void registrarLiquidacao(LiquidacaoPixEstatico liquidacao) { chamadas.add(liquidacao); }
            },
            new MensagensNaoAceitas() {
                public void ignorada(MotivoIgnorado motivo) { chamadas.add(motivo); }
                public void malformada(String referencia, List<String> erros) { chamadas.add(new Malformada(referencia, erros)); }
            });

    private record Malformada(String referencia, List<String> erros) {}

    @Test
    void preliminarAceitoVaiParaORegistroDeIniciado() {
        var traducao = processar.processar(new Preliminar(preliminarValido()));

        assertEquals(List.of(((Aceito) traducao).fato()), chamadas);
        assertInstanceOf(PixEstaticoIniciado.class, chamadas.getFirst());
    }

    @Test
    void segregacaoAceitaVaiParaORegistroDeLiquidacao() {
        var traducao = processar.processar(new Segregacao(segregacao("ACSC", "2026-10-27T10:15:04-03:00")));

        assertEquals(List.of(((Aceito) traducao).fato()), chamadas);
        assertInstanceOf(LiquidacaoPixEstatico.class, chamadas.getFirst());
    }

    @Test
    void rejeitadaTambemVaiParaORegistroDeLiquidacao() {
        processar.processar(new Segregacao(segregacao("RJCT", null)));

        assertInstanceOf(LiquidacaoPixEstatico.class, chamadas.getFirst());
        assertEquals(1, chamadas.size());
    }

    @Test
    void ignoradaVaiParaAsNaoAceitas() {
        processar.processar(new Segregacao(segregacao("PDNG", null)));

        assertEquals(List.of(new StatusNaoFinal("PDNG")), chamadas);
    }

    @Test
    void malformadaVaiParaAsNaoAceitas() {
        processar.processar(new Preliminar(preliminar("QRDN", ISPB_NOS.valor(), null, null, null)));

        assertEquals(List.of(new Malformada(E2E, List.of("formaIniciacao: QRDN chegou ao pxe-adapter, mas não é Pix Estático"))),
                chamadas);
    }
}
