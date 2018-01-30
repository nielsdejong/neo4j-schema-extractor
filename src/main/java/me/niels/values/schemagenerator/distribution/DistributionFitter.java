/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.values.schemagenerator.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.peterbloem.powerlaws.Continuous;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

/**
 * Attempts to fit a set of data points to either a normal, Gaussian or Zipfian
 * distribution. Picks the best distribution based on mean square error or
 * standard deviation.
 *
 * @author Niels
 */
public class DistributionFitter {

    public static final int MAX_GAUSSIAN_FITTING_ITERATIONS = 256;
    public static final int ZIPFIAN_UNIQUE_VALUE_LIMIT = 1000;

    /**
     * Fits data to a normal, Gaussian and Zipfian distribution. Returns the
     * best fitting distribution based on the Kolmogorov Smirnov statistic.
     *
     * @param values
     * @return a fitting distribution.
     */
    public static NumericDistribution fit(List<Double> values) {

        NumericDistribution uniform = fitUniform(values);
        NumericDistribution bestFit = uniform;

        if (values.size() >= 3) {
            NumericDistribution gaussian = fitGaussian(values);
            if (gaussian.testparameter < bestFit.testparameter) {
                bestFit = gaussian;
            }
        }
        if (values.size() >= 3) {
            if(values.get(0) > 1000000){
              //  return bestFit;
            }
            NumericDistribution zipfian = fitZipfian(values);
            if (zipfian.testparameter < bestFit.testparameter) {
                bestFit = zipfian;

            }
        }

        return bestFit;
    }

    public static NumericDistribution fitIntegerList(List<Integer> values) {
        List<Double> doubleValues = new ArrayList<>(values.size());
        for (int value : values) {
            doubleValues.add((double) value);
        }
        return fit(doubleValues);
    }

    private static UniformDistribution fitUniform(List<Double> values) {
        double yMin = Integer.MAX_VALUE;
        double yMax = Integer.MIN_VALUE;
        double[] valueArray = new double[values.size()];
        for (int a = 0; a < values.size(); a++) {
            valueArray[a] = values.get(a);
            if (valueArray[a] < yMin) {
                yMin = valueArray[a];
            }
            if (valueArray[a] > yMax) {
                yMax = valueArray[a];
            }
        }
        if (yMin == yMax) {
            return new UniformDistribution(0.0, yMin, yMax);
        }
        UniformRealDistribution uniform = new UniformRealDistribution(yMin, yMax);
        KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
        return new UniformDistribution(test.kolmogorovSmirnovStatistic(uniform, valueArray), yMin, yMax);
    }

    private static GaussianDistribution fitGaussian(List<Double> values) {

        WeightedObservedPoints obs = new WeightedObservedPoints();
        Map<Double, Double> valueCounts = new HashMap<>();

        for (double value : values) {
            if (valueCounts.containsKey(value)) {
                valueCounts.put(value, valueCounts.get(value) + 1);
            } else {
                valueCounts.put(value, 1.0);
            }
        }

        // Too few data points to fit a gaussian distribution
        if (valueCounts.keySet().size() < 3) {
            return new GaussianDistribution(Double.MAX_VALUE, Double.NaN, Double.NaN, Double.NaN);
        }
        for (Double key : valueCounts.keySet()) {
            obs.add(key, valueCounts.get(key));
        }
        
        // Attempt to fit to gaussian distribution
        try {
            double[] parameters = GaussianCurveFitter.create().withMaxIterations(MAX_GAUSSIAN_FITTING_ITERATIONS).fit(obs.toList());

            // Extract parameters
            double norm = parameters[0];
            double mean = parameters[1];
            double sigma = parameters[2];

            // Convert list to array.
            double[] valueArray = new double[values.size()];
            for (int a = 0; a < values.size(); a++) {
                valueArray[a] = values.get(a);
            }

            // Do KS test.
            NormalDistribution normal = new NormalDistribution(mean, sigma);
            KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
            return new GaussianDistribution(test.kolmogorovSmirnovStatistic(normal, valueArray), norm, mean, sigma);
        } catch (TooManyIterationsException e) {
            // Impossible to fit
            return new GaussianDistribution(Double.MAX_VALUE, Double.NaN, Double.NaN, Double.NaN);
        }
    }

    private static ZipfianDistribution fitZipfian(List<Double> values) {
         Map<Double, Double> valueCounts = new HashMap<>();

        for (double value : values) {
            if (valueCounts.containsKey(value)) {
                valueCounts.put(value, valueCounts.get(value) + 1);
            } else {
                valueCounts.put(value, 1.0);
            }
        }
        if(valueCounts.size() > ZIPFIAN_UNIQUE_VALUE_LIMIT){
            return new ZipfianDistribution(Double.MAX_VALUE, Double.NaN, Double.NaN);
        }
        
        
        Continuous distribution = Continuous.fit(values).fit();
        if (distribution == null) {
            return new ZipfianDistribution(Double.MAX_VALUE, Double.NaN, Double.NaN);
        }
        ZipfDistribution d = new ZipfDistribution(values.size(), distribution.exponent());
        int[] distributionSample = d.sample(values.size());

        double[] valueArray = new double[values.size()];
        double[] distributionArray = new double[values.size()];
        for (int a = 0; a < values.size(); a++) {
            valueArray[a] = values.get(a);
            distributionArray[a] = distributionSample[a];
        }

        KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
        double error = test.kolmogorovSmirnovStatistic(distributionArray, valueArray);
        return new ZipfianDistribution(error, distribution.exponent(), distribution.xMin());
    }
}
