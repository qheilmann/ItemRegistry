package dev.qheilmann.itemregistry.sources;

import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import dev.qheilmann.itemregistry.ItemSource;

import java.util.Map;

/**
 * Strict similarity-based {@link ItemSource}.
 * <p>
 * Resolves items by comparing them to registered templates using
 * {@link ItemStack#isSimilar(ItemStack)}. This approach works for any ItemStack
 * but is vulnerable to modifications like anvil renames or enchantment changes.
 * <p>
 * For persistent item identification across modifications, use
 * {@link PdcItemSource} instead.
 */
@NullMarked
public class StrictItemSource extends AbstractItemSource {

    /**
     * Creates a new StrictItemSource with the given source key.
     *
     * @param sourceKey the key representing this source (e.g., "myplugin:myitemsource")
     */
    public StrictItemSource(Key sourceKey) {
        super(sourceKey);
    }

    @Override
    public @Nullable ItemStack createItem(Key key) {
        ItemStack template = templates.get(key);
        return template != null ? template.clone() : null;
    }

    @Override
    public boolean canResolve(Key key) {
        return templates.containsKey(key);
    }

    @Override
    public @Nullable Key resolveKey(ItemStack itemStack) {
        for (Map.Entry<Key, ItemStack> entry : templates.entrySet()) {
            if (entry.getValue().isSimilar(itemStack)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
