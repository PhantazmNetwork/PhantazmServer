package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.display.creator.PlayerDisplayCreator;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

@Description("""
        A shop display that maintains multiple, independent displays for each player. Each player will only see the shop
        display that belongs to them.
        """)
@Model("zombies.map.shop.display.player")
@Cache(false)
public class PlayerDisplay implements ShopDisplay {
    private final PlayerDisplayCreator playerDisplayCreator;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    private final Map<? super UUID, ShopDisplay> playerDisplays;

    @FactoryMethod
    public PlayerDisplay(@NotNull @Child("display_creator") PlayerDisplayCreator playerDisplayCreator,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
        this.playerDisplayCreator = Objects.requireNonNull(playerDisplayCreator, "playerDisplayCreator");
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
        this.playerDisplays = new LinkedHashMap<>();
    }

    private ShopDisplay displayFor(ZombiesPlayer zombiesPlayer) {
        return playerDisplays.computeIfAbsent(zombiesPlayer.getUUID(),
                key -> playerDisplayCreator.forPlayer(zombiesPlayer));
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
            displayFor(zombiesPlayer).initialize(shop);
        }
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        for (ShopDisplay shopDisplay : playerDisplays.values()) {
            shopDisplay.destroy(shop);
        }

        playerDisplays.clear();
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
        displayFor(interaction.player()).update(shop, interaction, interacted);
    }

    @Override
    public void tick(long time) {
        for (ShopDisplay display : playerDisplays.values()) {
            display.tick(time);
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("display_creator") String displayCreator) {
    }
}
