package split.pxe.client;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import split.pxe.domain.LiquidacaoPixEstatico;
import split.pxe.domain.ResultadoLiquidacao.Liquidado;
import split.pxe.domain.ResultadoLiquidacao.Rejeitado;

/**
 * Corpo do POST de uma LiquidacaoPixEstatico ao oracle-service.
 *
 *   {"e2eId":"...","resultado":"LIQUIDADO","liquidadoEm":"...","chegouEm":"..."}
 *   {"e2eId":"...","resultado":"REJEITADO","status":"RJCT","chegouEm":"..."}
 *
 * TODO o contrato ainda precisa ser acordado com o oracle-service: nomes, tipos e caminho são provisórios.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LiquidacaoPixEstaticoJson(
        String e2eId,
        Resultado resultado,
        Instant liquidadoEm,   // só em LIQUIDADO
        String status,         // só em REJEITADO
        Instant chegouEm) {

    public enum Resultado { LIQUIDADO, REJEITADO }

    static LiquidacaoPixEstaticoJson de(LiquidacaoPixEstatico liquidacao) {
        var e2eId = liquidacao.e2eId().valor();
        var chegouEm = liquidacao.chegouEm();
        return switch (liquidacao.resultado()) {
            case Liquidado(var liquidadoEm) -> new LiquidacaoPixEstaticoJson(e2eId, Resultado.LIQUIDADO, liquidadoEm, null, chegouEm);
            case Rejeitado(var status) -> new LiquidacaoPixEstaticoJson(e2eId, Resultado.REJEITADO, null, status, chegouEm);
        };
    }
}
