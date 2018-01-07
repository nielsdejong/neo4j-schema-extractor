/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.values.schemagenerator.distribution;

/**
 *
 * @author Niels
 */
public class GaussianDistribution extends Distribution {

    public double mu;
    public double sigma;
    public double norm;

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
}
