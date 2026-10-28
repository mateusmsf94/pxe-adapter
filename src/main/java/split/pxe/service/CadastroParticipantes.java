package split.pxe.service;

import java.util.Optional;

import split.pxe.domain.tipos.CnpjRaiz;
import split.pxe.domain.tipos.Ispb;

/**
 * Cadastro de participantes: converte o ISPB (como o SPI identifica) na raiz do CNPJ
 * (como a PP identifica). É dado de referência, vindo de fora do adapter.
 */
public interface CadastroParticipantes {

    Optional<CnpjRaiz> raizCnpjDo(Ispb ispb);
}
