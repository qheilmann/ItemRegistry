package dev.qheilmann.itemregistry;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * Functional interface for providing {@link ItemStack} instances.
 * <p>
 * Implementations should return a fresh itemstack instance on every call.
 * <p>
 * Examples:
 * <pre>{@code
 * () -> ItemStack.of(Material.STONE)
 * templateItem::clone
 * }</pre>
 */
@NullMarked
@FunctionalInterface
public interface ItemProvider {

    /**
     * Creates and returns a new {@link ItemStack} instance.
     * @return a ItemStack.of instance
     */
    ItemStack create();
}
