package split.pxe.domain;

import java.time.Instant;
import java.util.Objects;

import split.pxe.domain.tipos.E2eId;

/**
 * O SPI deu o resultado final de um Pix Estático (veio na segregação): liquidou ou rejeitou.
 * Só tem o e2eId para ligar à iniciação; quem faz essa ligação é o oracle-service.
 */
public record LiquidacaoPixEstatico(
        E2eId e2eId,
        Instant chegouEm,
        ResultadoLiquidacao resultado) implements FatoPixEstatico {

    public LiquidacaoPixEstatico {
        Objects.requireNonNull(e2eId, "e2eId");
        Objects.requireNonNull(chegouEm, "chegouEm");
        Objects.requireNonNull(resultado, "resultado");
    }
}
