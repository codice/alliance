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
package org.codice.alliance.nsili.transformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.codice.alliance.nsili.common.CorbaUtils;
import org.codice.alliance.nsili.common.NsiliApprovalStatus;
import org.codice.alliance.nsili.common.NsiliCommonUtils;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.NsiliCxpMetacardType;
import org.codice.alliance.nsili.common.NsiliCxpStatusType;
import org.codice.alliance.nsili.common.NsiliExploitationSubQualCode;
import org.codice.alliance.nsili.common.NsiliGmtiMetacardType;
import org.codice.alliance.nsili.common.NsiliIRMetacardType;
import org.codice.alliance.nsili.common.NsiliImageryDecompressionTech;
import org.codice.alliance.nsili.common.NsiliImageryMetacardType;
import org.codice.alliance.nsili.common.NsiliImageryType;
import org.codice.alliance.nsili.common.NsiliMessageMetacardType;
import org.codice.alliance.nsili.common.NsiliMetacardType;
import org.codice.alliance.nsili.common.NsiliMetadataEncodingScheme;
import org.codice.alliance.nsili.common.NsiliProductType;
import org.codice.alliance.nsili.common.NsiliReportMetacardType;
import org.codice.alliance.nsili.common.NsiliReportPriority;
import org.codice.alliance.nsili.common.NsiliReportType;
import org.codice.alliance.nsili.common.NsiliRfiMetacardType;
import org.codice.alliance.nsili.common.NsiliRfiStatus;
import org.codice.alliance.nsili.common.NsiliRfiWorkflowStatus;
import org.codice.alliance.nsili.common.NsiliScanningMode;
import org.codice.alliance.nsili.common.NsiliSdsOpStatus;
import org.codice.alliance.nsili.common.NsiliTaskMetacardType;
import org.codice.alliance.nsili.common.NsiliTaskStatus;
import org.codice.alliance.nsili.common.NsiliTdlMetacardType;
import org.codice.alliance.nsili.common.NsiliVideoCategoryType;
import org.codice.alliance.nsili.common.NsiliVideoEncodingScheme;
import org.codice.alliance.nsili.common.NsiliVideoMetacardType;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.AbsTimeHelper;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeHelper;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.RectangleHelper;
import org.codice.alliance.nsili.common.UCO.Time;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.Resource;
import ddf.catalog.resource.impl.URLResourceReader;

public class DAGConverterTest {

    private static final String CARD_ID = "Card ID";

    private static final String SOURCE_PUBLISHER = "Source Publisher";

    private static final String SOURCE_LIBRARY = "SourceLibrary";

    private static final String ARCHIVE_INFORMATION = "Archive Information";

    private static final Boolean STREAM_ARCHIVED = false;

    private static final String STREAM_CREATOR = "Stream Creator";

    private static final String STREAM_STANDARD = "STANAG4609";

    private static final String STREAM_STANDARD_VER = "1.0";

    private static final String STREAM_SOURCE_URL = "http://localhost:1234/stream";

    private static final Short STREAM_PROGRAM_ID = 3;

    private static final String CLASS_POLICY = "NATO/EU";

    private static final String CLASS_CLASSIFICATION = "UNCLASSIFIED";

    private static final String CLASS_RELEASABILITY = "NATO";

    private static final String COM_DESCRIPTION_ABSTRACT = "Product Description";

    private static final String COM_ID_MSN = "CX100";

    private static final String COM_ID_UUID = UUID.randomUUID()
            .toString();

    private static final Integer COM_JC3ID = 1234;

    private static final String COM_LANGUAGE = "eng";

    private static final String COM_SOURCE = "TestSourceSystem";

    private static final String COM_SUBJECT_CATEGORY_TARGET = "Airfields";

    private static final String COM_TARGET_NUMBER = "123-456-7890";

    private static final NsiliProductType COM_TYPE = NsiliProductType.COLLECTION_EXPLOITATION_PLAN;

    private static final String COVERAGE_COUNTRY_CD = "USA";

    private static final Double UPPER_LEFT_LAT = 5.0;

    private static final Double UPPER_LEFT_LON = 1.0;

    private static final Double LOWER_RIGHT_LAT = 1.0;

    private static final Double LOWER_RIGHT_LON = 5.0;

    private static final String EXPLOITATION_DESC = "Exploitation Info Description";

    private static final Short EXPLOITATION_LEVEL = 0;

    private static final Boolean EXPLOITATION_AUTO_GEN = false;

    private static final String EXPLOITATION_SUBJ_QUAL_CODE =
            NsiliExploitationSubQualCode.GOOD.toString();

    private static final String IMAGERY_CATEGORY = NsiliImageryType.VIS.toString();

    private static final String BAD_ENUM_VALUE = "Not Valid";

    private static final Short IMAGERY_CLOUD_COVER_PCT = 35;

    private static final String IMAGERY_COMMENTS = "Imagery Comments";

    private static final String IMAGERY_DECOMPRESSION_TECH =
            NsiliImageryDecompressionTech.C1.toString();

    private static final String IMAGERY_IDENTIFIER = "1234";

    private static final Short IMAGERY_NIIRS = 2;

    private static final Integer IMAGERY_NUM_BANDS = 5000;

    private static final Integer IMAGERY_NUM_ROWS = 500;

    private static final Integer IMAGERY_NUM_COLS = 400;

    private static final String IMAGERY_TITLE = "Imagery Title";

    private static final String WKT_LOCATION = "POLYGON ((5 5, 5 1, 1 1, 1 5, 5 5))";

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

    private static final String VIDEO_CATEGORY = NsiliVideoCategoryType.VIS.name();

    private static final NsiliVideoEncodingScheme VIDEO_ENCODING_SCHEME =
            NsiliVideoEncodingScheme.MPEG2;

    private static final Double VIDEO_FRAME_RATE = 15.4;

    private static final Integer VIDEO_NUM_ROWS = 500;

    private static final Integer VIDEO_NUM_COLS = 400;

    private static final String VIDEO_METADATA_ENC_SCHEME = NsiliMetadataEncodingScheme.KLV.name();

    private static final Short VIDEO_MISM_LEVEL = 4;

    private static final String VIDEO_SCANNING_MODE = NsiliScanningMode.PROGRESSIVE.name();

    private static final String REPORT_REQ_SERIAL_NUM = "112233";

    private static final String REPORT_PRIORITY = NsiliReportPriority.FLASH.name();

    private static final String REPORT_TYPE = NsiliReportType.MTIEXREP.name();

    private static final Short TDL_ACTIVITY = 99;

    private static final String TDL_MESSAGE_NUM = "J3.2";

    private static final Short TDL_PLATFORM_NUM = 42;

    private static final String TDL_TRACK_NUM = "AK320";

    private static final String CXP_STATUS = NsiliCxpStatusType.CURRENT.name();

    private static final String RFI_FOR_ACTION = "USAF";

    private static final String RFI_FOR_INFORMATION = "USMC";

    private static final String RFI_SERIAL_NUM = "123456";

    private static final String RFI_STATUS = NsiliRfiStatus.APPROVED.name();

    private static final String RFI_WORKFLOW_STATUS = NsiliRfiWorkflowStatus.ACCEPTED.name();

    private static final String TASK_COMMENTS = "Task Comments";

    private static final String TASK_STATUS = NsiliTaskStatus.INTERRUPTED.name();

    private static final String SOURCE_ID = "myNsiliSource";

    private static final Integer NUM_ASSOCIATIONS = 5;

    private static final NsiliApprovalStatus APPROVAL_STATUS = NsiliApprovalStatus.NOT_APPLICABLE;

    private static final String APPROVED_BY = "ApprovedBy";

    private static final NsiliSdsOpStatus SDS_OP_STATUS = NsiliSdsOpStatus.LIMITED_OPERATIONAL;

    private ORB orb;

    private Calendar cal;

    private DAGConverter dagConverter;

    private URLResourceReader mockResourceReader = mock(URLResourceReader.class);

    private static final boolean SHOULD_PRINT_CARD = false;

    @Before
    public void setUp() throws Exception {
        this.orb = ORB.init();

        int year = 2016;
        int month = 01;
        int dayOfMonth = 29;
        int hourOfDay = 17;
        int minute = 05;
        int second = 10;
        cal = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
        dagConverter = new DAGConverter(mockResourceReader);

        setupMocks();
    }

    /**
     * Test the Imagery View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_STREAM
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_IMAGERY
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testImageryViewConversion() {
        DAG imageryDAG = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addImageryPart(graph, productNode);
        addAssocationNode(graph, productNode);
        addApprovalNode(graph, productNode);
        addSdsNode(graph, productNode);
        addRelatedFile(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        imageryDAG.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        imageryDAG.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(imageryDAG, false, SOURCE_ID);

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
        assertThat(NsiliImageryMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(FILE_TITLE, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.IMAGERY.toString(), is(metacard.getContentTypeName()));
        assertThat(FILE_FORMAT_VER, is(metacard.getContentTypeVersion()));
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkImageryAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
        checkAssociationAttribute(metacard);
        checkApprovalAttribute(metacard);
        checkSdsAttribute(metacard);

        DAGConverter.logMetacard(metacard, "123");
    }

    @Test
    public void testSwapCoordinates() {
        String swapWktLocation = "POLYGON ((1 1, 1 5, 5 5, 5 1, 1 1))";

        DAG imageryDAG = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addImageryPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        imageryDAG.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        imageryDAG.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(imageryDAG, true, SOURCE_ID);

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
        assertThat(NsiliImageryMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(metacard.getLocation(), is(swapWktLocation));
    }

    private void checkExploitationInfoAttributes(MetacardImpl metacard) {
        Attribute exploitationDescAttr =
                metacard.getAttribute(NsiliMetacardType.EXPLOITATION_DESCRIPTION);
        assertThat(exploitationDescAttr, notNullValue());
        assertThat(EXPLOITATION_DESC,
                is(exploitationDescAttr.getValue()
                        .toString()));

        Attribute exploitationLevelAttr =
                metacard.getAttribute(NsiliMetacardType.EXPLOITATION_LEVEL);
        assertThat(exploitationLevelAttr, notNullValue());
        assertThat(EXPLOITATION_LEVEL, is((short) exploitationLevelAttr.getValue()));

        Attribute exploitationAutoGenAttr =
                metacard.getAttribute(NsiliMetacardType.EXPLOITATION_AUTO_GEN);
        assertThat(exploitationAutoGenAttr, notNullValue());
        assertThat(EXPLOITATION_AUTO_GEN, is((boolean) exploitationAutoGenAttr.getValue()));

        Attribute subjQualCodeAttr =
                metacard.getAttribute(NsiliMetacardType.EXPLOITATION_SUBJ_QUAL_CODE);
        assertThat(subjQualCodeAttr, notNullValue());
        assertThat(EXPLOITATION_SUBJ_QUAL_CODE,
                is(subjQualCodeAttr.getValue()
                        .toString()));
    }

    private void checkStreamAttributes(MetacardImpl metacard) {
        Attribute archivedAttr = metacard.getAttribute(NsiliMetacardType.STREAM_ARCHIVED);
        assertThat(archivedAttr, notNullValue());
        assertThat(STREAM_ARCHIVED, is((boolean) archivedAttr.getValue()));

        Attribute archivalInfoAttr = metacard.getAttribute(NsiliMetacardType.STREAM_ARCHIVAL_INFO);
        assertThat(archivalInfoAttr, notNullValue());
        assertThat(ARCHIVE_INFORMATION, is(archivalInfoAttr.getValue()));

        Attribute creatorAttr = metacard.getAttribute(NsiliMetacardType.STREAM_CREATOR);
        assertThat(creatorAttr, notNullValue());
        assertThat(STREAM_CREATOR,
                is(creatorAttr.getValue()
                        .toString()));

        Attribute dateTimeDeclaredAttr =
                metacard.getAttribute(NsiliMetacardType.STREAM_DATETIME_DECLARED);
        assertThat(dateTimeDeclaredAttr, notNullValue());
        assertThat(cal.getTime(), is(dateTimeDeclaredAttr.getValue()));

        Attribute programIdAttr = metacard.getAttribute(NsiliMetacardType.STREAM_PROGRAM_ID);
        assertThat(programIdAttr, notNullValue());
        assertThat(STREAM_PROGRAM_ID, is((short) programIdAttr.getValue()));

    }

    private void checkCommonAttributes(MetacardImpl metacard) {
        Attribute identifierMsnAttr = metacard.getAttribute(NsiliMetacardType.IDENTIFIER_MISSION);
        assertThat(identifierMsnAttr, notNullValue());
        assertThat(COM_ID_MSN,
                is(identifierMsnAttr.getValue()
                        .toString()));

        Attribute identifierJc3idmAttr = metacard.getAttribute(NsiliMetacardType.ID_JC3IEDM);
        assertThat(identifierJc3idmAttr, notNullValue());
        assertThat(COM_JC3ID, is((int) identifierJc3idmAttr.getValue()));

        Attribute languageAttr = metacard.getAttribute(NsiliMetacardType.LANGUAGE);
        assertThat(languageAttr, notNullValue());
        assertThat(COM_LANGUAGE,
                is(languageAttr.getValue()
                        .toString()));

        Attribute stanagSourceAttr = metacard.getAttribute(NsiliMetacardType.SOURCE);
        assertThat(stanagSourceAttr, notNullValue());
        assertThat(COM_SOURCE,
                is(stanagSourceAttr.getValue()
                        .toString()));

        Attribute subjCatTgtAttr = metacard.getAttribute(NsiliMetacardType.SUBJECT_CATEGORY_TARGET);
        assertThat(subjCatTgtAttr, notNullValue());
        assertThat(COM_SUBJECT_CATEGORY_TARGET,
                is(subjCatTgtAttr.getValue()
                        .toString()));

        Attribute tgtNumAttr = metacard.getAttribute(NsiliMetacardType.TARGET_NUMBER);
        assertThat(tgtNumAttr, notNullValue());
        assertThat(COM_TARGET_NUMBER,
                is(tgtNumAttr.getValue()
                        .toString()));

        Attribute productTypeAttr = metacard.getAttribute(NsiliMetacardType.PRODUCT_TYPE);
        assertThat(productTypeAttr, notNullValue());
        assertThat(COM_TYPE.name(),
                is(productTypeAttr.getValue()
                        .toString()));
    }

    private void checkImageryAttributes(MetacardImpl metacard) {
        Attribute cloudCoverPctAttr =
                metacard.getAttribute(NsiliImageryMetacardType.CLOUD_COVER_PCT);
        assertThat(cloudCoverPctAttr, notNullValue());
        assertThat(IMAGERY_CLOUD_COVER_PCT, is((short) cloudCoverPctAttr.getValue()));

        Attribute imageryCommentsAttr =
                metacard.getAttribute(NsiliImageryMetacardType.IMAGERY_COMMENTS);
        assertThat(imageryCommentsAttr, notNullValue());
        assertThat(IMAGERY_COMMENTS, is(imageryCommentsAttr.getValue()));

        Attribute imageryCategoryAttr =
                metacard.getAttribute(NsiliImageryMetacardType.IMAGERY_CATEGORY);
        assertThat(imageryCategoryAttr, notNullValue());
        assertThat(IMAGERY_CATEGORY,
                is(imageryCategoryAttr.getValue()
                        .toString()));

        Attribute decompressionTechAttr =
                metacard.getAttribute(NsiliImageryMetacardType.DECOMPRESSION_TECHNIQUE);
        assertThat(decompressionTechAttr, notNullValue());
        assertThat(IMAGERY_DECOMPRESSION_TECH,
                is(decompressionTechAttr.getValue()
                        .toString()));

        Attribute imageIdAttr = metacard.getAttribute(NsiliImageryMetacardType.IMAGE_ID);
        assertThat(imageIdAttr, notNullValue());
        assertThat(IMAGERY_IDENTIFIER,
                is(imageIdAttr.getValue()
                        .toString()));

        Attribute niirsAttr = metacard.getAttribute(NsiliImageryMetacardType.NIIRS);
        assertThat(niirsAttr, notNullValue());
        assertThat(IMAGERY_NIIRS, is((short) niirsAttr.getValue()));

        Attribute numBandsAttr = metacard.getAttribute(NsiliImageryMetacardType.NUM_BANDS);
        assertThat(numBandsAttr, notNullValue());
        assertThat(IMAGERY_NUM_BANDS, is((int) numBandsAttr.getValue()));

        Attribute numRowsAttr = metacard.getAttribute(NsiliImageryMetacardType.NUM_ROWS);
        assertThat(numRowsAttr, notNullValue());
        assertThat(IMAGERY_NUM_ROWS, is((int) numRowsAttr.getValue()));

        Attribute numColsAttr = metacard.getAttribute(NsiliImageryMetacardType.NUM_COLS);
        assertThat(numColsAttr, notNullValue());
        assertThat(IMAGERY_NUM_COLS, is((int) numColsAttr.getValue()));

        Attribute endDateTimeAttr = metacard.getAttribute(NsiliImageryMetacardType.END_DATETIME);
        assertThat(endDateTimeAttr, notNullValue());
        assertThat(cal.getTime(), is(endDateTimeAttr.getValue()));

        assertThat(metacard.getResourceSize(), notNullValue());
        int size = Integer.parseInt(metacard.getResourceSize());
        assertThat(size, is(DAGConverter.convertMegabytesToBytes(FILE_EXTENT)));
    }

    private void checkSecurityAttributes(MetacardImpl metacard) {
        Attribute classificationAttr =
                metacard.getAttribute(NsiliMetacardType.SECURITY_CLASSIFICATION);
        assertThat(classificationAttr, notNullValue());
        assertThat(CLASS_CLASSIFICATION,
                is(classificationAttr.getValue()
                        .toString()));

        Attribute policyAttr = metacard.getAttribute(NsiliMetacardType.SECURITY_POLICY);
        assertThat(policyAttr, notNullValue());
        assertThat(CLASS_POLICY,
                is(policyAttr.getValue()
                        .toString()));

        Attribute releasabilityAttr =
                metacard.getAttribute(NsiliMetacardType.SECURITY_RELEASABILITY);
        assertThat(releasabilityAttr, notNullValue());
        assertThat(CLASS_RELEASABILITY,
                is(releasabilityAttr.getValue()
                        .toString()));
    }

    private void checkCoverageAttributes(MetacardImpl metacard) {
        Attribute spatialCtryCodeAttr = metacard.getAttribute(NsiliMetacardType.COUNTRY_CODE);
        assertThat(spatialCtryCodeAttr, notNullValue());
        assertThat(COVERAGE_COUNTRY_CD,
                is(spatialCtryCodeAttr.getValue()
                        .toString()));

        Attribute startTimeAttr = metacard.getAttribute(NsiliMetacardType.START_DATETIME);
        assertThat(startTimeAttr, notNullValue());
        assertThat(cal.getTime(), is(startTimeAttr.getValue()));

        Attribute endTimeAttr = metacard.getAttribute(NsiliMetacardType.END_DATETIME);
        assertThat(endTimeAttr, notNullValue());
        assertThat(cal.getTime(), is(endTimeAttr.getValue()));
    }

    private void checkAssociationAttribute(MetacardImpl metacard) {
        Attribute associationsAttr = metacard.getAttribute(NsiliMetacardType.ASSOCIATIONS);
        assertThat(associationsAttr, notNullValue());
        List<Serializable> associations = associationsAttr.getValues();
        assertThat(NUM_ASSOCIATIONS, is(associations.size()));
    }

    private void checkApprovalAttribute(MetacardImpl metacard) {
        Attribute approvedByAttr = metacard.getAttribute(NsiliMetacardType.APPROVAL_BY);
        assertThat(approvedByAttr, notNullValue());
        assertThat(APPROVED_BY,
                is(approvedByAttr.getValue()
                        .toString()));

        Attribute modifiedAttr =
                metacard.getAttribute(NsiliMetacardType.APPROVAL_DATETIME_MODIFIED);
        assertThat(modifiedAttr, notNullValue());
        assertThat(cal.getTime(), is(modifiedAttr.getValue()));

        Attribute statusAttr = metacard.getAttribute(NsiliMetacardType.APPROVAL_STATUS);
        assertThat(statusAttr, notNullValue());
        assertThat(APPROVAL_STATUS.name(),
                is(statusAttr.getValue()
                        .toString()));
    }

    private void checkSdsAttribute(MetacardImpl metacard) {
        Attribute opStatusAttr = metacard.getAttribute(NsiliMetacardType.SDS_OPERATIONAL_STATUS);
        assertThat(opStatusAttr, notNullValue());
        assertThat(SDS_OP_STATUS.name(),
                is(opStatusAttr.getValue()
                        .toString()));
    }

    /**
     * Test the GMTI View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_STREAM
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_GMTI
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
     */
    @Test
    public void testGmtiViewConversion() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addGmtiPart(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliGmtiMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.GMTI.toString(), is(metacard.getContentTypeName()));
        assertThat(STREAM_STANDARD_VER, is(metacard.getContentTypeVersion()));
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(STREAM_SOURCE_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkGmtiAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkGmtiAttributes(MetacardImpl metacard) {
        Attribute gmtiJobAttr = metacard.getAttribute(NsiliGmtiMetacardType.JOB_ID);
        assertThat(gmtiJobAttr, notNullValue());
        assertThat(GMTI_JOB_ID, is(gmtiJobAttr.getValue()));

        Attribute numTgtAttr = metacard.getAttribute(NsiliGmtiMetacardType.NUM_TARGET_REPORTS);
        assertThat(numTgtAttr, notNullValue());
        assertThat(GMTI_TARGET_REPORTS, is((int) numTgtAttr.getValue()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_STREAM
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_MESSAGE
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
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

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliMessageMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(MESSAGE_SUBJECT, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.MESSAGE.toString(), is(metacard.getContentTypeName()));
        assertThat(metacard.getContentTypeVersion(), nullValue());
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(MESSAGE_BODY, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkMessageAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkMessageAttributes(MetacardImpl metacard) {
        Attribute messageRecipAttr = metacard.getAttribute(NsiliMessageMetacardType.RECIPIENT);
        assertThat(messageRecipAttr, notNullValue());
        assertThat(MESSAGE_RECIPIENT, is(messageRecipAttr.getValue()));

        Attribute typeAttr = metacard.getAttribute(NsiliMessageMetacardType.MESSAGE_TYPE);
        assertThat(typeAttr, notNullValue());
        assertThat(MESSAGE_TYPE,
                is(typeAttr.getValue()
                        .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_STREAM
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_VIDEO
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
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

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliVideoMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(FILE_TITLE, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.VIDEO.toString(), is(metacard.getContentTypeName()));
        assertThat(metacard.getContentTypeVersion(), nullValue());
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkVideoAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    @Test
    public void testOnlyRootNodeDAG() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(rootNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);
        assertThat(metacard.getTitle(), nullValue());
    }

    @Test
    public void testRootNodeNotProduct() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = new Node(0,
                NodeType.ROOT_NODE,
                NsiliConstants.NSIL_APPROVAL,
                orb.create_any());
        graph.addVertex(rootNode);

        Node attribNode = new Node(0, NodeType.ATTRIBUTE_NODE, NsiliConstants.NSIL_CARD, null);
        graph.addVertex(attribNode);
        graph.addEdge(rootNode, attribNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(rootNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);
        assertThat(metacard.getTitle(), nullValue());
    }

    @Test
    public void testAttributeWithNoValue() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        Node entityNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_CARD,
                orb.create_any());
        graph.addVertex(entityNode);
        graph.addEdge(rootNode, entityNode);

        Node attrNode = new Node(0, NodeType.ATTRIBUTE_NODE, NsiliConstants.STATUS, null);
        graph.addVertex(attrNode);
        graph.addEdge(entityNode, attrNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(rootNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);
        assertThat(metacard.getTitle(), nullValue());
    }

    @Test
    public void testRecordNodePresent() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        Node recordNode = new Node(0,
                NodeType.RECORD_NODE,
                NsiliConstants.NSIL_CARD,
                orb.create_any());
        graph.addVertex(recordNode);
        graph.addEdge(rootNode, recordNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(rootNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);
        assertThat(metacard.getTitle(), nullValue());
    }

    @Test
    public void testEmptyDAG() {
        DAG dag = new DAG();
        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);
        assertThat(metacard, nullValue());
    }

    @Test
    public void testDAGNoEdges() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);
        assertThat(metacard, nullValue());
    }

    @Test
    public void testStartNodeOfEdgeNull() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        Edge[] edges = new Edge[1];
        Edge edge = new Edge(0, 1, "");
        edges[0] = edge;
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);
        dag.edges = edges;

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);
        assertThat(metacard.getTitle(), nullValue());
    }

    @Test
    public void testEndNodeOfEdgeNull() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        //Create invalid root node
        Node rootNode = createRootNode();
        graph.addVertex(rootNode);

        Edge[] edges = new Edge[1];
        Edge edge = new Edge(1, 2, "");
        edges[0] = edge;
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);
        dag.edges = edges;

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);
        assertThat(metacard.getTitle(), nullValue());
    }

    private void checkVideoAttributes(MetacardImpl metacard) {
        Attribute avgBitRateAttr = metacard.getAttribute(NsiliVideoMetacardType.AVG_BIT_RATE);
        assertThat(avgBitRateAttr, notNullValue());
        assertThat(VIDEO_AVG_BIT_RATE, is(avgBitRateAttr.getValue()));

        Attribute categoryAttr = metacard.getAttribute(NsiliVideoMetacardType.CATEGORY);
        assertThat(categoryAttr, notNullValue());
        assertThat(VIDEO_CATEGORY,
                is(categoryAttr.getValue()
                        .toString()));

        Attribute encodingSchemeAttr =
                metacard.getAttribute(NsiliVideoMetacardType.ENCODING_SCHEME);
        assertThat(encodingSchemeAttr, notNullValue());
        assertThat(VIDEO_ENCODING_SCHEME.name(),
                is(encodingSchemeAttr.getValue()
                        .toString()));

        Attribute frameRateAttr = metacard.getAttribute(NsiliVideoMetacardType.FRAME_RATE);
        assertThat(frameRateAttr, notNullValue());
        assertThat(VIDEO_FRAME_RATE, is(frameRateAttr.getValue()));

        Attribute numRowsAttr = metacard.getAttribute(NsiliVideoMetacardType.NUM_ROWS);
        assertThat(numRowsAttr, notNullValue());
        assertThat(VIDEO_NUM_ROWS, is((int) numRowsAttr.getValue()));

        Attribute numColsAttr = metacard.getAttribute(NsiliVideoMetacardType.NUM_COLS);
        assertThat(numColsAttr, notNullValue());
        assertThat(VIDEO_NUM_COLS, is((int) numColsAttr.getValue()));

        Attribute metadataEncSchemeAttr =
                metacard.getAttribute(NsiliVideoMetacardType.METADATA_ENCODING_SCHEME);
        assertThat(metadataEncSchemeAttr, notNullValue());
        assertThat(VIDEO_METADATA_ENC_SCHEME,
                is(metadataEncSchemeAttr.getValue()
                        .toString()));

        Attribute mismLevelAttr = metacard.getAttribute(NsiliVideoMetacardType.MISM_LEVEL);
        assertThat(mismLevelAttr, notNullValue());
        assertThat(VIDEO_MISM_LEVEL, is((short) mismLevelAttr.getValue()));

        Attribute scanningModeAttr = metacard.getAttribute(NsiliVideoMetacardType.SCANNING_MODE);
        assertThat(scanningModeAttr, notNullValue());
        assertThat(VIDEO_SCANNING_MODE,
                is(scanningModeAttr.getValue()
                        .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_REPORT
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
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

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliReportMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(FILE_TITLE, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.REPORT.toString(), is(metacard.getContentTypeName()));
        assertThat(FILE_FORMAT_VER, is(metacard.getContentTypeVersion()));
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkReportAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkReportAttributes(MetacardImpl metacard) {
        Attribute origReqSerialAttr =
                metacard.getAttribute(NsiliReportMetacardType.ORIGINATOR_REQ_SERIAL_NUM);
        assertThat(origReqSerialAttr, notNullValue());
        assertThat(REPORT_REQ_SERIAL_NUM,
                is(origReqSerialAttr.getValue()
                        .toString()));

        Attribute priorityAttr = metacard.getAttribute(NsiliReportMetacardType.PRIORITY);
        assertThat(priorityAttr, notNullValue());
        assertThat(REPORT_PRIORITY,
                is(priorityAttr.getValue()
                        .toString()));

        Attribute typeAttr = metacard.getAttribute(NsiliReportMetacardType.TYPE);
        assertThat(typeAttr, notNullValue());
        assertThat(REPORT_TYPE,
                is(typeAttr.getValue()
                        .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_CXP
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
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

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliCxpMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(FILE_TITLE, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.COLLECTION_EXPLOITATION_PLAN.toString(),
                is(metacard.getContentTypeName()));
        assertThat(FILE_FORMAT_VER, is(metacard.getContentTypeVersion()));
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkCxpAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkCxpAttributes(MetacardImpl metacard) {
        Attribute cxpAttr = metacard.getAttribute(NsiliCxpMetacardType.STATUS);
        assertThat(cxpAttr, notNullValue());
        assertThat(CXP_STATUS,
                is(cxpAttr.getValue()
                        .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_IR
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
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

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliIRMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(FILE_TITLE, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.DOCUMENT.toString(), is(metacard.getContentTypeName()));
        assertThat(FILE_FORMAT_VER, is(metacard.getContentTypeVersion()));
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

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
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_RFI
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
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

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliRfiMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(FILE_TITLE, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.RFI.toString(), is(metacard.getContentTypeName()));
        assertThat(FILE_FORMAT_VER, is(metacard.getContentTypeVersion()));
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkRFIAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkRFIAttributes(MetacardImpl metacard) {
        Attribute forActionAttr = metacard.getAttribute(NsiliRfiMetacardType.FOR_ACTION);
        assertThat(forActionAttr, notNullValue());
        assertThat(RFI_FOR_ACTION,
                is(forActionAttr.getValue()
                        .toString()));

        Attribute forInfoAttr = metacard.getAttribute(NsiliRfiMetacardType.FOR_INFORMATION);
        assertThat(forInfoAttr, notNullValue());
        assertThat(RFI_FOR_INFORMATION,
                is(forInfoAttr.getValue()
                        .toString()));

        Attribute serialNumAttr = metacard.getAttribute(NsiliRfiMetacardType.SERIAL_NUMBER);
        assertThat(serialNumAttr, notNullValue());
        assertThat(RFI_SERIAL_NUM,
                is(serialNumAttr.getValue()
                        .toString()));

        Attribute statusAttr = metacard.getAttribute(NsiliRfiMetacardType.STATUS);
        assertThat(statusAttr, notNullValue());
        assertThat(RFI_STATUS,
                is(statusAttr.getValue()
                        .toString()));

        Attribute workflowStatusAttr = metacard.getAttribute(NsiliRfiMetacardType.WORKFLOW_STATUS);
        assertThat(workflowStatusAttr, notNullValue());
        assertThat(RFI_WORKFLOW_STATUS,
                is(workflowStatusAttr.getValue()
                        .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_TASK
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
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

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliTaskMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(FILE_TITLE, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.TASK.toString(), is(metacard.getContentTypeName()));
        assertThat(FILE_FORMAT_VER, is(metacard.getContentTypeVersion()));
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkTaskAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    @Test
    public void testGetNodeValue() {
        Any any = orb.create_any();
        any.insert_long(12);
        String value = CorbaUtils.getNodeValue(any);
        assertThat(value, notNullValue());
        assertThat(value.isEmpty(), is(false));

        any = orb.create_any();
        any.insert_string("test string");
        value = CorbaUtils.getNodeValue(any);
        assertThat(value, notNullValue());
        assertThat(value.isEmpty(), is(false));

        any = orb.create_any();
        any.insert_boolean(false);
        value = CorbaUtils.getNodeValue(any);
        assertThat(value, notNullValue());
        assertThat(value.isEmpty(), is(false));

        any = orb.create_any();
        any.insert_short((short) 12);
        value = CorbaUtils.getNodeValue(any);
        assertThat(value, notNullValue());
        assertThat(value.isEmpty(), is(false));

        any = orb.create_any();
        AbsTimeHelper.insert(any, getTestTime());
        value = CorbaUtils.getNodeValue(any);
        assertThat(value, notNullValue());
        assertThat(value.isEmpty(), is(false));

        any = orb.create_any();
        Any any2 = orb.create_any();
        any2.insert_string("test value");
        NodeHelper.insert(any, new Node(1, NodeType.ATTRIBUTE_NODE, "test", any2));
        value = CorbaUtils.getNodeValue(any);
        assertThat(value, nullValue());
    }

    private void checkTaskAttributes(MetacardImpl metacard) {
        Attribute commentAttr = metacard.getAttribute(NsiliTaskMetacardType.COMMENTS);
        assertThat(commentAttr, notNullValue());
        assertThat(TASK_COMMENTS,
                is(commentAttr.getValue()
                        .toString()));

        Attribute statusAttr = metacard.getAttribute(NsiliTaskMetacardType.STATUS);
        assertThat(statusAttr, notNullValue());
        assertThat(TASK_STATUS,
                is(statusAttr.getValue()
                        .toString()));
    }

    /**
     * Test the Message View DAG to Metacard
     * <p>
     * NSIL_PRODUCT
     * NSIL_APPROVAL
     * NSIL_CARD
     * NSIL_STREAM
     * NSIL_FILE
     * NSIL_METADATASECURITY
     * NSIL_RELATED_FILE
     * NSIL_SECURITY
     * NSIL_PART
     * NSIL_SECURITY
     * NSIL_COMMON
     * NSIL_COVERAGE
     * NSIL_EXPLOITATION_INFO
     * NSIL_TDL
     * NSIL_ASSOCIATION
     * NSIL_RELATION
     * NSIL_SOURCE
     * NSIL_CARD
     * NSIL_DESTINATION
     * NSIL_CARD
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

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

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
        assertThat(NsiliTdlMetacardType.class.getCanonicalName(),
                is(metacard.getMetacardType()
                        .getClass()
                        .getCanonicalName()));
        assertThat(FILE_TITLE, is(metacard.getTitle()));
        assertThat(CARD_ID, is(metacard.getId()));
        assertThat(NsiliProductType.TDL_DATA.toString(), is(metacard.getContentTypeName()));
        assertThat(FILE_FORMAT_VER, is(metacard.getContentTypeVersion()));
        assertThat(metacard.getCreatedDate(), notNullValue());
        assertThat(metacard.getEffectiveDate(), notNullValue());
        assertThat(cal.getTime(), is(metacard.getModifiedDate()));
        assertThat(COM_DESCRIPTION_ABSTRACT, is(metacard.getDescription()));
        assertThat(metacard.getLocation(), is(WKT_LOCATION));
        assertThat(FILE_PRODUCT_URL,
                is(metacard.getAttribute(Metacard.RESOURCE_URI)
                        .getValue()
                        .toString()));

        checkCommonAttributes(metacard);
        checkExploitationInfoAttributes(metacard);
        checkStreamAttributes(metacard);
        checkTdlAttributes(metacard);
        checkSecurityAttributes(metacard);
        checkCoverageAttributes(metacard);
    }

    private void checkTdlAttributes(MetacardImpl metacard) {
        Attribute activityAttr = metacard.getAttribute(NsiliTdlMetacardType.ACTIVITY);
        assertThat(activityAttr, notNullValue());
        assertThat(TDL_ACTIVITY, is((short) activityAttr.getValue()));

        Attribute msgNumAttr = metacard.getAttribute(NsiliTdlMetacardType.MESSAGE_NUM);
        assertThat(msgNumAttr, notNullValue());
        assertThat(TDL_MESSAGE_NUM,
                is(msgNumAttr.getValue()
                        .toString()));

        Attribute platformNumAttr = metacard.getAttribute(NsiliTdlMetacardType.PLATFORM);
        assertThat(platformNumAttr, notNullValue());
        assertThat(TDL_PLATFORM_NUM, is((short) platformNumAttr.getValue()));

        Attribute trackNumAttr = metacard.getAttribute(NsiliTdlMetacardType.TRACK_NUM);
        assertThat(trackNumAttr, notNullValue());
        assertThat(TDL_TRACK_NUM,
                is(trackNumAttr.getValue()
                        .toString()));
    }

    @Test
    public void testBadEnumValues() {
        DAG dag = new DAG();
        DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

        Node productNode = createRootNode();
        graph.addVertex(productNode);

        addCardNode(graph, productNode);
        addFileNode(graph, productNode);
        addStreamNode(graph, productNode);
        addMetadataSecurity(graph, productNode);
        addSecurityNode(graph, productNode);
        addBadImageryNode(graph, productNode);
        addBadVideoPart(graph, productNode);
        addBadCxpNode(graph, productNode);
        addBadRFINode(graph, productNode);
        addBadTaskNode(graph, productNode);
        addBadExpoloitationInfoNode(graph, productNode);
        addBadSdsNode(graph, productNode);
        addBadApprovalNode(graph, productNode);
        addBadReportNode(graph, productNode);

        graph.addVertex(productNode);

        NsiliCommonUtils.setUCOEdgeIds(graph);
        NsiliCommonUtils.setUCOEdges(productNode, graph);
        dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
        dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

        MetacardImpl metacard = dagConverter.convertDAG(dag, false, SOURCE_ID);

        //Check top-level meta-card attributes
        assertThat(CARD_ID, is(metacard.getId()));
    }

    private Node createRootNode() {
        return new Node(0, NodeType.ROOT_NODE, NsiliConstants.NSIL_PRODUCT, orb.create_any());
    }

    private void addCardNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node cardNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CARD, any);
        graph.addVertex(cardNode);
        graph.addEdge(productNode, cardNode);

        ResultDAGConverter.addStringAttribute(graph,
                cardNode,
                NsiliConstants.IDENTIFIER,
                CARD_ID,
                orb);
        addTestDateAttribute(graph, cardNode, NsiliConstants.SOURCE_DATE_TIME_MODIFIED, orb);
        addTestDateAttribute(graph, cardNode, NsiliConstants.DATE_TIME_MODIFIED, orb);
        ResultDAGConverter.addStringAttribute(graph,
                cardNode,
                NsiliConstants.PUBLISHER,
                SOURCE_PUBLISHER,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                cardNode,
                NsiliConstants.SOURCE_LIBRARY,
                SOURCE_LIBRARY,
                orb);
    }

    private void addFileNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node fileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_FILE, any);
        graph.addVertex(fileNode);
        graph.addEdge(productNode, fileNode);

        ResultDAGConverter.addBooleanAttribute(graph,
                fileNode,
                NsiliConstants.ARCHIVED,
                FILE_ARCHIVED,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                fileNode,
                NsiliConstants.ARCHIVE_INFORMATION,
                FILE_ARCHIVE_INFO,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                fileNode,
                NsiliConstants.CREATOR,
                FILE_CREATOR,
                orb);
        addTestDateAttribute(graph, fileNode, NsiliConstants.DATE_TIME_DECLARED, orb);
        ResultDAGConverter.addDoubleAttribute(graph,
                fileNode,
                NsiliConstants.EXTENT,
                FILE_EXTENT,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                fileNode,
                NsiliConstants.FORMAT,
                FILE_FORMAT,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                fileNode,
                NsiliConstants.FORMAT_VERSION,
                FILE_FORMAT_VER,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                fileNode,
                NsiliConstants.PRODUCT_URL,
                FILE_PRODUCT_URL,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                fileNode,
                NsiliConstants.TITLE,
                FILE_TITLE,
                orb);
    }

    private void addRelatedFile(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node relatedFileNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_RELATED_FILE,
                any);
        graph.addVertex(relatedFileNode);
        graph.addEdge(productNode, relatedFileNode);

        ResultDAGConverter.addStringAttribute(graph,
                relatedFileNode,
                NsiliConstants.CREATOR,
                FILE_CREATOR,
                orb);
        addTestDateAttribute(graph, relatedFileNode, NsiliConstants.DATE_TIME_DECLARED, orb);
        ResultDAGConverter.addDoubleAttribute(graph,
                relatedFileNode,
                NsiliConstants.EXTENT,
                FILE_EXTENT,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                relatedFileNode,
                NsiliConstants.FILE_TYPE,
                NsiliConstants.THUMBNAIL_TYPE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                relatedFileNode,
                NsiliConstants.URL,
                FILE_PRODUCT_URL,
                orb);
        ResultDAGConverter.addBooleanAttribute(graph,
                relatedFileNode,
                NsiliConstants.IS_FILE_LOCAL,
                true,
                orb);
    }

    private void addStreamNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node streamNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_STREAM, any);
        graph.addVertex(streamNode);
        graph.addEdge(productNode, streamNode);

        ResultDAGConverter.addBooleanAttribute(graph,
                streamNode,
                NsiliConstants.ARCHIVED,
                STREAM_ARCHIVED,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                streamNode,
                NsiliConstants.ARCHIVE_INFORMATION,
                ARCHIVE_INFORMATION,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                streamNode,
                NsiliConstants.CREATOR,
                STREAM_CREATOR,
                orb);
        addTestDateAttribute(graph, streamNode, NsiliConstants.DATE_TIME_DECLARED, orb);
        ResultDAGConverter.addStringAttribute(graph,
                streamNode,
                NsiliConstants.STANDARD,
                STREAM_STANDARD,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                streamNode,
                NsiliConstants.STANDARD_VERSION,
                STREAM_STANDARD_VER,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                streamNode,
                NsiliConstants.SOURCE_URL,
                STREAM_SOURCE_URL,
                orb);
        ResultDAGConverter.addShortAttribute(graph,
                streamNode,
                NsiliConstants.PROGRAM_ID,
                STREAM_PROGRAM_ID,
                orb);
    }

    private void addMetadataSecurity(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node metadataSecurityNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_METADATA_SECURITY,
                any);
        graph.addVertex(metadataSecurityNode);
        graph.addEdge(productNode, metadataSecurityNode);

        ResultDAGConverter.addStringAttribute(graph,
                metadataSecurityNode,
                NsiliConstants.POLICY,
                CLASS_POLICY,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                metadataSecurityNode,
                NsiliConstants.RELEASABILITY,
                CLASS_RELEASABILITY,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                metadataSecurityNode,
                NsiliConstants.CLASSIFICATION,
                CLASS_CLASSIFICATION,
                orb);
    }

    private void addSecurityNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node securityNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_SECURITY, any);
        graph.addVertex(securityNode);
        graph.addEdge(productNode, securityNode);

        ResultDAGConverter.addStringAttribute(graph,
                securityNode,
                NsiliConstants.POLICY,
                CLASS_POLICY,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                securityNode,
                NsiliConstants.RELEASABILITY,
                CLASS_RELEASABILITY,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                securityNode,
                NsiliConstants.CLASSIFICATION,
                CLASS_CLASSIFICATION,
                orb);
    }

    private void addAssocationNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        //First we create the NSIL_ASSOCATION
        Any assocAny = orb.create_any();
        Node associationNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_ASSOCIATION,
                assocAny);
        graph.addVertex(associationNode);
        graph.addEdge(productNode, associationNode);

        //Next create the NSIL_DESTINATION -- 1 per associated card
        for (int i = 0; i < NUM_ASSOCIATIONS; i++) {
            Any destAny = orb.create_any();
            Node destinationNode = new Node(0,
                    NodeType.ENTITY_NODE,
                    NsiliConstants.NSIL_DESTINATION,
                    destAny);
            graph.addVertex(destinationNode);
            graph.addEdge(associationNode, destinationNode);

            Any cardAny = orb.create_any();
            Node cardNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CARD, cardAny);
            graph.addVertex(cardNode);
            graph.addEdge(destinationNode, cardNode);

            ResultDAGConverter.addStringAttribute(graph,
                    cardNode,
                    NsiliConstants.IDENTIFIER,
                    UUID.randomUUID()
                            .toString(),
                    orb);
            addTestDateAttribute(graph, cardNode, NsiliConstants.SOURCE_DATE_TIME_MODIFIED, orb);
            addTestDateAttribute(graph, cardNode, NsiliConstants.DATE_TIME_MODIFIED, orb);
            ResultDAGConverter.addStringAttribute(graph,
                    cardNode,
                    NsiliConstants.PUBLISHER,
                    SOURCE_PUBLISHER,
                    orb);
            ResultDAGConverter.addStringAttribute(graph,
                    cardNode,
                    NsiliConstants.SOURCE_LIBRARY,
                    SOURCE_LIBRARY,
                    orb);
        }
    }

    private void addApprovalNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any approvalAny = orb.create_any();
        Node approvalNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_APPROVAL,
                approvalAny);
        graph.addVertex(approvalNode);
        graph.addEdge(productNode, approvalNode);

        ResultDAGConverter.addStringAttribute(graph,
                approvalNode,
                NsiliConstants.APPROVED_BY,
                APPROVED_BY,
                orb);
        addTestDateAttribute(graph, approvalNode, NsiliConstants.DATE_TIME_MODIFIED, orb);
        ResultDAGConverter.addStringAttribute(graph,
                approvalNode,
                NsiliConstants.STATUS,
                APPROVAL_STATUS.getSpecName(),
                orb);
    }

    private void addBadApprovalNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any approvalAny = orb.create_any();
        Node approvalNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_APPROVAL,
                approvalAny);
        graph.addVertex(approvalNode);
        graph.addEdge(productNode, approvalNode);

        ResultDAGConverter.addStringAttribute(graph,
                approvalNode,
                NsiliConstants.STATUS,
                BAD_ENUM_VALUE,
                orb);
    }

    private void addSdsNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any sdsAny = orb.create_any();
        Node sdsNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_SDS, sdsAny);
        graph.addVertex(sdsNode);
        graph.addEdge(productNode, sdsNode);

        ResultDAGConverter.addStringAttribute(graph,
                sdsNode,
                NsiliConstants.OPERATIONAL_STATUS,
                SDS_OP_STATUS.getSpecName(),
                orb);
    }

    private void addBadSdsNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any sdsAny = orb.create_any();
        Node sdsNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_SDS, sdsAny);
        graph.addVertex(sdsNode);
        graph.addEdge(productNode, sdsNode);

        ResultDAGConverter.addStringAttribute(graph,
                sdsNode,
                NsiliConstants.OPERATIONAL_STATUS,
                BAD_ENUM_VALUE,
                orb);
    }

    private Node addPartNode(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Any any = orb.create_any();
        Node partNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_PART, any);
        graph.addVertex(partNode);
        graph.addEdge(productNode, partNode);
        return partNode;
    }

    private void addImageryPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addImageryNode(graph, partNode3);
    }

    private void addGmtiPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addGmtiNode(graph, partNode3);
    }

    private void addMessagePart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addMessageNode(graph, partNode3);
    }

    private void addVideoPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addVideoNode(graph, partNode3);
    }

    private void addBadVideoPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addBadCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addBadVideoNode(graph, partNode3);
    }

    private void addReportPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addReportNode(graph, partNode3);
    }

    private void addTdlPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addTdlNode(graph, partNode3);
    }

    private void addCxpPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addCxpNode(graph, partNode3);
    }

    private void addIRPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addIRNode(graph, partNode3);
    }

    private void addRFIPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addRFINode(graph, partNode3);
    }

    private void addTaskPart(DirectedAcyclicGraph<Node, Edge> graph, Node productNode) {
        Node partNode1 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode1);
        addCommonNode(graph, partNode1);
        addCoverageNode(graph, partNode1);

        Node partNode2 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode2);
        addCommonNode(graph, partNode2);
        addExpoloitationInfoNode(graph, partNode2);

        Node partNode3 = addPartNode(graph, productNode);
        addSecurityNode(graph, partNode3);
        addCommonNode(graph, partNode3);
        addTaskNode(graph, partNode3);
    }

    private void addCommonNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any commonAny = orb.create_any();
        Node commonNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_COMMON, commonAny);
        graph.addVertex(commonNode);
        graph.addEdge(parentNode, commonNode);

        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.DESCRIPTION_ABSTRACT,
                COM_DESCRIPTION_ABSTRACT,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.IDENTIFIER_MISSION,
                COM_ID_MSN,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.IDENTIFIER_UUID,
                COM_ID_UUID,
                orb);
        ResultDAGConverter.addIntegerAttribute(graph,
                commonNode,
                NsiliConstants.IDENTIFIER_JC3IEDM,
                COM_JC3ID,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.LANGUAGE,
                COM_LANGUAGE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.SOURCE,
                COM_SOURCE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.SUBJECT_CATEGORY_TARGET,
                COM_SUBJECT_CATEGORY_TARGET,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.TARGET_NUMBER,
                COM_TARGET_NUMBER,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.TYPE,
                COM_TYPE.getSpecName(),
                orb);
    }

    private void addBadCommonNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any commonAny = orb.create_any();
        Node commonNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_COMMON, commonAny);
        graph.addVertex(commonNode);
        graph.addEdge(parentNode, commonNode);

        ResultDAGConverter.addStringAttribute(graph,
                commonNode,
                NsiliConstants.TYPE,
                BAD_ENUM_VALUE,
                orb);
    }

    private void addImageryNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any imageryAny = orb.create_any();
        Node imageryNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_IMAGERY,
                imageryAny);
        graph.addVertex(imageryNode);
        graph.addEdge(parentNode, imageryNode);

        ResultDAGConverter.addStringAttribute(graph,
                imageryNode,
                NsiliConstants.CATEGORY,
                IMAGERY_CATEGORY,
                orb);
        ResultDAGConverter.addShortAttribute(graph,
                imageryNode,
                NsiliConstants.CLOUD_COVER_PCT,
                IMAGERY_CLOUD_COVER_PCT,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                imageryNode,
                NsiliConstants.COMMENTS,
                IMAGERY_COMMENTS,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                imageryNode,
                NsiliConstants.DECOMPRESSION_TECHNIQUE,
                IMAGERY_DECOMPRESSION_TECH,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                imageryNode,
                NsiliConstants.IDENTIFIER,
                IMAGERY_IDENTIFIER,
                orb);
        ResultDAGConverter.addShortAttribute(graph,
                imageryNode,
                NsiliConstants.NIIRS,
                IMAGERY_NIIRS,
                orb);
        ResultDAGConverter.addIntegerAttribute(graph,
                imageryNode,
                NsiliConstants.NUMBER_OF_BANDS,
                IMAGERY_NUM_BANDS,
                orb);
        ResultDAGConverter.addIntegerAttribute(graph,
                imageryNode,
                NsiliConstants.NUMBER_OF_ROWS,
                IMAGERY_NUM_ROWS,
                orb);
        ResultDAGConverter.addIntegerAttribute(graph,
                imageryNode,
                NsiliConstants.NUMBER_OF_COLS,
                IMAGERY_NUM_COLS,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                imageryNode,
                NsiliConstants.TITLE,
                IMAGERY_TITLE,
                orb);
    }

    private void addBadImageryNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any imageryAny = orb.create_any();
        Node imageryNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_IMAGERY,
                imageryAny);
        graph.addVertex(imageryNode);
        graph.addEdge(parentNode, imageryNode);

        ResultDAGConverter.addStringAttribute(graph,
                imageryNode,
                NsiliConstants.CATEGORY,
                BAD_ENUM_VALUE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                imageryNode,
                NsiliConstants.DECOMPRESSION_TECHNIQUE,
                BAD_ENUM_VALUE,
                orb);
    }

    private void addGmtiNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any gmtiAny = orb.create_any();
        Node gmtiNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_GMTI, gmtiAny);
        graph.addVertex(gmtiNode);
        graph.addEdge(parentNode, gmtiNode);

        ResultDAGConverter.addDoubleAttribute(graph,
                gmtiNode,
                NsiliConstants.IDENTIFIER_JOB,
                GMTI_JOB_ID,
                orb);
        ResultDAGConverter.addIntegerAttribute(graph,
                gmtiNode,
                NsiliConstants.NUMBER_OF_TARGET_REPORTS,
                GMTI_TARGET_REPORTS,
                orb);
    }

    private void addMessageNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any messageAny = orb.create_any();
        Node messageNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_MESSAGE,
                messageAny);
        graph.addVertex(messageNode);
        graph.addEdge(parentNode, messageNode);

        ResultDAGConverter.addStringAttribute(graph,
                messageNode,
                NsiliConstants.RECIPIENT,
                MESSAGE_RECIPIENT,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                messageNode,
                NsiliConstants.SUBJECT,
                MESSAGE_SUBJECT,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                messageNode,
                NsiliConstants.MESSAGE_BODY,
                MESSAGE_BODY,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                messageNode,
                NsiliConstants.MESSAGE_TYPE,
                MESSAGE_TYPE,
                orb);
    }

    private void addVideoNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any videoAny = orb.create_any();
        Node videoNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_VIDEO, videoAny);
        graph.addVertex(videoNode);
        graph.addEdge(parentNode, videoNode);

        ResultDAGConverter.addDoubleAttribute(graph,
                videoNode,
                NsiliConstants.AVG_BIT_RATE,
                VIDEO_AVG_BIT_RATE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                videoNode,
                NsiliConstants.CATEGORY,
                VIDEO_CATEGORY,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                videoNode,
                NsiliConstants.ENCODING_SCHEME,
                VIDEO_ENCODING_SCHEME.getSpecName(),
                orb);
        ResultDAGConverter.addDoubleAttribute(graph,
                videoNode,
                NsiliConstants.FRAME_RATE,
                VIDEO_FRAME_RATE,
                orb);
        ResultDAGConverter.addIntegerAttribute(graph,
                videoNode,
                NsiliConstants.NUMBER_OF_ROWS,
                VIDEO_NUM_ROWS,
                orb);
        ResultDAGConverter.addIntegerAttribute(graph,
                videoNode,
                NsiliConstants.NUMBER_OF_COLS,
                VIDEO_NUM_COLS,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                videoNode,
                NsiliConstants.METADATA_ENC_SCHEME,
                VIDEO_METADATA_ENC_SCHEME,
                orb);
        ResultDAGConverter.addShortAttribute(graph,
                videoNode,
                NsiliConstants.MISM_LEVEL,
                VIDEO_MISM_LEVEL,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                videoNode,
                NsiliConstants.SCANNING_MODE,
                VIDEO_SCANNING_MODE,
                orb);
    }

    private void addBadVideoNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any videoAny = orb.create_any();
        Node videoNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_VIDEO, videoAny);
        graph.addVertex(videoNode);
        graph.addEdge(parentNode, videoNode);

        ResultDAGConverter.addStringAttribute(graph,
                videoNode,
                NsiliConstants.CATEGORY,
                BAD_ENUM_VALUE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                videoNode,
                NsiliConstants.ENCODING_SCHEME,
                BAD_ENUM_VALUE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                videoNode,
                NsiliConstants.METADATA_ENC_SCHEME,
                BAD_ENUM_VALUE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                videoNode,
                NsiliConstants.SCANNING_MODE,
                BAD_ENUM_VALUE,
                orb);
    }

    private void addReportNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any reportAny = orb.create_any();
        Node reportNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_REPORT, reportAny);
        graph.addVertex(reportNode);
        graph.addEdge(parentNode, reportNode);

        ResultDAGConverter.addStringAttribute(graph,
                reportNode,
                NsiliConstants.ORIGINATORS_REQ_SERIAL_NUM,
                REPORT_REQ_SERIAL_NUM,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                reportNode,
                NsiliConstants.PRIORITY,
                REPORT_PRIORITY,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                reportNode,
                NsiliConstants.TYPE,
                REPORT_TYPE,
                orb);
    }

    private void addBadReportNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any reportAny = orb.create_any();
        Node reportNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_REPORT, reportAny);
        graph.addVertex(reportNode);
        graph.addEdge(parentNode, reportNode);

        ResultDAGConverter.addStringAttribute(graph,
                reportNode,
                NsiliConstants.PRIORITY,
                BAD_ENUM_VALUE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                reportNode,
                NsiliConstants.TYPE,
                BAD_ENUM_VALUE,
                orb);
    }

    private void addTdlNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any tdlAny = orb.create_any();
        Node tdlNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_TDL, tdlAny);
        graph.addVertex(tdlNode);
        graph.addEdge(parentNode, tdlNode);

        ResultDAGConverter.addShortAttribute(graph,
                tdlNode,
                NsiliConstants.ACTIVITY,
                TDL_ACTIVITY,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                tdlNode,
                NsiliConstants.MESSAGE_NUM,
                TDL_MESSAGE_NUM,
                orb);
        ResultDAGConverter.addShortAttribute(graph,
                tdlNode,
                NsiliConstants.PLATFORM,
                TDL_PLATFORM_NUM,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                tdlNode,
                NsiliConstants.TRACK_NUM,
                TDL_TRACK_NUM,
                orb);
    }

    private void addCxpNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any cxpAny = orb.create_any();
        Node cxpNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CXP, cxpAny);
        graph.addVertex(cxpNode);
        graph.addEdge(parentNode, cxpNode);

        ResultDAGConverter.addStringAttribute(graph,
                cxpNode,
                NsiliConstants.STATUS,
                CXP_STATUS,
                orb);
    }

    private void addBadCxpNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any cxpAny = orb.create_any();
        Node cxpNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CXP, cxpAny);
        graph.addVertex(cxpNode);
        graph.addEdge(parentNode, cxpNode);

        ResultDAGConverter.addStringAttribute(graph,
                cxpNode,
                NsiliConstants.STATUS,
                BAD_ENUM_VALUE,
                orb);
    }

    private void addIRNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any irAny = orb.create_any();
        Node irNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_IR, irAny);
        graph.addVertex(irNode);
        graph.addEdge(parentNode, irNode);
    }

    private void addRFINode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any rfiAny = orb.create_any();
        Node rfiNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_RFI, rfiAny);
        graph.addVertex(rfiNode);
        graph.addEdge(parentNode, rfiNode);

        ResultDAGConverter.addStringAttribute(graph,
                rfiNode,
                NsiliConstants.FOR_ACTION,
                RFI_FOR_ACTION,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                rfiNode,
                NsiliConstants.FOR_INFORMATION,
                RFI_FOR_INFORMATION,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                rfiNode,
                NsiliConstants.SERIAL_NUMBER,
                RFI_SERIAL_NUM,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                rfiNode,
                NsiliConstants.STATUS,
                RFI_STATUS,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                rfiNode,
                NsiliConstants.WORKFLOW_STATUS,
                RFI_WORKFLOW_STATUS,
                orb);
    }

    private void addBadRFINode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any rfiAny = orb.create_any();
        Node rfiNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_RFI, rfiAny);
        graph.addVertex(rfiNode);
        graph.addEdge(parentNode, rfiNode);

        ResultDAGConverter.addStringAttribute(graph,
                rfiNode,
                NsiliConstants.STATUS,
                BAD_ENUM_VALUE,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                rfiNode,
                NsiliConstants.WORKFLOW_STATUS,
                BAD_ENUM_VALUE,
                orb);
    }

    private void addTaskNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any taskAny = orb.create_any();
        Node taskNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_TASK, taskAny);
        graph.addVertex(taskNode);
        graph.addEdge(parentNode, taskNode);

        ResultDAGConverter.addStringAttribute(graph,
                taskNode,
                NsiliConstants.COMMENTS,
                TASK_COMMENTS,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                taskNode,
                NsiliConstants.STATUS,
                TASK_STATUS,
                orb);
    }

    private void addBadTaskNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any taskAny = orb.create_any();
        Node taskNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_TASK, taskAny);
        graph.addVertex(taskNode);
        graph.addEdge(parentNode, taskNode);

        ResultDAGConverter.addStringAttribute(graph,
                taskNode,
                NsiliConstants.STATUS,
                BAD_ENUM_VALUE,
                orb);
    }

    private void addCoverageNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any coverageAny = orb.create_any();
        Node coverageNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_COVERAGE,
                coverageAny);
        graph.addVertex(coverageNode);
        graph.addEdge(parentNode, coverageNode);

        ResultDAGConverter.addStringAttribute(graph,
                coverageNode,
                NsiliConstants.SPATIAL_COUNTRY_CODE,
                COVERAGE_COUNTRY_CD,
                orb);
        addTestDateAttribute(graph, coverageNode, NsiliConstants.TEMPORAL_START, orb);
        addTestDateAttribute(graph, coverageNode, NsiliConstants.TEMPORAL_END, orb);

        org.codice.alliance.nsili.common.UCO.Coordinate2d upperLeft =
                new org.codice.alliance.nsili.common.UCO.Coordinate2d(UPPER_LEFT_LAT,
                        UPPER_LEFT_LON);
        org.codice.alliance.nsili.common.UCO.Coordinate2d lowerRight =
                new org.codice.alliance.nsili.common.UCO.Coordinate2d(LOWER_RIGHT_LAT,
                        LOWER_RIGHT_LON);
        org.codice.alliance.nsili.common.UCO.Rectangle rectangle =
                new org.codice.alliance.nsili.common.UCO.Rectangle(upperLeft, lowerRight);
        Any spatialCoverage = orb.create_any();
        RectangleHelper.insert(spatialCoverage, rectangle);
        ResultDAGConverter.addAnyAttribute(graph,
                coverageNode,
                NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX,
                spatialCoverage,
                orb);
    }

    private void addExpoloitationInfoNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any exploitationAny = orb.create_any();
        Node exploitationNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_EXPLOITATION_INFO,
                exploitationAny);
        graph.addVertex(exploitationNode);
        graph.addEdge(parentNode, exploitationNode);

        ResultDAGConverter.addStringAttribute(graph,
                exploitationNode,
                NsiliConstants.DESCRIPTION,
                EXPLOITATION_DESC,
                orb);
        ResultDAGConverter.addShortAttribute(graph,
                exploitationNode,
                NsiliConstants.LEVEL,
                EXPLOITATION_LEVEL,
                orb);
        ResultDAGConverter.addBooleanAttribute(graph,
                exploitationNode,
                NsiliConstants.AUTO_GENERATED,
                EXPLOITATION_AUTO_GEN,
                orb);
        ResultDAGConverter.addStringAttribute(graph,
                exploitationNode,
                NsiliConstants.SUBJ_QUALITY_CODE,
                EXPLOITATION_SUBJ_QUAL_CODE,
                orb);
    }

    private void addBadExpoloitationInfoNode(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode) {
        Any exploitationAny = orb.create_any();
        Node exploitationNode = new Node(0,
                NodeType.ENTITY_NODE,
                NsiliConstants.NSIL_EXPLOITATION_INFO,
                exploitationAny);
        graph.addVertex(exploitationNode);
        graph.addEdge(parentNode, exploitationNode);

        ResultDAGConverter.addStringAttribute(graph,
                exploitationNode,
                NsiliConstants.SUBJ_QUALITY_CODE,
                BAD_ENUM_VALUE,
                orb);
    }

    private void printMetacard(MetacardImpl metacard, PrintStream outStream) {
        MetacardType metacardType = metacard.getMetacardType();
        outStream.println("Metacard Type : " + metacardType.getClass()
                .getCanonicalName());
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
        outStream.println("Resource URI : " + metacard.getAttribute(Metacard.RESOURCE_DOWNLOAD_URL)
                .getValue()
                .toString());

        Set<AttributeDescriptor> descriptors = metacardType.getAttributeDescriptors();
        for (AttributeDescriptor descriptor : descriptors) {
            Attribute attribute = metacard.getAttribute(descriptor.getName());
            if (attribute != null) {
                if (attribute.getValues() != null) {
                    String valueStr = getValueString(attribute.getValues());
                    outStream.println("  " + descriptor.getName() + " : " +
                            valueStr);
                } else {
                    outStream.println("  " + descriptor.getName() + " : " +
                            attribute.getValue());
                }
            }
        }
    }

    private static String getValueString(Collection<Serializable> collection) {
        return collection.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    public static void addTestDateAttribute(DirectedAcyclicGraph<Node, Edge> graph, Node parentNode,
            String key, ORB orb) {
        Any any = orb.create_any();
        AbsTime absTime = getTestTime();
        AbsTimeHelper.insert(any, absTime);
        Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
        graph.addVertex(node);
        graph.addEdge(parentNode, node);
    }

    private static AbsTime getTestTime() {
        Calendar cal = getDefaultCalendar();
        return new AbsTime(new org.codice.alliance.nsili.common.UCO.Date((short) cal.get(Calendar.YEAR),
                (short) (cal.get(Calendar.MONTH) + 1),
                (short) cal.get(Calendar.DAY_OF_MONTH)),
                new Time((short) cal.get(Calendar.HOUR_OF_DAY),
                        (short) cal.get(Calendar.MINUTE),
                        (short) cal.get(Calendar.SECOND)));
    }

    private static Calendar getDefaultCalendar() {
        int year = 2016;
        int month = 01;
        int dayOfMonth = 29;
        int hourOfDay = 17;
        int minute = 05;
        int second = 10;
        return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
    }

    private void setupMocks() throws Exception {
        byte[] testReturn = "TEST RETURN".getBytes();
        Resource mockResource = mock(Resource.class);
        ResourceResponse mockResponse = mock(ResourceResponse.class);
        doReturn(Long.valueOf(testReturn.length)).when(mockResource)
                .getSize();
        doReturn(testReturn).when(mockResource)
                .getByteArray();
        doReturn(mockResource).when(mockResponse)
                .getResource();
        doReturn(mockResponse).when(mockResourceReader)
                .retrieveResource(anyObject(), anyMap());
    }
}
