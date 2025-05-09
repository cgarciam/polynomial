package home.polynomial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Class that represents a polynomial and allows operations like multiplication
 * and simplification.
 */
/*
//@formatter:off
@SuppressWarnings({ // NOSONAR
    "PMD.AssignmentInOperand"
    , "PMD.AvoidLiteralsInIfCondition"
    , "PMD.OnlyOneReturn"
    , "PMD.UseTryWithResources"
})
// @formatter:on
//*/
@Slf4j
public class Polynomial {
    /** Exponent separator in the term key. */
    private static final String CARET = "\\^";
    /** Separator for the factors of a term. */
    private static final String FACTOR_SEP = "\\*";
    /** Flag to delete temporary files after multiply execution. */
    private static final boolean DELETE_ON_EXIT = true;

    /** Log time interval in milliseconds. */
    private final long timeLog;
    /** Temporary file to store the polynomial terms. */
    /* default */ final File tempFile;

    /**
     * Constructor to create a polynomial with a specified time log and a temporary
     * file to store the polynomial terms.
     *
     * @throws IOException if an error occurs while creating the temporary file.
     */
    /* default */ Polynomial(final long timeLog) throws IOException {
        tempFile = File.createTempFile("polynomial", ".tmp");
        this.timeLog = timeLog;
        if (log.isDebugEnabled()) {
            log.debug("Time Interval for Log: {}", timeLog);
        }
        if (log.isTraceEnabled()) {
            log.trace("Temporary file created: {}", tempFile.getAbsolutePath());
        }
        if (DELETE_ON_EXIT) {
            log.debug("The temporary file will be deleted at the end of the program.");
            this.tempFile.deleteOnExit();
        }
    }

    /**
     * Default constructor to create a polynomial with a default time log and a
     * temporary file to store the polynomial terms.
     *
     * @throws IOException if an error occurs while creating the temporary file.
     */
    /* default */ Polynomial() throws IOException {
        timeLog = Duration.of(10, ChronoUnit.MINUTES).toMillis();
        tempFile = File.createTempFile("polynomial", ".tmp");
        if (log.isTraceEnabled()) {
            log.trace("Temporary file created: {}", tempFile.getAbsolutePath());
        }
        if (DELETE_ON_EXIT) {
            log.debug("The temporary file will be deleted at the end of the program.");
            this.tempFile.deleteOnExit();
        }
    }

    /**
     * Adds a term to the polynomial.
     *
     * @param key         the key of the term (example: “x^2*y^3”).
     * @param coefficient the coefficient of the term (example: 3.0).
     * @throws IOException if an error occurs while writing to the temporary file.
     */
    /* default */ void addTerm(final String key, final double coefficient) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(Paths.get(tempFile.getAbsolutePath()),
                java.nio.file.StandardOpenOption.APPEND))) {
            writer.write(key + "=" + coefficient);
            writer.newLine();
        }
    }

    /**
     * Multiplies this polynomial with another polynomial and returns the result.
     *
     * @param other the other polynomial to multiply with.
     * @return the resulting polynomial after multiplication.
     * @throws IOException if an error occurs while reading or writing to the
     *                     temporary
     */
    public Polynomial multiply(final Polynomial other) throws IOException {
        final Polynomial result = new Polynomial(timeLog);
        final Timer timer = new Timer();

        // Schedule a task to log the size of the temporary file at regular time
        // intervals.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PolynomialUtils.logFileSize(result.tempFile);
            }
        }, 0, timeLog);

        BufferedReader reader2 = Files.newBufferedReader(Paths.get(other.tempFile.getAbsolutePath()));
        try (BufferedReader reader1 = Files.newBufferedReader(Paths.get(this.tempFile.getAbsolutePath()))) {
            reader2 = multiply(other, result, reader2, reader1);
        } finally {
            reader2.close();
            if (log.isDebugEnabled()) {
                log.debug("Tamaño final del archivo temporal: {} bytes", result.tempFile.length());
            }
        }
        timer.cancel();
        return result;
    }

    private BufferedReader multiply(final Polynomial other, final Polynomial result, BufferedReader reader2,
            final BufferedReader reader1) throws IOException {
        String line1;
        while ((line1 = reader1.readLine()) != null) {
            if (log.isTraceEnabled()) {
                log.trace("line1: {}", line1);
            }
            final String[] term1 = line1.split("=");
            final String key1 = term1[0];
            final double coefficient1 = Double.parseDouble(term1[1]);

            String line2;
            while ((line2 = reader2.readLine()) != null) {
                if (log.isTraceEnabled()) {
                    log.trace("line2: {}", line2);
                }
                final String[] term2 = line2.split("=");
                final String key2 = term2[0];
                final double coefficient2 = Double.parseDouble(term2[1]);

                final String newKey = combineKeys(key1, key2);
                final double newCoefficient = coefficient1 * coefficient2;
                result.addTerm(newKey, newCoefficient);
            }

            // Reset reader2 for the next term in reader1
            reader2.close();
            reader2 = Files.newBufferedReader(Paths.get(other.tempFile.getAbsolutePath())); // NOPMD
                                                                                            // AvoidReassigningParameters
        }
        return reader2;
    }

    /**
     * Combines two keys by summing the exponents of the same factors.
     *
     * @param key1 first key.
     * @param key2 second key.
     * @return the combined key.
     */
    // @SuppressWarnings({"PMD.CognitiveComplexity"})
    /* default */ String combineKeys(final String key1, final String key2) {
        final Map<String, Integer> exponents = new ConcurrentHashMap<>();

        if (!key1.isEmpty()) {
            for (final String part : key1.split(FACTOR_SEP)) {
                final String[] split = part.split(CARET);
                if (split.length == 1) {
                    exponents.put(split[0], exponents.getOrDefault(split[0], 0) + 1);
                } else {
                    exponents.put(split[0], exponents.getOrDefault(split[0], 0) + Integer.parseInt(split[1]));
                }
            }
        }

        if (!key2.isEmpty()) {
            for (final String part : key2.split(FACTOR_SEP)) {
                final String[] split = part.split(CARET);
                if (split.length == 1) {
                    exponents.put(split[0], exponents.getOrDefault(split[0], 0) + 1);
                } else {
                    exponents.put(split[0], exponents.getOrDefault(split[0], 0) + Integer.parseInt(split[1]));
                }
            }
        }

        if (exponents.isEmpty()) {
            return ""; // Independent term
        }

        return combineKeys(exponents);
    }

    private String combineKeys(final Map<String, Integer> exponents) {
        final StringBuilder result = new StringBuilder();
        for (final Map.Entry<String, Integer> entry : exponents.entrySet()) {
            if (!result.isEmpty()) {
                result.append('*');
            }
            result.append(entry.getKey());
            if (entry.getValue() > 1) {
                result.append('^').append(entry.getValue());
            }
        }

        return result.toString();
    }

    /**
     * Prints the terms of the polynomial ordered by total degree to the log.
     *
     * @throws IOException if an error occurs while reading the temporary file.
     */
    public void printOrderedByDegree() throws IOException {
        final Map<String, Double> terms = getTerms();
        // Sort the terms by total degree
        terms.entrySet().stream().sorted((entry1, entry2) -> {
            final int degree1 = getDegree(entry1.getKey());
            final int degree2 = getDegree(entry2.getKey());
            return Integer.compare(degree1, degree2);
        }).forEach(entry -> log.debug(entry.getKey() + " = " + entry.getValue()));
    }

    /**
     * Saves the terms of the polynomial ordered by total degree to a file.
     *
     * @param filePath the path to the file where the terms will be saved.
     * @throws IOException if an error occurs while reading or writing to the file.
     */
    public void saveOrderedByDegree(final String filePath) throws IOException {
        final Map<String, Double> terms = getTerms();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            // Ordenar los términos por grado total
            terms.entrySet().stream().sorted((entry1, entry2) -> {
                final int degree1 = getDegree(entry1.getKey());
                final int degree2 = getDegree(entry2.getKey());
                return Integer.compare(degree1, degree2);
            }).forEach(entry -> {
                try {
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                } catch (final IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error("Error al guardar el polinomio ordenado por grado: {}", e.getMessage());
                    }
                }
            });
        }
    }

    private Map<String, Double> getTerms() throws IOException {
        final Map<String, Double> terms = new ConcurrentHashMap<>();

        // Leer los términos del archivo temporal
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tempFile.getAbsolutePath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] term = line.split("=");
                final String key = term[0];
                final double coefficient = Double.parseDouble(term[1]);
                terms.put(key, coefficient);
            }
        }
        return terms;
    }

    /**
     * Get the total degree of a term based on its key.
     *
     * @param key the key of the term (example: “x^2*y^3”).
     * @return the total degree of the term.
     */
    private int getDegree(final String key) {
        int degree = 0;
        for (final String part : key.split(FACTOR_SEP)) {
            final String[] split = part.split(CARET);
            if (split.length == 1) {
                degree += 1; // Grado 1
            } else {
                degree += Integer.parseInt(split[1]);
            }
        }
        return degree;
    }

    /**
     * Converts the polynomial to a string representation.
     *
     * @return the string representation of the polynomial.
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tempFile.getAbsolutePath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] term = line.split("=");
                final String key = term[0];
                final double coefficient = Double.parseDouble(term[1]);
                if (coefficient != 0) { // Ignorar términos con coeficiente 0
                    if (result.length() > 0) {
                        result.append(" + ");
                    }
                    // Si el término no tiene clave (es constante), no agregar "*"
                    if (key.isEmpty()) {
                        result.append(coefficient);
                    } else {
                        result.append(coefficient).append('*').append(key);
                    }
                }
            }
        } catch (final IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Error al convertir el polinomio a cadena: {}", e.getMessage());
            }
        }
        return result.toString();
    }

}