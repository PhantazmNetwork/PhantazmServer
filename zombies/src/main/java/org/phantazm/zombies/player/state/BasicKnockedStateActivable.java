package org.phantazm.zombies.player.state;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.commons.MiniMessageUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;
import org.phantazm.zombies.player.state.revive.ReviveHandler;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BasicKnockedStateActivable implements Activable {

    private static final TagResolver[] EMPTY_TAG_RESOLVER_ARRAY = new TagResolver[0];

    private final KnockedPlayerStateContext context;

    private final Instance instance;

    private final PlayerView playerView;

    private final ZombiesPlayerActionBar actionBar;

    private final MapSettingsInfo settings;

    private final ReviveHandler reviveHandler;

    private final TickFormatter tickFormatter;

    private final Sidebar sidebar;

    private final TabList tabList;

    private final ZombiesPlayerMapStats stats;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BasicKnockedStateActivable(@NotNull KnockedPlayerStateContext context, @NotNull Instance instance,
            @NotNull PlayerView playerView, @NotNull ZombiesPlayerActionBar actionBar,
            @NotNull MapSettingsInfo settings,
            @NotNull ReviveHandler reviveHandler,
            @NotNull TickFormatter tickFormatter, @NotNull Sidebar sidebar, @NotNull TabList tabList,
            @NotNull ZombiesPlayerMapStats stats) {
        this.context = Objects.requireNonNull(context, "context");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.actionBar = Objects.requireNonNull(actionBar, "actionBar");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.reviveHandler = Objects.requireNonNull(reviveHandler, "reviveHandler");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.tabList = Objects.requireNonNull(tabList, "tabList");
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(true);
            player.setAllowFlying(true);
            player.setFlyingSpeed(0F);
            player.setFlying(true);
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0F);
            player.setGameMode(GameMode.SPECTATOR);
            sidebar.addViewer(player);
            tabList.addViewer(player);
            context.getVehicle().addPassenger(player);
        });
        playerView.getDisplayName().thenAccept(displayName -> {
            Set<Player> players = new HashSet<>(instance.getPlayers());
            TagResolver[] tagResolvers = getTagResolvers(displayName);
            playerView.getPlayer().ifPresent(player -> {
                players.remove(player);
                player.sendMessage(miniMessage.deserialize(settings.knockedMessageToKnockedFormat(), tagResolvers));
            });
            Audience instanceAudience = PacketGroupingAudience.of(players);
            instanceAudience.sendMessage(
                    miniMessage.deserialize(settings.knockedMessageToOthersFormat(), tagResolvers));
            instanceAudience.sendTitlePart(TitlePart.TITLE,
                    miniMessage.deserialize(settings.knockedTitleFormat(), tagResolvers));
            instanceAudience.sendTitlePart(TitlePart.SUBTITLE,
                    miniMessage.deserialize(settings.knockedSubtitleFormat(), tagResolvers));
        });

        stats.setKnocks(stats.getKnocks() + 1);
    }

    @Override
    public void tick(long time) {
        if (reviveHandler.isReviving()) {
            reviveHandler.getReviver().ifPresent(reviver -> {
                Wrapper<Component> knockedDisplayName = Wrapper.ofNull(), reviverDisplayName = Wrapper.ofNull();
                CompletableFuture<Void> knockedFuture = playerView.getDisplayName().thenAccept(knockedDisplayName::set);
                CompletableFuture<Void> reviverFuture =
                        reviver.module().getPlayerView().getDisplayName().thenAccept(reviverDisplayName::set);
                CompletableFuture.allOf(knockedFuture, reviverFuture).thenAccept(v -> {
                    sendReviveStatus(reviver, knockedDisplayName.get(), reviverDisplayName.get());
                });
            });
        }
        else {
            sendDyingStatus();
        }
    }

    @Override
    public void end() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setFlying(false);
            player.setAllowFlying(false);
            player.setFlyingSpeed(0.4F);
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1F);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
            tabList.addViewer(player);
            context.getVehicle().remove();
            player.teleport(Pos.fromPoint(context.getKnockLocation()));
        });
        actionBar.sendActionBar(Component.empty(), ZombiesPlayerActionBar.REVIVE_MESSAGE_CLEAR_PRIORITY);
    }

    private void sendReviveStatus(@NotNull ZombiesPlayer reviver, @NotNull Component knockedDisplayName,
            @NotNull Component reviverDisplayName) {
        reviver.module().getPlayerView().getPlayer().ifPresent(reviverPlayer -> {
            TagResolver knockedNamePlaceholder = Placeholder.component("knocked", knockedDisplayName);
            TagResolver timePlaceholder =
                    Placeholder.unparsed("time", tickFormatter.format(reviveHandler.getTicksUntilRevive()));
            Component message = miniMessage.deserialize(settings.reviveStatusToReviverFormat(), knockedNamePlaceholder,
                    timePlaceholder);
            reviver.module().getActionBar().sendActionBar(message, ZombiesPlayerActionBar.REVIVE_MESSAGE_PRIORITY);
        });
        playerView.getPlayer().ifPresent(player -> {
            TagResolver reviverNamePlaceholder = Placeholder.component("reviver", reviverDisplayName);
            TagResolver timePlaceholder =
                    Placeholder.unparsed("time", tickFormatter.format(reviveHandler.getTicksUntilRevive()));
            Component message = miniMessage.deserialize(settings.reviveStatusToKnockedFormat(), reviverNamePlaceholder,
                    timePlaceholder);
            actionBar.sendActionBar(message, ZombiesPlayerActionBar.REVIVE_MESSAGE_PRIORITY);
        });
    }

    private void sendDyingStatus() {
        playerView.getPlayer().ifPresent(player -> {
            TagResolver timePlaceholder = Placeholder.component("time",
                    Component.text(tickFormatter.format(Math.max(reviveHandler.getTicksUntilDeath(), 0))));
            actionBar.sendActionBar(miniMessage.deserialize(settings.dyingStatusFormat(), timePlaceholder), ZombiesPlayerActionBar.REVIVE_MESSAGE_PRIORITY);
        });
    }

    private TagResolver[] getTagResolvers(@NotNull Component displayName) {
        boolean knockedRoomPresent = context.getKnockRoom().isPresent();
        boolean killerPresent = context.getKiller().isPresent();
        List<TagResolver> tagResolvers = new ArrayList<>();
        tagResolvers.add(Placeholder.component("knocked", displayName));
        tagResolvers.add(MiniMessageUtils.optional("knocked_room_present", knockedRoomPresent));
        tagResolvers.add(MiniMessageUtils.optional("killer_present", killerPresent));
        tagResolvers.add(Placeholder.unparsed("time", tickFormatter.format(reviveHandler.getTicksUntilDeath())));
        if (knockedRoomPresent) {
            tagResolvers.add(Placeholder.component("knocked_room", context.getKnockRoom().get()));
        }
        if (killerPresent) {
            tagResolvers.add(Placeholder.component("killer", context.getKiller().get()));
        }

        return tagResolvers.toArray(EMPTY_TAG_RESOLVER_ARRAY);
    }

}
