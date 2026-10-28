package split.pxe.client;

/** O oracle-service respondeu com erro de servidor (5xx). Repetir pode adiantar. */
public final class OracleIndisponivel extends RuntimeException {

    public OracleIndisponivel(int status) {
        super("oracle-service respondeu HTTP " + status);
    }
}
