package split.pxe.service;

import split.pxe.domain.LiquidacaoPixEstatico;
import split.pxe.domain.PixEstaticoIniciado;

/**
 * Saída do adapter: o oracle-service, que guarda os fatos, junta iniciação e liquidação
 * pelo e2eId e publica pela outbox. Uma operação por fato.
 *
 * Chamar de novo com o mesmo fato é seguro: o oracle-service é idempotente por e2eId e tipo de fato.
 */
public interface RegistroPixEstatico {

    void registrarIniciado(PixEstaticoIniciado iniciado);

    void registrarLiquidacao(LiquidacaoPixEstatico liquidacao);
}
