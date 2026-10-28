package split.pxe.domain.tipos;

import java.util.regex.Pattern;

/** Identificador ponta a ponta do Pix. No Pix Estático, identifica a transação e o pagamento. */
public record E2eId(String valor) {

    /** Regra da PP (Manual de Integração 3.1): 1 a 35 alfanuméricos. */
    private static final Pattern FORMATO = Pattern.compile("^[A-Za-z0-9]{1,35}$");

    public E2eId {
        Formato.exigir(FORMATO, valor, "e2eId");
    }
}
