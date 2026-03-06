package dev.qheilmann.itemregistry.playground;

import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class PlayerJoinListener implements Listener {

    private final ItemRegistry registry;

    public PlayerJoinListener(ItemRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Give custom items from the playground source
        giveCustomItems(player);
        
        // Send welcome message
        player.sendMessage(Component.text("Welcome! You've been given custom items from ItemRegistry.", 
            NamedTextColor.GREEN));
    }

    /**
     * Gives all custom items from registered sources to the player.
     */
    private void giveCustomItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        
        // Get items from each source
        for (Key sourceKey : registry.getSourceKeys()) {
            ItemSource source = registry.getSource(sourceKey);
            if (source == null) continue;
            
            // Only get items from custom sources (not vanilla)
            if (sourceKey.namespace().equals("playground") && 
                sourceKey.value().equals("custom")) {
                
                for (Key itemKey : source.registeredKeys()) {
                    ItemStack item = registry.create(itemKey);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        }
        
        if (!items.isEmpty()) {
            player.getInventory().addItem(items.toArray(new ItemStack[0]));
        }
    }
}
