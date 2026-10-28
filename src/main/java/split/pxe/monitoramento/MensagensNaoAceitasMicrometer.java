package split.pxe.monitoramento;

import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Singleton;
import split.pxe.service.MensagensNaoAceitas;
import split.pxe.service.Traducao.MotivoIgnorado;
import split.pxe.service.Traducao.MotivoIgnorado.StatusNaoFinal;

/** Ignoradas viram contador por motivo; malformadas, log estruturado. */
@Singleton
public class MensagensNaoAceitasMicrometer implements MensagensNaoAceitas {

    private static final Logger LOG = Logger.getLogger(MensagensNaoAceitasMicrometer.class);

    static final String IGNORADAS = "pxe.mensagens.ignoradas";

    private final MeterRegistry metricas;

    MensagensNaoAceitasMicrometer(MeterRegistry metricas) {
        this.metricas = metricas;
    }

    @Override
    public void ignorada(MotivoIgnorado motivo) {
        // Só o motivo vira tag: o status vem de fora e, como tag, poderia criar séries sem limite.
        switch (motivo) {
            case StatusNaoFinal(var status) -> {
                metricas.counter(IGNORADAS, "motivo", "status_nao_final").increment();
                LOG.debugf("segregação com status não final ignorada: status=%s", status);
            }
        }
    }

    /** TODO mandar para a fila de erro (e alertar) quando ela existir. Por enquanto, só o log. */
    @Override
    public void malformada(String referencia, List<String> erros) {
        MDC.put("referencia", referencia);
        MDC.put("erros", String.join(" | ", erros));
        try {
            LOG.warnf("mensagem Pix Estático malformada: referencia=%s erros=%s", referencia, erros);
        } finally {
            MDC.remove("referencia");
            MDC.remove("erros");
        }
    }
}
