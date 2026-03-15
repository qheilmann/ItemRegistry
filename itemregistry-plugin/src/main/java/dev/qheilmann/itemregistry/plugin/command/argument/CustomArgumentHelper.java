package dev.qheilmann.itemregistry.plugin.command.argument;

import java.util.function.Function;

import org.jspecify.annotations.NullMarked;

import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentInfo;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@NullMarked
public final class CustomArgumentHelper {

    // Private constructor prevents instantiation
    private CustomArgumentHelper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Creates a custom argument adventure component exception with a Minecraft-like error message.
     * This method generates an error message with a custom first line and appends a "here" reference
     * to indicate the location of the error in the input.
     * <p>
     * Example:
     * <pre>
     * {@code
     * minecraftLikeException((arg) -> Component.text("Unknow item '" + arg + "'"))
     * }
     * </pre>
     * With the input:
     * <pre> /give @p diirt</pre>
     * Results in:
     * <pre>
     * Unknown item 'minecraft:diirt'
     * [gray]/give @p [red]diirt<--[HERE]
     * </pre>
     * </p>
     *
     * @param firstLineSupplier A function that generates the first line of the error message based on the input argument.
     * @param input The CustomArgumentInfo object containing the input information.
     * @param <B> The type of the argument.
     * @return A CustomArgumentException with a Minecraft-like error message.
     * @see #minecraftLikeException(Component, CustomArgumentInfo)
     * @see #minecraftLikeException(String, CustomArgumentInfo)
     */
    public static <B> CustomArgumentException minecraftLikeException(Function<B, Component> firstLineSupplier, CustomArgumentInfo<B> input) {
        Component result = firstLineSupplier.apply(input.currentInput())
            .colorIfAbsent(NamedTextColor.RED)
            .appendNewline()
            .append(failInputWithHere(input));
        return CustomArgumentException.fromAdventureComponent(result);
    }

    /**
     * Creates a custom argument adventure component exception with a Minecraft-like error message.
     * 
     * <p>This method is a convenience overload of {@link #minecraftLikeException(Function, CustomArgumentInfo)}
     * that accepts a static pre-built {@link Component} as the first line of the error message.</p>
     *
     * @param firstLine The static first line of the error message.
     * @param input The CustomArgumentInfo object containing the input information.
     * @param <B> The type of the argument.
     * @return A CustomArgumentException with a Minecraft-like error message.
     */
    public static <B> CustomArgumentException minecraftLikeException(Component firstLine, CustomArgumentInfo<B> input) {
        return minecraftLikeException(dummy -> firstLine, input);
    }

    /**
     * Creates a custom argument adventure component exception with a Minecraft-like error message.
     * 
     * <p>This method is a convenience overload of {@link #minecraftLikeException(Function, CustomArgumentInfo)}
     * that accepts a static {@link String} as the first line of the error message.</p>
     *
     * @param firstLine The static first line of the error message.
     * @param input The CustomArgumentInfo object containing the input information.
     * @param <B> The type of the argument.
     * @return A CustomArgumentException with a Minecraft-like error message.
     */
    public static <B> CustomArgumentException minecraftLikeException(String firstLine, CustomArgumentInfo<B> input) {
        return minecraftLikeException(Component.text(firstLine), input);
    }

    /**
     * Appends the last part of the input, highlight in red the failed argument, and adds a "here" reference.
     *
     * @param input The CustomArgumentInfo object containing the input information.
     * @param <B> The type of the argument.
     * @return A Component representing the "here" reference.
     */
    public static <B> Component failInputWithHere(CustomArgumentInfo<B> input) {
        String lastArg = input.input();
        String fullInput = input.previousArgs().fullInput();
        int lastArgIndex = fullInput.lastIndexOf(lastArg);
        String lastPartBeforeArg;
        
        if (lastArgIndex == -1 || lastArgIndex < 10) {
            lastPartBeforeArg = fullInput.substring(0, lastArgIndex);
        } else {
            lastPartBeforeArg = "..." + fullInput.substring(lastArgIndex - 10, lastArgIndex);
        }

        return Component.text()
            .append(Component.text(lastPartBeforeArg).color(NamedTextColor.GRAY))
            .append(Component.text(lastArg, NamedTextColor.RED).decorate(TextDecoration.UNDERLINED))
            .append(Component.text(new MessageBuilder().appendHere().toString(), NamedTextColor.RED))
            .build();
    }
}
