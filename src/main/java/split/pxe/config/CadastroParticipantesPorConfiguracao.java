package split.pxe.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import split.pxe.domain.tipos.CnpjRaiz;
import split.pxe.domain.tipos.Ispb;
import split.pxe.service.CadastroParticipantes;

/**
 * TODO provisório: o cadastro de participantes vem da configuração (PxeConfig.participantes).
 *  Trocar pelo cadastro real de participantes quando ele existir.
 *
 * Lê o mapa na subida, para um ISPB ou raiz fora do formato derrubar a aplicação, não a primeira mensagem.
 */
@Startup
@Singleton
public class CadastroParticipantesPorConfiguracao implements CadastroParticipantes {

    private final Map<Ispb, CnpjRaiz> raizes;

    CadastroParticipantesPorConfiguracao(PxeConfig config) {
        var lidas = new HashMap<Ispb, CnpjRaiz>();
        config.participantes().forEach((ispb, raiz) -> lidas.put(new Ispb(ispb), new CnpjRaiz(raiz)));
        this.raizes = Map.copyOf(lidas);
    }

    @Override
    public Optional<CnpjRaiz> raizCnpjDo(Ispb ispb) {
        return Optional.ofNullable(raizes.get(ispb));
    }
}
