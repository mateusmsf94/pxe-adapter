package split.pxe.config;

import java.time.Clock;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import split.pxe.domain.tipos.CnpjRaiz;
import split.pxe.domain.tipos.Ispb;
import split.pxe.service.CadastroParticipantes;
import split.pxe.service.MensagensNaoAceitas;
import split.pxe.service.ProcessarMensagemPixEstatico;
import split.pxe.service.PspProprio;
import split.pxe.service.RegistroPixEstatico;
import split.pxe.service.Tradutor;

/** Liga o service, que não conhece CDI, às implementações das portas. */
@ApplicationScoped
public class Produtores {

    @Produces
    @Singleton
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Produces
    @Startup
    @Singleton
    PspProprio pspProprio(PxeConfig config) {
        return new PspProprio(new Ispb(config.ispbProprio()), new CnpjRaiz(config.raizPropria()));
    }

    @Produces
    @Singleton
    Tradutor tradutor(PspProprio pspProprio, CadastroParticipantes cadastro, Clock relogio) {
        return new Tradutor(pspProprio, cadastro, relogio);
    }

    @Produces
    @Singleton
    ProcessarMensagemPixEstatico processar(Tradutor tradutor, RegistroPixEstatico registro, MensagensNaoAceitas naoAceitas) {
        return new ProcessarMensagemPixEstatico(tradutor, registro, naoAceitas);
    }
}
