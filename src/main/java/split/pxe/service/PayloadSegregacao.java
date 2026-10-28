package split.pxe.service;

/**
 * A liquidação do Pix Estático (pacs.002 na especificação), como chega no payload JSON
 * da operação da segregação. Chega separada da iniciação, em outro momento; o e2eId é
 * o que liga as duas.
 */
public record PayloadSegregacao(
        String e2eId,
        String status,               // código do status da ordem no SPI
        String dataHoraLiquidacao) {} // ISO-8601 com offset; só interessa quando liquidou
