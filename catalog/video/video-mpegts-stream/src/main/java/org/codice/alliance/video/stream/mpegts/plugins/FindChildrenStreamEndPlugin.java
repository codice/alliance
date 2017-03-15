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
package org.codice.alliance.video.stream.mpegts.plugins;

import java.util.List;
import java.util.stream.Collectors;

import org.codice.alliance.video.stream.mpegts.Context;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.Result;
import ddf.catalog.data.types.Associations;
import ddf.catalog.data.types.Core;
import ddf.catalog.federation.FederationException;
import ddf.catalog.filter.FilterBuilder;
import ddf.catalog.filter.impl.SortByImpl;
import ddf.catalog.operation.QueryRequest;
import ddf.catalog.operation.QueryResponse;
import ddf.catalog.operation.impl.QueryImpl;
import ddf.catalog.operation.impl.QueryRequestImpl;
import ddf.catalog.source.SourceUnavailableException;
import ddf.catalog.source.UnsupportedQueryException;

/**
 * Find the children of the parent metacard and process the children in batches. The children will be
 * processed in the order in which they were originally created. If an exception occurs during when a
 * batch is being retrieved from the CatalogFramework or from a batch handler, then the {@link Handler#end(Context, Metacard)}
 * will not be called.
 */
public class FindChildrenStreamEndPlugin implements StreamEndPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(FindChildrenStreamEndPlugin.class);

    private static final int BATCH_SIZE = 50;

    private final Factory factory;

    private final FilterBuilder filterBuilder;

    public FindChildrenStreamEndPlugin(FilterBuilder filterBuilder, Factory factory) {
        this.filterBuilder = filterBuilder;
        this.factory = factory;
    }

    public Factory getFactory() {
        return factory;
    }

    @Override
    public void streamEnded(Context context) {

        synchronized (context.isParentDirty()) {

            if (!context.isParentDirty()
                    .get()) {
                return;
            }

            context.getParentMetacard()
                    .ifPresent(parentMetacard -> handleParentMetacard(context, parentMetacard));

            context.isParentDirty()
                    .set(false);
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    private void handleParentMetacard(Context context, Metacard parentMetacard) {

        Handler handler = factory.build();

        try {

            Filter filter = filterBuilder.attribute(Associations.DERIVED)
                    .is()
                    .equalTo()
                    .text(parentMetacard.getId());

            int startIndex = 1;
            int expectedReturnCount = 0;

            do {
                QueryRequest queryRequest = new QueryRequestImpl(new QueryImpl(filter,
                        startIndex,
                        BATCH_SIZE,
                        new SortByImpl(Core.METACARD_CREATED, SortOrder.ASCENDING),
                        true,
                        0));

                QueryResponse queryResponse = context.getUdpStreamProcessor()
                        .getCatalogFramework()
                        .query(queryRequest);

                if (startIndex == 1) {
                    expectedReturnCount = Math.toIntExact(queryResponse.getHits());
                }

                List<Metacard> batch = queryResponse.getResults()
                        .stream()
                        .map(Result::getMetacard)
                        .collect(Collectors.toList());

                handler.handle(context, parentMetacard, batch);

                startIndex += batch.size();

            } while (startIndex <= expectedReturnCount);

            handler.end(context, parentMetacard);

        } catch (UnsupportedQueryException | SourceUnavailableException | FederationException e) {
            LOGGER.debug("unable to find the children for a parent metacard: metacardId={}",
                    parentMetacard.getId(),
                    e);
        }
    }

    /**
     * Handle the children.
     */
    public interface Handler {

        /**
         * Handle each batch of child metacards.
         *
         * @param context  not-null
         * @param parent   the parent metacard, not-null
         * @param children a batch of children, not-null, can be empty
         */
        void handle(Context context, Metacard parent, List<Metacard> children);

        /**
         * This is called after all the batches have been passed to {@link #handle(Context, Metacard, List)}.
         */
        void end(Context context, Metacard parentMetacard);

    }

    /**
     * Factory for building {@link Handler}
     */
    public interface Factory {
        Handler build();

        void accept(Visitor visit);

        interface Visitor {
            void visit(UpdateParentFactory factory);
        }
    }

}
