package org.phantazm.zombies.endless;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
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
import java.util.function.Supplier;

@Model("zombies.endless.basic")
@Cache(false)
public class BasicEndless implements Endless {
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
            @NotNull @ChildPath("spawn_actions") List<String> spawnActions) {
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
            @NotNull @ChildPath("wave_actions") List<String> waveActions,
            @NotNull @ChildPath("start_actions") List<String> startActions,
            @NotNull @ChildPath("end_actions") List<String> endActions) {
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
        @NotNull @Child("themes") List<Theme> themes) {
        this.data = data;
        this.zombiesScene = zombiesScene;
        this.themes = themes;

        this.introductions = new ArrayList<>(data.introductions);
        this.introductions.sort(Comparator.comparingInt(Introduction::startRound));
    }

    @Override
    public @NotNull Round generateRound(int roundIndex) {
        ZombiesScene zombiesScene = this.zombiesScene.get();
        int endlessRound = (roundIndex - zombiesScene.map().roundHandler().roundCount()) + 1;
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
            waveWeights[j] = roundTheme.waveWeight().scale(j + 1, data.waveWeightBase);
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
            int additionalMobs = Math.max((int) Math.rint(spawnPercentages[j] * cap), 1);

            int newTotal = totalMobs + additionalMobs;
            if (newTotal > cap) {
                additionalMobs -= newTotal - cap;
                newTotal = cap;
            }

            mergedCounts[j] = additionalMobs;
            totalMobs = newTotal;
        }

        int[][] allocatedMobs = new int[mergedCounts.length][waveCount];
        for (int j = 0; j < mergedCounts.length; j++) {
            int[] waveCounts = allocatedMobs[j];

            int mobCount = mergedCounts[j];
            int totalSpawned = 0;
            for (int k = 0; k < waveCount; k++) {
                int thisSpawn = (int) Math.rint(waveWeights[k] * mobCount);
                if (totalSpawned + thisSpawn > mobCount) {
                    thisSpawn -= (totalSpawned + thisSpawn) - mobCount;
                    waveCounts[k] = thisSpawn;
                    totalSpawned = mobCount;
                    break;
                }

                waveCounts[k] = thisSpawn;
                totalSpawned += thisSpawn;
            }

            if (totalSpawned == mobCount) {
                balanceAllocations(waveCounts, waveWeights);
                continue;
            }

            //not all necessary spawns were allocated
            int unspawnedMobs = mobCount - totalSpawned;

            //assign to the highest weight that's got the fewest number of mobs
            while (unspawnedMobs != 0) {
                int highestWeightIndex = -1;
                double highestWeight = Double.NEGATIVE_INFINITY;
                for (int k = 0; k < waveCount; k++) {
                    double preference = waveWeights[k] / waveCounts[k];
                    if (preference > highestWeight) {
                        highestWeightIndex = k;
                        highestWeight = preference;
                    }
                }

                //should never happen, but don't break stuff if it does!
                if (highestWeightIndex == -1) {
                    break;
                }

                waveCounts[highestWeightIndex] += 1;
                unspawnedMobs--;
            }

            balanceAllocations(waveCounts, waveWeights);
        }

        for (int j = 0; j < waveCount; j++) {
            double baseDelay = roundTheme.baseWaveDelayTicks().scale(endlessRound, data.waveDelayBase);
            int actualDelay = MathUtils.clamp((int) Math.rint(roundTheme.offsetWaveDelayTicks()
                .scale(j + 1, baseDelay)), ABSOLUTE_WAVE_DELAY_MINIMUM, ABSOLUTE_WAVE_DELAY_MAXIMUM);

            List<Action<List<Mob>>> spawnActions = roundTheme.spawnActions(j + 1);

            List<SpawnInfo> spawns = new ArrayList<>(mergedCounts.length);
            for (int k = 0; k < mergedCounts.length; k++) {
                int count = allocatedMobs[k][j];
                if (count == 0) {
                    continue;
                }

                WeightedMob mob = k < themeMobs.size() ? themeMobs.get(k) : introducedMobs.get(k - themeMobs.size());
                spawns.add(new SpawnInfo(mob.key, mob.spawnType, count));
            }

            waves.add(new Wave(actualDelay, spawnActions, spawns));
        }

        return new Round(roundIndex + 1, waves, roundTheme.startActions(), roundTheme.endActions(), this.zombiesScene);
    }

    private static void balanceAllocations(int[] waveCounts, double[] waveWeights) {
        for (int i = 0; i < waveCounts.length; i++) {
            int thisCount = waveCounts[i];
            double thisWeight = waveWeights[i];

            for (int k = i + 1; k < waveCounts.length; k++) {
                int thatCount = waveCounts[k];
                double thatWeight = waveWeights[k];

                //this has more mobs, but a smaller weight!
                //this is just an off-by-one due to rounding
                if (thisWeight < thatWeight && thisCount > thatCount) {
                    waveCounts[i]--;
                    waveCounts[k]++;
                } else if (thisWeight > thatWeight && thisCount < thatCount) {
                    waveCounts[k]--;
                    waveCounts[i]++;
                }
            }
        }
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

        return introducedMobs == null ? List.of() : introducedMobs;
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

    @DataObject
    @Default("""
        {
          waveBase=3,
          waveDelayBase=0,
          waveWeightBase=0
        }
        """)
    public record Data(@NotNull ScalingValue healthScaling,
        @NotNull ScalingValue damageScaling,
        @NotNull ScalingValue speedScaling,
        @NotNull ScalingValue spawnAmountScaling,
        double spawnAmountBase,
        @NotNull ScalingValue waveScaling,
        double waveBase,
        double waveDelayBase,
        double waveWeightBase,
        @NotNull List<Introduction> introductions,
        @NotNull @ChildPath("themes") List<String> themes) {

    }
}