package dev.qheilmann.itemregistry.playground;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import dev.qheilmann.itemregistry.ItemRegistry;

public class ItemRegistryPlayground extends JavaPlugin {

    private final ItemRegistry registry = new ItemRegistry();

    @Override
    public void onEnable() {
        // Register a simple stone block creator
        registry.register(
            Key.key("playground", "stone"),
            () -> new ItemStack(Material.STONE)
        );

        // Register a diamond sword clone from a template
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        registry.register(Key.key("playground", "diamond_sword"), sword::clone);

        getLogger().info("Registered keys: " + registry.registeredKeys());
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(registry), this);
        getLogger().info("ItemRegistryPlayground enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemRegistryPlayground disabled.");
    }
}

