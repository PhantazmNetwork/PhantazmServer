package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;

@Model("mob.skill.spawn_mob.callback.none")
@Cache
public class NoCallback implements SpawnCallbackComponent {
    private static final SpawnCallback INSTANCE = mob -> {
    };

    @FactoryMethod
    public NoCallback() {
    }

    @Override
    public @NotNull SpawnCallback apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return INSTANCE;
    }
}