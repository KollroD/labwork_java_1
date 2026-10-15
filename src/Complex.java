import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Неизменяемое комплексное число a + bi.
 */
public final class Complex {
    public static final Complex ZERO = new Complex(0, 0);
    public static final Complex ONE = new Complex(1, 0);

    /** Одно слагаемое: необязательный знак, необязательное число, необязательная "i". */
    private static final Pattern TERM =
            Pattern.compile("([+-]?)((?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?)?(i?)");

    private final double re;
    private final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double getRe() {
        return re;
    }

    public double getIm() {
        return im;
    }

    public Complex add(Complex o) {
        return new Complex(re + o.re, im + o.im);
    }

    public Complex subtract(Complex o) {
        return new Complex(re - o.re, im - o.im);
    }

    public Complex multiply(Complex o) {
        return new Complex(re * o.re - im * o.im, re * o.im + im * o.re);
    }

    /** @throws ArithmeticException при делении на ноль */
    public Complex divide(Complex o) {
        double d = o.re * o.re + o.im * o.im;
        if (d == 0) {
            throw new ArithmeticException("division by zero");
        }
        return new Complex((re * o.re + im * o.im) / d, (im * o.re - re * o.im) / d);
    }

    public Complex negate() {
        return new Complex(-re, -im);
    }

    /** Модуль числа. */
    public double abs() {
        return Math.hypot(re, im);
    }

    /**
     * Разбирает строку вида "3", "-2.5", "2i", "-i", "1+2i", "1.5-0.5i".
     * Допускается запись "j" вместо "i". Пробелов внутри числа быть не должно.
     *
     * @throws NumberFormatException если строка не является комплексным числом
     */
    public static Complex parse(String text) {
        String s = text.trim().replace('j', 'i');
        if (s.isEmpty()) {
            throw new NumberFormatException("empty value");
        }
        double re = 0;
        double im = 0;
        int pos = 0;
        boolean first = true;
        Matcher m = TERM.matcher(s);
        while (pos < s.length()) {
            m.region(pos, s.length());
            if (!m.lookingAt() || m.end() == pos) {
                throw bad(text);
            }
            String sign = m.group(1);
            String number = m.group(2);
            boolean imaginary = !m.group(3).isEmpty();
            // после первого слагаемого знак обязателен, иначе "2i3" прочиталось бы как 2i + 3
            if ((number == null && !imaginary) || (!first && sign.isEmpty())) {
                throw bad(text);
            }
            double value = number == null ? 1 : Double.parseDouble(number);
            if (sign.equals("-")) {
                value = -value;
            }
            if (imaginary) {
                im += value;
            } else {
                re += value;
            }
            first = false;
            pos = m.end();
        }
        return new Complex(re, im);
    }

    private static NumberFormatException bad(String text) {
        return new NumberFormatException("cannot parse complex number: \"" + text + "\"");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Complex)) {
            return false;
        }
        Complex c = (Complex) o;
        return Double.compare(re, c.re) == 0 && Double.compare(im, c.im) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }

    /** Вывод с точностью до 6 знаков после запятой, без хвостовых нулей: "3", "2i", "1-0.5i". */
    @Override
    public String toString() {
        String r = format(re);
        String i = format(im);
        if (i.equals("0")) {
            return r;
        }
        String imPart = i.equals("1") ? "i" : i.equals("-1") ? "-i" : i + "i";
        if (r.equals("0")) {
            return imPart;
        }
        return r + (im > 0 ? "+" : "") + imPart;
    }

    private static String format(double v) {
        if (Math.abs(v) < 5e-7) {
            return "0";
        }
        String s = String.format(Locale.ROOT, "%.6f", v);
        int end = s.length();
        while (s.charAt(end - 1) == '0') {
            end--;
        }
        if (s.charAt(end - 1) == '.') {
            end--;
        }
        return s.substring(0, end);
    }
}
