package dev.qheilmann.itemregistry.examples.shared.consumer;

import dev.qheilmann.itemregistry.GlobalItemRegistry;
import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.sources.StrictItemSource;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
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

        // Register a local source using StrictItemSource (demonstrates alternative strategy).
        StrictItemSource localSource = new StrictItemSource(Key.key("shared_consumer_example", "strict_source"));
        localSource.register(Key.key("shared_consumer_example", "local_item"), ItemStack.of(Material.LAPIS_LAZULI));
        globalRegistry.registerSource(localSource);

        // Wait to ensure all providers have registered before testing item resolution
        getServer().getScheduler().runTask(this, () -> {
            ItemStack vanillaItem = globalRegistry.createItem(Key.key("minecraft", "diamond"));
            ItemStack providerItem = globalRegistry.createItem(Key.key("shared_provider_example", "shared_item"));
            ItemStack localItem = globalRegistry.createItem(Key.key("shared_consumer_example", "local_item"));
            getSLF4JLogger().info("Does 'minecraft:diamond' resolve? {}", vanillaItem != null);
            getSLF4JLogger().info("Does 'shared_provider_example:shared_item' resolve? {}", providerItem != null);
            getSLF4JLogger().info("Does 'shared_consumer_example:local_item' resolve? {}", localItem != null);

            if (vanillaItem != null) {
                Key resolvedVanillaKey = globalRegistry.resolveKey(vanillaItem);
                getSLF4JLogger().info("Reverse lookup for vanilla item -> {}", resolvedVanillaKey);
            }

            if (providerItem != null) {
                Key resolvedProviderKey = globalRegistry.resolveKey(providerItem);
                getSLF4JLogger().info("Reverse lookup for provider item (PDC-based) -> {}", resolvedProviderKey);
            }

            if (localItem != null) {
                Key resolvedLocalKey = globalRegistry.resolveKey(localItem);
                getSLF4JLogger().info("Reverse lookup for local item (Strict-based) -> {}", resolvedLocalKey);
            }
        });
    }
}
