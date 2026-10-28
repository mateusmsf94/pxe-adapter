package split.pxe.domain.tipos;

import java.util.function.Function;

/**
 * Um campo fiscal como chegou na mensagem. Três estados, não dois:
 *
 *   Ausente   — não veio (omitido, null ou vazio são a mesma coisa, Manual de Integração 3.1)
 *   Presente  — veio e foi lido
 *   Invalido  — veio, mas não dá para ler (ex.: "-10.00")
 *
 * O Pix já aconteceu quando a mensagem chega. Um campo fiscal ilegível não pode
 * derrubar a leitura do pagamento: ele vira um dado (Invalido) e quem decide o
 * que isso significa para o split é o split-service, mais adiante.
 */
public sealed interface CampoFiscal<T> {

    record Ausente<T>() implements CampoFiscal<T> {}

    record Presente<T>(T valor) implements CampoFiscal<T> {}

    record Invalido<T>(String bruto, String motivo) implements CampoFiscal<T> {}

    static <T> CampoFiscal<T> ler(String bruto, Function<String, T> leitor) {
        if (bruto == null || bruto.isBlank()) return new Ausente<>();
        try {
            return new Presente<>(leitor.apply(bruto));
        } catch (IllegalArgumentException e) {
            return new Invalido<>(bruto, e.getMessage());
        }
    }
}
