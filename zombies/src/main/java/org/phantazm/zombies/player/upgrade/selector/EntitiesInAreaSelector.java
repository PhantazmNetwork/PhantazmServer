package org.phantazm.zombies.player.upgrade.selector;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.EntityTrackerUtils;
import org.phantazm.core.Target;
import org.phantazm.core.TrackerTargetType;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.validator.Validator;
import org.phantazm.zombies.player.upgrade.validator.ValidatorComponent;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

@Model("zombies.upgrade.selector.entities_in_area")
@Cache
public class EntitiesInAreaSelector implements SelectorComponent {
    private final Data data;
    private final SelectorComponent originSelector;
    private final ValidatorComponent validator;

    @FactoryMethod
    public EntitiesInAreaSelector(@NotNull Data data,
        @NotNull @Child("originSelector") SelectorComponent originSelector,
        @NotNull @Child("validator") ValidatorComponent validator) {
        this.data = data;
        this.originSelector = originSelector;
        this.validator = validator;
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data, originSelector.apply(injectionStore, zombiesPlayer),
            validator.apply(injectionStore, zombiesPlayer));
    }

    private static final class Internal implements Selector {
        private final Data data;
        private final Selector originSelector;
        private final Validator validator;

        private Internal(Data data, Selector originSelector, Validator validator) {
            this.data = data;
            this.originSelector = originSelector;
            this.validator = validator;
        }

        @Override
        public @NotNull Target select(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer,
            @NotNull TriggerData triggerData) {
            Target origin = originSelector.select(upgrade, zombiesPlayer, triggerData);
            Entity self = zombiesPlayer.getPlayer().orElse(null);

            return EntityTrackerUtils.select(zombiesPlayer.getScene().instance(), data.target.target(), origin,
                data.limit, data.range, candidate -> {
                    return (!data.limitSelf || candidate != self) && validator.test(candidate);
                });
        }
    }

    @Default("""
        {
          limitSelf=true,
          target='ENTITIES',
          range=-1.0,
          limit=-1
        }
        """)
    @DataObject
    public record Data(
        boolean limitSelf,
        @NotNull TrackerTargetType target,
        double range,
        int limit) {
    }
}