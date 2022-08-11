package com.github.phantazmnetwork.core.gui;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Handles clicks in a GUI.
 */
public interface ClickHandler {
    /**
     * Handles a click, performing any required actions.
     *
     * @param owner     the GUI responsible for calling this GuiItem
     * @param slot      the slot the GuiItem is present in
     * @param clickType the type of click that occurs
     */
    void handleClick(@NotNull Gui owner, int slot, @NotNull GuiItem.ClickType clickType);

    /**
     * Returns a new ClickHandler which will invoke the current ClickHandler only if the required ClickType is supplied.
     *
     * @param requiredType the necessary ClickType
     * @return a new ClickHandler instance
     */
    default @NotNull ClickHandler filter(@NotNull GuiItem.ClickType requiredType) {
        Objects.requireNonNull(requiredType, "requiredType");

        return (owner, slot, clickType) -> {
            if (clickType == requiredType) {
                ClickHandler.this.handleClick(owner, slot, clickType);
            }
        };
    }

    /**
     * Returns a new ClickHandler which will invoke the current ClickHandler only if the given predicate is satisfied.
     *
     * @param clickPredicate the predicate to test against
     * @return a new ClickHandler instance
     */
    default @NotNull ClickHandler filter(@NotNull Predicate<? super GuiItem.ClickType> clickPredicate) {
        Objects.requireNonNull(clickPredicate, "requiredType");

        return (owner, slot, clickType) -> {
            if (clickPredicate.test(clickType)) {
                ClickHandler.this.handleClick(owner, slot, clickType);
            }
        };
    }
}
