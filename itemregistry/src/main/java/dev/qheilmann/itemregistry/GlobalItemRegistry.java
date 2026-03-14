package dev.qheilmann.itemregistry;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Global access point for the shared ItemRegistry runtime plugin.
 * <p>
 * Consumers should use {@link #registry()} and {@link #isAvaible()} only.
 * </p>
 */
@NullMarked
public final class GlobalItemRegistry {

    /** The key for the global item registry. */
    public static final Key GLOBAL_REGISTRY_KEY = Key.key(ItemRegistry.NAMESPACE, "global");

    private static final AtomicReference<ItemRegistry> INSTANCE = new AtomicReference<>();

    private GlobalItemRegistry() {
    }

    /**
     * Returns the shared global ItemRegistry.
     *
     * @return the global item registry
     * @throws IllegalStateException when the ItemRegistry plugin is not available yet
     */
    public static ItemRegistry registry() {
        ItemRegistry registry = INSTANCE.get();
        if (registry == null) {
            throw new IllegalStateException(
                "Global ItemRegistry is not available. " +
                "Make sure ItemRegistry is installed, listed in depend/softdepend, " +
                "and that access happens after plugin enable. " +
                "If this still fails, check that ItemRegistry classes were not shaded/relocated."
            );
        }
        return registry;
    }

    /**
     * Returns whether the shared global ItemRegistry is currently available.
     *
     * @return true when the shared registry is available
     */
    public static boolean isAvaible() {
        return INSTANCE.get() != null;
    }

    /**
     * Registers the shared global registry.
     * <p><b>Internal use only.</b> Called by the ItemRegistry runtime plugin.</p>
     * @param registry the registry to register as the global shared registry
     */
    public static void register(ItemRegistry registry) {
        if (!INSTANCE.compareAndSet(null, registry)) {
            throw new IllegalStateException("Global ItemRegistry is already registered");
        }
    }

    /**
     * Unregisters the shared global registry.
     * <p><b>Internal use only.</b> Called by the ItemRegistry runtime plugin.</p>
     */
    public static void unregister() {
        INSTANCE.set(null);
    }
}
