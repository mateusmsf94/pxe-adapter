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

    /**
     * Para documento de pessoa (CPF, CNPJ): a mensagem não repete o valor, porque ela vai
     * parar no log das mensagens malformadas. Diz só o tamanho recebido.
     */
    static String exigirSemExpor(Pattern formato, String valor, String nome) {
        if (valor == null || !formato.matcher(valor).matches()) {
            var recebido = valor == null ? "ausente" : valor.length() + " caracteres";
            throw new IllegalArgumentException(nome + " fora do formato (" + recebido + ")");
        }
        return valor;
    }
}
