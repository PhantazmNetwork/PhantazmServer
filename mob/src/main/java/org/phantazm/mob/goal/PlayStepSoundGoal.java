package org.phantazm.mob.goal;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

// TODO: this naively tracks movement, whereas vanilla MC is handling velocity changes. Find a better way to do this
@Model("mob.goal.play_step_sound")
@Cache(false)
public class PlayStepSoundGoal implements GoalCreator {

    private final Random random;

    @FactoryMethod
    public PlayStepSoundGoal(@NotNull Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull PhantazmMob mob) {
        return new Goal(mob, random);
    }

    private static class Goal implements ProximaGoal {

        private static final Map<EntityType, Consumer<Goal>> STEP_SOUND_MAP = new HashMap<>();

        static {
            // TODO: add special handlers for steps
        }

        private final PhantazmMob mob;

        private final Random random;

        private Point lastPosition;

        private double nextStepDistance = 1.0;

        private double distanceTraveled = 0;

        private int age = 0;

        private int lastChimeAge = 0;

        private float lastChimeIntensity = 0;

        public Goal(@NotNull PhantazmMob mob, @NotNull Random random) {
            this.mob = Objects.requireNonNull(mob, "mob");
            this.random = Objects.requireNonNull(random, "random");
            this.lastPosition = mob.entity().getPosition();
        }

        @Override
        public boolean shouldStart() {
            return true;
        }

        @Override
        public boolean shouldEnd() {
            return false;
        }

        @Override
        public void tick(long time) {
            ++age;

            Point newPosition = mob.entity().getPosition();
            double delta = newPosition.distance(lastPosition) * 0.6;
            distanceTraveled += delta;
            lastPosition = newPosition;

            if (distanceTraveled > nextStepDistance) {
                nextStepDistance = distanceTraveled + 1; // differs from regular MC since not handling teleports, etc.
                playStepSound();
            }
        }

        private void playStepSound() {
            Instance instance = mob.entity().getInstance();
            if (instance == null) {
                return;
            }

            Point pos = mob.entity().getPosition();
            Vec landingPos = new Vec(pos.blockX(), (int)Math.floor(pos.y() - 0.2), pos.blockZ());
            Block landingBlock = instance.getBlock(landingPos);
            if (landingBlock.isAir()) {
                Vec lowerPos = landingPos.sub(0, -1, 0);
                Block under = instance.getBlock(lowerPos);
                if (false) { // TODO: fence, wall, fence gate block tags
                    landingPos = lowerPos;
                    landingBlock = under;
                }
            }

            if (landingBlock.compare(Block.AMETHYST_BLOCK) || landingBlock.compare(Block.BUDDING_AMETHYST)) {
                lastChimeIntensity *= (float)Math.pow(0.997, age - lastChimeAge);
                lastChimeIntensity = Math.min(1.0f, lastChimeIntensity + 0.07F);
                float f = 0.5F + lastChimeIntensity * random.nextFloat() * 1.2F;
                float g = 0.1F + lastChimeIntensity * 1.2F;
                instance.playSound(Sound.sound(SoundEvent.BLOCK_AMETHYST_BLOCK_CHIME.key(), Sound.Source.HOSTILE, g, f),
                        mob.entity().getPosition());
                lastChimeAge = age;
            }

            Consumer<Goal> handler = STEP_SOUND_MAP.get(mob.entity().getEntityType());
            if (handler != null) {
                handler.accept(this);
                return;
            }

            Block block = instance.getBlock(landingPos.add(0, 1, 0));
            if (false) {
                // TODO: block = INSIDE_STEP_SOUND_BLOCKS
            }

            instance.playSound(Sound.sound(block.getStepSound().key(), Sound.Source.HOSTILE, 1.0F, 1.0F),
                    mob.entity().getPosition());
        }

    }

}
