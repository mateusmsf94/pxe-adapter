package split.pxe.service;

/**
 * A iniciação do Pix Estático (pacs.008 na especificação), como chega no payload JSON
 * da operação do preliminar.
 *
 * Tudo é texto, como veio: aqui não há regra nenhuma. Quem preenche este record a partir
 * do JSON é o resource; o nome de cada campo no payload ainda está em aberto.
 */
public record PayloadPreliminar(
        String e2eId,
        String formaIniciacao,       // só Pix Estático deveria chegar: APES, DICT, MANU, QRES
        String valor,                // decimal com ponto
        String dataHoraPagamento,    // ISO-8601 com offset
        String documentoPagador,     // CPF ou CNPJ
        String ispbPspPagador,
        String documentoRecebedor,   // CPF ou CNPJ
        String ispbPspRecebedor,     // onde o recebedor tem conta: nós ou um PSP indireto nosso
        String cnpjItp,              // preenchido = iniciado por ITP
        String cbs,
        String ibs,
        String documentoFiscal) {}
