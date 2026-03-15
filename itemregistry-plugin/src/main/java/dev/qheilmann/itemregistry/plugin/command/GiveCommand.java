package dev.qheilmann.itemregistry.plugin.command;

import java.util.Collection;
import java.util.Collections;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.plugin.ItemRegistryPlugin;
import dev.qheilmann.itemregistry.plugin.command.argument.KeyItemArgument;

@SuppressWarnings("java:S1192") // Allow string literals
@NullMarked
public class GiveCommand {
    public static final String NAME = "give";
    public static final String[] ALIASES = {};
    public static final CommandPermission PERMISSION = CommandPermission.OP;
    public static final String SHORT_HELP = "Give items to players";
    public static final String LONG_HELP = "This command is used for giving regsitered items to players";
    public static final String USAGE = """

                                    /give <player> <item> [amount]
                                    """;

    private GiveCommand() {} // Static class

    public static void register(ItemRegistry itemRegistry) {

        new CommandAPICommand(NAME)
            .withAliases(ALIASES)
            .withPermission(PERMISSION)
            .withHelp(SHORT_HELP, LONG_HELP)
            .withUsage(USAGE)

            // give <player> <item> [amount]
            .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
            .withArguments(new KeyItemArgument("item", itemRegistry))
            .withOptionalArguments(new IntegerArgument("count", 1, 6400)) // minecraft default max give value count is 6400
            .executes((sender, args) -> {
                Collection<Player> players = args.getUnchecked("player");
                ItemStack item = args.getUnchecked("item");
                int count = args.getOrDefaultUnchecked("count", 1);

                for (Player player : players) {
                    player.give(Collections.nCopies(count, item));
                }
            })

            .register();
    }
}
