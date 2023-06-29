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
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.permission.Permission;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouterStore;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.SceneRouter;
import org.phantazm.core.time.AnalogTickFormatter;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.server.config.server.ZombiesGamereportConfig;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.scene.ZombiesSceneRouter;
import org.phantazm.zombies.stage.EndStage;
import org.phantazm.zombies.stage.InGameStage;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;

import java.time.Instant;
import java.util.*;

public class GamereportCommand extends Command {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private interface PageFormatter {
        @NotNull Component page(int pageIndex, @NotNull List<? extends Scene<?>> scenes);

        int itemsPerPage();
    }

    private record ZombiesPageFormatter(ZombiesGamereportConfig config) implements PageFormatter {
        private static final int ITEMS_PER_PAGE = 3;
        private static final TickFormatter TIME_FORMATTER = new AnalogTickFormatter(new AnalogTickFormatter.Data(true));

        @Override
        public @NotNull Component page(int page, @NotNull List<? extends Scene<?>> scenes) {
            int maxPages = (int)Math.ceil(scenes.size() / (double)ITEMS_PER_PAGE);

            List<Component> gameEntries = new ArrayList<>(scenes.size());

            TagResolver totalGamesTag = Placeholder.unparsed("total_games", Integer.toString(scenes.size()));

            for (int i = (page - 1) * ITEMS_PER_PAGE, j = 0; i < scenes.size() && j < ITEMS_PER_PAGE; i++, j++) {
                ZombiesScene zombiesScene = (ZombiesScene)scenes.get(i);

                TagResolver currentGameTag = Placeholder.unparsed("current_game", Integer.toString(i + 1));
                TagResolver gameUUIDTag = Placeholder.unparsed("game_uuid", zombiesScene.getUUID().toString());

                List<Component> playerNames = new ArrayList<>(zombiesScene.getMapSettingsInfo().maxPlayers());
                for (ZombiesPlayer player : zombiesScene.getZombiesPlayers().values()) {
                    player.getPlayer().ifPresent(actualPlayer -> {
                        Component displayName = actualPlayer.getDisplayName();
                        playerNames.add(displayName == null ? Component.text(actualPlayer.getUsername()) : displayName);
                    });
                }

                Component playerList = Component.join(JoinConfiguration.commas(true), playerNames);
                TagResolver playerListTag = Placeholder.component("player_list", playerList);
                TagResolver mapNameTag =
                        Placeholder.component("map_name", zombiesScene.getMapSettingsInfo().displayName());

                Component gameState;
                Stage currentStage = zombiesScene.getCurrentStage();

                TagResolver currentRoundTag = Placeholder.parsed("current_round",
                        Integer.toString(zombiesScene.getMap().roundHandler().currentRoundIndex() + 1));

                if (currentStage == null || currentStage.key().equals(StageKeys.IDLE_STAGE)) {
                    gameState = MINI_MESSAGE.deserialize(config.idleStageFormat());
                }
                else if (currentStage.key().equals(StageKeys.COUNTDOWN)) {
                    gameState = MINI_MESSAGE.deserialize(config.countdownStageFormat());
                }
                else if (currentStage.key().equals(StageKeys.IN_GAME)) {
                    InGameStage inGameStage = (InGameStage)currentStage;

                    TagResolver gameTimeTag =
                            Placeholder.unparsed("game_time", TIME_FORMATTER.format(inGameStage.ticksSinceStart()));

                    gameState = MINI_MESSAGE.deserialize(config.inGameFormat(), currentRoundTag, gameTimeTag);
                }
                else if (currentStage.key().equals(StageKeys.END)) {
                    EndStage endStage = (EndStage)currentStage;

                    TagResolver gameTimeTag =
                            Placeholder.parsed("game_time", TIME_FORMATTER.format(endStage.ticksSinceStart()));
                    gameState = MINI_MESSAGE.deserialize(config.endedFormat(),
                            Formatter.choice("result", endStage.hasWon() ? 1 : 0), currentRoundTag, gameTimeTag);
                }
                else {
                    gameState = Component.empty();
                }

                TagResolver gameStateTag = Placeholder.component("game_state", gameState);

                TagResolver warpTag = TagResolver.resolver("warp",
                        Tag.styling(ClickEvent.runCommand("/ghost " + zombiesScene.getUUID())));

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
            }
            else {
                TagResolver pageAdvancingResolver = TagResolver.resolver("next_page",
                        Tag.styling(ClickEvent.runCommand("/gamereport phantazm:zombies " + (page + 1))));

                nextPageOptionalComponent = MINI_MESSAGE.deserialize(config.nextPageFormat(), pageAdvancingResolver);
            }

            Component previousPageOptionalComponent;
            if (page == 1) {
                previousPageOptionalComponent = Component.empty();
            }
            else {
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
    }

    public static final Permission PERMISSION = new Permission("admin.gamereport");

    private final Map<Key, PageFormatter> pageFormatters;

    public GamereportCommand(@NotNull RouterStore routerStore, @NotNull ZombiesGamereportConfig config) {
        super("gamereport");

        Map<Key, PageFormatter> temp = new HashMap<>();
        temp.put(ZombiesSceneRouter.KEY, new ZombiesPageFormatter(config));
        pageFormatters = Map.copyOf(temp);

        Argument<String> routerArgument = ArgumentType.String("router-key");
        routerArgument.setSuggestionCallback((sender, context, suggestion) -> {
            for (SceneRouter<?, ?> router : routerStore.getRouters()) {
                if (!router.isGame()) {
                    continue;
                }

                suggestion.addEntry(
                        new SuggestionEntry(router.key().asString(), Component.text(router.key().asString())));
            }
        });

        Argument<Integer> pageArgument = ArgumentType.Integer("page").setDefaultValue(1);

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));

        addConditionalSyntax(getCondition(), (sender, context) -> {
            @Subst(Constants.NAMESPACE_OR_KEY)
            String key = context.get(routerArgument);

            if (!Key.parseable(key)) {
                sender.sendMessage(Component.text("Invalid key!").color(NamedTextColor.RED));
                return;
            }

            Key routerKey = Key.key(key);
            SceneRouter<?, ?> targetRouter = null;
            for (SceneRouter<?, ?> router : routerStore.getRouters()) {
                if (!router.isGame()) {
                    continue;
                }

                if (router.key().equals(routerKey)) {
                    targetRouter = router;
                    break;
                }
            }

            if (targetRouter == null) {
                sender.sendMessage(Component.text("Target router does not exist!").color(NamedTextColor.RED));
                return;
            }

            PageFormatter formatter = pageFormatters.get(targetRouter.key());
            if (formatter == null) {
                sender.sendMessage(Component.text("No formatter for this scene!").color(NamedTextColor.RED));
                return;
            }

            List<? extends Scene<?>> scenes = List.copyOf(targetRouter.getScenes());

            if (scenes.isEmpty()) {
                sender.sendMessage(Component.text("There are no scenes in this router!").color(NamedTextColor.RED));
                return;
            }

            int pageCount = (int)Math.ceil(scenes.size() / (double)formatter.itemsPerPage());
            int page = context.get(pageArgument);
            if (page < 1 || page > pageCount) {
                sender.sendMessage(Component.text("Target page does not exist!").color(NamedTextColor.RED));
                return;
            }

            sender.sendMessage(formatter.page(page, scenes));
        }, routerArgument, pageArgument);
    }
}