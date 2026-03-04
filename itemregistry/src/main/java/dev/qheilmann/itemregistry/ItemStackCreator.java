package dev.qheilmann.itemregistry;

import org.bukkit.inventory.ItemStack;

/**
 * Factory for producing {@link ItemStack} instances.
 * <p>
 * Implementations should return a fresh or cloned stack on every call.
 * Examples:
 * <pre>{@code
 * () -> new ItemStack(Material.STONE)
 * templateItem::clone
 * }</pre>
 */
@FunctionalInterface
public interface ItemStackCreator {

    /**
     * Creates and returns a new {@link ItemStack} instance.
     * @return a new ItemStack instance
     */
    ItemStack create();
}
