package split.pxe.domain.tipos;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Dinheiro em centavos. Nunca negativo: o Manual de Integração (3.1) proíbe sinal nos campos Decimal.
 * Ter um tipo próprio impede somar centavos com um id ou com uma quantidade.
 */
public record Centavos(long valor) implements Comparable<Centavos> {

    public static final Centavos ZERO = new Centavos(0);

    /** Ponto como separador, até 2 casas, sem sinal e sem notação científica. */
    private static final Pattern DECIMAL = Pattern.compile("^\\d{1,16}(\\.\\d{1,2})?$");

    public Centavos {
        if (valor < 0) throw new IllegalArgumentException("valor negativo: " + valor);
    }

    /** "1234.5" → 123450 centavos. */
    public static Centavos deDecimal(String texto) {
        Formato.exigir(DECIMAL, texto, "decimal");
        return new Centavos(new BigDecimal(texto).movePointRight(2).longValueExact());
    }

    public Centavos mais(Centavos outro) {
        return new Centavos(Math.addExact(valor, outro.valor));
    }

    public boolean maiorQue(Centavos outro) {
        return compareTo(outro) > 0;
    }

    @Override
    public int compareTo(Centavos outro) {
        return Long.compare(valor, outro.valor);
    }

    @Override
    public String toString() {
        return String.format("R$%d,%02d", valor / 100, valor % 100);
    }
}
