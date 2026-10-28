package split.pxe.service;

import java.util.List;

/**
 * O que chega mas não vira fato para o oracle-service. Cada caso pede um tratamento diferente:
 * a ignorada é esperada e só se conta; a malformada precisa de alguém olhando.
 */
public interface MensagensNaoAceitas {

    void ignorada(Traducao.MotivoIgnorado motivo);

    void malformada(String referencia, List<String> erros);
}
