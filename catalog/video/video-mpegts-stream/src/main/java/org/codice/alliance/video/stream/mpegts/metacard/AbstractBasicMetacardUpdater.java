package org.codice.alliance.video.stream.mpegts.metacard;

import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Copies child attribute to parent if a specific condition is met.
 */
public abstract class AbstractBasicMetacardUpdater implements MetacardUpdater {

    private final String attributeName;

    protected AbstractBasicMetacardUpdater(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public final void update(Metacard parent, Metacard child) {
        if (isChildAvailable(child) && isCondition(parent, child)) {
            parent.setAttribute(new AttributeImpl(attributeName,
                    child.getAttribute(attributeName)
                            .getValue()));
        }
    }

    private boolean isChildAvailable(Metacard child) {
        return child.getAttribute(attributeName) != null;
    }

    /**
     * The child is guaranteed to already have the attribute.
     *
     * @param parent parent metacard
     * @param child  child metacard
     * @return true if the parent should be set with the child's value
     */
    protected abstract boolean isCondition(Metacard parent, Metacard child);

}
