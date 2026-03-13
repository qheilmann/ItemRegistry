package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.ItemProvider;
import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ItemSource implementation that provides vanilla Minecraft items.
 * <p>
 * This source maps all valid vanilla item materials to their corresponding
 * {@link ItemStack}s using the {@code minecraft:} namespace.
 * <p>
 * Example keys:
 * <ul>
 *   <li>{@code minecraft:diamond}</li>
 *   <li>{@code minecraft:stone}</li>
 *   <li>{@code minecraft:diamond_sword}</li>
 * </ul>
 * <p>
 * This source is immutable after construction - the set of vanilla
 * items does not change at runtime.
 * <p>
 * This class is a singleton - use {@link #getInstance()} to access the shared instance.
 *
 * @see ItemSource
 */
@NullMarked
@SuppressWarnings("java:S6548") // Singleton pattern is appropriate here
public class VanillaItemSource implements ItemSource {

    public static final Key SOURCE_KEY = Key.key(ItemRegistry.NAMESPACE, "vanilla");
    
    /**
     * The singleton instance of VanillaItemSource.
     */
    private static final VanillaItemSource INSTANCE = new VanillaItemSource();

    private final Map<Key, ItemProvider> keyProviders = new HashMap<>();

    /**
     * Private constructor to prevent external instantiation.
     * Use {@link #getInstance()} instead.
     */
    private VanillaItemSource() {
        Registry.ITEM.forEach(type -> 
            keyProviders.put(type.key(), type::createItemStack)
        );
    }
    
    /**
     * Returns the singleton instance of VanillaItemSource.
     *
     * @return the shared VanillaItemSource instance
     */
    public static VanillaItemSource getInstance() {
        return INSTANCE;
    }

    @Override
    public @Nullable ItemProvider resolve(Key key) {
        if (!Key.MINECRAFT_NAMESPACE.equals(key.namespace())) {
            return null;
        }
        
        return keyProviders.get(key);
    }

    @Override
    public Set<Key> registeredKeys() {
        return Set.copyOf(keyProviders.keySet());
    }


    /**
     * Returns the number of vanilla items provided by this source.
     *
     * @return the count of available vanilla items
     */
    public int size() {
        return keyProviders.size();
    }

    @Override
    public @NotNull Key key() {
        return SOURCE_KEY;
    }
}
