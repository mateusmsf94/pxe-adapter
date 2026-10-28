package split.pxe.domain.tipos;

import java.util.regex.Pattern;

/**
 * Raiz do CNPJ: 8 caracteres. É como a PP identifica PSPs.
 * Tipo diferente de Cnpj para que um não seja passado no lugar do outro.
 */
public record CnpjRaiz(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[A-Za-z0-9]{8}$");

    public CnpjRaiz {
        Formato.exigir(FORMATO, valor, "raiz de CNPJ");
    }
}
