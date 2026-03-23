package dev.qheilmann.itemregistry.plugin.command.argument;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.qheilmann.itemregistry.ItemRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

/**
 * Parses a string as an item key and retrieves the corresponding ItemStack from the ItemRegistry.
 *
 * <p>Expected format:
 * {@code minecraft:diamond_sword}
 */
@NullMarked
@SuppressWarnings("java:S110") // Inheritance depth from CommandAPI's CustomArgument
public class KeyItemArgument extends CustomArgument<ItemStack, NamespacedKey> {

    /** The time-to-live for cached suggestions in milliseconds. */
    private static final long SUGGESTIONS_CACHE_TTL_MILLIS = 1_000L;
    /** The maximum number of suggestions to display. */
    private static final int MAX_SUGGESTIONS = 500;

    // Cache for suggestions to improve performance.
    private static final Map<ItemRegistry, CachedSuggestions> suggestionsCache = new ConcurrentHashMap<>();
    
    private record CachedSuggestions(Collection<String> values, long expiresAtMillis) {
    }

    /**
     * Creates a new KeyItemArgument.
     *
     * @param nodeName the argument node name
     * @param itemRegistry the registry used to resolve item keys to {@link ItemStack}s
     */
    public KeyItemArgument(String nodeName, ItemRegistry itemRegistry) {
        super(new NamespacedKeyArgument(nodeName), input -> {
            NamespacedKey itemKey = input.currentInput();
            ItemStack item = itemRegistry.createItem(itemKey);
            
            if (item == null) {
                Component notFoundMessage = Component.translatable("argument.resource_or_id.no_such_element", // "Can't find element '%s' in registry '%s'"
                    Component.text(itemKey.asString()),
                    Component.text(itemRegistry.key().asString())
                );
                throw CustomArgumentHelper.minecraftLikeException(notFoundMessage, input);
            }

            return item;
        });

        // Default suggestions
        this.replaceSuggestions(KeyItemArgument.argumentSuggestions(itemRegistry));
    }

    /**
     * Create argument suggestions for item keys based on all indexed items.
     *
     * @return ArgumentSuggestions providing available item key strings
     */
    public static ArgumentSuggestions<CommandSender> argumentSuggestions(ItemRegistry itemRegistry) {
        return (info, builder) -> {
            Collection<String> suggestions = getCachedSuggestions(itemRegistry);

            String currentInputLowerCase = builder.getRemainingLowerCase();
            int count = 0;
            for (String suggestion : suggestions) {
                if (shouldSuggest(suggestion, currentInputLowerCase)) {
                    builder.suggest(suggestion);
                    if (++count >= MAX_SUGGESTIONS) {
                        break;
                    }
                }
            }
            return CompletableFuture.completedFuture(builder.build());
        };
    }
        
    /**
     * Determines if a suggestion should be included based on the current input.
     * 
     * @param suggestion The suggestion string to check
     * @param currentInputLowerCase The current user input in lowercase
     * @return true if the suggestion matches the current input, false otherwise
     */
    private static boolean shouldSuggest(String suggestion, String currentInputLowerCase) {
        // No need to call toLowerCase() on suggestion since NamespacedKeys are always lowercase
        return suggestion.contains(currentInputLowerCase);
    }

    private static Collection<String> getCachedSuggestions(ItemRegistry itemRegistry) {
        long now = System.currentTimeMillis();
        CachedSuggestions cached = suggestionsCache.compute(itemRegistry, (registry, existing) -> {
            if (existing != null && now < existing.expiresAtMillis()) {
                return existing;
            }

            Collection<String> rebuilt = suggestionFrom(registry);
            return new CachedSuggestions(rebuilt, now + SUGGESTIONS_CACHE_TTL_MILLIS);
        });

        return cached.values();
    }

    /**
     * Fetches all registered item keys from the given ItemRegistry and returns them as a collection of strings.
     * This is used for providing suggestions in command arguments.
     *
     * @param itemRegistry the ItemRegistry to fetch item keys from
     * @return a collection of item key strings for suggestions
     */
    public static Collection<String> suggestionFrom(ItemRegistry itemRegistry) {
        Set<String> uniqueKeys = new HashSet<>();
        itemRegistry.getSources().forEach(source -> {
            for (Key key : source.registeredKeys()) {
                uniqueKeys.add(key.asString());
            }
        });

        return Set.copyOf(uniqueKeys);
    }
}
