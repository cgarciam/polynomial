package home.polynomial;

import static home.polynomial.PolynomialUtils.fromString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import home.aspect.AppConfig;
import home.aspect.ExecutionTimeAspect;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase de prueba para la clase {@link PolynomialService}. Esta clase contiene
 * pruebas de integración para verificar el correcto funcionamiento de la
 * aplicación de un aspecto a través de Spring. {@link Polynomial}.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { PolynomialService.class, AppConfig.class, ExecutionTimeAspect.class })
@Slf4j
class PolynomialSpringTest { // NOPMD AtLeastOneConstructor
    /**
     * Se inyecta el servicio de polinomios para realizar pruebas de integración.
     * Esto permite verificar el correcto funcionamiento del servicio y su
     * interacción con otros componentes de la aplicación.
     */
    @Autowired
    private PolynomialService polynomialService;

    @Test
    void multiplyShort() throws IOException {
        assertNotNull(polynomialService, "PolynomialService should not be null");
        final String polynomial1 = "1 + x^50";
        final String polynomial2 = "1 + y^100 + y^200";
        final Polynomial poly1 = fromString(polynomial1.replace("x", "1*x"));
        final Polynomial poly2 = fromString(polynomial2.replace("y", "1*y"));
        final Polynomial result = polynomialService.multiply(poly1, poly2);
        if (log.isDebugEnabled()) {
            log.debug("({})*({}) = {}", polynomial1, polynomial2, "1 + x^50 + y^100 + x^50*y^100 + y^200 + x^50*y^200");
        }
        result.printOrderedByDegree();
        // 1 + y^100 + y^200 + x^50 + x^50*y^100 + x^50*y^200
        assert "1.0 + 1.0*y^100 + 1.0*y^200 + 1.0*x^50 + 1.0*x^50*y^100 + 1.0*x^50*y^200".equals(result.toString());
    }

}