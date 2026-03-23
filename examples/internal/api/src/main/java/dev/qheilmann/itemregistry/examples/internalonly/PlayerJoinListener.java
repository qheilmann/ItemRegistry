package dev.qheilmann.itemregistry.examples.internalonly;

import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.ItemSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@NullMarked
public final class PlayerJoinListener implements Listener {

    private final ItemRegistry registry;

    public PlayerJoinListener(ItemRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveRegisteredItems(player);
        player.sendMessage(Component.text("Welcome! You have been given demo items from InternalOnlyExample.", NamedTextColor.GREEN));
    }

    private void giveRegisteredItems(Player player) {
        
        // First for demo purposes, we will give the "api_only_example:magic_wand" from our custom source from our item registry
        Key itemKey = Key.key("internal_only_example:magic_wand");
        ItemStack magicWand = registry.createItem(itemKey);
        if (magicWand != null) {
            player.give(magicWand);
        }

        // Now we will give all custom items from all non-vanilla sources in the registry
        registry.getSources().stream()
            .filter(itemSource -> !ItemSource.VANILLA_SOURCE.key().equals(itemSource.key())) // Skip vanilla items to only give custom demo items
            .flatMap(source -> source.registeredKeys().stream()
                .map(source::createItem) // Create each registered key into a fresh ItemStack
                .filter(Objects::nonNull)
            )
            .forEach(player::give); // Give each item to the player
    }
}
