package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.LazyComponent;
import org.phantazm.commons.chat.ChatDestination;
import org.phantazm.core.time.TickFormatter;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.map.round.action.announce")
@Cache
public class AnnounceRoundAction implements LazyComponent<ZombiesScene, Action<Round>> {
    private final Data data;
    private final TickFormatter tickFormatter;

    @FactoryMethod
    public AnnounceRoundAction(@NotNull Data data,
        @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = Objects.requireNonNull(data);
        this.tickFormatter = Objects.requireNonNull(tickFormatter);
    }

    @Override
    public @NotNull Action<Round> apply(@NotNull InjectionStore injectionStore,
        @NotNull Supplier<@NotNull ZombiesScene> sceneSupplier) {
        return new Impl(data, tickFormatter, sceneSupplier);
    }

    @DataObject
    public record Data(
        @NotNull String format,
        @NotNull ChatDestination destination,
        @NotNull @ChildPath("tick_formatter") String tickFormatter) {

    }

    private record Impl(Data data,
        TickFormatter tickFormatter,
        Supplier<ZombiesScene> zombiesScene) implements Action<Round> {

        @Override
        public void perform(@NotNull Round round) {
            TagResolver roundPlaceholder = Placeholder.component("round", Component.text(round.round()));
            String timeString = tickFormatter.format(zombiesScene.get().map().objects().module().ticksSinceStart().get());
            TagResolver timePlaceholder = Placeholder.unparsed("time", timeString);

            Component message = MiniMessage.miniMessage().deserialize(data.format(), roundPlaceholder, timePlaceholder);
            switch (data.destination()) {
                case TITLE -> zombiesScene.get().sendTitlePart(TitlePart.TITLE, message);
                case SUBTITLE -> zombiesScene.get().sendTitlePart(TitlePart.SUBTITLE, message);
                case CHAT -> zombiesScene.get().sendMessage(message);
                case ACTION_BAR -> zombiesScene.get().sendActionBar(message);
            }
        }
    }
}
