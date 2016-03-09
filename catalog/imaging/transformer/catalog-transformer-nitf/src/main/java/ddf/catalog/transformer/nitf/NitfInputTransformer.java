/**
 * Copyright (c) Codice Foundation
 * <p/>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package ddf.catalog.transformer.nitf;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.codice.imaging.nitf.core.AllDataExtractionParseStrategy;
import org.codice.imaging.nitf.core.NitfFileHeader;
import org.codice.imaging.nitf.core.NitfFileParser;
import org.codice.imaging.nitf.core.common.CommonNitfSegment;
import org.codice.imaging.nitf.core.common.NitfDateTime;
import org.codice.imaging.nitf.core.common.NitfInputStreamReader;
import org.codice.imaging.nitf.core.common.NitfReader;
import org.codice.imaging.nitf.core.graphic.NitfGraphicSegmentHeader;
import org.codice.imaging.nitf.core.image.ImageCoordinates;
import org.codice.imaging.nitf.core.image.ImageCoordinatesRepresentation;
import org.codice.imaging.nitf.core.image.NitfImageSegmentHeader;
import org.codice.imaging.nitf.core.label.LabelSegmentHeader;
import org.codice.imaging.nitf.core.security.FileSecurityMetadata;
import org.codice.imaging.nitf.core.security.SecurityMetadata;
import org.codice.imaging.nitf.core.symbol.SymbolSegmentHeader;
import org.codice.imaging.nitf.core.text.TextSegmentHeader;
import org.codice.imaging.nitf.core.tre.Tre;
import org.codice.imaging.nitf.core.tre.TreCollection;
import org.codice.imaging.nitf.core.tre.TreEntry;
import org.codice.imaging.nitf.core.tre.TreGroup;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.dynamic.api.DynamicMetacard;
import ddf.catalog.data.dynamic.api.MetacardFactory;
import ddf.catalog.transform.CatalogTransformerException;
import ddf.catalog.transform.InputTransformer;

/**
 * Converts NITF images into a Metacard.
 */
public class NitfInputTransformer implements InputTransformer {

    private static final String ID = "ddf/catalog/transformer/nitf";

    private static final String MIME_TYPE = "image/nitf";

    private static final Logger LOGGER = LoggerFactory.getLogger(NitfInputTransformer.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.
            forPattern("yyyyMMddHHmmss")
            .withZone(DateTimeZone.UTC);

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(
            PrecisionModel.FLOATING), 4326);

    private MetacardFactory metacardFactory;

    private static void outputThisTre(StringBuilder treXml, Tre tre) {
        treXml.append("    <tre name=\"");
        treXml.append(tre.getName()
                .trim());
        treXml.append("\">\n");
        for (TreEntry entry : tre.getEntries()) {
            outputThisEntry(treXml, entry, 2);
        }
        treXml.append("    </tre>\n");
    }

    private static void doIndent(StringBuilder treXml, int indentLevel) {
        for (int i = 0; i < indentLevel; ++i) {
            treXml.append("  ");
        }
    }

    private static void outputThisEntry(StringBuilder treXml, TreEntry entry, int indentLevel) {
        if (entry.getFieldValue() != null) {
            doIndent(treXml, indentLevel);
            treXml.append("<field name=\"");
            treXml.append(entry.getName());
            treXml.append("\" value=\"");
            treXml.append(entry.getFieldValue());
            treXml.append("\" />\n");
        }
        if ((entry.getGroups() != null) && (!entry.getGroups()
                .isEmpty())) {
            doIndent(treXml, indentLevel);
            treXml.append("<repeated name=\"");
            treXml.append(entry.getName());
            treXml.append("\" number=\"");
            treXml.append(entry.getGroups()
                    .size());
            treXml.append("\">\n");
            int i = 0;
            for (TreGroup group : entry.getGroups()) {
                doIndent(treXml, indentLevel + 1);
                treXml.append(String.format("<group index=\"%d\">%n", i));
                for (TreEntry groupEntry : group.getEntries()) {
                    outputThisEntry(treXml, groupEntry, indentLevel + 2);
                }
                doIndent(treXml, indentLevel + 1);
                treXml.append(String.format("</group>%n"));
                i++;
            }
            doIndent(treXml, indentLevel);
            treXml.append("</repeated>\n");
        }
    }

    /**
     * Transforms NITF images into a {@link Metacard}
     */
    public Metacard transform(InputStream input) throws IOException, CatalogTransformerException {
        return transform(input, null);
    }

    public Metacard transform(InputStream input, String id)
            throws IOException, CatalogTransformerException {
        if (input == null) {
            throw new CatalogTransformerException("Cannot transform null input.");
        }

        final DynamicMetacard metacard = metacardFactory.newInstance(Nitf.NAME);
        if (metacard == null) {
            throw new CatalogTransformerException("Unable to create NITF metacard.");
        }

        parseNitf(input, metacard);
        metacard.setAttribute(Metacard.ID, id);
        metacard.setAttribute(Metacard.CONTENT_TYPE, MIME_TYPE);

        return metacard;
    }

    private void parseNitf(InputStream input, DynamicMetacard metacard)
            throws CatalogTransformerException {
        List<Polygon> polygonList = new ArrayList<>();
        // TODO: update to XStream or some xml streaming library to create this metadata
        StringBuilder metadataXml = new StringBuilder();
        metadataXml.append("<metadata>\n");

        try {
            // TODO: implement and use flow API (available in imaging-nitf 0.3)

            AllDataExtractionParseStrategy parsingStrategy = new AllDataExtractionParseStrategy();
            NitfReader reader = new NitfInputStreamReader(input);

            NitfFileParser.parse(reader, parsingStrategy);

            setHeaderAttributes(metacard, parsingStrategy.getNitfHeader());

            handleSegment(parsingStrategy.getImageSegmentHeaders(), (imageHeader) -> {
                handleImageSegmentHeader(metacard, imageHeader, polygonList);
                setMetadataFromImageSegments(metadataXml, imageHeader);
            });

            handleSegment(parsingStrategy.getGraphicSegmentHeaders(),
                    (graphic) -> setMetadataForGraphicSegment(metadataXml, graphic));

            handleSegment(parsingStrategy.getSymbolSegmentHeaders(),
                    (symbol) -> setMetadataForSymbolHeader(metadataXml, symbol));

            handleSegment(parsingStrategy.getLabelSegmentHeaders(),
                    (label) -> setMetadataForLabelHeader(metadataXml, label));

            handleSegment(parsingStrategy.getTextSegmentHeaders(),
                    (text) -> setMetadataForTextHeader(metadataXml, text));

            metadataXml.append("</metadata>\n");
            metacard.setAttribute(Metacard.METADATA, metadataXml.toString());

            // Set GEOGRAPHY from discovered polygons
            if (polygonList.size() == 1) {
                metacard.setAttribute(Metacard.GEOGRAPHY,
                        polygonList.get(0)
                                .toText());
            } else if (polygonList.size() > 1) {
                Polygon[] polyAry = polygonList.toArray(new Polygon[0]);
                MultiPolygon multiPolygon = GEOMETRY_FACTORY.createMultiPolygon(polyAry);
                metacard.setAttribute(Metacard.GEOGRAPHY, multiPolygon.toText());
            }

            //TODO: Generate thumbnails - this doesn't work in a headless environment
/*
            ImageInputStream imageData = new MemoryCacheImageInputStream(new ByteArrayInputStream(parsingStrategy.getImageSegmentData().get(0)));
            NitfRenderer renderer = new NitfRenderer();
            BufferedImage img = renderer.render(parsingStrategy.getImageSegmentHeaders().get(0), imageData);
            img = scale(img);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", bos);
            if (bos.size() > 1024*128) {
                LOGGER.info("Thumbnail too big, ignoring.");
            } else {
                metacard.setAttribute(Metacard.THUMBNAIL, bos.toByteArray());
            }
*/

        } catch (ParseException e) {
            throw new CatalogTransformerException(e);
        }
    }

    private <T extends CommonNitfSegment> void handleSegment(List<T> segmentList,
            Consumer<T> segmentConsumer) {
        for (T segment : segmentList) {
            segmentConsumer.accept(segment);
        }
    }

    private void setHeaderAttributes(DynamicMetacard metacard, NitfFileHeader header) {
        Date date = getDate(header.getFileDateTime()
                .getSourceString());
        metacard.setAttribute(Metacard.CREATED, date);
        metacard.setAttribute(Metacard.TITLE, header.getFileTitle());
        metacard.setAttribute(Nitf.NITF_VERSION, header.getFileType());
        metacard.addAttribute(Nitf.FILE_DATE_TIME, date);
        // TODO: modified date from HISTOA?
        metacard.setAttribute(Metacard.MODIFIED, date);
        metacard.setAttribute(Metacard.CREATED, date);
        metacard.setAttribute(Metacard.EFFECTIVE, date);

        metacard.addAttribute(Nitf.FILE_TITLE, header.getFileTitle());
        metacard.addAttribute(Nitf.COMPLEXITY_LEVEL, header.getComplexityLevel());
        metacard.addAttribute(Nitf.ORIGINATOR_NAME, header.getOriginatorsName());
        metacard.addAttribute(Nitf.ORIGINATING_STATION_ID, header.getOriginatingStationId());

        FileSecurityMetadata security = header.getFileSecurityMetadata();
        metacard.addAttribute(Nitf.CODE_WORDS, security.getCodewords());
        metacard.addAttribute(Nitf.CONTROL_CODE, security.getControlAndHandling());
        metacard.addAttribute(Nitf.RELEASE_INSTRUCTION, security.getReleaseInstructions());
        metacard.addAttribute(Nitf.CONTROL_NUMBER, security.getSecurityControlNumber());
        metacard.addAttribute(Nitf.CLASSIFICATION_SYSTEM,
                security.getSecurityClassificationSystem());
        metacard.addAttribute(Nitf.CLASSIFICATION_AUTHORITY, security.getClassificationAuthority());
        metacard.addAttribute(Nitf.CLASSIFICATION_AUTHORITY_TYPE,
                security.getClassificationAuthorityType());
        metacard.addAttribute(Nitf.CLASSIFICATION_TEXT, security.getClassificationText());
        metacard.addAttribute(Nitf.CLASSIFICATION_REASON, security.getClassificationReason());
        String s = security.getSecuritySourceDate();
        /*
        if (StringUtils.isNotEmpty(s)) {
            // TODO: Convert to date
            metacard.addAttribute(Nitf.CLASSIFICATION_DATE, s);
        }
        */
        metacard.addAttribute(Nitf.DECLASSIFICATION_TYPE, security.getDeclassificationType());

        // TODO convert to date
        /*
        metacard.addAttribute(Nitf.DECLASSIFICATION_DATE,
                security()
                        .getDeclassificationDate());
*/
        // TODO: add the TRE's as attributes to the MetacardType Dynamically?
    }

    private void handleImageSegmentHeader(DynamicMetacard metacard,
            NitfImageSegmentHeader imagesegmentHeader, List<Polygon> polygons) {
        metacard.addAttribute(Nitf.IMAGE_ID, imagesegmentHeader.getImageIdentifier2());
        metacard.addAttribute(Nitf.ISOURCE, imagesegmentHeader.getImageSource());
        metacard.addAttribute(Nitf.NUMBER_OF_ROWS, imagesegmentHeader.getNumberOfRows());
        metacard.addAttribute(Nitf.NUMBER_OF_COLUMNS, imagesegmentHeader.getNumberOfColumns());
        metacard.addAttribute(Nitf.NUMBER_OF_BANDS, imagesegmentHeader.getNumBands());
        metacard.addAttribute(Nitf.REPRESENTATION,
                imagesegmentHeader.getImageRepresentation()
                        .name());
        metacard.addAttribute(Nitf.SUBCATEGORY,
                imagesegmentHeader.getImageCategory()
                        .name());
        metacard.addAttribute(Nitf.BITS_PER_PIXEL_PER_BAND,
                imagesegmentHeader.getNumberOfBitsPerPixelPerBand());
        metacard.addAttribute(Nitf.IMAGE_MODE,
                imagesegmentHeader.getImageMode()
                        .name());
        metacard.addAttribute(Nitf.COMPRESSION,
                imagesegmentHeader.getImageCompression()
                        .name());
        metacard.addAttribute(Nitf.RATE_CODE, imagesegmentHeader.getCompressionRate());
        metacard.addAttribute(Nitf.TARGET_ID,
                imagesegmentHeader.getImageTargetId()
                        .toString());
        metacard.addAttribute(Nitf.COMMENT, imagesegmentHeader.getImageComments());

        // handle geometry
        // TODO: add more coordinate support
        // TODO: handle case where its really a point.
        if ((imagesegmentHeader.getImageCoordinatesRepresentation()
                == ImageCoordinatesRepresentation.GEOGRAPHIC) || (
                imagesegmentHeader.getImageCoordinatesRepresentation()
                        == ImageCoordinatesRepresentation.DECIMALDEGREES)) {
            polygons.add(getPolygonForSegment(imagesegmentHeader, GEOMETRY_FACTORY));
        } else if (imagesegmentHeader.getImageCoordinatesRepresentation()
                != ImageCoordinatesRepresentation.NONE) {
            LOGGER.info("Unsupported representation: {}",
                    imagesegmentHeader.getImageCoordinatesRepresentation());
        }
    }

    private Date getDate(String string) {
        Date date = null;
        if (StringUtils.isNotEmpty(string)) {
            date = DATE_TIME_FORMATTER.parseDateTime(string)
                    .toDate();
            if (date == null) {
                LOGGER.warn("Error parsing date - source value {}", string);
            }
        }
        return date;
    }

    private Polygon getPolygonForSegment(NitfImageSegmentHeader segment,
            GeometryFactory geomFactory) {
        Coordinate[] coords = new Coordinate[5];
        ImageCoordinates imageCoordinates = segment.getImageCoordinates();
        coords[0] = new Coordinate(imageCoordinates.getCoordinate00()
                .getLongitude(),
                imageCoordinates.getCoordinate00()
                        .getLatitude());
        coords[4] = new Coordinate(coords[0]);
        coords[1] = new Coordinate(imageCoordinates.getCoordinate0MaxCol()
                .getLongitude(),
                imageCoordinates.getCoordinate0MaxCol()
                        .getLatitude());
        coords[2] = new Coordinate(imageCoordinates.getCoordinateMaxRowMaxCol()
                .getLongitude(),
                imageCoordinates.getCoordinateMaxRowMaxCol()
                        .getLatitude());
        coords[3] = new Coordinate(imageCoordinates.getCoordinateMaxRow0()
                .getLongitude(),
                imageCoordinates.getCoordinateMaxRow0()
                        .getLatitude());
        LinearRing externalRing = geomFactory.createLinearRing(coords);
        return geomFactory.createPolygon(externalRing, null);
    }

    private void setMetadataForLabelHeader(StringBuilder metadataXml, LabelSegmentHeader label) {
        metadataXml.append("  <label>\n");
        metadataXml.append(buildMetadataEntry("labelIdentifier", label.getIdentifier()));
        addSecurityMetadata(metadataXml, label.getSecurityMetadata());
        metadataXml.append(buildMetadataEntry("labelLocationRow", label.getLabelLocationRow()));
        metadataXml.append(buildMetadataEntry("labelLocationColumn",
                label.getLabelLocationColumn()));
        metadataXml.append(buildMetadataEntry("labelCellWidth", label.getLabelCellWidth()));
        metadataXml.append(buildMetadataEntry("labelCellHeight", label.getLabelCellHeight()));
        metadataXml.append(buildMetadataEntry("labelDisplayLevel", label.getLabelDisplayLevel()));
        metadataXml.append(buildMetadataEntry("labelAttachmentLevel", label.getAttachmentLevel()));
        metadataXml.append(buildMetadataEntry("labelTextColour",
                label.getLabelTextColour()
                        .toString()));
        metadataXml.append(buildMetadataEntry("labelBackgroundColour",
                label.getLabelBackgroundColour()
                        .toString()));
        metadataXml.append(buildTREsMetadata(label.getTREsRawStructure()));
        metadataXml.append("  </label>\n");
    }

    private void setMetadataForTextHeader(StringBuilder metadataXml, TextSegmentHeader text) {
        metadataXml.append("  <text>\n");
        metadataXml.append(buildMetadataEntry("textIdentifier", text.getIdentifier()));
        addSecurityMetadata(metadataXml, text.getSecurityMetadata());
        metadataXml.append(buildMetadataEntry("textDateTime",
                text.getTextDateTime()
                        .toString()));
        metadataXml.append(buildMetadataEntry("textTitle", text.getTextTitle()));
        metadataXml.append(buildMetadataEntry("textFormat",
                text.getTextFormat()
                        .toString()));
        metadataXml.append(buildTREsMetadata(text.getTREsRawStructure()));
        metadataXml.append("  </text>\n");
    }

    private void setMetadataForSymbolHeader(StringBuilder metadataXml, SymbolSegmentHeader symbol) {
        metadataXml.append("  <symbol>\n");
        metadataXml.append(buildMetadataEntry("symbolIdentifier", symbol.getIdentifier()));
        metadataXml.append(buildMetadataEntry("symbolName", symbol.getSymbolName()));
        addSecurityMetadata(metadataXml, symbol.getSecurityMetadata());
        metadataXml.append(buildMetadataEntry("symbolType",
                symbol.getSymbolType()
                        .toString()));
        metadataXml.append(buildMetadataEntry("symbolColour",
                symbol.getSymbolColour()
                        .toString()));
        metadataXml.append(buildMetadataEntry("numberOfLinesPerSymbol",
                symbol.getNumberOfLinesPerSymbol()));
        metadataXml.append(buildMetadataEntry("numberOfPixelsPerLine",
                symbol.getNumberOfPixelsPerLine()));
        metadataXml.append(buildMetadataEntry("lineWidth", symbol.getLineWidth()));
        metadataXml.append(buildMetadataEntry("numberOfBitsPerPixel",
                symbol.getNumberOfBitsPerPixel()));
        metadataXml.append(buildMetadataEntry("symbolDisplayLevel",
                symbol.getSymbolDisplayLevel()));
        metadataXml.append(buildMetadataEntry("symbolAttachmentLevel",
                symbol.getAttachmentLevel()));
        metadataXml.append(buildMetadataEntry("symbolLocationRow", symbol.getSymbolLocationRow()));
        metadataXml.append(buildMetadataEntry("symbolLocationColumn",
                symbol.getSymbolLocationColumn()));
        metadataXml.append(buildMetadataEntry("symbolLocation2Row",
                symbol.getSymbolLocation2Row()));
        metadataXml.append(buildMetadataEntry("symbolLocation2Column",
                symbol.getSymbolLocation2Column()));
        metadataXml.append(buildMetadataEntry("symbolNumber", symbol.getSymbolNumber()));
        metadataXml.append(buildMetadataEntry("symbolRotation", symbol.getSymbolRotation()));
        metadataXml.append(buildTREsMetadata(symbol.getTREsRawStructure()));
        metadataXml.append("  </symbol>\n");
    }

    private void setMetadataForGraphicSegment(StringBuilder metadataXml,
            NitfGraphicSegmentHeader graphic) {
        metadataXml.append("  <graphic>\n");
        metadataXml.append(buildMetadataEntry("graphicIdentifier", graphic.getIdentifier()));
        metadataXml.append(buildMetadataEntry("graphicName", graphic.getGraphicName()));
        addSecurityMetadata(metadataXml, graphic.getSecurityMetadata());
        metadataXml.append(buildMetadataEntry("graphicDisplayLevel",
                graphic.getGraphicDisplayLevel()));
        metadataXml.append(buildMetadataEntry("graphicAttachmentLevel",
                graphic.getAttachmentLevel()));
        metadataXml.append(buildMetadataEntry("graphicLocationRow",
                graphic.getGraphicLocationRow()));
        metadataXml.append(buildMetadataEntry("graphicLocationColumn",
                graphic.getGraphicLocationColumn()));
        metadataXml.append(buildMetadataEntry("graphicBoundingBox1Row",
                graphic.getBoundingBox1Row()));
        metadataXml.append(buildMetadataEntry("graphicBoundingBox1Column",
                graphic.getBoundingBox1Column()));
        metadataXml.append(buildMetadataEntry("graphicBoundingBox2Row",
                graphic.getBoundingBox2Row()));
        metadataXml.append(buildMetadataEntry("graphicBoundingBox2Column",
                graphic.getBoundingBox2Column()));
        metadataXml.append(buildMetadataEntry("graphicColour",
                graphic.getGraphicColour()
                        .toString()));
        metadataXml.append(buildTREsMetadata(graphic.getTREsRawStructure()));
        metadataXml.append("  </graphic>\n");
    }

    private void setMetadataFromImageSegments(StringBuilder metadataXml,
            NitfImageSegmentHeader image) {
        metadataXml.append("  <image>\n");
        metadataXml.append(buildMetadataEntry("imageIdentifer1", image.getIdentifier()));
        metadataXml.append(buildMetadataEntry("imageDateTime", image.getImageDateTime()));
        metadataXml.append(buildMetadataEntry("imageBasicEncyclopediaNumber",
                image.getImageTargetId()
                        .getBasicEncyclopediaNumber()
                        .trim()));
        metadataXml.append(buildMetadataEntry("imageOSuffix",
                image.getImageTargetId()
                        .getOSuffix()
                        .trim()));
        metadataXml.append(buildMetadataEntry("imageCountryCode",
                image.getImageTargetId()
                        .getCountryCode()
                        .trim()));
        metadataXml.append(buildMetadataEntry("imageIdentifer2", image.getImageIdentifier2()));
        addSecurityMetadata(metadataXml, image.getSecurityMetadata());
        metadataXml.append(buildMetadataEntry("imageSource", image.getImageSource()));
        metadataXml.append(buildMetadataEntry("numberOfRows", image.getNumberOfRows()));
        metadataXml.append(buildMetadataEntry("numberOfColumns", image.getNumberOfColumns()));
        metadataXml.append(buildMetadataEntry("pixelValueType",
                image.getPixelValueType()
                        .toString()));
        metadataXml.append(buildMetadataEntry("imageRepresentation",
                image.getImageRepresentation()
                        .toString()));
        metadataXml.append(buildMetadataEntry("imageCategory",
                image.getImageCategory()
                        .toString()));
        metadataXml.append(buildMetadataEntry("actualBitsPerPixelPerBand",
                image.getActualBitsPerPixelPerBand()));
        metadataXml.append(buildMetadataEntry("pixelJustification",
                image.getPixelJustification()
                        .toString()));
        metadataXml.append(buildMetadataEntry("imageCoordinatesRepresentation",
                image.getImageCoordinatesRepresentation()
                        .toString()));
        for (String comment : image.getImageComments()) {
            metadataXml.append(buildMetadataEntry("imageComment", comment));
        }
        metadataXml.append(buildMetadataEntry("imageCompression",
                image.getImageCompression()
                        .toString()));
        metadataXml.append(buildMetadataEntry("compressionRate", image.getCompressionRate()));
        metadataXml.append(buildMetadataEntry("imageMode",
                image.getImageMode()
                        .toString()));
        metadataXml.append(buildMetadataEntry("numberOfBlocksPerRow",
                image.getNumberOfBlocksPerRow()));
        metadataXml.append(buildMetadataEntry("numberOfBlocksPerColumn",
                image.getNumberOfBlocksPerColumn()));
        metadataXml.append(buildMetadataEntry("numberOfPixelsPerBlockHorizontal",
                image.getNumberOfPixelsPerBlockHorizontal()));
        metadataXml.append(buildMetadataEntry("numberOfPixelsPerBlockVertical",
                image.getNumberOfPixelsPerBlockVertical()));
        metadataXml.append(buildMetadataEntry("numberOfBitsPerPixelPerBand",
                image.getNumberOfBitsPerPixelPerBand()));
        metadataXml.append(buildMetadataEntry("imageDisplayLevel", image.getImageDisplayLevel()));
        metadataXml.append(buildMetadataEntry("imageAttachmentLevel", image.getAttachmentLevel()));
        metadataXml.append(buildMetadataEntry("imageLocationRow", image.getImageLocationRow()));
        metadataXml.append(buildMetadataEntry("imageLocationColumn",
                image.getImageLocationColumn()));
        if (image.getImageMagnification() != null) {
            metadataXml.append(buildMetadataEntry("imageMagnification",
                    image.getImageMagnification()));
        }
        if (image.getImageCoordinates() != null) {
            metadataXml.append(buildMetadataEntry("imageCoordinates",
                    image.getImageCoordinates()
                            .toString()));
        }
        metadataXml.append(buildTREsMetadata(image.getTREsRawStructure()));
        metadataXml.append("  </image>\n");
    }

    private String buildTREsMetadata(TreCollection treCollection) {
        StringBuilder treXml = new StringBuilder();
        for (Tre tre : treCollection.getTREs()) {
            outputThisTre(treXml, tre);
        }
        return treXml.toString();
    }

    private String buildMetadataEntry(String label, int value) {
        return buildMetadataEntry(label, Integer.toString(value));
    }

    private String buildMetadataEntry(String label, long value) {
        return buildMetadataEntry(label, Long.toString(value));
    }

    private String buildMetadataEntry(String label, NitfDateTime value) {
        return buildMetadataEntry(label, value.getSourceString());
    }

    private String buildMetadataEntry(String label, String value) {
        StringBuilder entryBuilder = new StringBuilder();
        entryBuilder.append("    <");
        entryBuilder.append(label);
        entryBuilder.append(">");
        entryBuilder.append(StringEscapeUtils.escapeXml(value));
        entryBuilder.append("</");
        entryBuilder.append(label);
        entryBuilder.append(">\n");
        return entryBuilder.toString();
    }

    private void addFileSecurityMetadata(StringBuilder metadataXml, NitfFileHeader nitfFile) {
        FileSecurityMetadata security = nitfFile.getFileSecurityMetadata();
        addSecurityMetadata(metadataXml, security);
        metadataXml.append(buildMetadataEntry("securityFileCopyNumber",
                nitfFile.getFileSecurityMetadata()
                        .getFileCopyNumber()));
        metadataXml.append(buildMetadataEntry("securityFileNumberOfCopies",
                nitfFile.getFileSecurityMetadata()
                        .getFileNumberOfCopies()));
    }

    private void addSecurityMetadata(StringBuilder metadataXml, SecurityMetadata security) {
        metadataXml.append(buildMetadataEntry("securityClassification",
                security.getSecurityClassification()
                        .toString()));
        addMetadataIfNotNull(metadataXml,
                "securityClassificationSystem",
                security.getSecurityClassificationSystem());
        metadataXml.append(buildMetadataEntry("securityCodewords", security.getCodewords()));
        addMetadataIfNotNull(metadataXml,
                "securityControlAndHandling",
                security.getControlAndHandling());
        addMetadataIfNotNull(metadataXml,
                "securityReleaseInstructions",
                security.getReleaseInstructions());
        addMetadataIfNotNull(metadataXml,
                "securityDeclassificationType",
                security.getDeclassificationType());
        addMetadataIfNotNull(metadataXml,
                "securityDeclassificationDate",
                security.getDeclassificationDate());
        addMetadataIfNotNull(metadataXml,
                "securityDeclassificationExemption",
                security.getDeclassificationExemption());
        addMetadataIfNotNull(metadataXml, "securityDowngrade", security.getDowngrade());
        addMetadataIfNotNull(metadataXml, "securityDowngradeDate", security.getDowngradeDate());
        addMetadataIfNotNull(metadataXml,
                "securityDowngradeDateOrSpecialCase",
                security.getDowngradeDateOrSpecialCase());
        addMetadataIfNotNull(metadataXml, "securityDowngradeEvent", security.getDowngradeEvent());
    }

    private void addMetadataIfNotNull(StringBuilder metadataXml, String label, String value) {
        if (value != null) {
            metadataXml.append(buildMetadataEntry(label, value));
        }
    }

    @Override
    public String toString() {
        return "InputTransformer {Impl=" + this.getClass()
                .getName() + ", id=" + ID + ", mime-type=" + MIME_TYPE + "}";
    }

    public void setMetacardFactory(MetacardFactory factory) {
        LOGGER.info("NitfInputTransformer setMetacardFactory()");
        this.metacardFactory = factory;
    }

    private BufferedImage scale(BufferedImage source) {
        BufferedImage bi = getCompatibleImage(200, 200);
        Graphics2D g2d = bi.createGraphics();
        double xScale = (double) 200 / source.getWidth();
        double yScale = (double) 200 / source.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
        g2d.drawRenderedImage(source, at);
        g2d.dispose();
        return bi;
    }

    private BufferedImage getCompatibleImage(int w, int h) {
        System.setProperty("java.awt.headless", "true");
        Toolkit tk = Toolkit.getDefaultToolkit();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        return gc.createCompatibleImage(w, h);
    }
}
