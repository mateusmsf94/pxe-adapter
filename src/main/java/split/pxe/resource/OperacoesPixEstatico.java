package split.pxe.resource;

import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import split.pxe.client.OracleIndisponivel;
import split.pxe.client.RecusadoPeloOracle;
import split.pxe.service.MensagemPixEstatico.Preliminar;
import split.pxe.service.MensagemPixEstatico.Segregacao;
import split.pxe.service.MensagensNaoAceitas;
import split.pxe.service.ProcessarMensagemPixEstatico;
import split.pxe.service.Traducao;
import split.pxe.service.Traducao.Aceito;
import split.pxe.service.Traducao.Ignorado;
import split.pxe.service.Traducao.Malformado;
import split.pxe.service.Traducao.MotivoIgnorado.StatusNaoFinal;

/**
 * As duas operações de entrada. Quem chama manda um payload JSON por operação, em momentos
 * diferentes: o do preliminar (a iniciação) e o da segregação (a liquidação).
 *
 * Respostas:
 *   202  Aceito ou Ignorado: o adapter tratou a mensagem
 *   422  Malformado: o payload chegou, mas os campos não servem; mandar de novo não muda nada
 *   400  o corpo nem é o JSON esperado
 *   502  o oracle-service recusou o fato (4xx)
 *   503  o oracle-service não respondeu mesmo com as retentativas; quem chamou pode tentar depois
 *
 * TODO os caminhos são provisórios: no barramento as operações não têm nome semântico, e sim um
 *  número de OP. Trocar OP_PRELIMINAR e OP_SEGREGACAO pelos números quando forem atribuídos.
 */
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OperacoesPixEstatico {

    static final String OP_PRELIMINAR = "/op/TODO-preliminar";
    static final String OP_SEGREGACAO = "/op/TODO-segregacao";

    private static final Logger LOG = Logger.getLogger(OperacoesPixEstatico.class);
    private static final int UNPROCESSABLE_CONTENT = 422;

    private final ProcessarMensagemPixEstatico processar;
    private final MensagensNaoAceitas naoAceitas;
    private final ObjectMapper json;

    OperacoesPixEstatico(ProcessarMensagemPixEstatico processar, MensagensNaoAceitas naoAceitas, ObjectMapper json) {
        this.processar = processar;
        this.naoAceitas = naoAceitas;
        this.json = json;
    }

    @POST
    @Path(OP_PRELIMINAR)
    public Response preliminar(String corpo) {
        var payload = ler(corpo, PreliminarJson.class);
        return responder(processar.processar(new Preliminar(payload.paraPayload())));
    }

    @POST
    @Path(OP_SEGREGACAO)
    public Response segregacao(String corpo) {
        var payload = ler(corpo, SegregacaoJson.class);
        return responder(processar.processar(new Segregacao(payload.paraPayload())));
    }

    private static Response responder(Traducao traducao) {
        return switch (traducao) {
            case Aceito(var fato) -> Response.accepted(Resposta.aceito(fato.e2eId().valor())).build();
            case Ignorado(StatusNaoFinal(var status)) -> Response.accepted(Resposta.ignorado("status não final: " + status)).build();
            case Malformado(var referencia, var erros) ->
                    Response.status(UNPROCESSABLE_CONTENT).entity(Resposta.malformado(referencia, erros)).build();
        };
    }

    /** O corpo é lido aqui, e não pelo framework, para o payload ilegível também chegar às MensagensNaoAceitas. */
    private <T> T ler(String corpo, Class<T> tipo) {
        try {
            T payload = json.readValue(corpo == null ? "" : corpo, tipo);
            if (payload == null) throw new PayloadIlegivel("corpo vazio");
            return payload;
        } catch (JsonProcessingException e) {
            throw new PayloadIlegivel(e.getOriginalMessage());
        }
    }

    @ServerExceptionMapper
    Response payloadIlegivel(PayloadIlegivel e) {
        var erros = List.of("payload: " + e.getMessage());
        naoAceitas.malformada("(payload ilegível)", erros);
        return Response.status(Response.Status.BAD_REQUEST).entity(Resposta.malformado(null, erros)).build();
    }

    @ServerExceptionMapper
    Response recusadoPeloOracle(RecusadoPeloOracle e) {
        LOG.errorf("fato não registrado: %s", e.getMessage());
        return Response.status(Response.Status.BAD_GATEWAY).entity(Resposta.naoRegistrado(e.getMessage())).build();
    }

    @ServerExceptionMapper
    Response oracleIndisponivel(OracleIndisponivel e) {
        return indisponivel(e);
    }

    /** Falha de rede ao chamar o oracle-service (conexão recusada, timeout). */
    @ServerExceptionMapper
    Response semConexaoComOOracle(ProcessingException e) {
        return indisponivel(e);
    }

    private static Response indisponivel(RuntimeException e) {
        LOG.errorf(e, "fato não registrado, oracle-service indisponível");
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Resposta.naoRegistrado("oracle-service indisponível; tente de novo"))
                .build();
    }
}
