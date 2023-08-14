package org.phantazm.mob2.selector;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;

public class PlayersInAreaSelector implements SelectorComponent {
    private final Data data;
    private final SelectorComponent originSelector;
    private final ValidatorComponent validator;

    @FactoryMethod
    public PlayersInAreaSelector(@NotNull Data data, @NotNull @Child("origin") SelectorComponent originSelector,
            @NotNull @Child("validator") ValidatorComponent validator) {
        this.data = data;
        this.originSelector = originSelector;
        this.validator = validator;
    }

    @Override
    public @NotNull Selector apply(@NotNull InjectionStore injectionStore) {
        return new AreaSelector(injectionStore.get(Keys.MOB_KEY), data, originSelector.apply(injectionStore),
                validator.apply(injectionStore));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("origin") String originSelector,
                       @NotNull @ChildPath("validator") String validator,
                       double range,
                       int limit) {
        @Default("limit")
        public static @NotNull ConfigElement limitDefault() {
            return ConfigPrimitive.of(-1);
        }
    }

    private static final class AreaSelector extends AreaSelectorAbstract<Player> {
        private AreaSelector(Mob self, Data data, Selector originSelector, Validator validator) {
            super(self, originSelector, EntityTracker.Target.PLAYERS, validator, data.limit, data.range);
        }

        @Override
        protected @NotNull Player mapEntity(@NotNull Entity entity) {
            return (Player)entity;
        }
    }
}
