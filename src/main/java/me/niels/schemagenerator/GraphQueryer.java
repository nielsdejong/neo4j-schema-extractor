package me.niels.schemagenerator;

import org.neo4j.queryexecutor.CypherExecutor;
import org.neo4j.queryexecutor.BoltCypherExecutor;
import org.neo4j.helpers.collection.Iterators;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.neo4j.helpers.collection.MapUtil.map;

public class GraphQueryer {

    private final CypherExecutor cypher;
    
    public GraphQueryer(String uri) {
        cypher = createCypherExecutor(uri);
    }

    private CypherExecutor createCypherExecutor(String uri) {
        try {
            String auth = new URL(uri.replace("bolt","http")).getUserInfo();
            if (auth != null) {
                String[] parts = auth.split(":");
                return new BoltCypherExecutor(uri,parts[0],parts[1]);
            }
            return new BoltCypherExecutor(uri);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Neo4j-ServerURL " + uri);
        }
    }

    public Iterable<Map<String, Object>> getNodes(){
        return Iterators.asCollection(cypher.query("MATCH (n) RETURN ID(n)+labels(n),n", map("ID+LABEL", "PROPERTIES")));
    }
    
    public Iterable<Map<String, Object>> getEdges(){
         return Iterators.asCollection(cypher.query("match (n)-[r]->(m) where n<>m return ID(r)+\" \"+TYPE(r),r", map("ID+TYPE","PROPERTIES")));
    }
   
    public Iterable<Map<String, Object>> getStructure(){
         return Iterators.asCollection(cypher.query("match (n)-[r]->(m) where n<>m return ID(n), ID(r)+\" \"+ID(m)", map("SOURCE","REL. + DEST.")));
    }
}
