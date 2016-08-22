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
package org.codice.alliance.transformer.nitf.image;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.function.Function;

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.types.Isr;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.imaging.nitf.core.common.DateTime;
import org.codice.imaging.nitf.core.common.NitfFormatException;
import org.codice.imaging.nitf.core.image.ImageSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.MetacardType;
import ddf.catalog.data.impl.types.DateTimeAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.types.Media;

/**
 * NitfAttributes to represent the properties of a ImageSegment.
 */
public enum ImageAttribute implements NitfAttribute<ImageSegment> {
    IMAGE_DATE_AND_TIME(ddf.catalog.data.types.DateTime.START,
            "IDATIM",
            segment -> convertNitfDate(segment.getImageDateTime()),
            new DateTimeAttributes()),
    TARGET_IDENTIFIER(Isr.TARGET_ID,
            "TGTID",
            segment -> getTargetId(segment),
            new IsrAttributes()),
    IMAGE_IDENTIFIER_2(Isr.IMAGE_ID,
            "IID2",
            ImageSegment::getImageIdentifier2,
            new IsrAttributes()),
    IMAGE_SOURCE(Isr.ORIGINAL_SOURCE,
            "ISORCE",
            ImageSegment::getImageSource,
            new IsrAttributes()),
    NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE(Media.HEIGHT,
            "NROWS",
            ImageSegment::getNumberOfRows,
            new MediaAttributes()),
    NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE(Media.WIDTH,
            "NCOLS",
            ImageSegment::getNumberOfColumns,
            new MediaAttributes()),
    IMAGE_REPRESENTATION(Media.ENCODING,
            "IREP",
            segment -> segment.getImageRepresentation()
                    .name(),
            new MediaAttributes()),
    IMAGE_CATEGORY(Isr.CATEGORY,
            "ICAT",
            segment -> segment.getImageCategory()
                    .name(),
            new IsrAttributes()),
    IMAGE_COMPRESSION(Media.COMPRESSION,
            "IC",
            segment -> segment.getImageCompression()
                    .name(),
            new MediaAttributes());

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageAttribute.class);

    private String shortName;

    private String longName;

    private Function<ImageSegment, Serializable> accessorFunction;

    private AttributeDescriptor attributeDescriptor;
    
    ImageAttribute(final String lName,
                   final String sName,
                   final Function<ImageSegment, Serializable> accessor,
                   MetacardType metacardType) {
        this.accessorFunction = accessor;
        this.shortName = sName;
        this.longName = lName;
        // retrieving metacard attribute descriptor for this attribute to prevent later lookups
        this.attributeDescriptor = metacardType.getAttributeDescriptor(longName);
    }

    private static String getTargetId(ImageSegment imageSegment) {
        if (imageSegment == null || imageSegment.getImageTargetId() == null) {
            return null;
        }

        try {
            return imageSegment.getImageTargetId()
                    .textValue()
                    .trim();
        } catch (NitfFormatException nfe) {
            LOGGER.debug(nfe.getMessage(), nfe);
        }

        return null;
    }

    private static Date convertNitfDate(DateTime nitfDateTime) {
        if (nitfDateTime == null || nitfDateTime.getZonedDateTime() == null) {
            return null;
        }

        ZonedDateTime zonedDateTime = nitfDateTime.getZonedDateTime();
        Instant instant = zonedDateTime.toInstant();

        if (instant != null) {
            return Date.from(instant);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getShortName() {
        return this.shortName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getLongName() {
        return this.longName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Function<ImageSegment, Serializable> getAccessorFunction() {
        return accessorFunction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeDescriptor getAttributeDescriptor() {
        return this.attributeDescriptor;
    }

    /**
     * Equivalent to getLongName()
     *
     * @return the attribute's long name.
     */
    @Override
    public String toString() {
        return getLongName();
    }
}
