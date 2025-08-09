package io.github.drag0n1zed.ai.predicate;

import io.github.drag0n1zed.OverrideMod;
import io.github.drag0n1zed.ai.predicate.factory.VanillaPredicateFactory;
import io.github.drag0n1zed.api.OverrideRegistries;
import io.github.drag0n1zed.api.ai.predicate.IPredicateRegistry;
import io.github.drag0n1zed.api.ai.predicate.PredicateDefinition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PredicateRegistry implements IPredicateRegistry {

    private static final PredicateRegistry INSTANCE = new PredicateRegistry();

    private final Map<String, PredicateDefinition> definitionRegistry = new ConcurrentHashMap<>();

    private PredicateRegistry() {}

    public static void initialize() {
        OverrideRegistries.setPredicateRegistryProvider(INSTANCE);
        VanillaPredicateFactory.registerAll(INSTANCE);
    }

    @Override
    public void register(PredicateDefinition definition) {
        definitionRegistry.put(definition.identifier(), definition);
        OverrideMod.LOGGER.debug("Registered AI Predicate Definition for identifier '{}'", definition.identifier());
    }

    @Override
    public Optional<PredicateDefinition> getDefinition(String identifier) {
        return Optional.ofNullable(definitionRegistry.get(identifier));
    }

    @Override
    public List<PredicateDefinition> getAllDefinitions() {
        return List.copyOf(definitionRegistry.values());
    }
}