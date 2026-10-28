package split.pxe.domain.tipos;

import java.util.regex.Pattern;

/**
 * Código ISPB: 8 dígitos. É como o SPB (SPI, STR) identifica instituições.
 * Não é a raiz do CNPJ, mesmo quando coincide; a conversão exige o cadastro de participantes.
 */
public record Ispb(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^\\d{8}$");

    public Ispb {
        Formato.exigir(FORMATO, valor, "ISPB");
    }
}
