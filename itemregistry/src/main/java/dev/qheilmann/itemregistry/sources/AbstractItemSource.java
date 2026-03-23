package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.ItemSource;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for {@link ItemSource} implementations that manage templates.
 * <p>
 * Provides common functionality for template storage and registration management.
 * Subclasses must implement {@link #createItem(Key)}, {@link #canResolve(Key)},
 * and {@link #resolveKey(ItemStack)} to define resolution behavior.
 * 
 * @see StrictItemSource
 * @see PdcItemSource
 */
@NullMarked
public abstract class AbstractItemSource implements ItemSource {

    /**
     * Map of keys to immutable templates.
     */
    protected final Map<Key, ItemStack> templates = new ConcurrentHashMap<>();

    /**
     * The key representing this source, used for identification.
     */
    protected final Key sourceKey;

    /**
     * Creates a new AbstractItemSource with the given source key.
     *
     * @param sourceKey the key representing this source (e.g., "myplugin:myitemsource")
     */
    protected AbstractItemSource(Key sourceKey) {
        this.sourceKey = Objects.requireNonNull(sourceKey, "sourceKey cannot be null");
    }

    /**
     * Registers an item under the given key.
     * <p>
     * The template ItemStack is stored and cloned on each
     * {@link #createItem(Key)} call.
     * If an item is already registered under this key, it will be replaced.
     *
     * @param key the key to register the item under
     * @param template the ItemStack template to clone on creation
     * @return the previous template for this key, or null if none existed
     * @throws IllegalArgumentException if template is null or air
     */
    @SuppressWarnings("java:S2589") // Enforce non-null explicitly
    @Nullable
    public ItemStack register(Key key, ItemStack template) {
        Objects.requireNonNull(key, "Key cannot be null");
        if (template.getType().isAir()) {
            throw new IllegalArgumentException("Item template cannot be null or air");
        }

        // Clone the template for storage to prevent external mutation.
        ItemStack previous = templates.put(key, template.clone());
        return previous != null ? previous : null;
    }

    /**
     * Unregisters the item with the given key.
     *
     * @param key the key to unregister
     * @return the previous template for this key, or null if none existed
     */
    @Nullable
    public ItemStack unregister(Key key) {
        ItemStack removed = templates.remove(key);
        return removed != null ? removed : null;
    }

    /**
     * Checks if an item is registered under the given key.
     *
     * @param key the key to check
     * @return true if an item is registered under this key
     */
    public boolean has(Key key) {
        return templates.containsKey(key);
    }

    /**
     * Removes all registered items from this source.
     */
    public void clear() {
        templates.clear();
    }

    /**
     * Returns the number of items currently registered in this source.
     *
     * @return the count of registered items
     */
    public int size() {
        return templates.size();
    }

    @Override
    public Set<Key> registeredKeys() {
        return Set.copyOf(templates.keySet());
    }

    @Override
    public Key key() {
        return sourceKey;
    }
}
