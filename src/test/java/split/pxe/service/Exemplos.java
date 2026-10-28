package split.pxe.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import split.pxe.domain.tipos.CnpjRaiz;
import split.pxe.domain.tipos.Ispb;

/** Mensagens e cadastro de exemplo para os testes. */
final class Exemplos {

    private Exemplos() {}

    // Nós: ISPB e raiz diferentes, para mostrar que não dá para trocar um pelo outro
    static final Ispb ISPB_NOS = new Ispb("11111111");
    static final CnpjRaiz RAIZ_NOS = new CnpjRaiz("AB111111");
    static final PspProprio NOS = new PspProprio(ISPB_NOS, RAIZ_NOS);

    static final String ISPB_OUTRO_BANCO = "22222222";
    static final CnpjRaiz RAIZ_OUTRO_BANCO = new CnpjRaiz("33333333");
    static final String ISPB_INDIRETO = "44444444";
    static final CnpjRaiz RAIZ_INDIRETO = new CnpjRaiz("55555555");

    static final String E2E = "E22222222202610271015abcdEFGH123";
    static final String PJ_PAGADOR = "11222333000181";
    static final String PJ_RECEBEDOR = "12ABC34501DE35";   // CNPJ alfanumérico

    static final Instant AGORA = Instant.parse("2026-10-27T13:15:05Z");

    static final CadastroParticipantes CADASTRO = ispb -> Optional.ofNullable(Map.of(
            ISPB_NOS, RAIZ_NOS,
            new Ispb(ISPB_OUTRO_BANCO), RAIZ_OUTRO_BANCO,
            new Ispb(ISPB_INDIRETO), RAIZ_INDIRETO).get(ispb));

    static Tradutor tradutor() {
        return new Tradutor(NOS, CADASTRO, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    static PayloadPreliminar preliminar(String forma, String ispbPspRecebedor, String cbs, String ibs, String documentoFiscal) {
        return new PayloadPreliminar(E2E, forma, "1000.00", "2026-10-27T10:15:03-03:00",
                PJ_PAGADOR, ISPB_OUTRO_BANCO, PJ_RECEBEDOR, ispbPspRecebedor, null, cbs, ibs, documentoFiscal);
    }

    static PayloadPreliminar preliminarValido() {
        return preliminar("QRES", ISPB_NOS.valor(), "92.50", "177.00", "NFE-35261012ABC34501DE3555");
    }

    static PayloadSegregacao segregacao(String status, String dataHoraLiquidacao) {
        return new PayloadSegregacao(E2E, status, dataHoraLiquidacao);
    }
}
