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

import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.transformer.nitf.AbstractNitfMetacardType;
import org.codice.alliance.transformer.nitf.ExtNitfUtility;
import org.codice.alliance.transformer.nitf.common.AcftbAttribute;
import org.codice.alliance.transformer.nitf.common.AimidbAttribute;
import org.codice.alliance.transformer.nitf.common.CsdidaAttribute;
import org.codice.alliance.transformer.nitf.common.CsexraAttribute;
import org.codice.alliance.transformer.nitf.common.HistoaAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute;
import org.codice.alliance.transformer.nitf.common.PiaimcAttribute;
import org.codice.alliance.transformer.nitf.gmti.IndexedMtirpbAttribute;
import org.codice.alliance.transformer.nitf.gmti.MtirpbAttribute;

import ddf.catalog.data.impl.types.AssociationsAttributes;
import ddf.catalog.data.impl.types.ContactAttributes;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.impl.types.DateTimeAttributes;
import ddf.catalog.data.impl.types.LocationAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.impl.types.ValidationAttributes;

public class ImageMetacardType extends AbstractNitfMetacardType {
    private static final String NAME = "isr.image";

    public static final String COMPLEXITY_LEVEL =
            ExtNitfUtility.EXT_NITF_PREFIX + "complexityLevel";

    public static final String FILE_CLASSIFICATION_AUTHORITY = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileClassificationAuthority";

    public static final String FILE_CLASSIFICATION_AUTHORITY_TYPE = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileClassificationAuthorityType";

    public static final String FILE_CLASSIFICATION_REASON = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileClassificationReason";

    public static final String FILE_CLASSIFICATION_SECURITY_SYSTEM = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileClassificationSecuritySystem";

    public static final String FILE_CLASSIFICATION_TEXT = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileClassificationText";

    public static final String FILE_CODE_WORDS =
            ExtNitfUtility.EXT_NITF_PREFIX + "fileCodewords";

    public static final String FILE_CONTROL_AND_HANDLING = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileControlAndHandling";

    public static final String FILE_DATE_AND_TIME =
            ExtNitfUtility.EXT_NITF_PREFIX + "fileDateAndTime";

    public static final String FILE_DECLASSIFICATION_DATE = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileDeclassificationDate";

    public static final String FILE_DECLASSIFICATION_TYPE = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileDeclassificationType";

    public static final String FILE_RELEASING_INSTRUCTIONS = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileReleasingInstructions";

    public static final String FILE_SECURITY_CLASSIFICATION = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileSecurityClassification";

    public static final String FILE_SECURITY_CONTROL_NUMBER = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileSecurityControlNumber";

    public static final String FILE_SECURITY_SOURCE_DATE = ExtNitfUtility.EXT_NITF_PREFIX
            + "fileSecuritySourceDate";

    public static final String FILE_TITLE =
            ExtNitfUtility.EXT_NITF_PREFIX + "fileTitle";

    public static final String FILE_VERSION =
            ExtNitfUtility.EXT_NITF_PREFIX + "fileVersion";

    public static final String ORIGINATORS_NAME =
            ExtNitfUtility.EXT_NITF_PREFIX + "originatorsName";

    public static final String ORIGINATING_STATION_ID = ExtNitfUtility.EXT_NITF_PREFIX
            + "originatingStationId";

    public static final String FILE_PART_TYPE =
            ExtNitfUtility.EXT_NITF_PREFIX + "filePartType";

    public static final String IMAGE_CATEGORY =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageCategory";

    public static final String IMAGE_COMMENT_1 =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageComment1";

    public static final String IMAGE_COMPRESSION =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageCompression";

    public static final String IMAGE_COORDINATE_REPRESENTATION = ExtNitfUtility.EXT_NITF_PREFIX
            + "imageCoordinateRepresentation";

    public static final String IMAGE_DATE_AND_TIME =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageDateAndTime";

    public static final String IMAGE_IDENTIFIER_1 =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageIdentifier1";

    public static final String IMAGE_MAGNIFICATION =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageMagnification";

    public static final String IMAGE_MODE =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageMode";

    public static final String IMAGE_REPRESENTATION =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageRepresentation";

    public static final String IMAGE_SOURCE =
            ExtNitfUtility.EXT_NITF_PREFIX + "imageSource";

    public static final String NUMBER_OF_BANDS =
            ExtNitfUtility.EXT_NITF_PREFIX + "numberOfBands";

    public static final String NUMBER_OF_BITS_PER_PIXEL =
            ExtNitfUtility.EXT_NITF_PREFIX + "numberOfBitsPerPixel";

    public static final String NUMBER_OF_SIGNIFICANT_COLUMNS_IN_IMAGE =
            ExtNitfUtility.EXT_NITF_PREFIX
                    + "numberOfSignificantColumnsInImage";

    public static final String NUMBER_OF_SIGNIFICANT_ROWS_IN_IMAGE = ExtNitfUtility.EXT_NITF_PREFIX
            + "numberOfSignificantRowsInImage";

    public static final String TARGET_IDENTIFIER =
            ExtNitfUtility.EXT_NITF_PREFIX + "targetIdentifier";

    public ImageMetacardType() {
        super(NAME, null);
        this.initDescriptors();
    }

    @Override
    public void initDescriptors() {
        descriptors.addAll(getDescriptors(GraphicAttribute.values()));
        descriptors.addAll(getDescriptors(ImageAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(LabelAttribute.values()));
        descriptors.addAll(getDescriptors(SymbolAttribute.values()));
        descriptors.addAll(getDescriptors(TextAttribute.values()));
        descriptors.addAll(getDescriptors(NitfHeaderAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(AcftbAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(AimidbAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(IndexedMtirpbAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(MtirpbAttribute.getAttributes()));
        descriptors.addAll(new CoreAttributes().getAttributeDescriptors());
        descriptors.addAll(new AssociationsAttributes().getAttributeDescriptors());
        descriptors.addAll(new ContactAttributes().getAttributeDescriptors());
        descriptors.addAll(new MediaAttributes().getAttributeDescriptors());
        descriptors.addAll(new DateTimeAttributes().getAttributeDescriptors());
        descriptors.addAll(new LocationAttributes().getAttributeDescriptors());
        descriptors.addAll(new ValidationAttributes().getAttributeDescriptors());
        descriptors.addAll(new IsrAttributes().getAttributeDescriptors());
        descriptors.addAll(new SecurityAttributes().getAttributeDescriptors());
        descriptors.addAll(getDescriptors(HistoaAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(PiaimcAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(CsdidaAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(CsexraAttribute.getAttributes()));
    }
}
