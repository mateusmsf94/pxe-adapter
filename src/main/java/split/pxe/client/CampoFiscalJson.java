package split.pxe.client;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;

import split.pxe.domain.tipos.CampoFiscal;
import split.pxe.domain.tipos.CampoFiscal.Ausente;
import split.pxe.domain.tipos.CampoFiscal.Invalido;
import split.pxe.domain.tipos.CampoFiscal.Presente;

/**
 * Um CampoFiscal no JSON: o estado e, conforme ele, o valor lido ou o bruto e o motivo.
 *
 *   {"estado":"AUSENTE"}
 *   {"estado":"PRESENTE","valor":9250}
 *   {"estado":"INVALIDO","bruto":"53,10","motivo":"decimal fora do formato: 53,10"}
 *
 * TODO o contrato ainda precisa ser acordado com o oracle-service.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CampoFiscalJson<J>(Estado estado, J valor, String bruto, String motivo) {

    public enum Estado { AUSENTE, PRESENTE, INVALIDO }

    static <T, J> CampoFiscalJson<J> de(CampoFiscal<T> campo, Function<T, J> paraJson) {
        return switch (campo) {
            case Ausente<T> _ -> new CampoFiscalJson<>(Estado.AUSENTE, null, null, null);
            case Presente<T>(var valor) -> new CampoFiscalJson<>(Estado.PRESENTE, paraJson.apply(valor), null, null);
            case Invalido<T>(var bruto, var motivo) -> new CampoFiscalJson<>(Estado.INVALIDO, null, bruto, motivo);
        };
    }
}
