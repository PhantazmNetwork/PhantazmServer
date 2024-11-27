package org.phantazm.core;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import net.minestom.server.command.builder.suggestion.SuggestionCallback;

/**
 * Static utility methods and fields relating to commands.
 */
public final class CommandUtils {
    /**
     * The shared {@link CommandCondition} that is only usable by {@link Player} senders.
     */
    public static final CommandCondition PLAYER_CONDITION = (sender, commandString) -> sender instanceof Player;

    private static final Comparator<SuggestionEntry> SUGGESTION_ENTRY_COMPARATOR =
        Comparator.comparing(SuggestionEntry::getEntry);

    private CommandUtils() {
    }

    /**
     * "Tab-completes" an argument. Generally used inside a {@link SuggestionCallback}. {@link SuggestionEntry}
     * instances that match the input string are added to the provided {@link Suggestion}, optionally including a
     * tooltip.
     * <p>
     * Value matching is based on whether a candidate value (an element of {@code possibleValues}, mapped to a string
     * using {@code matchExtractor}) contains the current (maybe partially complete) argument. The "current argument" is
     * a substring of the value returned by calling {@link Suggestion#getInput()} on {@code suggestion}.
     * <p>
     * Suggestions will be sorted by the natural order of their values.
     *
     * @param suggestion      the Suggestion instance to which SuggestionEntry instances are added
     * @param possibleValues  some number of objects to match against
     * @param valueExtractor  a function to map {@code T} to String, applied to each element of {@code possibleValues}.
     *                        This should represent the "whole object" and will be used to create a new SuggestionEntry,
     *                        when matching. If this function returns {@code null}, no entry will be added to the
     *                        suggestion
     * @param matchExtractor  a function to map {@code T} to String, applied to each element of {@code possibleValues};
     *                        if the returned string contains the input string, it is considered matching. If
     *                        {@code null} (or the function returns {@code null}), the result of {@code valueExtractor}
     *                        will be used instead
     * @param tooltipFunction a function to generate {@link Component}s representing the tooltip of each suggestion. If
     *                        {@code null} (or the function returns {@code null}) no tooltip will be used
     * @param <T>             the type of object to iterate
     */
    public static <T> void tabComplete(@NotNull Suggestion suggestion,
        @NotNull Iterable<? extends @NotNull T> possibleValues,
        @NotNull Function<? super @NotNull T, ? extends @Nullable String> valueExtractor,
        @Nullable Function<? super @NotNull T, ? extends @Nullable String> matchExtractor,
        @Nullable Function<? super @NotNull T, ? extends @Nullable Component> tooltipFunction) {
        String inputString = suggestion.getInput().toLowerCase(Locale.ROOT);

        int start = suggestion.getStart() - 1;
        int length = suggestion.getLength();
        if (start < 0 || length < 0 || start + length > inputString.length())
            return;

        inputString = inputString.substring(start, start + length).trim();

        List<SuggestionEntry> entryList = null;
        for (T value : possibleValues) {
            String valueString = valueExtractor.apply(value);
            if (valueString == null) continue;

            if (inputString.isEmpty()) {
                if (entryList == null) entryList = new ArrayList<>();

                entryList.add(tooltipFunction == null ? new SuggestionEntry(valueString) :
                    new SuggestionEntry(valueString, tooltipFunction.apply(value)));
                continue;
            }

            String matchString = (matchExtractor == null ? valueString :
                Objects.requireNonNullElse(matchExtractor.apply(value), valueString)).toLowerCase(Locale.ROOT);

            if (matchString.contains(inputString)) {
                if (entryList == null) entryList = new ArrayList<>();

                entryList.add(tooltipFunction == null ? new SuggestionEntry(valueString) :
                    new SuggestionEntry(valueString, tooltipFunction.apply(value)));
            }
        }

        if (entryList == null) return;

        entryList.sort(SUGGESTION_ENTRY_COMPARATOR);
        for (SuggestionEntry entry : entryList) {
            suggestion.addEntry(entry);
        }
    }

    /**
     * Convenience overload of {@link CommandUtils#tabComplete(Suggestion, Iterable, Function, Function, Function)}.
     * {@code valueExtractor} is {@link Objects#toString(Object)}.
     *
     * @param suggestion      the Suggestion instance to which SuggestionEntry instances are added
     * @param possibleValues  some number of objects to match against
     * @param matchExtractor  a function to map {@code T} to String, applied to each element of {@code possibleValues};
     *                        if the returned string contains the input string, it is considered matching. If
     *                        {@code null} (or the function returns {@code null}), the result of {@code valueExtractor}
     *                        will be used instead
     * @param tooltipFunction a function to generate {@link Component}s representing the tooltip of each suggestion. If
     *                        {@code null} (or the function returns {@code null}) no tooltip will be used
     * @param <T>             the type of object to iterate
     */
    public static <T> void tabComplete(@NotNull Suggestion suggestion,
        @NotNull Iterable<? extends @NotNull T> possibleValues,
        @Nullable Function<? super T, ? extends String> matchExtractor,
        @Nullable Function<? super T, ? extends Component> tooltipFunction) {
        tabComplete(suggestion, possibleValues, Objects::toString, matchExtractor, tooltipFunction);
    }

    /**
     * Convenience overload of {@link CommandUtils#tabComplete(Suggestion, Iterable, Function, Function, Function)}.
     * {@code valueExtractor} is {@link Objects#toString(Object)}, and {@code tooltipFunction} is {@code null}.
     *
     * @param suggestion     the Suggestion instance to which SuggestionEntry instances are added
     * @param possibleValues some number of objects to match against
     * @param matchExtractor a function to map {@code T} to String, applied to each element of {@code possibleValues};
     *                       if the returned string contains the input string, it is considered matching. If
     *                       {@code null} (or the function returns {@code null}), the result of {@code valueExtractor}
     *                       will be used instead
     * @param <T>            the type of object to iterate
     */
    public static <T> void tabComplete(@NotNull Suggestion suggestion,
        @NotNull Iterable<? extends @NotNull T> possibleValues,
        @Nullable Function<? super T, ? extends String> matchExtractor) {
        tabComplete(suggestion, possibleValues, Objects::toString, matchExtractor, null);
    }

    /**
     * Convenience overload of {@link CommandUtils#tabComplete(Suggestion, Iterable, Function, Function, Function)}.
     * {@code valueExtractor} is {@link Objects#toString(Object)}, {@code tooltipFunction} is {@code null}, and
     * {@code matchExtractor} is {@code null}.
     *
     * @param suggestion     the Suggestion instance to which SuggestionEntry instances are added
     * @param possibleValues some number of objects to match against matching. if {@code null}, use the result of
     *                       {@code valueExtractor} instead
     * @param <T>            the type of object to iterate
     */
    public static <T> void tabComplete(@NotNull Suggestion suggestion,
        @NotNull Iterable<? extends @NotNull T> possibleValues) {
        tabComplete(suggestion, possibleValues, Objects::toString, null, null);
    }
}
