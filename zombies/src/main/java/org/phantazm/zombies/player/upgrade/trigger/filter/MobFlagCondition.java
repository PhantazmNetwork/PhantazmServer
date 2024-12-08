package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.EventUtils;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.condition.mob_flag")
@Cache
public class MobFlagCondition implements EventConditionComponent {
    private final Data data;

    @FactoryMethod
    public MobFlagCondition(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private record Internal(Data data) implements EventCondition<EntityEvent> {
        @Override
        public @NotNull Class<EntityEvent> eventType() {
            return EntityEvent.class;
        }

        @Override
        public boolean filter(@NotNull EntityEvent event) {
            if (!(EventUtils.extractEntity(event, data.useTarget) instanceof Mob mob)) return false;
            return mob.data().tags().contains(data.flag) != data.invert;
        }
    }

    @Default("""
        {
          useTarget=false,
          invert=false
        }
        """)
    @DataObject
    public record Data(@NotNull Key flag,
        boolean useTarget,
        boolean invert) {

    }
}
