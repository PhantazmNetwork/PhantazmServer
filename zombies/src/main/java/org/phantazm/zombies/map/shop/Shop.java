package org.phantazm.zombies.map.shop;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Tickable;
import org.phantazm.core.tracker.BoundedBase;
import org.phantazm.zombies.map.BasicFlaggable;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.ShopInfo;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.map.shop.interactor.ShopInteractor;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Shop extends BoundedBase implements Tickable {
    public static final long SHOP_ACTIVATION_DELAY = 1000L;

    private final Point mapOrigin;
    private final Instance instance;
    private final ShopInfo shopInfo;

    private final List<ShopPredicate> predicates;
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;
    private final List<ShopDisplay> displays;

    private final Tag<Long> lastActivationTag;

    private final Flaggable flaggable;

    public Shop(@NotNull Point mapOrigin, @NotNull ShopInfo shopInfo, @NotNull Instance instance,
            @NotNull List<ShopPredicate> predicates, @NotNull List<ShopInteractor> successInteractors,
            @NotNull List<ShopInteractor> failureInteractors, @NotNull List<ShopDisplay> displays) {
        super(mapOrigin, shopInfo.trigger());

        this.mapOrigin = mapOrigin;
        this.instance = Objects.requireNonNull(instance, "instance");
        this.shopInfo = Objects.requireNonNull(shopInfo, "shopInfo");

        this.predicates = List.copyOf(predicates);
        this.successInteractors = List.copyOf(successInteractors);
        this.failureInteractors = List.copyOf(failureInteractors);
        this.displays = List.copyOf(displays);

        this.lastActivationTag = Tag.Long(UUID.randomUUID().toString()).defaultValue(-1L);
        this.flaggable = new BasicFlaggable();
    }

    public @NotNull Point mapOrigin() {
        return mapOrigin;
    }

    public @NotNull @Unmodifiable List<ShopPredicate> predicates() {
        return predicates;
    }

    public @NotNull @Unmodifiable List<ShopInteractor> successInteractors() {
        return successInteractors;
    }

    public @NotNull @Unmodifiable List<ShopInteractor> failureInteractors() {
        return failureInteractors;
    }

    public @NotNull @Unmodifiable List<ShopDisplay> displays() {
        return displays;
    }

    public @NotNull Instance instance() {
        return instance;
    }

    public @NotNull ShopInfo info() {
        return shopInfo;
    }

    public void initialize() {
        for (ShopDisplay display : displays) {
            display.initialize(this);
        }

        for (ShopInteractor interactor : successInteractors) {
            interactor.initialize(this);
        }

        for (ShopInteractor interactor : failureInteractors) {
            interactor.initialize(this);
        }
    }

    public @NotNull Flaggable flags() {
        return flaggable;
    }

    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        Optional<Player> playerOptional = interaction.player().getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();

            long lastActivate = player.getTag(lastActivationTag);
            if (lastActivate != -1 && System.currentTimeMillis() - lastActivate < SHOP_ACTIVATION_DELAY) {
                return;
            }
        }

        boolean success = shopInfo.predicateEvaluation().evaluate(predicates, interaction, this);
        List<ShopInteractor> interactorsToCall = success ? successInteractors : failureInteractors;

        for (ShopInteractor interactor : interactorsToCall) {
            success &= interactor.handleInteraction(interaction);
        }

        for (ShopDisplay display : displays) {
            display.update(this, interaction, success);
        }

        playerOptional.ifPresent(player -> player.setTag(lastActivationTag, System.currentTimeMillis()));
    }

    @Override
    public void tick(long time) {
        for (ShopDisplay display : displays) {
            display.tick(time);
        }

        for (ShopInteractor interactor : successInteractors) {
            interactor.tick(time);
        }

        for (ShopInteractor interactor : failureInteractors) {
            interactor.tick(time);
        }
    }
}
