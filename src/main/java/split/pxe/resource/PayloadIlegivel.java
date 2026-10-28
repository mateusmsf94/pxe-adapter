package split.pxe.resource;

/** O corpo nem é o JSON esperado: não chega ao Tradutor. */
final class PayloadIlegivel extends RuntimeException {

    PayloadIlegivel(String motivo) {
        super(motivo);
    }
}
