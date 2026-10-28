package split.pxe.domain;

import java.time.Instant;

import split.pxe.domain.tipos.E2eId;

/**
 * O que o adapter produz a partir de uma mensagem. Cada mensagem vira um fato, sozinha:
 * a iniciação (preliminar) e a liquidação (segregação) chegam em momentos diferentes, e quem
 * junta as duas pelo e2eId é o oracle-service, dono das tabelas.
 *
 * É só fato, sem opinião: não diz se tem split nem quanto segregar.
 */
public sealed interface FatoPixEstatico permits PixEstaticoIniciado, LiquidacaoPixEstatico {

    E2eId e2eId();

    /** Quando a mensagem chegou ao adapter. */
    Instant chegouEm();
}
