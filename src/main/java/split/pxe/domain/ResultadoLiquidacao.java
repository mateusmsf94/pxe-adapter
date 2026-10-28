package split.pxe.domain;

import java.time.Instant;
import java.util.Objects;

/** O desfecho final da ordem no SPI. Status que não é final não vira fato (fica no Ignorado). */
public sealed interface ResultadoLiquidacao {

    /** O dinheiro andou. liquidadoEm é o horário de liquidação que veio na segregação. */
    record Liquidado(Instant liquidadoEm) implements ResultadoLiquidacao {
        public Liquidado {
            Objects.requireNonNull(liquidadoEm, "liquidadoEm");
        }
    }

    /** O SPI rejeitou a ordem. status é o código como veio na segregação. */
    record Rejeitado(String status) implements ResultadoLiquidacao {
        public Rejeitado {
            Objects.requireNonNull(status, "status");
        }
    }
}
