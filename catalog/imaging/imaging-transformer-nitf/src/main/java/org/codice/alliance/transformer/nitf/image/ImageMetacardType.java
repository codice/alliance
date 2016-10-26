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
        descriptors.addAll(getDescriptors(CsdidaAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(CsexraAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(PiaimcAttribute.getAttributes()));
        descriptors.addAll(getDescriptors(HistoaAttribute.getAttributes()));
    }
}
