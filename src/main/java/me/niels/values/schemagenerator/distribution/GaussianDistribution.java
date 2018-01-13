/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.values.schemagenerator.distribution;

import java.util.ArrayList;
import org.apache.commons.math3.distribution.NormalDistribution;
import java.util.List;

/**
 *
 * @author Niels
 */
public class GaussianDistribution extends NumericDistribution {

    public static final double BINNING_PRECISION = 0.001;
    public double mu;
    public double sigma;
    public double norm;
    public List<Double> bins;

    GaussianDistribution(double testparameter, double norm, double mu, double sigma) {

        this.testparameter = testparameter;
        this.norm = norm;
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public String toString() {
        return "GAUSSIAN(norm=" + norm + ", mu=" + mu + ", sigma=" + sigma + ", testparameter=" + testparameter;
    }

    public List<Double> getBins(int nrOfBins) {
       
        if (bins != null) {
            return bins;
        }
        bins = new ArrayList<Double>();
        NormalDistribution normal = new NormalDistribution(mu, sigma);
        double start = mu - 4 * sigma;
        double end = mu + 4 * sigma;
        double targetPercentage = 0;
        for (double i = start; i <= end; i += sigma * BINNING_PRECISION) {
            if (normal.cumulativeProbability(start, i) >= targetPercentage) {
                bins.add(i);
                targetPercentage += 1.0 / nrOfBins;
            }
        }
        return bins;
    }
}
