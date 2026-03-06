package dev.qheilmann.itemregistry;

import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Central registry that manages multiple {@link ItemSource}s and resolves
 * item keys to {@link ItemStack} instances.
 * <p>
 * <strong>Thread Safety:</strong><br>
 * All public methods are synchronized to ensure thread-safe access from
 * multiple threads (e.g., async tasks, event handlers).
 */
@NullMarked
public class ItemRegistry {

    /**
     * Map of source keys to ItemSource instances.
     * LinkedHashMap preserves insertion order for predictable resolution.
     */
    private final Map<Key, ItemSource> sources = new LinkedHashMap<>();

    /**
     * Creates a new, empty ItemRegistry with no sources.
     */
    public ItemRegistry() {
        // No initialization needed
    }

    /**
     * Registers an {@link ItemSource} under the given key.
     * <p>
     * The source key is used to identify and unregister the source later.
     * 
     * @param source the ItemSource to register
     */
    public synchronized void registerSource(ItemSource source) {
        sources.put(source.key(), source);
    }

    /**
     * Unregisters the {@link ItemSource} with the given key.
     * <p>
     * If no source is registered under this key, this method does nothing.
     *
     * @param sourceKey the key of the source to unregister
     * @return the removed ItemSource, or null if no source was registered with this key
     */
    public synchronized ItemSource unregisterSource(Key sourceKey) {
        return sources.remove(sourceKey);
    }

    /**
     * Checks if a source is registered under the given key.
     *
     * @param sourceKey the source key to check
     * @return true if a source is registered with this key
     */
    public synchronized boolean hasSource(Key sourceKey) {
        return sources.containsKey(sourceKey);
    }

    /**
     * Returns the {@link ItemSource} registered under the given key.
     *
     * @param sourceKey the source key to look up
     * @return the ItemSource, or null if no source is registered with this key
     */
    public synchronized @Nullable ItemSource getSource(Key sourceKey) {
        return sources.get(sourceKey);
    }

    /**
     * Returns an unmodifiable set of all registered source keys.
     * <p>
     * The returned set is a snapshot and will not reflect future changes.
     *
     * @return a set of source keys currently registered
     */
    public synchronized Set<Key> getSourceKeys() {
        return Collections.unmodifiableSet(sources.keySet());
    }

    /**
     * Creates an {@link ItemStack} for the given item key by querying registered sources.
     * <p>
     * Sources are queried in registration order. The first source that returns
     * a non-null {@link ItemProvider} for this key will be used to create the item.
     * <p>
     * Returns null if no registered source can resolve the key.
     *
     * @param itemKey the key of the item to create (e.g., "minecraft:diamond")
     * @return a new ItemStack instance, or null if the key cannot be resolved
     */
    public synchronized @Nullable ItemStack create(Key itemKey) {
        for (ItemSource source : sources.values()) {
            ItemProvider provider = source.resolve(itemKey);
            if (provider != null) {
                return provider.create();
            }
        }
        return null;
    }

    /**
     * Checks if any registered source can resolve the given item key.
     * <p>
     * This is equivalent to {@code create(itemKey) != null} but may be
     * more efficient if you only need to check existence.
     *
     * @param itemKey the item key to check
     * @return true if at least one source can resolve this key
     */
    public synchronized boolean canResolve(Key itemKey) {
        for (ItemSource source : sources.values()) {
            if (source.resolve(itemKey) != null) {
                return true;
            }
        }
        return false;
    }
}

