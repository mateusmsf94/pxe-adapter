package split.pxe.client;

/** O oracle-service recusou o fato (4xx). Repetir não adianta: o contrato está quebrado de um dos lados. */
public final class RecusadoPeloOracle extends RuntimeException {

    private final int status;

    public RecusadoPeloOracle(int status) {
        super("oracle-service recusou o fato com HTTP " + status);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
