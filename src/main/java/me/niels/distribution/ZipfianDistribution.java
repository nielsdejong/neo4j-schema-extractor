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
public class ZipfianDistribution extends Distribution {

    public double alpha;
    public double xMin;

    public ZipfianDistribution(double error, double alpha, double xMin) {
        this.error = error;
        this.alpha = alpha;
        this.xMin = xMin;
    }

    @Override
    public String toString() {
        return "ZIPFIAN(alpha=" + alpha +", xMin="+xMin + ", error=" + error + ")";
    }
}
