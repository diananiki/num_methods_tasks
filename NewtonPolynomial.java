//7. f(x) = 3x - cos(x) - 1

import java.util.Arrays;
import java.util.function.Function;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class NewtonPolynomial {

    public static void main(String[] args) {
        double a = -1, b = 1;
        int ind = -1;
        int[] N = new int[]{3, 6, 10, 12, 13, 14, 20, 35, 75};
        String[][] data = new String[45][4];
        String[] titles = new String[]{"n", "m", "RL", "RLopt"};
        XYSeriesCollection dataset = null;
        int sp_n = 0;
        int sp_m = 0;
        for (int n : N) {
            int[] M = new int[]{500, 1000, 2000, 5000, 10000};
            for (int m : M) {
                ind += 1;
                data[ind][0] = String.valueOf(n);
                data[ind][1] = String.valueOf(m);
                double[] xValues = new double[n];
                double[] yValues = new double[n];
                for (int i = 0; i < n; i++) {
                    xValues[i] = a + (b - a) / n * i;
                    yValues[i] = 3 * xValues[i] - Math.cos(xValues[i]) - 1;
                }
                Function<Double, Double> newtonPolynomial = createNewtonPolynomial(xValues, yValues);
                data[ind][2] = String.valueOf(maximumDeviation(m, a, b, newtonPolynomial));

                double[] xOptimalValues = optimalNodes(n, a, b);
                double[] yOptimalValues = new double[n + 1];
                for (int i = 0; i < n + 1; i++) {
                    yOptimalValues[i] = 3 * xOptimalValues[i] - Math.cos(xOptimalValues[i]) - 1;
                }
                Function<Double, Double> newtonOptimalPolynomial = createNewtonPolynomial(xOptimalValues, yOptimalValues);
                data[ind][3] = String.valueOf(maximumDeviation(m, a, b, newtonOptimalPolynomial));
                if (n == 35 && m == 1000) {
                    sp_n = n;
                    sp_m = m;
                    String[] graphTitles = new String[]{"f(x)", "NewtonPolynomial", "NewtonOptimalPolynomial"};
                    dataset = new XYSeriesCollection();
                    double[][] yData = new double[3][m];
                    double[] xData = new double[m];
                    double x = a;
                    for (int i = 0; i < m; i++) {
                        x += (double) 2 / m;
                        xData[i] = x;
                        yData[0][i] = 3 * x - Math.cos(x) - 1;
                        yData[1][i] = newtonPolynomial.apply(x);
                        yData[2][i] = newtonOptimalPolynomial.apply(x);
                    }
                    for (int i = 0; i < 3; i++) {
                        XYSeries series = new XYSeries(graphTitles[i]);
                        for (int j = 0; j < xData.length; j++) {
                            series.add(xData[j], yData[i][j]);
                        }
                        dataset.addSeries(series);
                    }
                }
            }
        }
        toUI(data, titles, dataset, sp_n, sp_m);
    }

    public static double[] optimalNodes(int n, double a, double b) {
        double[] xValues = new double[n + 1];
        for (int i = 0; i < n + 1; i++) {
            xValues[i] = 0.5 * ((b - a) * Math.cos((double) (2 * i + 1) / (2 * (n + 1)) * Math.PI) + (b + a));
        }
        Arrays.sort(xValues);
        return xValues;
    }

    public static double calculateDividedDifferences(double[] x, double[] y, int k) {
        double result = 0;
        for (int j = 0; j <= k; j++) {
            double mul = 1;
            for (int i = 0; i <= k; i++) {
                if (j != i) {
                    mul *= (x[j] - x[i]);
                }
            }
            result += y[j] / mul;
        }
        return result;
    }

    public static Function<Double, Double> createNewtonPolynomial(double[] x, double[] y) {
        double[] divDiff = new double[x.length - 1];
        for (int i = 1; i < x.length; i++) {
            divDiff[i - 1] = calculateDividedDifferences(x, y, i);
        }
        return (xVal) -> {
            double result = y[0];
            for (int k = 1; k < x.length; k++) {
                double mul = 1;
                for (int j = 0; j < k; j++) {
                    mul *= (xVal - x[j]);
                }
                result += divDiff[k - 1] * mul;
            }
            return result;
        };
    }

    public static double maximumDeviation(int m, double a, double b, Function<Double, Double> function) {
        double result = Math.abs(3 * a - Math.cos(a) - 1 - function.apply(a));
        double x = a;
        for (int i = 0; i < m; i++) {
            x += (b - a) / m;
            result = Math.max(result, Math.abs(3 * x - Math.cos(x) - 1 - function.apply(x)));
        }
        return result;
    }

    public static void toUI(String[][] data, String[] titles, XYSeriesCollection dataset, int n, int m) {
        JFrame frame = new JFrame("f(x) = 3x - cos(x) - 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DefaultTableModel model = new DefaultTableModel(data, titles);
        JTable table = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(table);
        JFreeChart chart = ChartFactory.createXYLineChart("", "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.white);
        ChartPanel chartPanel = new ChartPanel(chart);
        JLabel label = new JLabel("Graph for n=" + n + ", m=" + m);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(tableScrollPane, BorderLayout.NORTH);
        contentPanel.add(label, BorderLayout.CENTER);
        contentPanel.add(chartPanel, BorderLayout.SOUTH);
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(scrollPane);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}