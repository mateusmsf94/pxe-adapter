package split.pxe.service;

import java.util.Objects;

import split.pxe.domain.LiquidacaoPixEstatico;
import split.pxe.domain.PixEstaticoIniciado;
import split.pxe.service.Traducao.Aceito;
import split.pxe.service.Traducao.Ignorado;
import split.pxe.service.Traducao.Malformado;

/**
 * O caso de uso: traduz a mensagem e entrega o resultado à porta certa.
 * Devolve a Traducao para quem chamou responder a origem da mensagem.
 */
public final class ProcessarMensagemPixEstatico {

    private final Tradutor tradutor;
    private final RegistroPixEstatico registro;
    private final MensagensNaoAceitas naoAceitas;

    public ProcessarMensagemPixEstatico(Tradutor tradutor, RegistroPixEstatico registro, MensagensNaoAceitas naoAceitas) {
        this.tradutor = Objects.requireNonNull(tradutor);
        this.registro = Objects.requireNonNull(registro);
        this.naoAceitas = Objects.requireNonNull(naoAceitas);
    }

    public Traducao processar(MensagemPixEstatico mensagem) {
        var traducao = tradutor.traduzir(mensagem);
        switch (traducao) {
            case Aceito(PixEstaticoIniciado iniciado) -> registro.registrarIniciado(iniciado);
            case Aceito(LiquidacaoPixEstatico liquidacao) -> registro.registrarLiquidacao(liquidacao);
            case Ignorado(var motivo) -> naoAceitas.ignorada(motivo);
            case Malformado(var referencia, var erros) -> naoAceitas.malformada(referencia, erros);
        }
        return traducao;
    }
}
