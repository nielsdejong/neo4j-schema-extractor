/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.schema;

/**
 *
 * @author Niels
 */
public class EdgeType {

    public String type;
    public int edgeCount;


    public EdgeType(String type) {
        this.type = type;
        this.edgeCount = 1;
    }

    @Override
    public String toString() {
        return type;
    }
}
