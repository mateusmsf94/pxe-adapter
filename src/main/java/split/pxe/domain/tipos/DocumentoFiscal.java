package split.pxe.domain.tipos;

/** Identificador do Documento Fiscal vinculado à transação: 1 a 50 caracteres. */
public record DocumentoFiscal(String valor) {

    public DocumentoFiscal {
        if (valor == null || valor.isBlank() || valor.length() > 50) {
            throw new IllegalArgumentException("Documento Fiscal precisa ter de 1 a 50 caracteres: " + valor);
        }
    }
}
