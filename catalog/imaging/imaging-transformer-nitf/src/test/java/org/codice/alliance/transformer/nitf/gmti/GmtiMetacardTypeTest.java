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
package org.codice.alliance.transformer.nitf.gmti;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.types.AssociationsAttributes;
import ddf.catalog.data.impl.types.ContactAttributes;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.impl.types.DateTimeAttributes;
import ddf.catalog.data.impl.types.LocationAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.impl.types.ValidationAttributes;
import java.util.Set;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.transformer.nitf.AbstractNitfMetacardType;
import org.codice.alliance.transformer.nitf.common.AcftbAttribute;
import org.codice.alliance.transformer.nitf.common.AimidbAttribute;
import org.codice.alliance.transformer.nitf.common.CsdidaAttribute;
import org.codice.alliance.transformer.nitf.common.CsexraAttribute;
import org.codice.alliance.transformer.nitf.common.ExpltbAttribute;
import org.codice.alliance.transformer.nitf.common.HistoaAttribute;
import org.codice.alliance.transformer.nitf.common.IndexedPiaprdAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute;
import org.codice.alliance.transformer.nitf.common.PiaimcAttribute;
import org.codice.alliance.transformer.nitf.common.PiaprdAttribute;
import org.codice.alliance.transformer.nitf.common.PiatgbAttribute;
import org.codice.alliance.transformer.nitf.common.StdidcAttribute;
import org.codice.alliance.transformer.nitf.complex.CmetaaAttribute;
import org.junit.Test;

public class GmtiMetacardTypeTest {

  @Test
  public void testGmtiAttributes() {
    GmtiMetacardType gmtiCardType = new GmtiMetacardType();
    Set<AttributeDescriptor> descriptors =
        AbstractNitfMetacardType.getDescriptors(NitfHeaderAttribute.getAttributes());
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(AcftbAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(AimidbAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(ExpltbAttribute.getAttributes()));
    descriptors.addAll(
        AbstractNitfMetacardType.getDescriptors(IndexedMtirpbAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(MtirpbAttribute.getAttributes()));
    descriptors.addAll(new CoreAttributes().getAttributeDescriptors());
    descriptors.addAll(new AssociationsAttributes().getAttributeDescriptors());
    descriptors.addAll(new ContactAttributes().getAttributeDescriptors());
    descriptors.addAll(new MediaAttributes().getAttributeDescriptors());
    descriptors.addAll(new DateTimeAttributes().getAttributeDescriptors());
    descriptors.addAll(new LocationAttributes().getAttributeDescriptors());
    descriptors.addAll(new ValidationAttributes().getAttributeDescriptors());
    descriptors.addAll(new IsrAttributes().getAttributeDescriptors());
    descriptors.addAll(new SecurityAttributes().getAttributeDescriptors());
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(PiaimcAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(CmetaaAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(CsdidaAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(CsexraAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(HistoaAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(PiatgbAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(PiaprdAttribute.getAttributes()));
    descriptors.addAll(
        AbstractNitfMetacardType.getDescriptors(IndexedPiaprdAttribute.getAttributes()));
    descriptors.addAll(AbstractNitfMetacardType.getDescriptors(StdidcAttribute.getAttributes()));
    assertThat(
        gmtiCardType.getAttributeDescriptors(),
        containsInAnyOrder(descriptors.toArray(new AttributeDescriptor[0])));
  }
}
