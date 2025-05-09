package home.polynomial;

import java.io.IOException;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para realizar operaciones con polinomios. Esta clase proporciona
 * métodos para multiplicar polinomios.
 */
@Component
@Slf4j
public class PolynomialService { // NOPMD AtLeastOneConstructor

    /**
     * Multiplica un polinomio con otro polinomio.
     *
     * @param tthis el primer polinomio.
     * @param other el otro polinomio.
     * @return el resultado de la multiplicación.
     * @throws IOException si ocurre un error al leer o escribir en el archivo
     *                     temporal.
     */
    public Polynomial multiply(final Polynomial first, final Polynomial other) throws IOException {
        return first.multiply(other);
    }

}