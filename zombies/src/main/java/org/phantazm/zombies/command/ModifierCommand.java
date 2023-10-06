package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class ModifierCommand extends Command {
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
                sender.sendMessage(Component.text("Cleared modifiers!", NamedTextColor.GREEN));
                return;
            }

            @Subst(Constants.NAMESPACE_OR_KEY)
            String modifier = context.get(modifierArgument);
            if (modifier.equals("none")) {
                sender.sendMessage(Component.text("You must specify a modifier!", NamedTextColor.RED));
                return;
            }

            if (!keyParser.isValidKey(modifier)) {
                sender.sendMessage(Component.text("Invalid modifier name!", NamedTextColor.RED));
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
                case MODIFIER_ENABLED -> sender.sendMessage(Component.text("Enabled modifier!", NamedTextColor.GREEN));
                case MODIFIER_DISABLED ->
                    sender.sendMessage(Component.text("Disabled modifier!", NamedTextColor.GREEN));
                case MODIFIER_ALREADY_ENABLED ->
                    sender.sendMessage(Component.text("Modifier already enabled!", NamedTextColor.RED));
                case CONFLICTING_MODIFIERS -> {
                    Component conflicts = Component.join(JoinConfiguration.commas(true),
                        result.conflictingModifiers().stream().map(ModifierComponent::displayName).toList());

                    sender.sendMessage(
                        Component.text("That modifier would conflict with the already-enabled modifier(s) ",
                            NamedTextColor.RED).append(conflicts));
                }
                case INVALID_MODIFIER -> sender.sendMessage(Component.text("An internal error occurred when " +
                    "attempting to enable that modifier; please report this incident to server staff!", NamedTextColor.RED));
            }
        }, modifierAction, modifierArgument);
    }
}
