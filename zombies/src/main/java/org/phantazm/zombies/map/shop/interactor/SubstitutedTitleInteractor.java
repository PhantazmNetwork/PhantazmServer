package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.interactor.show_substituted_title")
@Cache(false)
public class SubstitutedTitleInteractor extends InteractorBase<SubstitutedTitleInteractor.Data> {
    private Shop shop;

    @FactoryMethod
    public SubstitutedTitleInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Shop shop = this.shop;
        if (shop == null) {
            return false;
        }

        Component playerName = interaction.player().module().getPlayerView().getDisplayNameIfPresent();
        TagResolver playerPlaceholder = Placeholder.component("player", playerName);
        Component message = MiniMessage.miniMessage().deserialize(data.format, playerPlaceholder);

        if (data.broadcast) {
            shop.instance().sendTitlePart(data.titlePart, message);
        } else {
            interaction.player().getPlayer().ifPresent(player -> player.sendTitlePart(data.titlePart, message));
        }

        return true;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @DataObject
    public record Data(@NotNull String format,
        boolean broadcast,
        TitlePart<Component> titlePart) {
    }
}
