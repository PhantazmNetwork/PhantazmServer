package org.phantazm.zombies.modifier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public record ModifierCommandConfig(@NotNull String listFormat,
    @NotNull String clearFormat,
    @NotNull String showConflictsFormat,
    @NotNull Component internalError,
    @NotNull Component invalidModifier,
    @NotNull Component modifierEnabled,
    @NotNull Component modifierDisabled,
    @NotNull Component modifierAlreadyEnabled,
    @NotNull Component noEnabledModifiers,
    @NotNull Component enabledModifiers,
    @NotNull Component modifierKeyTooLong,
    @NotNull Component noPermission,
    @NotNull String modifierDoesNotExistFormat) {

    public static ModifierCommandConfig DEFAULT = new ModifierCommandConfig("Modifier Key: <modifier_key><br>Modifiers: <modifiers>",
        "Removed <modifier_count> modifiers", "Conflicting modifiers: <conflicts>",
        Component.text("An internal error occurred when executing this command, please report this incident to server staff!", NamedTextColor.RED),
        Component.text("Invalid modifier name!", NamedTextColor.RED), Component.text("Enabled modifier!", NamedTextColor.GREEN),
        Component.text("Disabled modifier!", NamedTextColor.GREEN),
        Component.text("Modifier is already enabled!", NamedTextColor.RED),
        Component.text("There are no enabled modifiers!", NamedTextColor.RED),
        Component.text("Enabled modifiers!", NamedTextColor.GREEN),
        Component.text("That modifier key is too long!", NamedTextColor.RED),
        Component.text("You do not have permission to use this modifier!", NamedTextColor.RED),
        "<red><modifier> does not exist!</red>");
}
