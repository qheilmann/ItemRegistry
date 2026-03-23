package dev.qheilmann.itemregistry.examples.shared.consumer;

import dev.qheilmann.itemregistry.GlobalItemRegistry;
import dev.qheilmann.itemregistry.ItemRegistry;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SharedConsumerExample extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!GlobalItemRegistry.isAvailable()) {
            getSLF4JLogger().error("Global item registry is unavailable. Ensure the ItemRegistry plugin is enabled. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ItemRegistry globalRegistry = GlobalItemRegistry.registry();

        getSLF4JLogger().info("Shared consumer enabled. Reading from registry {}.", globalRegistry.key());

        // Wait to ensure all providers have registered before testing item resolution
        getServer().getScheduler().runTask(this, () -> {
            ItemStack vanillaItem = globalRegistry.createItem(Key.key("minecraft", "diamond"));
            ItemStack providerItem = globalRegistry.createItem(Key.key("shared_provider_example", "shared_item"));
            getSLF4JLogger().info("Does 'minecraft:diamond' resolve? {}", vanillaItem != null);
            getSLF4JLogger().info("Does 'shared_provider_example:shared_item' resolve? {}", providerItem != null);
        });
    }
}
