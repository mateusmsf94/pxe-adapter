package split.pxe.domain.tipos;

import java.util.regex.Pattern;

/** CPF: 11 dígitos. */
public record Cpf(String valor) implements CnpjOuCpf {

    private static final Pattern FORMATO = Pattern.compile("^\\d{11}$");

    public Cpf {
        Formato.exigir(FORMATO, valor, "CPF");
    }
}
