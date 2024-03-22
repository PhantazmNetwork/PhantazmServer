package org.phantazm.zombies.endless;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
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
import java.util.function.Supplier;

@Model("zombies.endless.basic")
@Cache(false)
public class BasicEndless implements Endless {
    //could be substantially raised but will degrade client performance
    //maps can be configured to have lower spawn caps than this, but not higher!
    @VisibleForTesting
    static final int ABSOLUTE_MAX_SPAWN_AMOUNT = 250;

    private static final int ABSOLUTE_WAVE_DELAY_MINIMUM = 1;
    private static final int ABSOLUTE_WAVE_DELAY_MAXIMUM = 200;

    private static final double PI_2 = Math.PI * 2;

    public enum ScalingMethod {
        LINEAR,
        LINEAR_SINUSOIDAL,
        CONSTANT,
        SINUSOIDAL
    }

    @Default("""
        {
          floor=0,
          a=1,
          b=1,
          c=1,
          d=1
        }
        """)
    public record ScalingValue(@NotNull ScalingMethod kind,
        double ceiling,
        double floor,
        double a,
        double b,
        double c,
        double d) {
        public double scale(int x, double base) {
            return base + MathUtils.clamp(switch (kind) {
                case LINEAR -> (a * x);
                case LINEAR_SINUSOIDAL -> ((b * x) / a) + ((b / (PI_2)) * Math.sin((PI_2 / a) * x));
                case CONSTANT -> a;
                case SINUSOIDAL -> (Math.sin(Math.PI * (b * x + a)) * c);
            }, floor, ceiling);
        }
    }

    public record IndexedWeightedMob(@NotNull Key key,
        @NotNull Key spawnType,
        int wave,
        int weight) {

    }

    public record WeightedMob(@NotNull Key key,
        @NotNull Key spawnType,
        int weight) {
    }

    public enum WaveIntroduceMode {
        INSERT,
        APPEND,
        SET
    }

    @Model("zombies.endless.basic.weighted_wave")
    @Cache(false)
    public static class WeightedWave {
        private final Data data;
        private final List<Action<List<Mob>>> spawnActions;

        @FactoryMethod
        public WeightedWave(@NotNull Data data, @NotNull @Child("spawnActions") List<Action<List<Mob>>> spawnActions) {
            this.data = data;
            this.spawnActions = spawnActions;
        }

        @Default("""
            {
              introduceMode='APPEND',
              introduceIndex=-1,
              spawnActions=[]
            }
            """)
        @DataObject
        public record Data(@NotNull List<WeightedMob> wave,
            @NotNull WaveIntroduceMode introduceMode,
            int introduceIndex,
            int mobCountWeight) {
        }
    }

    public interface Theme {
        @NotNull
        List<WeightedWave> waves();

        @NotNull
        ScalingValue baseWaveDelayTicks();

        @NotNull
        ScalingValue offsetWaveDelayTicks();

        @NotNull
        List<Action<Round>> startActions();

        @NotNull
        List<Action<Round>> endActions();
    }

    @Model("zombies.endless.basic.theme")
    @Cache(false)
    public static class BasicTheme implements Theme {
        private final Data data;
        private final List<WeightedWave> waves;
        private final List<Action<Round>> startActions;
        private final List<Action<Round>> endActions;

        @FactoryMethod
        public BasicTheme(@NotNull Data data,
            @NotNull @Child("waves") List<WeightedWave> waves,
            @NotNull @Child("startActions") List<Action<Round>> startActions,
            @NotNull @Child("endActions") List<Action<Round>> endActions) {
            this.data = data;
            this.waves = waves;
            this.startActions = startActions;
            this.endActions = endActions;
        }

        @Override
        public @NotNull List<WeightedWave> waves() {
            return waves;
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
        public @NotNull List<Action<Round>> startActions() {
            return startActions;
        }

        @Override
        public @NotNull List<Action<Round>> endActions() {
            return endActions;
        }

        @DataObject
        public record Data(
            @NotNull ScalingValue baseWaveDelayTicks,
            @NotNull ScalingValue offsetWaveDelayTicks) {
        }
    }

    @Model("zombies.endless.basic.introduction")
    @Cache(false)
    public static class Introduction {
        private final Data data;
        private final List<WeightedWave> waves;

        @FactoryMethod
        public Introduction(@NotNull Data data, @NotNull @Child("waves") List<WeightedWave> waves) {
            this.data = data;
            this.waves = waves;
        }

        @Default("""
            {
              waves=[],
              mobs=[],
              count=-1
            }
            """)
        public record Data(@NotNull List<IndexedWeightedMob> mobs,
            int startRound,
            int period,
            int count) {

        }
    }

    private final Data data;
    private final Supplier<ZombiesScene> zombiesScene;
    private final List<Theme> themes;
    private final List<Introduction> introductions;

    @FactoryMethod
    public BasicEndless(@NotNull Data data, @NotNull Supplier<ZombiesScene> zombiesScene,
        @NotNull @Child("introductions") List<Introduction> introductions,
        @NotNull @Child("themes") List<Theme> themes) {
        this.data = data;
        this.zombiesScene = zombiesScene;
        this.themes = themes;

        this.introductions = introductions;
        this.introductions.sort(Comparator.comparingInt(intro -> intro.data.startRound));
    }

    @Override
    public @NotNull Round generateRound(int roundIndex) {
        int endlessRound = (roundIndex - this.zombiesScene.get().map().roundHandler().roundCount()) + 1;
        if (endlessRound < 1) {
            throw new IllegalArgumentException("Tried to generate endless round before final round");
        }

        if (themes.isEmpty()) {
            throw new IllegalArgumentException("No defined themes");
        }

        int endlessRoundIndex = endlessRound - 1;

        int baseMobCount = Math.min((int) Math.rint(data.spawnAmountScaling.scale(endlessRoundIndex,
            data.spawnAmountBase)), ABSOLUTE_MAX_SPAWN_AMOUNT);

        List<Introduction> applicableIntroductions = applicableIntroductions(endlessRoundIndex);
        Theme currentTheme = themes.get(endlessRoundIndex % themes.size());

        List<WeightedWave> weightedWaves = new ArrayList<>(currentTheme.waves());

        List<WeightedWave> inserts = null;
        List<IntObjectPair<WeightedWave>> sets = null;
        Int2ObjectMap<List<WeightedMob>> introducedMobs = null;
        for (Introduction introduction : applicableIntroductions) {
            List<IndexedWeightedMob> introductionMobs = introduction.data.mobs;
            if (!introductionMobs.isEmpty()) {
                if (introducedMobs == null) {
                    introducedMobs = new Int2ObjectOpenHashMap<>();
                }

                for (IndexedWeightedMob indexedWeightedMob : introductionMobs) {
                    introducedMobs.computeIfAbsent(indexedWeightedMob.wave, ignored -> new ArrayList<>())
                        .add(new WeightedMob(indexedWeightedMob.key, indexedWeightedMob.spawnType, indexedWeightedMob.weight));
                }
            }

            for (WeightedWave introducedWave : introduction.waves) {
                switch (introducedWave.data.introduceMode) {
                    case INSERT ->
                        weightedWaves.add(Math.max(0, Math.min(introducedWave.data.introduceIndex, weightedWaves.size())),
                            introducedWave);
                    case APPEND -> {
                        if (inserts == null) {
                            inserts = new ArrayList<>(4);
                        }

                        inserts.add(introducedWave);
                    }
                    case SET -> {
                        if (sets == null) {
                            sets = new ArrayList<>(4);
                        }

                        sets.add(IntObjectPair.of(introducedWave.data.introduceIndex, introducedWave));
                    }
                }
            }
        }

        if (inserts != null) {
            weightedWaves.addAll(inserts);
        }

        if (sets != null) {
            for (IntObjectPair<WeightedWave> pair : sets) {
                weightedWaves.set(Math.max(0, Math.min(pair.firstInt(), weightedWaves.size() - 1)), pair.second());
            }
        }

        int[] waveWeights = new int[weightedWaves.size()];
        for (int i = 0; i < waveWeights.length; i++) {
            waveWeights[i] = weightedWaves.get(i).data.mobCountWeight;
        }

        int[] waveMobCounts = distributeWeights(waveWeights, baseMobCount);

        List<Wave> waves = new ArrayList<>(weightedWaves.size());

        double baseWaveDelay = currentTheme.baseWaveDelayTicks().scale(endlessRoundIndex, data.waveDelayBase);
        for (int i = 0; i < weightedWaves.size(); i++) {
            int waveMobCount = waveMobCounts[i];
            WeightedWave currentWave = weightedWaves.get(i);

            List<WeightedMob> actualWaveMobs = new ArrayList<>(currentWave.data.wave);
            List<WeightedMob> introduced = introducedMobs == null ? null : introducedMobs.get(i);
            if (introduced != null) {
                actualWaveMobs.addAll(introduced);
            }

            List<SpawnInfo> waveSpawns = new ArrayList<>(actualWaveMobs.size());
            int[] mobTypeWeights = new int[actualWaveMobs.size()];
            for (int j = 0; j < actualWaveMobs.size(); j++) {
                mobTypeWeights[j] = actualWaveMobs.get(j).weight;
            }

            int[] mobTypeCounts = distributeWeights(mobTypeWeights, waveMobCount);
            for (int j = 0; j < actualWaveMobs.size(); j++) {
                WeightedMob weightedMob = actualWaveMobs.get(j);
                waveSpawns.add(new SpawnInfo(weightedMob.key, weightedMob.spawnType, mobTypeCounts[j]));
            }

            long waveDelayTicks = Math.max(ABSOLUTE_WAVE_DELAY_MINIMUM, Math.min(ABSOLUTE_WAVE_DELAY_MAXIMUM,
                (long) Math.rint(currentTheme.offsetWaveDelayTicks().scale(i, baseWaveDelay))));

            waves.add(new Wave(waveDelayTicks, currentWave.spawnActions, waveSpawns));
        }

        return new Round(roundIndex + 1, waves, currentTheme.startActions(), currentTheme.endActions(),
            this.zombiesScene);
    }

    private static int[] distributeWeights(int[] weights, int count) {
        double[] normalizedWeights = normalizeWeights(weights);
        double[] exactCounts = new double[normalizedWeights.length];
        for (int i = 0; i < exactCounts.length; i++) {
            exactCounts[i] = normalizedWeights[i] * count;
        }

        int[] actualCounts = new int[weights.length];
        balance(actualCounts, exactCounts);
        return actualCounts;
    }

    @VisibleForTesting
    static double[] normalizeWeights(int[] weights) {
        double weightSum = 0;
        double[] normalized = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            int value = Math.max(1, Math.abs(weights[i]));
            weightSum += value;
            normalized[i] = value;
        }

        for (int i = 0; i < weights.length; i++) {
            normalized[i] /= weightSum;
        }

        return normalized;
    }

    @VisibleForTesting
    static void balance(int[] actualCounts, double[] exactCounts) {
        assert actualCounts.length == exactCounts.length;

        double overflow;
        while (true) {
            overflow = Double.MAX_VALUE;
            int smallestCurrentDecimalPartIndex = -1;
            double smallestFloor = -1;
            for (int i = 0; i < exactCounts.length; i++) {
                double current = exactCounts[i];
                double currentFloor = Math.floor(current);
                double currentDecimalPart = current - currentFloor;
                if (currentDecimalPart < 0.001) {
                    continue;
                }

                if (currentDecimalPart < overflow) {
                    overflow = currentDecimalPart;
                    smallestCurrentDecimalPartIndex = i;
                    smallestFloor = currentFloor;
                }
            }

            if (smallestCurrentDecimalPartIndex == -1) {
                break;
            }

            int largestOtherDecimalPartIndex = getLargestDecimalPartIndex(exactCounts, smallestCurrentDecimalPartIndex);
            if (largestOtherDecimalPartIndex == -1) {
                break;
            }

            exactCounts[smallestCurrentDecimalPartIndex] = smallestFloor;
            exactCounts[largestOtherDecimalPartIndex] += overflow;
        }

        for (int i = 0; i < exactCounts.length; i++) {
            actualCounts[i] = (int) Math.floor(exactCounts[i]);
        }

        if (overflow < 1 && overflow >= 0.5) {
            double largest = Double.MIN_VALUE;
            int index = -1;
            for (int i = 0; i < exactCounts.length; i++) {
                double current = exactCounts[i];
                if (current > largest) {
                    largest = current;
                    index = i;
                }
            }

            if (index != -1) {
                actualCounts[index]++;
            }
        }
    }

    private static int getLargestDecimalPartIndex(double[] exactCounts, int skipIndex) {
        double largestOtherDecimalPart = Double.MIN_VALUE;
        int largestOtherDecimalPartIndex = -1;
        for (int j = 0; j < exactCounts.length; j++) {
            if (j == skipIndex) {
                continue;
            }

            double other = exactCounts[j];
            double otherDecimalPart = other - Math.floor(other);
            if (otherDecimalPart < 0.001) {
                continue;
            }

            if (otherDecimalPart > largestOtherDecimalPart) {
                largestOtherDecimalPart = otherDecimalPart;
                largestOtherDecimalPartIndex = j;
            }
        }
        return largestOtherDecimalPartIndex;
    }

    @Override
    public void init() {
        this.zombiesScene.get().addListener(ZombiesMobSetupEvent.class, this::onMobSetup);
    }

    private List<Introduction> applicableIntroductions(int endlessRound) {
        List<Introduction> applicableIntroductions = null;
        for (Introduction introduction : introductions) {
            if (introduction.data.startRound() > endlessRound) {
                //any future introductions are too high
                return applicableIntroductions == null ? List.of() : applicableIntroductions;
            }

            int count = introduction.data.count();
            if (count == 0) {
                //should logically never be introduced, but probably a config mistake
                continue;
            }

            if (introduction.waves.isEmpty() && introduction.data.mobs.isEmpty()) {
                //most likely another config mistake
                continue;
            }

            int period = introduction.data.period();
            int start = introduction.data.startRound();
            int sinceStart = endlessRound - start;
            if (sinceStart == 0 ||
                ((count < 0 || endlessRound < start + (period * (count - 1) + 1)) && sinceStart % period == 0)) {
                if (applicableIntroductions == null) {
                    applicableIntroductions = new ArrayList<>();
                }

                applicableIntroductions.add(introduction);
            }
        }

        return applicableIntroductions == null ? List.of() : applicableIntroductions;
    }

    private void onMobSetup(@NotNull ZombiesMobSetupEvent event) {
        Mob mob = event.getEntity();
        ConfigElement bypassesScaling = mob.data().extra().atOrDefault(ExtraNodeKeys.BYPASS_ENDLESS_SCALING,
            ConfigPrimitive.FALSE);

        if (!bypassesScaling.isBoolean() || bypassesScaling.asBoolean()) {
            return;
        }

        this.zombiesScene.get().getAcquirable().sync(self -> {
            RoundHandler roundHandler = self.map().roundHandler();
            if (!roundHandler.isEndless()) {
                return;
            }

            int endlessRound = (roundHandler.currentRoundIndex() - roundHandler.roundCount()) + 1;
            if (endlessRound < 1) {
                return;
            }

            scaleAttribute(mob, Attribute.MAX_HEALTH, data.healthScaling, endlessRound);
            scaleAttribute(mob, Attribute.ATTACK_DAMAGE, data.damageScaling, endlessRound);
            scaleAttribute(mob, Attribute.MOVEMENT_SPEED, data.speedScaling, endlessRound);

            mob.heal();
        });
    }

    private static void scaleAttribute(Mob mob, Attribute attribute, ScalingValue scalingValue, int endlessRound) {
        AttributeInstance instance = mob.getAttribute(attribute);
        instance.setBaseValue((float) scalingValue.scale(endlessRound, instance.getBaseValue()));
    }

    @Default("""
        {
          waveBase=3,
          waveDelayBase=0,
        }
        """)
    @DataObject
    public record Data(@NotNull ScalingValue healthScaling,
        @NotNull ScalingValue damageScaling,
        @NotNull ScalingValue speedScaling,
        @NotNull ScalingValue spawnAmountScaling,
        double spawnAmountBase,
        double waveDelayBase) {

    }
}