package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.ZombiesTagUtils;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.upgrade.UpgradeActivatorComponent;
import org.phantazm.zombies.scene2.ZombiesScene;
import net.kyori.adventure.key.Key;

import java.util.List;

@Model("zombies.map.shop.interactor.player_upgrade")
@Cache(false)
public class PlayerUpgradeInteractor implements ShopInteractor {
    private final Data data;
    private final List<ShopInteractor> eligible;
    private final List<ShopInteractor> notEligible;
    private final List<ShopInteractor> cannotAfford;
    private final List<ShopInteractor> alreadyPurchased;

    @FactoryMethod
    public PlayerUpgradeInteractor(@NotNull Data data,
        @NotNull @Child("eligible") List<ShopInteractor> eligible,
        @NotNull @Child("notEligible") List<ShopInteractor> notEligible,
        @NotNull @Child("cannotAfford") List<ShopInteractor> cannotAfford,
        @NotNull @Child("alreadyPurchased") List<ShopInteractor> alreadyPurchased) {
        this.data = data;
        this.eligible = eligible;
        this.notEligible = notEligible;
        this.cannotAfford = cannotAfford;
        this.alreadyPurchased = alreadyPurchased;
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        ZombiesScene scene = interaction.player().getScene();
        TagHandler localTags = ZombiesTagUtils.sceneLocalTags(interaction.player());
        UpgradeActivatorComponent activatorComponent = scene.upgradeActivatorComponent();

        Tag<Boolean> purchaseTag = activatorComponent.purchaseTag(data.upgrade);

        if (!activatorComponent.mayPurchase(data.upgrade, localTags, data.isSynergy)) {
            ShopInteractor.handle(notEligible, interaction);
            return false;
        }

        if (localTags.getTag(purchaseTag)) {
            ShopInteractor.handle(alreadyPurchased, interaction);
            return false;
        }

        PlayerCoins coins = interaction.player().module().getCoins();
        TransactionResult result = coins.runTransaction(new Transaction(
            interaction.player().module().compositeTransactionModifiers().modifiers(data.modifierType),
            -data.cost));

        if (!result.applyIfAffordable(coins)) {
            ShopInteractor.handle(cannotAfford, interaction);
            return false;
        }

        localTags.setTag(purchaseTag, true);
        scene.upgradeActivator().refresh(interaction.player());

        ShopInteractor.handle(eligible, interaction);
        return true;
    }

    @DataObject
    @Default("""
        {
          modifierType='coin_spend.shop',
          isSynergy=false
        }
        """)
    public record Data(@NotNull Key upgrade,
        int cost,
        @NotNull Key modifierType,
        boolean isSynergy) {
    }
}
