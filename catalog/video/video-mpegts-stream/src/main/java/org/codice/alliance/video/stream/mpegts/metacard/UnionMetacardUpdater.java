package org.codice.alliance.video.stream.mpegts.metacard;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Set the parent to the union of the child and parent values.
 */
public class UnionMetacardUpdater implements MetacardUpdater {

    private final String attributeName;

    public UnionMetacardUpdater(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public final void update(Metacard parent, Metacard child) {

        List<Serializable> serializables = Stream.of(parent, child)
                .map(this::getAttribute)
                .filter(Objects::nonNull)
                .map(Attribute::getValues)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        if (!serializables.isEmpty()) {
            parent.setAttribute(new AttributeImpl(attributeName, serializables));
        }
    }

    private Attribute getAttribute(Metacard metacard) {
        return metacard.getAttribute(attributeName);
    }

    @Override
    public final void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
