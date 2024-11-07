import Jama.Matrix;
import java.util.HashSet;

public class InversePowerIterationMethod {
    public static void main(String[] args) {
        int n = 3;
        int m = 20;
        Matrix L = Matrix.identity(n, n).arrayTimes(Matrix.random(n, n).times(m));
        Matrix C = Matrix.random(n, n).times(m);
        Matrix A = C.inverse().times(L).times(C);
        Matrix y = Matrix.random(n, 1).times(m);
        L.print(5, 6);
        HashSet<Double> set = new HashSet<>();
        for (int i = 0; i < m; i++) {
            double o = i;
            double[] result = powerIterationMethod(A, y, o);
            if (set.add(Math.round(result[0] * 1e2) / 1e2)) {
                System.out.println("Приближение: " + o);
                System.out.printf("Собственное значение: %.6f%n", result[0]);
                System.out.println("Соответствующий собственный вектор:");
                for (int j = 1; j < n + 1; j++) {
                    System.out.printf("%.6f%n", result[j]);
                }
                System.out.println();
            }
        }
    }

    private static double[] powerIterationMethod(Matrix A, Matrix y_0, double o) {
        int n = A.getColumnDimension();
        Matrix z = y_0.times(1/y_0.normF());
        Matrix prev_z;
        Matrix y;
        Matrix m;
        double prev_o;
        double I;
        double sum_m;

        do {
            I = 0;
            sum_m = 0;
            prev_z = z.copy();
            y = A.minus(Matrix.identity(n, n).times(o)).inverse().times(prev_z);
            z = y.times(1 / y.normF());
            m = prev_z.arrayRightDivide(y);
            for (int i = 0; i < n; i++) {
                sum_m += m.get(i, 0);
                if (Math.abs(y.get(i, 0)) > 1e-8) {
                    I += i;
                }
            }
            prev_o = o;
            o += sum_m / I;
        } while (Math.abs(prev_o - o) > 1e-6 || Math.abs(prev_z.normF() - z.normF()) > 1e-6);

        double[] result = new double[n + 1];
        result[0] = o;
        for (int i = 0; i < n; i++) {
            result[i + 1] = z.get(i, 0);
        }
        return result;
    }
}