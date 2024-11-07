import Jama.*;

public class QRDecompositionWithHessenberg {

    public static void main(String[] args) {
        int n = 5;
        int m = 20;
        Matrix L = Matrix.identity(n, n).arrayTimes(Matrix.random(n, n).times(m));
        Matrix C = Matrix.random(n, n).times(m);
        Matrix A = C.inverse().times(L).times(C);
        L.print(5, 6);
        Matrix H = hessenberg(A);
        H.print(5, 6);
        double[] result = qrDecomposition(A);
        System.out.println("Собственные числа:");
        for (int i = 0; i < n; i++) {
            System.out.printf("%.6f%n", result[i]);
        }
    }

    public static Matrix hessenberg(Matrix A) {
        int n = A.getRowDimension();
        Matrix H = A.copy();
        for (int k = 1; k < n - 1; k++) {
            Matrix a = H.getMatrix(k, n - 1, k + 1, k + 1);
            double[] v = new double[a.getRowDimension()];
            v[0] = Math.signum(a.get(0, 0)) * a.norm2();
            for (int i = 0; i < a.getRowDimension(); i++) {
                v[i] += a.get(i, 0);
            }
            double normV = new Matrix(v, 1).norm2();
            for (int i = 0; i < v.length; i++) {
                v[i] /= normV;
            }
            Matrix vMatrix = new Matrix(v, 1).transpose();
            Matrix subMatrix1 = H.getMatrix(k, n - 1, k - 1, n - 1);
            Matrix subMatrix2 = H.getMatrix(0, n - 1, k, n - 1);
            H.setMatrix(k, n - 1, k - 1, n - 1, subMatrix1.minus(vMatrix.times(vMatrix.transpose().times(subMatrix1))));
            H.setMatrix(0, n - 1, k, n - 1, subMatrix2.minus(subMatrix2.times(vMatrix).times(vMatrix.transpose())));
            for (int i = k + 1; i < n; i++) {
                H.set(i, k - 1, 0);
            }
        }
        return H;
    }

    public static double[] qrDecomposition(Matrix B){
        int n = B.getRowDimension();
        int k = n - 1;
        double[] res = new double[n];
        while (k > 0) {
            B = B.getMatrix(0, k, 0, k);
            double prev_b = B.get(k, k);
            Matrix b = Matrix.identity(k + 1, k + 1).times(prev_b);
            QRDecomposition qrDecomposition = new QRDecomposition(B.minus(b));
            Matrix Q = qrDecomposition.getQ();
            Matrix R = qrDecomposition.getR();
            B = R.times(Q).plus(b);
            double new_b = B.get(k, k);
            if (Math.abs(B.get(k, k - 1)) < 1e-8 && Math.abs(prev_b - new_b) < Math.abs(prev_b) / 3){
                res[n - k - 1] = B.get(k, k);
                k -= 1;
            }
        }
        res[n - 1] = B.get(0, 0);
        return res;
    }
}