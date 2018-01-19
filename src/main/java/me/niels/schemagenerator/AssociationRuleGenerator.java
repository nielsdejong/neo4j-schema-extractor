/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.kevalmorabia97.FrequentItemsetGeneration;
import me.kevalmorabia97.Preprocess;
import me.kevalmorabia97.RuleGeneration;

/**
 *
 * @author Niels
 */
public class AssociationRuleGenerator {

    public static List<String> generate( List<List<String>> transactions, double minSup, double minConf ) {
        try {
            int noOfChildsInHT = 4, maxItemsPerNodeInHT = 5;
            Preprocess p = new Preprocess(transactions);
            FrequentItemsetGeneration f = new FrequentItemsetGeneration(noOfChildsInHT, maxItemsPerNodeInHT, minSup, p.noOfTransactions, p.noOfAttributes);
            RuleGeneration g = new RuleGeneration(f.freqK, f.maxLengthOfFreqItemsets, minSup, minConf, p.noOfTransactions, p.noToAttr);
            return g.rules;
        } catch (IOException ex) {
            Logger.getLogger(AssociationRuleGenerator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
