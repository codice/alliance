package org.codice.alliance.libs.klv;

import java.util.Collections;
import java.util.List;

import org.codice.alliance.libs.stanag4609.Stanag4609TransportStreamParser;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;
import ddf.catalog.data.impl.AttributeImpl;

public class SensorAltitudeKlvProcessor extends AbstractMultiKlvProcessor {
    public SensorAltitudeKlvProcessor() {
        super(Collections.singleton(Stanag4609TransportStreamParser.SENSOR_TRUE_ALTITUDE));
    }

    @Override
    protected void doProcess(List<Attribute> attributes, Metacard metacard) {
        attributes.stream()
                .flatMap(attribute -> attribute.getValues()
                        .stream())
                .filter(Double.class::isInstance)
                .mapToDouble(Double.class::cast)
                .average()
                .ifPresent(doubleValue -> setMetacard(doubleValue, metacard));
    }

    private void setMetacard(double value, Metacard metacard) {
        metacard.setAttribute(new AttributeImpl(AttributeNameConstants.SENSOR_TRUE_ALTITUDE,
                value));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
