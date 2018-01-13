/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.values.schemagenerator.distribution;

import java.util.ArrayList;
import java.util.List;
import static me.niels.values.schemagenerator.distribution.GaussianDistribution.BINNING_PRECISION;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

/**
 *
 * @author Niels
 */
public class ZipfianDistribution extends NumericDistribution {

    public static final int BINNING_PRECISION = 64;
    public double alpha;
    public double xMin;
    public List<Double> bins;

    public ZipfianDistribution(double testparameter, double alpha, double xMin) {
        this.testparameter = testparameter;
        this.alpha = alpha;
        this.xMin = xMin;
    }

    @Override
    public String toString() {
        return "ZIPFIAN(alpha=" + alpha + ", xMin=" + xMin + ", testparameter=" + testparameter + ")";
    }

    public List<Double> getBins(int nrOfBins, int nrOfValues) {

        if (bins != null) {
            return bins;
        }
        bins = new ArrayList<Double>();
        ZipfDistribution zipf = new ZipfDistribution((int) nrOfValues, alpha);
        double targetPercentage = 0;
        for (int i = 0; i <= BINNING_PRECISION; i += 1) {
            if (zipf.cumulativeProbability(i) >= targetPercentage) {
                bins.add((double) i);
                targetPercentage += 1.0 / nrOfBins;
            }
        }
        return bins;
    }
}
