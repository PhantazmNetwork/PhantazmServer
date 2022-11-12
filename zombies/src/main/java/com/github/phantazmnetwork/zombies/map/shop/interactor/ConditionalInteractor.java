package com.github.phantazmnetwork.zombies.map.shop.interactor;

import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.map.shop.predicate.ShopPredicate;
import com.github.phantazmnetwork.zombies.map.Evaluation;
import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.conditional")
public class ConditionalInteractor extends InteractorBase<ConditionalInteractor.Data> {
    private final List<ShopPredicate> predicates;
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;

    @FactoryMethod
    public ConditionalInteractor(@NotNull Data data, @DataName("predicates") List<ShopPredicate> predicates,
            @DataName("success_interactors") List<ShopInteractor> successInteractors,
            @DataName("failure_interactors") List<ShopInteractor> failureInteractors) {
        super(data);
        this.predicates = Objects.requireNonNull(predicates, "predicates");
        this.successInteractors = Objects.requireNonNull(successInteractors, "successInteractors");
        this.failureInteractors = Objects.requireNonNull(failureInteractors, "failureInteractors");
    }

    @Override
    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        List<ShopInteractor> interactors =
                data.evaluation.evaluate(predicates, interaction) ? successInteractors : failureInteractors;

        for (ShopInteractor interactor : interactors) {
            interactor.handleInteraction(interaction);
        }
    }

    @DataObject
    record Data(Evaluation evaluation,
                @DataPath("predicates") List<String> predicates,
                @DataPath("success_interactors") List<String> successInteractors,
                @DataPath("failure_interactors") List<String> failureInteractors) {

    }
}
