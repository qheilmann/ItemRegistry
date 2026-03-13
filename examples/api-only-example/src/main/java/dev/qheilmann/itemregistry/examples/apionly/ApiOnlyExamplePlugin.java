package dev.qheilmann.itemregistry.examples.apionly;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import dev.qheilmann.itemregistry.sources.SimpleItemSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// Demo goal: simple plugin that registers items directly through the API
@NullMarked
public final class ApiOnlyExamplePlugin extends JavaPlugin {

    public static final String NAMESPACE = "api_only_example";
    public static final Key REGISTRY_KEY = Key.key(NAMESPACE, "main");

    // Create an empty registry instance owned by this plugin.
    // In a real plugin, you might want to expose this to other plugins or use a shared registry instance.
    private final ItemRegistry registry = new ItemRegistry(REGISTRY_KEY);

    @Override
    public void onEnable() {

        // Add vanilla items to the registry (like minecraft:diamond_sword)
        registry.registerSource(ItemSource.VANILLA_SOURCE);

        // Create and register a custom source with demo items.
        // Here we use SimpleItemSource for convenience, but plugins can implement their own ItemSource for more complex behavior.
        SimpleItemSource custom = new SimpleItemSource(Key.key(NAMESPACE, "my_custom_source"));
        addCustomItems(custom);
        registry.registerSource(custom);

        // Register a listener that gives players registered custom items on join,
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(registry), this);

        // Log some info about the registered items for demonstration purposes.
        int totalSources = registry.getSources().size();
        int totalItems = registry.getSources().stream()
            .mapToInt(s -> s.registeredKeys().size())
            .sum();
        int customItems = registry.getSources().stream()
            .filter(s -> !s.key().equals(ItemSource.VANILLA_SOURCE.key()))
            .mapToInt(s -> s.registeredKeys().size())
            .sum();
        getSLF4JLogger().info("ApiOnlyExample enabled with {} sources, {} total items (including {} custom items).",
                                    totalSources, totalItems, customItems);
    }

    @Override
    public void onDisable() {
        getLogger().info("ApiOnlyExample disabled.");
    }

    /**
     * Sets up custom items in the SimpleItemSource.
     */
    private void addCustomItems(SimpleItemSource source) {
        ItemStack wand = ItemStack.of(Material.STICK);
        wand.editMeta(meta -> {
            meta.displayName(Component.text("Magic Wand", NamedTextColor.LIGHT_PURPLE));
            meta.lore(List.of(
                Component.text("A mysterious wand", NamedTextColor.GRAY),
                Component.text("imbued with magical power", NamedTextColor.GRAY)
            ));
            meta.setEnchantmentGlintOverride(true);
        });
        // Add the wand to the source under the key "api_only_example:magic_wand"
        Key wandKey = Key.key(NAMESPACE, "magic_wand");
        source.register(wandKey, wand);

        ItemStack superSword = ItemStack.of(Material.DIAMOND_SWORD);
        superSword.editMeta(meta -> {
            meta.displayName(Component.text("Legendary Blade", NamedTextColor.GOLD));
            meta.lore(List.of(
                Component.text("Forged in dragon fire", NamedTextColor.DARK_RED),
                Component.text("Unbreakable and deadly", NamedTextColor.DARK_RED)
            ));
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        });
        Key legendaryBladeKey = Key.key(NAMESPACE, "legendary_blade");
        source.register(legendaryBladeKey, superSword);

        ItemStack teleportStone = ItemStack.of(Material.ENDER_PEARL);
        teleportStone.editMeta(meta -> {
            meta.displayName(Component.text("Teleport Globe", NamedTextColor.AQUA));
            meta.lore(List.of(
                Component.text("Right-click to teleport", NamedTextColor.YELLOW)
            ));
        });
        Key teleportGlobeKey = Key.key(NAMESPACE, "teleport_globe");
        source.register(teleportGlobeKey, teleportStone);
    }
}
