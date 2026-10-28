package split.pxe.service;

import java.util.List;

import split.pxe.domain.FatoPixEstatico;

/**
 * O resultado de traduzir uma mensagem. Três desfechos, cada um com destino próprio:
 *
 *   Aceito      → vai para o oracle-service (e de lá, outbox → Kafka)
 *   Ignorado    → a segregação traz um status que não é final; métrica
 *   Malformado  → chegou ao adapter, mas não dá para ler; fila de erro e alerta
 *
 * Nenhum dos três é exceção: são dados, e quem chama decide com um switch.
 */
public sealed interface Traducao {

    record Aceito(FatoPixEstatico fato) implements Traducao {}

    record Ignorado(MotivoIgnorado motivo) implements Traducao {}

    record Malformado(String referencia, List<String> erros) implements Traducao {
        public Malformado {
            erros = List.copyOf(erros);
        }
    }

    sealed interface MotivoIgnorado {
        /** A segregação não traz um resultado final: ainda não liquidou nem foi rejeitada. */
        record StatusNaoFinal(String status) implements MotivoIgnorado {}
    }
}
