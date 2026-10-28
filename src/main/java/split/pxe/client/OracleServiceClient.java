package split.pxe.client;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

/**
 * As duas operações do oracle-service que o adapter chama, uma por fato.
 * TODO caminhos provisórios: o contrato ainda precisa ser acordado com o oracle-service.
 */
@RegisterRestClient(configKey = "oracle-service")
@RegisterProvider(RespostasDeErroDoOracle.class)
@Path("/pix-estatico")
@Consumes(MediaType.APPLICATION_JSON)
public interface OracleServiceClient {

    @POST
    @Path("/iniciados")
    void registrarIniciado(PixEstaticoIniciadoJson corpo);

    @POST
    @Path("/liquidacoes")
    void registrarLiquidacao(LiquidacaoPixEstaticoJson corpo);
}
