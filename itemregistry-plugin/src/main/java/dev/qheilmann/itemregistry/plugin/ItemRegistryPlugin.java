package dev.qheilmann.itemregistry.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import dev.qheilmann.itemregistry.GlobalItemRegistry;
import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import dev.qheilmann.itemregistry.event.ItemRegistryReadyEvent;
import dev.qheilmann.itemregistry.plugin.command.GiveCommand;
import dev.qheilmann.itemregistry.plugin.command.WhatAmIHoldingCommand;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * Standalone shared ItemRegistry runtime plugin.
 */
@NullMarked
public final class ItemRegistryPlugin extends JavaPlugin {

    public static final String PLUGIN_NAME = "ItemRegistry";
    public static final String NAMESPACE = ItemRegistry.NAMESPACE;
    public static final ComponentLogger LOGGER = ComponentLogger.logger(PLUGIN_NAME);
    
    private final ItemRegistry globalRegistry = new ItemRegistry(GlobalItemRegistry.GLOBAL_REGISTRY_KEY);
    private boolean failOnload = false;

    @Override
    public void onLoad() {
        try {
            onLoadCommandAPI();
        } catch (Exception e) {
            LOGGER.error("Failed to load {}: {}", PLUGIN_NAME, e.getMessage());
            failOnload = true;
        }
    }

    @Override
    public void onEnable() {
        if (failOnload) {
            LOGGER.error("{} failed to load and will not be enabled.", PLUGIN_NAME);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        CommandAPI.onEnable();

        GlobalItemRegistry.register(globalRegistry);
        globalRegistry.registerSource(ItemSource.VANILLA_SOURCE);
        Bukkit.getPluginManager().callEvent(new ItemRegistryReadyEvent(globalRegistry));

        // Commands
        GiveCommand.register(globalRegistry);
        WhatAmIHoldingCommand.register(globalRegistry);
    }

    @Override
    public void onDisable() {
        GlobalItemRegistry.unregister();
    }

    private void onLoadCommandAPI() {
        CommandAPIPaperConfig commandApiConfig = new CommandAPIPaperConfig(this);
        commandApiConfig.setNamespace(ItemRegistry.NAMESPACE);

        CommandAPI.onLoad(commandApiConfig);
    }
}
