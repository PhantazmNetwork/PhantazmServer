package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;

@Model("zombies.map.shop.interactor.add_equipment")
@Cache(false)
public class AddEquipmentInteractor extends InteractorBase<AddEquipmentInteractor.Data> {
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;

    @FactoryMethod
    public AddEquipmentInteractor(@NotNull Data data,
        @NotNull @Child("success") List<ShopInteractor> successInteractors,
        @NotNull @Child("failure") List<ShopInteractor> failureInteractors) {
        super(data);
        this.successInteractors = successInteractors;
        this.failureInteractors = failureInteractors;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(successInteractors, shop);
        ShopInteractor.initialize(failureInteractors, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        boolean result = addOrReplaceEquipment(interaction);
        List<ShopInteractor> interactors = result ? successInteractors : failureInteractors;
        return result & ShopInteractor.handle(interactors, interaction);
    }

    private boolean addOrReplaceEquipment(PlayerInteraction interaction) {
        return interaction.player().module().getEquipmentHandler()
            .addOrReplaceEquipment(data.groupKey, data.equipmentKey, data.allowReplace, data.specificSlot,
                data.allowDuplicate,
                () -> interaction.player().module().getEquipmentCreator().createEquipment(data.equipmentKey),
                interaction.player().getPlayer().orElse(null)).success();
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(successInteractors, time);
        ShopInteractor.tick(failureInteractors, time);
    }

    @DataObject
    public record Data(
        @NotNull Key equipmentKey,
        @NotNull Key groupKey,
        boolean allowReplace,
        boolean allowDuplicate,
        int specificSlot,
        @NotNull @ChildPath("success") List<String> successInteractors,
        @NotNull @ChildPath("failure") List<String> failureInteractors) {
    }
}
