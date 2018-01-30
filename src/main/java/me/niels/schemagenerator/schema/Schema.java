/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONException;
import org.neo4j.shell.util.json.JSONObject;

/**
 *
 * @author Niels
 */
public class Schema {

    // Index node/edge types by label/type.
    public Map<String, NodeType> nodeTypes;
    public Map<String, EdgeType> edgeTypes;
    public int totalNodeCount = 0;
    public int totalEdgeCount = 0;
    public List<String> rules;

    public Schema() {
        nodeTypes = new HashMap<>();
        edgeTypes = new HashMap<>();
        rules = new ArrayList<>();
    }

    @Override
    public String toString() {
        return nodeTypes + "\n" + edgeTypes;
     }
}
