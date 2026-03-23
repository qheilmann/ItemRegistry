package dev.qheilmann.itemregistry.plugin.command.argument;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.qheilmann.itemregistry.ItemRegistry;
import dev.qheilmann.itemregistry.plugin.command.argument.GreadyItemArgument.ItemAndCountResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

/**
 * Parses a greedy string as an item key with an optional count.
 *
 * <p>Expected format:
 * {@code minecraft:diamond_sword[custom_name:"hello world"] 3}
 *
 * <p><strong>Not fully implemented.</strong> Do not use this argument in production.
 * Use {@link KeyItemArgument} instead.
 *
 * @see KeyItemArgument
 */
@NullMarked
@SuppressWarnings("java:S110") // Inheritance depth from CommandAPI's CustomArgument
public class GreadyItemArgument extends CustomArgument<ItemAndCountResult, String> {

    /**
     * Creates a new GreadyItemArgument.
     *
     * @param nodeName the argument node name
     * @param itemRegistry the registry used to resolve item keys to {@link ItemStack}s
     */
    @SuppressWarnings("unused") // Not fully implemented yet
    public GreadyItemArgument(String nodeName, ItemRegistry itemRegistry) {
        super(new GreedyStringArgument(nodeName), input -> {
            if (true) {
                throw new UnsupportedOperationException("This argument is not fully implemented yet. Please use " + KeyItemArgument.class.getSimpleName() + " instead.");
            }

            // TODO(feature) implement count parsing
            // simple way: just split by space and take last part and parse it as int
            // complex: add extra param support and check for be avare of is extra apram contains space (like minecraft:dirt[custom_name:"hello world"])
            // finaly we can also impl a custom parser with lot of suggestions and error handling like minecraft does for give command

            String itemStr = input.currentInput();

            if (!Key.parseable(itemStr)) {
                Component invlideParseMessage = Component.translatable("argument.resource_or_id.failed_to_parse", Component.text(itemStr)); // "Failed to parse structure: %s"
                throw CustomArgumentHelper.minecraftLikeException(invlideParseMessage, input);
            }
            Key itemKey = Key.key(itemStr);
            
            ItemStack item = itemRegistry.createItem(itemKey);
            if (item == null) {
                Component notFoundMessage = Component.translatable("argument.resource_or_id.no_such_element", // "Can't find element '%s' in registry '%s'"
                    Component.text(itemKey.asString()),
                    Component.text(itemRegistry.key().asString())
                );
                throw CustomArgumentHelper.minecraftLikeException(notFoundMessage, input);
            }

            return new ItemAndCountResult(item, 100); // dummy count
        });
    }

    public record ItemAndCountResult(ItemStack item, int count) {
    }
}
