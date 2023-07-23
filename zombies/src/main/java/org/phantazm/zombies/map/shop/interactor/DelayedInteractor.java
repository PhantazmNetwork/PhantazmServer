package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.*;

@Model("zombies.map.shop.interactor.delayed")
@Cache(false)
public class DelayedInteractor extends InteractorBase<DelayedInteractor.Data> {
    private final List<ShopInteractor> interactors;

    private final Deque<Interaction> interactions;

    private record Interaction(long startTime, PlayerInteraction interaction) {
    }

    @FactoryMethod
    public DelayedInteractor(@NotNull Data data, @NotNull @Child("target") List<ShopInteractor> interactors) {
        super(data);
        this.interactors = Objects.requireNonNull(interactors, "interactors");
        this.interactions = new ArrayDeque<>();
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(interactors, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        interactions.add(new Interaction(System.currentTimeMillis(), interaction));
        return true;
    }

    @Override
    public void tick(long time) {
        long currentTime = System.currentTimeMillis();

        Iterator<Interaction> interactionIterator = interactions.iterator();
        while (interactionIterator.hasNext()) {
            Interaction interaction = interactionIterator.next();

            long elapsedTicks = (currentTime - interaction.startTime) / MinecraftServer.TICK_MS;
            if (elapsedTicks > data.delayTicks) {
                break;
            }

            ShopInteractor.handle(interactors, interaction.interaction);
            interactionIterator.remove();
        }

        ShopInteractor.tick(interactors, time);
    }

    @DataObject
    record Data(@ChildPath("target") List<String> interactors, int delayTicks) {

    }
}
