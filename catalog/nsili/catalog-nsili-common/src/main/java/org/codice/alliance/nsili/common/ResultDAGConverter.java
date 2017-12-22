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
package org.codice.alliance.nsili.common;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import ddf.catalog.core.versioning.MetacardVersion;
import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.DateTime;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.alliance.nsili.common.UCO.AbsTime;
import org.codice.alliance.nsili.common.UCO.AbsTimeHelper;
import org.codice.alliance.nsili.common.UCO.DAG;
import org.codice.alliance.nsili.common.UCO.Edge;
import org.codice.alliance.nsili.common.UCO.Node;
import org.codice.alliance.nsili.common.UCO.NodeType;
import org.codice.alliance.nsili.common.UCO.Rectangle;
import org.codice.alliance.nsili.common.UCO.RectangleHelper;
import org.codice.alliance.nsili.common.UCO.Time;
import org.codice.alliance.nsili.common.UID.Product;
import org.codice.alliance.nsili.common.UID.ProductHelper;
import org.codice.ddf.configuration.SystemBaseUrl;
import org.codice.ddf.configuration.SystemInfo;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultDAGConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResultDAGConverter.class);

  private static final String CATALOG_SOURCE_PATH = "/catalog/sources";

  private static final String THUMBNAIL_TRANSFORMER = "thumbnail";

  private static final String ENCODING = StandardCharsets.UTF_8.name();

  private static final Pattern ATTRIBUTE_PATTERN =
      Pattern.compile("([a-zA-Z0-9_:]+):([a-zA-Z0-9_]+).([a-zA-Z0-9]+)");

  public static DAG convertResult(
      Result result,
      ORB orb,
      POA poa,
      List<String> resultAttributes,
      Map<String, List<String>> mandatoryAttributes)
      throws DagParsingException {
    Double distanceInMeters = result.getDistanceInMeters();
    Double resultScore = result.getRelevanceScore();
    Metacard metacard = result.getMetacard();

    DAG dag = new DAG();
    DirectedAcyclicGraph<Node, Edge> graph = new DirectedAcyclicGraph<>(Edge.class);

    ProductImpl productImpl = new ProductImpl();

    String id = result.getMetacard().getId();

    if (!CorbaUtils.isIdActive(poa, id.getBytes(Charset.forName(ENCODING)))) {
      try {
        poa.activate_object_with_id(id.getBytes(Charset.forName(ENCODING)), productImpl);
      } catch (ServantAlreadyActive | ObjectAlreadyActive | WrongPolicy e) {
        LOGGER.debug(
            "Convert DAG : Unable to activate product impl object ({}): {}",
            result.getMetacard().getId(),
            e.getLocalizedMessage());
      }
    }

    org.omg.CORBA.Object obj =
        poa.create_reference_with_id(id.getBytes(Charset.forName(ENCODING)), ProductHelper.id());
    Product product = ProductHelper.narrow(obj);

    Node productNode = createRootNode(orb);
    String attributeName = NsiliConstants.NSIL_PRODUCT;

    Any productAny = orb.create_any();
    ProductHelper.insert(productAny, product);
    productNode.value = productAny;

    graph.addVertex(productNode);

    List<String> addedAttributes = new ArrayList<>();
    addedAttributes.addAll(
        addCardNodeWithAttributes(
            graph, productNode, metacard, orb, attributeName + ":", resultAttributes));
    addedAttributes.addAll(
        addFileNodeWithAttributes(
            graph, productNode, metacard, orb, attributeName + ":", resultAttributes));
    addedAttributes.addAll(
        addSecurityNodeWithAttributes(
            graph, productNode, metacard, orb, attributeName + ":", resultAttributes));
    addedAttributes.addAll(
        addMetadataSecurityNodeWithAttributes(
            graph, productNode, metacard, orb, attributeName + ":", resultAttributes));
    addedAttributes.addAll(
        addParts(graph, productNode, metacard, orb, attributeName + ":", resultAttributes));

    if (metacard.getThumbnail() != null && metacard.getThumbnail().length > 0) {
      addedAttributes.addAll(
          addThumbnailRelatedFile(
              graph, productNode, metacard, orb, attributeName + ":", resultAttributes));
    }

    if (mandatoryAttributes != null && !mandatoryAttributes.isEmpty()) {
      final ThreadLocal<Boolean> dataIsValid = new ThreadLocal<>();
      dataIsValid.set(true);
      Map<String, List<String>> addedAttrMap = getAttrMap(addedAttributes);
      addedAttrMap
          .entrySet()
          .forEach(
              entry ->
                  dataIsValid.set(
                      dataIsValid.get()
                          && processEntry(
                              entry.getKey(),
                              mandatoryAttributes.get(entry.getKey()),
                              entry.getValue())));

      if (!dataIsValid.get()) {
        throw new DagParsingException(
            "One or more mandatory attributes is missing on outgoing data");
      }
    }

    graph.addVertex(productNode);

    NsiliCommonUtils.setUCOEdgeIds(graph);
    NsiliCommonUtils.setUCOEdges(productNode, graph);
    dag.edges = NsiliCommonUtils.getEdgeArrayFromGraph(graph);
    dag.nodes = NsiliCommonUtils.getNodeArrayFromGraph(graph);

    return dag;
  }

  public static List<String> addCardNodeWithAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node productNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any any = orb.create_any();
    Node cardNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CARD, any);
    graph.addVertex(cardNode);
    graph.addEdge(productNode, cardNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_CARD;

    if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER), resultAttributes)
        && metacard.getId() != null) {
      addStringAttribute(graph, cardNode, NsiliConstants.IDENTIFIER, metacard.getId(), orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.IDENTIFIER));
    }

    addDateAttributes(graph, metacard, orb, resultAttributes, addedAttributes, cardNode, attribute);

    if (shouldAdd(buildAttr(attribute, NsiliConstants.SOURCE_LIBRARY), resultAttributes)) {
      if (StringUtils.isNotBlank(metacard.getSourceId())) {
        addStringAttribute(
            graph, cardNode, NsiliConstants.SOURCE_LIBRARY, metacard.getSourceId(), orb);
      } else {
        addStringAttribute(
            graph, cardNode, NsiliConstants.SOURCE_LIBRARY, NsiliConstants.UNKNOWN, orb);
      }
      addedAttributes.add(buildAttr(attribute, NsiliConstants.SOURCE_LIBRARY));
    }

    addStatusAttributes(
        graph, metacard, orb, resultAttributes, addedAttributes, cardNode, attribute);

    addStrAttribute(
        graph,
        metacard.getAttribute(Contact.PUBLISHER_NAME),
        orb,
        resultAttributes,
        addedAttributes,
        cardNode,
        attribute,
        NsiliConstants.PUBLISHER);

    return addedAttributes;
  }

  private static void addStatusAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node cardNode,
      String attribute) {
    if (!shouldAdd(buildAttr(attribute, NsiliConstants.STATUS), resultAttributes)) {
      return;
    }
    String status = NsiliCardStatus.CHANGED.name();
    Attribute createdAttr = metacard.getAttribute(Core.METACARD_CREATED);
    Attribute modifiedAttr = metacard.getAttribute(Core.METACARD_MODIFIED);
    if (createdAttr != null && modifiedAttr != null) {
      Date createdDate = (Date) createdAttr.getValue();
      Date modifiedDate = (Date) modifiedAttr.getValue();
      if (createdDate.equals(modifiedDate)) {
        status = NsiliCardStatus.NEW.name();
      }
    }

    Attribute versionAction = metacard.getAttribute(MetacardVersion.ACTION);

    if (versionAction != null) {
      for (Serializable action : versionAction.getValues()) {
        if (action.toString().trim().equals(MetacardVersion.Action.DELETED.getKey())) {
          status = NsiliCardStatus.OBSOLETE.name();
          break;
        }
      }
    }

    addStringAttribute(graph, cardNode, NsiliConstants.STATUS, status, orb);

    addedAttributes.add(buildAttr(attribute, NsiliConstants.STATUS));
  }

  private static void addDateAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node cardNode,
      String attribute) {
    if (metacard.getCreatedDate() != null
        && shouldAdd(
            buildAttr(attribute, NsiliConstants.SOURCE_DATE_TIME_MODIFIED), resultAttributes)) {
      addDateAttribute(
          graph,
          cardNode,
          NsiliConstants.SOURCE_DATE_TIME_MODIFIED,
          metacard.getCreatedDate(),
          orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.SOURCE_DATE_TIME_MODIFIED));
    }

    if (metacard.getModifiedDate() != null
        && shouldAdd(buildAttr(attribute, NsiliConstants.DATE_TIME_MODIFIED), resultAttributes)) {
      addDateAttribute(
          graph, cardNode, NsiliConstants.DATE_TIME_MODIFIED, metacard.getModifiedDate(), orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.DATE_TIME_MODIFIED));
    }
  }

  public static List<String> addFileNodeWithAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node productNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {

    List<String> addedAttributes = new ArrayList<>();

    Attribute downloadUrlAttr = metacard.getAttribute(Core.RESOURCE_DOWNLOAD_URL);

    if (downloadUrlAttr == null) {
      return addedAttributes;
    }
    Any any = orb.create_any();
    Node fileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_FILE, any);
    graph.addVertex(fileNode);
    graph.addEdge(productNode, fileNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_FILE;

    // Although not required, CSD Alpha requires this field to be populated on synchronization
    if (shouldAdd(buildAttr(attribute, NsiliConstants.ARCHIVED), resultAttributes)) {
      addBooleanAttribute(graph, fileNode, NsiliConstants.ARCHIVED, false, orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.ARCHIVED));
    }

    if (shouldAdd(buildAttr(attribute, NsiliConstants.TITLE), resultAttributes)
        && metacard.getTitle() != null) {
      addStringAttribute(graph, fileNode, NsiliConstants.TITLE, metacard.getTitle(), orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.TITLE));
    }

    if (shouldAdd(buildAttr(attribute, NsiliConstants.PRODUCT_URL), resultAttributes)) {
      String downloadUrl = String.valueOf(downloadUrlAttr.getValue());
      if (downloadUrl != null) {
        downloadUrl = modifyUrl(downloadUrl, metacard.getTitle());
        addStringAttribute(graph, fileNode, NsiliConstants.PRODUCT_URL, downloadUrl, orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.PRODUCT_URL));
      }
    }

    addExtentResourceSizeAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, fileNode, attribute);

    addTimeDeclaredAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, fileNode, attribute);

    addStrAttribute(
        graph,
        metacard.getAttribute(Media.FORMAT),
        orb,
        resultAttributes,
        addedAttributes,
        fileNode,
        attribute,
        NsiliConstants.FORMAT);

    addStrAttribute(
        graph,
        metacard.getAttribute(Media.FORMAT_VERSION),
        orb,
        resultAttributes,
        addedAttributes,
        fileNode,
        attribute,
        NsiliConstants.FORMAT_VERSION);

    addCreatorAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, fileNode, attribute);

    if (shouldAdd(buildAttr(attribute, NsiliConstants.IS_PRODUCT_LOCAL), resultAttributes)) {
      String siteName = SystemInfo.getSiteName();
      boolean productLocal = true;
      if (siteName != null
          && metacard.getSourceId() != null
          && !siteName.equals(metacard.getSourceId())) {
        productLocal = false;
      }
      addBooleanAttribute(graph, fileNode, NsiliConstants.IS_PRODUCT_LOCAL, productLocal, orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.IS_PRODUCT_LOCAL));
    }
    return addedAttributes;
  }

  private static void addExtentResourceSizeAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node fileNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.EXTENT), resultAttributes)
        && metacard.getResourceSize() != null) {
      try {
        Double resSize = Double.valueOf(metacard.getResourceSize());
        Double resSizeMB = convertToMegabytes(resSize);
        if (resSizeMB != null) {
          addDoubleAttribute(graph, fileNode, NsiliConstants.EXTENT, resSizeMB, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.EXTENT));
        }
      } catch (NumberFormatException nfe) {
        LOGGER.debug(
            "Couldn't convert the resource size to double: {}", metacard.getResourceSize());
      }
    }
  }

  private static void addCreatorAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node fileNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.CREATOR), resultAttributes)) {
      Attribute pocAttr = metacard.getAttribute(Contact.CREATOR_NAME);
      if (pocAttr != null) {
        String pocString = String.valueOf(pocAttr.getValue());
        if (StringUtils.isNotBlank(pocString)) {
          addStringAttribute(graph, fileNode, NsiliConstants.CREATOR, pocString, orb);
        } else {
          addStringAttribute(
              graph, fileNode, NsiliConstants.CREATOR, SystemInfo.getSiteName(), orb);
        }
      } else {
        addStringAttribute(graph, fileNode, NsiliConstants.CREATOR, SystemInfo.getSiteName(), orb);
      }
      addedAttributes.add(buildAttr(attribute, NsiliConstants.CREATOR));
    }
  }

  private static void addTimeDeclaredAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node fileNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.DATE_TIME_DECLARED), resultAttributes)) {
      if (metacard.getCreatedDate() != null) {
        addDateAttribute(
            graph, fileNode, NsiliConstants.DATE_TIME_DECLARED, metacard.getCreatedDate(), orb);
      } else {
        addDateAttribute(graph, fileNode, NsiliConstants.DATE_TIME_DECLARED, new Date(), orb);
      }
      addedAttributes.add(buildAttr(attribute, NsiliConstants.DATE_TIME_DECLARED));
    }
  }

  public static List<String> addSecurityNodeWithAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node productNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any any = orb.create_any();
    Node securityNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_SECURITY, any);
    graph.addVertex(securityNode);
    graph.addEdge(productNode, securityNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_SECURITY;

    boolean classificationAdded =
        isClassificationAdded(
            graph, metacard, orb, resultAttributes, addedAttributes, securityNode, attribute);

    addPolicyAttributes(
        graph, metacard, orb, resultAttributes, addedAttributes, securityNode, attribute);

    addReleasabilityAttributes(
        graph, metacard, orb, resultAttributes, addedAttributes, securityNode, attribute);

    addClassificationAttributes(
        graph, orb, addedAttributes, securityNode, attribute, classificationAdded);

    return addedAttributes;
  }

  private static void addClassificationAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      ORB orb,
      List<String> addedAttributes,
      Node securityNode,
      String attribute,
      boolean classificationAdded) {
    if (!classificationAdded) {
      // Add defaults from NSILI
      addStringAttribute(
          graph,
          securityNode,
          NsiliConstants.CLASSIFICATION,
          NsiliClassification.NO_CLASSIFICATION.getSpecName(),
          orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.CLASSIFICATION));

      addStringAttribute(graph, securityNode, NsiliConstants.POLICY, NsiliConstants.NATO, orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.POLICY));

      addStringAttribute(
          graph, securityNode, NsiliConstants.RELEASABILITY, NsiliConstants.NATO, orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.RELEASABILITY));
    }
  }

  private static void addPolicyAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node securityNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.POLICY), resultAttributes)) {
      Attribute metadataPolicyAttr = metacard.getAttribute(Security.METADATA_CLASSIFICATION_SYSTEM);
      String metadataPolicy = null;
      if (metadataPolicyAttr != null) {
        metadataPolicy = String.valueOf(metadataPolicyAttr.getValue());
      } else {
        Attribute policyAttr = metacard.getAttribute(Security.CLASSIFICATION_SYSTEM);
        if (policyAttr != null) {
          metadataPolicy = String.valueOf(policyAttr.getValue());
        }
      }

      if (metadataPolicy != null) {
        addStringAttribute(graph, securityNode, NsiliConstants.POLICY, metadataPolicy, orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.POLICY));
      }
    }
  }

  public static List<String> addMetadataSecurityNodeWithAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node productNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any any = orb.create_any();
    Node metadataSecurityNode =
        new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_METADATA_SECURITY, any);
    graph.addVertex(metadataSecurityNode);
    graph.addEdge(productNode, metadataSecurityNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_METADATA_SECURITY;

    boolean classificationAdded =
        isClassificationAdded(
            graph,
            metacard,
            orb,
            resultAttributes,
            addedAttributes,
            metadataSecurityNode,
            attribute);

    addPolicyAttributes(
        graph, metacard, orb, resultAttributes, addedAttributes, metadataSecurityNode, attribute);

    addReleasabilityAttributes(
        graph, metacard, orb, resultAttributes, addedAttributes, metadataSecurityNode, attribute);

    addClassificationAttributes(
        graph, orb, addedAttributes, metadataSecurityNode, attribute, classificationAdded);

    return addedAttributes;
  }

  private static void addReleasabilityAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node metadataSecurityNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.RELEASABILITY), resultAttributes)) {
      Attribute metadataReleasabilityAttr = metacard.getAttribute(Security.METADATA_RELEASABILITY);
      String metadataReleasability = null;
      if (metadataReleasabilityAttr != null) {
        metadataReleasability = String.valueOf(metadataReleasabilityAttr.getValue());
      } else {
        Attribute releasabilityAttr = metacard.getAttribute(Security.RELEASABILITY);
        if (releasabilityAttr != null) {
          metadataReleasability = String.valueOf(releasabilityAttr.getValue());
        }
      }

      if (metadataReleasability != null) {
        addStringAttribute(
            graph, metadataSecurityNode, NsiliConstants.RELEASABILITY, metadataReleasability, orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.RELEASABILITY));
      }
    }
  }

  private static boolean isClassificationAdded(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node metadataSecurityNode,
      String attribute) {
    Boolean classificationAdded = false;
    if (shouldAdd(buildAttr(attribute, NsiliConstants.CLASSIFICATION), resultAttributes)) {
      Attribute metadataClassificationAttr =
          metacard.getAttribute(Security.METADATA_CLASSIFICATION);
      String classification = null;
      if (metadataClassificationAttr != null) {
        classification = getClassification(metadataClassificationAttr.getValue());
      } else {
        Attribute classificationAttr = metacard.getAttribute(Security.CLASSIFICATION);
        if (classificationAttr != null) {
          classification = getClassification(classificationAttr.getValue());
        }
      }

      if (classification != null) {
        addStringAttribute(
            graph, metadataSecurityNode, NsiliConstants.CLASSIFICATION, classification, orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.CLASSIFICATION));
        classificationAdded = true;
      }
    }
    return classificationAdded;
  }

  public static List<String> addParts(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node productNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any any = orb.create_any();
    Node partNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_PART, any);
    graph.addVertex(partNode);
    graph.addEdge(productNode, partNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_PART;

    String type = NsiliProductType.DOCUMENT.getSpecName();

    String partIdentifier = "1";
    if (shouldAdd(buildAttr(attribute, NsiliConstants.PART_IDENTIFIER), resultAttributes)) {
      addStringAttribute(graph, partNode, NsiliConstants.PART_IDENTIFIER, partIdentifier, orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.PART_IDENTIFIER));
    }

    addedAttributes.addAll(
        addSecurityNodeWithAttributes(
            graph, partNode, metacard, orb, attribute + ":", resultAttributes));
    addedAttributes.addAll(
        addCoverageNodeWithAttributes(
            graph, partNode, metacard, orb, attribute + ":", resultAttributes));

    Attribute typeAttr = metacard.getAttribute(Core.DATATYPE);
    if (typeAttr != null) {
      type = getType(String.valueOf(typeAttr.getValue()));
    }

    if (type.equalsIgnoreCase(NsiliProductType.IMAGERY.getSpecName())) {
      addedAttributes.addAll(
          addImageryPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));
    } else if (type.equalsIgnoreCase(NsiliProductType.VIDEO.getSpecName())) {
      addedAttributes.addAll(
          addVideoPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));
    } else if (type.equalsIgnoreCase(NsiliProductType.TDL_DATA.getSpecName())) {
      addedAttributes.addAll(
          addTdlPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));
    } else if (type.equalsIgnoreCase(NsiliProductType.GMTI.getSpecName())) {
      addedAttributes.addAll(
          addGmtiPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));
    } else if (type.equalsIgnoreCase(NsiliProductType.REPORT.getSpecName())) {
      addedAttributes.addAll(
          addReportPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));
    } else if (type.equalsIgnoreCase(NsiliProductType.RFI.getSpecName())) {
      addedAttributes.addAll(
          addRfiPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));
    } else if (type.equalsIgnoreCase(NsiliProductType.TASK.getSpecName())) {
      addedAttributes.addAll(
          addTaskPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));
    }

    addedAttributes.addAll(
        addExploitationInfoPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));

    addedAttributes.addAll(
        addCbrnPart(graph, partNode, metacard, orb, attribute + ":", resultAttributes));

    addedAttributes.addAll(
        addCommonNodeWithAttributes(
            graph, partNode, metacard, type, orb, attribute + ":", resultAttributes));

    return addedAttributes;
  }

  public static List<String> addImageryPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any imageryAny = orb.create_any();
    Node imageryNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_IMAGERY, imageryAny);
    graph.addVertex(imageryNode);
    graph.addEdge(partNode, imageryNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_IMAGERY;

    addStrAttribute(
        graph,
        metacard.getAttribute(Core.TITLE),
        orb,
        resultAttributes,
        addedAttributes,
        imageryNode,
        attribute,
        NsiliConstants.TITLE);

    addIntAttribute(
        graph,
        metacard.getAttribute(Media.HEIGHT),
        orb,
        resultAttributes,
        addedAttributes,
        imageryNode,
        attribute,
        NsiliConstants.NUMBER_OF_ROWS);

    addIntAttribute(
        graph,
        metacard.getAttribute(Media.WIDTH),
        orb,
        resultAttributes,
        addedAttributes,
        imageryNode,
        attribute,
        NsiliConstants.NUMBER_OF_COLS);

    addDecompressionAttributes(
        graph, metacard, orb, resultAttributes, addedAttributes, imageryNode, attribute);

    addBandsAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, imageryNode, attribute);

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE),
        orb,
        resultAttributes,
        addedAttributes,
        imageryNode,
        attribute,
        NsiliConstants.NIIRS);

    addCategoryAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, imageryNode, attribute, true);

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.CLOUD_COVER),
        orb,
        resultAttributes,
        addedAttributes,
        imageryNode,
        attribute,
        NsiliConstants.CLOUD_COVER_PCT);

    addIdentifierAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, imageryNode, attribute);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.COMMENTS),
        orb,
        resultAttributes,
        addedAttributes,
        imageryNode,
        attribute,
        NsiliConstants.COMMENTS);

    return addedAttributes;
  }

  private static void addIdentifierAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node imageryNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER), resultAttributes)) {
      Attribute imageIdAttr = metacard.getAttribute(Isr.IMAGE_ID);
      if (imageIdAttr != null) {
        String imageId = String.valueOf(imageIdAttr.getValue());
        if (imageId != null) {
          imageId = imageId.substring(0, 10);
          addStringAttribute(graph, imageryNode, NsiliConstants.IDENTIFIER, imageId, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.IDENTIFIER));
        }
      } else {
        // Default to 10 characters of title or 10 characters of ID
        String identifier = metacard.getId().substring(0, 10);
        Attribute titleAttr = metacard.getAttribute(Core.TITLE);
        if (titleAttr != null) {
          String title = String.valueOf(titleAttr.getValue());
          identifier = title.substring(0, 10);
        }

        addStringAttribute(graph, imageryNode, NsiliConstants.IDENTIFIER, identifier, orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.IDENTIFIER));
      }
    }
  }

  private static void addCategoryAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node node,
      String attribute,
      Boolean visDefault) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.CATEGORY), resultAttributes)) {
      Attribute categoryAttr = metacard.getAttribute(Isr.CATEGORY);
      if (categoryAttr != null) {
        String categoryStr = String.valueOf(categoryAttr.getValue());
        if (categoryStr != null) {
          addStringAttribute(graph, node, NsiliConstants.CATEGORY, categoryStr, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.CATEGORY));
        }
      } else if (visDefault) {
        // Default to VIS if we don't know the category from data.
        addStringAttribute(
            graph, node, NsiliConstants.CATEGORY, NsiliImageryType.VIS.toString(), orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.CATEGORY));
      }
    }
  }

  private static void addBandsAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node imageryNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.NUMBER_OF_BANDS), resultAttributes)) {
      Attribute numBandsAttr = metacard.getAttribute(Media.NUMBER_OF_BANDS);
      if (numBandsAttr != null) {
        Integer numBands = getInteger(numBandsAttr.getValue());
        if (numBands != null) {
          addIntegerAttribute(graph, imageryNode, NsiliConstants.NUMBER_OF_BANDS, numBands, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.NUMBER_OF_BANDS));
        }
      } else {
        // Default to 0 if not set
        addIntegerAttribute(graph, imageryNode, NsiliConstants.NUMBER_OF_BANDS, 0, orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.NUMBER_OF_BANDS));
      }
    }
  }

  private static void addDecompressionAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node imageryNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.DECOMPRESSION_TECHNIQUE), resultAttributes)) {
      Attribute compressionAttr = metacard.getAttribute(Media.COMPRESSION);
      if (compressionAttr != null) {
        String compressionStr = String.valueOf(compressionAttr.getValue());
        if (compressionStr != null) {
          String compressionTechValue = getCompressionTechValue(compressionStr);
          if (compressionTechValue != null) {
            addStringAttribute(
                graph,
                imageryNode,
                NsiliConstants.DECOMPRESSION_TECHNIQUE,
                compressionTechValue,
                orb);
            addedAttributes.add(buildAttr(attribute, NsiliConstants.DECOMPRESSION_TECHNIQUE));
          }
        }
      } else {
        // Default to JPEG compression
        addStringAttribute(
            graph,
            imageryNode,
            NsiliConstants.DECOMPRESSION_TECHNIQUE,
            NsiliImageryDecompressionTech.C3.toString(),
            orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.DECOMPRESSION_TECHNIQUE));
      }
    }
  }

  public static List<String> addVideoPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any videoAny = orb.create_any();
    Node videoNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_VIDEO, videoAny);
    graph.addVertex(videoNode);
    graph.addEdge(partNode, videoNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_VIDEO;

    addIntAttribute(
        graph,
        metacard.getAttribute(Media.HEIGHT),
        orb,
        resultAttributes,
        addedAttributes,
        videoNode,
        attribute,
        NsiliConstants.NUMBER_OF_ROWS);

    addIntAttribute(
        graph,
        metacard.getAttribute(Media.WIDTH),
        orb,
        resultAttributes,
        addedAttributes,
        videoNode,
        attribute,
        NsiliConstants.NUMBER_OF_COLS);

    addEncodingSchemeAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, videoNode, attribute);

    addDblAttribute(
        graph,
        metacard.getAttribute(Media.BITS_PER_SECOND),
        orb,
        resultAttributes,
        addedAttributes,
        videoNode,
        attribute,
        NsiliConstants.AVG_BIT_RATE);

    addDblAttribute(
        graph,
        metacard.getAttribute(Media.FRAMES_PER_SECOND),
        orb,
        resultAttributes,
        addedAttributes,
        videoNode,
        attribute,
        NsiliConstants.FRAME_RATE);

    addStrAttribute(
        graph,
        metacard.getAttribute(Media.SCANNING_MODE),
        orb,
        resultAttributes,
        addedAttributes,
        videoNode,
        attribute,
        NsiliConstants.SCANNING_MODE);

    addVmtiAttributes(
        graph, metacard, orb, resultAttributes, addedAttributes, videoNode, attribute);

    addCategoryAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, videoNode, attribute, false);

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.VIDEO_MOTION_IMAGERY_SYSTEMS_MATRIX_LEVEL),
        orb,
        resultAttributes,
        addedAttributes,
        videoNode,
        attribute,
        NsiliConstants.MISM_LEVEL);

    return addedAttributes;
  }

  private static void addVmtiAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node videoNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.VMTI_PROCESSED), resultAttributes)) {
      Attribute vmtiProcessedAttr =
          metacard.getAttribute(Isr.VIDEO_MOVING_TARGET_INDICATOR_PROCESSED);
      if (vmtiProcessedAttr != null && vmtiProcessedAttr.getValue() instanceof Boolean) {
        Boolean vmtiProcessed = (Boolean) vmtiProcessedAttr.getValue();
        if (vmtiProcessed != null) {
          addBooleanAttribute(graph, videoNode, NsiliConstants.VMTI_PROCESSED, vmtiProcessed, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.VMTI_PROCESSED));
        }
      }
    }

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.TARGET_REPORT_COUNT),
        orb,
        resultAttributes,
        addedAttributes,
        videoNode,
        attribute,
        NsiliConstants.NUM_VMTI_TGT_REPORTS);
  }

  private static void addEncodingSchemeAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node videoNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.ENCODING_SCHEME), resultAttributes)) {
      Attribute encodingSchemeAttr = metacard.getAttribute(Media.ENCODING);
      if (encodingSchemeAttr != null) {
        String encodingScheme = getEncodingScheme(encodingSchemeAttr.getValue());
        if (encodingScheme != null) {
          addStringAttribute(graph, videoNode, NsiliConstants.ENCODING_SCHEME, encodingScheme, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.ENCODING_SCHEME));
        }
      }
    }
  }

  public static List<String> addTdlPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any tdlAny = orb.create_any();
    Node tdlNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_TDL, tdlAny);
    graph.addVertex(tdlNode);
    graph.addEdge(partNode, tdlNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_TDL;

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.TACTICAL_DATA_LINK_PLATFORM),
        orb,
        resultAttributes,
        addedAttributes,
        tdlNode,
        attribute,
        NsiliConstants.PLATFORM);

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.TACTICAL_DATA_LINK_ACTIVITY),
        orb,
        resultAttributes,
        addedAttributes,
        tdlNode,
        attribute,
        NsiliConstants.ACTIVITY);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.TACTICAL_DATA_LINK_MESSAGE_NUMBER),
        orb,
        resultAttributes,
        addedAttributes,
        tdlNode,
        attribute,
        NsiliConstants.MESSAGE_NUM);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.TACTICAL_DATA_LINK_TRACK_NUMBER),
        orb,
        resultAttributes,
        addedAttributes,
        tdlNode,
        attribute,
        NsiliConstants.TRACK_NUM);

    return addedAttributes;
  }

  public static List<String> addGmtiPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any gmtiAny = orb.create_any();
    Node gmtiNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_GMTI, gmtiAny);
    graph.addVertex(gmtiNode);
    graph.addEdge(partNode, gmtiNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_GMTI;

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.MOVING_TARGET_INDICATOR_JOB_ID),
        orb,
        resultAttributes,
        addedAttributes,
        gmtiNode,
        attribute,
        NsiliConstants.IDENTIFIER_JOB);

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.TARGET_REPORT_COUNT),
        orb,
        resultAttributes,
        addedAttributes,
        gmtiNode,
        attribute,
        NsiliConstants.NUMBER_OF_TARGET_REPORTS);

    return addedAttributes;
  }

  public static List<String> addReportPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any reportAny = orb.create_any();
    Node reportNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_REPORT, reportAny);
    graph.addVertex(reportNode);
    graph.addEdge(partNode, reportNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_REPORT;

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.REPORT_SERIAL_NUMBER),
        orb,
        resultAttributes,
        addedAttributes,
        reportNode,
        attribute,
        NsiliConstants.ORIGINATORS_REQ_SERIAL_NUM);

    addTypeAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, reportNode, attribute);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.REPORT_INFO_RATING),
        orb,
        resultAttributes,
        addedAttributes,
        reportNode,
        attribute,
        NsiliConstants.INFORMATION_RATING);

    addPriorityAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, reportNode, attribute);

    addedAttributes.addAll(
        addIntRepPart(graph, reportNode, metacard, orb, attribute + ":", resultAttributes));

    addedAttributes.addAll(
        addEntityPart(graph, reportNode, metacard, orb, attribute + ":", resultAttributes));

    return addedAttributes;
  }

  private static void addPriorityAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node reportNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.PRIORITY), resultAttributes)) {
      Attribute priorityAttr = metacard.getAttribute(Isr.REPORT_PRIORITY);
      if (priorityAttr != null) {
        String priority = getReportPriority(priorityAttr.getValue());
        if (priority != null) {
          addStringAttribute(graph, reportNode, NsiliConstants.PRIORITY, priority, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.PRIORITY));
        }
      }
    }
  }

  private static void addTypeAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node reportNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.TYPE), resultAttributes)) {
      Attribute reportTypeAttr = metacard.getAttribute(Isr.REPORT_TYPE);
      if (reportTypeAttr != null) {
        String reportType = getReportType(reportTypeAttr.getValue());
        if (reportType != null) {
          addStringAttribute(graph, reportNode, NsiliConstants.TYPE, reportType, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.TYPE));
        }
      }
    }
  }

  private static void addDblAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Attribute attr,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node node,
      String attribute,
      String entry) {
    if (shouldAdd(buildAttr(attribute, entry), resultAttributes) && attr != null) {
      Double attrValue = getDouble(attr.getValue());
      if (attrValue != null) {
        addDoubleAttribute(graph, node, entry, attrValue, orb);
        addedAttributes.add(buildAttr(attribute, entry));
      }
    }
  }

  private static void addIntAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Attribute attr,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node node,
      String attribute,
      String entry) {
    if (shouldAdd(buildAttr(attribute, entry), resultAttributes) && attr != null) {
      Integer attrValue = getInteger(attr.getValue());
      if (attrValue != null) {
        addIntegerAttribute(graph, node, entry, attrValue, orb);
        addedAttributes.add(buildAttr(attribute, entry));
      }
    }
  }

  private static void addStrAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Attribute attr,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node node,
      String attribute,
      String entry) {
    if (shouldAdd(buildAttr(attribute, entry), resultAttributes) && attr != null) {
      String attrValue = String.valueOf(attr.getValue());
      if (attrValue != null) {
        addStringAttribute(graph, node, entry, attrValue, orb);
        addedAttributes.add(buildAttr(attribute, entry));
      }
    }
  }

  private static void addValStrAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Attribute attr,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node node,
      String attribute,
      String entry) {
    if (shouldAdd(buildAttr(attribute, entry), resultAttributes) && attr != null) {
      String attrValue = getValueString(attr.getValues());
      if (attrValue != null) {
        addStringAttribute(graph, node, entry, attrValue, orb);
        addedAttributes.add(buildAttr(attribute, entry));
      }
    }
  }

  public static List<String> addRfiPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any rfiAny = orb.create_any();
    Node rfiNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_RFI, rfiAny);
    graph.addVertex(rfiNode);
    graph.addEdge(partNode, rfiNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_RFI;

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.REQUEST_FOR_INFORMATION_FOR_ACTION),
        orb,
        resultAttributes,
        addedAttributes,
        rfiNode,
        attribute,
        NsiliConstants.FOR_ACTION);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.REQUEST_FOR_INFORMATION_FOR_INFORMATION),
        orb,
        resultAttributes,
        addedAttributes,
        rfiNode,
        attribute,
        NsiliConstants.FOR_INFORMATION);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.REQUEST_FOR_INFORMATION_SERIAL_NUMBER),
        orb,
        resultAttributes,
        addedAttributes,
        rfiNode,
        attribute,
        NsiliConstants.SERIAL_NUMBER);

    addStatusAttribute(graph, metacard, orb, resultAttributes, addedAttributes, rfiNode, attribute);

    addWorkflowStatusAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, rfiNode, attribute);

    return addedAttributes;
  }

  private static void addWorkflowStatusAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node rfiNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.WORKFLOW_STATUS), resultAttributes)) {
      Attribute rfiWorkflowStatusAttr =
          metacard.getAttribute(Isr.REQUEST_FOR_INFORMATION_WORKFLOW_STATUS);
      if (rfiWorkflowStatusAttr != null) {
        String rfiWorkflowStatus = getRfiWorkflowStatus(rfiWorkflowStatusAttr.getValue());
        if (rfiWorkflowStatus != null) {
          addStringAttribute(
              graph, rfiNode, NsiliConstants.WORKFLOW_STATUS, rfiWorkflowStatus, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.WORKFLOW_STATUS));
        }
      }
    }
  }

  private static void addStatusAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node rfiNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.STATUS), resultAttributes)) {
      Attribute rfiStatusAttr = metacard.getAttribute(Isr.REQUEST_FOR_INFORMATION_STATUS);
      if (rfiStatusAttr != null) {
        String rfiStatus = getRfiStatus(rfiStatusAttr.getValue());
        if (rfiStatus != null) {
          addStringAttribute(graph, rfiNode, NsiliConstants.STATUS, rfiStatus, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.STATUS));
        }
      }
    }
  }

  public static List<String> addTaskPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any taskAny = orb.create_any();
    Node taskNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_TASK, taskAny);
    graph.addVertex(taskNode);
    graph.addEdge(partNode, taskNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_TASK;

    addValStrAttribute(
        graph,
        metacard.getAttribute(Isr.TASK_COMMENTS),
        orb,
        resultAttributes,
        addedAttributes,
        taskNode,
        attribute,
        NsiliConstants.COMMENTS);

    if (shouldAdd(buildAttr(attribute, NsiliConstants.STATUS), resultAttributes)) {
      Attribute taskStatusAttr = metacard.getAttribute(Isr.TASK_STATUS);
      if (taskStatusAttr != null) {
        String taskStatus = getTaskStatus(taskStatusAttr.getValue());
        if (taskStatus != null) {
          addStringAttribute(graph, taskNode, NsiliConstants.STATUS, taskStatus, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.STATUS));
        }
      }
    }

    return addedAttributes;
  }

  public static List<String> addCbrnPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any cbrnAny = orb.create_any();
    Node cbrnNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_CBRN, cbrnAny);
    graph.addVertex(cbrnNode);
    graph.addEdge(partNode, cbrnNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_CBRN;

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_OPERATION_NAME),
        orb,
        resultAttributes,
        addedAttributes,
        cbrnNode,
        attribute,
        NsiliConstants.OPERATION_NAME);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_INCIDENT_NUMBER),
        orb,
        resultAttributes,
        addedAttributes,
        cbrnNode,
        attribute,
        NsiliConstants.INCIDENT_NUM);

    addEventTypeAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, cbrnNode, attribute);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_CATEGORY),
        orb,
        resultAttributes,
        addedAttributes,
        cbrnNode,
        attribute,
        NsiliConstants.CBRN_CATEGORY);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_SUBSTANCE),
        orb,
        resultAttributes,
        addedAttributes,
        cbrnNode,
        attribute,
        NsiliConstants.SUBSTANCE);

    addAlarmClassifiationAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, cbrnNode, attribute);

    if (addedAttributes.isEmpty()) {
      graph.removeEdge(partNode, cbrnNode);
      graph.removeVertex(cbrnNode);
    }

    return addedAttributes;
  }

  private static void addAlarmClassifiationAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node cbrnNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.ALARM_CLASSIFICATION), resultAttributes)) {
      Attribute alarmClassAttr =
          metacard.getAttribute(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_ALARM_CLASSIFICATION);
      if (alarmClassAttr != null) {
        String alarmClass = getCbrnAlarmClassification(alarmClassAttr.getValue());
        if (alarmClass != null) {
          addStringAttribute(graph, cbrnNode, NsiliConstants.ALARM_CLASSIFICATION, alarmClass, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.ALARM_CLASSIFICATION));
        }
      }
    }
  }

  private static void addEventTypeAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node cbrnNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.EVENT_TYPE), resultAttributes)) {
      Attribute eventTypeAttr =
          metacard.getAttribute(Isr.CHEMICAL_BIOLOGICAL_RADIOLOGICAL_NUCLEAR_TYPE);
      if (eventTypeAttr != null) {
        String eventType = getCbrnEventType(eventTypeAttr.getValue());
        if (eventType != null) {
          addStringAttribute(graph, cbrnNode, NsiliConstants.EVENT_TYPE, eventType, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.EVENT_TYPE));
        }
      }
    }
  }

  public static List<String> addIntRepPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any intRepAny = orb.create_any();
    Node intRepNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_INTREP, intRepAny);
    graph.addVertex(intRepNode);
    graph.addEdge(partNode, intRepNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_INTREP;

    if (shouldAdd(buildAttr(attribute, NsiliConstants.SITUATION_TYPE), resultAttributes)) {
      Attribute situationTypeAttr = metacard.getAttribute(Isr.REPORT_SITUATION_TYPE);
      if (situationTypeAttr != null) {
        String situationType = getIntRepSituationType(situationTypeAttr.getValue());
        if (situationType != null) {
          addStringAttribute(graph, intRepNode, NsiliConstants.SITUATION_TYPE, situationType, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.SITUATION_TYPE));
        }
      }
    }

    return addedAttributes;
  }

  public static List<String> addEntityPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any entityAny = orb.create_any();
    Node entityPartNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_ENTITY, entityAny);
    graph.addVertex(entityPartNode);
    graph.addEdge(partNode, entityPartNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_ENTITY;

    if (shouldAdd(buildAttr(attribute, NsiliConstants.TYPE), resultAttributes)) {
      Attribute situationTypeAttr = metacard.getAttribute(Isr.REPORT_ENTITY_TYPE);
      if (situationTypeAttr != null) {
        String situationType = getEntityType(situationTypeAttr.getValue());
        if (situationType != null) {
          addStringAttribute(graph, entityPartNode, NsiliConstants.TYPE, situationType, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.TYPE));
        }
      }
    }

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.REPORT_ENTITY_NAME),
        orb,
        resultAttributes,
        addedAttributes,
        entityPartNode,
        attribute,
        NsiliConstants.NAME);

    addValStrAttribute(
        graph,
        metacard.getAttribute(Isr.REPORT_ENTITY_ALIAS),
        orb,
        resultAttributes,
        addedAttributes,
        entityPartNode,
        attribute,
        NsiliConstants.ALIAS);

    return addedAttributes;
  }

  public static List<String> addExploitationInfoPart(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any exploitationInfoAny = orb.create_any();
    Node exploitationInfoNode =
        new Node(
            0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_EXPLOITATION_INFO, exploitationInfoAny);
    graph.addVertex(exploitationInfoNode);
    graph.addEdge(partNode, exploitationInfoNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_EXPLOITATION_INFO;

    addIntAttribute(
        graph,
        metacard.getAttribute(Isr.EXPLOITATION_LEVEL),
        orb,
        resultAttributes,
        addedAttributes,
        exploitationInfoNode,
        attribute,
        NsiliConstants.LEVEL);

    addAutoGeneratedAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, exploitationInfoNode, attribute);

    addSubjQualityAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, exploitationInfoNode, attribute);

    if (addedAttributes.isEmpty()) {
      graph.removeEdge(partNode, exploitationInfoNode);
      graph.removeVertex(exploitationInfoNode);
    }

    return addedAttributes;
  }

  private static void addSubjQualityAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node exploitationInfoNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.SUBJ_QUALITY_CODE), resultAttributes)) {
      Attribute subQualCodeAttr = metacard.getAttribute(Isr.EXPLOITATION_SUBJECTIVE_QUALITY_CODE);
      if (subQualCodeAttr != null) {
        String subQualCodeStr = getSubjectiveQualityCode(subQualCodeAttr.getValue());
        if (subQualCodeStr != null) {
          addStringAttribute(
              graph, exploitationInfoNode, NsiliConstants.SUBJ_QUALITY_CODE, subQualCodeStr, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.SUBJ_QUALITY_CODE));
        }
      }
    }
  }

  private static void addAutoGeneratedAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      Node exploitationInfoNode,
      String attribute) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.AUTO_GENERATED), resultAttributes)) {
      Attribute autoGenAttr = metacard.getAttribute(Isr.EXPLOTATION_AUTO_GENERATED);
      if (autoGenAttr != null && autoGenAttr.getValue() instanceof Boolean) {
        Boolean autoGen = (Boolean) autoGenAttr.getValue();
        if (autoGen != null) {
          addBooleanAttribute(
              graph, exploitationInfoNode, NsiliConstants.AUTO_GENERATED, autoGen, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.AUTO_GENERATED));
        }
      }
    }
  }

  public static List<String> addCommonNodeWithAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      String type,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any any = orb.create_any();
    Node commonNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_COMMON, any);
    graph.addVertex(commonNode);
    graph.addEdge(partNode, commonNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_COMMON;

    if (shouldAdd(buildAttr(attribute, NsiliConstants.IDENTIFIER_UUID), resultAttributes)) {
      String metacardId = getMetacardId(metacard);
      if (metacardId != null) {
        UUID uuid = getUUIDFromCard(metacardId);
        addStringAttribute(graph, commonNode, NsiliConstants.IDENTIFIER_UUID, uuid.toString(), orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.IDENTIFIER_UUID));
      }
    }

    if (shouldAdd(buildAttr(attribute, NsiliConstants.TYPE), resultAttributes) && type != null) {
      addStringAttribute(graph, commonNode, NsiliConstants.TYPE, type, orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.TYPE));
    }

    addValStrAttribute(
        graph,
        metacard.getAttribute(Core.DESCRIPTION),
        orb,
        resultAttributes,
        addedAttributes,
        commonNode,
        attribute,
        NsiliConstants.DESCRIPTION_ABSTRACT);

    addValStrAttribute(
        graph,
        metacard.getAttribute(Core.LANGUAGE),
        orb,
        resultAttributes,
        addedAttributes,
        commonNode,
        attribute,
        NsiliConstants.LANGUAGE);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.TARGET_ID),
        orb,
        resultAttributes,
        addedAttributes,
        commonNode,
        attribute,
        NsiliConstants.TARGET_NUMBER);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.TARGET_CATEGORY_CODE),
        orb,
        resultAttributes,
        addedAttributes,
        commonNode,
        attribute,
        NsiliConstants.SUBJECT_CATEGORY_TARGET);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.ORIGINAL_SOURCE),
        orb,
        resultAttributes,
        addedAttributes,
        commonNode,
        attribute,
        NsiliConstants.SOURCE);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.MISSION_ID),
        orb,
        resultAttributes,
        addedAttributes,
        commonNode,
        attribute,
        NsiliConstants.IDENTIFIER_MISSION);

    addStrAttribute(
        graph,
        metacard.getAttribute(Isr.JC3IEDM_ID),
        orb,
        resultAttributes,
        addedAttributes,
        commonNode,
        attribute,
        NsiliConstants.IDENTIFIER_JC3IEDM);

    return addedAttributes;
  }

  public static List<String> addCoverageNodeWithAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node partNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any any = orb.create_any();

    String attribute = parentAttrName + NsiliConstants.NSIL_COVERAGE;

    Node coverageNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_COVERAGE, any);

    if (metacardContainsGeoInfo(metacard)) {
      graph.addVertex(coverageNode);
      graph.addEdge(partNode, coverageNode);

      addStrAttribute(
          graph,
          metacard.getAttribute(Core.LOCATION),
          orb,
          resultAttributes,
          addedAttributes,
          coverageNode,
          attribute,
          NsiliConstants.ADVANCED_GEOSPATIAL);

      addSpatialGeoRefBoxAttribute(
          graph, metacard, orb, resultAttributes, addedAttributes, attribute, coverageNode);
    }

    addTemporalAttributes(
        graph, metacard, orb, resultAttributes, addedAttributes, attribute, coverageNode);

    addValStrAttribute(
        graph,
        metacard.getAttribute(Location.COUNTRY_CODE),
        orb,
        resultAttributes,
        addedAttributes,
        coverageNode,
        attribute,
        NsiliConstants.SPATIAL_COUNTRY_CODE);

    return addedAttributes;
  }

  private static void addTemporalAttributes(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      String attribute,
      Node coverageNode) {
    if (shouldAdd(buildAttr(attribute, NsiliConstants.TEMPORAL_END), resultAttributes)) {
      Attribute endAttr = metacard.getAttribute(DateTime.END);
      if (endAttr != null) {
        Date endDate = (Date) endAttr.getValue();
        if (endDate != null) {
          addDateAttribute(graph, coverageNode, NsiliConstants.TEMPORAL_END, endDate, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.TEMPORAL_END));
        }
      }
    }

    if (shouldAdd(buildAttr(attribute, NsiliConstants.TEMPORAL_START), resultAttributes)) {
      Attribute startAttr = metacard.getAttribute(DateTime.START);
      if (startAttr != null) {
        Date startDate = (Date) startAttr.getValue();
        if (startDate != null) {
          addDateAttribute(graph, coverageNode, NsiliConstants.TEMPORAL_START, startDate, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.TEMPORAL_START));
        }
      }
    }
  }

  private static void addSpatialGeoRefBoxAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Metacard metacard,
      ORB orb,
      List<String> resultAttributes,
      List<String> addedAttributes,
      String attribute,
      Node coverageNode) {
    if (shouldAdd(
        buildAttr(attribute, NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX), resultAttributes)) {
      Attribute geoAttr = metacard.getAttribute(Core.LOCATION);
      if (geoAttr != null) {
        String wktGeo = String.valueOf(geoAttr.getValue());
        try {
          Geometry boundingGeo = WKTUtil.getWKTBoundingRectangle(wktGeo);
          Rectangle rect = NsiliGeomUtil.getRectangle(boundingGeo);
          addGeomAttribute(
              graph, coverageNode, NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX, rect, orb);
          addedAttributes.add(buildAttr(attribute, NsiliConstants.SPATIAL_GEOGRAPHIC_REF_BOX));
        } catch (ParseException pe) {
          LOGGER.debug("Unable to parse WKT for bounding box: {}", wktGeo, pe);
        }
      }
    }
  }

  public static List<String> addThumbnailRelatedFile(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node productNode,
      Metacard metacard,
      ORB orb,
      String parentAttrName,
      List<String> resultAttributes) {
    List<String> addedAttributes = new ArrayList<>();
    Any any = orb.create_any();
    Node relatedFileNode = new Node(0, NodeType.ENTITY_NODE, NsiliConstants.NSIL_RELATED_FILE, any);
    graph.addVertex(relatedFileNode);
    graph.addEdge(productNode, relatedFileNode);

    String attribute = parentAttrName + NsiliConstants.NSIL_RELATED_FILE;

    addCreatorAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, relatedFileNode, attribute);

    addTimeDeclaredAttribute(
        graph, metacard, orb, resultAttributes, addedAttributes, relatedFileNode, attribute);

    if (shouldAdd(buildAttr(attribute, NsiliConstants.EXTENT), resultAttributes)
        && metacard.getThumbnail() != null) {
      try {
        Double resSize = (double) metacard.getThumbnail().length;
        Double resSizeMB = convertToMegabytes(resSize);
        addDoubleAttribute(graph, relatedFileNode, NsiliConstants.EXTENT, resSizeMB, orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.EXTENT));
      } catch (NumberFormatException nfe) {
        LOGGER.debug(
            "Couldn't convert the thumbnail size to double: {}", metacard.getResourceSize());
      }
    }

    if (shouldAdd(buildAttr(attribute, NsiliConstants.URL), resultAttributes)) {
      try {
        String thumbnailURL =
            new URI(
                    SystemBaseUrl.constructUrl(
                        CATALOG_SOURCE_PATH
                            + "/"
                            + metacard.getSourceId()
                            + "/"
                            + metacard.getId()
                            + "?transform="
                            + THUMBNAIL_TRANSFORMER,
                        true))
                .toASCIIString();
        addStringAttribute(graph, relatedFileNode, NsiliConstants.URL, thumbnailURL, orb);
        addedAttributes.add(buildAttr(attribute, NsiliConstants.URL));
      } catch (URISyntaxException e) {
        LOGGER.debug("Unable to construct URI: ", e);
      }
    }

    if (shouldAdd(buildAttr(attribute, NsiliConstants.IS_FILE_LOCAL), resultAttributes)) {
      String siteName = SystemInfo.getSiteName();

      boolean fileLocal = true;
      if (siteName != null
          && metacard.getSourceId() != null
          && !siteName.equals(metacard.getSourceId())) {
        fileLocal = false;
      }
      addBooleanAttribute(graph, relatedFileNode, NsiliConstants.IS_FILE_LOCAL, fileLocal, orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.IS_FILE_LOCAL));
    }

    if (shouldAdd(buildAttr(attribute, NsiliConstants.FILE_TYPE), resultAttributes)) {
      addStringAttribute(
          graph, relatedFileNode, NsiliConstants.FILE_TYPE, NsiliConstants.THUMBNAIL_TYPE, orb);
      addedAttributes.add(buildAttr(attribute, NsiliConstants.FILE_TYPE));
    }

    return addedAttributes;
  }

  public static Node createRootNode(ORB orb) {
    return new Node(0, NodeType.ROOT_NODE, NsiliConstants.NSIL_PRODUCT, orb.create_any());
  }

  public static void addStringAttribute(
      DirectedAcyclicGraph<Node, Edge> graph, Node parentNode, String key, String value, ORB orb) {
    Any any = orb.create_any();
    any.insert_string(value);
    Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
    graph.addVertex(node);
    graph.addEdge(parentNode, node);
  }

  public static void addIntegerAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node parentNode,
      String key,
      Integer integer,
      ORB orb) {
    Any any = orb.create_any();
    any.insert_ulong(integer);
    Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
    graph.addVertex(node);
    graph.addEdge(parentNode, node);
  }

  public static void addShortAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node parentNode,
      String key,
      Short shortVal,
      ORB orb) {
    Any any = orb.create_any();
    any.insert_short(shortVal);
    Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
    graph.addVertex(node);
    graph.addEdge(parentNode, node);
  }

  public static void addDoubleAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node parentNode,
      String key,
      Double doubleVal,
      ORB orb) {
    Any any = orb.create_any();
    any.insert_double(doubleVal);
    Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
    graph.addVertex(node);
    graph.addEdge(parentNode, node);
  }

  public static void addBooleanAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node parentNode,
      String key,
      Boolean boolVal,
      ORB orb) {
    Any any = orb.create_any();
    any.insert_boolean(boolVal);
    Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
    graph.addVertex(node);
    graph.addEdge(parentNode, node);
  }

  public static void addAnyAttribute(
      DirectedAcyclicGraph<Node, Edge> graph, Node parentNode, String key, Any any, ORB orb) {
    Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
    graph.addVertex(node);
    graph.addEdge(parentNode, node);
  }

  public static void addDateAttribute(
      DirectedAcyclicGraph<Node, Edge> graph, Node parentNode, String key, Date date, ORB orb) {
    Any any = orb.create_any();
    AbsTimeHelper.insert(any, getAbsTime(date));
    Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
    graph.addVertex(node);
    graph.addEdge(parentNode, node);
  }

  public static void addGeomAttribute(
      DirectedAcyclicGraph<Node, Edge> graph,
      Node parentNode,
      String key,
      Rectangle rectangle,
      ORB orb) {
    if (rectangle != null) {
      Any any = orb.create_any();
      RectangleHelper.insert(any, rectangle);
      Node node = new Node(0, NodeType.ATTRIBUTE_NODE, key, any);
      graph.addVertex(node);
      graph.addEdge(parentNode, node);
    }
  }

  public static Double convertToMegabytes(Double resSizeBytes) {
    if (resSizeBytes != null) {
      return resSizeBytes / (1024 * 1024);
    } else {
      return null;
    }
  }

  public static AbsTime getAbsTime(Date date) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(date);

    return new AbsTime(
        new org.codice.alliance.nsili.common.UCO.Date(
            (short) cal.get(Calendar.YEAR),
            (short) (cal.get(Calendar.MONTH) + 1),
            (short) cal.get(Calendar.DAY_OF_MONTH)),
        new Time(
            (short) cal.get(Calendar.HOUR_OF_DAY),
            (short) cal.get(Calendar.MINUTE),
            (short) cal.get(Calendar.SECOND)));
  }

  public static Map<Integer, Node> createNodeMap(Node[] nodes) {
    Map<Integer, Node> nodeMap = new HashMap<>();
    for (Node node : nodes) {
      nodeMap.put(node.id, node);
    }

    return nodeMap;
  }

  public static List<String> getAttributes(DAG dag) {
    return new ArrayList<>(getAttributeMap(dag).keySet());
  }

  public static Map<String, String> getAttributeMap(DAG dag) {
    Map<String, String> attributes = new HashMap<>();

    Map<Integer, Node> nodeMap = createNodeMap(dag.nodes);
    DirectedAcyclicGraph<Node, Edge> graph = getNodeEdgeDirectedAcyclicGraph(dag, nodeMap);

    DepthFirstIterator<Node, Edge> graphIT = new DepthFirstIterator<>(graph, nodeMap.get(0));
    List<String> nodeStack = new ArrayList<>();

    graphIT.addTraversalListener(
        new TraversalListener<Node, Edge>() {
          @Override
          public void connectedComponentFinished(
              ConnectedComponentTraversalEvent connectedComponentTraversalEvent) {
            // This method is not expected to be called
          }

          @Override
          public void connectedComponentStarted(
              ConnectedComponentTraversalEvent connectedComponentTraversalEvent) {
            // This method is not expected to be called
          }

          @Override
          public void edgeTraversed(EdgeTraversalEvent<Node, Edge> edgeTraversalEvent) {
            // This method is not expected to be called
          }

          @Override
          public void vertexTraversed(VertexTraversalEvent<Node> vertexTraversalEvent) {
            Node node = vertexTraversalEvent.getVertex();
            if (node.node_type != NodeType.ATTRIBUTE_NODE) {
              nodeStack.add(node.attribute_name);
            }
          }

          @Override
          public void vertexFinished(VertexTraversalEvent<Node> vertexTraversalEvent) {
            Node node = vertexTraversalEvent.getVertex();
            if (node.node_type == NodeType.ATTRIBUTE_NODE) {
              String attribute = "";
              int currEntry = 0;
              int size = nodeStack.size();
              for (String nodeEntry : nodeStack) {
                attribute += nodeEntry;
                if (currEntry < (size - 1)) {
                  attribute += ":";
                } else {
                  attribute += ".";
                }
                currEntry++;
              }
              attribute += node.attribute_name;
              attributes.put(attribute, CorbaUtils.getNodeValue(node.value));
            } else {
              int lastIdx = nodeStack.size() - 1;
              nodeStack.remove(lastIdx);
            }
          }
        });

    Node rootNode = null;
    while (graphIT.hasNext()) {
      graphIT.setCrossComponentTraversal(false);

      Node node = graphIT.next();
      if (rootNode == null) {
        rootNode = node;
      }
    }

    return attributes;
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

  private static boolean metacardContainsGeoInfo(Metacard metacard) {
    boolean foundGeoInfo = false;

    if (metacard.getAttribute(Core.LOCATION) != null) {
      foundGeoInfo = true;
    }

    return foundGeoInfo;
  }

  private static String modifyUrl(String url, String name) {
    return url + "&nsiliFilename=" + name;
  }

  private static UUID getUUIDFromCard(String id) {
    UUID uuid = null;
    try {
      if (id.contains("-")) {
        uuid = UUID.fromString(id);
      } else if (id.length() == 32) {
        // Attempt to parse as a UUID with no dashes
        StringBuilder sb = new StringBuilder(id);
        sb.insert(8, "-");
        sb.insert(13, "-");
        sb.insert(18, "-");
        sb.insert(23, "-");
        uuid = UUID.fromString(sb.toString());
      }
    } catch (Exception e) {
    }

    // If parsing fails, get a v3 UUID from the bytes of the metacard ID
    if (uuid == null) {
      uuid = UUID.nameUUIDFromBytes(id.getBytes());
    }

    return uuid;
  }

  private static boolean shouldAdd(String attributeName, List<String> resultAttributes) {
    boolean shouldAddAttribute = false;

    if (resultAttributes != null && !resultAttributes.isEmpty()) {
      if (resultAttributes.contains(attributeName)) {
        shouldAddAttribute = true;
      } else {
        if (attributeName.lastIndexOf(':') != -1) {
          String nonScopeAttr = attributeName.substring(attributeName.lastIndexOf(':') + 1);
          if (resultAttributes.contains(nonScopeAttr)) {
            shouldAddAttribute = true;
          }
        }

        if (!shouldAddAttribute) {
          int lastDot = attributeName.lastIndexOf('.');
          if (lastDot != -1) {
            String simpleAttrName = attributeName.substring(lastDot + 1);
            if (resultAttributes.contains(simpleAttrName)) {
              shouldAddAttribute = true;
            }
          }
        }
      }
    } else {
      shouldAddAttribute = true;
    }

    if (!shouldAddAttribute) {
      LOGGER.trace("Attribute is not supported in destination data model: {}", attributeName);
    }

    return shouldAddAttribute;
  }

  private static String buildAttr(String parentAttr, String attribute) {
    return parentAttr + "." + attribute;
  }

  private static Map<String, List<String>> getAttrMap(List<String> attributes) {
    return attributes
        .stream()
        .map(ATTRIBUTE_PATTERN::matcher)
        .filter(Matcher::matches)
        .collect(
            Collectors.groupingBy(
                m -> m.group(2), Collectors.mapping(m -> m.group(3), Collectors.toList())));
  }

  private static boolean processEntry(
      String entryName, List<String> requiredAttrs, List<String> parsedAttrs) {
    final ThreadLocal<Boolean> dataIsValid = new ThreadLocal<>();
    dataIsValid.set(true);

    if (requiredAttrs != null) {
      List<String> missingAttrs =
          requiredAttrs
              .stream()
              .filter(requiredAttr -> !parsedAttrs.contains(requiredAttr))
              .collect(Collectors.toList());
      if (!missingAttrs.isEmpty()) {
        dataIsValid.set(false);
        LOGGER.debug("Node: {} is missing attributes: {}", entryName, missingAttrs);
      }
    }

    return dataIsValid.get();
  }

  public static String getMetacardId(Metacard metacard) {
    String id = metacard.getId();

    Attribute origIdAttr = metacard.getAttribute(MetacardVersion.VERSION_OF_ID);
    if (origIdAttr != null && origIdAttr.getValue().toString() != null) {
      id = origIdAttr.getValue().toString();
    }

    return id;
  }

  public static String getValueString(Collection<Serializable> collection) {
    return collection.stream().map(Object::toString).collect(Collectors.joining(", "));
  }

  private static String getType(String metacardType) {
    String type = NsiliProductType.DOCUMENT.getSpecName();

    String lowerType = metacardType.toLowerCase();
    for (NsiliProductType productTypeValue : NsiliProductType.values()) {
      if (productTypeValue.getSpecName().equalsIgnoreCase(lowerType)) {
        type = productTypeValue.getSpecName();
        break;
      }
    }

    return type;
  }

  private static String getCompressionTechValue(String mediaCompressionType) {
    String compressionType = null;
    if (mediaCompressionType != null) {
      for (NsiliImageryDecompressionTech decompressionTechValue :
          NsiliImageryDecompressionTech.values()) {
        if (decompressionTechValue.name().equalsIgnoreCase(mediaCompressionType)) {
          compressionType = decompressionTechValue.name();
          break;
        }
      }

      if (compressionType != null) {
        return compressionType;
      }
      if (mediaCompressionType.equalsIgnoreCase("JPEG")
          || mediaCompressionType.equalsIgnoreCase("6")
          || mediaCompressionType.equalsIgnoreCase("7")
          || mediaCompressionType.equalsIgnoreCase("99")) {
        compressionType = NsiliImageryDecompressionTech.C3.name();
      } else if (mediaCompressionType.equalsIgnoreCase("Uncompressed")
          || mediaCompressionType.equalsIgnoreCase("1")) {
        compressionType = NsiliImageryDecompressionTech.NC.name();
      } else if (mediaCompressionType.equalsIgnoreCase("JPEG 2000")
          || mediaCompressionType.equalsIgnoreCase("34712")) {
        compressionType = NsiliImageryDecompressionTech.C8.name();
      }
    }

    return compressionType;
  }

  private static Integer getInteger(Serializable value) {
    Integer integer = null;

    if (value instanceof Integer) {
      integer = (Integer) value;
    }

    return integer;
  }

  private static Double getDouble(Serializable value) {
    Double doubleVal = null;

    if (value instanceof Double) {
      doubleVal = (Double) value;
    }

    return doubleVal;
  }

  private static String getEncodingScheme(Serializable encodingSchemeValue) {
    String encodingScheme = null;
    String encodingSchemeValueStr = String.valueOf(encodingSchemeValue);

    if (encodingSchemeValueStr != null) {
      for (NsiliVideoEncodingScheme encodingValue : NsiliVideoEncodingScheme.values()) {
        if (encodingValue.getSpecName().equalsIgnoreCase(encodingSchemeValueStr)) {
          encodingScheme = encodingValue.getSpecName();
        }
      }
    }

    return encodingScheme;
  }

  private static String getSubjectiveQualityCode(Serializable value) {
    String subjectiveQualityCode = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliExploitationSubQualCode qualCodeValue : NsiliExploitationSubQualCode.values()) {
        if (qualCodeValue.name().equalsIgnoreCase(valueStr)) {
          subjectiveQualityCode = qualCodeValue.name();
        }
      }
    }

    return subjectiveQualityCode;
  }

  private static String getReportType(Serializable value) {
    String reportType = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliReportType reportTypeValue : NsiliReportType.values()) {
        if (reportTypeValue.name().equalsIgnoreCase(valueStr)) {
          reportType = reportTypeValue.name();
        }
      }
    }

    return reportType;
  }

  private static String getReportPriority(Serializable value) {
    String reportPriority = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliReportPriority reportPriorityValue : NsiliReportPriority.values()) {
        if (reportPriorityValue.name().equalsIgnoreCase(valueStr)) {
          reportPriority = reportPriorityValue.name();
          break;
        }
      }
    }

    return reportPriority;
  }

  private static String getIntRepSituationType(Serializable value) {
    String situationType = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliSituationType situationTypeValue : NsiliSituationType.values()) {
        if (situationTypeValue.getSpecName().equalsIgnoreCase(valueStr)) {
          situationType = situationTypeValue.getSpecName();
          break;
        }
      }
    }

    return situationType;
  }

  private static String getEntityType(Serializable value) {
    String entityType = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliEntityType entityTypeValue : NsiliEntityType.values()) {
        if (entityTypeValue.name().equalsIgnoreCase(valueStr)) {
          entityType = entityTypeValue.name();
          break;
        }
      }
    }

    return entityType;
  }

  private static String getClassification(Serializable value) {
    String classification = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliClassification classificationValue : NsiliClassification.values()) {
        if (classificationValue.getSpecName().equalsIgnoreCase(valueStr)) {
          classification = classificationValue.getSpecName();
          break;
        }
      }
    }

    return classification;
  }

  private static String getRfiStatus(Serializable value) {
    String rfiStatus = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliRfiStatus rfiStatusValue : NsiliRfiStatus.values()) {
        if (rfiStatusValue.name().equalsIgnoreCase(valueStr)) {
          rfiStatus = rfiStatusValue.name();
          break;
        }
      }
    }

    return rfiStatus;
  }

  private static String getRfiWorkflowStatus(Serializable value) {
    String rfiWorkflowStatus = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliRfiWorkflowStatus workflowStatusValue : NsiliRfiWorkflowStatus.values()) {
        if (workflowStatusValue.name().equalsIgnoreCase(valueStr)) {
          rfiWorkflowStatus = workflowStatusValue.name();
          break;
        }
      }
    }

    return rfiWorkflowStatus;
  }

  private static String getTaskStatus(Serializable value) {
    String taskStatus = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliTaskStatus taskStatusValue : NsiliTaskStatus.values()) {
        if (taskStatusValue.name().toLowerCase().equalsIgnoreCase(valueStr)) {
          taskStatus = taskStatusValue.name();
          break;
        }
      }
    }

    return taskStatus;
  }

  private static String getCbrnEventType(Serializable value) {
    String cbrnEventType = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliCbrnEvent cbrnEventValue : NsiliCbrnEvent.values()) {
        if (cbrnEventValue.getSpecName().equalsIgnoreCase(valueStr)) {
          cbrnEventType = cbrnEventValue.getSpecName();
          break;
        }
      }
    }

    return cbrnEventType;
  }

  private static String getCbrnAlarmClassification(Serializable value) {
    String cbrnAlarmClassification = null;
    String valueStr = String.valueOf(value);

    if (valueStr != null) {
      for (NsiliCbrnAlarmClassification alarmClassificationValue :
          NsiliCbrnAlarmClassification.values()) {
        if (alarmClassificationValue.getSpecName().equalsIgnoreCase(valueStr)) {
          cbrnAlarmClassification = alarmClassificationValue.getSpecName();
          break;
        }
      }
    }

    return cbrnAlarmClassification;
  }
}
