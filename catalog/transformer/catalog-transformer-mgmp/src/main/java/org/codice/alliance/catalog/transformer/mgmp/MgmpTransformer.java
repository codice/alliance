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
package org.codice.alliance.catalog.transformer.mgmp;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.catalog.core.api.types.Security;
import org.codice.ddf.spatial.ogc.csw.catalog.common.GmdConstants;
import org.codice.ddf.spatial.ogc.csw.catalog.converter.XstreamPathValueTracker;
import org.codice.ddf.spatial.ogc.csw.catalog.transformer.GmdTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.io.path.Path;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.AttributeRegistry;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;
import ddf.catalog.data.impl.MetacardImpl;
import ddf.catalog.data.types.Contact;
import ddf.catalog.data.types.Core;
import ddf.catalog.data.types.Location;
import ddf.catalog.data.types.Media;
import ddf.catalog.transform.CatalogTransformerException;

public class MgmpTransformer extends GmdTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MgmpTransformer.class);

    private static final String GCO_PREFIX = "gco:";

    private static final String SRV_PREFIX = "srv:";

    private static final String GML_PREFIX = "gml:";

    private static final String MGMP_PREFIX = "mgmp:";

    private static final String COUNTRY_SEPARATOR = "/";

    private static final int ALPHA_3_LENGTH = 3;

    private AttributeRegistry attributeRegistry;

    private MetacardType mgmpMetacardType;

    public MgmpTransformer(MetacardType metacardType, AttributeRegistry attributeRegistry) {
        super(metacardType);
        this.mgmpMetacardType = metacardType;
        this.attributeRegistry = attributeRegistry;
        registerResourceStatus();
    }

    private void registerResourceStatus() {
        if (attributeRegistry != null) {
            attributeRegistry.register(new AttributeDescriptorImpl(GmdConstants.RESOURCE_STATUS,
                    true,
                    true,
                    false,
                    false,
                    BasicTypes.STRING_TYPE));
        }
    }

    public void destroy() {
        if (attributeRegistry != null) {
            attributeRegistry.deregister(GmdConstants.RESOURCE_STATUS);
        }
        super.destroy();
        LOGGER.debug("Deregistering {} from attribute registry", GmdConstants.RESOURCE_STATUS);
    }

    @Override
    public Metacard transform(InputStream inputStream)
            throws IOException, CatalogTransformerException {
        return super.transform(inputStream);
    }

    @Override
    public Metacard transform(InputStream inputStream, String id)
            throws IOException, CatalogTransformerException {
        return super.transform(inputStream, id);
    }

    @Override
    public LinkedHashSet<Path> buildPaths() {
        LinkedHashSet<Path> paths = Stream.of(MgmpConstants.RESOURCE_ORIGINATOR_SECURITY_PATH,
                MgmpConstants.RESOURCE_SECURITY_RELEASABILITY_PATH,
                MgmpConstants.LANGUAGE_PATH,
                MgmpConstants.CLOUD_COVERAGE_PATH,
                MgmpConstants.FORMAT_PATH,
                GmdConstants.FORMAT_VERSION_PATH,

                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_NUMBER_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_NUMBER_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_NUMBER_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_NUMBER_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_NUMBER_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_WKT_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_WKT_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_WKT_TYPE_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_WKT_TYPE_PATH,

                MgmpConstants.ISR_COVERAGE_CATEGORY_PATH,
                MgmpConstants.ISR_COVERAGE_COMMENT_PATH,
                MgmpConstants.ISR_IMAGE_COMMENT_PATH,
                MgmpConstants.ISR_IMAGE_DESCRIPTION_PATH,
                MgmpConstants.ISR_MD_IMAGE_COMMENT_PATH,
                MgmpConstants.ISR_MD_IMAGE_DESCRIPTION_PATH,
                MgmpConstants.ISR_VIDEO_COMMENT_PATH,
                MgmpConstants.ISR_VIDEO_DESCRIPTION_PATH,

                MgmpConstants.METADATA_ORIGINATOR_SECURITY_PATH,
                MgmpConstants.METADATA_RELEASABILITY_PATH,
                MgmpConstants.RESOURCE_SECURITY_RELEASABILITY_PATH,
                MgmpConstants.NIIRS_PATH,
                MgmpConstants.NIIRS_RATING_PATH,
                MgmpConstants.RESOURCE_SECURITY_PATH,
                MgmpConstants.METADATA_SECURITY_PATH,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_CRS_WKT_TYPE_PATH)
                .map(this::toPath)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        paths.addAll(super.buildPaths());
        return paths;
    }

    @Override
    public MetacardImpl toMetacard(final XstreamPathValueTracker pathValueTracker, String id) {
        MetacardImpl metacard = new MetacardImpl(mgmpMetacardType);
        super.setMetacardAttributes(metacard, id, pathValueTracker);
        addMetacardIsrInformation(pathValueTracker, metacard);
        addMetacardNiirsInformation(pathValueTracker, metacard);
        addMetacardLanguage(pathValueTracker, metacard);
        addMetacardCloudCoverPercentage(pathValueTracker, metacard);
        addMetacardResourceSecurityClassification(pathValueTracker, metacard);
        addMetacardResourceSecurityReleasability(pathValueTracker, metacard);
        addMetacardResourceSecurityOriginatorClassification(pathValueTracker, metacard);
        addMetacardMetadataSecurityReleasability(pathValueTracker, metacard);
        addMetacardMetadataSecurityOriginatorClassification(pathValueTracker, metacard);
        addMetacardMetadataSecurityClassification(pathValueTracker, metacard);
        addMetacardDataQuality(metacard);
        return metacard;
    }

    @Override
    public void addMetacardFormat(final XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        String format = pathValueTracker.getFirstValue(toPath(MgmpConstants.FORMAT_PATH));
        String formatVersion =
                pathValueTracker.getFirstValue(toPath(GmdConstants.FORMAT_VERSION_PATH));
        if (StringUtils.isNotEmpty(format)) {
            metacard.setAttribute(Media.FORMAT, format);
            if (StringUtils.isNotEmpty(formatVersion)) {
                metacard.setAttribute(Media.FORMAT_VERSION, formatVersion);
            }
        }
    }

    @Override
    public void addMetacardLanguage(final XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        List<String> languageList =
                pathValueTracker.getAllValues(toPath(MgmpConstants.LANGUAGE_PATH));
        if (CollectionUtils.isNotEmpty(languageList)) {
            languageList = filterResourceLanguages(languageList);
            metacard.setAttribute(Core.LANGUAGE, (Serializable) languageList);
        } else {
            metacard.setAttribute(Core.LANGUAGE, (Serializable) MgmpConstants.DEFAULT_LANGUAGE);
        }
    }

    @Override
    public void addMetacardCrs(final XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {

        super.addMetacardCrs(pathValueTracker, metacard);

        ArrayList<String> mgmpTypes = new ArrayList<>();
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpTypes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_NUMBER_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpTypes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_NUMBER_TYPE_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpTypes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_NUMBER_TYPE_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpTypes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_NUMBER_TYPE_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpTypes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_NUMBER_TYPE_PATH,
                false);

        ArrayList<String> mgmpCodes = new ArrayList<>();
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_TYPE_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_TYPE_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_TYPE_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_TYPE_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                mgmpCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_TYPE_PATH,
                false);

        List<String> crsCodes = new ArrayList<>();
        addValuesFromPathToListIfPresent(pathValueTracker,
                crsCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_CRS_WKT_TYPE_PATH,
                true);
        addValuesFromPathToListIfPresent(pathValueTracker,
                crsCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_GRID_WKT_TYPE_PATH,
                true);
        addValuesFromPathToListIfPresent(pathValueTracker,
                crsCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_PROJECTION_WKT_TYPE_PATH,
                true);
        addValuesFromPathToListIfPresent(pathValueTracker,
                crsCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_ELLIPSOID_WKT_TYPE_PATH,
                true);
        addValuesFromPathToListIfPresent(pathValueTracker,
                crsCodes,
                MgmpConstants.MGMP_SPATIAL_REFERENCE_SYSTEM_DATUM_WKT_TYPE_PATH,
                true);

        List<String> codes = pathValueTracker.getAllValues(toPath(GmdConstants.CRS_CODE_PATH));
        List<String> types = pathValueTracker.getAllValues(toPath(GmdConstants.CRS_AUTHORITY_PATH));
        if (CollectionUtils.isNotEmpty(types) && CollectionUtils.isNotEmpty(codes)) {
            if (types.size() != codes.size()) {
                LOGGER.debug(
                        "The size of the CRS code and codeSpaces do not match (code size : {}, types size :{}).  The Coordinate Reference System code will not be set.",
                        codes.size(),
                        types.size());
            } else {
                for (int i = 0; i < types.size() && i < codes.size(); i++) {
                    crsCodes.add(types.get(i) + ":" + codes.get(i));
                }
            }
        }

        if (CollectionUtils.isNotEmpty(mgmpTypes) && CollectionUtils.isNotEmpty(mgmpCodes)) {
            if (mgmpTypes.size() != mgmpCodes.size()) {
                LOGGER.debug(
                        "The size of the MGMP CRS code and codeSpaces do not match (code size : {}, types size :{}).  The Coordinate Reference System code will not be set.",
                        mgmpTypes.size(),
                        mgmpCodes.size());
            } else {
                for (int i = 0; i < mgmpTypes.size() && i < mgmpCodes.size(); i++) {
                    crsCodes.add(mgmpTypes.get(i) + ":" + mgmpCodes.get(i));
                }
            }
        }
        metacard.setAttribute(Location.COORDINATE_REFERENCE_SYSTEM_CODE, (Serializable) crsCodes);
    }

    private void addMetacardIsrInformation(final XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        List<String> isrComments = new ArrayList<>();
        addValuesFromPathToListIfPresent(pathValueTracker,
                isrComments,
                MgmpConstants.ISR_COVERAGE_COMMENT_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                isrComments,
                MgmpConstants.ISR_IMAGE_COMMENT_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                isrComments,
                MgmpConstants.ISR_MD_IMAGE_COMMENT_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                isrComments,
                MgmpConstants.ISR_VIDEO_COMMENT_PATH,
                false);

        List<String> isrCategory = new ArrayList<>();
        addValuesFromPathToListIfPresent(pathValueTracker,
                isrCategory,
                MgmpConstants.ISR_COVERAGE_CATEGORY_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                isrCategory,
                MgmpConstants.ISR_IMAGE_DESCRIPTION_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                isrCategory,
                MgmpConstants.ISR_MD_IMAGE_DESCRIPTION_PATH,
                false);
        addValuesFromPathToListIfPresent(pathValueTracker,
                isrCategory,
                MgmpConstants.ISR_VIDEO_DESCRIPTION_PATH,
                false);

        metacard.setAttribute(Isr.COMMENTS, (Serializable) isrComments);
        metacard.setAttribute(Isr.CATEGORY, (Serializable) isrCategory);

        addMetacardIsrOrganizationalUnit(metacard);
    }

    private void addMetacardIsrOrganizationalUnit(MetacardImpl metacard) {
        Attribute pocName = metacard.getAttribute(Contact.POINT_OF_CONTACT_NAME);
        Attribute publisherName = metacard.getAttribute(Contact.PUBLISHER_NAME);

        if (pocName != null) {
            setIsrOrganizationalUnitFromAttribute(pocName, metacard);
        } else if (publisherName != null) {
            setIsrOrganizationalUnitFromAttribute(publisherName, metacard);
        }
    }

    private void setIsrOrganizationalUnitFromAttribute(Attribute pocName, MetacardImpl metacard) {
        Optional.ofNullable(pocName.getValue())
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(StringUtils::isNotEmpty)
                .ifPresent(pocValue -> metacard.setAttribute(Isr.ORGANIZATIONAL_UNIT, pocValue));
    }

    private void addMetacardNiirsInformation(final XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        List<String> ratingSystemList =
                pathValueTracker.getAllValues(toPath(MgmpConstants.NIIRS_PATH));
        List<String> ratingsList =
                pathValueTracker.getAllValues(toPath(MgmpConstants.NIIRS_RATING_PATH));
        if (CollectionUtils.isNotEmpty(ratingsList)
                && CollectionUtils.isNotEmpty(ratingSystemList)) {
            if (ratingsList.size() != ratingSystemList.size()) {
                LOGGER.debug(
                        "The size of the NIIRS code and codeSpaces do not match (code size : {}, codeSpace size :{}).  The Coordinate Reference System code will not be set.",
                        ratingSystemList.size(),
                        ratingsList.size());
            } else {
                for (int i = 0; i < ratingsList.size() && i < ratingSystemList.size(); i++) {
                    if (!ratingSystemList.get(i)
                            .equals(MgmpConstants.NIIRS)) {
                        continue;
                    }
                    try {
                        Double niirsRating = Double.parseDouble(ratingsList.get(i));
                        metacard.setAttribute(Isr.NATIONAL_IMAGERY_INTERPRETABILITY_RATING_SCALE,
                                niirsRating);
                    } catch (NumberFormatException e) {
                        LOGGER.debug("Unable to parse double when setting NIIRS value", e);
                    }
                }
            }
        }
    }

    private void addMetacardResourceSecurityReleasability(
            final XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        addMetacardSecurityReleasability(pathValueTracker,
                metacard,
                MgmpConstants.RESOURCE_SECURITY_RELEASABILITY_PATH,
                Security.RESOURCE_RELEASABILITY,
                Security.RESOURCE_DISSEMINATION);
    }

    private void addMetacardMetadataSecurityReleasability(
            final XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        addMetacardSecurityReleasability(pathValueTracker,
                metacard,
                MgmpConstants.METADATA_RELEASABILITY_PATH,
                Security.METADATA_RELEASABILITY,
                Security.METADATA_DISSEMINATION);
    }

    private void addMetacardSecurityReleasability(final XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard, String path, String releasabilityConstant,
            String disseminationConstants) {
        String releasability = pathValueTracker.getFirstValue(toPath(path));
        if (StringUtils.isNotEmpty(releasability)) {
            addMetacardReleasabilityAndDisseminationControls(releasability,
                    metacard,
                    releasabilityConstant,
                    disseminationConstants);
        }
    }

    private void addMetacardReleasabilityAndDisseminationControls(String caveatString,
            MetacardImpl metacard, String releasability, String disseminationControls) {
        if (caveatString.startsWith(MgmpConstants.RELEASABLE_TO)) {
            String[] nations = StringUtils.substringAfter(caveatString,
                    MgmpConstants.RELEASABLE_TO + " ")
                    .split(COUNTRY_SEPARATOR);
            List<String> nationsList = Arrays.asList(nations);
            metacard.setAttribute(releasability, (Serializable) nationsList);
            metacard.setAttribute(disseminationControls, MgmpConstants.RELEASABLE_TO);
        } else if (caveatString.endsWith(MgmpConstants.EYES_ONLY)) {
            String[] nations = StringUtils.substringBefore(caveatString,
                    " " + MgmpConstants.EYES_ONLY)
                    .split(COUNTRY_SEPARATOR);
            List<String> nationsList = Arrays.asList(nations);
            metacard.setAttribute(releasability, (Serializable) nationsList);
            metacard.setAttribute(disseminationControls, MgmpConstants.EYES_ONLY);
        } else if (caveatString.endsWith(MgmpConstants.EYES_DISCRETION)) {
            String[] nations = StringUtils.substringBefore(caveatString,
                    " " + MgmpConstants.EYES_DISCRETION)
                    .split(COUNTRY_SEPARATOR);
            List<String> nationsList = Arrays.asList(nations);
            metacard.setAttribute(releasability, (Serializable) nationsList);
            metacard.setAttribute(disseminationControls, MgmpConstants.EYES_DISCRETION);
        }
    }

    private void addMetacardResourceSecurityClassification(
            final XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        addFieldToMetacardIfPresent(pathValueTracker,
                metacard,
                MgmpConstants.RESOURCE_SECURITY_PATH,
                Security.RESOURCE_CLASSIFICATION);
    }

    private void addMetacardMetadataSecurityClassification(
            final XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        addFieldToMetacardIfPresent(pathValueTracker,
                metacard,
                MgmpConstants.METADATA_SECURITY_PATH,
                Security.METADATA_CLASSIFICATION);
    }

    private void addMetacardDataQuality(MetacardImpl metacard) {

        try (InputStream inputStream = getSourceInputStream()) {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            XPath xPath = XPathFactory.newInstance()
                    .newXPath();

            List<String> dataQualityList = new ArrayList<>();
            for (String string : MgmpConstants.DATA_QUALITY_LIST) {

                XPathExpression dataExpression = xPath.compile(string);
                NodeList dataList = (NodeList) dataExpression.evaluate(document,
                        XPathConstants.NODESET);

                for (int i = 0; i < dataList.getLength(); i++) {
                    String dataQualityName = null;
                    String resultValue = null;

                    NodeList children = dataList.item(i)
                            .getChildNodes();
                    for (int c = 0; c < children.getLength(); c++) {
                        Node node = children.item(c);
                        String name = node.getNodeName();

                        switch (name) {
                        case MgmpConstants.RESULT:
                            NodeList childNodes = node.getChildNodes();
                            for (int x = 0; x < childNodes.getLength(); x++) {
                                Node resultNode = childNodes.item(x);
                                if (StringUtils.isEmpty(resultValue)) {
                                    resultValue = getDataQualityResultValue(resultNode);
                                }
                            }
                            break;
                        case MgmpConstants.NAME_OF_MEASURE:
                            if (StringUtils.isEmpty(dataQualityName)) {
                                dataQualityName = node.getTextContent()
                                        .trim();
                            }
                            break;

                        default:
                            break;
                        }
                    }

                    if (StringUtils.isNotEmpty(dataQualityName) && StringUtils.isNotEmpty(
                            resultValue)) {
                        dataQualityList.add(dataQualityName + " : " + resultValue);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(dataQualityList)) {
                metacard.setAttribute(Isr.DATA_QUALITY, (Serializable) dataQualityList);
            }
        } catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException e) {
            LOGGER.debug(
                    "Unable to parse Data Quality elements.  Metacard Data Quality will not be set.",
                    e);
            return;
        }
    }

    private String getDataQualityResultValue(Node resultNode) {
        String quantitativeResult = getDataQualityResultValue(MgmpConstants.DQ_QUANTITATIVE_RESULT,
                "value",
                resultNode);
        String descriptiveResult = getDataQualityResultValue(MgmpConstants.DESCRIPTIVE_RESULT,
                "statement",
                resultNode);
        if (StringUtils.isNotEmpty(quantitativeResult)) {
            return quantitativeResult;
        } else if (StringUtils.isNotEmpty(descriptiveResult)) {
            return descriptiveResult;
        }
        return null;
    }

    private String getDataQualityResultValue(String resultType, String resultField,
            Node resultNode) {
        if (resultNode.getNodeName()
                .equals(resultType)) {
            NodeList resultNodeChildren = resultNode.getChildNodes();
            for (int p = 0; p < resultNodeChildren.getLength(); p++) {
                Node childNode = resultNodeChildren.item(p);
                if (childNode.getNodeName()
                        .equals(resultField)) {
                    return childNode.getTextContent()
                            .trim();
                }
            }
        }
        return null;
    }

    private void addMetacardMetadataSecurityOriginatorClassification(
            final XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        addFieldToMetacardIfPresent(pathValueTracker,
                metacard,
                MgmpConstants.METADATA_ORIGINATOR_SECURITY_PATH,
                Security.METADATA_ORIGINATOR_CLASSIFICATION);
    }

    private void addMetacardResourceSecurityOriginatorClassification(
            final XstreamPathValueTracker pathValueTracker, MetacardImpl metacard) {
        addFieldToMetacardIfPresent(pathValueTracker,
                metacard,
                MgmpConstants.RESOURCE_ORIGINATOR_SECURITY_PATH,
                Security.RESOURCE_ORIGINATOR_CLASSIFICATION);
    }

    private void addMetacardCloudCoverPercentage(final XstreamPathValueTracker pathValueTracker,
            MetacardImpl metacard) {
        String cloudCoverage = pathValueTracker.getFirstValue(toPath(MgmpConstants.CLOUD_COVERAGE_PATH));
        if (StringUtils.isNotEmpty(cloudCoverage)) {
            try {
                Double cloudCoverageDouble = Double.parseDouble(cloudCoverage);
                metacard.setAttribute(Isr.CLOUD_COVER, cloudCoverageDouble);
            } catch (NumberFormatException e) {
                LOGGER.debug("Unable to parse {} into a double.  Skipping cloud coverage.", cloudCoverage);
            }
        }
    }

    private void addValuesFromPathToListIfPresent(final XstreamPathValueTracker pathValueTracker,
            List<String> list, String path, boolean parseCrsInformation) {
        List<String> values = pathValueTracker.getAllValues(toPath(path));
        if (CollectionUtils.isNotEmpty(values)) {
            if (parseCrsInformation) {
                list.addAll(parseCrsInformationFromWktSpecification(values));
            } else {
                list.addAll(values);
            }
        }
    }

    private List<String> parseCrsInformationFromWktSpecification(List<String> crsCodes) {
        return crsCodes.stream()
                .map(crsCode -> StringUtils.substringBetween(crsCode, ",ID[", "]"))
                .filter(StringUtils::isNotEmpty)
                .map(inf -> inf.replace("\"", ""))
                .map(inf -> inf.replace(',', ':'))
                .collect(Collectors.toList());
    }

    private List<String> filterResourceLanguages(List<String> languages) {
        return languages.stream()
                .filter(language -> language.length() == ALPHA_3_LENGTH)
                .collect(Collectors.toList());
    }

    @Override
    public Path toPath(String stringPath) {
        String filteredPath = stringPath.replace(GCO_PREFIX, "")
                .replace(SRV_PREFIX, "")
                .replace(MGMP_PREFIX, "")
                .replace(GML_PREFIX, "");
        return new Path(filteredPath);
    }
}
