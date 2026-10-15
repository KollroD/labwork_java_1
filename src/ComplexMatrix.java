/**
 * Неизменяемая матрица комплексных чисел. Все операции возвращают новую матрицу.
 */
public final class ComplexMatrix {
    /** Порог, ниже которого ведущий элемент считается нулём (относительно наибольшего элемента). */
    private static final double EPS = 1e-10;

    private final int rows;
    private final int cols;
    private final Complex[][] data;

    /** Создаёт матрицу, копируя переданный массив. */
    public ComplexMatrix(Complex[][] values) {
        if (values == null || values.length == 0 || values[0].length == 0) {
            throw new IllegalArgumentException("matrix must have at least one row and one column");
        }
        this.rows = values.length;
        this.cols = values[0].length;
        this.data = new Complex[rows][cols];
        for (int i = 0; i < rows; i++) {
            if (values[i].length != cols) {
                throw new IllegalArgumentException("all rows must have the same length");
            }
            for (int j = 0; j < cols; j++) {
                if (values[i][j] == null) {
                    throw new IllegalArgumentException("matrix element must not be null");
                }
                data[i][j] = values[i][j];
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Complex get(int row, int col) {
        return data[row][col];
    }

    public boolean isSquare() {
        return rows == cols;
    }

    /** Сумма матриц одинакового размера. */
    public ComplexMatrix add(ComplexMatrix o) {
        if (rows != o.rows || cols != o.cols) {
            throw new IllegalArgumentException("cannot add " + size() + " and " + o.size()
                    + " matrices: sizes must be equal");
        }
        Complex[][] r = new Complex[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                r[i][j] = data[i][j].add(o.data[i][j]);
            }
        }
        return new ComplexMatrix(r);
    }

    /** Произведение this * o; число столбцов this должно равняться числу строк o. */
    public ComplexMatrix multiply(ComplexMatrix o) {
        if (cols != o.rows) {
            throw new IllegalArgumentException("cannot multiply " + size() + " by " + o.size()
                    + ": columns of the first must equal rows of the second");
        }
        Complex[][] r = new Complex[rows][o.cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < o.cols; j++) {
                Complex sum = Complex.ZERO;
                for (int k = 0; k < cols; k++) {
                    sum = sum.add(data[i][k].multiply(o.data[k][j]));
                }
                r[i][j] = sum;
            }
        }
        return new ComplexMatrix(r);
    }

    /**
     * Деление this / o, понимаемое как this * o^(-1).
     * Возможно, если o квадратная и невырожденная, а число столбцов this равно её порядку.
     */
    public ComplexMatrix divide(ComplexMatrix o) {
        if (!o.isSquare()) {
            throw new IllegalArgumentException("divisor must be a square matrix, got " + o.size());
        }
        if (cols != o.rows) {
            throw new IllegalArgumentException("cannot divide " + size() + " by " + o.size()
                    + ": columns of the dividend must equal the order of the divisor");
        }
        return multiply(o.inverse());
    }

    public ComplexMatrix transpose() {
        Complex[][] r = new Complex[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                r[j][i] = data[i][j];
            }
        }
        return new ComplexMatrix(r);
    }

    /** Определитель квадратной матрицы (метод Гаусса с выбором главного элемента). */
    public Complex determinant() {
        requireSquare("determinant");
        Complex[][] a = copy();
        Complex det = Complex.ONE;
        double threshold = EPS * Math.max(1, maxAbs());
        for (int col = 0; col < rows; col++) {
            int pivot = pivotRow(a, col);
            if (a[pivot][col].abs() < threshold) {
                return Complex.ZERO;
            }
            if (pivot != col) {
                swapRows(a, pivot, col);
                det = det.negate();
            }
            det = det.multiply(a[col][col]);
            for (int r = col + 1; r < rows; r++) {
                Complex f = a[r][col].divide(a[col][col]);
                for (int c = col; c < rows; c++) {
                    a[r][c] = a[r][c].subtract(f.multiply(a[col][c]));
                }
            }
        }
        return det;
    }

    /** Обратная матрица (метод Гаусса-Жордана). */
    public ComplexMatrix inverse() {
        requireSquare("inverse");
        int n = rows;
        Complex[][] a = copy();
        Complex[][] inv = new Complex[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inv[i][j] = i == j ? Complex.ONE : Complex.ZERO;
            }
        }
        double threshold = EPS * Math.max(1, maxAbs());
        for (int col = 0; col < n; col++) {
            int pivot = pivotRow(a, col);
            if (a[pivot][col].abs() < threshold) {
                throw new ArithmeticException("matrix is singular (determinant is 0), cannot invert");
            }
            swapRows(a, pivot, col);
            swapRows(inv, pivot, col);
            Complex p = a[col][col];
            for (int c = 0; c < n; c++) {
                a[col][c] = a[col][c].divide(p);
                inv[col][c] = inv[col][c].divide(p);
            }
            for (int r = 0; r < n; r++) {
                if (r == col) {
                    continue;
                }
                Complex f = a[r][col];
                for (int c = 0; c < n; c++) {
                    a[r][c] = a[r][c].subtract(f.multiply(a[col][c]));
                    inv[r][c] = inv[r][c].subtract(f.multiply(inv[col][c]));
                }
            }
        }
        return new ComplexMatrix(inv);
    }

    private void requireSquare(String what) {
        if (!isSquare()) {
            throw new IllegalArgumentException(what + " is defined only for square matrices, got " + size());
        }
    }

    private String size() {
        return rows + "x" + cols;
    }

    private Complex[][] copy() {
        Complex[][] a = new Complex[rows][];
        for (int i = 0; i < rows; i++) {
            a[i] = data[i].clone();
        }
        return a;
    }

    private double maxAbs() {
        double max = 0;
        for (Complex[] row : data) {
            for (Complex c : row) {
                max = Math.max(max, c.abs());
            }
        }
        return max;
    }

    /** Строка (начиная с col) с наибольшим по модулю элементом в столбце col. */
    private static int pivotRow(Complex[][] a, int col) {
        int best = col;
        for (int r = col + 1; r < a.length; r++) {
            if (a[r][col].abs() > a[best][col].abs()) {
                best = r;
            }
        }
        return best;
    }

    private static void swapRows(Complex[][] a, int i, int j) {
        Complex[] t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    /** Строковое представление: столбцы выровнены по правому краю. */
    @Override
    public String toString() {
        String[][] cells = new String[rows][cols];
        int width = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = data[i][j].toString();
                width = Math.max(width, cells[i][j].length());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append("| ");
            for (int j = 0; j < cols; j++) {
                for (int k = cells[i][j].length(); k < width; k++) {
                    sb.append(' ');
                }
                sb.append(cells[i][j]).append(' ');
            }
            sb.append('|');
            if (i < rows - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
