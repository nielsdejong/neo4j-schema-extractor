/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.distribution;

import java.util.ArrayList;
import java.util.List;
import nl.peterbloem.powerlaws.Continuous;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

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
     * best fitting distribution based on the Kolmogorov Smirnov statistic.
     *
     * @param values
     * @return a fitting distribution.
     */
    public static Distribution fit(List<Double> values) {
        Distribution uniform = fitUniform(values);
        Distribution bestFit = uniform;

        if (values.size() >= 3) {
            Distribution gaussian = fitGaussian(values);
            if (gaussian.error < bestFit.error) {
                bestFit = gaussian;
            }
        }
        if (values.size() >= 2) {
            Distribution zipfian = fitZipfian(values);
            if (zipfian.error < bestFit.error) {
                bestFit = zipfian;
                
            }   
        }
    
        return bestFit;
    }

    public static Distribution fitIntegerList(List<Integer> values) {
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
        int mean = 1;
        int sd = 1;
        double[] valueArray = new double[values.size()];
        for (int a = 0; a < values.size(); a++) {
            valueArray[a] = values.get(a);
        }
        NormalDistribution normal = new NormalDistribution(mean, sd);
        return new GaussianDistribution(Double.MAX_VALUE, 0, 0, 0);
    }

    private static ZipfianDistribution fitZipfian(List<Double> values) {
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
