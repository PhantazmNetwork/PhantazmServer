package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.Upgradable;
import org.phantazm.core.equipment.UpgradePath;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;
import java.util.Optional;

@Model("zombies.map.shop.interactor.upgrade_equipment")
@Cache(false)
public class UpgradeEquipmentInteractor implements ShopInteractor {
    private final Data data;
    private final UpgradePath upgradePath;
    private final List<ShopInteractor> notUpgradableInteractors;
    private final List<ShopInteractor> noHeldEquipmentInteractors;
    private final List<ShopInteractor> noUpgradeInteractors;
    private final List<ShopInteractor> upgradeInteractors;

    @FactoryMethod
    public UpgradeEquipmentInteractor(@NotNull Data data, @NotNull @Child("upgrade_path") UpgradePath upgradePath,
            @NotNull @Child("not_upgradable_interactors") List<ShopInteractor> notUpgradableInteractors,
            @NotNull @Child("no_held_equipment_interactors") List<ShopInteractor> noHeldEquipmentInteractors,
            @NotNull @Child("no_upgrade_interactors") List<ShopInteractor> noUpgradeInteractors,
            @NotNull @Child("upgrade_interactors") List<ShopInteractor> upgradeInteractors) {
        this.data = data;
        this.upgradePath = upgradePath;
        this.notUpgradableInteractors = notUpgradableInteractors;
        this.noHeldEquipmentInteractors = noHeldEquipmentInteractors;
        this.noUpgradeInteractors = noUpgradeInteractors;
        this.upgradeInteractors = upgradeInteractors;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(notUpgradableInteractors, shop);
        ShopInteractor.initialize(noHeldEquipmentInteractors, shop);
        ShopInteractor.initialize(noUpgradeInteractors, shop);
        ShopInteractor.initialize(upgradeInteractors, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        ZombiesPlayer zombiesPlayer = interaction.player();
        Optional<Equipment> heldEquipmentOptional = zombiesPlayer.getHeldEquipment();
        if (heldEquipmentOptional.isEmpty()) {
            ShopInteractor.handle(noHeldEquipmentInteractors, interaction);
            return false;
        }

        Equipment equipment = heldEquipmentOptional.get();
        if (!(equipment instanceof Upgradable upgradable)) {
            ShopInteractor.handle(notUpgradableInteractors, interaction);
            return false;
        }

        Optional<Key> upgradeKeyOptional = upgradePath.nextUpgrade(upgradable.currentLevel());
        if (upgradeKeyOptional.isEmpty()) {
            ShopInteractor.handle(noUpgradeInteractors, interaction);
            return false;
        }

        Key upgradeKey = upgradeKeyOptional.get();
        if (!upgradable.getSuggestedUpgrades().contains(upgradeKey)) {
            ShopInteractor.handle(notUpgradableInteractors, interaction);
            return false;
        }

        upgradable.setLevel(upgradeKey);

        ItemStack itemStack = equipment.getItemStack();
        Component component = itemStack.getDisplayName();
        if (component == null) {
            component = Component.translatable(itemStack.material().registry().translationKey());
        }

        TagResolver tag = Placeholder.component("upgraded_item", component);
        zombiesPlayer.getPlayer().ifPresent(
                player -> player.sendMessage(MiniMessage.miniMessage().deserialize(data.upgradeFormatMessage, tag)));

        ShopInteractor.handle(upgradeInteractors, interaction);
        return true;
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(notUpgradableInteractors, time);
        ShopInteractor.tick(noHeldEquipmentInteractors, time);
        ShopInteractor.tick(noUpgradeInteractors, time);
        ShopInteractor.tick(upgradeInteractors, time);
    }

    @DataObject
    public record Data(@NotNull String upgradeFormatMessage,
                       @NotNull @ChildPath("upgrade_path") String upgradePath,
                       @NotNull @ChildPath("not_upgradable_interactors") List<String> notUpgradableInteractors,
                       @NotNull @ChildPath("no_held_equipment_interactors") List<String> noHeldEquipmentInteractors,
                       @NotNull @ChildPath("no_upgrade_interactors") List<String> noUpgradeInteractors,
                       @NotNull @ChildPath("upgrade_interactors") List<String> upgradeInteractors) {
    }
}
