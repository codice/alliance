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
package org.codice.alliance.nsili.common.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codice.alliance.nsili.common.GIAS.Association;
import org.codice.alliance.nsili.common.GIAS.AttributeInformation;
import org.codice.alliance.nsili.common.GIAS.ConceptualAttributeType;
import org.codice.alliance.nsili.common.GIAS.RequirementMode;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.UCO.Cardinality;
import org.codice.alliance.nsili.common.UCO.EntityGraph;
import org.codice.alliance.nsili.common.UCO.EntityNode;
import org.codice.alliance.nsili.common.UCO.EntityRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsiliDataModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NsiliDataModel.class);

    private EntityNode productNode = new EntityNode(0, NsiliConstants.NSIL_PRODUCT);

    private EntityNode cardNode = new EntityNode(1, NsiliConstants.NSIL_CARD);

    private EntityNode commonNode = new EntityNode(2, NsiliConstants.NSIL_COMMON);

    private EntityNode coverageNode = new EntityNode(3, NsiliConstants.NSIL_COVERAGE);

    private EntityNode fileNode = new EntityNode(4, NsiliConstants.NSIL_FILE);

    private EntityNode gmtiNode = new EntityNode(5, NsiliConstants.NSIL_GMTI);

    private EntityNode imageryNode = new EntityNode(6, NsiliConstants.NSIL_IMAGERY);

    private EntityNode messageNode = new EntityNode(7, NsiliConstants.NSIL_MESSAGE);

    private EntityNode metadataSecurityNode = new EntityNode(8,
            NsiliConstants.NSIL_METADATA_SECURITY);

    private EntityNode partNode = new EntityNode(9, NsiliConstants.NSIL_PART);

    private EntityNode relatedFileNode = new EntityNode(10, NsiliConstants.NSIL_RELATED_FILE);

    private EntityNode relationNode = new EntityNode(11, NsiliConstants.NSIL_RELATION);

    private EntityNode securityNode = new EntityNode(12, NsiliConstants.NSIL_SECURITY);

    private EntityNode streamNode = new EntityNode(13, NsiliConstants.NSIL_STREAM);

    private EntityNode videoNode = new EntityNode(14, NsiliConstants.NSIL_VIDEO);

    private EntityNode approvalNode = new EntityNode(15, NsiliConstants.NSIL_APPROVAL);

    private EntityNode exploitationNode = new EntityNode(16, NsiliConstants.NSIL_EXPLOITATION_INFO);

    private EntityNode sdsNode = new EntityNode(17, NsiliConstants.NSIL_SDS);

    private EntityNode tdlNode = new EntityNode(18, NsiliConstants.NSIL_TDL);

    private EntityNode rfiNode = new EntityNode(19, NsiliConstants.NSIL_RFI);

    private EntityNode cxpNode = new EntityNode(20, NsiliConstants.NSIL_CXP);

    private EntityNode reportNode = new EntityNode(21, NsiliConstants.NSIL_REPORT);

    private EntityNode taskNode = new EntityNode(22, NsiliConstants.NSIL_TASK);

    private EntityNode sourceNode = new EntityNode(23, NsiliConstants.NSIL_SOURCE);

    private EntityNode destinationNode = new EntityNode(24, NsiliConstants.NSIL_DESTINATION);

    private EntityNode associationNode = new EntityNode(25, NsiliConstants.NSIL_ASSOCIATION);

    private EntityNode entityNode = new EntityNode(26, NsiliConstants.NSIL_ENTITY);

    private EntityNode intrepNode = new EntityNode(27, NsiliConstants.NSIL_INTREP);

    private EntityNode intsumNode = new EntityNode(28, NsiliConstants.NSIL_INTSUM);

    private EntityNode cbrnNode = new EntityNode(29, NsiliConstants.NSIL_CBRN);

    private EntityRelationship productAssociationRln = new EntityRelationship(productNode.id,
            associationNode.id,
            Cardinality.ONE_TO_ZERO_OR_MORE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship productApprovalRln = new EntityRelationship(productNode.id,
            approvalNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship productCardRln = new EntityRelationship(productNode.id,
            cardNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship productFileRln = new EntityRelationship(productNode.id,
            fileNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship productStreamRln = new EntityRelationship(productNode.id,
            streamNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship productMetadataSecurityRln = new EntityRelationship(productNode.id,
            metadataSecurityNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship productRelatedFileRln = new EntityRelationship(productNode.id,
            relatedFileNode.id,
            Cardinality.ONE_TO_ZERO_OR_MORE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship productSecurityRln = new EntityRelationship(productNode.id,
            securityNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship productPartRln = new EntityRelationship(productNode.id,
            partNode.id,
            Cardinality.ONE_TO_ZERO_OR_MORE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partCommonRln = new EntityRelationship(partNode.id,
            commonNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partCoverageRln = new EntityRelationship(partNode.id,
            coverageNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partSecurityRln = new EntityRelationship(partNode.id,
            securityNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ZERO_OR_ONE);

    private EntityRelationship partExploitationRln = new EntityRelationship(partNode.id,
            exploitationNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partCxpRln = new EntityRelationship(partNode.id,
            cxpNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partGmtiRln = new EntityRelationship(partNode.id,
            gmtiNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partImageryRln = new EntityRelationship(partNode.id,
            imageryNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partMessageRln = new EntityRelationship(partNode.id,
            messageNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partReportRln = new EntityRelationship(partNode.id,
            reportNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partRfiRln = new EntityRelationship(partNode.id,
            rfiNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partSdsRln = new EntityRelationship(partNode.id,
            sdsNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partTaskRln = new EntityRelationship(partNode.id,
            taskNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partTdlRln = new EntityRelationship(partNode.id,
            tdlNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partVideoRln = new EntityRelationship(partNode.id,
            videoNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship partCbrnRln = new EntityRelationship(partNode.id,
            cbrnNode.id,
            Cardinality.ONE_TO_ZERO_OR_MORE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship reportEntityRln = new EntityRelationship(reportNode.id,
            entityNode.id,
            Cardinality.ONE_TO_ZERO_OR_MORE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship reportIntrepRln = new EntityRelationship(reportNode.id,
            intrepNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship reportIntsumRln = new EntityRelationship(reportNode.id,
            intsumNode.id,
            Cardinality.ONE_TO_ZERO_OR_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship assocCardRln = new EntityRelationship(associationNode.id,
            cardNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ZERO_OR_MORE);

    private EntityRelationship assocSourceRln = new EntityRelationship(associationNode.id,
            sourceNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ZERO_OR_MORE);

    private EntityRelationship assocDestRln = new EntityRelationship(associationNode.id,
            destinationNode.id,
            Cardinality.ONE_TO_ONE_OR_MORE,
            Cardinality.ONE_TO_ZERO_OR_MORE);

    private EntityRelationship assocRelationRln = new EntityRelationship(associationNode.id,
            relationNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ONE);

    private EntityRelationship sourceCardRln = new EntityRelationship(sourceNode.id,
            cardNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ZERO_OR_ONE);

    private EntityRelationship destCardRln = new EntityRelationship(destinationNode.id,
            cardNode.id,
            Cardinality.ONE_TO_ONE,
            Cardinality.ONE_TO_ZERO_OR_ONE);

    private Pair<ConceptualAttributeType, String> classificationPair = new ImmutablePair<>(
            ConceptualAttributeType.CLASSIFICATION,
            buildAttr(NsiliConstants.NSIL_SECURITY, NsiliConstants.CLASSIFICATION));

    private Pair<ConceptualAttributeType, String> dataSetTypePair = new ImmutablePair<>(
            ConceptualAttributeType.DATASETTYPE,
            buildAttr(NsiliConstants.NSIL_PART, NsiliConstants.NSIL_COMMON, NsiliConstants.TYPE));

    private Pair<ConceptualAttributeType, String> dataSizePair = new ImmutablePair<>(
            ConceptualAttributeType.DATASIZE,
            buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.EXTENT));

    private Pair<ConceptualAttributeType, String> directAccessPair = new ImmutablePair<>(
            ConceptualAttributeType.DIRECTACCESS,
            buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.PRODUCT_URL));

    private Pair<ConceptualAttributeType, String> footprintPair = new ImmutablePair<>(
            ConceptualAttributeType.FOOTPRINT,
            buildAttr(NsiliConstants.NSIL_PART,
                    NsiliConstants.NSIL_COVERAGE,
                    NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX));

    private Pair<ConceptualAttributeType, String> modificationDatePair = new ImmutablePair<>(
            ConceptualAttributeType.MODIFICATIONDATE,
            buildAttr(NsiliConstants.NSIL_CARD, NsiliConstants.DATE_TIME_MODIFIED));

    private Pair<ConceptualAttributeType, String> productTitlePair = new ImmutablePair<>(
            ConceptualAttributeType.PRODUCTTITLE,
            buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.TITLE));

    private Pair<ConceptualAttributeType, String> uniqueIdPair = new ImmutablePair<>(
            ConceptualAttributeType.UNIQUEIDENTIFIER,
            buildAttr(NsiliConstants.NSIL_CARD, NsiliConstants.IDENTIFIER));

    private Map<String, EntityGraph> viewGraphMap = new HashMap<>();

    private Map<String, List<Pair<String, String>>> aliasCategoryMap = new HashMap<>();

    private Map<String, List<Pair<ConceptualAttributeType, String>>> conceptualAttrMap =
            new HashMap<>();

    private List<Association> associations = new ArrayList<>();

    private Map<String, Map<String, List<String>>> requiredAttrMap = new HashMap<>();

    public NsiliDataModel() {
        init();
    }

    private void init() {
        initAllViewGraph();
        initImageryViewGraph();
        initGmtiViewGraph();
        initMessageViewGraph();
        initVideoViewGraph();
        initAssociationViewGraph();
        initReportViewGraph();
        initTdlViewGraph();
        initCcirmViewGraph();
        initCbrnViewGraph();

        initAliasCategoryMap();
        initAssociations();
    }

    public List<AttributeInformation> getAttributeInformation(String entityName) {
        List<AttributeInformation> attributes = new ArrayList<>();

        switch (entityName) {
        case NsiliConstants.NSIL_PRODUCT:
            attributes = new ArrayList<>();
            break;
        case NsiliConstants.NSIL_CARD:
            attributes = NsiliAttributesGenerator.getNsilCardAttributes();
            break;
        case NsiliConstants.NSIL_COMMON:
            attributes = NsiliAttributesGenerator.getNsilCommonAttributes();
            break;
        case NsiliConstants.NSIL_COVERAGE:
            attributes = NsiliAttributesGenerator.getNsilCoverageAttributes();
            break;
        case NsiliConstants.NSIL_FILE:
            attributes = NsiliAttributesGenerator.getNsilFileAttributes();
            break;
        case NsiliConstants.NSIL_GMTI:
            attributes = NsiliAttributesGenerator.getNsilGmtiAttributes();
            break;
        case NsiliConstants.NSIL_IMAGERY:
            attributes = NsiliAttributesGenerator.getNsilImageryAttributes();
            break;
        case NsiliConstants.NSIL_MESSAGE:
            attributes = NsiliAttributesGenerator.getNsilMessageAttributes();
            break;
        case NsiliConstants.NSIL_METADATA_SECURITY:
            attributes = NsiliAttributesGenerator.getNsilMetadataSecurityAttributes();
            break;
        case NsiliConstants.NSIL_PART:
            attributes = NsiliAttributesGenerator.getNsilPartAttributes();
            break;
        case NsiliConstants.NSIL_RELATED_FILE:
            attributes = NsiliAttributesGenerator.getNsilRelatedFileAttributes();
            break;
        case NsiliConstants.NSIL_RELATION:
            attributes = NsiliAttributesGenerator.getNsilRelationAttributes();
            break;
        case NsiliConstants.NSIL_SECURITY:
            attributes = NsiliAttributesGenerator.getNsilSecurityAttributes();
            break;
        case NsiliConstants.NSIL_STREAM:
            attributes = NsiliAttributesGenerator.getNsilStreamAttributes();
            break;
        case NsiliConstants.NSIL_VIDEO:
            attributes = NsiliAttributesGenerator.getNsilVideoAttributes();
            break;
        case NsiliConstants.NSIL_APPROVAL:
            attributes = NsiliAttributesGenerator.getNsilApprovalAttributes();
            break;
        case NsiliConstants.NSIL_EXPLOITATION_INFO:
            attributes = NsiliAttributesGenerator.getNsilExploitationInfoAttributes();
            break;
        case NsiliConstants.NSIL_SDS:
            attributes = NsiliAttributesGenerator.getNsilSdsAttributes();
            break;
        case NsiliConstants.NSIL_TDL:
            attributes = NsiliAttributesGenerator.getNsilTdlAttributes();
            break;
        case NsiliConstants.NSIL_RFI:
            attributes = NsiliAttributesGenerator.getNsilRfiAttributes();
            break;
        case NsiliConstants.NSIL_CXP:
            attributes = NsiliAttributesGenerator.getNsilCxpAttributes();
            break;
        case NsiliConstants.NSIL_REPORT:
            attributes = NsiliAttributesGenerator.getNsilReportAttributes();
            break;
        case NsiliConstants.NSIL_TASK:
            attributes = NsiliAttributesGenerator.getNsilTaskAttributes();
            break;
        case NsiliConstants.NSIL_ASSOCIATION:
            attributes = NsiliAttributesGenerator.getNsilAssocationAttributes();
            break;
        case NsiliConstants.NSIL_SOURCE:
            attributes = NsiliAttributesGenerator.getNsilSourceAttributes();
            break;
        case NsiliConstants.NSIL_DESTINATION:
            attributes = NsiliAttributesGenerator.getNsilDestinationAttributes();
            break;
        case NsiliConstants.NSIL_CBRN:
            attributes = NsiliAttributesGenerator.getNsilCbrnAttributes();
            break;
        case NsiliConstants.NSIL_INTREP:
            attributes = NsiliAttributesGenerator.getNsilIntrepAttributes();
            break;
        case NsiliConstants.NSIL_INTSUM:
            attributes = NsiliAttributesGenerator.getNsilIntsumAttributes();
            break;
        case NsiliConstants.NSIL_ENTITY:
            attributes = NsiliAttributesGenerator.getNsilEntityAttributes();
            break;
        default:
            break;
        }

        return attributes;
    }

    private void initAllViewGraph() {
        EntityNode viewNodes[] =
                new EntityNode[] {productNode, cardNode, commonNode, coverageNode, fileNode,
                        gmtiNode, imageryNode, messageNode, metadataSecurityNode, partNode,
                        relatedFileNode, relationNode, securityNode, streamNode, videoNode,
                        approvalNode, exploitationNode, sdsNode, tdlNode, rfiNode, cxpNode,
                        reportNode, taskNode, sourceNode, destinationNode, associationNode,
                        cbrnNode, intrepNode, intsumNode, entityNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productAssociationRln, productApprovalRln, productCardRln,
                        productFileRln, productStreamRln, productMetadataSecurityRln,
                        productRelatedFileRln, productSecurityRln, productPartRln, partCommonRln,
                        partCoverageRln, partSecurityRln, partExploitationRln, partCxpRln,
                        partGmtiRln, partImageryRln, partMessageRln, partReportRln, partRfiRln,
                        partSdsRln, partTaskRln, partTdlRln, partVideoRln, assocCardRln,
                        assocSourceRln, assocDestRln, assocRelationRln, sourceCardRln, destCardRln,
                        partCbrnRln, reportEntityRln, reportIntrepRln, reportIntsumRln};

        viewGraphMap.put(NsiliConstants.NSIL_ALL_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);
        conceptualAttrMap.put(NsiliConstants.NSIL_ALL_VIEW, conceptualPairs);
        updateMandatoryAttrs(NsiliConstants.NSIL_ALL_VIEW, viewNodes);
    }

    private void initImageryViewGraph() {
        EntityNode viewNodes[] =
                new EntityNode[] {productNode, partNode, sourceNode, destinationNode,
                        associationNode, approvalNode, cardNode, fileNode, streamNode,
                        metadataSecurityNode, relatedFileNode, securityNode, commonNode,
                        coverageNode, exploitationNode, imageryNode, relationNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productAssociationRln, productApprovalRln, productCardRln,
                        productFileRln, productStreamRln, productMetadataSecurityRln,
                        productRelatedFileRln, productSecurityRln, productPartRln, partCommonRln,
                        partCoverageRln, partSecurityRln, partExploitationRln, partImageryRln,
                        assocCardRln, assocSourceRln, assocDestRln, assocRelationRln, sourceCardRln,
                        destCardRln};

        viewGraphMap.put(NsiliConstants.NSIL_IMAGERY_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);
        conceptualAttrMap.put(NsiliConstants.NSIL_IMAGERY_VIEW, conceptualPairs);

        updateMandatoryAttrs(NsiliConstants.NSIL_IMAGERY_VIEW, viewNodes);
    }

    private void initGmtiViewGraph() {
        EntityRelationship productPartRln = new EntityRelationship(productNode.id,
                partNode.id,
                Cardinality.ONE_TO_ONE,
                Cardinality.ONE_TO_ONE);

        EntityNode viewNodes[] =
                new EntityNode[] {productNode, cardNode, commonNode, coverageNode, fileNode,
                        gmtiNode, metadataSecurityNode, partNode, relatedFileNode, relationNode,
                        securityNode, streamNode, approvalNode, exploitationNode, sourceNode,
                        destinationNode, associationNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productAssociationRln, productApprovalRln, productCardRln,
                        productFileRln, productStreamRln, productMetadataSecurityRln,
                        productRelatedFileRln, productSecurityRln, productPartRln, partCommonRln,
                        partCoverageRln, partSecurityRln, partExploitationRln, partGmtiRln,
                        assocCardRln, assocSourceRln, assocDestRln, assocRelationRln, sourceCardRln,
                        destCardRln};

        viewGraphMap.put(NsiliConstants.NSIL_GMTI_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);
        conceptualAttrMap.put(NsiliConstants.NSIL_GMTI_VIEW, conceptualPairs);
        updateMandatoryAttrs(NsiliConstants.NSIL_GMTI_VIEW, viewNodes);
    }

    private void initMessageViewGraph() {
        EntityRelationship productPartRln = new EntityRelationship(productNode.id,
                partNode.id,
                Cardinality.ONE_TO_ONE,
                Cardinality.ONE_TO_ONE);

        EntityNode viewNodes[] =
                new EntityNode[] {productNode, cardNode, commonNode, coverageNode, fileNode,
                        messageNode, metadataSecurityNode, partNode, relatedFileNode, relationNode,
                        securityNode, approvalNode, exploitationNode, sourceNode, destinationNode,
                        associationNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productAssociationRln, productApprovalRln, productCardRln,
                        productFileRln, productMetadataSecurityRln, productRelatedFileRln,
                        productSecurityRln, productPartRln, partCommonRln, partCoverageRln,
                        partSecurityRln, partExploitationRln, partMessageRln, assocCardRln,
                        assocSourceRln, assocDestRln, assocRelationRln, sourceCardRln, destCardRln};

        viewGraphMap.put(NsiliConstants.NSIL_MESSAGE_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);
        conceptualAttrMap.put(NsiliConstants.NSIL_MESSAGE_VIEW, conceptualPairs);
        updateMandatoryAttrs(NsiliConstants.NSIL_MESSAGE_VIEW, viewNodes);
    }

    private void initVideoViewGraph() {
        EntityRelationship productPartRln = new EntityRelationship(productNode.id,
                partNode.id,
                Cardinality.ONE_TO_ONE,
                Cardinality.ONE_TO_ONE);

        EntityNode viewNodes[] =
                new EntityNode[] {productNode, cardNode, commonNode, coverageNode, fileNode,
                        streamNode, metadataSecurityNode, partNode, relatedFileNode, relationNode,
                        securityNode, videoNode, approvalNode, exploitationNode, sourceNode,
                        destinationNode, associationNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productAssociationRln, productApprovalRln, productCardRln,
                        productFileRln, productStreamRln, productMetadataSecurityRln,
                        productRelatedFileRln, productSecurityRln, productPartRln, partCommonRln,
                        partCoverageRln, partSecurityRln, partExploitationRln, partVideoRln,
                        assocCardRln, assocSourceRln, assocDestRln, assocRelationRln, sourceCardRln,
                        destCardRln};

        viewGraphMap.put(NsiliConstants.NSIL_VIDEO_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);

        conceptualAttrMap.put(NsiliConstants.NSIL_VIDEO_VIEW, conceptualPairs);
        updateMandatoryAttrs(NsiliConstants.NSIL_VIDEO_VIEW, viewNodes);
    }

    private void initCbrnViewGraph() {
        EntityRelationship productPartRln = new EntityRelationship(productNode.id,
                partNode.id,
                Cardinality.ONE_TO_ONE,
                Cardinality.ONE_TO_ONE);

        EntityNode viewNodes[] =
                new EntityNode[] {productNode, approvalNode, cardNode, metadataSecurityNode,
                        relatedFileNode, partNode, securityNode, commonNode, coverageNode, cbrnNode,
                        associationNode, relationNode, sourceNode, destinationNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productApprovalRln, productCardRln, productFileRln,
                        productMetadataSecurityRln, productRelatedFileRln, productSecurityRln,
                        productPartRln, partCommonRln, partCoverageRln, partCbrnRln, assocCardRln,
                        assocSourceRln, assocDestRln, assocRelationRln, sourceCardRln, destCardRln};

        viewGraphMap.put(NsiliConstants.NSIL_CBRN_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);

        conceptualAttrMap.put(NsiliConstants.NSIL_CBRN_VIEW, conceptualPairs);
    }

    private void initAssociationViewGraph() {
        EntityNode viewNodes[] =
                new EntityNode[] {cardNode, relationNode, sourceNode, destinationNode,
                        associationNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {assocCardRln, assocSourceRln, assocDestRln,
                        assocRelationRln, sourceCardRln, destCardRln};

        viewGraphMap.put(NsiliConstants.NSIL_ASSOCIATION_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(uniqueIdPair);
        conceptualAttrMap.put(NsiliConstants.NSIL_ASSOCIATION_VIEW, conceptualPairs);
        updateMandatoryAttrs(NsiliConstants.NSIL_ASSOCIATION_VIEW, viewNodes);
    }

    private void initReportViewGraph() {
        EntityRelationship productPartRln = new EntityRelationship(productNode.id,
                partNode.id,
                Cardinality.ONE_TO_ONE,
                Cardinality.ONE_TO_ONE);

        EntityRelationship partReportRln = new EntityRelationship(partNode.id,
                reportNode.id,
                Cardinality.ONE_TO_ONE,
                Cardinality.ONE_TO_ONE);

        EntityNode viewNodes[] =
                new EntityNode[] {productNode, approvalNode, cardNode, commonNode, coverageNode,
                        fileNode, metadataSecurityNode, partNode, relatedFileNode, relationNode,
                        securityNode, approvalNode, exploitationNode, reportNode, sourceNode,
                        destinationNode, associationNode, intrepNode, intsumNode, entityNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productApprovalRln, productCardRln, productAssociationRln,
                        productFileRln, productMetadataSecurityRln, productRelatedFileRln,
                        assocSourceRln, assocDestRln, assocRelationRln, sourceCardRln, destCardRln,
                        productPartRln, partSecurityRln, partCommonRln, partCoverageRln,
                        partExploitationRln, partReportRln, reportEntityRln, reportIntsumRln,
                        reportIntrepRln};

        viewGraphMap.put(NsiliConstants.NSIL_REPORT_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);
        conceptualAttrMap.put(NsiliConstants.NSIL_REPORT_VIEW, conceptualPairs);
        updateMandatoryAttrs(NsiliConstants.NSIL_REPORT_VIEW, viewNodes);
    }

    private void initTdlViewGraph() {
        EntityRelationship productPartRln = new EntityRelationship(productNode.id,
                partNode.id,
                Cardinality.ONE_TO_ONE,
                Cardinality.ONE_TO_ONE);

        EntityNode viewNodes[] =
                new EntityNode[] {productNode, approvalNode, cardNode, fileNode, streamNode,
                        metadataSecurityNode, relatedFileNode, securityNode, partNode, commonNode,
                        coverageNode, exploitationNode, tdlNode, associationNode, relationNode,
                        sourceNode, destinationNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productApprovalRln, productCardRln, productFileRln,
                        productStreamRln, productMetadataSecurityRln, productRelatedFileRln,
                        productSecurityRln, productPartRln, partSecurityRln, partCommonRln,
                        partCoverageRln, partExploitationRln, partTdlRln, productAssociationRln,
                        assocCardRln, assocRelationRln, assocSourceRln, assocDestRln};

        viewGraphMap.put(NsiliConstants.NSIL_TDL_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);
        conceptualAttrMap.put(NsiliConstants.NSIL_TDL_VIEW, conceptualPairs);
        updateMandatoryAttrs(NsiliConstants.NSIL_TDL_VIEW, viewNodes);
    }

    private void initCcirmViewGraph() {
        productPartRln = new EntityRelationship(productNode.id,
                partNode.id,
                Cardinality.ONE_TO_ONE,
                Cardinality.ONE_TO_ONE);

        EntityNode viewNodes[] = new EntityNode[] {productNode, approvalNode, cardNode, fileNode,
                metadataSecurityNode, relatedFileNode, securityNode, partNode, commonNode,
                coverageNode, exploitationNode, cxpNode, rfiNode, taskNode, associationNode,
                relationNode, sourceNode, destinationNode};

        EntityRelationship viewRelationships[] =
                new EntityRelationship[] {productApprovalRln, productCardRln, productFileRln,
                        productMetadataSecurityRln, productRelatedFileRln, productSecurityRln,
                        productPartRln, partSecurityRln, partCommonRln, partCoverageRln,
                        partExploitationRln, partCxpRln, partRfiRln, partTaskRln,
                        productAssociationRln, assocCardRln, assocRelationRln, assocSourceRln,
                        assocDestRln};

        viewGraphMap.put(NsiliConstants.NSIL_CCIRM_VIEW,
                new EntityGraph(viewNodes, viewRelationships));

        List<Pair<ConceptualAttributeType, String>> conceptualPairs = new ArrayList<>();
        conceptualPairs.add(classificationPair);
        conceptualPairs.add(dataSetTypePair);
        conceptualPairs.add(dataSizePair);
        conceptualPairs.add(directAccessPair);
        conceptualPairs.add(footprintPair);
        conceptualPairs.add(modificationDatePair);
        conceptualPairs.add(productTitlePair);
        conceptualPairs.add(uniqueIdPair);
        conceptualAttrMap.put(NsiliConstants.NSIL_CCIRM_VIEW, conceptualPairs);
        updateMandatoryAttrs(NsiliConstants.NSIL_CCIRM_VIEW, viewNodes);
    }

    private void initAliasCategoryMap() {
        String identifierMission = buildAttr(NsiliConstants.NSIL_COMMON,
                NsiliConstants.IDENTIFIER_MISSION);
        addMapping(NsiliConstants.NSIL_CORE, "MISNID", identifierMission);
        addMapping(NsiliConstants.STANAG_4607, "/GMTI/PacketHeader/MissionID", identifierMission);
        addMapping(NsiliConstants.STANAG_4609, "EpisodeNumber", identifierMission);

        String commonSource = buildAttr(NsiliConstants.NSIL_COMMON, NsiliConstants.SOURCE);
        addMapping(NsiliConstants.STANAG_4545, "ISORCE", commonSource);
        addMapping(NsiliConstants.NSIL_CORE, "ISORCE", commonSource);
        addMapping(NsiliConstants.STANAG_4607, "/GMTI/PacketHeader/PlatformID", commonSource);
        addMapping(NsiliConstants.STANAG_4609, "ImageSourceDevice", commonSource);
        addMapping(NsiliConstants.NACT_L16, "/HJ3/SOURCE_TRK_NBR", commonSource);

        String commonTgtNum = buildAttr(NsiliConstants.NSIL_COMMON, NsiliConstants.TARGET_NUMBER);
        addMapping(NsiliConstants.NSIL_CORE, "TGTID", commonTgtNum);

        String spatialCtryCode = buildAttr(NsiliConstants.NSIL_COVERAGE,
                NsiliConstants.SPATIAL_COUNTRY_CODE);
        addMapping(NsiliConstants.STANAG_4545, "TGTID", spatialCtryCode);
        addMapping(NsiliConstants.NSIL_CORE, "CNTRYCODE", spatialCtryCode);
        addMapping(NsiliConstants.STANAG_4609, "ObjectCountryCode", spatialCtryCode);

        String spatialGeoBox = buildAttr(NsiliConstants.NSIL_COVERAGE,
                NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX);
        addMapping(NsiliConstants.STANAG_4545, "IGEOLO", spatialGeoBox);
        addMapping(NsiliConstants.NSIL_CORE, "IGEOLO", spatialGeoBox);
        addMapping(NsiliConstants.STANAG_4607, "/GMTI/DwellSegment/DwellArea", spatialGeoBox);
        addMapping(NsiliConstants.STANAG_4609,
                "FrameCenterLatitude + FrameCenterLongitude",
                spatialGeoBox);

        String coverageEnd = buildAttr(NsiliConstants.NSIL_COVERAGE, NsiliConstants.TEMPORAL_END);
        addMapping(NsiliConstants.STANAG_4545, "IDATIM", coverageEnd);
        addMapping(NsiliConstants.STANAG_4607,
                "/GMTI/MissionSegment/ReferenceTime + /GMTI/DwellSegment/DwellTime",
                coverageEnd);
        addMapping(NsiliConstants.STANAG_4609,
                "TimingReconciliationMetadataSet/UserDefinedTimeStamp",
                coverageEnd);
        addMapping(NsiliConstants.NACT_L16, "/HJ9/TIME_STAMP", coverageEnd);

        String coverageStart = buildAttr(NsiliConstants.NSIL_COVERAGE,
                NsiliConstants.TEMPORAL_START);
        addMapping(NsiliConstants.STANAG_4545, "IDATIM", coverageStart);
        addMapping(NsiliConstants.NSIL_CORE, "IDATIM", coverageStart);
        addMapping(NsiliConstants.STANAG_4607,
                "/GMTI/MissionSegment/ReferenceTime + /GMTI/DwellSegment/DwellTime",
                coverageStart);
        addMapping(NsiliConstants.STANAG_4609,
                "TimingReconciliationMetadataSet/UserDefinedTimeStamp",
                coverageStart);
        addMapping(NsiliConstants.NACT_L16, "/HJ9/TIME_STAMP", coverageStart);

        String fileCreator = buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.CREATOR);
        addMapping(NsiliConstants.STANAG_4545, "OSTAID", fileCreator);
        addMapping(NsiliConstants.NSIL_CORE, "OSTAID", fileCreator);

        String fileDateDeclared = buildAttr(NsiliConstants.NSIL_FILE,
                NsiliConstants.DATE_TIME_DECLARED);
        addMapping(NsiliConstants.STANAG_4545, "FDT", fileDateDeclared);
        addMapping(NsiliConstants.NSIL_CORE, "FDT", fileDateDeclared);

        String fileExtent = buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.EXTENT);
        addMapping(NsiliConstants.STANAG_4545, "FL", fileExtent);
        addMapping(NsiliConstants.NSIL_CORE, "FL", fileExtent);

        String fileFormat = buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.FORMAT);
        addMapping(NsiliConstants.STANAG_4545, "FHDR", fileFormat);

        String fileFormatVer = buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.FORMAT_VERSION);
        addMapping(NsiliConstants.STANAG_4545, "FHDR", fileFormatVer);
        addMapping(NsiliConstants.STANAG_4607, "/GMTI/PacketHeader/VersionID", fileFormatVer);

        String fileProductURL = buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.PRODUCT_URL);
        addMapping(NsiliConstants.NSIL_CORE, "DAID", fileProductURL);

        String fileTitle = buildAttr(NsiliConstants.NSIL_FILE, NsiliConstants.TITLE);
        addMapping(NsiliConstants.STANAG_4545, "FTITLE", fileTitle);
        addMapping(NsiliConstants.NSIL_CORE, "FTITLE", fileTitle);

        String gmtiIdJob = buildAttr(NsiliConstants.NSIL_GMTI, NsiliConstants.IDENTIFIER_JOB);
        addMapping(NsiliConstants.STANAG_4607, "/GMTI/PacketHeader/JobID", gmtiIdJob);

        String gmtiNumTgtReports = buildAttr(NsiliConstants.NSIL_GMTI,
                NsiliConstants.NUMBER_OF_TARGET_REPORTS);
        addMapping(NsiliConstants.STANAG_4607,
                "/GMTI/DwellSegment/TargetReportCount",
                gmtiNumTgtReports);

        String imageryCat = buildAttr(NsiliConstants.NSIL_IMAGERY, NsiliConstants.CATEGORY);
        addMapping(NsiliConstants.STANAG_4545, "ICAT", imageryCat);
        addMapping(NsiliConstants.NSIL_CORE, "ICAT", imageryCat);

        String imageryComments = buildAttr(NsiliConstants.NSIL_IMAGERY, NsiliConstants.COMMENTS);
        addMapping(NsiliConstants.STANAG_4545, "ICOM", imageryComments);
        addMapping(NsiliConstants.NSIL_CORE, "ICOM", imageryComments);

        String imageryDecompression = buildAttr(NsiliConstants.NSIL_IMAGERY,
                NsiliConstants.DECOMPRESSION_TECHNIQUE);
        addMapping(NsiliConstants.STANAG_4545, "IC", imageryDecompression);

        String imageryId = buildAttr(NsiliConstants.NSIL_IMAGERY, NsiliConstants.IDENTIFIER);
        addMapping(NsiliConstants.STANAG_4545, "IID1", imageryId);
        addMapping(NsiliConstants.NSIL_CORE, "IID1", imageryId);

        String imageryBands = buildAttr(NsiliConstants.NSIL_IMAGERY,
                NsiliConstants.NUMBER_OF_BANDS);
        addMapping(NsiliConstants.STANAG_4545, "NBANDS", imageryBands);

        String imageryRows = buildAttr(NsiliConstants.NSIL_IMAGERY, NsiliConstants.NUMBER_OF_ROWS);
        addMapping(NsiliConstants.STANAG_4545, "NROWS", imageryRows);

        String imageryCols = buildAttr(NsiliConstants.NSIL_IMAGERY, NsiliConstants.NUMBER_OF_COLS);
        addMapping(NsiliConstants.STANAG_4545, "NCOLS", imageryCols);

        String imageryTitle = buildAttr(NsiliConstants.NSIL_IMAGERY, NsiliConstants.TITLE);
        addMapping(NsiliConstants.STANAG_4545, "IID2", imageryTitle);
        addMapping(NsiliConstants.NSIL_CORE, "IID2", imageryTitle);

        String relatedFileCreator = buildAttr(NsiliConstants.NSIL_RELATED_FILE,
                NsiliConstants.CREATOR);
        addMapping(NsiliConstants.STANAG_4545, "OSTAID", relatedFileCreator);
        addMapping(NsiliConstants.NSIL_CORE, "OSTAID", relatedFileCreator);

        String relatedFileExtent = buildAttr(NsiliConstants.NSIL_RELATED_FILE,
                NsiliConstants.EXTENT);
        addMapping(NsiliConstants.STANAG_4545, "FL", relatedFileExtent);
        addMapping(NsiliConstants.NSIL_CORE, "FL", relatedFileExtent);

        String securityClassification = buildAttr(NsiliConstants.NSIL_SECURITY,
                NsiliConstants.CLASSIFICATION);
        addMapping(NsiliConstants.STANAG_4545, "ISCLAS", securityClassification);
        addMapping(NsiliConstants.NSIL_CORE, "PSCLAS", securityClassification);
        addMapping(NsiliConstants.STANAG_4607,
                "/GMTI/PacketHeader/Security/Classification",
                securityClassification);
        addMapping(NsiliConstants.STANAG_4609, "SecurityClassification", securityClassification);

        String securityPolicy = buildAttr(NsiliConstants.NSIL_SECURITY, NsiliConstants.POLICY);
        addMapping(NsiliConstants.STANAG_4545, "ISCLSY", securityPolicy);
        addMapping(NsiliConstants.NSIL_CORE, "PSCLSY", securityPolicy);
        addMapping(NsiliConstants.STANAG_4607,
                "/GMTI/PacketHeader/Security/ClassificationSystem",
                securityPolicy);

        String securityReleasability = buildAttr(NsiliConstants.NSIL_SECURITY,
                NsiliConstants.RELEASABILITY);
        addMapping(NsiliConstants.STANAG_4545, "ISREL", securityReleasability);
        addMapping(NsiliConstants.NSIL_CORE, "PSREL", securityReleasability);
        addMapping(NsiliConstants.STANAG_4609, "ReleasingInstructions", securityReleasability);

        String streamCreator = buildAttr(NsiliConstants.NSIL_STREAM, NsiliConstants.CREATOR);
        addMapping(NsiliConstants.STANAG_4545, "OSTAID", streamCreator);
        addMapping(NsiliConstants.NSIL_CORE, "OSTAID", streamCreator);

        String streamDateDeclared = buildAttr(NsiliConstants.NSIL_STREAM,
                NsiliConstants.DATE_TIME_DECLARED);
        addMapping(NsiliConstants.STANAG_4545, "FDT", streamDateDeclared);
        addMapping(NsiliConstants.NSIL_CORE, "FDT", streamDateDeclared);

        String streamStdVer = buildAttr(NsiliConstants.NSIL_STREAM,
                NsiliConstants.STANDARD_VERSION);
        addMapping(NsiliConstants.STANAG_4607, "/GMTI/PacketHeader/VersionID", streamStdVer);

        String tdlTrackNum = buildAttr(NsiliConstants.NSIL_TDL, NsiliConstants.TRACK_NUM);
        addMapping(NsiliConstants.STANAG_5516, "TRACK NUMBER REFERENCE", tdlTrackNum);

        String tdlMessageNum = buildAttr(NsiliConstants.NSIL_TDL, NsiliConstants.MESSAGE_NUM);
        addMapping(NsiliConstants.NACT_L16, "/HJ1/MessageNumber", tdlMessageNum);

    }

    private void addMapping(String category, String aliasName, String nsilName) {
        Pair<String, String> aliasPair = new ImmutablePair<>(aliasName, nsilName);
        List<Pair<String, String>> pairs = aliasCategoryMap.get(category);
        if (pairs == null) {
            pairs = new ArrayList<>();
            aliasCategoryMap.put(category, pairs);
        }

        pairs.add(aliasPair);
    }

    private static String buildAttr(String gParent, String parent, String attr) {
        return gParent + ":" + buildAttr(parent, attr);
    }

    private static String buildAttr(String parent, String attr) {
        return parent + "." + attr;
    }

    private void initAssociations() {
        List<AttributeInformation> associationAttrs = new ArrayList<>();
        associationAttrs.addAll(NsiliAttributesGenerator.getNsilCardAttributes());
        associationAttrs.addAll(NsiliAttributesGenerator.getNsilRelationAttributes());
        AttributeInformation[] associationAttrArray =
                associationAttrs.toArray(new AttributeInformation[0]);

        Association hasPartAssoc = new Association(NsiliConstants.HAS_PART,
                NsiliConstants.NSIL_ALL_VIEW,
                NsiliConstants.NSIL_ALL_VIEW,
                "Described resource (e.g. 'Target Folder') includes the referenced resource either physically or logically.",
                Cardinality.MANY_TO_MANY,
                associationAttrArray);

        Association isVersionAssoc = new Association(NsiliConstants.IS_VERSION_OF,
                NsiliConstants.NSIL_ALL_VIEW,
                NsiliConstants.NSIL_ALL_VIEW,
                "Described resource (source) is a version edition or adaptation of the referenced resource (destination). A change in version implies substantive changes in content rather than differences in format.",
                Cardinality.MANY_TO_ONE,
                associationAttrArray);

        Association replacesAssoc = new Association(NsiliConstants.REPLACES,
                NsiliConstants.NSIL_ALL_VIEW,
                NsiliConstants.NSIL_ALL_VIEW,
                "Described resource (source) supplants, displaces or supersedes the referenced resource (destination).",
                Cardinality.ONE_TO_MANY,
                associationAttrArray);

        Association isSupportDataToAssoc = new Association(NsiliConstants.IS_SUPPORT_DATA_TO,
                NsiliConstants.NSIL_ALL_VIEW,
                NsiliConstants.NSIL_ALL_VIEW,
                "Described resource (source) supplements information to the referenced RFI and IR (destination)",
                Cardinality.ONE_TO_MANY,
                associationAttrArray);

        Association originatingFromAssoc = new Association(NsiliConstants.ORIGINATING_FROM,
                NsiliConstants.NSIL_ALL_VIEW,
                NsiliConstants.NSIL_ALL_VIEW,
                "Described resource (source) originates from referenced resource (destination) and thereby shows the hierarchical relationship between the two products.",
                Cardinality.MANY_TO_ONE,
                associationAttrArray);

        Association followsAssoc = new Association(NsiliConstants.FOLLOWS,
                NsiliConstants.NSIL_ALL_VIEW,
                NsiliConstants.NSIL_ALL_VIEW,
                "Described resource (source in association) is the next in the chronological sequence after the referenced resource (destination in the association).",
                Cardinality.ONE_TO_ONE,
                associationAttrArray);

        associations.add(hasPartAssoc);
        associations.add(isVersionAssoc);
        associations.add(replacesAssoc);
        associations.add(isSupportDataToAssoc);
        associations.add(originatingFromAssoc);
        associations.add(followsAssoc);
    }

    public EntityGraph getEntityGraph(String viewName) {
        EntityGraph entityGraph = viewGraphMap.get(viewName);
        if (entityGraph == null) {
            LOGGER.debug("Returning null entity graph for view name: {}", viewName);
        }
        return entityGraph;
    }

    public List<AttributeInformation> getAttributesForView(String viewName) {
        List<AttributeInformation> attributeInformation = new ArrayList<>();

        EntityGraph graph = getEntityGraph(viewName);
        for (EntityNode node : graph.nodes) {
            List<AttributeInformation> nodeAttrs = getAttributeInformation(node.entity_name);
            if (nodeAttrs != null) {
                attributeInformation.addAll(nodeAttrs);
            }
        }

        return attributeInformation;
    }

    public List<String> getAliasCategories() {
        return new ArrayList<>(aliasCategoryMap.keySet());
    }

    public List<Pair<String, String>> getAliasesForCategory(String categoryName) {
        return new ArrayList<>(aliasCategoryMap.get(categoryName));
    }

    public List<Pair<ConceptualAttributeType, String>> getConceptualAttrsForView(String viewName) {
        return conceptualAttrMap.get(viewName);
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public Map<String, List<String>> getRequiredAttrsForView(String viewName) {
        return requiredAttrMap.get(viewName);
    }

    private void updateMandatoryAttrs(String viewName, EntityNode[] viewNodes) {
        Map<String, List<String>> attrMap = new HashMap<>();

        for (EntityNode entityNode : viewNodes) {
            List<AttributeInformation> nodeAttrs = getAttributeInformation(entityNode.entity_name);
            if (nodeAttrs != null) {
                for (AttributeInformation nodeAttr : nodeAttrs) {
                    if (nodeAttr.mode == RequirementMode.MANDATORY) {
                        String attributeName = nodeAttr.attribute_name;
                        String[] attrNameArr = attributeName.split("\\.");
                        if (attrNameArr.length == 2) {
                            String parentNode = attrNameArr[0];
                            String attrName = attrNameArr[1];
                            List<String> attrs = attrMap.get(parentNode);
                            if (attrs == null) {
                                attrs = new ArrayList<>(4);
                                attrMap.put(parentNode, attrs);
                            }
                            attrs.add(attrName);
                        }
                    }
                }
            }
        }
        requiredAttrMap.put(viewName, attrMap);
    }

}
