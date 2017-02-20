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
package org.codice.alliance.video.stream.mpegts;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.codice.alliance.libs.klv.GeometryOperator;
import org.codice.alliance.video.stream.mpegts.netty.UdpStreamProcessor;

import ddf.catalog.data.Metacard;

/**
 * This class supplies data used by different parts of the stream processor.
 */
public class Context {

    private static final ThreadLocal<Boolean> IN_MODIFY_PARENT = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private final UdpStreamProcessor udpStreamProcessor;

    /**
     * Certain metacard fields in the parent are updated when the stream ends (either manually or
     * by timeout). When the parent is updated, this field is set to FALSE. If a child video chunk
     * is ingested, this fields is set to TRUE, indicating that the parent is now out of sync with
     * the child chunks. This also prevents multiple calls to the stream-end hooks from causing
     * unnecessary updates to the parent metacard. Code that updates the parent and child
     * metacards should perform their actions through {@link #modifyParentOrChild(Function)}.
     */
    private final AtomicBoolean isParentDirty = new AtomicBoolean(false);

    private Optional<Metacard> parentMetacard = Optional.empty();

    private final GeometryOperator.Context geometryOperatorContext = new GeometryOperator.Context();

    /**
     * @param udpStreamProcessor must be non-null
     */
    public Context(UdpStreamProcessor udpStreamProcessor) {
        notNull(udpStreamProcessor, "udpStreamProcessor must be non-null");
        this.udpStreamProcessor = udpStreamProcessor;
    }

    public UdpStreamProcessor getUdpStreamProcessor() {
        return udpStreamProcessor;
    }

    public Optional<Metacard> getParentMetacard() {
        return parentMetacard;
    }

    /**
     * @param parentMetacard must be non-null
     */
    public void setParentMetacard(Metacard parentMetacard) {
        notNull(parentMetacard, "parentMetacard must be non-null");
        this.parentMetacard = Optional.of(parentMetacard);
    }

    public GeometryOperator.Context getGeometryOperatorContext() {
        return geometryOperatorContext;
    }

    /**
     * Callers that wish to modify the parent or child metacards should perform their actions
     * with the {@link Function} passed into this method. The function will be passed an
     * {@link AtomicBoolean} that is TRUE if the parent metacard is in sync with the child
     * metacards. Callers should not use the AtomicBoolean outside the context of this method
     * call. Callers may modify the AtomicBoolean within the context of this method call.
     *
     * @param function must be non-null
     * @param <T>      the return type of this method and the Function
     * @return the return value of the Function
     */
    public <T> T modifyParentOrChild(Function<AtomicBoolean, T> function) {
        synchronized (isParentDirty) {
            return function.apply(isParentDirty);
        }
    }

}
