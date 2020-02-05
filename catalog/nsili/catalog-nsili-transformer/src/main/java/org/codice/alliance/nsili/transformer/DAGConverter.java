/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.transformer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.NullConverter;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeImpl;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.impl.types.ContactAttributes;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.operation.ResourceResponse;
import ddf.catalog.resource.ResourceNotFoundException;
import ddf.catalog.resource.ResourceNotSupportedException;
import ddf.catalog.resource.ResourceReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.nsili.common.CorbaUtils;
import org.codice.alliance.nsili.common.NsiliConstants;
import org.codice.alliance.nsili.common.ResultDAGConverter;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.RectangleHelper;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAGConverter {

  private static final long MEGABYTE = 1024L * 1024L;

  private static final Logger LOGGER = LoggerFactory.getLogger(DAGConverter.class);

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  private static String sourceId;

  private ResourceReader resourceReader;

  private String relatedFileType;

  private String relatedFileUrl;

  private XStream xstream;

  private MetacardType nsiliMetacardType;

  private static Map<String, String> typeConversionMap;

  static {
    // TODO (CAL-520): Complete NSIL -> DCMI type mapping
    typeConversionMap = new HashMap<>();
    typeConversionMap.put("COLLECTION/EXPLOITATION PLAN", "Collection");
    typeConversionMap.put("DOCUMENT", "Text");
    typeConversionMap.put("GEOGRAPHIC AREA OF INTEREEST", "Dataset");
    // typeConversionMap.put("GEOSPATIAL VECTOR", "");
    // typeConversionMap.put("GMTI", "");
    typeConversionMap.put("IMAGERY", "Image");
    // typeConversionMap.put("INTELLIGENCE REQUIREMENT", "");
    typeConversionMap.put("MESSAGE", "Text");
    // typeConversionMap.put("OPERATIONAL ROLES", "");
    // typeConversionMap.put("ORBAT", "");
    // typeConversionMap.put("REPORT", "");
    // typeConversionMap.put("RFI", "");
    // typeConversionMap.put("SYSTEM ASSIGNMENTS", "");
    // typeConversionMap.put("SYSTEM SPECIFICATIONS, "");
    // typeConversionMap.put("SYSTEM DEPLOYMENT STATUS, "");
    // typeConversionMap.put("TACTICAL SYMBOL", "");
    // typeConversionMap.put("TDL DATA", "");
    typeConversionMap.put("VIDEO", "Video");
    typeConversionMap.put("CBRN", "Text");
    typeConversionMap.put("ELECTRONIC ORDER OF BATTLE", "Interactive Resource");
  }

  public DAGConverter(ResourceReader resourceReader) {
    this.resourceReader = resourceReader;
  }

  public void setNsiliMetacardType(MetacardType nsiliMetacardType) {
    this.nsiliMetacardType = nsiliMetacardType;
  }

  public MetacardImpl convertDAG(DAG dag, boolean swapCoordinates, String logSourceId) {
    MetacardImpl metacard = null;
    sourceId = logSourceId;
    String metadata;

    // Need to have at least 2 nodes and an edge for anything useful
    if (dag.nodes != null && dag.edges != null) {
      Map<Integer, Node> nodeMap = ResultDAGConverter.createNodeMap(dag.nodes);
      DirectedAcyclicGraph<Node, Edge> graph = getNodeEdgeDirectedAcyclicGraph(dag, nodeMap);

      metacard = parseGraph(graph, swapCoordinates);
      metacard.setSourceId(sourceId);

      metadata = dagToXML(dag);
      metacard.setMetadata(metadata);
    }

    return metacard;
  }

  private MetacardImpl parseGraph(DirectedAcyclicGraph<Node, Edge> graph, boolean swapCoordinates) {
    MetacardImpl metacard = new MetacardImpl(nsiliMetacardType);

    List<Serializable> associatedCards = new ArrayList<>();

    // Traverse the graph
    DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph);
    Node parentEntity = null;
    Node assocNode = null;

    while (depthFirstIterator.hasNext()) {
      Node node = depthFirstIterator.next();

      if (node.node_type == NodeType.ROOT_NODE
          && node.attribute_name.equals(NsiliConstants.NSIL_PRODUCT)) {
        // Nothing to process from root node
      } else if (node.node_type == NodeType.ENTITY_NODE) {
        parentEntity = node;
        assocNode = getAssocNode(graph, assocNode, node);
      } else if (node.node_type == NodeType.RECORD_NODE) {
        // Nothing to process from record node
      } else if (parentEntity != null
          && node.node_type == NodeType.ATTRIBUTE_NODE
          && node.value != null) {
        addNsiliAttribute(
            swapCoordinates, metacard, associatedCards, parentEntity, assocNode, node);
      }
    }

    // Add associated data
    if (!associatedCards.isEmpty()) {
      boolean firstAssoc = true;
      AttributeImpl attribute = null;
      for (Serializable association : associatedCards) {
        if (firstAssoc) {
          attribute = new AttributeImpl(Associations.RELATED, association);
          metacard.setAttribute(attribute);
          firstAssoc = false;
        } else {
          attribute.addValue(association);
        }
      }
    }

    return metacard;
  }

  private void addNsiliAttribute(
      boolean swapCoordinates,
      MetacardImpl metacard,
      List<Serializable> associatedCards,
      Node parentEntity,
      Node assocNode,
      Node node) {
    switch (parentEntity.attribute_name) {
      case NsiliConstants.NSIL_CARD:
        if (assocNode != null) {
          addNsilAssociation(associatedCards, node);
        } else {
          addNsilCardAttribute(metacard, node);
        }
        break;
      case NsiliConstants.NSIL_SECURITY:
        addNsilSecurityAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_METADATA_SECURITY:
        addNsilMetadataSecurityAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_COMMON:
        addNsilCommonAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_COVERAGE:
        addNsilCoverageAttribute(metacard, node, swapCoordinates);
        break;
      case NsiliConstants.NSIL_EXPLOITATION_INFO:
        addNsilExploitationInfoAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_FILE:
        addNsilFileAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_GMTI:
        addNsilGmtiAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_IMAGERY:
        addNsilImageryAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_REPORT:
        addNsilReportAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_RFI:
        addNsilRfiAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_STREAM:
        addNsilStreamAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_TASK:
        addNsilTaskAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_TDL:
        addNsilTdlAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_VIDEO:
        addNsilVideoAttribute(metacard, node);
        break;
      case NsiliConstants.NSIL_RELATED_FILE:
        addNsilRelatedFile(metacard, node);
        break;
      case NsiliConstants.NSIL_CBRN:
        addNsilCbrnAttribute(metacard, node);
        break;
      default:
        break;
    }
  }

  private Node getAssocNode(DirectedAcyclicGraph<Node, Edge> graph, Node assocNode, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.NSIL_ASSOCIATION:
        return node;
      case NsiliConstants.NSIL_RELATED_FILE:
        relatedFileType = "";
        relatedFileUrl = "";
        break;
      default:
        if (assocNode != null && !isNodeChildOfStart(graph, assocNode, node)) {
          return null;
        }
        break;
    }
    return assocNode;
  }

  /**
   * Determines if the end node is a child of the start node.
   *
   * @param graph - The complete graph.
   * @param start - Starting node.
   * @param end - Child node to check
   * @return true if the end node is a child of the start node in the provided graph.
   */
  private static boolean isNodeChildOfStart(
      DirectedAcyclicGraph<Node, Edge> graph, Node start, Node end) {
    boolean endNodeInTree = false;
    DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph, start);
    while (depthFirstIterator.hasNext()) {
      Node currNode = depthFirstIterator.next();
      if (currNode.id == end.id) {
        endNodeInTree = true;
      }
    }
    return endNodeInTree;
  }

  private void addNsilCardAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.IDENTIFIER:
        metacard.setId(getString(node.value));
        break;
      case NsiliConstants.SOURCE_DATE_TIME_MODIFIED:
        Date cardDate = CorbaUtils.convertDate(node.value);
        metacard.setCreatedDate(cardDate);
        break;
      case NsiliConstants.DATE_TIME_MODIFIED:
        metacard.setModifiedDate(CorbaUtils.convertDate(node.value));
        break;
      case NsiliConstants.PUBLISHER:
        metacard.setAttribute(
            new AttributeImpl(ContactAttributes.PUBLISHER_NAME, getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilCommonAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.DESCRIPTION_ABSTRACT:
        addDescription(metacard, getString(node.value));
        break;
      case NsiliConstants.IDENTIFIER_MISSION:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.MISSION_ID, getString(node.value)));
        break;
      case NsiliConstants.IDENTIFIER_UUID:
        metacard.setId(getString(node.value));
        break;
      case NsiliConstants.IDENTIFIER_JC3IEDM:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.JC3IEDM_ID, getInteger(node.value)));
        break;
      case NsiliConstants.LANGUAGE:
        metacard.setAttribute(new AttributeImpl(CoreAttributes.LANGUAGE, getString(node.value)));
        break;
      case NsiliConstants.SOURCE:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.PLATFORM_NAME, getString(node.value)));
        break;
      case NsiliConstants.SUBJECT_CATEGORY_TARGET:
        String categoryCodes = getString(node.value);
        if (categoryCodes != null) {
          List<Serializable> codes =
              Stream.of(categoryCodes.split(",")).map(String::trim).collect(Collectors.toList());
          metacard.setAttribute(new AttributeImpl(IsrAttributes.NATO_REPORTING_CODE, codes));
        }
        break;
      case NsiliConstants.TARGET_NUMBER:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.TARGET_ID, getString(node.value)));
        break;
      case NsiliConstants.TYPE:
        metacard.setAttribute(
            new AttributeImpl(CoreAttributes.DATATYPE, translateType(getString(node.value))));
        break;
      default:
        break;
    }
  }

  private void addNsilCoverageAttribute(MetacardImpl metacard, Node node, boolean swapCoordinates) {
    switch (node.attribute_name) {
      case NsiliConstants.SPATIAL_COUNTRY_CODE:
        metacard.setAttribute(new AttributeImpl(Location.COUNTRY_CODE, getString(node.value)));
        break;
      case NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX:
        if (metacard.getLocation() == null) {
          metacard.setLocation(convertShape(node.value, swapCoordinates));
        }
        break;
      case NsiliConstants.ADVANCED_GEOSPATIAL:
        String wkt = checkForEmptyPolygon(node.value, swapCoordinates);
        if (wkt != null) {
          metacard.setLocation(wkt);
        }
        break;
      case NsiliConstants.TEMPORAL_START:
        metacard.setAttribute(
            new AttributeImpl(DateTime.START, CorbaUtils.convertDate(node.value)));
        break;
      case NsiliConstants.TEMPORAL_END:
        Date temporalEnd = CorbaUtils.convertDate(node.value);
        metacard.setAttribute(new AttributeImpl(DateTime.END, temporalEnd));
        metacard.setEffectiveDate(temporalEnd);
        break;
      default:
        break;
    }
  }

  private void addNsilExploitationInfoAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.DESCRIPTION:
        addDescription(metacard, getString(node.value));
        break;
      case NsiliConstants.LEVEL:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.EXPLOITATION_LEVEL, getShort(node.value)));
        break;
      case NsiliConstants.AUTO_GENERATED:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.EXPLOTATION_AUTO_GENERATED, node.value.extract_boolean()));
        break;
      case NsiliConstants.SUBJ_QUALITY_CODE:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.EXPLOITATION_SUBJECTIVE_QUALITY_CODE, getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilFileAttribute(MetacardImpl metacard, Node node) {

    switch (node.attribute_name) {
      case NsiliConstants.CREATOR:
        metacard.setAttribute(new AttributeImpl(Isr.ORGANIZATIONAL_UNIT, getString(node.value)));
        break;
      case NsiliConstants.DATE_TIME_DECLARED:
        metacard.setCreatedDate(CorbaUtils.convertDate(node.value));
        break;
      case NsiliConstants.EXTENT:
        metacard.setResourceSize(
            String.valueOf(convertMegabytesToBytes(node.value.extract_double())));
        break;
      case NsiliConstants.FORMAT:
        metacard.setAttribute(new AttributeImpl(Media.FORMAT, getString(node.value)));
        break;
      case NsiliConstants.FORMAT_VERSION:
        metacard.setAttribute(new AttributeImpl(Media.FORMAT_VERSION, getString(node.value)));
        break;
      case NsiliConstants.PRODUCT_URL:
        metacard.setResourceURI(convertURI(getString(node.value)));
        break;
      case NsiliConstants.TITLE:
        if (StringUtils.isBlank(metacard.getTitle())) {
          metacard.setTitle(getString(node.value));
        }
        break;
      default:
        break;
    }
  }

  private void addNsilGmtiAttribute(MetacardImpl metacard, Node node) {
    // If any GMTI node is added, then we will set the MetacardType
    switch (node.attribute_name) {
      case NsiliConstants.IDENTIFIER_JOB:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.MOVING_TARGET_INDICATOR_JOB_ID, node.value.extract_double()));
        break;
      case NsiliConstants.NUMBER_OF_TARGET_REPORTS:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.TARGET_REPORT_COUNT, getInteger(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilImageryAttribute(MetacardImpl metacard, Node node) {
    // If any Imagery attribute is added, set the card type
    switch (node.attribute_name) {
      case NsiliConstants.CATEGORY:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.CATEGORY, getString(node.value)));
        break;
      case NsiliConstants.CLOUD_COVER_PCT:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.CLOUD_COVER, getDoubleFromShort(node.value)));
        break;
      case NsiliConstants.COMMENTS:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.COMMENTS, getString(node.value)));
        break;
      case NsiliConstants.DECOMPRESSION_TECHNIQUE:
        metacard.setAttribute(new AttributeImpl(Media.COMPRESSION, getString(node.value)));
        break;
      case NsiliConstants.IDENTIFIER:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.IMAGE_ID, getString(node.value)));
        break;
      case NsiliConstants.NIIRS:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE,
                getDoubleFromShort(node.value)));
        break;
      case NsiliConstants.NUMBER_OF_BANDS:
        metacard.setAttribute(new AttributeImpl(Media.NUMBER_OF_BANDS, getInteger(node.value)));
        break;
      case NsiliConstants.NUMBER_OF_ROWS:
        metacard.setAttribute(new AttributeImpl(Media.HEIGHT, getInteger(node.value)));
        break;
      case NsiliConstants.NUMBER_OF_COLS:
        metacard.setAttribute(new AttributeImpl(Media.WIDTH, getInteger(node.value)));
        break;
      case NsiliConstants.TITLE:
        metacard.setTitle(getString(node.value));
        break;
      default:
        break;
    }
  }

  private void addNsilReportAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.ORIGINATORS_REQ_SERIAL_NUM:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.REPORT_SERIAL_NUMBER, getString(node.value)));
        break;
      case NsiliConstants.PRIORITY:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.REPORT_PRIORITY, getString(node.value)));
        break;
      case NsiliConstants.TYPE:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.REPORT_TYPE, getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilRfiAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.FOR_ACTION:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.REQUEST_FOR_INFORMATION_FOR_ACTION, getString(node.value)));
        break;
      case NsiliConstants.FOR_INFORMATION:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.REQUEST_FOR_INFORMATION_FOR_INFORMATION, getString(node.value)));
        break;
      case NsiliConstants.SERIAL_NUMBER:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.REQUEST_FOR_INFORMATION_SERIAL_NUMBER, getString(node.value)));
        break;
      case NsiliConstants.STATUS:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.REQUEST_FOR_INFORMATION_STATUS, getString(node.value)));
        break;
      case NsiliConstants.WORKFLOW_STATUS:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.REQUEST_FOR_INFORMATION_WORKFLOW_STATUS, getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilSecurityAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.POLICY:
        metacard.setAttribute(
            new AttributeImpl(Security.RESOURCE_CLASSIFICATION_SYSTEM, getString(node.value)));
        break;
      case NsiliConstants.RELEASABILITY:
        metacard.setAttribute(
            new AttributeImpl(Security.RESOURCE_RELEASABILITY, getString(node.value)));
        break;
      case NsiliConstants.CLASSIFICATION:
        metacard.setAttribute(
            new AttributeImpl(Security.RESOURCE_CLASSIFICATION, getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilMetadataSecurityAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.POLICY:
        metacard.setAttribute(
            new AttributeImpl(Security.METADATA_CLASSIFICATION_SYSTEM, getString(node.value)));
        break;
      case NsiliConstants.RELEASABILITY:
        metacard.setAttribute(
            new AttributeImpl(Security.METADATA_RELEASABILITY, getString(node.value)));
        break;
      case NsiliConstants.CLASSIFICATION:
        metacard.setAttribute(
            new AttributeImpl(Security.METADATA_CLASSIFICATION, getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilStreamAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.CREATOR:
        metacard.setAttribute(new AttributeImpl(Isr.ORGANIZATIONAL_UNIT, getString(node.value)));
        break;
      case NsiliConstants.DATE_TIME_DECLARED:
        metacard.setCreatedDate(CorbaUtils.convertDate(node.value));
        break;
      case NsiliConstants.STANDARD:
        metacard.setAttribute(new AttributeImpl(Media.FORMAT, getString(node.value)));
        break;
      case NsiliConstants.STANDARD_VERSION:
        metacard.setAttribute(new AttributeImpl(Media.FORMAT_VERSION, getString(node.value)));
        break;
      case NsiliConstants.SOURCE_URL:
        metacard.setResourceURI(convertURI(getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilTaskAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.COMMENTS:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.TASK_COMMENTS, getString(node.value)));
        break;
      case NsiliConstants.STATUS:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.TASK_STATUS, getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilTdlAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.ACTIVITY:
        metacard.setAttribute(
            new AttributeImpl(IsrAttributes.TACTICAL_DATA_LINK_ACTIVITY, getShort(node.value)));
        break;

      case NsiliConstants.MESSAGE_NUM:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.TACTICAL_DATA_LINK_MESSAGE_NUMBER, getString(node.value)));
        break;
      case NsiliConstants.PLATFORM:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.PLATFORM_ID, getShort(node.value)));
        break;
      case NsiliConstants.TRACK_NUM:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.TACTICAL_DATA_LINK_TRACK_NUMBER, getString(node.value)));
        break;
      default:
        break;
    }
  }

  private void addNsilVideoAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.AVG_BIT_RATE:
        metacard.setAttribute(
            new AttributeImpl(Media.BITS_PER_SECOND, node.value.extract_double()));
        break;
      case NsiliConstants.CATEGORY:
        metacard.setAttribute(new AttributeImpl(IsrAttributes.CATEGORY, getString(node.value)));
        break;
      case NsiliConstants.ENCODING_SCHEME:
        metacard.setAttribute(new AttributeImpl(Media.ENCODING, getString(node.value)));
        break;
      case NsiliConstants.FRAME_RATE:
        metacard.setAttribute(
            new AttributeImpl(Media.FRAMES_PER_SECOND, node.value.extract_double()));
        break;
      case NsiliConstants.NUMBER_OF_ROWS:
        metacard.setAttribute(new AttributeImpl(Media.HEIGHT, getInteger(node.value)));
        break;
      case NsiliConstants.NUMBER_OF_COLS:
        metacard.setAttribute(new AttributeImpl(Media.WIDTH, getInteger(node.value)));
        break;
      case NsiliConstants.MISM_LEVEL:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.VIDEO_MOTION_IMAGERY_SYSTEMS_MATRIX_LEVEL, getShort(node.value)));
        break;
      case NsiliConstants.SCANNING_MODE:
        metacard.setAttribute(new AttributeImpl(Media.SCANNING_MODE, getString(node.value)));
        break;
      case NsiliConstants.VMTI_PROCESSED:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.VIDEO_MOVING_TARGET_INDICATOR_PROCESSED,
                node.value.extract_boolean()));
        break;
      default:
        break;
    }
  }

  private void addNsilAssociation(List<Serializable> associations, Node node) {
    if (node.attribute_name.equals(NsiliConstants.IDENTIFIER)) {
      associations.add(getString(node.value));
    }
  }

  private String addNsilRelatedFile(MetacardImpl metacard, Node node) {
    if (node.attribute_name.equals(NsiliConstants.FILE_TYPE)) {
      relatedFileType = getString(node.value);
      if (NsiliConstants.THUMBNAIL_TYPE.equalsIgnoreCase(relatedFileType)
          && StringUtils.isNotBlank(relatedFileUrl)) {
        metacard.setThumbnail(getThumbnail(relatedFileUrl));
      }
    } else if (node.attribute_name.equals(NsiliConstants.URL)) {
      relatedFileUrl = getString(node.value);
      if (NsiliConstants.THUMBNAIL_TYPE.equalsIgnoreCase(relatedFileType)
          && StringUtils.isNotBlank(relatedFileUrl)) {
        metacard.setThumbnail(getThumbnail(relatedFileUrl));
      }
    }
    return relatedFileType;
  }

  private void addNsilCbrnAttribute(MetacardImpl metacard, Node node) {
    switch (node.attribute_name) {
      case NsiliConstants.OPERATION_NAME:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_OPERATION_NAME,
                getString(node.value)));
        break;
      case NsiliConstants.INCIDENT_NUM:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_INCIDENT_NUMBER,
                getString(node.value)));
        break;
      case NsiliConstants.EVENT_TYPE:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_TYPE,
                getString(node.value)));
        break;
      case NsiliConstants.CBRN_CATEGORY:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_CATEGORY,
                getString(node.value)));
        break;
      case NsiliConstants.SUBSTANCE:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_SUBSTANCE,
                getString(node.value)));
        break;
      case NsiliConstants.ALARM_CLASSIFICATION:
        metacard.setAttribute(
            new AttributeImpl(
                IsrAttributes.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_ALARM_CLASSIFICATION,
                getString(node.value)));
        break;
      default:
        break;
    }
  }

  private String convertShape(Any any, boolean swapCoordinates) {
    org.codice.alliance.nsili.common.UCO.Rectangle rectangle = RectangleHelper.extract(any);
    org.codice.alliance.nsili.common.UCO.Coordinate2d upperLeft = rectangle.upper_left;
    org.codice.alliance.nsili.common.UCO.Coordinate2d lowerRight = rectangle.lower_right;

    Geometry geom;

    final WKTWriter wktWriter = new WKTWriter();

    if (upperLeft.x == lowerRight.x && upperLeft.y == lowerRight.y) {
      // Build a Point vs Polygon
      Coordinate pointCoord;
      if (swapCoordinates) {
        pointCoord = new Coordinate(upperLeft.y, upperLeft.x);
      } else {
        pointCoord = new Coordinate(upperLeft.x, upperLeft.y);
      }
      geom = GEOMETRY_FACTORY.createPoint(pointCoord);
    } else {
      Coordinate[] coordinates = new Coordinate[5];
      Coordinate lowerLeftCoord;
      Coordinate upperLeftCoord;
      Coordinate upperRightCoord;
      Coordinate lowerRightCoord;

      if (swapCoordinates) {
        lowerLeftCoord = new Coordinate(upperLeft.y, lowerRight.x);
        upperLeftCoord = new Coordinate(upperLeft.y, upperLeft.x);
        upperRightCoord = new Coordinate(lowerRight.y, upperLeft.x);
        lowerRightCoord = new Coordinate(lowerRight.y, lowerRight.x);
      } else {
        lowerLeftCoord = new Coordinate(upperLeft.x, lowerRight.y);
        upperLeftCoord = new Coordinate(upperLeft.x, upperLeft.y);
        upperRightCoord = new Coordinate(lowerRight.x, upperLeft.y);
        lowerRightCoord = new Coordinate(lowerRight.x, lowerRight.y);
      }

      coordinates[0] = lowerLeftCoord;
      coordinates[1] = upperLeftCoord;
      coordinates[2] = upperRightCoord;
      coordinates[3] = lowerRightCoord;
      coordinates[4] = lowerLeftCoord;

      LinearRing shell = GEOMETRY_FACTORY.createLinearRing(coordinates);
      geom = new Polygon(shell, null, GEOMETRY_FACTORY);
    }

    return wktWriter.write(geom);
  }

  private static String checkForEmptyPolygon(Any any, boolean swapCoordinates) {
    String wkt = getString(any);
    Geometry geom;
    if (wkt != null) {
      WKTReader reader = new WKTReader(GEOMETRY_FACTORY);
      try {
        geom = reader.read(wkt);
        if (geom instanceof Polygon) {
          if (geom.getArea() > 0.0) {
            LOGGER.debug("Polygon detected with valid area.");
          } else {
            LOGGER.debug("Polygon detected with empty area - converting to point.");
            Coordinate pointCoord = geom.getCoordinate();
            if (swapCoordinates) {
              pointCoord = new Coordinate(pointCoord.y, pointCoord.x);
            }
            geom = GEOMETRY_FACTORY.createPoint(pointCoord);
          }
        }
        final WKTWriter wktWriter = new WKTWriter();
        wkt = wktWriter.write(geom);
      } catch (ParseException e) {
        LOGGER.debug("Unable to parse WKT of {}", wkt);
        wkt = null;
      }
    }
    return wkt;
  }

  public static int convertMegabytesToBytes(Double megabytes) {
    int bytes = 0;

    if (megabytes != null) {
      bytes = (int) (megabytes * MEGABYTE);
    }

    return bytes;
  }

  private static URI convertURI(String uriStr) {
    URI uri = null;

    try {
      uri = new URI(uriStr);
    } catch (URISyntaxException e) {
      LOGGER.debug("Unable to parse URI for metacard: {}", uriStr, e);
    }

    return uri;
  }

  public static String getString(Any any) {
    if (any.type().kind() == TCKind.tk_wstring) {
      return any.extract_wstring();
    } else if (any.type().kind() == TCKind.tk_string) {
      return any.extract_string();
    }
    return null;
  }

  public static Integer getInteger(Any any) {
    if (any.type().kind() == TCKind.tk_long) {
      return any.extract_long();
    } else if (any.type().kind() == TCKind.tk_ulong) {
      return any.extract_ulong();
    } else if (any.type().kind() == TCKind.tk_longlong) {
      // STANAG-4559 Unsigned Longs are within Java's Integer range
      Long longVal = any.extract_longlong();
      return longVal.intValue();
    }
    return null;
  }

  private static Short getShort(Any any) {
    if (any.type().kind() == TCKind.tk_short) {
      return any.extract_short();
    } else if (any.type().kind() == TCKind.tk_ushort) {
      return any.extract_ushort();
    }
    return null;
  }

  private static Double getDoubleFromShort(Any any) {
    Short theShort = getShort(any);
    if (theShort != null) {
      return (double) theShort;
    }

    return null;
  }

  public static void logMetacard(Metacard metacard, String id) {
    MetacardType metacardType = metacard.getMetacardType();
    LOGGER.trace("{} : ID : {}", id, metacard.getId());
    LOGGER.trace("{} :  Metacard Type : {}", id, metacardType.getClass().getCanonicalName());
    LOGGER.trace("{} :  Title : {}", id, metacard.getTitle());
    if (metacard instanceof MetacardImpl) {
      LOGGER.trace("{} :  Description : {}", id, ((MetacardImpl) metacard).getDescription());
    }
    LOGGER.trace("{} :  Content Type Name : {}", id, metacard.getContentTypeName());
    LOGGER.trace("{} :  Content Type Version : {}", id, metacard.getContentTypeVersion());
    LOGGER.trace("{} :  Created Date : {}", id, metacard.getCreatedDate());
    LOGGER.trace("{} :  Effective Date : {}", id, metacard.getEffectiveDate());
    LOGGER.trace("{} :  Location : {}", id, metacard.getLocation());
    LOGGER.trace("{} :  SourceID : {}", id, metacard.getSourceId());
    LOGGER.trace("{} :  Modified Date : {}", id, metacard.getModifiedDate());
    if (metacard.getResourceURI() != null) {
      LOGGER.trace("{} :  Resource URI : {}", id, metacard.getResourceURI());
    }

    Set<AttributeDescriptor> descriptors = metacardType.getAttributeDescriptors();
    for (AttributeDescriptor descriptor : descriptors) {
      Attribute attribute = metacard.getAttribute(descriptor.getName());
      if (attribute != null) {
        if (attribute.getValues() != null) {
          String valueStr = getValueString(attribute.getValues());
          LOGGER.trace("{} : {} : {}", id, descriptor.getName(), valueStr);
        } else {
          LOGGER.trace("{} : {} : {}", id, descriptor.getName(), attribute.getValue());
        }
      }
    }
  }

  public static String getValueString(Collection<Serializable> collection) {
    return collection.stream().map(Object::toString).sorted().collect(Collectors.joining(", "));
  }

  private byte[] getThumbnail(String thumbnailUrlStr) {
    byte[] thumbnail = null;

    try {
      URI thumbnailURI = new URI(thumbnailUrlStr);
      ResourceResponse resourceResponse = null;
      try {
        resourceResponse = resourceReader.retrieveResource(thumbnailURI, new HashMap<>());
        thumbnail = resourceResponse.getResource().getByteArray();
      } catch (ResourceNotSupportedException e) {
        LOGGER.debug("Resource is not supported: {} ", thumbnailURI, e);
      }
    } catch (IOException | ResourceNotFoundException | URISyntaxException e) {
      LOGGER.debug("Unable to get thumbnail from URL {}", thumbnailUrlStr, e);
    }

    return thumbnail;
  }

  private void addDescription(Metacard metacard, String description) {
    Attribute descAttr = metacard.getAttribute(Core.DESCRIPTION);
    if (descAttr != null) {
      descAttr.getValues().add(description);
    } else {
      metacard.setAttribute(new AttributeImpl(Core.DESCRIPTION, description));
    }
  }

  private String translateType(String nsilType) {
    String result = typeConversionMap.get(nsilType);
    if (result == null) {
      result = nsilType;
    }
    return result;
  }

  public static String printDAG(DAG dag) {
    if (dag.nodes == null || dag.edges == null) {
      return null;
    } else {
      StringBuilder sb = new StringBuilder();

      Map<Integer, Node> nodeMap = ResultDAGConverter.createNodeMap(dag.nodes);
      DirectedAcyclicGraph<Node, Edge> graph = getNodeEdgeDirectedAcyclicGraph(dag, nodeMap);

      DepthFirstIterator<Node, Edge> depthFirstIterator = new DepthFirstIterator<>(graph);
      Node rootNode = null;
      while (depthFirstIterator.hasNext()) {
        depthFirstIterator.setCrossComponentTraversal(false);
        Node node = depthFirstIterator.next();
        if (rootNode == null) {
          rootNode = node;
        }

        DijkstraShortestPath<Node, Edge> path = new DijkstraShortestPath<>(graph, rootNode, node);

        sb.append(printNode(node, (int) Math.round(path.getPathLength())));
      }

      return sb.toString();
    }
  }

  private static DirectedAcyclicGraph<Node, Edge> getNodeEdgeDirectedAcyclicGraph(
      DAG dag, Map<Integer, Node> nodeMap) {
    DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

    for (Node node : dag.nodes) {
      graph.addVertex(node);
    }

    for (Edge edge : dag.edges) {
      Node node1 = nodeMap.get(edge.start_node);
      Node node2 = nodeMap.get(edge.end_node);
      if (node1 != null && node2 != null) {
        graph.addEdge(node1, node2);
      }
    }
    return graph;
  }

  public static String printNode(Node node, int offset) {
    String attrName = node.attribute_name;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < offset; i++) {
      sb.append("\t");
    }

    if (node.node_type == NodeType.ATTRIBUTE_NODE) {
      if (node.value != null && node.value.type() != null) {
        sb.append(attrName);
        sb.append("=");
        sb.append(CorbaUtils.getNodeValue(node.value));
      }
    } else {
      sb.append(attrName);
    }
    sb.append('\n');
    return sb.toString();
  }

  private String dagToXML(DAG dag) {
    final String cleanupStr = " class=\"com.sun.corba.se.impl.corba.AnyImpl\"";

    xstream = new XStream();

    xstream.alias("dag", DAG.class);
    xstream.alias("node", Node.class);

    xstream.registerConverter(new NullConverter());
    xstream.registerConverter(new ArrayConverter(xstream.getMapper()));
    xstream.registerConverter(new AnyConverter());

    xstream.omitField(DAG.class, "edges");
    xstream.omitField(Node.class, "id");
    xstream.omitField(Node.class, "node_type");

    String xmlDAG = xstream.toXML(dag);
    xmlDAG = xmlDAG.replaceAll(cleanupStr, "");

    return xmlDAG;
  }
}
