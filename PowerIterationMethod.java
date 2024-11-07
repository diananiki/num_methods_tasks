import Jama.Matrix;

public class PowerIterationMethod {
    public static void main(String[] args) {
        int n = 3;
        int m = 20;
        Matrix L = Matrix.identity(n, n).arrayTimes(Matrix.random(n, n).times(m));
        Matrix C = Matrix.random(n, n).times(m);
        Matrix A = C.inverse().times(L).times(C);
        Matrix y = Matrix.random(n, 1).times(m);
        double[] result = powerIterationMethod(A, y);
        L.print(5, 6);
        System.out.printf("Наибольшее собственное значение: %.6f%n", result[0]);
        System.out.println("Соответствующий собственный вектор:");
        for (int i = 0; i < n; i++) {
            System.out.printf("%.6f%n", result[i + 1]);
        }
    }

    private static double[] powerIterationMethod(Matrix A, Matrix y_0) {
        int n = A.getColumnDimension();
        Matrix z = y_0.times(1/y_0.normF());
        Matrix l = new Matrix(n, 1);
        Matrix prev_z;
        Matrix y;
        Matrix prev_l;
        double I = 0;

        do {
            prev_z = z.copy();
            prev_l = l.copy();
            y = A.times(prev_z);
            z = y.times(1 / y.normF());
            l = y.arrayRightDivide(prev_z);
        } while (l.minus(prev_l).normF() > 1e-6 * Math.max(l.normF(), prev_l.normF()));

        double[] result = new double[n + 1];
        result[0] = 0;
        for (int i = 0; i < n; i++) {
            result[0] += l.get(i, 0);
            result[i + 1] = z.get(i, 0);
            if (Math.abs(prev_z.get(i, 0)) > 1e-8) {
                I += i;
            }
        }
        result[0] /=  I;
        return result;
    }
}