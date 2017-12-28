/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.schema;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Niels
 */
public class Schema {

    // Index node/edge types by label/type.
    public Map<String, NodeType> nodeTypes;
    public Map<String, EdgeType> edgeTypes;

    public Schema() {
        nodeTypes = new HashMap<>();
        edgeTypes = new HashMap<>();
    }
}
