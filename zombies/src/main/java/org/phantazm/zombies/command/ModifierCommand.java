package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.modifier.ModifierComponent;
import org.phantazm.zombies.modifier.ModifierHandler;
import org.phantazm.zombies.modifier.ModifierUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModifierCommand extends Command {
    private static final Component INTERNAL_ERROR_MESSAGE = Component.text("An internal error occurred when " +
        "attempting to enable that modifier; please report this incident to server staff!", NamedTextColor.RED);

    private static final Component CLEARED_MODIFIERS_MESSAGE = Component.text("Cleared modifiers!",
        NamedTextColor.GREEN);

    private static final Component INVALID_MODIFIER_MESSAGE = Component.text("Invalid modifier name!",
        NamedTextColor.RED);

    private static final Component MODIFIER_ENABLED_MESSAGE = Component.text("Enabled modifier!",
        NamedTextColor.GREEN);

    private static final Component MODIFIER_DISABLED_MESSAGE = Component.text("Disabled modifier!",
        NamedTextColor.GREEN);

    private static final Component MODIFIER_ALREADY_ENABLED_MESSAGE = Component.text("Modifier already enabled!",
        NamedTextColor.RED);

    private static final Component NO_ENABLED_MODIFIERS = Component.text("You do not have any modifiers enabled!",
        NamedTextColor.RED);

    private static final Component ENABLED_MODIFIERS_MESSAGE = Component.text("Enabled modifiers!", NamedTextColor.GREEN);

    private enum Actions {
        SET,
        CLEAR,
        TOGGLE
    }

    public ModifierCommand(@NotNull ModifierHandler modifierHandler, @NotNull KeyParser keyParser) {
        super("modifier");

        Argument<Actions> modifierAction = ArgumentType.Enum("action", Actions.class)
            .setFormat(ArgumentEnum.Format.LOWER_CASED);

        Argument<String> modifierArgument = ArgumentType.Word("target")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Key key : modifierHandler.componentMap().keySet()) {
                    String name = key.asString();
                    suggestion.addEntry(new SuggestionEntry(name, Component.text(name)));
                }
            });

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);

            Actions actions = context.get(modifierAction);
            if (actions == Actions.CLEAR) {
                modifierHandler.clearModifiers(playerView);
                sender.sendMessage(CLEARED_MODIFIERS_MESSAGE);
                return;
            }

            @Subst(Constants.NAMESPACE_OR_KEY)
            String modifier = context.get(modifierArgument);

            if (!keyParser.isValidKey(modifier)) {
                sender.sendMessage(INVALID_MODIFIER_MESSAGE);
                return;
            }

            Key key = keyParser.parseKey(modifier);
            if (!modifierHandler.componentMap().containsKey(key)) {
                sender.sendMessage(Component.text("Modifier " + key + " does not exist!", NamedTextColor.RED));
                return;
            }

            ModifierHandler.ModifierResult result = actions == Actions.SET ?
                modifierHandler.setModifier(playerView, key) : modifierHandler.toggleModifier(playerView, key);
            switch (result.status()) {
                case MODIFIER_ENABLED -> sender.sendMessage(MODIFIER_ENABLED_MESSAGE);
                case MODIFIER_DISABLED -> sender.sendMessage(MODIFIER_DISABLED_MESSAGE);
                case MODIFIER_ALREADY_ENABLED -> sender.sendMessage(MODIFIER_ALREADY_ENABLED_MESSAGE);
                case CONFLICTING_MODIFIERS -> {
                    Component conflicts = Component.join(JoinConfiguration.commas(true),
                        result.conflictingModifiers().stream().map(ModifierComponent::displayName).toList());

                    sender.sendMessage(
                        Component.text("That modifier would conflict with some already enabled modifier(s): ",
                            NamedTextColor.RED).append(conflicts));
                }
                case INVALID_MODIFIER -> sender.sendMessage(INTERNAL_ERROR_MESSAGE);
            }
        }, modifierAction, modifierArgument);

        addSubcommand(new ListModifiers(modifierHandler));
        addSubcommand(new LoadModifiers(modifierHandler));
    }

    private static class ListModifiers extends Command {

        private ListModifiers(ModifierHandler modifierHandler) {
            super("list");

            addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
                PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);
                Set<Key> modifiers = modifierHandler.getModifiers(playerView);
                Map<Key, ModifierComponent> map = modifierHandler.componentMap();

                Set<ModifierComponent> activeModifiers = modifiers.stream().map(map::get).collect(Collectors.toUnmodifiableSet());
                if (activeModifiers.isEmpty()) {
                    sender.sendMessage(NO_ENABLED_MODIFIERS);
                    return;
                }

                String descriptor = ModifierUtils.modifierDescriptor(activeModifiers);
                sender.sendMessage(Component.text("Modifier key: ", NamedTextColor.GREEN)
                    .append(Component.text(descriptor, Style.style().decorate(TextDecoration.BOLD)
                        .color(NamedTextColor.GREEN).build())).appendNewline());

                sender.sendMessage(Component.join(JoinConfiguration.commas(true),
                    activeModifiers.stream().map(ModifierComponent::displayName).toList()));
            });
        }
    }

    private static class LoadModifiers extends Command {
        private LoadModifiers(ModifierHandler modifierHandler) {
            super("load");

            Argument<String> descriptorArgument = ArgumentType.Word("descriptor");
            addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
                PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);

                ModifierHandler.ModifierResult result = modifierHandler.setFromDescriptor(playerView,
                    context.get(descriptorArgument));
                switch (result.status()) {
                    case MODIFIER_ENABLED -> sender.sendMessage(ENABLED_MODIFIERS_MESSAGE);
                    case CONFLICTING_MODIFIERS -> sender.sendMessage(
                        Component.text("That modifier code has the following conflicting modifiers: ", NamedTextColor.GREEN)
                            .append(Component.join(JoinConfiguration.commas(true),
                                result.conflictingModifiers().stream().map(ModifierComponent::displayName).toList())));
                    case INVALID_MODIFIER -> sender.sendMessage(INTERNAL_ERROR_MESSAGE);
                }

            }, descriptorArgument);
        }
    }
}
