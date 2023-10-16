package org.phantazm.zombies.endless;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.event.mob.ZombiesMobSetupEvent;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.SpawnInfo;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Model("zombies.endless.basic")
@Cache(false)
public class BasicEndless implements Endless {
    private static final UUID HEALTH_MODIFIER = UUID.randomUUID();
    private static final String HEALTH_MODIFIER_STRING = HEALTH_MODIFIER.toString();

    private static final UUID DAMAGE_MODIFIER = UUID.randomUUID();
    private static final String DAMAGE_MODIFIER_STRING = DAMAGE_MODIFIER.toString();

    private static final UUID SPEED_MODIFIER = UUID.randomUUID();
    private static final String SPEED_MODIFIER_STRING = SPEED_MODIFIER.toString();

    //could be substantially raised but will degrade client performance
    private static final int MOB_SPAWN_LIMIT = 250;

    public enum ScalingMethod {
        /**
         * ax, where {@code x} is the current endless round, and {@code a} is the slope (value increment)
         */
        LINEAR,

        /**
         * {@code (ax + sin(ax)) * b}, where {@code x} is the current endless round, {@code a} is the horizontal
         * stretch, and {@code b} is the vertical stretch.
         */
        SINUSOIDAL
    }

    public interface Theme {
        @NotNull List<SpawnInfo> spawns();
    }

    private final Data data;
    private final Supplier<ZombiesScene> zombiesScene;

    @FactoryMethod
    public BasicEndless(@NotNull Data data, @NotNull Supplier<ZombiesScene> zombiesScene) {
        this.data = data;
        this.zombiesScene = zombiesScene;
    }

    @Override
    public @NotNull Round generateRound(int roundIndex) {
        ZombiesScene zombiesScene = this.zombiesScene.get();
        int endlessIndex = roundIndex - zombiesScene.map().roundHandler().roundCount() - 1;
        if (endlessIndex < 0) {
            throw new IllegalArgumentException("Tried to generate endless round before final round");
        }

        return null;
    }

    @Override
    public void init() {
        ZombiesScene zombiesScene = this.zombiesScene.get();

        zombiesScene.addListener(ZombiesMobSetupEvent.class, event -> {
            Mob mob = event.getEntity();
            if (mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.BYPASS_ENDLESS_SCALING)) {
                return;
            }

            zombiesScene.getAcquirable().sync(self -> {
                RoundHandler roundHandler = zombiesScene.map().roundHandler();
                if (!roundHandler.isEndless()) {
                    return;
                }

                int endlessRound = roundHandler.currentRoundIndex() - roundHandler.roundCount();
                if (endlessRound < 1) {
                    return;
                }

                AttributeInstance health = mob.getAttribute(Attribute.MAX_HEALTH);
                AttributeInstance damage = mob.getAttribute(Attribute.ATTACK_DAMAGE);
                AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);

                health.addModifier(new AttributeModifier(HEALTH_MODIFIER, HEALTH_MODIFIER_STRING,
                    data.healthScaling.scale(endlessRound, health.getBaseValue()), AttributeOperation.ADDITION));

                damage.addModifier(new AttributeModifier(DAMAGE_MODIFIER, DAMAGE_MODIFIER_STRING,
                    data.damageScaling.scale(endlessRound, damage.getBaseValue()), AttributeOperation.ADDITION));

                speed.addModifier(new AttributeModifier(SPEED_MODIFIER, SPEED_MODIFIER_STRING,
                    data.speedScaling.scale(endlessRound, speed.getBaseValue()), AttributeOperation.ADDITION));

                mob.heal();
            });
        });
    }

    public record ScalingValue(@NotNull ScalingMethod kind,
        double cap,
        double a,
        double b) {
        public double scale(int endlessRound, double baseValue) {
            return Math.min(switch (kind) {
                case LINEAR -> baseValue + (a * endlessRound);
                case SINUSOIDAL -> {
                    double invBPi = b / (2 * Math.PI);
                    double invA = 2 / a;

                    double t1 = invA * Math.PI * endlessRound;

                    yield invBPi * (t1 + Math.cos(t1));
                }
            }, cap);
        }

        @Default("a")
        public static @NotNull ConfigElement defaultA() {
            return ConfigPrimitive.of(1);
        }

        @Default("b")
        public static @NotNull ConfigElement defaultB() {
            return ConfigPrimitive.of(1);
        }
    }

    @DataObject
    public record Data(@NotNull ScalingValue healthScaling,
        @NotNull ScalingValue damageScaling,
        @NotNull ScalingValue speedScaling) {
    }
}
