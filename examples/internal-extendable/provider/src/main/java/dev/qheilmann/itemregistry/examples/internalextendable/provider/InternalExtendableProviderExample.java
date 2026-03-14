package dev.qheilmann.itemregistry.examples.internalextendable.provider;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import dev.qheilmann.itemregistry.event.ItemRegistryReadyEvent;
import dev.qheilmann.itemregistry.sources.SimpleItemSource;
import net.kyori.adventure.key.Key;

// Demo goal: provider plugin that contributes items to consumer registries.
@NullMarked
public final class InternalExtendableProviderExample extends JavaPlugin implements Listener {

    private static final String NAMESPACE = "provider_example";
    // This provider chooses to target only one registry identity.
    private static final Key TARGET_REGISTRY_KEY = Key.key("consumer_example", "main");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("InternalExtendable provider example enabled and listening for ItemRegistryReadyEvent.");
    }

    // Listen for registries becoming ready so we can register our items into them.
    @EventHandler
    public void onRegistryReady(ItemRegistryReadyEvent event) {
        getLogger().info("Received ItemRegistryReadyEvent for registry: " + event.getRegistry().key());
        // Ignore unrelated registries when multiple registries are present.
        if (!TARGET_REGISTRY_KEY.equals(event.getRegistry().key())) {
            getSLF4JLogger().info("Registry key does not match target. Skipping registration.");
            return;
        }

        // Register this plugin's source into the consumer-owned registry.
        SimpleItemSource source = new SimpleItemSource(Key.key(NAMESPACE, "provider_source"));
        source.register(Key.key(NAMESPACE, "shared_item"), ItemStack.of(Material.GOLD_NUGGET));
        event.getRegistry().registerSource(source);

        getLogger().info("Registered provider source into registry: " + event.getRegistry().key());
    }
}
