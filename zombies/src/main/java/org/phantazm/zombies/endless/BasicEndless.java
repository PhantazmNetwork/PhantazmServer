package org.phantazm.zombies.endless;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MathUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.event.mob.ZombiesMobSetupEvent;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.SpawnInfo;
import org.phantazm.zombies.map.Wave;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.ArrayList;
import java.util.Comparator;
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
    //maps can be configured to have lower spawn caps than this, but not higher!
    private static final int ABSOLUTE_MAX_SPAWN_AMOUNT = 250;

    //arbitrary, but prevents excessive wave counts in case of weird scaling
    //maps can be configured to have lower wave caps than this, but not higher!
    private static final int ABSOLUTE_WAVE_COUNT_LIMIT = 32;

    private static final int ABSOLUTE_WAVE_DELAY_MINIMUM = 1;
    private static final int ABSOLUTE_WAVE_DELAY_MAXIMUM = 200;

    private static final double PI_2 = Math.PI * 2;

    public enum ScalingMethod {
        LINEAR,
        LINEAR_SINUSOIDAL,
        CONSTANT,
        SINUSOIDAL
    }

    public record ScalingValue(@NotNull ScalingMethod kind,
        double ceiling,
        double floor,
        double a,
        double b,
        double c,
        double d) {
        public double scale(int x, double base) {
            return MathUtils.clamp(switch (kind) {
                case LINEAR -> base + (a * x);
                case LINEAR_SINUSOIDAL -> base + ((b * x) / a) + ((b / (PI_2)) * Math.sin((PI_2 / a) * x));
                case CONSTANT -> base + a;
                case SINUSOIDAL -> base + (Math.sin(Math.PI * (b * x + a)) * c);
            }, floor, ceiling);
        }

        @Default("a")
        public static @NotNull ConfigElement defaultA() {
            return ConfigPrimitive.of(1);
        }

        @Default("b")
        public static @NotNull ConfigElement defaultB() {
            return ConfigPrimitive.of(1);
        }

        @Default("c")
        public static @NotNull ConfigElement defaultC() {
            return ConfigPrimitive.of(1);
        }

        @Default("d")
        public static @NotNull ConfigElement defaultD() {
            return ConfigPrimitive.of(1);
        }
    }

    public record WeightedMob(@NotNull Key key,
        @NotNull Key spawnType,
        int weight) {
    }

    public interface Theme {
        @NotNull List<WeightedMob> mobs();

        @NotNull ScalingValue waveWeight();

        @NotNull ScalingValue baseWaveDelayTicks();

        @NotNull ScalingValue offsetWaveDelayTicks();

        @NotNull List<Action<List<Mob>>> spawnActions(int wave);

        @NotNull List<Action<Round>> startActions();

        @NotNull List<Action<Round>> endActions();
    }

    @Model("zombies.endless.basic.wave_actions")
    @Cache(false)
    public static class IndicatedWaveActions {
        private final Data data;
        private final List<Action<List<Mob>>> spawnActions;

        @FactoryMethod
        public IndicatedWaveActions(@NotNull Data data,
            @NotNull @Child("spawn_actions") List<Action<List<Mob>>> spawnActions) {
            this.data = data;
            this.spawnActions = spawnActions;
        }

        @DataObject
        public record Data(int wave,
            @NotNull @ChildPath("spawn_actions") ConfigList spawnActions) {
        }
    }

    @Model("zombies.endless.basic.theme")
    @Cache(false)
    public static class BasicTheme implements Theme {
        private final Data data;
        private final Int2ObjectMap<List<Action<List<Mob>>>> actionMap;
        private final List<Action<Round>> startActions;
        private final List<Action<Round>> endActions;

        @FactoryMethod
        public BasicTheme(@NotNull Data data,
            @NotNull @Child("wave_actions") List<IndicatedWaveActions> waveActions,
            @NotNull @Child("start_actions") List<Action<Round>> startActions,
            @NotNull @Child("end_actions") List<Action<Round>> endActions) {
            this.data = data;
            this.actionMap = new Int2ObjectOpenHashMap<>(waveActions.size());
            this.startActions = startActions;
            this.endActions = endActions;

            for (IndicatedWaveActions actions : waveActions) {
                this.actionMap.put(actions.data.wave, actions.spawnActions);
            }
        }

        @Override
        public @NotNull List<WeightedMob> mobs() {
            return data.mobs;
        }

        @Override
        public @NotNull ScalingValue waveWeight() {
            return data.waveWeight;
        }

        @Override
        public @NotNull ScalingValue baseWaveDelayTicks() {
            return data.baseWaveDelayTicks;
        }

        @Override
        public @NotNull ScalingValue offsetWaveDelayTicks() {
            return data.offsetWaveDelayTicks;
        }

        @Override
        public @NotNull List<Action<List<Mob>>> spawnActions(int wave) {
            return actionMap.getOrDefault(wave, List.of());
        }

        @Override
        public @NotNull List<Action<Round>> startActions() {
            return startActions;
        }

        @Override
        public @NotNull List<Action<Round>> endActions() {
            return endActions;
        }

        @DataObject
        public record Data(@NotNull List<WeightedMob> mobs,
            @NotNull ScalingValue waveWeight,
            @NotNull ScalingValue baseWaveDelayTicks,
            @NotNull ScalingValue offsetWaveDelayTicks,
            @NotNull @ChildPath("wave_actions") ConfigList waveActions,
            @NotNull @ChildPath("start_actions") ConfigList startActions,
            @NotNull @ChildPath("end_actions") ConfigList endActions) {
        }
    }

    public record Introduction(@NotNull List<WeightedMob> spawns,
        int startRound,
        int period,
        int count) {

    }

    private final Data data;
    private final Supplier<ZombiesScene> zombiesScene;
    private final List<Theme> themes;
    private final List<Introduction> introductions;

    @FactoryMethod
    public BasicEndless(@NotNull Data data, @NotNull Supplier<ZombiesScene> zombiesScene,
        @NotNull List<Theme> themes, @NotNull List<Introduction> introductions) {
        this.data = data;
        this.zombiesScene = zombiesScene;
        this.themes = themes;

        this.introductions = new ArrayList<>(introductions);
        this.introductions.sort(Comparator.comparingInt(Introduction::startRound));
    }

    @Override
    public @NotNull Round generateRound(int round) {
        ZombiesScene zombiesScene = this.zombiesScene.get();
        int endlessRound = round - zombiesScene.map().roundHandler().roundCount();
        if (endlessRound < 1) {
            throw new IllegalArgumentException("Tried to generate endless round before final round");
        }

        if (themes.isEmpty()) {
            throw new IllegalArgumentException("No defined themes");
        }

        Theme roundTheme = themes.get((endlessRound - 1) % themes.size());
        List<WeightedMob> themeMobs = roundTheme.mobs();
        List<WeightedMob> introducedMobs = introducedMobs(endlessRound);

        double weightSum = 0;
        for (WeightedMob themeMob : themeMobs) {
            weightSum += themeMob.weight;
        }

        for (WeightedMob introducedMob : introducedMobs) {
            weightSum += introducedMob.weight;
        }

        double[] spawnPercentages = new double[themeMobs.size() + introducedMobs.size()];
        int i;
        for (i = 0; i < themeMobs.size(); i++) {
            spawnPercentages[i] = themeMobs.get(i).weight / weightSum;
        }

        for (int j = 0; j < introducedMobs.size(); j++) {
            spawnPercentages[i + j] = introducedMobs.get(j).weight / weightSum;
        }

        int waveCount = Math.min((int) Math.rint(data.waveScaling.scale(endlessRound, data.waveBase)), ABSOLUTE_WAVE_COUNT_LIMIT);
        List<Wave> waves = new ArrayList<>(waveCount);

        double mobSpawnAmount = Math.min(data.spawnAmountScaling.scale(endlessRound, data.spawnAmountBase), ABSOLUTE_MAX_SPAWN_AMOUNT);
        int cap = (int) Math.rint(mobSpawnAmount);

        double[] waveWeights = new double[waveCount];
        for (int j = 0; j < waveCount; j++) {
            waveWeights[j] = roundTheme.waveWeight().scale(j + 1, 0);
        }

        double waveWeightSum = 0;
        for (double weight : waveWeights) {
            waveWeightSum += weight;
        }

        for (int j = 0; j < waveCount; j++) {
            waveWeights[j] /= waveWeightSum;
        }

        int[] mergedCounts = new int[spawnPercentages.length];

        int totalMobs = 0;
        for (int j = 0; j < mergedCounts.length; j++) {
            int additionalMobs = (int) Math.rint((j < themeMobs.size() ? themeMobs.get(j) :
                introducedMobs.get(j - themeMobs.size())).weight * spawnPercentages[j]);

            int newTotal = totalMobs + additionalMobs;
            if (newTotal > cap) {
                additionalMobs -= newTotal - cap;
                newTotal = cap;
            }

            mergedCounts[j] = additionalMobs;
            totalMobs = newTotal;
        }

        int totalMobCount = 0;
        for (int j = 0; j < waveCount; j++) {
            int baseDelay = (int) Math.rint(roundTheme.baseWaveDelayTicks().scale(endlessRound, 0));
            int actualDelay = MathUtils.clamp((int) Math.rint(roundTheme.offsetWaveDelayTicks()
                .scale(j + 1, baseDelay)), ABSOLUTE_WAVE_DELAY_MINIMUM, ABSOLUTE_WAVE_DELAY_MAXIMUM);

            List<Action<List<Mob>>> spawnActions = roundTheme.spawnActions(j + 1);

            int[] waveCounts = new int[mergedCounts.length];
            List<SpawnInfo> spawns = new ArrayList<>(waveCounts.length);
            for (int k = 0; k < waveCounts.length; k++) {
                int mobCount = (int) Math.rint(mergedCounts[k] * waveWeights[j]);

                int newTotal = totalMobCount + mobCount;
                if (newTotal > totalMobs) {
                    mobCount -= newTotal - totalMobs;
                    newTotal = totalMobs;
                }

                if (mobCount == 0) {
                    continue;
                }

                WeightedMob mob = k < themeMobs.size() ? themeMobs.get(k) : introducedMobs.get(k - themeMobs.size());
                spawns.add(new SpawnInfo(mob.key, mob.spawnType, mobCount));
                totalMobCount = newTotal;
            }

            waves.add(new Wave(actualDelay, spawnActions, spawns));
        }

        return new Round(round, waves, roundTheme.startActions(), roundTheme.endActions(), this.zombiesScene);
    }

    private List<WeightedMob> introducedMobs(int endlessRound) {
        List<WeightedMob> introducedMobs = null;
        for (Introduction introduction : introductions) {
            if (introduction.startRound() > endlessRound) {
                //any future introductions are too high
                return introducedMobs == null ? List.of() : introducedMobs;
            }

            int count = introduction.count();
            if (count == 0) {
                //should logically never be introduced, but probably a config mistake
                continue;
            }

            if (introduction.spawns().isEmpty()) {
                //most likely another config mistake
                continue;
            }

            int period = introduction.period();
            int start = introduction.startRound();
            int sinceStart = endlessRound - start;
            if (sinceStart == 0 ||
                ((count < 0 || endlessRound < start + (period * (count - 1) + 1)) && sinceStart % period == 0)) {
                if (introducedMobs == null) {
                    introducedMobs = new ArrayList<>();
                }

                introducedMobs.addAll(introduction.spawns());
            }
        }

        return introducedMobs;
    }

    @Override
    public void init() {
        this.zombiesScene.get().addListener(ZombiesMobSetupEvent.class, this::onMobSetup);
    }

    private void onMobSetup(@NotNull ZombiesMobSetupEvent event) {
        Mob mob = event.getEntity();
        if (mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.BYPASS_ENDLESS_SCALING)) {
            return;
        }

        this.zombiesScene.get().getAcquirable().sync(self -> {
            RoundHandler roundHandler = self.map().roundHandler();
            if (!roundHandler.isEndless()) {
                return;
            }

            int endlessRound = roundHandler.currentRoundIndex() - roundHandler.roundCount();
            if (endlessRound < 1) {
                return;
            }

            scaleAttribute(mob, Attribute.MAX_HEALTH, HEALTH_MODIFIER, HEALTH_MODIFIER_STRING, data.healthScaling, endlessRound);
            scaleAttribute(mob, Attribute.ATTACK_DAMAGE, DAMAGE_MODIFIER, DAMAGE_MODIFIER_STRING, data.damageScaling, endlessRound);
            scaleAttribute(mob, Attribute.MOVEMENT_SPEED, SPEED_MODIFIER, SPEED_MODIFIER_STRING, data.speedScaling, endlessRound);

            mob.heal();
        });
    }

    private static void scaleAttribute(Mob mob, Attribute attribute, UUID id, String name, ScalingValue scalingValue,
        int endlessRound) {
        AttributeInstance instance = mob.getAttribute(attribute);
        instance.addModifier(new AttributeModifier(id, name, scalingValue.scale(endlessRound, instance.getBaseValue()),
            AttributeOperation.ADDITION));
    }

    @DataObject
    public record Data(@NotNull ScalingValue healthScaling,
        @NotNull ScalingValue damageScaling,
        @NotNull ScalingValue speedScaling,
        @NotNull ScalingValue spawnAmountScaling,
        double spawnAmountBase,
        @NotNull ScalingValue waveScaling,
        double waveBase) {
    }
}