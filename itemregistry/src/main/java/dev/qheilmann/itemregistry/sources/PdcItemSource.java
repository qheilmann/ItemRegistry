package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.persistentdatatype.KeyDataType;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * PDC-based item source that stores item keys directly in ItemStack metadata.
 * <p>
 * Items created by this source have their key stored in the ItemStack's persistent
 * data container (PDC). This enables fast O(1) resolution and allows items to be
 * identified even after modifications like anvil renames or enchantment changes.
 * <p>
 * <strong>Ownership Model:</strong><br>
 * Only items created through {@link #createItem(Key)} can be resolved back.
 * External items or vanilla items will not be recognized unless explicitly PDC tagged.
 */
@NullMarked
public class PdcItemSource extends AbstractItemSource {

    public static final NamespacedKey ITEM_KEY_PDC = new NamespacedKey(ItemRegistry.NAMESPACE, "item_key");

    /**
     * Creates a new PdcItemSource with the given source key.
     *
     * @param sourceKey the key representing this source (e.g., "myplugin:pdcitemsource")
     */
    public PdcItemSource(Key sourceKey) {
        super(sourceKey);
    }

    @Override
    public @Nullable ItemStack createItem(Key key) {
        ItemStack template = templates.get(key);
        if (template == null) {
            return null;
        }

        // Clone and tag with the key in PDC
        ItemStack item = template.clone();
        item.editPersistentDataContainer(pdc -> pdc.set(ITEM_KEY_PDC, KeyDataType.TYPE, key));
        return item;
    }

    @Override
    public boolean canResolve(Key key) {
        return templates.containsKey(key);
    }

    @Override
    public @Nullable Key resolveKey(ItemStack itemStack) {
        // Fast PDC-based resolution
        return itemStack.getPersistentDataContainer().get(ITEM_KEY_PDC, KeyDataType.TYPE);
    }
}
