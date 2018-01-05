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
public class UniformDistribution extends Distribution {

    public double min;
    public double max;

    public UniformDistribution(double error, double min, double max) {
        this.error = error;
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return "UNIFORM(min=" + min + ", max=" + max + ", error=" + error +")";
    }
}
