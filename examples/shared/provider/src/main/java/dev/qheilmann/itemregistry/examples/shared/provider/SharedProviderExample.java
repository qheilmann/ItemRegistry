package dev.qheilmann.itemregistry.examples.shared.provider;

import dev.qheilmann.itemregistry.GlobalItemRegistry;
import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.sources.PdcItemSource;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SharedProviderExample extends JavaPlugin {

    private static final String NAMESPACE = "shared_provider_example";

    @Override
    public void onEnable() {
        if (!GlobalItemRegistry.isAvailable()) {
            getSLF4JLogger().error("Global item registry is unavailable. Ensure the ItemRegistry plugin is enabled. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ItemRegistry globalRegistry = GlobalItemRegistry.registry();

        PdcItemSource source = new PdcItemSource(Key.key(NAMESPACE, "shared_provider_source"));
        source.register(Key.key(NAMESPACE, "shared_item"), ItemStack.of(Material.EMERALD));
        globalRegistry.registerSource(source);
        getSLF4JLogger().info("Registered {} into shared registry {}.", source.key(), globalRegistry.key());
    }
}
