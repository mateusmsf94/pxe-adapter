package split.pxe.domain.tipos;

/**
 * Documento de uma pessoa: CNPJ (PJ) ou CPF (PF).
 *
 * É sealed porque só existem esses dois casos. Um switch sobre CnpjOuCpf
 * não precisa de default, e o compilador avisa se aparecer um terceiro.
 */
public sealed interface CnpjOuCpf permits Cnpj, Cpf {

    String valor();

    /** Decide pelo tamanho, como o regex do manual: 11 dígitos = CPF, 14 alfanuméricos = CNPJ. */
    static CnpjOuCpf de(String texto) {
        if (texto == null) throw new IllegalArgumentException("documento ausente");
        return switch (texto.length()) {
            case 11 -> new Cpf(texto);
            case 14 -> new Cnpj(texto);
            default -> throw new IllegalArgumentException(
                    "documento com " + texto.length() + " caracteres; esperado 11 (CPF) ou 14 (CNPJ)");
        };
    }
}
