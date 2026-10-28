package split.pxe.domain.tipos;

import java.util.Objects;

/**
 * Os três campos fiscais exatamente como o pagador preencheu. Nenhuma regra de split aqui:
 * dizer se isso é uma declaração completa, ausente ou inconsistente é do split-service.
 */
public record CamposFiscais(
        CampoFiscal<Centavos> cbs,
        CampoFiscal<Centavos> ibs,
        CampoFiscal<DocumentoFiscal> documentoFiscal) {

    public CamposFiscais {
        Objects.requireNonNull(cbs, "cbs");
        Objects.requireNonNull(ibs, "ibs");
        Objects.requireNonNull(documentoFiscal, "documentoFiscal");
    }

    public static CamposFiscais ler(String cbs, String ibs, String documentoFiscal) {
        return new CamposFiscais(
                CampoFiscal.ler(cbs, Centavos::deDecimal),
                CampoFiscal.ler(ibs, Centavos::deDecimal),
                CampoFiscal.ler(documentoFiscal, DocumentoFiscal::new));
    }
}
