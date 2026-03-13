package dev.qheilmann.itemregistry.examples.consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import dev.qheilmann.itemregistry.event.ItemRegistryReadyEvent;
import dev.qheilmann.itemregistry.sources.SimpleItemSource;
import net.kyori.adventure.key.Key;

// Demo goal: consumer plugin that owns a registry and exposes it to producers.
@NullMarked
public final class ConsumerPluginExample extends JavaPlugin {

    public static final String NAMESPACE = "consumer_example";
    public static final Key REGISTRY_KEY = Key.key(NAMESPACE, "main");

    private final ItemRegistry registry = new ItemRegistry(REGISTRY_KEY);

    @Override
    public void onEnable() {
        registry.registerSource(ItemSource.VANILLA_SOURCE);

        // Register local items owned by this consumer plugin.
        SimpleItemSource localItems = new SimpleItemSource(Key.key(NAMESPACE, "local_source"));
        localItems.register(Key.key(NAMESPACE, "consumer_item"), ItemStack.of(Material.IRON_NUGGET));
        registry.registerSource(localItems);

        // Broadcast readiness so producer plugins can register their own sources.
        // Producers can filter by registry key to support multiple registries.
        Bukkit.getPluginManager().callEvent(new ItemRegistryReadyEvent(registry));

        // These checks demonstrate expected behavior:
        // - consumer_item should always resolve
        // - producer_example:shared_item resolves only if producer plugin is installed
        ItemStack consumerItem = registry.create(Key.key(NAMESPACE, "consumer_item"));
        ItemStack producerItem = registry.create(Key.key("producer_example", "shared_item"));
        getSLF4JLogger().info("Consumer example enabled.");
        getSLF4JLogger().info("Does consumer_item resolve? {}", consumerItem != null);
        getSLF4JLogger().info("Does producer_example:shared_item resolve? {}", producerItem != null);
    }
}
