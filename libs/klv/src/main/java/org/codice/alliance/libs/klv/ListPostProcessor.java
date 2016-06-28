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
package org.codice.alliance.libs.klv;

import java.util.List;
import java.util.Map;

import org.codice.ddf.libs.klv.KlvDataElement;

public class ListPostProcessor implements PostProcessor {

    private final List<PostProcessor> postProcessorList;

    public ListPostProcessor(List<PostProcessor> postProcessorList) {
        this.postProcessorList = postProcessorList;
    }

    @Override
    public void postProcess(Map<String, KlvDataElement> dataElements,
            Map<String, KlvHandler> handlers) {
        postProcessorList.forEach(postProcessor -> postProcessor.postProcess(dataElements,
                handlers));
    }

    @Override
    public String toString() {
        return "ListPostProcessor{" +
                "postProcessorList=" + postProcessorList +
                '}';
    }
}
