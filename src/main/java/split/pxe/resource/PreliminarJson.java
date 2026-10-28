package split.pxe.resource;

import split.pxe.service.PayloadPreliminar;

/**
 * O corpo JSON da operação do preliminar, como chega. Vira o PayloadPreliminar do service.
 *
 * TODO o contrato do payload ainda precisa ser confirmado com quem chama a operação: os nomes
 *  abaixo são os do domínio, provisórios. Tudo é texto para o Jackson não recusar valor nenhum;
 *  quem lê os campos e junta os erros é o Tradutor.
 */
public record PreliminarJson(
        String e2eId,
        String formaIniciacao,
        String valor,
        String dataHoraPagamento,
        String documentoPagador,
        String ispbPspPagador,
        String documentoRecebedor,
        String ispbPspRecebedor,
        String cnpjItp,
        String cbs,
        String ibs,
        String documentoFiscal) {

    PayloadPreliminar paraPayload() {
        return new PayloadPreliminar(e2eId, formaIniciacao, valor, dataHoraPagamento, documentoPagador, ispbPspPagador,
                documentoRecebedor, ispbPspRecebedor, cnpjItp, cbs, ibs, documentoFiscal);
    }
}
