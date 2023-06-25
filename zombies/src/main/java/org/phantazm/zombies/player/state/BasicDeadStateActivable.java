package org.phantazm.zombies.player.state;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.scoreboard.TabList;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.commons.MiniMessageUtils;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.player.PlayerView;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.player.state.context.DeadPlayerStateContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BasicDeadStateActivable implements Activable {
    private static final TagResolver[] EMPTY_TAG_RESOLVER_ARRAY = new TagResolver[0];
    private final InventoryAccessRegistry accessRegistry;
    private final DeadPlayerStateContext context;
    private final Instance instance;
    private final PlayerView playerView;
    private final MapSettingsInfo settings;
    private final Sidebar sidebar;
    private final TabList tabList;
    private final ZombiesPlayerMapStats stats;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BasicDeadStateActivable(@NotNull InventoryAccessRegistry accessRegistry,
            @NotNull DeadPlayerStateContext context, @NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull MapSettingsInfo settings, @NotNull Sidebar sidebar, @NotNull TabList tabList,
            @NotNull ZombiesPlayerMapStats stats) {
        this.accessRegistry = Objects.requireNonNull(accessRegistry, "accessRegistry");
        this.context = Objects.requireNonNull(context, "context");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.tabList = Objects.requireNonNull(tabList, "tabList");
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public void start() {
        playerView.getPlayer().ifPresent(player -> {
            player.setInvisible(true);
            player.setGameMode(GameMode.SPECTATOR);
            sidebar.addViewer(player);
            tabList.addViewer(player);
        });
        playerView.getDisplayName().thenAccept(displayName -> {
            instance.sendMessage(buildDeathMessage(displayName));
        });

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
        });

        accessRegistry.switchAccess(null);
    }

    private @NotNull Component buildDeathMessage(@NotNull Component displayName) {
        if (context.isRejoin()) {
            TagResolver rejoinerPlaceholder = Placeholder.component("rejoiner", displayName);
            return miniMessage.deserialize(settings.rejoinMessageFormat(), rejoinerPlaceholder);
        }

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

        return miniMessage.deserialize(settings.deathMessageFormat(), tagResolvers.toArray(EMPTY_TAG_RESOLVER_ARRAY));
    }

}
