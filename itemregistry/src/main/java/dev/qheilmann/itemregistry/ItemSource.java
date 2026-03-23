package dev.qheilmann.itemregistry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.inventory.ItemStack;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import dev.qheilmann.itemregistry.sources.StrictItemSource;
import dev.qheilmann.itemregistry.sources.PdcItemSource;
import dev.qheilmann.itemregistry.sources.VanillaItemSource;

import java.util.Set;

/**
 * Represents a source of items that can create {@link ItemStack}s from {@link Key}s,
 * and resolve {@link Key}s from {@link ItemStack}s.
 * <p>
 * ItemSources do not need to be namespace-exclusive. Multiple sources can provide
 * items with the same namespace, and one source can provide items across multiple
 * namespaces. Key conflicts are resolved by source registration order in the registry.
 * <p>
 * Common implementations include:
 * <ul>
 * <li>{@link VanillaItemSource} - provides all vanilla Minecraft items</li>
 * <li>{@link PdcItemSource} - PDC-based resolution for persistent item tracking (recommended for mutable custom items, like anvil renaming, enchanting)</li>
 * <li>{@link StrictItemSource} - strict similarity-based resolution (for cases where item should stay consistent and are immutable)</li>
 * </ul>
 * @see ItemRegistry
 */
@NullMarked
public interface ItemSource extends Keyed {

    /** The singleton instance of the vanilla item source. */
    public static final VanillaItemSource VANILLA_SOURCE = VanillaItemSource.getInstance();

    /**
     * Creates an {@link ItemStack} for the given key, or null if not found.
     * <p>
     * Implementations should return a fresh mutable ItemStack instance on each call
     * so callers can safely modify the returned item.
     * <p>
     * Implementations should be fast to return null when the key is not provided by
     * this source to allow efficient short-circuiting in the registry.
     *
     * @param key the item key to create
     * @return a fresh ItemStack for this key, or null if this source doesn't provide it
     */
    @Nullable ItemStack createItem(Key key);

    /**
     * Resolves the given ItemStack to its key, or null if this source cannot map it.
     *
     * @param itemStack the item stack to resolve
     * @return the key for this item stack, or null if this source doesn't recognize it
     */
    @Nullable Key resolveKey(ItemStack itemStack);

    /**
     * Checks if this source can resolve the given item key.
     * <p>
     * Implementations note: The default implementation delegates to {@link #createItem(Key)} and checks
     * for a non-null result. Override this method if your source can determine key existence more efficiently
     * than creating an ItemStack (e.g., by checking a set of registered keys).
     *
     * @param key the item key to check
     * @return true if this source can create an item for the key
     */
    default boolean canResolve(Key key) {
        return createItem(key) != null;
    }

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
