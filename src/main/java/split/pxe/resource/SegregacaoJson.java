package split.pxe.resource;

import split.pxe.service.PayloadSegregacao;

/**
 * O corpo JSON da operação da segregação, como chega. Vira o PayloadSegregacao do service.
 *
 * TODO o contrato do payload ainda precisa ser confirmado com quem chama a operação: os nomes
 *  abaixo são os do domínio, provisórios. Tudo é texto; quem lê os campos é o Tradutor.
 */
public record SegregacaoJson(
        String e2eId,
        String status,
        String dataHoraLiquidacao) {

    PayloadSegregacao paraPayload() {
        return new PayloadSegregacao(e2eId, status, dataHoraLiquidacao);
    }
}
