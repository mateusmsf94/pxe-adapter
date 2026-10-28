package split.pxe.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import split.pxe.domain.tipos.CamposFiscais;
import split.pxe.domain.tipos.Centavos;
import split.pxe.domain.tipos.Cnpj;
import split.pxe.domain.tipos.CnpjOuCpf;
import split.pxe.domain.tipos.CnpjRaiz;
import split.pxe.domain.tipos.E2eId;

/**
 * Um Pix Estático foi iniciado (veio no preliminar), e isto é o que veio nele.
 *
 * Todo campo obrigatório já está num tipo do domínio; o que é opcional é Optional;
 * os campos fiscais guardam o que veio, inclusive o que veio errado.
 * Ainda não diz se liquidou: isso chega depois, numa LiquidacaoPixEstatico com o mesmo e2eId.
 *
 * Pagador e recebedor podem ser PF: um Pix de PF chega do mesmo jeito. Concluir que PF
 * significa "sem split" é regra do split-service, não fato.
 */
public record PixEstaticoIniciado(
        E2eId e2eId,
        FormaIniciacao formaIniciacao,
        Centavos valorPago,
        Instant pagoEm,
        CnpjOuCpf pagador,
        CnpjOuCpf recebedor,
        Optional<Cnpj> itp,                       // Iniciador de Transação de Pagamento, quando houver
        CnpjRaiz pspPagador,
        CnpjRaiz pspRecebedorDireto,
        Optional<CnpjRaiz> pspRecebedorIndireto,
        CamposFiscais camposFiscais,
        Instant chegouEm) implements FatoPixEstatico {

    public PixEstaticoIniciado {
        Objects.requireNonNull(e2eId, "e2eId");
        Objects.requireNonNull(formaIniciacao, "formaIniciacao");
        Objects.requireNonNull(valorPago, "valorPago");
        Objects.requireNonNull(pagoEm, "pagoEm");
        Objects.requireNonNull(pagador, "pagador");
        Objects.requireNonNull(recebedor, "recebedor");
        Objects.requireNonNull(itp, "itp (use Optional.empty())");
        Objects.requireNonNull(pspPagador, "pspPagador");
        Objects.requireNonNull(pspRecebedorDireto, "pspRecebedorDireto");
        Objects.requireNonNull(pspRecebedorIndireto, "pspRecebedorIndireto (use Optional.empty())");
        Objects.requireNonNull(camposFiscais, "camposFiscais");
        Objects.requireNonNull(chegouEm, "chegouEm");
    }
}
