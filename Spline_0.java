//7. f(x) = 3x - cos(x) - 1

import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class Spline_0 {

    public static void main(String[] args) {
        double a = -1, b = 1;
        int ind = -1;
        int[] N = new int[]{10, 50, 75, 100, 500, 1000, 2000, 3500, 5000};
        String[][] data = new String[45][5];
        String[] titles = new String[]{"n", "m", "RL_1_0", "RL_2_0", "RL_3_0"};
        XYSeriesCollection dataset = null;
        int sp_n = 0;
        int sp_m = 0;
        for (int n : N) {
            int[] M = new int[]{10000, 20000, 50000, 75000, 100000};
            for (int m : M) {
                ind += 1;
                data[ind][0] = String.valueOf(n);
                data[ind][1] = String.valueOf(m);
                List<Double> xValues = new ArrayList<>(n);
                List<Double> yValues = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    xValues.add(i, a + (b - a) / n * i);
                    yValues.add(i, 3 * xValues.get(i) - Math.cos(xValues.get(i)) - 1);
                }
                double step = (b - a) / m;
                List<Double> spline_1_0 = createSpline_1_0(xValues, yValues, step);
                List<Double> spline_2_0 = createSpline_2_0(xValues, yValues, step);
                List<Double> spline_3_0 = createSpline_3_0(xValues, yValues, step);
                data[ind][2] = String.valueOf(maximumDeviation(a, b, spline_1_0));
                data[ind][3] = String.valueOf(maximumDeviation(a, b, spline_2_0));
                data[ind][4] = String.valueOf(maximumDeviation(a, b, spline_3_0));

                if (n == 10 && m == 10000) {
                    sp_n = n;
                    sp_m = m;
                    String[] graphTitles = new String[]{"f(x)", "Spline_1_0", "Spline_2_0", "Spline_3_0"};
                    dataset = new XYSeriesCollection();
                    double steps = spline_3_0.size();
                    double[][] yData = new double[4][(int) steps];
                    double[] xData = new double[(int) steps];
                    double x = a;
                    for (int i = 0; i < steps; i++) {
                        x += (double) 2 / steps;
                        xData[i] = x;
                        yData[0][i] = 3 * x - Math.cos(x) - 1;
                        yData[1][i] = spline_1_0.get(i);
                        yData[2][i] = spline_2_0.get(i);
                        yData[3][i] = spline_3_0.get(i);
                    }
                    for (int i = 0; i < 4; i++) {
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

    public static List<Double> createSpline_1_0(List<Double> x, List<Double> y, double step) {
        List<Double> interpolatedY = new ArrayList<>();
        for (double i = x.get(0); i <= x.get(x.size() - 1); i += step) {
            int index = findIndex(x, i);
            double x0 = x.get(index);
            double y0 = y.get(index);
            double x1 = x.get(index + 1);
            double y1 = y.get(index + 1);
            double interpolated = y0 + ((y1 - y0) / (x1 - x0)) * (i - x0);
            interpolatedY.add(interpolated);
        }
        return interpolatedY;
    }

    public static List<Double> createSpline_2_0(List<Double> x, List<Double> y, double step) {
        List<Double> interpolatedY = new ArrayList<>();
        for (double i = x.get(0); i <= x.get(x.size() - 2); i += step) {
            int index = findIndex(x, i);
            double x0 = x.get(index);
            double y0 = y.get(index);
            double x1 = x.get(index + 1);
            double y1 = y.get(index + 1);
            double x2 = x.get(index + 2);
            double y2 = y.get(index + 2);
            double interpolated = y0 + (i - x0) * (y1 - y0) / (x1 - x0)
                    + (i - x0) * (i - x1) * (y2 - y1) / ((x2 - x1) * (x2 - x0));
            interpolatedY.add(interpolated);
        }
        return interpolatedY;
    }

    public static List<Double> createSpline_3_0(List<Double> x, List<Double> y, double step) {
        List<Double> interpolatedY = new ArrayList<>();
        for (double i = x.get(0); i <= x.get(x.size() - 3); i += step) {
            int index = findIndex(x, i);
            double x0 = x.get(index);
            double y0 = y.get(index);
            double x1 = x.get(index + 1);
            double y1 = y.get(index + 1);
            double x2 = x.get(index + 2);
            double y2 = y.get(index + 2);
            double x3 = x.get(index + 3);
            double y3 = y.get(index + 3);
            double interpolated = y1 + 0.5 * (i - x1) * m1(x0, y0, x1, y1, x2, y2)
                    + 0.5 * (i - x1) * (i - x0) * m2(x0, y0, x1, y1, x2, y2, x3, y3);
            interpolatedY.add(interpolated);
        }
        return interpolatedY;
    }

    private static int findIndex(List<Double> x, double value) {
        for (int i = 0; i < x.size() - 1; i++) {
            if (x.get(i) <= value && value <= x.get(i + 1)) {
                return i;
            }
        }
        return x.size() - 2;
    }

    private static double m1(double x0, double y0, double x1, double y1, double x2, double y2) {
        return ((y1 - y0) / (x1 - x0) + (y2 - y1) / (x2 - x1)) / 2;
    }

    private static double m2(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        return (y2 - 2 * y1 + y0) / ((x2 - x1) * (x1 - x0))
                + (y3 - 2 * y2 + y1) / ((x3 - x2) * (x2 - x1));
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