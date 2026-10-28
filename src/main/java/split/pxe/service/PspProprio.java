package split.pxe.service;

import java.util.Objects;

import split.pxe.domain.tipos.CnpjRaiz;
import split.pxe.domain.tipos.Ispb;

/** Quem somos: o PSP Recebedor Direto. */
public record PspProprio(Ispb ispb, CnpjRaiz raizCnpj) {

    public PspProprio {
        Objects.requireNonNull(ispb, "ispb");
        Objects.requireNonNull(raizCnpj, "raizCnpj");
    }
}
