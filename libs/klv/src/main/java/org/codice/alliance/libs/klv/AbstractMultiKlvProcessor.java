package org.codice.alliance.libs.klv;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ddf.catalog.data.Attribute;
import ddf.catalog.data.Metacard;

public abstract class AbstractMultiKlvProcessor implements KlvProcessor {

    private final List<String> stanagFieldNames;

    public AbstractMultiKlvProcessor(List<String> stanagFieldNames) {
        this.stanagFieldNames = stanagFieldNames;
    }

    @Override
    public void process(Map<String, KlvHandler> handlers, Metacard metacard,
            Configuration configuration) {
        List<Attribute> attributes = findKlvHandlers(handlers).stream()
                .map(KlvHandler::asAttribute)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        doProcess(attributes, metacard);
    }

    protected abstract void doProcess(List<Attribute> attributes, Metacard metacard);

    private List<KlvHandler> findKlvHandlers(Map<String, KlvHandler> handlers) {
        return handlers.entrySet()
                .stream()
                .filter(entry -> stanagFieldNames.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .distinct()
                .collect(Collectors.toList());
    }
}
