import Jama.Matrix;
import Jama.QRDecomposition;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Spline_3_2 {

    public static void main(String[] args) {
        double a = -1, b = 1;
        int ind = -1;
        int[] N = new int[]{100, 250, 500, 1000};
        String[][] data = new String[16][3];
        String[] titles = new String[]{"n", "m", "RL_3_2"};
        XYSeriesCollection dataset = null;
        int sp_n = 0;
        int sp_m = 0;
        for (int n : N) {
            int[] M = new int[]{1000, 1500, 2000, 2500};
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
                double step = (b - a) / m;
                Spline_3_2 spline = new Spline_3_2(xValues, yValues);
                List<Double> spline_3_2 = new ArrayList<>(m + 1);
                double int_x = a;
                int index = 0;
                while (int_x <= b) {
                    spline_3_2.add(index, spline.interpolate(int_x));
                    int_x += step;
                    index += 1;
                }
                data[ind][2] = String.valueOf(maximumDeviation(a, b, spline_3_2));

                if (n == 100 && m == 1000) {
                    sp_n = n;
                    sp_m = m;
                    String[] graphTitles = new String[]{"f(x)", "Spline_3_2"};
                    dataset = new XYSeriesCollection();
                    double steps = spline_3_2.size();
                    double[][] yData = new double[2][(int) steps];
                    double[] xData = new double[(int) steps];
                    double x = a;
                    for (int i = 0; i < steps; i++) {
                        x += (double) 2 / steps;
                        xData[i] = x;
                        yData[0][i] = 3 * x - Math.cos(x) - 1;
                        yData[1][i] = spline_3_2.get(i);
                    }
                    for (int i = 0; i < 2; i++) {
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

    private final double[] x;
    private final double[] a;
    private final double[] b;
    private final double[] c;
    private final double[] d;

    public Spline_3_2(double[] x, double[] y) {
        this.x = x;
        int n = x.length;
        double[] h = new double[n - 1];
        a = new double[n - 1];
        b = new double[n - 1];
        c = new double[n - 1];
        d = new double[n - 1];

        for (int i = 0; i < n - 1; i++) {
            h[i] = x[i + 1] - x[i];
        }

        Matrix H = new Matrix(n, n);
        Matrix Y = new Matrix(n, 1);

        for (int i = 1; i < n - 1; i++) {
            H.set(i, i - 1, h[i - 1]);
            H.set(i, i, 2 * (h[i - 1] + h[i]));
            H.set(i, i + 1, h[i]);
            Y.set(i, 0, 6 * ((y[i + 1] - y[i]) / h[i] - (y[i] - y[i - 1]) / h[i - 1]));
        }

        H.set(0, 0, 1);
        H.set(n - 1, n - 1, 1);

        QRDecomposition decomp = new QRDecomposition(H);
        Matrix X = decomp.solve(Y);

        X.set(0, 0, 0);
        X.set(n - 1, 0, 0);

        for (int i = 0; i < n - 1; i++) {
            a[i] = y[i];
            b[i] = (y[i + 1] - y[i]) / h[i] - X.get(i + 1, 0) * h[i] / 6 - X.get(i, 0) / 3;
            c[i] = X.get(i + 1, 0);
            d[i] = X.get(i, 0);
        }
    }

    public double interpolate(double xi) {
        int i = 0;
        int n = x.length;
        while (i < n && xi > this.x[i]) {
            i++;
        }
        if (i == 0) {
            i = 1;
        } else if (i >= n - 1) {
            i = n - 2;
        }
        double dx = xi - this.x[i];
        return a[i] + b[i] * dx + c[i] * dx * dx + d[i] * dx * dx * dx;
    }

    public static double maximumDeviation(double a, double b, List<Double> y) {
        double result = Math.abs(3 * a - Math.cos(a) - 1 - y.get(0));
        double x = a;
        int steps = y.size();
        for (int i = 1; i < steps; i++) {
            x += (b - a) / steps;
            result = Math.max(result, Math.abs(3 * x - Math.cos(x) - 1 - y.get(i)));
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