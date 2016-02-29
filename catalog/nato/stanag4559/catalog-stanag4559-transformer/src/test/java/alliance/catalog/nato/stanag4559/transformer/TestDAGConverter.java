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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import alliance.catalog.nato.stanag4559.common.Stanag4559CxpMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559CxpStatusType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ExploitationSubQualCode;
import alliance.catalog.nato.stanag4559.common.Stanag4559GmtiMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559IRMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryDecompressionTech;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ImageryType;
import alliance.catalog.nato.stanag4559.common.Stanag4559MessageMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559MetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559MetadataEncodingScheme;
import alliance.catalog.nato.stanag4559.common.Stanag4559ProductType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ReportMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559ReportPriority;
import alliance.catalog.nato.stanag4559.common.Stanag4559ReportType;
import alliance.catalog.nato.stanag4559.common.Stanag4559RfiMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559RfiStatus;
import alliance.catalog.nato.stanag4559.common.Stanag4559RfiWorkflowStatus;
import alliance.catalog.nato.stanag4559.common.Stanag4559ScanningMode;
import alliance.catalog.nato.stanag4559.common.Stanag4559TaskMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559TaskStatus;
import alliance.catalog.nato.stanag4559.common.Stanag4559TdlMetacardType;
import alliance.catalog.nato.stanag4559.common.Stanag4559VideoCategoryType;
import alliance.catalog.nato.stanag4559.common.Stanag4559VideoEncodingScheme;
import alliance.catalog.nato.stanag4559.common.Stanag4559VideoMetacardType;
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

    private static final String CARD_ID = "Card ID";

    private static final String SOURCE_PUBLISHER = "Source Publisher";

    private static final String SOURCE_LIBRARY = "SourceLibrary";

    private static final String ARCHIVE_INFORMATION = "Archive Information";

    private static final Boolean STREAM_ARCHIVED = false;

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

    private static final Stanag4559ProductType COM_TYPE = Stanag4559ProductType.COLLECTION_EXPLOITATION_PLAN;

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

    private static final Integer IMAGERY_NUM_COLS = 400;

    private static final String IMAGERY_TITLE = "Imagery Title";

    private static final String WKT_LOCATION = "POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1))";

    private static final Boolean FILE_ARCHIVED = false;

    private static final String FILE_ARCHIVE_INFO = "File Archive Info";

    private static final String FILE_CREATOR = "File Creator";

    private static final Double FILE_EXTENT = 25.5;

    private static final String FILE_FORMAT = "JPEG";

    private static final String FILE_FORMAT_VER = "1.0";

    private static final String FILE_PRODUCT_URL = "http://localhost/file.jpg";

    private static final String FILE_TITLE = "File Title";

    private static final Double GMTI_JOB_ID = 2.3;

    private static final Integer GMTI_TARGET_REPORTS = 2;

    private static final String MESSAGE_RECIPIENT = "john@doe.com";

    private static final String MESSAGE_SUBJECT = "Test Subject";

    private static final String MESSAGE_BODY = "Test Message Body";

    private static final String MESSAGE_TYPE = "XMPP";

    private static final Double VIDEO_AVG_BIT_RATE = 22.5;

    private static final String VIDEO_CATEGORY = Stanag4559VideoCategoryType.VIS.name();

    private static final Stanag4559VideoEncodingScheme VIDEO_ENCODING_SCHEME = Stanag4559VideoEncodingScheme.MPEG2;

    private static final Double VIDEO_FRAME_RATE = 15.4;

    private static final Integer VIDEO_NUM_ROWS = 500;

    private static final Integer VIDEO_NUM_COLS = 400;

    private static final String VIDEO_METADATA_ENC_SCHEME = Stanag4559MetadataEncodingScheme.KLV.name();

    private static final Integer VIDEO_MISM_LEVEL = 4;

    private static final String VIDEO_SCANNING_MODE = Stanag4559ScanningMode.PROGRESSIVE.name();

    private static final String REPORT_REQ_SERIAL_NUM = "112233";

    private static final String REPORT_PRIORITY = Stanag4559ReportPriority.FLASH.name();

    private static final String REPORT_TYPE = Stanag4559ReportType.MTIEXREP.name();

    private static final Integer TDL_ACTIVITY = 99;

    private static final String TDL_MESSAGE_NUM = "J3.2";

    private static final Integer TDL_PLATFORM_NUM = 42;

    private static final String TDL_TRACK_NUM = "AK320";

    private static final String CXP_STATUS = Stanag4559CxpStatusType.CURRENT.name();

    private static final String RFI_FOR_ACTION = "USAF";

    private static final String RFI_FOR_INFORMATION = "USMC";

    private static final String RFI_SERIAL_NUM = "123456";

    private static final String RFI_STATUS = Stanag4559RfiStatus.APPROVED.name();

    private static final String RFI_WORKFLOW_STATUS = Stanag4559RfiWorkflowStatus.ACCEPTED.name();

    private static final String TASK_COMMENTS = "Task Comments";

    private static final String TASK_STATUS = Stanag4559TaskStatus.INTERRUPTED.name();

    private ORB orb;

    private Calendar cal;

    private static final boolean SHOULD_PRINT_CARD = false;

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
        addFileNode(graph, productNode);
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

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-imagery.txt");
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
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(FILE_FORMAT.equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkImageryAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkExploitationInfoAttributes(MetacardImpl metacard) {
        Attribute exploitationDescAttr = metacard.getAttribute(Stanag4559MetacardType.EXPLOITATION_DESCRIPTION);
        assertNotNull(exploitationDescAttr);
        assertTrue(EXPLOITATION_DESC.equals(exploitationDescAttr.getValue().toString()));

        Attribute exploitationLevelAttr = metacard.getAttribute(Stanag4559MetacardType.EXPLOITATION_LEVEL);
        assertNotNull(exploitationLevelAttr);
        assertTrue(EXPLOITATION_LEVEL == (int)exploitationLevelAttr.getValue());

        Attribute exploitationAutoGenAttr = metacard.getAttribute(Stanag4559MetacardType.EXPLOITATION_AUTO_GEN);
        assertNotNull(exploitationAutoGenAttr);
        assertTrue(EXPLOITATION_AUTO_GEN == (boolean)exploitationAutoGenAttr.getValue());

        Attribute subjQualCodeAttr = metacard.getAttribute(Stanag4559MetacardType.EXPLOITATION_SUBJ_QUAL_CODE);
        assertNotNull(subjQualCodeAttr);
        assertTrue(EXPLOITATION_SUBJ_QUAL_CODE.equals(subjQualCodeAttr.getValue().toString()));
    }

    private void checkStreamAttributes(MetacardImpl metacard) {
        Attribute archivedAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_ARCHIVED);
        assertNotNull(archivedAttr);
        assertTrue(STREAM_ARCHIVED == (boolean)archivedAttr.getValue());

        Attribute archivalInfoAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_ARCHIVAL_INFO);
        assertNotNull(archivalInfoAttr);
        assertTrue(ARCHIVE_INFORMATION.equals(archivalInfoAttr.getValue()));

        Attribute creatorAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_CREATOR);
        assertNotNull(creatorAttr);
        assertTrue(STREAM_CREATOR.equals(creatorAttr.getValue().toString()));

        Attribute dateTimeDeclaredAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_DATETIME_DECLARED);
        assertNotNull(dateTimeDeclaredAttr);
        assertTrue(cal.getTime().equals(dateTimeDeclaredAttr.getValue()));

        Attribute programIdAttr = metacard.getAttribute(Stanag4559MetacardType.STREAM_PROGRAM_ID);
        assertNotNull(programIdAttr);
        assertTrue(STREAM_PROGRAM_ID == (int)programIdAttr.getValue());

    }

    private void checkCommonAttributes(MetacardImpl metacard) {
        Attribute identifierMsnAttr = metacard.getAttribute(Stanag4559MetacardType.IDENTIFIER_MISSION);
        assertNotNull(identifierMsnAttr);
        assertTrue(COM_ID_MSN.equals(identifierMsnAttr.getValue().toString()));

        Attribute identifierJc3idmAttr = metacard.getAttribute(Stanag4559MetacardType.ID_JC3IEDM);
        assertNotNull(identifierJc3idmAttr);
        assertTrue(COM_JC3ID == (int)identifierJc3idmAttr.getValue());

        Attribute languageAttr = metacard.getAttribute(Stanag4559MetacardType.LANGUAGE);
        assertNotNull(languageAttr);
        assertTrue(COM_LANGUAGE.equals(languageAttr.getValue().toString()));

        Attribute stanagSourceAttr = metacard.getAttribute(Stanag4559MetacardType.SOURCE);
        assertNotNull(stanagSourceAttr);
        assertTrue(COM_SOURCE.equals(stanagSourceAttr.getValue().toString()));

        Attribute subjCatTgtAttr = metacard.getAttribute(Stanag4559MetacardType.SUBJECT_CATEGORY_TARGET);
        assertNotNull(subjCatTgtAttr);
        assertTrue(COM_SUBJECT_CATEGORY_TARGET.equals(subjCatTgtAttr.getValue().toString()));

        Attribute tgtNumAttr = metacard.getAttribute(Stanag4559MetacardType.TARGET_NUMBER);
        assertNotNull(tgtNumAttr);
        assertTrue(COM_TARGET_NUMBER.equals(tgtNumAttr.getValue().toString()));

        Attribute productTypeAttr = metacard.getAttribute(Stanag4559MetacardType.PRODUCT_TYPE);
        assertNotNull(productTypeAttr);
        assertTrue(COM_TYPE.name().equals(productTypeAttr.getValue().toString()));
    }

    private void checkImageryAttributes(MetacardImpl metacard) {
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

        assertNotNull(metacard.getResourceSize());
        int size = Integer.parseInt(metacard.getResourceSize());
        assertTrue(size == DAGConverter.convertMegabytesToBytes(FILE_EXTENT));
    }

    private void checkSecurityAttributes(MetacardImpl metacard) {
        Attribute classificationAttr = metacard.getAttribute(Stanag4559MetacardType.SECURITY_CLASSIFICATION);
        assertNotNull(classificationAttr);
        assertTrue(CLASS_CLASSIFICATION.equals(classificationAttr.getValue().toString()));

        Attribute policyAttr = metacard.getAttribute(Stanag4559MetacardType.SECURITY_POLICY);
        assertNotNull(policyAttr);
        assertTrue(CLASS_POLICY.equals(policyAttr.getValue().toString()));

        Attribute releasabilityAttr = metacard.getAttribute(Stanag4559MetacardType.SECURITY_RELEASABILITY);
        assertNotNull(releasabilityAttr);
        assertTrue(CLASS_RELEASABILITY.equals(releasabilityAttr.getValue().toString()));
    }

    private void checkCoverageAttributes(MetacardImpl metacard) {
        Attribute spatialCtryCodeAttr = metacard.getAttribute(Stanag4559MetacardType.COUNTRY_CODE);
        assertNotNull(spatialCtryCodeAttr);
        assertTrue(COVERAGE_COUNTRY_CD.equals(spatialCtryCodeAttr.getValue().toString()));

        Attribute startTimeAttr = metacard.getAttribute(Stanag4559MetacardType.START_DATETIME);
        assertNotNull(startTimeAttr);
        assertTrue(cal.getTime().equals(startTimeAttr.getValue()));

        Attribute endTimeAttr = metacard.getAttribute(Stanag4559MetacardType.END_DATETIME);
        assertNotNull(endTimeAttr);
        assertTrue(cal.getTime().equals(endTimeAttr.getValue()));
    }

    /**
     * Test the GMTI View DAG to Metacard
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
     *     NSIL_GMTI
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testGmtiViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addGmtiPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-gmti.txt");
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
        assertTrue(Stanag4559GmtiMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(FILE_FORMAT.equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkGmtiAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkGmtiAttributes(MetacardImpl metacard) {
        Attribute gmtiJobAttr = metacard.getAttribute(Stanag4559GmtiMetacardType.JOB_ID);
        assertNotNull(gmtiJobAttr);
        assertEquals(GMTI_JOB_ID, gmtiJobAttr.getValue());

        Attribute numTgtAttr = metacard.getAttribute(Stanag4559GmtiMetacardType.NUM_TARGET_REPORTS);
        assertNotNull(numTgtAttr);
        assertTrue(GMTI_TARGET_REPORTS == (int)numTgtAttr.getValue());
    }

    /**
     * Test the Message View DAG to Metacard
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
     *     NSIL_MESSAGE
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testMessageViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addMessagePart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-message.txt");
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
        assertTrue(Stanag4559MessageMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(MESSAGE_SUBJECT.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(MESSAGE_TYPE.equals(metacard.getContentTypeName()));
        assertNull(metacard.getContentTypeVersion());
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(MESSAGE_BODY.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkMessageAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkMessageAttributes(MetacardImpl metacard) {
        Attribute messageRecipAttr = metacard.getAttribute(Stanag4559MessageMetacardType.RECIPIENT);
        assertNotNull(messageRecipAttr);
        assertTrue(MESSAGE_RECIPIENT.equals(messageRecipAttr.getValue()));

        Attribute typeAttr = metacard.getAttribute(Stanag4559MessageMetacardType.MESSAGE_TYPE);
        assertNotNull(typeAttr);
        assertTrue(MESSAGE_TYPE.equals(typeAttr.getValue().toString()));
    }

    /**
     * Test the Message View DAG to Metacard
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
     *     NSIL_VIDEO
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testVideoViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addVideoPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-video.txt");
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
        assertTrue(Stanag4559VideoMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(VIDEO_ENCODING_SCHEME.name().equals(metacard.getContentTypeName()));
        assertNull(metacard.getContentTypeVersion());
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkVideoAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkVideoAttributes(MetacardImpl metacard) {
        Attribute avgBitRateAttr = metacard.getAttribute(Stanag4559VideoMetacardType.AVG_BIT_RATE);
        assertNotNull(avgBitRateAttr);
        assertEquals(VIDEO_AVG_BIT_RATE, avgBitRateAttr.getValue());

        Attribute categoryAttr = metacard.getAttribute(Stanag4559VideoMetacardType.CATEGORY);
        assertNotNull(categoryAttr);
        assertTrue(VIDEO_CATEGORY.equals(categoryAttr.getValue().toString()));

        Attribute encodingSchemeAttr = metacard.getAttribute(Stanag4559VideoMetacardType.ENCODING_SCHEME);
        assertNotNull(encodingSchemeAttr);
        assertTrue(VIDEO_ENCODING_SCHEME.name().equals(encodingSchemeAttr.getValue().toString()));

        Attribute frameRateAttr = metacard.getAttribute(Stanag4559VideoMetacardType.FRAME_RATE);
        assertNotNull(frameRateAttr);
        assertEquals(VIDEO_FRAME_RATE, frameRateAttr.getValue());

        Attribute numRowsAttr = metacard.getAttribute(Stanag4559VideoMetacardType.NUM_ROWS);
        assertNotNull(numRowsAttr);
        assertTrue(VIDEO_NUM_ROWS == (int)numRowsAttr.getValue());

        Attribute numColsAttr = metacard.getAttribute(Stanag4559VideoMetacardType.NUM_COLS);
        assertNotNull(numColsAttr);
        assertTrue(VIDEO_NUM_COLS == (int)numColsAttr.getValue());

        Attribute metadataEncSchemeAttr = metacard.getAttribute(Stanag4559VideoMetacardType.METADATA_ENCODING_SCHEME);
        assertNotNull(metadataEncSchemeAttr);
        assertTrue(VIDEO_METADATA_ENC_SCHEME.equals(metadataEncSchemeAttr.getValue().toString()));

        Attribute mismLevelAttr = metacard.getAttribute(Stanag4559VideoMetacardType.MISM_LEVEL);
        assertNotNull(mismLevelAttr);
        assertTrue(VIDEO_MISM_LEVEL == (int)mismLevelAttr.getValue());

        Attribute scanningModeAttr = metacard.getAttribute(Stanag4559VideoMetacardType.SCANNING_MODE);
        assertNotNull(scanningModeAttr);
        assertTrue(VIDEO_SCANNING_MODE.equals(scanningModeAttr.getValue().toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     *
     * NSIL_PRODUCT
     *   NSIL_APPROVAL
     *   NSIL_CARD
     *   NSIL_FILE
     *   NSIL_METADATASECURITY
     *   NSIL_RELATED_FILE
     *   NSIL_SECURITY
     *   NSIL_PART
     *     NSIL_SECURITY
     *     NSIL_COMMON
     *     NSIL_COVERAGE
     *     NSIL_EXPLOITATION_INFO
     *     NSIL_REPORT
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testReportViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addReportPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-report.txt");
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
        assertTrue(Stanag4559ReportMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(FILE_FORMAT.equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkReportAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkReportAttributes(MetacardImpl metacard) {
        Attribute origReqSerialAttr = metacard.getAttribute(Stanag4559ReportMetacardType.ORIGINATOR_REQ_SERIAL_NUM);
        assertNotNull(origReqSerialAttr);
        assertTrue(REPORT_REQ_SERIAL_NUM.equals(origReqSerialAttr.getValue().toString()));

        Attribute priorityAttr = metacard.getAttribute(Stanag4559ReportMetacardType.PRIORITY);
        assertNotNull(priorityAttr);
        assertTrue(REPORT_PRIORITY.equals(priorityAttr.getValue().toString()));

        Attribute typeAttr = metacard.getAttribute(Stanag4559ReportMetacardType.TYPE);
        assertNotNull(typeAttr);
        assertTrue(REPORT_TYPE.equals(typeAttr.getValue().toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     *
     * NSIL_PRODUCT
     *   NSIL_APPROVAL
     *   NSIL_CARD
     *   NSIL_STREAM
     *   NSIL_FILE
     *   NSIL_METADATASECURITY
     *   NSIL_RELATED_FILE
     *   NSIL_SECURITY
     *   NSIL_PART
     *     NSIL_SECURITY
     *     NSIL_COMMON
     *     NSIL_COVERAGE
     *     NSIL_EXPLOITATION_INFO
     *     NSIL_CXP
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testCCIRMCXPViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addCxpPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-ccirm-cxp.txt");
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
        assertTrue(Stanag4559CxpMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(FILE_FORMAT.equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkCxpAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkCxpAttributes(MetacardImpl metacard) {
        Attribute cxpAttr = metacard.getAttribute(Stanag4559CxpMetacardType.STATUS);
        assertNotNull(cxpAttr);
        assertTrue(CXP_STATUS.equals(cxpAttr.getValue().toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     *
     * NSIL_PRODUCT
     *   NSIL_APPROVAL
     *   NSIL_CARD
     *   NSIL_STREAM
     *   NSIL_FILE
     *   NSIL_METADATASECURITY
     *   NSIL_RELATED_FILE
     *   NSIL_SECURITY
     *   NSIL_PART
     *     NSIL_SECURITY
     *     NSIL_COMMON
     *     NSIL_COVERAGE
     *     NSIL_EXPLOITATION_INFO
     *     NSIL_IR
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testCCIRMIRViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addIRPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-ccirm-ir.txt");
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
        assertTrue(Stanag4559IRMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(FILE_FORMAT.equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkIRAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkIRAttributes(MetacardImpl metacard) {
        //NSIL_IR is a marker node type only, no attributes to check
    }

    /**
     * Test the Message View DAG to Metacard
     *
     * NSIL_PRODUCT
     *   NSIL_APPROVAL
     *   NSIL_CARD
     *   NSIL_STREAM
     *   NSIL_FILE
     *   NSIL_METADATASECURITY
     *   NSIL_RELATED_FILE
     *   NSIL_SECURITY
     *   NSIL_PART
     *     NSIL_SECURITY
     *     NSIL_COMMON
     *     NSIL_COVERAGE
     *     NSIL_EXPLOITATION_INFO
     *     NSIL_RFI
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testCCIRMRFIViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addRFIPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-ccirm-rfi.txt");
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
        assertTrue(Stanag4559RfiMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(FILE_FORMAT.equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkRFIAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkRFIAttributes(MetacardImpl metacard) {
        Attribute forActionAttr = metacard.getAttribute(Stanag4559RfiMetacardType.FOR_ACTION);
        assertNotNull(forActionAttr);
        assertTrue(RFI_FOR_ACTION.equals(forActionAttr.getValue().toString()));

        Attribute forInfoAttr = metacard.getAttribute(Stanag4559RfiMetacardType.FOR_INFORMATION);
        assertNotNull(forInfoAttr);
        assertTrue(RFI_FOR_INFORMATION.equals(forInfoAttr.getValue().toString()));

        Attribute serialNumAttr = metacard.getAttribute(Stanag4559RfiMetacardType.SERIAL_NUMBER);
        assertNotNull(serialNumAttr);
        assertTrue(RFI_SERIAL_NUM.equals(serialNumAttr.getValue().toString()));

        Attribute statusAttr = metacard.getAttribute(Stanag4559RfiMetacardType.STATUS);
        assertNotNull(statusAttr);
        assertTrue(RFI_STATUS.equals(statusAttr.getValue().toString()));

        Attribute workflowStatusAttr = metacard.getAttribute(Stanag4559RfiMetacardType.WORKFLOW_STATUS);
        assertNotNull(workflowStatusAttr);
        assertTrue(RFI_WORKFLOW_STATUS.equals(workflowStatusAttr.getValue().toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     *
     * NSIL_PRODUCT
     *   NSIL_APPROVAL
     *   NSIL_CARD
     *   NSIL_STREAM
     *   NSIL_FILE
     *   NSIL_METADATASECURITY
     *   NSIL_RELATED_FILE
     *   NSIL_SECURITY
     *   NSIL_PART
     *     NSIL_SECURITY
     *     NSIL_COMMON
     *     NSIL_COVERAGE
     *     NSIL_EXPLOITATION_INFO
     *     NSIL_TASK
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testCCIRMTaskViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addTaskPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-ccirm-task.txt");
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
        assertTrue(Stanag4559TaskMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(FILE_FORMAT.equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkTaskAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkTaskAttributes(MetacardImpl metacard) {
        Attribute commentAttr = metacard.getAttribute(Stanag4559TaskMetacardType.COMMENTS);
        assertNotNull(commentAttr);
        assertTrue(TASK_COMMENTS.equals(commentAttr.getValue().toString()));

        Attribute statusAttr = metacard.getAttribute(Stanag4559TaskMetacardType.STATUS);
        assertNotNull(statusAttr);
        assertTrue(TASK_STATUS.equals(statusAttr.getValue().toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     *
     * NSIL_PRODUCT
     *   NSIL_APPROVAL
     *   NSIL_CARD
     *   NSIL_STREAM
     *   NSIL_FILE
     *   NSIL_METADATASECURITY
     *   NSIL_RELATED_FILE
     *   NSIL_SECURITY
     *   NSIL_PART
     *     NSIL_SECURITY
     *     NSIL_COMMON
     *     NSIL_COVERAGE
     *     NSIL_EXPLOITATION_INFO
     *     NSIL_TDL
     *   NSIL_ASSOCIATION
     *     NSIL_RELATION
     *     NSIL_SOURCE
     *        NSIL_CARD
     *     NSIL_DESTINATION
     *        NSIL_CARD
     */
    @Test
    public void testTdlViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addTdlPart(graph, productNode);

        graph.addVertex(productNode);

        Stanag4559CommonUtils.setUCOEdgeIds(graph);
        Stanag4559CommonUtils.setUCOEdges(productNode, graph);
        dag.edges = Stanag4559CommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = Stanag4559CommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = DAGConverter.convertDAG(dag);

        if (SHOULD_PRINT_CARD) {
            File file = new File("/tmp/output-tdl.txt");
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
        assertTrue(Stanag4559TdlMetacardType.class.getCanonicalName().equals(metacard.getMetacardType().getClass().getCanonicalName()));
        assertTrue(FILE_TITLE.equals(metacard.getTitle()));
        assertTrue(CARD_ID.equals(metacard.getId()));
        assertTrue(FILE_FORMAT.equals(metacard.getContentTypeName()));
        assertTrue(FILE_FORMAT_VER.equals(metacard.getContentTypeVersion()));
        assertTrue(metacard.getCreatedDate() != null);
        assertTrue(metacard.getEffectiveDate() != null);
        assertTrue(cal.getTime().equals(metacard.getModifiedDate()));
        assertTrue(COM_DESCRIPTION_ABSTRACT.equals(metacard.getDescription()));
        assertTrue(WKT_LOCATION.equals(metacard.getLocation()));
        assertTrue(FILE_PRODUCT_URL.equals(metacard.getResourceURI().toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkTdlAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkTdlAttributes(MetacardImpl metacard) {
        Attribute activityAttr = metacard.getAttribute(Stanag4559TdlMetacardType.ACTIVITY);
        assertNotNull(activityAttr);
        assertTrue(TDL_ACTIVITY == (int)activityAttr.getValue());

        Attribute msgNumAttr = metacard.getAttribute(Stanag4559TdlMetacardType.MESSAGE_NUM);
        assertNotNull(msgNumAttr);
        assertTrue(TDL_MESSAGE_NUM.equals(msgNumAttr.getValue().toString()));

        Attribute platformNumAttr = metacard.getAttribute(Stanag4559TdlMetacardType.PLATFORM);
        assertNotNull(platformNumAttr);
        assertTrue(TDL_PLATFORM_NUM == (int)platformNumAttr.getValue());

        Attribute trackNumAttr = metacard.getAttribute(Stanag4559TdlMetacardType.TRACK_NUM);
        assertNotNull(trackNumAttr);
        assertTrue(TDL_TRACK_NUM.equals(trackNumAttr.getValue().toString()));
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

    private void addFileNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node fileNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_FILE, any);
        graph.addVertex(fileNode);
        graph.addEdge(productNode, fileNode);

        addBooleanAttribute(graph, fileNode, Stanag4559Constants.ARCHIVED, FILE_ARCHIVED);
        addStringAttribute(graph, fileNode, Stanag4559Constants.ARCHIVE_INFORMATION, FILE_ARCHIVE_INFO);
        addStringAttribute(graph, fileNode, Stanag4559Constants.CREATOR, FILE_CREATOR);
        addDateAttribute(graph, fileNode, Stanag4559Constants.DATE_TIME_DECLARED);
        addDoubleAttribute(graph, fileNode, Stanag4559Constants.EXTENT, FILE_EXTENT);
        addStringAttribute(graph, fileNode, Stanag4559Constants.FORMAT, FILE_FORMAT);
        addStringAttribute(graph, fileNode, Stanag4559Constants.FORMAT_VERSION, FILE_FORMAT_VER);
        addStringAttribute(graph, fileNode, Stanag4559Constants.PRODUCT_URL, FILE_PRODUCT_URL);
        addStringAttribute(graph, fileNode, Stanag4559Constants.TITLE, FILE_TITLE);
    }

    private void addStreamNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node streamNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_STREAM, any);
        graph.addVertex(streamNode);
        graph.addEdge(productNode, streamNode);

        addBooleanAttribute(graph, streamNode, Stanag4559Constants.ARCHIVED, STREAM_ARCHIVED);
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

    private void addGmtiPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addGmtiNode(graph, partNode);
    }

    private void addMessagePart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addMessageNode(graph, partNode);
    }

    private void addVideoPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addVideoNode(graph, partNode);
    }

    private void addReportPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addReportNode(graph, partNode);
    }

    private void addTdlPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addTdlNode(graph, partNode);
    }

    private void addCxpPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addCxpNode(graph, partNode);
    }

    private void addIRPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addIRNode(graph, partNode);
    }

    private void addRFIPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addRFINode(graph, partNode);
    }

    private void addTaskPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode = addPartNode(graph, productNode);

        addSecurityNode(graph, partNode);
        addCommonNode(graph, partNode);
        addCoverageNode(graph, partNode);
        addExpoloitationInfoNode(graph, partNode);
        addTaskNode(graph, partNode);
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
        addStringAttribute(graph, commonNode, Stanag4559Constants.TYPE, COM_TYPE.getSpecName());
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

    private void addGmtiNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any gmtiAny = orb.create_any();
        Node gmtiNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_GMTI, gmtiAny);
        graph.addVertex(gmtiNode);
        graph.addEdge(parentNode, gmtiNode);

        addDoubleAttribute(graph, gmtiNode, Stanag4559Constants.IDENTIFIER_JOB, GMTI_JOB_ID);
        addIntegerAttribute(graph, gmtiNode, Stanag4559Constants.NUMBER_OF_TARGET_REPORTS, GMTI_TARGET_REPORTS);
    }

    private void addMessageNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any messageAny = orb.create_any();
        Node messageNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_MESSAGE, messageAny);
        graph.addVertex(messageNode);
        graph.addEdge(parentNode, messageNode);

        addStringAttribute(graph, messageNode, Stanag4559Constants.RECIPIENT, MESSAGE_RECIPIENT);
        addStringAttribute(graph, messageNode, Stanag4559Constants.SUBJECT, MESSAGE_SUBJECT);
        addStringAttribute(graph, messageNode, Stanag4559Constants.MESSAGE_BODY, MESSAGE_BODY);
        addStringAttribute(graph, messageNode, Stanag4559Constants.MESSAGE_TYPE, MESSAGE_TYPE);
    }

    private void addVideoNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any videoAny = orb.create_any();
        Node videoNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_VIDEO, videoAny);
        graph.addVertex(videoNode);
        graph.addEdge(parentNode, videoNode);

        addDoubleAttribute(graph, videoNode, Stanag4559Constants.AVG_BIT_RATE, VIDEO_AVG_BIT_RATE);
        addStringAttribute(graph, videoNode, Stanag4559Constants.CATEGORY, VIDEO_CATEGORY);
        addStringAttribute(graph, videoNode, Stanag4559Constants.ENCODING_SCHEME, VIDEO_ENCODING_SCHEME.getSpecName());
        addDoubleAttribute(graph, videoNode, Stanag4559Constants.FRAME_RATE, VIDEO_FRAME_RATE);
        addIntegerAttribute(graph, videoNode, Stanag4559Constants.NUMBER_OF_ROWS, VIDEO_NUM_ROWS);
        addIntegerAttribute(graph, videoNode, Stanag4559Constants.NUMBER_OF_COLS, VIDEO_NUM_COLS);
        addStringAttribute(graph, videoNode, Stanag4559Constants.METADATA_ENC_SCHEME, VIDEO_METADATA_ENC_SCHEME);
        addIntegerAttribute(graph, videoNode, Stanag4559Constants.MISM_LEVEL, VIDEO_MISM_LEVEL);
        addStringAttribute(graph, videoNode, Stanag4559Constants.SCANNING_MODE, VIDEO_SCANNING_MODE);
    }

    private void addReportNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any reportAny = orb.create_any();
        Node reportNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_REPORT, reportAny);
        graph.addVertex(reportNode);
        graph.addEdge(parentNode, reportNode);

        addStringAttribute(graph, reportNode, Stanag4559Constants.ORIGINATORS_REQ_SERIAL_NUM, REPORT_REQ_SERIAL_NUM);
        addStringAttribute(graph, reportNode, Stanag4559Constants.PRIORITY, REPORT_PRIORITY);
        addStringAttribute(graph, reportNode, Stanag4559Constants.TYPE, REPORT_TYPE);
    }

    private void addTdlNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any tdlAny = orb.create_any();
        Node tdlNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_TDL, tdlAny);
        graph.addVertex(tdlNode);
        graph.addEdge(parentNode, tdlNode);

        addIntegerAttribute(graph, tdlNode, Stanag4559Constants.ACTIVITY, TDL_ACTIVITY);
        addStringAttribute(graph, tdlNode, Stanag4559Constants.MESSAGE_NUM, TDL_MESSAGE_NUM);
        addIntegerAttribute(graph, tdlNode, Stanag4559Constants.PLATFORM, TDL_PLATFORM_NUM);
        addStringAttribute(graph, tdlNode, Stanag4559Constants.TRACK_NUM, TDL_TRACK_NUM);
    }

    private void addCxpNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any cxpAny = orb.create_any();
        Node cxpNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_CXP, cxpAny);
        graph.addVertex(cxpNode);
        graph.addEdge(parentNode, cxpNode);

        addStringAttribute(graph, cxpNode, Stanag4559Constants.STATUS, CXP_STATUS);
    }

    private void addIRNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any irAny = orb.create_any();
        Node irNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_IR, irAny);
        graph.addVertex(irNode);
        graph.addEdge(parentNode, irNode);
    }

    private void addRFINode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any rfiAny = orb.create_any();
        Node rfiNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_RFI, rfiAny);
        graph.addVertex(rfiNode);
        graph.addEdge(parentNode, rfiNode);

        addStringAttribute(graph, rfiNode, Stanag4559Constants.FOR_ACTION, RFI_FOR_ACTION);
        addStringAttribute(graph, rfiNode, Stanag4559Constants.FOR_INFORMATION, RFI_FOR_INFORMATION);
        addStringAttribute(graph, rfiNode, Stanag4559Constants.SERIAL_NUMBER, RFI_SERIAL_NUM);
        addStringAttribute(graph, rfiNode, Stanag4559Constants.STATUS, RFI_STATUS);
        addStringAttribute(graph, rfiNode, Stanag4559Constants.WORKFLOW_STATUS, RFI_WORKFLOW_STATUS);
    }

    private void addTaskNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any taskAny = orb.create_any();
        Node taskNode = new Node(0, NodeType.ENTITY_NODE, Stanag4559Constants.NSIL_TASK, taskAny);
        graph.addVertex(taskNode);
        graph.addEdge(parentNode, taskNode);


        addStringAttribute(graph, taskNode, Stanag4559Constants.COMMENTS, TASK_COMMENTS);
        addStringAttribute(graph, taskNode, Stanag4559Constants.STATUS, TASK_STATUS);
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
