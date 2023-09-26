package org.phantazm.zombies.player.state;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tick.Activable;
import org.phantazm.commons.MiniMessageUtils;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryObjectGroup;
import org.phantazm.core.player.PlayerView;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.player.ZombiesPlayerMeta;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;

import java.util.*;

public class BasicDeadStateActivable implements Activable {
    private static final TagResolver[] EMPTY_TAG_RESOLVER_ARRAY = new TagResolver[0];
    private final InventoryAccessRegistry accessRegistry;
    private final DeadPlayerStateContext context;
    private final Instance instance;
    private final PlayerView playerView;
    private final ZombiesPlayerMeta meta;
    private final MapSettingsInfo settings;
    private final Sidebar sidebar;
    private final TabList tabList;
    private final BelowNameTag belowNameTag;
    private final ZombiesPlayerMapStats stats;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BasicDeadStateActivable(@NotNull InventoryAccessRegistry accessRegistry,
        @NotNull DeadPlayerStateContext context, @NotNull Instance instance, @NotNull PlayerView playerView,
        @NotNull ZombiesPlayerMeta meta, @NotNull MapSettingsInfo settings, @NotNull Sidebar sidebar,
        @NotNull TabList tabList, @NotNull BelowNameTag belowNameTag, @NotNull ZombiesPlayerMapStats stats) {
        this.accessRegistry = Objects.requireNonNull(accessRegistry);
        this.context = Objects.requireNonNull(context);
        this.instance = Objects.requireNonNull(instance);
        this.playerView = Objects.requireNonNull(playerView);
        this.meta = Objects.requireNonNull(meta);
        this.settings = Objects.requireNonNull(settings);
        this.sidebar = Objects.requireNonNull(sidebar);
        this.tabList = Objects.requireNonNull(tabList);
        this.belowNameTag = Objects.requireNonNull(belowNameTag);
        this.stats = Objects.requireNonNull(stats);
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.heal();
            player.setInvisible(true);
            player.setGameMode(GameMode.SPECTATOR);
            sidebar.addViewer(player);
            tabList.addViewer(player);
            belowNameTag.addViewer(player);
        });
        
        playerView.getDisplayName().thenAccept(displayName -> {
            if (context.isRejoin()) {
                TagResolver rejoinerPlaceholder = Placeholder.component("rejoiner", displayName);
                instance.sendMessage(miniMessage.deserialize(settings.rejoinMessageFormat(), rejoinerPlaceholder));
                return;
            }

            TagResolver[] tagResolvers = getTagResolvers(displayName);
            if (meta.isInGame()) {
                playerView.getPlayer().ifPresent(player -> {
                    player.sendMessage(miniMessage.deserialize(settings.deathMessageToKilledFormat(), tagResolvers));
                });
            }

            Set<Player> players = new HashSet<>(instance.getPlayers());
            playerView.getPlayer().ifPresent(players::remove);
            Audience instanceAudience = PacketGroupingAudience.of(players);
            instanceAudience.sendMessage(miniMessage.deserialize(settings.deathMessageToOthersFormat(), tagResolvers));
        });

        Point deathLocation = context.getDeathLocation();
        if (deathLocation != null) {
            instance.playSound(settings.deathSound(), deathLocation.x(), deathLocation.y(), deathLocation.z());
        }

        InventoryAccess aliveAccess = accessRegistry.getAccess(InventoryKeys.ALIVE_ACCESS);
        for (Key key : settings.lostOnDeath()) {
            InventoryObjectGroup group = aliveAccess.groups().get(key);
            if (group == null) {
                continue;
            }

            InventoryObject defaultObject = group.defaultObject();
            for (int slot : group.getSlots()) {
                if (defaultObject == null) {
                    accessRegistry.removeObject(aliveAccess, slot);
                } else {
                    accessRegistry.replaceObject(aliveAccess, slot, defaultObject);
                }
            }
        }

        stats.setDeaths(stats.getDeaths() + 1);
        accessRegistry.switchAccess(InventoryKeys.DEAD_ACCESS);
    }

    @Override
    public void end() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(false);
            player.setGameMode(GameMode.ADVENTURE);
            sidebar.addViewer(player);
            tabList.addViewer(player);
            belowNameTag.addViewer(player);
        });

        accessRegistry.switchAccess(null);
    }

    private TagResolver[] getTagResolvers(@NotNull Component displayName) {
        boolean knockedRoomPresent = context.getDeathRoomName().isPresent();
        boolean killerPresent = context.getKiller().isPresent();
        List<TagResolver> tagResolvers = new ArrayList<>();
        tagResolvers.add(Placeholder.component("killed", displayName));
        tagResolvers.add(MiniMessageUtils.optional("death_room_present", knockedRoomPresent));
        tagResolvers.add(MiniMessageUtils.optional("killer_present", killerPresent));
        if (knockedRoomPresent) {
            tagResolvers.add(Placeholder.component("death_room", context.getDeathRoomName().get()));
        }
        if (killerPresent) {
            tagResolvers.add(Placeholder.component("killer", context.getKiller().get()));
        }

        return tagResolvers.toArray(EMPTY_TAG_RESOLVER_ARRAY);
    }

}
