package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
import org.phantazm.zombies.modifier.ModifierHandler;

import java.util.Set;

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

        Argument<String> modifierArgument = ArgumentType.Word("modifier")
            .setSuggestionCallback((sender, context, suggestion) -> {
                for (Key key : modifierHandler.modifiers().keySet()) {
                    String name = key.asString();
                    suggestion.addEntry(new SuggestionEntry(name, Component.text(name)));
                }
            })
            .setDefaultValue("");


        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);

            Actions actions = context.get(modifierAction);
            if (actions == Actions.CLEAR) {
                modifierHandler.clearModifiers(playerView);
                return;
            }

            @Subst(Constants.NAMESPACE_OR_KEY)
            String modifier = context.get(modifierArgument);
            if (modifier.isEmpty()) {
                sender.sendMessage(Component.text("You must specify a modifier!", NamedTextColor.RED));
                return;
            }

            if (!keyParser.isValidKey(modifier)) {
                sender.sendMessage(Component.text("Invalid modifier name!", NamedTextColor.RED));
                return;
            }

            Key key = keyParser.parseKey(modifier);
            if (!modifierHandler.modifiers().containsKey(key)) {
                sender.sendMessage(Component.text("Modifier " + key + " does not exist!", NamedTextColor.RED));
                return;
            }

            if (actions == Actions.SET) {
                modifierHandler.setModifiers(playerView, Set.of(key));
            } else {
                modifierHandler.toggleModifier(playerView, key);
            }
        }, modifierAction, modifierArgument);
    }
}
