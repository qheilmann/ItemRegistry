package dev.qheilmann.itemregistry.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import dev.qheilmann.itemregistry.GlobalItemRegistry;
import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import dev.qheilmann.itemregistry.event.ItemRegistryReadyEvent;

/**
 * Standalone shared ItemRegistry runtime plugin.
 */
@NullMarked
public final class ItemRegistryPlugin extends JavaPlugin {

    private final ItemRegistry globalRegistry = new ItemRegistry(GlobalItemRegistry.GLOBAL_REGISTRY_KEY);

    @Override
    public void onEnable() {
        GlobalItemRegistry.register(globalRegistry);
        globalRegistry.registerSource(ItemSource.VANILLA_SOURCE);
        Bukkit.getPluginManager().callEvent(new ItemRegistryReadyEvent(globalRegistry));
        getSLF4JLogger().info("ItemRegistry shared plugin enabled with global registry {}.", globalRegistry.key());
    }

    @Override
    public void onDisable() {
        GlobalItemRegistry.unregister();
    }
}
