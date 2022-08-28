package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.phantazmnetwork.zombies.map.Evaluation;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.conditional")
public class ConditionalInteractor extends InteractorBase<ConditionalInteractor.Data> {
    private final List<ShopPredicate> predicates;
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;

    @ProcessorMethod
    public static ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<List<String>> STRING_LIST_PROCESSOR =
                    ConfigProcessor.STRING.listProcessor();

            private static final ConfigProcessor<Evaluation> EVALUATION_PROCESSOR =
                    ConfigProcessor.enumProcessor(Evaluation.class);

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Evaluation evaluation = EVALUATION_PROCESSOR.dataFromElement(element.getElementOrThrow("evaluation"));
                List<String> predicates =
                        STRING_LIST_PROCESSOR.dataFromElement(element.getElementOrThrow("predicates"));
                List<String> successInteractors =
                        STRING_LIST_PROCESSOR.dataFromElement(element.getElementOrThrow("successInteractors"));
                List<String> failureInteractors =
                        STRING_LIST_PROCESSOR.dataFromElement(element.getElementOrThrow("failureInteractors"));
                return new Data(evaluation, predicates, successInteractors, failureInteractors);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("evaluation", EVALUATION_PROCESSOR.elementFromData(data.evaluation), "predicates",
                        STRING_LIST_PROCESSOR.elementFromData(data.predicates), "successInteractors",
                        STRING_LIST_PROCESSOR.elementFromData(data.successInteractors), "failureInteractors",
                        STRING_LIST_PROCESSOR.elementFromData(data.failureInteractors));
            }
        };
    }

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
