package split.pxe.domain.tipos;

import java.util.regex.Pattern;

/** CNPJ completo: 14 caracteres alfanuméricos (o CNPJ passa a admitir letras). */
public record Cnpj(String valor) implements CnpjOuCpf {

    private static final Pattern FORMATO = Pattern.compile("^[A-Za-z0-9]{14}$");

    public Cnpj {
        Formato.exigirSemExpor(FORMATO, valor, "CNPJ");
    }

    public CnpjRaiz raiz() {
        return new CnpjRaiz(valor.substring(0, 8));
    }
}
