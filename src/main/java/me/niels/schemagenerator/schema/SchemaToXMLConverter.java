/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.niels.schemagenerator.schema;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import me.niels.schemagenerator.values.enumerated.EnumDistribution;
import me.niels.values.schemagenerator.distribution.GaussianDistribution;
import me.niels.values.schemagenerator.distribution.NumericDistribution;
import me.niels.values.schemagenerator.distribution.UniformDistribution;
import me.niels.values.schemagenerator.distribution.ZipfianDistribution;
import me.niels.values.schemagenerator.regex.RegularExpression;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Niels
 */
public class SchemaToXMLConverter {

    public static final String RELAX_NG_SCHEMA_URI = "";

    public static String convert(Schema schema) {
        try {
            // Create a DOM Document using Document Builders
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Build XML File with Schmema Structure
            Element schemaElement = doc.createElement("gmark");
            doc.appendChild(schemaElement);
            Attr attr = doc.createAttribute("size");
            attr.setValue("100000");
            schemaElement.setAttributeNode(attr);

            // Nodes & Edges
            Element nodes = doc.createElement("types");
            Element edges = doc.createElement("predicates");
            Map<String, Element> nodeRelationsMap = new HashMap<>();

            for (String name : schema.nodeTypes.keySet()) {
                Element node = doc.createElement("type");

                // Set name value
                Attr nodeName = doc.createAttribute("name");
                nodeName.setValue(name);
                node.setAttributeNode(nodeName);

                // Set proportion
                Element fractionWrapper = doc.createElement("count");
                Element fraction = doc.createElement("proportion");
                fraction.setTextContent("" + schema.nodeTypes.get(name).nodeCount / (double) schema.totalNodeCount);
                fractionWrapper.appendChild(fraction);
                node.appendChild(fractionWrapper);
                nodes.appendChild(node);
                Element relations = doc.createElement("relations");
                node.appendChild(relations);
                nodeRelationsMap.put(name, relations);
                
                Element properties = doc.createElement("attributes");

                for (String propertyName : schema.nodeTypes.get(name).properties.keySet()) {
                    Element property = doc.createElement("attribute");
                    property.setAttribute("name", propertyName);
                    property.setAttribute("unique", "false");
                    property.setAttribute("required", "true");
                    ValueSchema valueSchema = schema.nodeTypes.get(name).properties.get(propertyName).valueSchema;
                    Element distributionElement = null;
                    Element distributionWrapper = null;
                    if (valueSchema.getClass() == ZipfianDistribution.class) {
                        distributionWrapper = doc.createElement("numeric");
                        distributionElement = doc.createElement("zipfianDistribution");
                        distributionElement.setAttribute("alpha", "" + ((ZipfianDistribution) valueSchema).alpha);
                        distributionWrapper.appendChild(distributionElement);
                    } else if (valueSchema.getClass() == GaussianDistribution.class) {
                        distributionWrapper = doc.createElement("numeric");
                        distributionElement = doc.createElement("gaussianDistribution");
                        distributionElement.setAttribute("mu", "" + ((GaussianDistribution) valueSchema).mu);
                        distributionElement.setAttribute("sigma", "" + ((GaussianDistribution) valueSchema).sigma);
                        distributionWrapper.appendChild(distributionElement);
                    } else if (valueSchema.getClass() == UniformDistribution.class) {
                        distributionWrapper = doc.createElement("numeric");
                        distributionElement = doc.createElement("uniformDistribution");
                        distributionElement.setAttribute("min", "" + ((UniformDistribution) valueSchema).min);
                        distributionElement.setAttribute("max", "" + ((UniformDistribution) valueSchema).max);
                        distributionWrapper.appendChild(distributionElement);
                    } else if (valueSchema.getClass() == EnumDistribution.class) {
                        distributionWrapper = doc.createElement("catagorical");
                        EnumDistribution enumDistribution = (EnumDistribution)valueSchema;
                        for(String category : enumDistribution.values.keySet()){
                            Element categoryElement = doc.createElement("category");
                            categoryElement.setTextContent(category);
                            categoryElement.setAttribute("probability", ""+enumDistribution.values.get(category));
                            distributionWrapper.appendChild(categoryElement);
                        }
                    } else if (valueSchema.getClass() == RegularExpression.class) {
                        distributionWrapper = doc.createElement("regex");
                        distributionWrapper.setTextContent(((RegularExpression) valueSchema).regex.replace("\n", " "));
                    }
                    property.appendChild(distributionWrapper);
                    properties.appendChild(property);
                    node.appendChild(properties);
                }
            }
            schemaElement.appendChild(nodes);

            for (String name : schema.nodeTypes.keySet()) {

                for (Map.Entry<EdgeType, NodeType> outEdge : schema.nodeTypes.get(name).outDistributions.keySet()) {
                    Element relation = doc.createElement("relation");
                    relation.setAttribute("predicate", "" + outEdge.getKey());
                    relation.setAttribute("target", "" + outEdge.getValue());
                    Element outDistribution = doc.createElement("outDistribution");

                    NumericDistribution distribution = schema.nodeTypes.get(name).outDistributions.get(outEdge);
                    Element distributionElement = null;
                    if (distribution.getClass() == ZipfianDistribution.class) {
                        distributionElement = doc.createElement("zipfianDistribution");
                        distributionElement.setAttribute("alpha", "" + ((ZipfianDistribution) distribution).alpha);
                    } else if (distribution.getClass() == GaussianDistribution.class) {
                        distributionElement = doc.createElement("gaussianDistribution");
                        distributionElement.setAttribute("mu", "" + ((GaussianDistribution) distribution).mu);
                        distributionElement.setAttribute("sigma", "" + ((GaussianDistribution) distribution).sigma);
                    } else {
                        distributionElement = doc.createElement("uniformDistribution");
                        distributionElement.setAttribute("min", "" + ((UniformDistribution) distribution).min);
                        distributionElement.setAttribute("max", "" + ((UniformDistribution) distribution).max);
                    }
                    outDistribution.appendChild(distributionElement);
                    relation.appendChild(outDistribution);

                    nodeRelationsMap.get(name).appendChild(relation);
                }

                for (Map.Entry<NodeType, EdgeType> inEdge : schema.nodeTypes.get(name).inDistributions.keySet()) {
                    Element relation = doc.createElement("relation");
                    relation.setAttribute("predicate", "" + inEdge.getValue());
                    relation.setAttribute("target", "" + inEdge.getKey());
                    Element inDistribution = doc.createElement("inDistribution");

                    NumericDistribution distribution = schema.nodeTypes.get(name).inDistributions.get(inEdge);
                    Element distributionElement = null;
                    if (distribution.getClass() == ZipfianDistribution.class) {
                        distributionElement = doc.createElement("zipfianDistribution");
                        distributionElement.setAttribute("alpha", "" + ((ZipfianDistribution) distribution).alpha);
                    } else if (distribution.getClass() == GaussianDistribution.class) {
                        distributionElement = doc.createElement("gaussianDistribution");
                        distributionElement.setAttribute("mu", "" + ((GaussianDistribution) distribution).mu);
                        distributionElement.setAttribute("sigma", "" + ((GaussianDistribution) distribution).sigma);
                    } else {
                        distributionElement = doc.createElement("uniformDistribution");
                        distributionElement.setAttribute("min", "" + ((UniformDistribution) distribution).min);
                        distributionElement.setAttribute("max", "" + ((UniformDistribution) distribution).max);
                    }
                    inDistribution.appendChild(distributionElement);
                    relation.appendChild(inDistribution);
                    Element relations = nodeRelationsMap.get("" + inEdge.getKey());

                    relations.appendChild(relation);
                }
                

            }

            // Add edges to XML
            for (String name : schema.edgeTypes.keySet()) {
                Element edge = doc.createElement("predicate");
                edge.setAttribute("name", name);
                Element properties = doc.createElement("attributes");

                for (String propertyName : schema.edgeTypes.get(name).properties.keySet()) {
                    Element property = doc.createElement("attribute");
                    property.setAttribute("name", propertyName);
                    property.setAttribute("unique", "false");
                    property.setAttribute("required", "true");
                    ValueSchema valueSchema = schema.edgeTypes.get(name).properties.get(propertyName).valueSchema;
                    Element distributionElement = null;
                    Element distributionWrapper = null;
                    if (valueSchema.getClass() == ZipfianDistribution.class) {
                        distributionWrapper = doc.createElement("numeric");
                        distributionElement = doc.createElement("zipfianDistribution");
                        distributionElement.setAttribute("alpha", "" + ((ZipfianDistribution) valueSchema).alpha);
                        distributionWrapper.appendChild(distributionElement);
                    } else if (valueSchema.getClass() == GaussianDistribution.class) {
                        distributionWrapper = doc.createElement("numeric");
                        distributionElement = doc.createElement("gaussianDistribution");
                        distributionElement.setAttribute("mu", "" + ((GaussianDistribution) valueSchema).mu);
                        distributionElement.setAttribute("sigma", "" + ((GaussianDistribution) valueSchema).sigma);
                        distributionWrapper.appendChild(distributionElement);
                    } else if (valueSchema.getClass() == UniformDistribution.class) {
                        distributionWrapper = doc.createElement("numeric");
                        distributionElement = doc.createElement("uniformDistribution");
                        distributionElement.setAttribute("min", "" + ((UniformDistribution) valueSchema).min);
                        distributionElement.setAttribute("max", "" + ((UniformDistribution) valueSchema).max);
                        distributionWrapper.appendChild(distributionElement);
                    } else if (valueSchema.getClass() == EnumDistribution.class) {
                        distributionWrapper = doc.createElement("catagorical");
                    } else if (valueSchema.getClass() == RegularExpression.class) {
                        distributionWrapper = doc.createElement("regex");
                        distributionWrapper.setTextContent(((RegularExpression) valueSchema).regex);
                    }
                    property.appendChild(distributionWrapper);
                    properties.appendChild(property);
                    edge.appendChild(properties);
                }
                edges.appendChild(edge);
                
            }
            schemaElement.appendChild(edges);

            // TODO: Add association rules to XML
            // Convert to XML String 
            return getXMLString(doc);

        } catch (ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(SchemaToXMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String getXMLString(Document doc) throws TransformerConfigurationException, TransformerException {
        // Transform to XML Format and return as string.
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(source, result);
        return writer.toString();
    }
}
