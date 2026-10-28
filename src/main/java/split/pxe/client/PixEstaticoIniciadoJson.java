package split.pxe.client;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import split.pxe.domain.PixEstaticoIniciado;
import split.pxe.domain.tipos.Centavos;
import split.pxe.domain.tipos.Cnpj;
import split.pxe.domain.tipos.CnpjRaiz;
import split.pxe.domain.tipos.DocumentoFiscal;

/**
 * Corpo do POST de um PixEstaticoIniciado ao oracle-service.
 * Optional vazio vira campo omitido; valores em centavos; instantes em ISO-8601 (UTC).
 *
 * TODO o contrato ainda precisa ser acordado com o oracle-service: nomes, tipos e caminho são provisórios.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PixEstaticoIniciadoJson(
        String e2eId,
        String formaIniciacao,
        long valorPagoCentavos,
        Instant pagoEm,
        String documentoPagador,
        String documentoRecebedor,
        String cnpjItp,                // omitido quando não houve ITP
        String pspPagador,
        String pspRecebedorDireto,
        String pspRecebedorIndireto,   // omitido quando o recebedor tem conta conosco
        CampoFiscalJson<Long> cbs,
        CampoFiscalJson<Long> ibs,
        CampoFiscalJson<String> documentoFiscal,
        Instant chegouEm) {

    static PixEstaticoIniciadoJson de(PixEstaticoIniciado pix) {
        var fiscais = pix.camposFiscais();
        return new PixEstaticoIniciadoJson(
                pix.e2eId().valor(),
                pix.formaIniciacao().name(),
                pix.valorPago().valor(),
                pix.pagoEm(),
                pix.pagador().valor(),
                pix.recebedor().valor(),
                pix.itp().map(Cnpj::valor).orElse(null),
                pix.pspPagador().valor(),
                pix.pspRecebedorDireto().valor(),
                pix.pspRecebedorIndireto().map(CnpjRaiz::valor).orElse(null),
                CampoFiscalJson.de(fiscais.cbs(), Centavos::valor),
                CampoFiscalJson.de(fiscais.ibs(), Centavos::valor),
                CampoFiscalJson.de(fiscais.documentoFiscal(), DocumentoFiscal::valor),
                pix.chegouEm());
    }
}
