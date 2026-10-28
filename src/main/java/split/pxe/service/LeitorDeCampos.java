package split.pxe.service;

import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Lê vários campos e junta todos os erros, em vez de parar no primeiro.
 * Assim um Malformado lista tudo o que está errado na mensagem de uma vez.
 *
 * obrigatorio() devolve null quando falha. O null não sai daqui: o Tradutor
 * confere temErros() antes de montar qualquer record.
 */
final class LeitorDeCampos {

    private final List<String> erros = new ArrayList<>();

    <T> T obrigatorio(String campo, String bruto, Function<String, T> leitura) {
        if (bruto == null || bruto.isBlank()) {
            erros.add(campo + ": ausente");
            return null;
        }
        return aplicar(campo, bruto, leitura);
    }

    <T> Optional<T> opcional(String campo, String bruto, Function<String, T> leitura) {
        if (bruto == null || bruto.isBlank()) return Optional.empty();
        return Optional.ofNullable(aplicar(campo, bruto, leitura));
    }

    boolean temErros() {
        return !erros.isEmpty();
    }

    List<String> erros() {
        return List.copyOf(erros);
    }

    private <T> T aplicar(String campo, String bruto, Function<String, T> leitura) {
        try {
            return leitura.apply(bruto);
        } catch (IllegalArgumentException | DateTimeException e) {
            erros.add(campo + ": " + e.getMessage());
            return null;
        }
    }
}
