package split.pxe.domain.tipos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CnpjOuCpfTest {

    @Test
    void onzeDigitosECpf() {
        assertEquals(new Cpf("12345678909"), CnpjOuCpf.de("12345678909"));
    }

    @Test
    void quatorzeCaracteresECnpj() {
        assertEquals(new Cnpj("11222333000181"), CnpjOuCpf.de("11222333000181"));
    }

    @Test
    void cnpjAlfanumericoECnpj() {
        assertEquals(new Cnpj("12ABC34501DE35"), CnpjOuCpf.de("12ABC34501DE35"));
    }

    @Test
    void tamanhoErradoDizOQueEsperava() {
        var erro = assertThrows(IllegalArgumentException.class, () -> CnpjOuCpf.de("1234567890123"));
        assertEquals("documento com 13 caracteres; esperado 11 (CPF) ou 14 (CNPJ)", erro.getMessage());
    }

    @Test
    void nuloEAusente() {
        assertThrows(IllegalArgumentException.class, () -> CnpjOuCpf.de(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890A", "11222333/00018"})
    void erroNaoRepeteODocumento(String texto) {
        // A mensagem vai para o log das malformadas: CPF e CNPJ não podem aparecer nela.
        var erro = assertThrows(IllegalArgumentException.class, () -> CnpjOuCpf.de(texto));
        assertFalse(erro.getMessage().contains(texto), erro.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890A", "123.456.789", "11222333/00018", "11.222.333/0001-81"})
    void caracteresForaDoFormato(String texto) {
        assertThrows(IllegalArgumentException.class, () -> CnpjOuCpf.de(texto));
    }
}
