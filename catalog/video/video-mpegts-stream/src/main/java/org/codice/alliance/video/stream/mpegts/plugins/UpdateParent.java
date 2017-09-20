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
package org.codice.alliance.video.stream.mpegts.plugins;

import ddf.catalog.data.Metacard;
import ddf.catalog.operation.UpdateRequest;
import ddf.catalog.operation.impl.UpdateRequestImpl;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.codice.alliance.video.stream.mpegts.Context;
import org.codice.alliance.video.stream.mpegts.framework.CatalogUpdateRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the parent metacard by calling an {@link UpdateField} and then by calling the catalog
 * framework.
 */
public class UpdateParent implements FindChildrenStreamEndPlugin.Handler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateParent.class);

  private static final long INITIAL_RETRY_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

  private static final long MAX_RETRY_MILLISECONDS = TimeUnit.MINUTES.toMillis(5);

  private final UpdateField updateField;

  private CatalogUpdateRetry catalogUpdateRetry = new CatalogUpdateRetry();

  public UpdateParent(UpdateField updateField) {
    this.updateField = updateField;
  }

  public void setCatalogUpdateRetry(CatalogUpdateRetry catalogUpdateRetry) {
    this.catalogUpdateRetry = catalogUpdateRetry;
  }

  @Override
  public void handle(Context context, Metacard parent, List<Metacard> children) {
    updateField.updateField(parent, children, context);
  }

  @Override
  public void end(Context context, Metacard parentMetacard) {

    updateField.end(parentMetacard, context);

    UpdateRequest updateRequest = createUpdateRequest(parentMetacard);

    update(context, parentMetacard, updateRequest);
  }

  private void update(Context context, Metacard parentMetacard, UpdateRequest updateRequest) {
    catalogUpdateRetry.submitUpdateRequestWithRetry(
        context.getUdpStreamProcessor().getCatalogFramework(),
        updateRequest,
        context.getUdpStreamProcessor().getMetacardUpdateInitialDelay(),
        INITIAL_RETRY_MILLISECONDS,
        MAX_RETRY_MILLISECONDS,
        update -> {
          LOGGER.debug("updated parent metacard: newMetacard={}", update.getNewMetacard().getId());
          context.setParentMetacard(update.getNewMetacard());
        });
  }

  private UpdateRequest createUpdateRequest(Metacard parentMetacard) {
    return new UpdateRequestImpl(parentMetacard.getId(), parentMetacard);
  }

  /**
   * Update a field in the parent metacard. {@link #updateField} will be called with batches of
   * child metacards. After all the batches have been submitted, then {@link #end(Metacard,
   * Context)} will be called.
   */
  interface UpdateField {
    /**
     * Called for each batch of child metacards to be processed. If this method is called after
     * {@link #end(Metacard, Context)} is called, then an {@link IllegalStateException} will be
     * thrown.
     *
     * @param parent not-null
     * @param children not-null
     * @param context not-null
     * @throws IllegalStateException thrown if this method is called after {@link #end(Metacard,
     *     Context)} is called
     */
    void updateField(Metacard parent, List<Metacard> children, Context context);

    /**
     * Called after the last batch is submitted to {@link #updateField(Metacard, List, Context)}.
     *
     * @param parent not-null
     * @param context not-null
     */
    void end(Metacard parent, Context context);
  }

  /**
   * Implementations should be derived from this class to enforce the correct call order of the
   * methods.
   */
  public abstract static class BaseUpdateField implements UpdateField {

    private boolean isEndCalled = false;

    @Override
    public final void updateField(Metacard parent, List<Metacard> children, Context context) {
      if (isEndCalled) {
        throw new IllegalStateException("'updateField' was called after 'end' was called");
      }
      doUpdateField(parent, children, context);
    }

    @Override
    public final void end(Metacard parent, Context context) {
      try {
        doEnd(parent, context);
      } finally {
        isEndCalled = true;
      }
    }

    protected abstract void doEnd(Metacard parent, Context context);

    protected abstract void doUpdateField(
        Metacard parent, List<Metacard> children, Context context);
  }
}
