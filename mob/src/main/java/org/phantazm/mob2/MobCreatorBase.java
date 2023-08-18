package org.phantazm.mob2;

import com.github.steanky.proxima.path.Pathfinder;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.skill.SkillComponent;
import org.phantazm.proxima.bindings.minestom.Pathfinding;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class MobCreatorBase implements MobCreator {
    private final MobData data;
    private final Pathfinding.Factory pathfinding;
    private final List<SkillComponent> skills;

    private final Pathfinder pathfinder;
    private final Function<? super Instance, ? extends InstanceSettings> settingsFunction;

    public MobCreatorBase(@NotNull MobData data, Pathfinding.@NotNull Factory pathfinding,
            @NotNull List<SkillComponent> skills, @NotNull Pathfinder pathfinder,
            @NotNull Function<? super @NotNull Instance, ? extends @NotNull InstanceSettings> settingsFunction) {
        this.data = Objects.requireNonNull(data);
        this.pathfinding = Objects.requireNonNull(pathfinding);
        this.skills = List.copyOf(skills);

        this.pathfinder = Objects.requireNonNull(pathfinder);
        this.settingsFunction = Objects.requireNonNull(settingsFunction);
    }

    @Override
    public @NotNull Mob create(@NotNull Key key, @NotNull Instance instance) {
        InstanceSettings settings = settingsFunction.apply(instance);
        Mob mob = new Mob(data.type(), UUID.randomUUID(),
                pathfinding.make(pathfinder, settings.nodeLocal(), settings.spaceHandler()));

        InjectionStore store = store();
        for (SkillComponent component : skills) {
            mob.addSkill(component.apply(mob, store));
        }

        return mob;
    }

    public @NotNull InjectionStore store() {
        return InjectionStore.EMPTY;
    }
}
