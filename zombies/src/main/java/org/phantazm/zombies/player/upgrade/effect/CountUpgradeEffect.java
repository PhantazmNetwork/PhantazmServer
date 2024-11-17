package org.phantazm.zombies.player.upgrade.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.zombies.event.player.ZombiesPlayerModifyUpgrade;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.Optional;
import java.util.Set;

@Model("zombies.upgrade.effect.count_upgrade")
@Cache
public class CountUpgradeEffect implements UpgradeEffectComponent {
    private final Data data;

    @FactoryMethod
    public CountUpgradeEffect(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull UpgradeEffect apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private static final class Internal extends SingleEventEffect<ZombiesPlayerModifyUpgrade> {
        private final Data data;
        private final Tag<Integer> countTag;

        private Internal(@NotNull Data data) {
            super(ZombiesPlayerModifyUpgrade.class);
            this.data = data;
            this.countTag = Tag.Integer(data.outputTag).defaultValue(0);
        }

        @Override
        protected void applyEvent(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData, @NotNull ZombiesPlayerModifyUpgrade zombiesPlayerModifyUpgrade) {
            if (data.upgradeKeys.contains(zombiesPlayerModifyUpgrade.upgradeKey()) != data.whitelist) {
                return;
            }

            Optional<? extends Player> player = zombiesPlayer.getPlayer();
            if (player.isEmpty()) {
                return;
            }

            TagUtils.sceneLocalTags(player.get(), zombiesPlayer.getScene())
                .updateTag(countTag, value -> zombiesPlayerModifyUpgrade.added() ? ++value : --value);
        }
    }

    @Default("""
        {
          upgradeKeys=[],
          whitelist=true
        }
        """)
    @DataObject
    public record Data(@NotNull Set<Key> upgradeKeys,
        boolean whitelist,
        @NotNull String outputTag) {}
}
