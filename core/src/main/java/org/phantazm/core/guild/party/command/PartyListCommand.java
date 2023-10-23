package org.phantazm.core.guild.party.command;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MiniMessageUtils;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.guild.party.PartyMember;
import org.phantazm.core.player.PlayerView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PartyListCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyListCommand.class);

    private static final CompletableFuture<?>[] EMPTY_COMPLETABLE_FUTURE_ARRAY = new CompletableFuture[0];

    private PartyListCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command listCommand(@NotNull PartyCommandConfig config, @NotNull MiniMessage miniMessage,
        @NotNull Map<? super UUID, Party> partyMap) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(miniMessage);
        Objects.requireNonNull(partyMap);

        Command command = new Command("list");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(config.mustBeAPlayer());
                return false;
            }

            Party party = partyMap.get(player.getUuid());
            if (party == null) {
                sender.sendMessage(config.notInParty());
                return false;
            }

            return true;
        }, (sender, context) -> {
            Party party = partyMap.get(((Player) sender).getUuid());
            int memberCount = party.getMemberManager().getMembers().size();
            List<CompletableFuture<? extends Component>> displayNameFutures = new ArrayList<>(memberCount);
            BooleanList online = new BooleanArrayList(memberCount);
            for (PartyMember member : party.getMemberManager().getMembers().values()) {
                PlayerView playerView = member.getPlayerView();
                displayNameFutures.add(playerView.getDisplayName());
                online.add(playerView.getPlayer().isPresent());
            }
            displayNameFutures.add(party.getOwner().get().getPlayerView().getDisplayName());

            CompletableFuture.allOf(displayNameFutures.toArray(EMPTY_COMPLETABLE_FUTURE_ARRAY))
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Exception while sending list message", throwable);
                        return;
                    }

                    Collection<Component> displayNames = new ArrayList<>(memberCount);
                    int onlineCount = 0;
                    for (int i = 0; i < memberCount; ++i) {
                        TagResolver memberPlaceholder =
                            Placeholder.component("member", displayNameFutures.get(i).join());
                        if (online.getBoolean(i)) {
                            ++onlineCount;
                            displayNames.add(
                                miniMessage.deserialize(config.onlineMemberFormat(), memberPlaceholder));
                        } else {
                            displayNames.add(
                                miniMessage.deserialize(config.offlineMemberFormat(), memberPlaceholder));
                        }
                    }

                    TagResolver ownerPlaceholder = Placeholder.component("owner",
                        displayNameFutures.get(displayNameFutures.size() - 1).join());
                    TagResolver memberCountPlaceholder =
                        Placeholder.component("member_count", Component.text(memberCount));
                    TagResolver onlineCountPlaceholder =
                        Placeholder.component("online_count", Component.text(onlineCount));
                    TagResolver offlineCountPlaceholder =
                        Placeholder.component("offline_count", Component.text(memberCount - onlineCount));
                    TagResolver membersPlaceholder = MiniMessageUtils.list("members", displayNames);

                    Component message =
                        miniMessage.deserialize(config.listFormat(), ownerPlaceholder, membersPlaceholder,
                            onlineCountPlaceholder, offlineCountPlaceholder, memberCountPlaceholder);
                    sender.sendMessage(message);
                });
        });

        return command;
    }

}
