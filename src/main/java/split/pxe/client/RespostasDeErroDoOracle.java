package split.pxe.client;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import jakarta.ws.rs.core.Response;

/** Separa a recusa (4xx, não adianta repetir) da indisponibilidade (5xx, pode adiantar). */
public class RespostasDeErroDoOracle implements ResponseExceptionMapper<RuntimeException> {

    @Override
    public RuntimeException toThrowable(Response resposta) {
        int status = resposta.getStatus();
        if (status >= 400 && status < 500) return new RecusadoPeloOracle(status);
        return new OracleIndisponivel(status);
    }
}
