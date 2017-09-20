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
package org.codice.alliance.transformer.nitf;

import ddf.catalog.data.AttributeDescriptor;
import ddf.catalog.data.impl.MetacardTypeImpl;
import ddf.catalog.data.impl.types.AssociationsAttributes;
import ddf.catalog.data.impl.types.ContactAttributes;
import ddf.catalog.data.impl.types.CoreAttributes;
import ddf.catalog.data.impl.types.DateTimeAttributes;
import ddf.catalog.data.impl.types.LocationAttributes;
import ddf.catalog.data.impl.types.MediaAttributes;
import ddf.catalog.data.impl.types.ValidationAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.codice.alliance.catalog.core.api.impl.types.IsrAttributes;
import org.codice.alliance.catalog.core.api.impl.types.SecurityAttributes;
import org.codice.alliance.transformer.nitf.common.AcftbAttribute;
import org.codice.alliance.transformer.nitf.common.AimidbAttribute;
import org.codice.alliance.transformer.nitf.common.CsdidaAttribute;
import org.codice.alliance.transformer.nitf.common.CsexraAttribute;
import org.codice.alliance.transformer.nitf.common.HistoaAttribute;
import org.codice.alliance.transformer.nitf.common.IndexedPiaprdAttribute;
import org.codice.alliance.transformer.nitf.common.NitfAttribute;
import org.codice.alliance.transformer.nitf.common.NitfHeaderAttribute;
import org.codice.alliance.transformer.nitf.common.PiaimcAttribute;
import org.codice.alliance.transformer.nitf.common.PiaprdAttribute;
import org.codice.alliance.transformer.nitf.common.PiatgbAttribute;
import org.codice.alliance.transformer.nitf.common.StdidcAttribute;
import org.codice.alliance.transformer.nitf.gmti.IndexedMtirpbAttribute;
import org.codice.alliance.transformer.nitf.gmti.MtirpbAttribute;

public abstract class AbstractNitfMetacardType extends MetacardTypeImpl {

  public AbstractNitfMetacardType(String name, Set<AttributeDescriptor> descriptors) {
    super(name, descriptors);
    setDefaultDescriptors();
  }

  public abstract void initDescriptors();

  public static Set<AttributeDescriptor> getDescriptors(NitfAttribute[] attributes) {
    Set<AttributeDescriptor> descriptors = new HashSet<>();
    for (NitfAttribute attribute : attributes) {
      descriptors.addAll(attribute.getAttributeDescriptors());
    }
    return descriptors;
  }

  public static <T> Set<AttributeDescriptor> getDescriptors(List<NitfAttribute<T>> attributes) {
    return getDescriptors(attributes.toArray(new NitfAttribute[attributes.size()]));
  }

  private void setDefaultDescriptors() {
    descriptors.addAll(getDescriptors(NitfHeaderAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(AcftbAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(AimidbAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(CsdidaAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(CsexraAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(HistoaAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(IndexedMtirpbAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(IndexedPiaprdAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(MtirpbAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(PiaimcAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(PiaprdAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(PiatgbAttribute.getAttributes()));
    descriptors.addAll(getDescriptors(StdidcAttribute.getAttributes()));
    descriptors.addAll(new CoreAttributes().getAttributeDescriptors());
    descriptors.addAll(new AssociationsAttributes().getAttributeDescriptors());
    descriptors.addAll(new ContactAttributes().getAttributeDescriptors());
    descriptors.addAll(new MediaAttributes().getAttributeDescriptors());
    descriptors.addAll(new DateTimeAttributes().getAttributeDescriptors());
    descriptors.addAll(new LocationAttributes().getAttributeDescriptors());
    descriptors.addAll(new ValidationAttributes().getAttributeDescriptors());
    descriptors.addAll(new IsrAttributes().getAttributeDescriptors());
    descriptors.addAll(new SecurityAttributes().getAttributeDescriptors());
  }
}
