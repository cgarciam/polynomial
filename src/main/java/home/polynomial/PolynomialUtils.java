package home.polynomial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Clase de utilidad para trabajar con polinomios. Esta clase proporciona
 * métodos para convertir cadenas de texto en polinomios y simplificar
 * polinomios.
 */
@Slf4j
public final class PolynomialUtils {
    /** Separador para los factores de un término. */
    private static final String FACTOR_SEP = "\\*";

    private PolynomialUtils() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Convierte una cadena de texto en un polinomio.
     *
     * @param polynomialString la cadena de texto que representa el polinomio.
     * @return el polinomio representado como un objeto Polynomial.
     * @throws IOException si ocurre un error al crear el archivo temporal.
     */
    public static Polynomial fromString(final String polynomialString) throws IOException {
        final Polynomial polynomial = new Polynomial();
        final String[] terms = polynomialString.replace(" ", "").split("(?=[+-])"); // Divide por "+" o "-"
        for (final String term : terms) {
            final String lTerm = term.trim();
            if (!lTerm.isEmpty()) {
                final String[] parts = lTerm.split(FACTOR_SEP);
                final double coefficient = Double.parseDouble(parts[0]);
                final String key = parts.length > 1 ? parts[1] : ""; // Maneja constantes
                polynomial.addTerm(key, coefficient);
            }
        }
        return polynomial;
    }

    /**
     * Simplifica el polinomio combinando términos semejantes.
     *
     * @throws IOException si ocurre un error al leer o escribir en el archivo
     *                     temporal.
     */
    // Se permite el acceso a la propiedad 'tempFile' de Polynomial
    // @SuppressWarnings("PMD.LawOfDemeter")
    /* default */ static void simplify(final Polynomial polynomial) throws IOException {
        final Map<String, Double> simplifiedTerms = new ConcurrentHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(polynomial.tempFile.getAbsolutePath()))) {
            String line;
            while ((line = reader.readLine()) != null) { // NOPMD AssignmentInOperand
                final String[] term = line.split("=");
                final String key = term[0];
                final double coefficient = Double.parseDouble(term[1]);

                // Combinar coeficientes de términos semejantes
                simplifiedTerms.merge(key, coefficient, Double::sum);
            }
        }

        // Reescribir el archivo temporal con los términos simplificados
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(polynomial.tempFile.getAbsolutePath()))) {
            for (final Map.Entry<String, Double> entry : simplifiedTerms.entrySet()) {
                if (entry.getValue() != 0) { // Ignorar términos con coeficiente 0
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
            }
        }
    }

    // Log the size of the temporary file every 'timeLog' milliseconds.
    /**
     * Registra el tamaño del archivo temporal.
     *
     * @param tempFile el archivo temporal.
     */
//    private 
    /* default */ static void logFileSize(final File tempFile) {
        if (log.isDebugEnabled()) {
            log.debug("{}\t{}", new Date(), tempFile.length());
        }
    }

}