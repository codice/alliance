/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package alliance.catalog.nato.stanag4559.transformer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.UUID;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import alliance.catalog.nato.stanag4559.common.Stanag4559CommonUtils;
import alliance.catalog.nato.stanag4559.common.Stanag4559Constants;
import alliance.catalog.nato.stanag4559.common.Stanag4559ExploitationSubQualCode;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryDecompressionTech;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryType;
import alliance.catalog.nato.stanag4559.common.Stanag4559MetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ProductType;
import alliance.catalog.nato.stanag4559.common.UCO.AbsTime;
import alliance.catalog.nato.stanag4559.common.UCO.AbsTimeHelper;
import alliance.catalog.nato.stanag4559.common.UCO.DAG;
import alliance.catalog.nato.stanag4559.common.UCO.Edge;
import alliance.catalog.nato.stanag4559.common.UCO.Node;
import alliance.catalog.nato.stanag4559.common.UCO.NodeType;
import alliance.catalog.nato.stanag4559.common.UCO.RectangleHelper;
import alliance.catalog.nato.stanag4559.common.UCO.Time;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;

public class TestDAGConverter {

    private ORB orb;
    private Calendar cal;

    private static final String CARD_ID = "Card ID";
    private static final String SOURCE_PUBLISHER = "Source Publisher";
    private static final String SOURCE_LIBRARY = "SourceLibrary";
    private static final String ARCHIVE_INFORMATION = "Archive Information";
    private static final String STREAM_CREATOR = "Stream Creator";
    private static final String STREAM_STANDARD = "STANAG4609";
    private static final String STREAM_STANDARD_VER = "1.0";
    private static final String STREAM_SOURCE_URL = "http://localhost:1234/stream";
    private static final Integer STREAM_PROGRAM_ID = 3;
    private static final String CLASS_POLICY = "NATO/EU";
    private static final String CLASS_CLASSIFICATION = "UNCLASSIFIED";
    private static final String CLASS_RELEASABILITY = "NATO";
    private static final String COM_DESCRIPTION_ABSTRACT = "Product Description";
    private static final String COM_ID_MSN = "CX100";
    private static final String COM_ID_UUID = UUID.randomUUID().toString();
    private static final Integer COM_JC3ID = 1234;
    private static final String COM_LANGUAGE = "eng";
    private static final String COM_SOURCE = "TestSourceSystem";
    private static final String COM_SUBJECT_CATEGORY_TARGET = "Airfields";
    private static final String COM_TARGET_NUMBER = "123-456-7890";
    private static final String COM_TYPE = Stanag4559ProductType.COLLECTION_EXPLOITATION_PLAN.getSpecName();
    private static final String COVERAGE_COUNTRY_CD = "USA";
    private static final Double UPPER_LEFT_LAT = 5.0;
    private static final Double UPPER_LEFT_LON = 1.0;
    private static final Double LOWER_RIGHT_LAT = 1.0;
    private static final Double LOWER_RIGHT_LON = 5.0;
    private static final String EXPLOITATION_DESC = "Exploitation Info Description";
    private static final Integer EXPLOITATION_LEVEL = 0;
    private static final Boolean EXPLOITATION_AUTO_GEN = false;
    private static final String EXPLOITATION_SUBJ_QUAL_CODE = Stanag4559ExploitationSubQualCode.GOOD.toString();
    private static final String IMAGERY_CATEGORY = Stanag4559ImageryType.VIS.toString();
    private static final Integer IMAGERY_CLOUD_COVER_PCT = 35;
    private static final String IMAGERY_COMMENTS = "Imagery Comments";
    private static final String IMAGERY_DECOMPRESSION_TECH = Stanag4559ImageryDecompressionTech.C1.toString();
    private static final String IMAGERY_IDENTIFIER = "1234";
    private static final Integer IMAGERY_NIIRS = 2;
    private static final Integer IMAGERY_NUM_BANDS = 5000;
    private static final Integer IMAGERY_NUM_ROWS = 500;
    private static final Integer IMAGERY_NUM_COLS = 500;
    private static final String IMAGERY_TITLE = "Imagery Title";
    private static final String WKT_LOCATION = "POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1))";
    private boolean shouldPrintCard = true;

    @Before
    public void setUp() {
        this.orb = ORB.init();

        int year = 2016;
        int month = 01;
        int dayOfMonth = 29;
        int hourOfDay = 17;
        int minute = 05;
        int second = 10;
        cal = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
    }

    /**
     * Test the Imagery View DAG to Metacard
     *
     * NSIL_PRODUCT
     *   NSIL_APPROVAL
     *   NSIL_CARD
     *   NSIL_FILE
     *   NSIL_STREAM
     *   NSIL_METADATASECURITY
     *   NSIL_RELATED_FILE
     *   NSIL_SECURITY
     *   NSIL_PART
     *     NSIL_SECURITY
     *     NSIL_COMMON
     *     NSIL_COVERAGE
     *     NSIL_EXPLOITATION_INFO
     *     NSIL_IMAGERY
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testImageryViewConversion() {
        DAG imageryDAG = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addImageryPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        imageryDAG.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        imageryDAG.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(imageryDAG);

        if (shouldPrintCard) {
            File file = new File("/tmp/output.txt");
            if (file.exists()) {
                file.delete();
            }

            try (PrintStream outStream = new PrintStream(file)) {
                printMetacard(metacard, outStream);
            } catch (IOException ioe) {
                //Ignore the error
            }
        }

        //Check top-level meta-card attributes
        assertTrue(Stanag4559ImageryMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(IMAGERY_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(STREAM_STANDARD.equals(metacard.getContentTypeName()));
        assertTrue(STREAM_STANDARD_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
//      TODO TROY -- check the source -- need to figure out how to set it in DAGConverter
        // assertTrue(metacard.getSourceId().equals(SOU))
        assertTrue(STREAM_SOURCE_URL.equals(metacard.getResourceURI().toString()));

        /* TODO TROY -- check the other metacard attributes */

        //Check Common Attributes
        //TODO TROY IMPL
        Attribute explotiationLevelAttr = metacard.getAttribute(Stanag4559MetacardType.EXPLOITATION_LEVEL);
        assertNotNull(explotiationLevelAttr);
        assertTrue(EXPLOITATION_LEVEL == (int)explotiationLevelAttr.getValue());

        Attribute archivalInfoAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_ARCHIVAL_INFO);
        assertNotNull(archivalInfoAttr);
        assertTrue(ARCHIVE_INFORMATION.equals(archivalInfoAttr.getValue()));

        //Check the Imagery Attributes
        Attribute cloudCoverPctAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.CLOUD_COVER_PCT);
        assertNotNull(cloudCoverPctAttr);
        assertTrue(IMAGERY_CLOUD_COVER_PCT == (int)cloudCoverPctAttr.getValue());

        Attribute imageryCommentsAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.IMAGERY_COMMENTS);
        assertNotNull(imageryCommentsAttr);
        assertTrue(IMAGERY_COMMENTS.equals(imageryCommentsAttr.getValue()));

        Attribute imageryCategoryAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.IMAGERY_CATEGORY);
        assertNotNull(imageryCategoryAttr);
        assertTrue(IMAGERY_CATEGORY.equals(imageryCategoryAttr.getValue().toString()));

        Attribute decompressionTechAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.DECOMPRESSION_TECHNIQUE);
        assertNotNull(decompressionTechAttr);
        assertTrue(IMAGERY_DECOMPRESSION_TECH.equals(decompressionTechAttr.getValue().toString()));

        Attribute imageIdAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.IMAGE_ID);
        assertNotNull(imageIdAttr);
        assertTrue(IMAGERY_IDENTIFIER.equals(imageIdAttr.getValue().toString()));

        Attribute niirsAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.NIIRS);
        assertNotNull(niirsAttr);
        assertTrue(IMAGERY_NIIRS == (int)niirsAttr.getValue());

        Attribute numBandsAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.NUM_BANDS);
        assertNotNull(numBandsAttr);
        assertTrue(IMAGERY_NUM_BANDS == (int)numBandsAttr.getValue());

        Attribute numRowsAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.NUM_ROWS);
        assertNotNull(numRowsAttr);
        assertTrue(IMAGERY_NUM_ROWS == (int)numRowsAttr.getValue());

        Attribute numColsAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.NUM_COLS);
        assertNotNull(numColsAttr);
        assertTrue(IMAGERY_NUM_COLS == (int)numColsAttr.getValue());

        Attribute endDateTimeAttr = metacard.getAttribute(Stanag4559ImageryMetacardType.END_DATETIME);
        assertNotNull(endDateTimeAttr);
        assertTrue(cal.getTime().equals(endDateTimeAttr.getValue()));

        /*
        Set<AttributeDescriptor> descriptors = metacardType.getAttributeDescriptors();
        for (AttributeDescriptor descriptor:descriptors) {
            Attribute attribute = metacard.getAttribute(descriptor.getName());
            if (attribute != null) {
                outStream.println("  " + descriptor.getName() + " : " +
                        attribute.getValue());
            }
        }
        */
    }

    @Test
    public void testGmtiViewConversion() {

    }

    @Test
    public void testMessageViewConversion() {

    }

    @Test
    public void testVideoViewConversion() {

    }

    @Test
    public void testReportViewConversion() {

    }

    @Test
    public void testCCIRMViewConversion() {

    }

    @Test
    public void testTdlViewConversion() {

    }

    private Node createRootNode() {
        return new Node(0, NodeType.ROOT_NODE, Stanag4559Constants.NSIL_PRODUCT, orb.create_any());
    }

    private void addCardNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node cardNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_CARD, any);
        graph.addVertex(cardNode);
        graph.addEdge(productNode, cardNode);

        addStringAttribute(graph, cardNode, Stanag4559Constants.IDENTIFIER, CARD_ID);
        addDateAttribute(graph, cardNode, Stanag4559Constants.SOURCE_DATE_TIME_MODIFIED);
        addDateAttribute(graph, cardNode, Stanag4559Constants.DATE_TIME_MODIFIED);
        addStringAttribute(graph, cardNode, Stanag4559Constants.PUBLISHER, SOURCE_PUBLISHER);
        addStringAttribute(graph, cardNode, Stanag4559Constants.SOURCE_LIBRARY, SOURCE_LIBRARY);
    }

    private void addStreamNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node streamNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_STREAM, any);
        graph.addVertex(streamNode);
        graph.addEdge(productNode, streamNode);

        addBooleanAttribute(graph, streamNode, Stanag4559Constants.ARCHIVED, false);
        addStringAttribute(graph, streamNode, Stanag4559Constants.ARCHIVE_INFORMATION, ARCHIVE_INFORMATION);
        addStringAttribute(graph, streamNode, Stanag4559Constants.CREATOR, STREAM_CREATOR);
        addDateAttribute(graph, streamNode, Stanag4559Constants.DATE_TIME_DECLARED);
        addStringAttribute(graph, streamNode, Stanag4559Constants.STANDARD, STREAM_STANDARD);
        addStringAttribute(graph, streamNode, Stanag4559Constants.STANDARD_VERSION, STREAM_STANDARD_VER);
        addStringAttribute(graph, streamNode, Stanag4559Constants.SOURCE_URL, STREAM_SOURCE_URL);
        addIntegerAttribute(graph, streamNode, Stanag4559Constants.PROGRAM_ID, STREAM_PROGRAM_ID);
    }

    private void addMetadataSecurity(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node metadataSecurityNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_METADATA_SECURITY, any);
        graph.addVertex(metadataSecurityNode);
        graph.addEdge(productNode, metadataSecurityNode);

        addStringAttribute(graph, metadataSecurityNode, Stanag4559Constants.POLICY, CLASS_POLICY);
        addStringAttribute(graph, metadataSecurityNode, Stanag4559Constants.RELEASABILITY, CLASS_RELEASABILITY);
        addStringAttribute(graph, metadataSecurityNode, Stanag4559Constants.CLASSIFICATION, CLASS_CLASSIFICATION);
    }

    private void addSecurityNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node securityNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_METADATA_SECURITY, any);
        graph.addVertex(securityNode);
        graph.addEdge(productNode, securityNode);

        addStringAttribute(graph, securityNode, Stanag4559Constants.POLICY, CLASS_POLICY);
        addStringAttribute(graph, securityNode, Stanag4559Constants.RELEASABILITY, CLASS_RELEASABILITY);
        addStringAttribute(graph, securityNode, Stanag4559Constants.CLASSIFICATION, CLASS_CLASSIFICATION);
    }

    private Node addPartNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node partNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_PART, any);
        graph.addVertex(partNode);
        graph.addEdge(productNode, partNode);
        return partNode;
    }

    private void addImageryPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addImageryNode(graph, partNode);
    }

    private void addCommonNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any commonAny = orb.create_any();
        Node commonNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_COMMON, commonAny);
        graph.addVertex(commonNode);
        graph.addEdge(parentNode, commonNode);

        addStringAttribute(graph, commonNode, Stanag4559Constants.DESCRIPTION_ABSTRACT, COM_DESCRIPTION_ABSTRACT);
        addStringAttribute(graph, commonNode, Stanag4559Constants.IDENTIFIER_MISSION, COM_ID_MSN);
        addStringAttribute(graph, commonNode, Stanag4559Constants.IDENTIFIER_UUID, COM_ID_UUID);
        addIntegerAttribute(graph, commonNode, Stanag4559Constants.IDENTIFIER_JC3IEDM, COM_JC3ID);
        addStringAttribute(graph, commonNode, Stanag4559Constants.LANGUAGE, COM_LANGUAGE);
        addStringAttribute(graph, commonNode, Stanag4559Constants.SOURCE, COM_SOURCE);
        addStringAttribute(graph, commonNode, Stanag4559Constants.SUBJECT_CATEGORY_TARGET, COM_SUBJECT_CATEGORY_TARGET);
        addStringAttribute(graph, commonNode, Stanag4559Constants.TARGET_NUMBER, COM_TARGET_NUMBER);
        addStringAttribute(graph, commonNode, Stanag4559Constants.TYPE, COM_TYPE);
    }

    private void addImageryNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any imageryAny = orb.create_any();
        Node imageryNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_IMAGERY, imageryAny);
        graph.addVertex(imageryNode);
        graph.addEdge(parentNode, imageryNode);

        addStringAttribute(graph, imageryNode, Stanag4559Constants.CATEGORY, IMAGERY_CATEGORY);
        addIntegerAttribute(graph, imageryNode, Stanag4559Constants.CLOUD_COVER_PCT, IMAGERY_CLOUD_COVER_PCT);
        addStringAttribute(graph, imageryNode, Stanag4559Constants.COMMENTS, IMAGERY_COMMENTS);
        addStringAttribute(graph, imageryNode, Stanag4559Constants.DECOMPRESSION_TECHNIQUE, IMAGERY_DECOMPRESSION_TECH);
        addStringAttribute(graph, imageryNode, Stanag4559Constants.IDENTIFIER, IMAGERY_IDENTIFIER);
        addIntegerAttribute(graph, imageryNode, Stanag4559Constants.NIIRS, IMAGERY_NIIRS);
        addIntegerAttribute(graph, imageryNode, Stanag4559Constants.NUMBER_OF_BANDS, IMAGERY_NUM_BANDS);
        addIntegerAttribute(graph, imageryNode, Stanag4559Constants.NUMBER_OF_ROWS, IMAGERY_NUM_ROWS);
        addIntegerAttribute(graph, imageryNode, Stanag4559Constants.NUMBER_OF_COLS, IMAGERY_NUM_COLS);
        addStringAttribute(graph, imageryNode, Stanag4559Constants.TITLE, IMAGERY_TITLE);
    }

    private void addCoverageNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any coverageAny = orb.create_any();
        Node coverageNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_COVERAGE, coverageAny);
        graph.addVertex(coverageNode);
        graph.addEdge(parentNode, coverageNode);

        addStringAttribute(graph, coverageNode, Stanag4559Constants.SPATIAL_COUNTRY_CODE, COVERAGE_COUNTRY_CD);
        addDateAttribute(graph, coverageNode, Stanag4559Constants.TEMPORAL_START);
        addDateAttribute(graph, coverageNode, Stanag4559Constants.TEMPORAL_END);

        alliance.catalog.nato.stanag4559.common.UCO.Coordinate2d upperLeft =
                new alliance.catalog.nato.stanag4559.common.UCO.Coordinate2d(UPPER_LEFT_LAT, UPPER_LEFT_LON);
        alliance.catalog.nato.stanag4559.common.UCO.Coordinate2d lowerRight =
                new alliance.catalog.nato.stanag4559.common.UCO.Coordinate2d(LOWER_RIGHT_LAT, LOWER_RIGHT_LON);
        alliance.catalog.nato.stanag4559.common.UCO.Rectangle rectangle =
                new alliance.catalog.nato.stanag4559.common.UCO.Rectangle(upperLeft, lowerRight);
        Any spatialCoverage = orb.create_any();
        RectangleHelper.insert(spatialCoverage, rectangle);
        addAnyAttribute(graph, coverageNode, Stanag4559Constants.SPATIAL_GEOGRAPHIC_REF_BOX, spatialCoverage);
    }

    private void addExpoloitationInfoNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any exploitationAny = orb.create_any();
        Node exploitationNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_EXPLOITATION_INFO, exploitationAny);
        graph.addVertex(exploitationNode);
        graph.addEdge(parentNode, exploitationNode);

        addStringAttribute(graph, exploitationNode, Stanag4559Constants.DESCRIPTION, EXPLOITATION_DESC);
        addIntegerAttribute(graph, exploitationNode, Stanag4559Constants.LEVEL, EXPLOITATION_LEVEL);
        addBooleanAttribute(graph, exploitationNode, Stanag4559Constants.AUTO_GENERATED, EXPLOITATION_AUTO_GEN);
        addStringAttribute(graph, exploitationNode, Stanag4559Constants.SUBJ_QUALITY_CODE, EXPLOITATION_SUBJ_QUAL_CODE);
    }

    private void addStringAttribute(DirectedAcyclicGraph<Node, Edge> graph,
            Node parentNode, String key, String value) {
        Any any = orb.create_any();
        any.insert_wstring(value);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addIntegerAttribute(DirectedAcyclicGraph<Node, Edge> graph,
            Node parentNode, String key, Integer integer) {
        Any any = orb.create_any();
        any.insert_long(integer);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addDoubleAttribute(DirectedAcyclicGraph<Node, Edge> graph,
            Node parentNode, String key, Double doubleVal) {
        Any any = orb.create_any();
        any.insert_double(doubleVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addBooleanAttribute(DirectedAcyclicGraph<Node, Edge> graph,
            Node parentNode, String key, Boolean boolVal) {
        Any any = orb.create_any();
        any.insert_boolean(boolVal);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addAnyAttribute(DirectedAcyclicGraph<Node, Edge> graph,
            Node parentNode, String key, Any any) {
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void addDateAttribute(DirectedAcyclicGraph<Node, Edge> graph,
            Node parentNode, String key) {
        Any any = orb.create_any();

        AbsTime absTime =
                new AbsTime(new alliance.catalog.nato.stanag4559.common.UCO.Date((short)cal.get(Calendar.YEAR),
                (short) (cal.get(Calendar.MONTH) + 1), (short) cal.get(Calendar.DAY_OF_MONTH)),
                new Time((short) cal.get(Calendar.HOUR_OF_DAY), (short) cal.get(Calendar.MINUTE),
                        (short) cal.get(Calendar.SECOND)));
        AbsTimeHelper.insert(any, absTime);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private void printMetacard(MetacardImpl metacard, PrintStream outStream) {
        MetacardType metacardType = metacard.getMetacardType();
        outStream.println("Metacard Type : " + metacardType.getClass().getCanonicalName());
        outStream.println("ID : " + metacard.getId());
        outStream.println("Title : " + metacard.getTitle());
        outStream.println("Description : " + metacard.getDescription());
        outStream.println("Content Type Name : " + metacard.getContentTypeName());
        outStream.println("Content Type Version : " + metacard.getContentTypeVersion());
        outStream.println("Created Date : " + metacard.getCreatedDate());
        outStream.println("Effective Date : " + metacard.getEffectiveDate());
        outStream.println("Location : " + metacard.getLocation());
        outStream.println("SourceID : " + metacard.getSourceId());
        outStream.println("Modified Date : " + metacard.getModifiedDate());
        outStream.println("Resource URI : " + metacard.getResourceURI().toString());

        Set<AttributeDescriptor> descriptors = metacardType.getAttributeDescriptors();
        for (AttributeDescriptor descriptor:descriptors) {
            Attribute attribute = metacard.getAttribute(descriptor.getName());
            if (attribute != null) {
                outStream.println("  " + descriptor.getName() + " : " +
                        attribute.getValue());
            }
        }
    }
}
