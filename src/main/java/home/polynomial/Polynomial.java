package home.polynomial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Clase que representa un polinomio y permite realizar operaciones
 * como multiplicación y simplificación.
 */
@Slf4j
//@SuppressWarnings({"PMD.AssignmentInOperand", "PMD.AvoidLiteralsInIfCondition", "PMD.OnlyOneReturn", "PMD.UseTryWithResources"})
public class Polynomial {
    /** Separador del exponente en la clave del término. */
    private static final String CARET = "\\^";
    /** Separador para los factores de un término. */
    private static final String FACTOR_SEP = "\\*";
    /** Archivo temporal para almacenar los términos del polinomio. */
    private final File tempFile;

    /**
     * Constructor que crea un archivo temporal para almacenar
     * los términos del polinomio.
     *
     * @throws IOException si ocurre un error al crear el archivo temporal.
     */
    /*default*/ Polynomial() throws IOException {
        tempFile = File.createTempFile("polynomial", ".tmp");
        if (log.isTraceEnabled()) {
            log.trace("Archivo temporal creado: {}", tempFile.getAbsolutePath());
        }
        this.tempFile.deleteOnExit();
    }

    /**
     * Agrega un término al polinomio.
     *
     * @param key        la clave del término (ejemplo: "x^2*y^3").
     * @param coefficient el coeficiente del término.
     * @throws IOException si ocurre un error al escribir en el archivo temporal.
     */
    /*default*/ void addTerm(final String key, final double coefficient) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(Paths.get(tempFile.getAbsolutePath()), 
                java.nio.file.StandardOpenOption.APPEND))) {
            writer.write(key + "=" + coefficient);
            writer.newLine();
        }
    }

    /**
     * Multiplica este polinomio con otro polinomio.
     *
     * @param other el otro polinomio.
     * @return el resultado de la multiplicación.
     * @throws IOException si ocurre un error al leer o escribir
     *         en el archivo temporal.
     */
    public Polynomial multiply(final Polynomial other) throws IOException {
        final Polynomial result = new Polynomial();
        BufferedReader reader2 = Files.newBufferedReader(Paths.get(other.tempFile.getAbsolutePath()));
        try (BufferedReader reader1 = Files.newBufferedReader(Paths.get(this.tempFile.getAbsolutePath()))) {
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
                reader2 = Files.newBufferedReader(Paths.get(other.tempFile.getAbsolutePath())); // NO PMD AvoidInstantiatingObjectsInLoops
            }
        } finally {
            reader2.close();
        }
        result.simplify(); // Simplificar el polinomio resultante

        return result;
    }

    /**
     * Combina dos claves de términos multiplicando sus exponentes.
     *
     * @param key1 la primera clave.
     * @param key2 la segunda clave.
     * @return la clave combinada.
     */
    String combineKeys(final String key1, final String key2) { // NOPMD NOSONAR CognitiveComplexity, CyclomaticComplexity
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
            return ""; // Término independiente
        }

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
     * Imprime los términos del polinomio ordenados por grado total.
     *
     * @throws IOException si ocurre un error al leer o escribir
     *         en el archivo temporal.
     */
    public void printOrderedByDegree() throws IOException {
        final Map<String, Double> terms = getTerms();
        // Ordenar los términos por grado total
        terms.entrySet().stream().sorted((entry1, entry2) -> {
            final int degree1 = getDegree(entry1.getKey());
            final int degree2 = getDegree(entry2.getKey());
            return Integer.compare(degree1, degree2);
        }).forEach(entry -> log.debug(entry.getKey() + " = " + entry.getValue()));
    }

    /**
     * Guarda los términos del polinomio ordenados por grado total
     * en el archivo temporal.
     *
     * @param filePath la ruta del archivo donde se guardarán.
     * @throws IOException si ocurre un error al leer o escribir
     *         en el archivo temporal.
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
     * Obtiene el grado total de un término.
     *
     * @param key la clave del término.
     * @return el grado total del término.
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
     * Simplifica el polinomio combinando términos semejantes.
     *
     * @throws IOException si ocurre un error al leer o escribir
     *         en el archivo temporal.
     */
    private void simplify() throws IOException {
        final Map<String, Double> simplifiedTerms = new ConcurrentHashMap<>();
    
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tempFile.getAbsolutePath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] term = line.split("=");
                final String key = term[0];
                final double coefficient = Double.parseDouble(term[1]);
    
                // Combinar coeficientes de términos semejantes
                simplifiedTerms.merge(key, coefficient, Double::sum);
            }
        }
    
        // Reescribir el archivo temporal con los términos simplificados
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(tempFile.getAbsolutePath()))) {
            for (final Map.Entry<String, Double> entry : simplifiedTerms.entrySet()) {
                if (entry.getValue() != 0) { // Ignorar términos con coeficiente 0
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
            }
        }
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
     * Convierte el polinomio a una cadena de texto.
     *
     * @return la representación en cadena del polinomio.
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