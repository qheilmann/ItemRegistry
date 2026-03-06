package dev.qheilmann.itemregistry.playground;

import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import dev.qheilmann.itemregistry.sources.SimpleItemSource;
import dev.qheilmann.itemregistry.sources.VanillaItemSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemRegistryPlayground extends JavaPlugin {

    public static final String NAMESPACE = "itemregistryplayground";
    
    private final ItemRegistry registry = new ItemRegistry();
    private static final ComponentLogger LOGGER = ComponentLogger.logger(ItemRegistryPlayground.class);
    
    @Override
    public void onEnable() {
        // Register vanilla items source
        VanillaItemSource vanilla = ItemSource.VANILLA_SOURCE;
        registry.registerSource(vanilla);
        LOGGER.info("Registered vanilla items: {0} items", vanilla.size());
        
        // Set up and register custom items source
        Key customSourceKey = Key.key(NAMESPACE, "custom");
        SimpleItemSource customItems = new SimpleItemSource(customSourceKey);
        setupCustomItems(customItems);
        registry.registerSource(customItems);
        LOGGER.info("Registered custom items: {0} items", customItems.size());

        // Register event listener
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(registry), this);
        
        getLogger().info("ItemRegistryPlayground enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemRegistryPlayground disabled.");
    }

    /**
     * Sets up custom items in the SimpleItemSource.
     */
    private void setupCustomItems(SimpleItemSource source) {
        // Magic Wand
        ItemStack wand = new ItemStack(Material.STICK);
        wand.editMeta(meta -> {
            meta.displayName(Component.text("Magic Wand", NamedTextColor.LIGHT_PURPLE));
            meta.lore(List.of(
                Component.text("A mysterious wand", NamedTextColor.GRAY),
                Component.text("imbued with magical power", NamedTextColor.GRAY)
            ));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        source.register(Key.key(NAMESPACE, "magic_wand"), wand);

        // Super Diamond Sword
        ItemStack superSword = new ItemStack(Material.DIAMOND_SWORD);
        superSword.editMeta(meta -> {
            meta.displayName(Component.text("Legendary Blade", NamedTextColor.GOLD));
            meta.lore(List.of(
                Component.text("Forged in dragon fire", NamedTextColor.DARK_RED),
                Component.text("Unbreakable and deadly", NamedTextColor.DARK_RED)
            ));
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        });
        source.register(Key.key(NAMESPACE, "legendary_blade"), superSword);

        // Teleport Stone
        ItemStack teleportStone = new ItemStack(Material.ENDER_PEARL);
        teleportStone.editMeta(meta -> {
            meta.displayName(Component.text("Teleport Stone", NamedTextColor.AQUA));
            meta.lore(List.of(
                Component.text("Right-click to teleport", NamedTextColor.YELLOW)
            ));
        });
        source.register(Key.key(NAMESPACE, "teleport_stone"), teleportStone);
    }

    /**
     * Gets the item registry instance.
     * 
     * @return the registry
     */
    public ItemRegistry getRegistry() {
        return registry;
    }
}

