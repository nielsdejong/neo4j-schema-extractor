/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.distribution;

/**
 *
 * @author Niels
 */
public class GaussianDistribution extends Distribution {

    public double mu;
    public double sigma;
    public double norm;

    GaussianDistribution(double error, double norm, double mu, double sigma) {

        this.error = error;
        this.norm = norm;
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public String toString() {
        return "GAUSSIAN(norm=" + norm + ", mu=" + mu + ", sigma=" + sigma + ", error=" + error;
    }
}
