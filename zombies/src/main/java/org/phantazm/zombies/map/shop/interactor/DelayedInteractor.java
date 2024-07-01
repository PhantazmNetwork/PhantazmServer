package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.*;

@Model("zombies.map.shop.interactor.delayed")
@Cache(false)
public class DelayedInteractor extends InteractorBase<DelayedInteractor.Data> {
    private final List<ShopInteractor> interactors;

    private final Deque<Interaction> interactions;

    private long ticks = 0;

    @FactoryMethod
    public DelayedInteractor(@NotNull Data data, @NotNull @Child("interactors") List<ShopInteractor> interactors) {
        super(data);
        this.interactors = Objects.requireNonNull(interactors);
        this.interactions = new ArrayDeque<>();
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(interactors, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        interactions.add(new Interaction(ticks, interaction));
        return true;
    }

    @Override
    public void tick(long time) {
        ++ticks;
        Iterator<Interaction> interactionIterator = interactions.iterator();
        while (interactionIterator.hasNext()) {
            Interaction interaction = interactionIterator.next();

            if (ticks - interaction.startTicks < data.delayTicks) {
                break;
            }

            ShopInteractor.handle(interactors, interaction.interaction);
            interactionIterator.remove();
        }

        ShopInteractor.tick(interactors, time);
    }

    private record Interaction(long startTicks,
        PlayerInteraction interaction) {
    }

    @DataObject
    public record Data(int delayTicks) {
    }
}
