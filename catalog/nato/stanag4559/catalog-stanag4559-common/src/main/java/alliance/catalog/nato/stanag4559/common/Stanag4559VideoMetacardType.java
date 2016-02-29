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
package alliance.catalog.nato.stanag4559.common;

import java.util.HashSet;
import java.util.Set;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.AttributeType;
import ddf.catalog.data.impl.AttributeDescriptorImpl;
import ddf.catalog.data.impl.BasicTypes;

public class Stanag4559VideoMetacardType extends Stanag4559MetacardType {
    public static final String METACARD_TYPE_NAME = STANAG4559_METACARD_TYPE_PREFIX+".video."+STANAG4559_METACARD_TYPE_POSTFIX;

    /**
     * The bit rate of the motion imagery product when being streamed.
     */
    public static final String AVG_BIT_RATE = "averageBitRate";

    /**
     * An indicator of the specific category of motion image (information about the bands of visible
     * or infrared region of electromagnetic continuum being used). The specific category of an image
     * reveals its intended use or the nature of its collector.
     */
    public static final String CATEGORY = "category";

    /**
     * A code that indicates the manner in which the motion imagery was encoded.
     */
    public static final String ENCODING_SCHEME = "encodingScheme";

    /**
     * The standard rate (in frames per second (FPS)) at which the motion imagery was captured.
     */
    public static final String FRAME_RATE = "frameRate";

    /**
     * The frame vertical resolution.
     */
    public static final String NUM_ROWS = "numRows";

    /**
     * The frame horizontal resolution.
     */
    public static final String NUM_COLS = "numCols";

    /**
     * A code that indicates the manner in which the metadata for the motion imagery was encoded.
     */
    public static final String METADATA_ENCODING_SCHEME = "metadataEncodingScheme";

    /**
     * From [AEDP-8]: The "Motion Imagery Systems (Spatial and Temporal) Matrix" (MISM) defines an
     * ENGINEERING GUIDELINE for the simple identification of broad categories of Motion Imagery Systems.
     * The intent of the MISM is to give user communities an easy to use, common shorthand reference
     * language to describe the fundamental technical capabilities of NATO motion imagery systems.
     */
    public static final String MISM_LEVEL = "mismLevel";

    /**
     * Indicate if progressive or interlaced scans are being applied.
     */
    public static final String SCANNING_MODE = "scanningMode";

    /**
     * Metacard AttributeType for the STANAG-4559 Video Category Type.
     */
    public static final AttributeType<Stanag4559VideoCategoryType> STANAG_4559_VIDEO_CAT_TYPE;

    /**
     * Metacard AttributeType for the STANAG-4559 Video Encoding Type.
     */
    public static final AttributeType<Stanag4559VideoEncodingScheme> STANAG_4559_VIDEO_ENC_TYPE;

    /**
     * Metacard AttributeType for the STANAG-4559 Metadata Encoding Scheme Type.
     */
    public static final AttributeType<Stanag4559MetadataEncodingScheme> STANAG_4559_METADATA_ENC_TYPE;

    /**
     * Metacard AttributeType for the STANAG-4559 Scanning Mode.
     */
    public static final AttributeType<Stanag4559ScanningMode> STANAG_4559_SCANNING_MODE_TYPE;

    private static final Set<AttributeDescriptor> STANAG4559_VIDEO_DESCRIPTORS = new HashSet<>();

    static {
        STANAG_4559_VIDEO_CAT_TYPE = new AttributeType<Stanag4559VideoCategoryType>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559VideoCategoryType> getBinding() {
                return Stanag4559VideoCategoryType.class;
            }
        };

        STANAG_4559_VIDEO_ENC_TYPE = new AttributeType<Stanag4559VideoEncodingScheme>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559VideoEncodingScheme> getBinding() {
                return Stanag4559VideoEncodingScheme.class;
            }
        };

        STANAG_4559_METADATA_ENC_TYPE = new AttributeType<Stanag4559MetadataEncodingScheme>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559MetadataEncodingScheme> getBinding() {
                return Stanag4559MetadataEncodingScheme.class;
            }
        };

        STANAG_4559_SCANNING_MODE_TYPE = new AttributeType<Stanag4559ScanningMode>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AttributeFormat getAttributeFormat() {
                return AttributeFormat.STRING;
            }

            @Override
            public Class<Stanag4559ScanningMode> getBinding() {
                return Stanag4559ScanningMode.class;
            }
        };

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(AVG_BIT_RATE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.FLOAT_TYPE));

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(CATEGORY,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_VIDEO_CAT_TYPE));

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(ENCODING_SCHEME,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_VIDEO_ENC_TYPE));

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(FRAME_RATE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.FLOAT_TYPE));

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(NUM_ROWS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(NUM_COLS,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(METADATA_ENCODING_SCHEME,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_METADATA_ENC_TYPE));

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(MISM_LEVEL,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                BasicTypes.INTEGER_TYPE));

        STANAG4559_VIDEO_DESCRIPTORS.add(new AttributeDescriptorImpl(SCANNING_MODE,
                true /* indexed */,
                true /* stored */,
                false /* tokenized */,
                false /* multivalued */,
                STANAG_4559_SCANNING_MODE_TYPE));
    }

    public Stanag4559VideoMetacardType() {
        super();
        attributeDescriptors.addAll(STANAG4559_VIDEO_DESCRIPTORS);
    }

    @Override
    public String getName() {
        return METACARD_TYPE_NAME;
    }
}
