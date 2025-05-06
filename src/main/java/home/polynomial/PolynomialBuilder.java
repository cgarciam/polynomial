package home.polynomial;

import static org.apache.commons.lang3.ArrayUtils.setAll;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

import lombok.extern.slf4j.Slf4j;

/** Problema de la mezcla.
 * @see https://es.slideshare.net/slideshow/acerca-de-los-algoritmos-de-mezcla-en-cajeros-automticos-76919693/76919693 */
@Slf4j
//@SuppressWarnings({"PMD.ShortVariable", "PMD.ShortClassName", "PMD.OnlyOneReturn", "PMD.CommentSize"})
public final class PolynomialBuilder {

    private PolynomialBuilder() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    /**
     * Build a polynomial function.<br/>
     * For example:<br/>
     * build(3, 50,  "x") gives as response 1 + x^50 + x^100 + x^150<br/>
     * build(4, 100, "y") gives as response 1 + y^100 + y^200 + y^300 + y^400<br/>
     * @param n The size of the polynomial coefficients array.
     * @param k The step of the polynomial.
     * @param variableName The name of the variable.
     * @return The polynomial function.
     */
    public static PolynomialFunction build(final int n, final int k, final String variableName) {
        final double[] coefficients = start(n, k);
        final int degree = n * k;
        final PolynomialFunction polynomial = new PolynomialFunction(toPrimitive(getCoefficients(degree, coefficients)));
        final String logFormatter = "Polynomial from ({}, {}):\n{}";
        final String polynomialString = polynomial.toString().replace("x", variableName);
        log.info(logFormatter, n, k, polynomialString);
        return polynomial;
    }

    /**
     * Get the coefficients of the polynomial.<br/>
     * For example:<br/>
     * getCoefficients(6, {0,2,4,6}) gives as response {1,0,1,0,1,0,1}<br/>
     * getCoefficients(12, {0,3,6,9,12}) gives as response {1,0,0,1,0,0,1,0,0,1,0,0,1}<br/>
     * @param degree The degree of the polynomial.
     *               The degree of the polynomial is the maximum exponent of the polynomial.
     *               For example, the degree of the polynomial 1 + x^50 + x^100 + x^150 is 150.
     * @param coefficients The indexes of the coefficients of the polynomial which are different from 0 and equal to 1.
     * @return The 'expanded' coefficients of the polynomial, as required by the PolynomialFunction constructor.
    */
    private static Double[] getCoefficients(final int degree, final double... coefficients) {
        if (log.isTraceEnabled()) {
            log.trace("Degree: {}", degree);
            log.trace("Coefficients: {}", ArrayUtils.toString(coefficients));
        }
        final Double[] coeffs = new Double[degree + 1];
        setAll(coeffs, i -> {
            if (contains(coefficients, i)) {
                return 1.0;
            }
            return 0.0;
        });
        if (log.isTraceEnabled()) {
            log.trace("CoefficientsN: {}", ArrayUtils.toString(coeffs));
        }
        return coeffs;
    }

    /**
     * Start the coefficients of the polynomial.<br/>
     * For example:<br/>
     * start(10, 200) gives as response {0,200,400,600,800,1000,1200,1400,1600,1800,2000}<br/>
     * start(4,  200) gives as response {0,200,400,600,800}<br/>
     * @param n The size of the polynomial coefficients array.
     * @param k The step of the polynomial.
     * @return The coefficients of the polynomial.
     */
    private static double[] start(final int n, final int k) {
        final double[] coefficients = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            coefficients[i] = k * i; // NOSONAR
        }
        return coefficients;
    }

    private static boolean contains(final double[] coefficients, final int i) {
        for (final double coefficient : coefficients) {
            if (coefficient == i) {
                return true;
            }
        }
        return false;
    }

}