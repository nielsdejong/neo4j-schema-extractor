/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.distribution;

import java.util.ArrayList;
import java.util.List;
import nl.peterbloem.powerlaws.Continuous;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.GaussianFitter;
import org.apache.commons.math3.fitting.PolynomialFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

/**
 * Attempts to fit a set of data points to either a normal, Gaussian or Zipfian
 * distribution. Picks the best distribution based on mean square error or
 * standard deviation.
 *
 * @author Niels
 */
public class DistributionFitter {

    /**
     * Fits data to a normal, Gaussian and Zipfian distribution. Returns the
     * best fitting distribution based on the RMSE/ST.DEV. measure.
     *
     * @param xValues
     * @param yValues
     * @return a fitting distribution.
     */
    public static Distribution fit(int[] xValues, int[] yValues) {

        Distribution uniform = fitUniform(xValues, yValues);
        Distribution bestFit = uniform;

        if (xValues.length >= 3) {
            Distribution gaussian = fitGaussian(xValues, yValues);
            if (gaussian.error < bestFit.error) {
                bestFit = gaussian;
            }
        }
        if (xValues.length >= 2) {
            Distribution zipfian = fitZipfian(xValues, yValues);
            if (zipfian.error < bestFit.error) {
                bestFit = zipfian;
            }
        }

        return bestFit;
    }

    private static UniformDistribution fitUniform(int[] xValues, int[] yValues) {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        PolynomialFitter fitter = new PolynomialFitter(optimizer);
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
        for (int a = 0; a < xValues.length; a++) {
            fitter.addObservedPoint(xValues[a], yValues[a]);

            if (yValues[a] < yMin) {
                yMin = yValues[a];
            }
            if (yValues[a] > yMax) {
                yMax = yValues[a];
            }
        }
        double[] initialGuess = {0};
        double[] func = fitter.fit(new PolynomialFunction.Parametric(), initialGuess);

        return new UniformDistribution(optimizer.getRMS(), yMin, yMax);
    }

    private static GaussianDistribution fitGaussian(int[] xValues, int[] yValues) {
        PolynomialFunction.Parametric f = new PolynomialFunction.Parametric();

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        GaussianFitter fitter = new GaussianFitter(optimizer);

        for (int a = 0; a < xValues.length; a++) {
            fitter.addObservedPoint(yValues[a], 0, xValues[a]);
        }

        double[] initialGuess = {1, xValues.length / 2, 1};
        try {

            double[] func = fitter.fit(256, new Gaussian.Parametric(), initialGuess);
            return new GaussianDistribution(Math.sqrt(func[2]), func[0], func[1], func[2]);
        } catch (TooManyEvaluationsException | NotStrictlyPositiveException e) {
            return new GaussianDistribution(Double.MAX_VALUE, 0, 0, 0);
        }
    }

    private static ZipfianDistribution fitZipfian(int[] xValues, int[] yValues) {
        List<Double> yValuesList = new ArrayList<>();
        for (int x = 0; x < xValues.length; x++) {
            for (int y = 0; y < yValues[x]; y++) {
                yValuesList.add((double)xValues[x]);
            }
        }

        Continuous distribution = Continuous.fit(yValuesList).fit();

        if (distribution == null) {
            return new ZipfianDistribution(Double.MAX_VALUE, 0);
        }

        double exponent = distribution.exponent();
        double xMin = distribution.xMin();
        
        double[] yFunction = new double[xValues.length];
        for (int x = 0; x < xValues.length; x++) {
            yFunction[x] = (1.0 / Math.pow((double) x+xMin, exponent));

        }

        return new ZipfianDistribution(RMSE(yValues, yFunction), exponent);

    }

    public static double RMSE(int[] yReal, double[] yFunction) {
        double a = 0.0;
        for (int i = 0; i < yReal.length; i++) {
            a += Math.sqrt((double) Math.abs(yReal[i] - yFunction[i]));

        }
        return Math.sqrt(a / (double) yReal.length);
    }
}
