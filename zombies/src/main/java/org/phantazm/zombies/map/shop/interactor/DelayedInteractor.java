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
    private final ShopInteractor target;

    private final Deque<Interaction> interactions;

    private record Interaction(long startTime, PlayerInteraction interaction) {
    }

    @FactoryMethod
    public DelayedInteractor(@NotNull Data data, @NotNull @Child("target") ShopInteractor target) {
        super(data);
        this.target = Objects.requireNonNull(target, "target");
        this.interactions = new ArrayDeque<>();
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        target.initialize(shop);
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
                return;
            }

            target.handleInteraction(interaction.interaction);
            interactionIterator.remove();
        }
    }

    @DataObject
    record Data(@ChildPath("target") String targetPath, int delayTicks) {

    }
}
