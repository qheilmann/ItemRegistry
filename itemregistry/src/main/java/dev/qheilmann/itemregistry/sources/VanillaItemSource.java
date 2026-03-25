package dev.qheilmann.itemregistry.sources;

import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

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

    /** The key representing the vanilla item source. */
    public static final Key SOURCE_KEY = Key.key(ItemRegistry.NAMESPACE, "vanilla");
    
    /**
     * The singleton instance of VanillaItemSource.
     */
    private static final VanillaItemSource INSTANCE = new VanillaItemSource();

    private static final Set<DataComponentType> IGNORED_PLAYER_MODIFIABLE_COMPONENTS = Set.of(
        DataComponentTypes.CUSTOM_NAME,
        DataComponentTypes.REPAIR_COST,
        DataComponentTypes.ENCHANTMENTS,
        DataComponentTypes.POTION_CONTENTS
    );

    private final Map<Key, ItemStack> keyTemplates = new HashMap<>();

    /**
     * Private constructor to prevent external instantiation.
     * Use {@link #getInstance()} instead.
     */
    private VanillaItemSource() {
        Registry.ITEM.forEach(type -> 
            keyTemplates.put(type.key(), type.createItemStack())
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
    public @Nullable ItemStack createItem(Key key) {
        if (!Key.MINECRAFT_NAMESPACE.equals(key.namespace())) {
            return null;
        }
        ItemStack template = keyTemplates.get(key);
        return template != null ? template.clone() : null;
    }

    @Override
    public boolean canResolve(Key key) {
        return Key.MINECRAFT_NAMESPACE.equals(key.namespace()) && keyTemplates.containsKey(key);
    }

    @Override
    public @Nullable Key resolveKey(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return null;
        }
        Key key = itemStack.getType().key();
        ItemStack vanillaTemplate = keyTemplates.get(key);
        if (vanillaTemplate == null) {
            return null;
        }
        
        if (!isVanillaEquivalent(itemStack, vanillaTemplate)) {
            return null;
        }

        return key;
    }

    private boolean isVanillaEquivalent(ItemStack candidate, ItemStack vanillaTemplate) {
        // Ignore player-modifiable data components
        Set<DataComponentType> candidateTypes = filteredDataTypes(candidate, IGNORED_PLAYER_MODIFIABLE_COMPONENTS::contains);
        Set<DataComponentType> vanillaTypes = filteredDataTypes(vanillaTemplate, IGNORED_PLAYER_MODIFIABLE_COMPONENTS::contains);

        // Same set of data component
        if (!candidateTypes.equals(vanillaTypes)) {
            return false;
        }

        // Same value for each data component
        for (DataComponentType type : candidateTypes) {
            if (!(type instanceof DataComponentType.Valued<?> valuedType)) {
                continue; // Skip non-valued components
            }

            if (!hasSameData(candidate, vanillaTemplate, valuedType)) {
                return false;
            }
        }

        return true;
    }

    private <T> boolean hasSameData(ItemStack candidate, ItemStack vanillaTemplate, DataComponentType.Valued<T> valuedType) {
        T candidateData = candidate.getData(valuedType);
        T vanillaData = vanillaTemplate.getData(valuedType);
        return Objects.equals(candidateData, vanillaData);
    }

    private Set<DataComponentType> filteredDataTypes(ItemStack itemStack, Predicate<DataComponentType> filter) {
        Set<DataComponentType> filtered = new HashSet<>(itemStack.getDataTypes());
        filtered.removeIf(filter);
        return filtered;
    }

    @Override
    public Set<Key> registeredKeys() {
        return Set.copyOf(keyTemplates.keySet());
    }


    /**
     * Returns the number of vanilla items provided by this source.
     *
     * @return the count of available vanilla items
     */
    public int size() {
        return keyTemplates.size();
    }

    @Override
    public @NotNull Key key() {
        return SOURCE_KEY;
    }
}
