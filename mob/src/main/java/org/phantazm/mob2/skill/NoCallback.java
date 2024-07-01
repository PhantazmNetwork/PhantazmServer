package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;

@Model("mob.skill.spawn_mob.callback.none")
@Cache
public class NoCallback implements SpawnCallbackComponent {
    private static final SpawnCallback INSTANCE = mob -> {
    };

    @FactoryMethod
    public NoCallback() {
    }

    @Override
    public @NotNull SpawnCallback get() {
        return INSTANCE;
    }
}