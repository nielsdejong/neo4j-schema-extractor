/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.values.schemagenerator.distribution;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Niels
 */
public class UniformDistribution extends NumericDistribution {

    public double min;
    public double max;
    public List<Double> bins;

    public UniformDistribution(double testparameter, double min, double max) {
        this.testparameter = testparameter;
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return "UNIFORM(min=" + min + ", max=" + max + ", testparameter=" + testparameter + ")";
    }

    public List<Double> getBins(int nrOfBins) {

        if (bins != null) {
            return bins;
        }
        bins = new ArrayList<>();
        for (double a = 0; a <= 1.0; a += 1.0 / nrOfBins) {
            bins.add(min + a * (max - min));
        }
        return bins;
    }
}
