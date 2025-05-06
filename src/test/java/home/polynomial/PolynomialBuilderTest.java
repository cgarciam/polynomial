package home.polynomial;

import static home.polynomial.PolynomialBuilder.build;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PolynomialBuilderTest {

    @Test
    void buildTest() {
        // 1° Number of terms. 2° Denomination 3° Variable Name
        final PolynomialFunction polynomial1 = build(3, 2, "x");
        final PolynomialFunction polynomial2 = build(2, 5, "y");
        final PolynomialFunction polynomial3 = build(5, 10, "z");
        final PolynomialFunction polynomial4 = build(4, 20, "w");
        log.info("Polynomial 1×2×3×4:\n{}",
                polynomial1.multiply(polynomial2).multiply(polynomial3).multiply(polynomial4));
    }

}