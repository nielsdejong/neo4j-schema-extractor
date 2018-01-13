/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.values.schemagenerator.regex;

import me.niels.schemagenerator.schema.ValueSchema;

/**
 *
 * @author Niels
 */
public class RegularExpression extends ValueSchema {

    public String regex;

    public RegularExpression(String regex){
        this.regex = regex;
    }
    
       @Override
    public String toString(){
        return  "REGEX(" + regex.substring(0,Math.min(regex.length(), 100)) + ")";

    }
}
