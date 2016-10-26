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
package org.codice.alliance.transformer.nitf.common;

import java.util.Arrays;
import java.util.List;

import org.codice.alliance.transformer.nitf.gmti.MtirpbAttribute;
import org.codice.imaging.nitf.core.tre.Tre;

public enum TreDescriptor {
    ACFTB(AcftbAttribute.getAttributes()),
    AIMIDB(AimidbAttribute.getAttributes()),
    MTIRPB(MtirpbAttribute.getAttributes()),
    CSEXRA(CsexraAttribute.getAttributes()),
    PIAIMC(PiaimcAttribute.getAttributes()),
    CSDIDA(CsdidaAttribute.getAttributes()),
    HISTOA(HistoaAttribute.getAttributes());

    private List<NitfAttribute<Tre>> nitfAttributes;

    TreDescriptor(List<NitfAttribute<Tre>> nitfAttributes) {
        this.nitfAttributes = nitfAttributes;
    }

    public static TreDescriptor forName(String name) {
        return Arrays.stream(TreDescriptor.values())
                .filter(treDescriptor -> treDescriptor.name()
                        .equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<NitfAttribute<Tre>> getValues() {
        return nitfAttributes;
    }
}
