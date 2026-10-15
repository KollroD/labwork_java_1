import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Консольный интерфейс калькулятора матриц комплексных чисел.
 * Текущая матрица хранится как накопленный результат: к ней можно последовательно
 * прибавлять, умножать на другие матрицы, делить на них и т.д.
 */
public class Main {
    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    /** Выбрасывается, когда поток ввода закончился. */
    private static final class InputEnded extends RuntimeException {
    }

    public static void main(String[] args) {
        System.out.println("=== Complex matrix calculator ===");
        System.out.println("Numbers: 3  -2.5  2i  -i  1+2i  1.5-0.5i  (no spaces inside a number)");
        System.out.println("Matrix rows: numbers separated by spaces.");
        try {
            run();
        } catch (InputEnded e) {
            System.out.println("\nInput closed. Bye.");
        }
    }

    private static void run() {
        ComplexMatrix current = readMatrix("First matrix", -1, -1);
        while (true) {
            System.out.println("\nCurrent matrix (" + current.getRows() + "x" + current.getCols() + "):");
            System.out.println(current);
            System.out.println("\n1) add a matrix          4) transpose");
            System.out.println("2) multiply by a matrix  5) determinant");
            System.out.println("3) divide by a matrix    6) enter a new matrix");
            System.out.println("0) exit");
            String choice = prompt("> ");
            try {
                switch (choice) {
                    case "1":
                        current = current.add(
                                readMatrix("Matrix to add", current.getRows(), current.getCols()));
                        break;
                    case "2":
                        current = current.multiply(readMatrix("Right factor", current.getCols(), -1));
                        break;
                    case "3":
                        current = current.divide(
                                readMatrix("Divisor (current * divisor^-1)", current.getCols(), current.getCols()));
                        break;
                    case "4":
                        current = current.transpose();
                        break;
                    case "5":
                        System.out.println("det = " + current.determinant());
                        break;
                    case "6":
                        current = readMatrix("New matrix", -1, -1);
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Unknown choice: " + choice);
                }
            } catch (IllegalArgumentException | ArithmeticException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Читает матрицу. Если rows/cols неотрицательны, размер задан заранее и не спрашивается.
     */
    private static ComplexMatrix readMatrix(String title, int rows, int cols) {
        System.out.println("\n" + title + ":");
        if (rows < 0) {
            rows = readPositiveInt("  rows: ");
        }
        if (cols < 0) {
            cols = readPositiveInt("  columns: ");
        }
        System.out.println("  size " + rows + "x" + cols + ", enter " + rows + " row(s) of " + cols + " number(s):");
        Complex[][] values = new Complex[rows][];
        for (int i = 0; i < rows; i++) {
            values[i] = readRow(i + 1, cols);
        }
        return new ComplexMatrix(values);
    }

    private static Complex[] readRow(int number, int cols) {
        while (true) {
            String[] tokens = prompt("  row " + number + ": ").trim().split("\\s+");
            if (tokens.length != cols) {
                System.out.println("  Expected " + cols + " number(s), got "
                        + (tokens[0].isEmpty() ? 0 : tokens.length) + ". Try again.");
                continue;
            }
            try {
                Complex[] row = new Complex[cols];
                for (int j = 0; j < cols; j++) {
                    row[j] = Complex.parse(tokens[j]);
                }
                return row;
            } catch (NumberFormatException e) {
                System.out.println("  " + e.getMessage() + ". Try again.");
            }
        }
    }

    private static int readPositiveInt(String message) {
        while (true) {
            String s = prompt(message).trim();
            try {
                int n = Integer.parseInt(s);
                if (n > 0) {
                    return n;
                }
            } catch (NumberFormatException ignored) {
                // повторим запрос ниже
            }
            System.out.println("  Please enter a positive integer.");
        }
    }

    private static String prompt(String message) {
        System.out.print(message);
        System.out.flush();
        try {
            String line = IN.readLine();
            if (line == null) {
                throw new InputEnded();
            }
            return line;
        } catch (IOException e) {
            throw new InputEnded();
        }
    }
}
