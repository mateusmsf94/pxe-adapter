package split.pxe.resource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * O que as operações devolvem a quem chamou.
 * TODO confirmar com quem chama as operações o formato da resposta e os códigos HTTP.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Resposta(Resultado resultado, String referencia, String motivo, List<String> erros) {

    public enum Resultado { ACEITO, IGNORADO, MALFORMADO, NAO_REGISTRADO }

    static Resposta aceito(String e2eId) {
        return new Resposta(Resultado.ACEITO, e2eId, null, List.of());
    }

    static Resposta ignorado(String motivo) {
        return new Resposta(Resultado.IGNORADO, null, motivo, List.of());
    }

    static Resposta malformado(String referencia, List<String> erros) {
        return new Resposta(Resultado.MALFORMADO, referencia, null, erros);
    }

    static Resposta naoRegistrado(String motivo) {
        return new Resposta(Resultado.NAO_REGISTRADO, null, motivo, List.of());
    }
}
