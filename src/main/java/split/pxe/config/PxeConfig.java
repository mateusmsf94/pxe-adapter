package split.pxe.config;

import java.util.Map;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "pxe")
public interface PxeConfig {

    /** ISPB do próprio PSP, o PSP Recebedor Direto. */
    String ispbProprio();

    /** Raiz do CNPJ do próprio PSP. */
    String raizPropria();

    /**
     * Cadastro de participantes, ISPB → raiz do CNPJ: pxe.participantes."ISPB"=raiz.
     * TODO provisório: trocar pelo cadastro real de participantes quando ele existir.
     */
    Map<String, String> participantes();
}
