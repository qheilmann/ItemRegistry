package dev.qheilmann.itemregistry.playground;

import dev.qheilmann.itemregistry.ItemRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final ItemRegistry registry;

    public PlayerJoinListener(ItemRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        List<ItemStack> items = registry.registeredKeys().stream()
            .flatMap(key -> registry.create(key).stream())
            .toList();

        player.getInventory().addItem(items.toArray(new ItemStack[0]));
    }
}
