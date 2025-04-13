package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.upgrade.activator.none")
@Cache
public class NoUpgradeActivator implements UpgradeActivatorComponent {
    public static NoUpgradeActivator INSTANCE = new NoUpgradeActivator();

    @FactoryMethod
    public NoUpgradeActivator() {

    }

    private static final UpgradeActivator NIL = new UpgradeActivator() {
        @Override
        public void hook() {

        }

        @Override
        public void refresh(@NotNull ZombiesPlayer zombiesPlayer) {

        }
    };

    @Override
    public @NotNull UpgradeActivator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene zombiesScene) {
        return NIL;
    }

    @Override
    public boolean mayPurchase(@NotNull Key upgrade, @NotNull TagHandler handler, boolean isSynergy) {
        return true;
    }

    @Override
    public @NotNull Tag<Boolean> purchaseTag(@NotNull Key upgrade) {
        return Tag.Boolean(upgrade.value()).defaultValue(false);
    }
}
