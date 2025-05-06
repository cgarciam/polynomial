package home.polynomial;

import static home.polynomial.Polynomial.fromString;
import static home.polynomial.PolynomialBuilder.build;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class PolynomialTest {

    @Test
    void multiplyShort() throws IOException {
        final Logger myLogger = (Logger) LoggerFactory.getLogger(Polynomial.class);
        myLogger.setLevel(Level.TRACE);
        final String p1 = "1 + x^50";
        final String p2 = "1 + y^100 + y^200";
        final Polynomial poly1 = fromString(p1.replace("x", "1*x"));
        final Polynomial poly2 = fromString(p2.replace("y", "1*y"));
        Polynomial result;
        result = poly1.multiply(poly2);
        if (log.isDebugEnabled()) {
            log.debug("({})*({}) = {}", p1, p2, "1 + x^50 + y^100 + x^50*y^100 + y^200 + x^50*y^200");
        }
        result.printOrderedByDegree();
        result.saveOrderedByDegree("result-short.txt");
    }

    @Test
    void multiplyConstants() throws IOException {
        final String p1 = "6";
        final String p2 = "3";
        final Polynomial poly1 = fromString(p1);
        final Polynomial poly2 = fromString(p2);
        final Polynomial result = poly1.multiply(poly2);
        if (log.isDebugEnabled()) {
            log.debug("({})*({}) = {}", p1, p2, result);
        }
    }

    @Test
    @Disabled("Test disabled to avoid long execution time")
    void multiplyMiddle() throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Start {}", new Date());
        }
        final Polynomial poly1 = fromString(
                "1 + x^50 + x^100 + x^150 + x^200 + x^250 + x^300 + x^350 + x^400 + x^450 + x^500 + x^550 + x^600 + x^650 + x^700 + x^750 + x^800 + x^850 + x^900 + x^950 + x^1000 + x^1050 + x^1100 + x^1150 + x^1200 + x^1250 + x^1300 + x^1350 + x^1400 + x^1450 + x^1500 + x^1550 + x^1600 + x^1650 + x^1700 + x^1750 + x^1800 + x^1850 + x^1900 + x^1950 + x^2000 + x^2050 + x^2100 + x^2150 + x^2200 + x^2250 + x^2300 + x^2350 + x^2400 + x^2450 + x^2500 + x^2550 + x^2600 + x^2650 + x^2700 + x^2750 + x^2800 + x^2850 + x^2900 + x^2950 + x^3000"
                        .replace("x", "1*x"));
        final Polynomial poly2 = fromString(
                "1 + y^100 + y^200 + y^300 + y^400 + y^500 + y^600 + y^700 + y^800 + y^900 + y^1000 + y^1100 + y^1200 + y^1300 + y^1400 + y^1500 + y^1600 + y^1700 + y^1800 + y^1900 + y^2000"
                        .replace("y", "1*y"));
        final Polynomial poly3 = fromString("1 + z^200 + z^400 + z^600 + z^800 + z^1000".replace("z", "1*z"));
        Polynomial result;
        result = poly1.multiply(poly2);
        if (log.isDebugEnabled()) {
            log.debug("'Mid' {}", new Date());
        }
        result = result.multiply(poly3);
        result.saveOrderedByDegree("result-middle.txt");
        if (log.isDebugEnabled()) {
            log.debug("End {}", new Date());
        }
    }

    @Test
    void testCombineKeysWithoutExplicitExponent() throws IOException {
        final Polynomial polynomial = new Polynomial();

        // Claves sin exponentes explícitos
        final String key1 = "x";
        final String key2 = "y";

        // Combinar claves
        final String result = polynomial.combineKeys(key1, key2);

        // Verificar que el resultado sea correcto
        assertEquals("x*y", result, "La combinación de claves sin exponentes explícitos no es correcta");
    }

    @Test
    void testToStringWithMultipleTerms() throws IOException {
        final Polynomial polynomial = new Polynomial();

        // Agregar términos al polinomio
        polynomial.addTerm("x^2", 3.0);
        polynomial.addTerm("y", 2.0);
        polynomial.addTerm("", 5.0); // Término constante

        // Obtener la representación en cadena
        final String result = polynomial.toString();

        // Verificar que los términos estén concatenados correctamente
        assertEquals("3.0*x^2 + 2.0*y + 5.0", result, "La representación en cadena no es correcta");
    }

    /**
     * Método principal para ejecutar el programa.
     *
     * @param args argumentos de línea de comandos (no se utilizan).
     * @throws IOException si ocurre un error al leer o escribir en los archivos.
     */
    public static void main(final String... args) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Start {}", new Date());
        }

        final PolynomialFunction polynomial1 = build(500, 50,  "x");
        final PolynomialFunction polynomial2 = build(800, 100, "y");
        final PolynomialFunction polynomial3 = build(400, 200, "z");

        final String p1 = polynomial1.toString().replace(" ", "").replace("x", "1*x");
        final String p2 = polynomial2.toString().replace(" ", "").replace("x", "1*y");
        final String p3 = polynomial3.toString().replace(" ", "").replace("x", "1*z");

        if (log.isTraceEnabled()) {
            log.trace("p1: {}", p1);
            log.trace("p2: {}", p2);
            log.trace("p3: {}", p3);
        }

        final Polynomial poly1 = fromString(p1);
        final Polynomial poly2 = fromString(p2);
        final Polynomial poly3 = fromString(p3);

        Polynomial result;
        result = poly1.multiply(poly2);
        if (log.isDebugEnabled()) {
            log.debug("Middle {}", new Date());
        }
        result = result.multiply(poly3);
        // If completed without exception, save the file output to a file.
        result.saveOrderedByDegree("result-ordered-3.txt");
        if (log.isDebugEnabled()) {
            log.debug("End {}", new Date());
        }
    }

}