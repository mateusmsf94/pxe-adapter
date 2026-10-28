package split.pxe.client;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.faulttolerance.api.ExponentialBackoff;
import jakarta.inject.Singleton;
import split.pxe.domain.LiquidacaoPixEstatico;
import split.pxe.domain.PixEstaticoIniciado;
import split.pxe.service.RegistroPixEstatico;

/**
 * Entrega os fatos ao oracle-service.
 *
 * Repetir é seguro: o oracle-service é idempotente por e2eId e tipo de fato. Repete em falha de
 * rede e em 5xx; não repete em 4xx (RecusadoPeloOracle), porque a mesma requisição seria recusada de novo.
 * Esgotadas as tentativas, a exceção sobe até o resource, que responde 503 para quem chamou tentar depois.
 */
@Singleton
public class RegistroPixEstaticoOracle implements RegistroPixEstatico {

    private final OracleServiceClient oracle;

    RegistroPixEstaticoOracle(@RestClient OracleServiceClient oracle) {
        this.oracle = oracle;
    }

    @Override
    @Retry(maxRetries = 3, delay = 200, jitter = 100, abortOn = RecusadoPeloOracle.class)
    @ExponentialBackoff
    public void registrarIniciado(PixEstaticoIniciado iniciado) {
        oracle.registrarIniciado(PixEstaticoIniciadoJson.de(iniciado));
    }

    @Override
    @Retry(maxRetries = 3, delay = 200, jitter = 100, abortOn = RecusadoPeloOracle.class)
    @ExponentialBackoff
    public void registrarLiquidacao(LiquidacaoPixEstatico liquidacao) {
        oracle.registrarLiquidacao(LiquidacaoPixEstaticoJson.de(liquidacao));
    }
}
