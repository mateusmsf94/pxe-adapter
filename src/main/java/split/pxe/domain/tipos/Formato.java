package split.pxe.domain.tipos;

import java.util.regex.Pattern;

/** Checagem de formato usada pelos construtores dos tipos de valor. */
final class Formato {
    private Formato() {}

    static String exigir(Pattern formato, String valor, String nome) {
        if (valor == null || !formato.matcher(valor).matches()) {
            throw new IllegalArgumentException(nome + " fora do formato: " + valor);
        }
        return valor;
    }
}
