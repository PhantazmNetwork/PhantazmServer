package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Evaluation;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.conditional")
@Cache(false)
public class ConditionalInteractor extends InteractorBase<ConditionalInteractor.Data> {
    private final List<ShopPredicate> predicates;
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;

    private Shop shop;

    @FactoryMethod
    public ConditionalInteractor(@NotNull Data data, @Child("predicates") List<ShopPredicate> predicates,
        @Child("successInteractors") List<ShopInteractor> successInteractors,
        @Child("failureInteractors") List<ShopInteractor> failureInteractors) {
        super(data);
        this.predicates = Objects.requireNonNull(predicates);
        this.successInteractors = Objects.requireNonNull(successInteractors);
        this.failureInteractors = Objects.requireNonNull(failureInteractors);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        boolean success = data.evaluation.evaluate(predicates, interaction, shop);
        List<ShopInteractor> interactors = success ? successInteractors : failureInteractors;
        return success & ShopInteractor.handle(interactors, interaction);
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(successInteractors, time);
        ShopInteractor.tick(failureInteractors, time);
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
        ShopInteractor.initialize(successInteractors, shop);
        ShopInteractor.initialize(failureInteractors, shop);
    }

    @DataObject
    public record Data(
        @NotNull Evaluation evaluation) {

    }
}
