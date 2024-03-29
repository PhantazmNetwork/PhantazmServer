package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;
import java.util.Optional;

@Model("zombies.map.shop.interactor.equipment_station")
@Cache(false)
public class EquipmentStationInteractor implements ShopInteractor {
    private final Data data;
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> holdingInteractors;
    private final List<ShopInteractor> duplicateInteractors;
    private final List<ShopInteractor> failureInteractors;

    @FactoryMethod
    public EquipmentStationInteractor(@NotNull Data data,
        @NotNull @Child("success") List<ShopInteractor> successInteractors,
        @NotNull @Child("holding") List<ShopInteractor> holdingInteractors,
        @NotNull @Child("duplicate") List<ShopInteractor> duplicateInteractors,
        @NotNull @Child("failure") List<ShopInteractor> failureInteractors) {
        this.data = data;
        this.successInteractors = successInteractors;
        this.holdingInteractors = holdingInteractors;
        this.duplicateInteractors = duplicateInteractors;
        this.failureInteractors = failureInteractors;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(successInteractors, shop);
        ShopInteractor.initialize(holdingInteractors, shop);
        ShopInteractor.initialize(duplicateInteractors, shop);
        ShopInteractor.initialize(failureInteractors, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        Optional<Player> playerOptional = interaction.player().getPlayer();
        if (playerOptional.isEmpty()) {
            return false;
        }

        EquipmentHandler.Result result = interaction.player().module().getEquipmentHandler()
            .addOrReplaceEquipment(data.groupKey, data.equipmentKey, data.allowReplace, data.specificSlot,
                data.allowDuplicate,
                () -> interaction.player().module().getEquipmentCreator().createEquipment(data.equipmentKey),
                playerOptional.get());

        if (!result.success()) {
            Optional<Equipment> equipmentOptional = interaction.player().getHeldEquipment();
            if (equipmentOptional.isPresent()) {
                Equipment equipment = equipmentOptional.get();
                if (equipment.key().equals(data.equipmentKey)) {
                    return ShopInteractor.handle(holdingInteractors, interaction);
                }
            }
        }

        return switch (result) {
            case ADDED, REPLACED -> ShopInteractor.handle(successInteractors, interaction);
            case DUPLICATE -> {
                ShopInteractor.handle(duplicateInteractors, interaction);
                yield false;
            }
            case FAILED -> {
                ShopInteractor.handle(failureInteractors, interaction);
                yield false;
            }
            case CANCELLED -> false;
        };
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(successInteractors, time);
        ShopInteractor.tick(holdingInteractors, time);
        ShopInteractor.tick(duplicateInteractors, time);
        ShopInteractor.tick(failureInteractors, time);
    }

    @DataObject
    public record Data(
        @NotNull Key equipmentKey,
        @NotNull Key groupKey,
        boolean allowReplace,
        int specificSlot,
        boolean allowDuplicate,
        @NotNull @ChildPath("success") List<String> successInteractors,
        @NotNull @ChildPath("holding") List<String> holdingInteractors,
        @NotNull @ChildPath("duplicate") List<String> duplicateInteractors,
        @NotNull @ChildPath("failure") List<String> failureInteractors) {
        @Default("allowReplace")
        public static ConfigElement defaultAllowReplace() {
            return ConfigPrimitive.of(true);
        }

        @Default("specificSlot")
        public static ConfigElement defaultSpecificSlot() {
            return ConfigPrimitive.of(-1);
        }

        @Default("allowDuplicate")
        public static ConfigElement defaultAllowDuplicate() {
            return ConfigPrimitive.of(false);
        }
    }
}
