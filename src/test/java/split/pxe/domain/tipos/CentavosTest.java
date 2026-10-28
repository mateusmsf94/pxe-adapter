package split.pxe.domain.tipos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CentavosTest {

    @ParameterizedTest
    @CsvSource({
            "1234.5,  123450",
            "1000.00, 100000",
            "0.01,    1",
            "10,      1000",
            "0,       0",
            "9999999999999999.99, 999999999999999999"})
    void deDecimalLeComPontoEAteDuasCasas(String texto, long esperado) {
        assertEquals(new Centavos(esperado), Centavos.deDecimal(texto));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"-10.00", "53,10", "1.234", "1e3", " 1.00", "1.", ".50", "R$1,00", "10000000000000000"})
    void deDecimalRecusaOQueNaoEDecimalDoManual(String texto) {
        assertThrows(IllegalArgumentException.class, () -> Centavos.deDecimal(texto));
    }
}
