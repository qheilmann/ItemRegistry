package dev.qheilmann.itemregistry;

import net.kyori.adventure.key.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

/**
 * Registry that maps Adventure {@link Key}s to {@link ItemStackCreator}s.
 * TODO: extend as needed (unregister, iterable, etc.)
 */
public class ItemRegistry {

    private final Map<Key, ItemStackCreator> registry = new HashMap<>();

    /** Creates a new, empty ItemRegistry. */
    public ItemRegistry() {
        // No initialization needed
    }

    /**
     * Registers a creator under the given key.
     *
     * @param key the unique key to identify the creator
     * @param creator the ItemStackCreator to register
     * @throws IllegalArgumentException if the key is already registered
     */
    public void register(Key key, ItemStackCreator creator) {
        if (registry.containsKey(key)) {
            throw new IllegalArgumentException("Key already registered: " + key.asString());
        }
        registry.put(key, creator);
    }

    /**
     * Creates an {@link ItemStack} for the given key, or empty if not registered.
     * 
     * @param key the key of the creator to use
     * @return an Optional containing the created ItemStack, or empty if the key is not registered
     */
    public Optional<ItemStack> create(Key key) {
        ItemStackCreator creator = registry.get(key);
        return creator == null ? Optional.empty() : Optional.of(creator.create());
    }

    /** 
     * Returns whether a creator is registered for this key. 
     * @param key the key to check
     * @return true if a creator is registered for the key, false otherwise
     */
    public boolean isRegistered(Key key) {
        return registry.containsKey(key);
    }

    /**
     * Returns an unmodifiable set of all registered keys.
     * @return a set of registered keys
     */
    public Set<Key> registeredKeys() {
        return Collections.unmodifiableSet(registry.keySet());
    }
}

