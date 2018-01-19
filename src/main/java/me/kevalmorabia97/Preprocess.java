package me.kevalmorabia97;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

public class Preprocess {

    public Hashtable<Integer, String> noToAttr = new Hashtable<>();
    public Hashtable<String, Integer> AttrToNo = new Hashtable<>();
    public File transactionFile;
    public int noOfTransactions = 0, noOfAttributes = 0;

    public Preprocess(List<List<String>> items) throws IOException {
        preprocess(items);
    }

    public void preprocess(List<List<String>> items) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("data/bins.txt"));

        for (List<String> entry : items) {
            String s = "";
            for (int i = 0; i < entry.size(); i++) {
                s += entry.get(i);
                if (i < entry.size() - 1) {
                    s += ",";
                }
            }

            bw.write(convert(s));
            noOfTransactions++;
        }
        bw.close();
    }

    public String convert(String transaction) {
        StringTokenizer st = new StringTokenizer(transaction, ",");
        String s = "";
        ArrayList<Integer> trans = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!AttrToNo.containsKey(token)) {
                AttrToNo.put(token, noOfAttributes);
                noToAttr.put(noOfAttributes, token);
                noOfAttributes++;
            }
            trans.add(AttrToNo.get(token));
        }
        Collections.sort(trans);
        ArrayList<Integer> transWithNoDuplicates = new ArrayList<>();//considering every item atmost once
        for (int i : trans) {
            if (!transWithNoDuplicates.contains(i)) {
                transWithNoDuplicates.add(i);
            }
        }
        for (int i : transWithNoDuplicates) {
            s += i + ",";
        }
        s += "\n";
        return s;
    }
}
