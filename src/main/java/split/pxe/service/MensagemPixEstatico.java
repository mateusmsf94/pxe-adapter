package split.pxe.service;

/**
 * O que chega ao adapter: duas mensagens separadas, em momentos diferentes, uma por operação.
 * Cada uma é traduzida sozinha. Juntar as duas pelo e2eId é trabalho do oracle-service.
 */
public sealed interface MensagemPixEstatico {

    /** Operação do preliminar: a iniciação. */
    record Preliminar(PayloadPreliminar payload) implements MensagemPixEstatico {}

    /** Operação da segregação: a liquidação. */
    record Segregacao(PayloadSegregacao payload) implements MensagemPixEstatico {}
}
