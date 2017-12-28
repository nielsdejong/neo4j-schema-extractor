/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.graph;

/**
 *
 * @author Niels
 */
public class Property {

    public String name;
    public Object values;

    public Property(String name, Object values) {
        this.name = name;
        this.values = values;
    }
    
    @Override
    public String toString(){
        return name + ": " + values;
    }
}
