package dev.qheilmann.itemregistry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import dev.qheilmann.itemregistry.sources.VanillaItemSource;

import java.util.Set;

/**
 * Represents a source of items that can resolve {@link Key}s to {@link ItemProvider}s.
 * <p>
 * ItemSources do not need to be namespace-exclusive. Multiple sources can provide
 * items with the same namespace, and one source can provide items across multiple
 * namespaces. Key conflicts are resolved by source registration order in the registry.
 * <p>
 * Common implementations include:
 * <ul>
 * <li>{@link VanillaItemSource} - provides all vanilla Minecraft items</li>
 * <li>{@link SimpleItemSource} - a simple mutable source for custom items</li>
 * </ul>
 * @see ItemRegistry
 * @see ItemProvider
 */
@NullMarked
public interface ItemSource extends Keyed {

    /** The singleton instance of the vanilla item source. */
    public static final VanillaItemSource VANILLA_SOURCE = VanillaItemSource.getInstance();

    /**
     * Resolves the given key to an {@link ItemProvider}, or null if not found.
     * <p>
     * This method is called dynamically by the registry each time an item is requested.
     * Implementations should be efficient as this may be called frequently.
     *
     * @param key the item key to resolve
     * @return the provider for this key, or null if this source doesn't provide it
     */
    @Nullable ItemProvider resolve(Key key);

    /**
     * Returns a snapshot of all keys currently available from this source.
     * <p>
     * The returned set represents the current state and may become outdated
     * if the source is modified after this method returns. The registry does
     * not cache this result.
     * <p>
     * Implementations may return an empty set if enumerating keys is not
     * practical or efficient (e.g., combinatorial explosion).
     *
     * @return an immutable set of currently available keys (may be empty)
     */
    Set<Key> registeredKeys();
}
