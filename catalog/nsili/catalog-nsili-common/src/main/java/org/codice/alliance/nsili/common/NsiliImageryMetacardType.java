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
package org.codice.alliance.nsili.common;

import java.util.HashSet;
import java.util.Set;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public class NsiliImageryMetacardType extends NsiliMetacardType {
    public static final String METACARD_TYPE_NAME = NSILI_METACARD_TYPE_PREFIX +".imagery."+ NSILI_METACARD_TYPE_POSTFIX;

    /**
     * A valid indicator of the specific category of image, raster, or grid data.
     * The specific category of an image reveals its intended use or the nature of its collector.
     */
    public static final String IMAGERY_CATEGORY = "imageryCategory";

    /**
     * A code that indicates the percentage of the image obscured by cloud cover.
     */
    public static final String CLOUD_COVER_PCT = "cloudCoverPct";

    /**
     * Comments related to the image.
     */
    public static final String IMAGERY_COMMENTS = "imageryComments";

    /**
     * Specification of algorithm or process to apply to read or expand the image to which compression
     * techniques have been applied.
     */
    public static final String DECOMPRESSION_TECHNIQUE = "decompressionTechnique";

    /**
     * Identification code associated with the image.
     */
    public static final String IMAGE_ID = "imageId";

    /**
     * National Image Interpretability Rating Scales (NIIRS) -defines and measures the quality of
     * images and the performance of imaging systems.
     */
    public static final String NIIRS = "niirs";

    /**
     * The number of data bands within the specified image.
     */
    public static final String NUM_BANDS = "numBands";

    /**
     * Number of significant rows in Image. This field shall contain the total number of rows of significant
     * pixels in the image. It could be that an image has been padded with fill data
     * (to fill out the last part of the block in a blocked image). The meaning of 'significant' is
     * then only the rows conveying image data excluding all the rows with padded fill data.
     */
    public static final String NUM_ROWS = "numRows";

    /**
     * Number of significant columns in Image. This field shall contain the total number of columns
     * of significant pixels in the image. It could be that an image has been padded with fill data
     * (to fill out the last part of the block in a blocked image). The meaning of 'significant'
     * is then only the columns conveying image data excluding all the columns with padded fill data.
     */
    public static final String NUM_COLS = "numCols";

    /**
     * Metacard AttributeType for the NSILI Imagery Category.
     */
    public static final AttributeType<NsiliImageryType> NSILI_IMAGERY_TYPE;

    /**
     * Metacard AttributeType for the NSILI Imagery Decompression Technique.
     */
    public static final AttributeType<NsiliImageryDecompressionTech>
            NSILI_IMAGERY_DECOMPRESSION_TECH_TYPE;

    private static final Set<AttributeDescriptor> NSILI_IMAGERY_DESCRIPTORS = new HashSet<>();

    static {
        NSILI_IMAGERY_TYPE = new AttributeType<NsiliImageryType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliImageryType> getBinding() {
                return NsiliImageryType.class;
            }
        };

        NSILI_IMAGERY_DECOMPRESSION_TECH_TYPE = new AttributeType<NsiliImageryDecompressionTech>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<NsiliImageryDecompressionTech> getBinding() {
                return NsiliImageryDecompressionTech.class;
            }
        };

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(IMAGERY_CATEGORY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */, NSILI_IMAGERY_TYPE));

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(CLOUD_COVER_PCT,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(IMAGERY_COMMENTS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(DECOMPRESSION_TECHNIQUE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */, NSILI_IMAGERY_DECOMPRESSION_TECH_TYPE));

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(IMAGE_ID,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.STRING_TYPE));

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(NIIRS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(NUM_BANDS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(NUM_ROWS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        NSILI_IMAGERY_DESCRIPTORS.add(new AttributeDescriptorImpl(NUM_COLS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

    }

    public NsiliImageryMetacardType() {
        super();
        attributeDescriptors.addAll(NSILI_IMAGERY_DESCRIPTORS);

    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }

}
