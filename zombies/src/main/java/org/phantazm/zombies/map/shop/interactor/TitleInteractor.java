package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.interactor.show_title")
@Cache(false)
public class TitleInteractor extends InteractorBase<TitleInteractor.Data> {
    private Shop shop;

    @FactoryMethod
    public TitleInteractor(@NotNull Data data) {
        super(data);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Shop shop = this.shop;
        if (shop == null) {
            return false;
        }

        if (data.broadcast) {
            shop.instance().sendTitlePart(data.titlePart, data.message);
        }
        else {
            interaction.player().getPlayer().ifPresent(player -> player.sendTitlePart(data.titlePart, data.message));
        }

        return true;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
    }

    @DataObject
    public record Data(@NotNull Component message, boolean broadcast, TitlePart<Component> titlePart) {
    }
}
