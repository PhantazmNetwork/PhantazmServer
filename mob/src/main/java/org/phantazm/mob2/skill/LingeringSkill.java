package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.*;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Model("mob.skill.lingering_skill")
@Cache
public class LingeringSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;
    private final List<SkillComponent> targetSkills;

    @FactoryMethod
    public LingeringSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector,
        @NotNull @Child("target_skills") List<SkillComponent> targetSkills) {
        this.data = data;
        this.selector = selector;
        this.targetSkills = targetSkills;
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, mob, selector.apply(mob, injectionStore), targetSkills, injectionStore,
            injectionStore.getOrDefault(InjectionKeys.SCHEDULER, MinecraftServer::getSchedulerManager));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
        @NotNull @ChildPath("target_skills") List<String> targetSkills,
        int lifetime) {

    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final List<SkillComponent> targetSkills;
        private final InjectionStore injectionStore;
        private final Scheduler scheduler;

        private Internal(Data data, Mob self, Selector selector, List<SkillComponent> targetSkills,
            InjectionStore injectionStore, Scheduler scheduler) {
            super(self, selector);
            this.data = data;
            this.targetSkills = targetSkills;
            this.injectionStore = injectionStore;
            this.scheduler = scheduler;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            Instance instance = self.getInstance();
            if (instance == null) {
                return;
            }

            Collection<? extends Point> locations = target.locations();
            List<Reference<Mob>> spawnedMobs = data.lifetime < 0 ? null : new ArrayList<>(locations.size());
            for (Point point : locations) {
                Mob mob = new Mob(EntityType.ARMOR_STAND, UUID.randomUUID());
                ArmorStandMeta armorStandMeta = (ArmorStandMeta) mob.getEntityMeta();
                armorStandMeta.setMarker(true);
                armorStandMeta.setInvisible(true);
                armorStandMeta.setHasNoGravity(true);
                mob.setHasPhysics(false);

                for (SkillComponent skillComponent : targetSkills) {
                    mob.addSkill(skillComponent.apply(mob, injectionStore));
                }

                mob.setInstance(instance, point);

                if (spawnedMobs != null) {
                    spawnedMobs.add(new WeakReference<>(mob));
                }
            }

            if (data.lifetime < 0) {
                return;
            }

            if (data.lifetime == 0) {
                killMobs(spawnedMobs);
                return;
            }

            scheduler.scheduleTask(() -> {
                killMobs(spawnedMobs);
            }, TaskSchedule.tick(data.lifetime), TaskSchedule.stop());
        }

        private void killMobs(List<Reference<Mob>> spawnedMobs) {
            for (Reference<Mob> mobReference : spawnedMobs) {
                Mob mob = mobReference.get();
                if (mob == null) {
                    continue;
                }

                mob.getAcquirable().sync(self -> {
                    ((Mob) self).kill();
                });
            }
        }
    }
}