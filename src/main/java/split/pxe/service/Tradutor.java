package split.pxe.service;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import split.pxe.domain.FormaIniciacao;
import split.pxe.domain.LiquidacaoPixEstatico;
import split.pxe.domain.PixEstaticoIniciado;
import split.pxe.domain.ResultadoLiquidacao;
import split.pxe.domain.ResultadoLiquidacao.Liquidado;
import split.pxe.domain.ResultadoLiquidacao.Rejeitado;
import split.pxe.domain.tipos.CamposFiscais;
import split.pxe.domain.tipos.Centavos;
import split.pxe.domain.tipos.Cnpj;
import split.pxe.domain.tipos.CnpjOuCpf;
import split.pxe.domain.tipos.CnpjRaiz;
import split.pxe.domain.tipos.E2eId;
import split.pxe.domain.tipos.Ispb;
import split.pxe.service.MensagemPixEstatico.Preliminar;
import split.pxe.service.MensagemPixEstatico.Segregacao;
import split.pxe.service.Traducao.Aceito;
import split.pxe.service.Traducao.Ignorado;
import split.pxe.service.Traducao.Malformado;
import split.pxe.service.Traducao.MotivoIgnorado.StatusNaoFinal;

/**
 * Mensagem → fato do domínio. É o coração do adapter, e é uma função:
 * dado o mesmo relógio e o mesmo cadastro, a mesma mensagem dá sempre a mesma Traducao.
 * Nada aqui grava, publica ou loga; quem faz isso é o ProcessarMensagemPixEstatico, com o resultado.
 *
 * Cada mensagem é traduzida sozinha: o preliminar não espera a segregação, e nenhum dos dois
 * consulta o que já passou por aqui. Traduz, não julga: split, segregado, PJ/PF e
 * CBS + IBS ≤ valor pago são do split-service.
 */
public final class Tradutor {

    // TODO confirmar no catálogo do SPI usado pelo banco quais status da segregação são finais.
    //  Suposição: ACSC = liquidado, RJCT = rejeitado; qualquer outro ainda não é final.
    private static final String STATUS_LIQUIDADO = "ACSC";
    private static final String STATUS_REJEITADO = "RJCT";

    private final PspProprio pspProprio;
    private final CadastroParticipantes cadastro;
    private final Clock relogio;

    public Tradutor(PspProprio pspProprio, CadastroParticipantes cadastro, Clock relogio) {
        this.pspProprio = Objects.requireNonNull(pspProprio);
        this.cadastro = Objects.requireNonNull(cadastro);
        this.relogio = Objects.requireNonNull(relogio);
    }

    public Traducao traduzir(MensagemPixEstatico mensagem) {
        return switch (mensagem) {
            case Preliminar(var payload) -> doPreliminar(payload);
            case Segregacao(var payload) -> daSegregacao(payload);
        };
    }

    /**
     * Lê todos os campos obrigatórios juntando os erros, e só depois monta o fato.
     * Os fiscais nunca são erro da mensagem: o que não dá para ler vira CampoFiscal.Invalido.
     */
    private Traducao doPreliminar(PayloadPreliminar p) {
        var l = new LeitorDeCampos();
        E2eId e2eId = l.obrigatorio("e2eId", p.e2eId(), E2eId::new);
        FormaIniciacao forma = l.obrigatorio("formaIniciacao", p.formaIniciacao(), Tradutor::formaEstatica);
        Centavos valor = l.obrigatorio("valor", p.valor(), Centavos::deDecimal);
        Instant pagoEm = l.obrigatorio("dataHoraPagamento", p.dataHoraPagamento(), Tradutor::instante);
        CnpjOuCpf pagador = l.obrigatorio("documentoPagador", p.documentoPagador(), CnpjOuCpf::de);
        CnpjOuCpf recebedor = l.obrigatorio("documentoRecebedor", p.documentoRecebedor(), CnpjOuCpf::de);
        Optional<Cnpj> itp = l.opcional("cnpjItp", p.cnpjItp(), Cnpj::new);
        CnpjRaiz pspPagador = l.obrigatorio("ispbPspPagador", p.ispbPspPagador(), this::raizDoParticipante);
        Optional<CnpjRaiz> indireto = l.obrigatorio("ispbPspRecebedor", p.ispbPspRecebedor(), this::recebedorIndireto);

        if (l.temErros()) return new Malformado(referencia(p.e2eId()), l.erros());

        var camposFiscais = CamposFiscais.ler(p.cbs(), p.ibs(), p.documentoFiscal());

        return new Aceito(new PixEstaticoIniciado(
                e2eId, forma, valor, pagoEm,
                pagador, recebedor, itp,
                pspPagador, pspProprio.raizCnpj(), indireto,
                camposFiscais, relogio.instant()));
    }

    /**
     * e2eId e status são lidos sempre; a data de liquidação, só quando liquidou.
     * Status que não é final não é erro nem fato: é Ignorado.
     */
    private Traducao daSegregacao(PayloadSegregacao p) {
        var l = new LeitorDeCampos();
        E2eId e2eId = l.obrigatorio("e2eId", p.e2eId(), E2eId::new);
        String status = l.obrigatorio("status", p.status(), Function.identity());
        Instant liquidadoEm = STATUS_LIQUIDADO.equals(status)
                ? l.obrigatorio("dataHoraLiquidacao", p.dataHoraLiquidacao(), Tradutor::instante)
                : null;

        if (l.temErros()) return new Malformado(referencia(p.e2eId()), l.erros());

        return switch (status) {
            case STATUS_LIQUIDADO -> liquidacao(e2eId, new Liquidado(liquidadoEm));
            case STATUS_REJEITADO -> liquidacao(e2eId, new Rejeitado(status));
            default -> new Ignorado(new StatusNaoFinal(status));
        };
    }

    private Aceito liquidacao(E2eId e2eId, ResultadoLiquidacao resultado) {
        return new Aceito(new LiquidacaoPixEstatico(e2eId, relogio.instant(), resultado));
    }

    /**
     * Só Pix Estático deveria chegar aqui; outra forma é erro de roteamento, não mensagem a ignorar.
     * TODO confirmar se um Pix Estático iniciado por ITP pode chegar com forma INIC. Hoje vira Malformado.
     */
    private static FormaIniciacao formaEstatica(String codigo) {
        return FormaIniciacao.de(codigo).orElseThrow(() -> new IllegalArgumentException(
                codigo + " chegou ao pxe-adapter, mas não é Pix Estático"));
    }

    private CnpjRaiz raizDoParticipante(String ispbTexto) {
        var ispb = new Ispb(ispbTexto);
        return cadastro.raizCnpjDo(ispb)
                .orElseThrow(() -> new IllegalArgumentException("ISPB " + ispb.valor() + " fora do cadastro de participantes"));
    }

    /** Se o recebedor tem conta conosco, não há indireto; senão, o PSP dele é o indireto. */
    private Optional<CnpjRaiz> recebedorIndireto(String ispbTexto) {
        var ispb = new Ispb(ispbTexto);
        if (ispb.equals(pspProprio.ispb())) return Optional.empty();
        return Optional.of(raizDoParticipante(ispbTexto));
    }

    /** O Malformado precisa de uma referência mesmo quando o e2eId é o campo quebrado. */
    private static String referencia(String e2eId) {
        return e2eId == null || e2eId.isBlank() ? "(sem e2eId)" : e2eId;
    }

    private static Instant instante(String texto) {
        return OffsetDateTime.parse(texto).toInstant();
    }
}
