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
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.BasicMobSpawner;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Model("mob.skill.lingering")
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
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, holder, selector.apply(holder), targetSkills);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
        @NotNull @ChildPath("target_skills") List<String> targetSkills,
        int lifetime) {

    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final List<Skill> targetSkills;

        private Internal(Data data, ExtensionHolder holder, Selector selector, List<SkillComponent> targetSkills) {
            super(selector);
            this.data = data;

            List<Skill> skills = new ArrayList<>(targetSkills.size());
            for (SkillComponent skillComponent : targetSkills) {
                skills.add(skillComponent.apply(holder));
            }

            this.targetSkills = List.copyOf(skills);
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Instance instance = mob.getInstance();
            if (instance == null) {
                return;
            }

            Collection<? extends Point> locations = target.locations();
            if (locations.isEmpty()) {
                return;
            }

            List<Reference<Mob>> spawnedMobs = data.lifetime < 0 ? null : new ArrayList<>(locations.size());
            for (Point point : locations) {
                Mob armorStand = new Mob(EntityType.ARMOR_STAND);

                ArmorStandMeta armorStandMeta = (ArmorStandMeta) armorStand.getEntityMeta();
                armorStandMeta.setMarker(true);
                armorStandMeta.setInvisible(true);
                armorStandMeta.setHasNoGravity(true);
                armorStand.setHasPhysics(false);
                armorStand.setExtensions(mob.extensions().sibling(true));
                armorStand.addSkills(targetSkills);

                armorStand.setInstance(instance, point);

                if (spawnedMobs != null) {
                    spawnedMobs.add(new WeakReference<>(armorStand));
                }
            }

            if (data.lifetime < 0) {
                return;
            }

            if (data.lifetime == 0) {
                killMobs(spawnedMobs);
                return;
            }

            Scheduler scheduler = mob.extensions().getOrDefault(BasicMobSpawner.SCHEDULER_KEY,
                MinecraftServer::getSchedulerManager);
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