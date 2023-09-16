package org.phantazm.server.command.server;

import com.github.steanky.element.core.key.Constants;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.permission.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.scene2.Scene;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.time.AnalogTickFormatter;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.server.config.server.ZombiesGamereportConfig;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.EndStage;
import org.phantazm.zombies.stage.InGameStage;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;

import java.time.Instant;
import java.util.*;

public class GamereportCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.gamereport");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Map<Key, PageFormatter> pageFormatters;

    public GamereportCommand(@NotNull ZombiesGamereportConfig config) {
        super("gamereport", PERMISSION);

        pageFormatters = Map.of(Key.key(Namespaces.PHANTAZM, "zombies"), new ZombiesPageFormatter(config));

        Argument<String> typeArgument = ArgumentType.String("type-key");
        typeArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (Key key : pageFormatters.keySet()) {
                suggestion.addEntry(
                    new SuggestionEntry(key.asString(), Component.text(key.asString())));
            }
        });

        Argument<Integer> pageArgument = ArgumentType.Integer("page").setDefaultValue(1);
        addSyntax((sender, context) -> {
            @Subst(Constants.NAMESPACE_OR_KEY)
            String key = context.get(typeArgument);

            if (!Key.parseable(key)) {
                sender.sendMessage(Component.text("Invalid key!").color(NamedTextColor.RED));
                return;
            }

            Key routerKey = Key.key(key);
            PageFormatter pageFormatter = pageFormatters.get(routerKey);
            if (pageFormatter == null) {
                sender.sendMessage(Component.text("Target type does not exist!").color(NamedTextColor.RED));
                return;
            }

            Set<? extends Scene> scenes = pageFormatter.scenes();

            if (scenes.isEmpty()) {
                sender.sendMessage(Component.text("There are no scenes of this type!").color(NamedTextColor.RED));
                return;
            }

            int pageCount = (int) Math.ceil(scenes.size() / (double) pageFormatter.itemsPerPage());
            int page = context.get(pageArgument);
            if (page < 1 || page > pageCount) {
                sender.sendMessage(Component.text("Target page does not exist!").color(NamedTextColor.RED));
                return;
            }

            sender.sendMessage(pageFormatter.page(page, List.copyOf(scenes)));
        }, typeArgument, pageArgument);
    }

    private interface PageFormatter {
        @NotNull Component page(int pageIndex, @NotNull List<? extends Scene> scenes);

        int itemsPerPage();

        @NotNull Set<? extends Scene> scenes();
    }

    private record ZombiesPageFormatter(ZombiesGamereportConfig config) implements PageFormatter {
        private static final int ITEMS_PER_PAGE = 3;
        private static final TickFormatter TIME_FORMATTER = new AnalogTickFormatter(new AnalogTickFormatter.Data(true));

        @Override
        public @NotNull Component page(int page, @NotNull List<? extends Scene> scenes) {
            int maxPages = (int) Math.ceil(scenes.size() / (double) ITEMS_PER_PAGE);

            List<Component> gameEntries = new ArrayList<>(scenes.size());

            TagResolver totalGamesTag = Placeholder.unparsed("total_games", Integer.toString(scenes.size()));

            for (int i = (page - 1) * ITEMS_PER_PAGE, j = 0; i < scenes.size() && j < ITEMS_PER_PAGE; i++, j++) {
                ZombiesScene zombiesScene = (ZombiesScene) scenes.get(i);

                TagResolver currentGameTag = Placeholder.unparsed("current_game", Integer.toString(i + 1));
                TagResolver gameUUIDTag = Placeholder.unparsed("game_uuid", zombiesScene.identity().toString());

                List<Component> playerNames = new ArrayList<>(zombiesScene.mapSettingsInfo().maxPlayers());
                for (ZombiesPlayer player : zombiesScene.managedPlayers().values()) {
                    player.getPlayer().ifPresent(actualPlayer -> {
                        Component displayName = actualPlayer.getDisplayName();
                        playerNames.add(displayName == null ? Component.text(actualPlayer.getUsername()) : displayName);
                    });
                }

                Component playerList = Component.join(JoinConfiguration.commas(true), playerNames);
                TagResolver playerListTag = Placeholder.component("player_list", playerList);
                TagResolver mapNameTag =
                    Placeholder.component("map_name", zombiesScene.mapSettingsInfo().displayName());

                Component gameState;
                Stage currentStage = zombiesScene.currentStage();

                TagResolver currentRoundTag = Placeholder.parsed("current_round",
                    Integer.toString(zombiesScene.map().roundHandler().currentRoundIndex() + 1));

                if (currentStage == null || currentStage.key().equals(StageKeys.IDLE_STAGE)) {
                    gameState = MINI_MESSAGE.deserialize(config.idleStageFormat());
                } else if (currentStage.key().equals(StageKeys.COUNTDOWN)) {
                    gameState = MINI_MESSAGE.deserialize(config.countdownStageFormat());
                } else if (currentStage.key().equals(StageKeys.IN_GAME)) {
                    InGameStage inGameStage = (InGameStage) currentStage;

                    TagResolver gameTimeTag =
                        Placeholder.unparsed("game_time", TIME_FORMATTER.format(inGameStage.ticksSinceStart()));

                    gameState = MINI_MESSAGE.deserialize(config.inGameFormat(), currentRoundTag, gameTimeTag);
                } else if (currentStage.key().equals(StageKeys.END)) {
                    EndStage endStage = (EndStage) currentStage;

                    TagResolver gameTimeTag =
                        Placeholder.parsed("game_time", TIME_FORMATTER.format(endStage.ticksSinceStart()));
                    gameState = MINI_MESSAGE.deserialize(config.endedFormat(),
                        Formatter.choice("result", endStage.hasWon() ? 1 : 0), currentRoundTag, gameTimeTag);
                } else {
                    gameState = Component.empty();
                }

                TagResolver gameStateTag = Placeholder.component("game_state", gameState);

                TagResolver warpTag = TagResolver.resolver("warp",
                    Tag.styling(ClickEvent.runCommand("/ghost " + zombiesScene.identity())));

                gameEntries.add(
                    MINI_MESSAGE.deserialize(config.gameEntryFormat(), totalGamesTag, currentGameTag, gameUUIDTag,
                        playerListTag, mapNameTag, gameStateTag, warpTag));
            }

            Component gameList = Component.join(JoinConfiguration.newlines(), gameEntries);

            TagResolver currentPageTag = Placeholder.unparsed("current_page", Integer.toString(page));
            TagResolver maxPagesTag = Placeholder.unparsed("max_pages", Integer.toString(maxPages));
            TagResolver timeTag = Placeholder.unparsed("current_time", Instant.now().toString());
            TagResolver gameListTag = Placeholder.component("game_list", gameList);

            Component nextPageOptionalComponent;
            if (page * ITEMS_PER_PAGE >= scenes.size()) {
                nextPageOptionalComponent = Component.empty();
            } else {
                TagResolver pageAdvancingResolver = TagResolver.resolver("next_page",
                    Tag.styling(ClickEvent.runCommand("/gamereport phantazm:zombies " + (page + 1))));

                nextPageOptionalComponent = MINI_MESSAGE.deserialize(config.nextPageFormat(), pageAdvancingResolver);
            }

            Component previousPageOptionalComponent;
            if (page == 1) {
                previousPageOptionalComponent = Component.empty();
            } else {
                TagResolver pageRetractingResolver = TagResolver.resolver("previous_page",
                    Tag.styling(ClickEvent.runCommand("/gamereport phantazm:zombies " + (page - 1))));

                previousPageOptionalComponent =
                    MINI_MESSAGE.deserialize(config.previousPageFormat(), pageRetractingResolver);
            }

            TagResolver nextPageOptionalTag = Placeholder.component("next_page_optional", nextPageOptionalComponent);
            TagResolver previousPageOptionalTag =
                Placeholder.component("previous_page_optional", previousPageOptionalComponent);

            return MINI_MESSAGE.deserialize(config.pageFormat(), currentPageTag, maxPagesTag, timeTag, totalGamesTag,
                gameListTag, nextPageOptionalTag, previousPageOptionalTag);
        }

        @Override
        public int itemsPerPage() {
            return ITEMS_PER_PAGE;
        }

        @Override
        public @NotNull Set<ZombiesScene> scenes() {
            return SceneManager.Global.instance().typed(ZombiesScene.class);
        }
    }
}
