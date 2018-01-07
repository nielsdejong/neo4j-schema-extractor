/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.values.enumerated;

import java.util.Map;
import me.niels.schemagenerator.schema.ValueSchema;

/**
 *
 * @author Niels
 */
public class EnumDistribution extends ValueSchema {
    
    // Every string value is linked to a probability value.
    public Map<String, Double> values;
    
    public EnumDistribution(Map<String, Double> values){
        this.values = values;
    }
    
    @Override
    public String toString(){
        String result = "ENUMDISTRIBUTION(";
        for(String value : values.keySet()){
            result+= "["+value+": "+values.get(value)+"] ";
        }
        return result +")";
    }
}
