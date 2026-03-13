package dev.qheilmann.itemregistry.event;

import dev.qheilmann.itemregistry.ItemRegistry;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Fired by consumer plugins once their {@link ItemRegistry} is initialized and
 * ready to accept third-party sources.
 * <p>
 * Producer plugins can listen to this event and call
 * {@link ItemRegistry#registerSource(dev.qheilmann.itemregistry.ItemSource)}
 * on the provided registry.
 */
@NullMarked
public final class ItemRegistryReadyEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemRegistry registry;

    /**
     * Creates a new ready event for the given registry.
     *
     * @param registry the ready item registry
     */
    public ItemRegistryReadyEvent(ItemRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        this.registry = registry;
    }

    /**
     * Returns the registry that is ready to receive additional sources.
     *
     * @return the consumer registry
     */
    public ItemRegistry getRegistry() {
        return registry;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}