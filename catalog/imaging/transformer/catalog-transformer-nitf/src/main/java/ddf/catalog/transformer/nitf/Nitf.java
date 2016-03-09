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

/**
 * Declaration of "nitf" metacard constants. The actual definition of the metacard is handled
 * by the xml files.
 */
public interface Nitf {
    String NAME = "nitf";

    String NITF_VERSION = "version";

    String FILE_DATE_TIME = "fileDateTime";

    String FILE_TITLE = "fileTitle";

    /* File Size in Bytes*/ String FILE_SIZE = "fileSize";

    String COMPLEXITY_LEVEL = "complexityLevel";

    String ORIGINATOR_NAME = "originatorName";

    String ORIGINATING_STATION_ID = "originatingStationId";

    String IMAGE_ID = "imageId";

    String ISOURCE = "isource";

    String NUMBER_OF_ROWS = "numberOfRows";

    String NUMBER_OF_COLUMNS = "numberOfColumns";

    String NUMBER_OF_BANDS = "numberOfBands";

    String NUMBER_OF_MULTISPECTRAL_BANDS = "numberOfMultispectralBands";

    String REPRESENTATION = "representation";

    String SUBCATEGORY = "subcategory";

    String BITS_PER_PIXEL_PER_BAND = "bitsPerPixelPerBand";

    String IMAGE_MODE = "imageMode";

    String COMPRESSION = "compression";

    String RATE_CODE = "rateCode";

    String TARGET_ID = "targetId";

    String COMMENT = "comment";

    /* NITF Security */ String CODE_WORDS = "codeWords";

    String CONTROL_CODE = "controlCode";

    String RELEASE_INSTRUCTION = "releaseInstruction";

    String CONTROL_NUMBER = "controlNumber";

    String CLASSIFICATION_SYSTEM = "system";

    String CLASSIFICATION_AUTHORITY = "authority";

    String CLASSIFICATION_AUTHORITY_TYPE = "authorityType";

    String CLASSIFICATION_TEXT = "text";

    String CLASSIFICATION_REASON = "reason";

    String CLASSIFICATION_DATE = "classificationDate";

    String DECLASSIFICATION_TYPE = "declassificationType";

    String DECLASSIFICATION_DATE = "declassificationDate";

    String SECURITY = "";

}
