package split.pxe.domain;

import java.util.Optional;

/**
 * Formas de iniciação do Pix Estático. Só elas chegam a este adapter: o roteamento por
 * forma de iniciação acontece antes. Por isso o enum não tem as formas dos outros arranjos;
 * um PixEstaticoIniciado com forma de Pix Dinâmico ou Automático não compila.
 *
 * Enum porque as variantes não carregam dados próprios; quando carregam, é sealed + records.
 */
public enum FormaIniciacao {
    APES,
    DICT,
    MANU,
    QRES;

    public static Optional<FormaIniciacao> de(String codigo) {
        for (FormaIniciacao f : values()) {
            if (f.name().equals(codigo)) return Optional.of(f);
        }
        return Optional.empty();
    }
}
