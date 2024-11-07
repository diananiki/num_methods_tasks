import Jama.Matrix;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Random;

public class PolynomialApproximation {

    public static void main(String[] args) {
        double[] x = new double[200 + 1];
        double[] y = new double[200 + 1];
        Random rand = new Random();
        for (int i = 0; i < 200; i += 3) {
            for (int j = 0; j < 3; j ++) {
                x[i + j] = -1 + (double) i / 100;
                y[i + j] = 3 * x[i + j] - Math.cos(x[i + j] + 1) + Math.pow(-1, rand.nextInt()) * rand.nextDouble() / 10000;
            }
        }
        int degree = 5;
        String[][] data = new String[degree][4];
        String[] titles = new String[]{"Polynomial Degree (n)", "Sum of Squared Errors for Normal Equations", "Sum of Squared Errors for Orthogonal Polynomials"};
        for (int printDegree = 1; printDegree <= 5; printDegree ++) {
            XYSeriesCollection dataset = new XYSeriesCollection();
            String[] graphTitles = new String[]{"f(x)", "Normal equations", "Orthogonal polynomials", "Approximation values"};
            double[][] Y = new double[4][200 + 1];
            for (int deg = 1; deg <= degree; deg++) {
                data[deg - 1][0] = String.valueOf(deg);
                double[] polynom_1 = normalEquations(x, y, deg);
                double sse_1 = calculateSSE(y, polynom_1);
                data[deg - 1][1] = String.valueOf(sse_1);
                double[] polynom_2 = orthogonalPolynomials(x, y, deg);
                double sse_2 = calculateSSE(y, polynom_2);
                data[deg - 1][2] = String.valueOf(sse_2);
                if (deg == printDegree) {
                    for (int i = 0; i < 201; i ++) {
                        Y[0][i] = 3 * x[i] - Math.cos(x[i] + 1);
                    }
                    Y[1] = polynom_1;
                    Y[2] = polynom_2;
                    Y[3] = y;
                    for (int i = 0; i < 3; i ++) {
                        XYSeries series = new XYSeries(graphTitles[i]);
                        for (int j = 0; j < x.length; j ++) {
                            series.add(x[j], Y[i][j]);
                        }
                        dataset.addSeries(series);
                    }
                    XYSeries scatterSeries = new XYSeries(graphTitles[3]);
                    for (int j = 0; j < x.length; j ++) {
                        scatterSeries.add(x[j], Y[3][j]);
                    }
                    dataset.addSeries(scatterSeries);
                }
            }
            toUI(data, titles, dataset, printDegree);
        }
    }

    public static double[] normalEquations(double[] x, double[] y, int degree) {
        int m = x.length;
        double[][] e = new double[m][degree + 1];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= degree; j++) {
                e[i][j] = Math.pow(x[i], j);
            }
        }

        Matrix E = new Matrix(e);
        Matrix f = new Matrix(y, m);
        Matrix coefficients = (E.transpose().times(E)).inverse().times(E.transpose().times(f));
        double[] qk = new double[m];
        for (int k = 0; k <= degree; k++) {
            for (int i = 0; i < m; i++) {
                qk[i] += coefficients.get(k, 0) * Math.pow(x[i], k);
            }
        }
        return qk;
    }

    public static double[] orthogonalPolynomials(double[] x, double[] y, int degree) {
        int m = x.length;
        double[][] q = new double[m][degree + 1];
        double sum = 0;
        for (double i : x) {
            sum += i;
        }
        for (int i = 0; i < m; i++) {
            q[i][0] = 1;
            q[i][1] = x[i] - sum / m;
        }
        Matrix f = new Matrix(y, m);
        double[] coefficients = new double[degree + 1];
        for (int j = 1; j < degree; j++) {
            Matrix qj = new Matrix(q).getMatrix(0, m - 1, j, j);
            Matrix prev_qj = new Matrix(q).getMatrix(0, m - 1, j - 1, j - 1);
            double sumAlpha1 = 0, sumAlpha2 = 0;
            for (int i = 0; i < m; i ++) {
                sumAlpha1 += x[i] * Math.pow(qj.get(i, 0), 2);
                sumAlpha2 += Math.pow(qj.get(i, 0), 2);
            }
            double alpha = sumAlpha1 / sumAlpha2;
            double sumBeta1 = 0, sumBeta2 = 0;
            for (int i = 0; i < m; i ++) {
                sumBeta1 += x[i] * qj.get(i, 0) * prev_qj.get(i, 0);
                sumBeta2 += Math.pow(prev_qj.get(i, 0), 2);
            }
            double beta = sumBeta1 / sumBeta2;
            for (int i = 0; i < m; i++) {
                q[i][j + 1] = qj.get(i, 0) * x[i] - alpha * qj.get(i, 0) - beta * prev_qj.get(i, 0);
            }
        }
        for (int k = 0; k <= degree; k++) {
            double sumK1 = 0, sumK2 = 0;
            for (int i = 0; i < m; i ++) {
                sumK1 += q[i][k] * f.get(i, 0);
                sumK2 += Math.pow(q[i][k], 2);
            }
            coefficients[k] = sumK1 / sumK2;
        }
        double[] qk = new double[m];
        for (int k = 0; k <= degree; k++) {
            for (int i = 0; i < m; i++) {
                qk[i] += coefficients[k] * q[i][k];
            }
        }
        return qk;
    }

    public static double calculateSSE( double[] y, double[] polynom) {
        double sse = 0.0;
        for (int i = 0; i < y.length; i++) {
            double error = y[i] - polynom[i];
            sse += error * error;
        }
        return sse;
    }

    public static void toUI(String[][] data, String[] titles, XYSeriesCollection dataset, int degree) {
        JFrame frame = new JFrame("f(x) = 3x - cos(x + 1)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DefaultTableModel model = new DefaultTableModel(data, titles);
        JTable table = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(table);
        JFreeChart chart = ChartFactory.createXYLineChart("", "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.white);
        ChartPanel chartPanel = new ChartPanel(chart);
        JLabel label = new JLabel("Graph for degree=" + degree);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(tableScrollPane, BorderLayout.NORTH);
        contentPanel.add(label, BorderLayout.CENTER);
        contentPanel.add(chartPanel, BorderLayout.SOUTH);
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(scrollPane);
        XYPlot plot = chart.getXYPlot();
        float lineWidth = 7f;
        Stroke stroke_1 = new BasicStroke(lineWidth);
        lineWidth = 3f;
        Stroke stroke_2 = new BasicStroke(lineWidth);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            if (i == 2) {
                renderer.setSeriesStroke(i, stroke_1);
            } else {
                renderer.setSeriesStroke(i, stroke_2);
            }
            if (i == 3) {
                renderer.setSeriesLinesVisible(i, false);
                renderer.setSeriesShapesVisible(i, true);
            } else {
                renderer.setSeriesLinesVisible(i, true);
                renderer.setSeriesShapesVisible(i, false);
            }
        }
        plot.setRenderer(renderer);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}