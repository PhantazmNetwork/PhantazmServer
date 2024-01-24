package org.phantazm.zombies.command;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.element.core.key.KeyParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
import org.phantazm.loader.Loader;
import org.phantazm.zombies.modifier.ModifierCommandConfig;
import org.phantazm.zombies.modifier.ModifierComponent;
import org.phantazm.zombies.modifier.ModifierHandler;
import org.phantazm.zombies.modifier.ModifierUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModifierCommand extends Command {
    private enum Actions {
        SET,
        TOGGLE
    }

    public ModifierCommand(@NotNull KeyParser keyParser, @NotNull ModifierCommandConfig config,
        @NotNull Loader<ModifierHandler> modifierHandlerLoader) {
        super("modifier");

        Argument<Actions> modifierAction = ArgumentType.Enum("action", Actions.class)
            .setFormat(ArgumentEnum.Format.LOWER_CASED);

        Argument<String> modifierArgument = ArgumentType.Word("target")
            .setSuggestionCallback((sender, context, suggestion) -> {
                ModifierHandler modifierHandler = modifierHandlerLoader.first();

                for (Key key : modifierHandler.componentMap().keySet()) {
                    String name = key.asString();
                    suggestion.addEntry(new SuggestionEntry(name, Component.text(name)));
                }
            });

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            ModifierHandler modifierHandler = modifierHandlerLoader.first();
            PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);

            Actions actions = context.get(modifierAction);
            @Subst(Constants.NAMESPACE_OR_KEY)
            String modifier = context.get(modifierArgument);

            if (!keyParser.isValidKey(modifier)) {
                sender.sendMessage(config.invalidModifier());
                return;
            }

            Key key = keyParser.parseKey(modifier);
            if (!modifierHandler.componentMap().containsKey(key)) {
                TagResolver modifierTag = Placeholder.unparsed("modifier", key.asString());
                sender.sendMessage(MiniMessage.miniMessage().deserialize(config.modifierDoesNotExistFormat(),
                    modifierTag));
                return;
            }

            ModifierHandler.ModifierResult result = actions == Actions.SET ?
                modifierHandler.setModifier(playerView, key) : modifierHandler.toggleModifier(playerView, key);
            switch (result.status()) {
                case MODIFIER_ENABLED -> sender.sendMessage(config.modifierEnabled());
                case MODIFIER_DISABLED -> sender.sendMessage(config.modifierDisabled());
                case MODIFIER_ALREADY_ENABLED -> sender.sendMessage(config.modifierAlreadyEnabled());
                case CONFLICTING_MODIFIERS -> {
                    TagResolver conflictsTag = Placeholder.component("conflicts",
                        Component.join(JoinConfiguration.commas(true),
                            result.conflictingModifiers().stream().map(ModifierComponent::displayName).toList()));
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(config.showConflictsFormat(), conflictsTag));
                }
                case INVALID_MODIFIER -> sender.sendMessage(config.internalError());
                case NO_PERMISSIONS -> sender.sendMessage(config.noPermission());
            }
        }, modifierAction, modifierArgument);

        addSubcommand(new ListModifiers(modifierHandlerLoader, config));
        addSubcommand(new ClearModifiers(modifierHandlerLoader, config));
        addSubcommand(new LoadModifiers(modifierHandlerLoader, config));
    }

    private static class ListModifiers extends Command {

        private ListModifiers(Loader<ModifierHandler> modifierHandlerLoader, ModifierCommandConfig config) {
            super("list");

            addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
                ModifierHandler modifierHandler = modifierHandlerLoader.first();
                PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);
                Set<Key> modifiers = modifierHandler.getModifiers(playerView);
                Map<Key, ModifierComponent> map = modifierHandler.componentMap();

                Set<ModifierComponent> activeModifiers = modifiers.stream().map(map::get).collect(Collectors.toUnmodifiableSet());
                if (activeModifiers.isEmpty()) {
                    sender.sendMessage(config.noEnabledModifiers());
                    return;
                }

                String descriptor = ModifierUtils.modifierDescriptor(activeModifiers);

                TagResolver modifiersTag = Placeholder.component("modifiers",
                    Component.join(JoinConfiguration.commas(true),
                        activeModifiers.stream().map(ModifierComponent::displayName).toList()));
                TagResolver modifierKeyTag = Placeholder.unparsed("modifier_key", descriptor);

                sender.sendMessage(MiniMessage.miniMessage().deserialize(config.listFormat(), modifiersTag, modifierKeyTag));
            });
        }
    }

    private static class ClearModifiers extends Command {
        private ClearModifiers(Loader<ModifierHandler> modifierHandlerLoader, ModifierCommandConfig config) {
            super("clear");

            addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
                ModifierHandler modifierHandler = modifierHandlerLoader.first();
                PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);

                Set<Key> modifiers;
                if ((modifiers = modifierHandler.getModifiers(playerView)).isEmpty()) {
                    sender.sendMessage(config.noEnabledModifiers());
                    return;
                }

                modifierHandler.clearModifiers(playerView);

                TagResolver modifierCountTag = Placeholder.unparsed("modifier_count", Integer.toString(modifiers.size()));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(config.clearFormat(), modifierCountTag));
            });
        }
    }

    private static class LoadModifiers extends Command {
        private LoadModifiers(Loader<ModifierHandler> modifierHandlerLoader, ModifierCommandConfig config) {
            super("load");

            Argument<String> descriptorArgument = ArgumentType.Word("descriptor");
            addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
                ModifierHandler modifierHandler = modifierHandlerLoader.first();
                PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);
                String descriptor = context.get(descriptorArgument);
                if (descriptor.length() > 64) {
                    sender.sendMessage(config.modifierKeyTooLong());
                    return;
                }

                ModifierHandler.ModifierResult result = modifierHandler.setFromDescriptor(playerView, descriptor);
                switch (result.status()) {
                    case MODIFIER_ENABLED -> sender.sendMessage(config.enabledModifiers());
                    case CONFLICTING_MODIFIERS -> {
                        TagResolver conflictsTag = Placeholder.component("conflicts",
                            Component.join(JoinConfiguration.commas(true),
                                result.conflictingModifiers().stream().map(ModifierComponent::displayName).toList()));
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(config.showConflictsFormat(), conflictsTag));
                    }
                    case INVALID_MODIFIER -> sender.sendMessage(config.internalError());
                    case NO_PERMISSIONS -> sender.sendMessage(config.noPermission());
                }
            }, descriptorArgument);
        }
    }
}
