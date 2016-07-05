package org.codice.alliance.libs.klv;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

/**
 * Union the values of multiple stanag fields into a single metacard attribute.
 */
public class UnionKlvProcessor extends AbstractMultiKlvProcessor {

    private final String attributeName;

    public UnionKlvProcessor(List<String> stanagFieldNames, String attributeName) {
        super(stanagFieldNames);
        this.attributeName = attributeName;
    }

    @Override
    protected final void doProcess(List<Attribute> attributes, Metacard metacard) {
        List<Serializable> serializables = attributes.stream()
                .filter(a -> a.getValue() != null)
                .flatMap(a -> a.getValues()
                        .stream())
                .distinct()
                .collect(Collectors.toList());
        if (serializables.isEmpty()) {
            metacard.setAttribute(new AttributeImpl(attributeName, serializables));
        }
    }

    @Override
    public final void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
