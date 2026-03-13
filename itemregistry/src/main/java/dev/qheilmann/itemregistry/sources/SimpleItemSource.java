package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.ItemProvider;
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
 * Simple mutable in-memory {@link ItemSource}.
 * <p>
 * This implementation provides a straightforward API for plugins that want to
 * register custom items without implementing their own ItemSource. Items are
 * stored as templates and cloned on each creation.
 */
@NullMarked
public class SimpleItemSource implements ItemSource {

    /**
     * Map of keys to their providers.
     */
    private final Map<Key, ItemProvider> providers = new ConcurrentHashMap<>();

    /**
     * The key representing this source, used for identification.
     */
    private final Key sourceKey;

    /**
     * Creates a new SimpleItemSource with the given source key.
     *
     * @param sourceKey the key representing this source (e.g., "myplugin:myitemsource")
     */
    public SimpleItemSource(Key sourceKey) {
        this.sourceKey = sourceKey;
    }

    /**
     * Registers an item under the given key.
     * <p>
     * The template ItemStack is stored and cloned on each {@link #resolve(Key)} call.
     * If an item is already registered under this key, it will be replaced.
     *
     * @param key the key to register the item under
     * @param template the ItemStack template to clone on creation
     * @return the previous provider for this key, or null if none existed
     * @throws IllegalArgumentException if template is null or air
     */
    @SuppressWarnings("java:S2589") // Enforce non-null explicitly
    @Nullable
    public ItemProvider register(Key key, ItemStack template) {
        Objects.requireNonNull(key, "Key cannot be null");
        if (template.getType().isAir()) {
            throw new IllegalArgumentException("Item template cannot be null or air");
        }
        
        // Clone the template for storage and return a cloning provider
        ItemStack immutableTemplate = template.clone();
        return providers.put(key, immutableTemplate::clone);
    }

    /**
     * Unregisters the item with the given key.
     *
     * @param key the key to unregister
     * @return the previous provider for this key, or null if none existed
     */
    public @Nullable ItemProvider unregister(Key key) {
        return providers.remove(key);
    }

    /**
     * Checks if an item is registered under the given key.
     *
     * @param key the key to check
     * @return true if an item is registered under this key
     */
    public boolean has(Key key) {
        return providers.containsKey(key);
    }

    /**
     * Removes all registered items from this source.
     */
    public void clear() {
        providers.clear();
    }

    /**
     * Returns the number of items currently registered in this source.
     *
     * @return the count of registered items
     */
    public int size() {
        return providers.size();
    }

    @Override
    public @Nullable ItemProvider resolve(Key key) {
        return providers.get(key);
    }

    @Override
    public Set<Key> registeredKeys() {
        return Set.copyOf(providers.keySet());
    }

    @Override
    public Key key() {
        return sourceKey;
    }
}
