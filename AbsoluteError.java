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
import java.awt.*;
import java.util.function.Function;


public class AbsoluteError {

    public static void main(String[] args) {
        double a = -1, b = 1;
        int n = 75;
        XYSeriesCollection dataset = null;
        int m = 10000;
        List<Double> xValues = new ArrayList<>(n);
        List<Double> yValues = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            xValues.add(i, a + (b - a) / n * i);
            yValues.add(i, 3 * xValues.get(i) - Math.cos(xValues.get(i)) - 1);
        }
        double step = (b - a) / m;
        Function<Double, Double> lagrangePolynomial = createLagrangePolynomial(xValues, yValues);
        Function<Double, Double> newtonPolynomial = createNewtonPolynomial(xValues, yValues);
        List<Double> spline_3_2 = createSpline_3_2(xValues, yValues, step);
        List<Double> lagrange = new ArrayList<>();
        List<Double> newton = new ArrayList<>();
        for (int i = 0; i < m; i ++) {
            lagrange.add(lagrangePolynomial.apply((b - a) / m));
            newton.add(newtonPolynomial.apply((b - a) / m));
        }
        List<Double> splineDeviation = deviation(a, b, spline_3_2);
        List<Double> lagrangeDeviation = deviation(a, b, lagrange);
        List<Double> newtonDeviation = deviation(a, b, newton);
        String[] graphTitles = new String[]{"Spline_3_2", "LagrangePolynomial", "NewtonPolynomial"};
        dataset = new XYSeriesCollection();
        double steps = spline_3_2.size();
        double[][] yData = new double[3][(int) steps];
        double[] xData = new double[(int) steps];
        double x = a;
        for (int i = 0; i < steps; i++) {
            x += (double) 2 / steps;
            xData[i] = x;
            yData[0][i] = splineDeviation.get(i);
            yData[1][i] = lagrangeDeviation.get(i);
            yData[2][i] = newtonDeviation.get(i);
        }
        for (int i = 0; i < 3; i++) {
            XYSeries series = new XYSeries(graphTitles[i]);
            for (int j = 0; j < xData.length; j++) {
                series.add(xData[j], yData[i][j]);
            }
            dataset.addSeries(series);
        }
        toUI(dataset);
    }

    public static List<Double> createSpline_3_2(List<Double> x, List<Double> y, double step) {
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

    public static Function<Double, Double> createLagrangeMultipliers(List<Double> xValues, int i) {
        return (x) -> {
            double divider = 1;
            double result = 1;
            for (int j = 0; j < xValues.size(); j++) {
                if (j != i) {
                    result *= x - xValues.get(j);
                    divider *= xValues.get(i) - xValues.get(j);
                }
            }
            return result / divider;
        };
    }

    public static Function<Double, Double> createLagrangePolynomial(List<Double> xValues, List<Double> yValues) {
        List<Function<Double, Double>> lagrangeMultipliers = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            lagrangeMultipliers.add(createLagrangeMultipliers(xValues, i));
        }
        return (x) -> {
            double result = 0;
            for (int i = 0; i < xValues.size(); i++) {
                result += yValues.get(i) * lagrangeMultipliers.get(i).apply(x);
            }
            return result;
        };
    }


    public static double calculateDividedDifferences(List<Double> x, List<Double> y, int k) {
        double result = 0;
        for (int j = 0; j <= k; j++) {
            double mul = 1;
            for (int i = 0; i <= k; i++) {
                if (j != i) {
                    mul *= (x.get(j) - x.get(i));
                }
            }
            result += y.get(j) / mul;
        }
        return result;
    }

    public static Function<Double, Double> createNewtonPolynomial(List<Double> x, List<Double> y) {
        double[] divDiff = new double[x.size() - 1];
        for (int i = 1; i < x.size(); i++) {
            divDiff[i - 1] = calculateDividedDifferences(x, y, i);
        }
        return (xVal) -> {
            double result = y.get(0);
            for (int k = 1; k < x.size(); k++) {
                double mul = 1;
                for (int j = 0; j < k; j++) {
                    mul *= (xVal - x.get(j));
                }
                result += divDiff[k - 1] * mul;
            }
            return result;
        };
    }

    public static List<Double> deviation(double a, double b, List<Double> y) {
        List<Double> deviations = new ArrayList<>();
        deviations.add(Math.abs(3 * a - Math.cos(a) - 1 - y.get(0)));
        double x = a;
        int steps = y.size();
        for (int i = 1; i < steps; i++) {
            x += (b - a) / steps;
            deviations.add(Math.abs(3 * x - Math.cos(x) - 1 - y.get(i)));
        }
        return deviations;
    }

    public static void toUI(XYSeriesCollection dataset) {
        JFrame frame = new JFrame("f(x) = 3x - cos(x) - 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JFreeChart chart = ChartFactory.createXYLineChart("", "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.white);
        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(chartPanel, BorderLayout.SOUTH);
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(scrollPane);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}
