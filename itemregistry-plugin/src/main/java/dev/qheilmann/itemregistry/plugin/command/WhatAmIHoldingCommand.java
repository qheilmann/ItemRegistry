package dev.qheilmann.itemregistry.plugin.command;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import dev.qheilmann.itemregistry.ItemRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

@NullMarked
public final class WhatAmIHoldingCommand {

    private static final String NAME = "whatamiholding";
    protected static final String[] ALIASES = { "waih" };
    private static final String SHORT_HELP = "Show the item key of your held item";
    private static final String LONG_HELP = "Resolves the item in your main hand using the shared ItemRegistry";
    private static final String USAGE = """

                                    /whatamiholding
                                    /waih
                                    """;

    private WhatAmIHoldingCommand() {}

    public static void register(ItemRegistry itemRegistry) {
        new CommandAPICommand(NAME)
            .withAliases(ALIASES)
            .withHelp(SHORT_HELP, LONG_HELP)
            .withUsage(USAGE)
            .executesPlayer((PlayerCommandExecutor) (player, args) -> executeAction(player, itemRegistry))
            .register();
    }

    private static void executeAction(Player player, ItemRegistry itemRegistry) {

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage(Component.text("You are not holding any item in your main hand.", NamedTextColor.RED));
            return;
        }

        Key key = itemRegistry.resolveKey(heldItem);
        if (key == null) {
            player.sendMessage(Component.text("Held item is not resolvable by the shared registry.", NamedTextColor.RED));
            return;
        }

        player.sendMessage(
            Component.text("Held item key: ", NamedTextColor.GRAY)
                .append(Component.text(key.asString(), NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.copyToClipboard(key.asString()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
                )
        );
    }
}
