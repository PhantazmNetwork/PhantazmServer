package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.config.ComplexData;
import com.github.phantazmnetwork.commons.factory.DependencyProvider;
import com.github.phantazmnetwork.commons.factory.Factory;
import com.github.phantazmnetwork.commons.factory.FactoryDependencyProvider;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.zombies.equipment.gun.data.GunLevelData;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.*;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.StateReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.GradientActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.ReloadActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar.StaticActionBarChooser;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.ShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.StateShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.*;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.HitScanFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.ProjectileFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.SpreadFirer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.*;
import com.github.phantazmnetwork.zombies.equipment.gun.target.BasicTargetFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.TargetFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.AroundEndFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.BetweenPointsFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.NearbyPhantazmMobFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional.PositionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.EyeHeightHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.HeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.StaticHeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.RayTraceTargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.StaticTargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.TargetTester;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ClipStackMapper;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.ReloadStackMapper;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Scratch {

    public static @NotNull GunLevel createGunLevel(@NotNull MobStore mobStore, @NotNull PlayerView playerView,
                                                   @NotNull Random random, @NotNull ComplexData complexData) {
        Factory<GunStats, GunStats> gunStats = (provider, data) -> data; // this is a little weird
        Factory<GunLevelData, GunLevel> gunLevel = (provider, data) -> {
            GunStats stats = provider.getDependency(data.stats());
            ShootTester shootTester = provider.getDependency(data.shootTester());
            ReloadTester reloadTester = provider.getDependency(data.reloadTester());
            Firer firer = provider.getDependency(data.firer());
            Collection<GunEffect> shootEffects = provider.getDependency(data.shootEffects());
            Collection<GunEffect> reloadEffects = provider.getDependency(data.reloadEffects());
            Collection<GunEffect> tickEffects = provider.getDependency(data.tickEffects());
            Collection<GunEffect> emptyClipEffects = provider.getDependency(data.emptyClipEffects());
            Collection<GunStackMapper> gunStackMappers = provider.getDependency(data.gunStackMappers());

            return new GunLevel(data.stack(), stats, shootTester, reloadTester, firer, shootEffects,
                    reloadEffects, tickEffects, emptyClipEffects, gunStackMappers);
        };
        Factory<AmmoLevelEffect.Data, AmmoLevelEffect> ammoLevelEffect
                = (provider, data) -> new AmmoLevelEffect(data, playerView);
        Factory<PlaySoundEffect.Data, PlaySoundEffect> playSoundEffect
                = (provider, data) -> new PlaySoundEffect(data, playerView);
        Factory<ReloadActionBarEffect.Data, ReloadActionBarEffect> reloadActionBarEffect = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            ReloadTester reloadTester = provider.getDependency(data.reloadTesterKey());
            ReloadActionBarChooser chooser = provider.getDependency(data.reloadActionBarChooserKey());
            return new ReloadActionBarEffect(data, playerView, stats, reloadTester, chooser);
        };
        Factory<SendMessageEffect.Data, SendMessageEffect> sendMessageEffect
                = (provider, data) -> new SendMessageEffect(data, playerView);
        Factory<ShootExpEffect.Data, ShootExpEffect> shootExpEffect = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            return new ShootExpEffect(data, playerView, stats);
        };
        Factory<GradientActionBarChooser.Data, GradientActionBarChooser> gradientActionBarChooser
                = (provider, data) -> new GradientActionBarChooser(data);
        Factory<StaticActionBarChooser.Data, StaticActionBarChooser> staticActionBarChooser
                = (provider, data) -> new StaticActionBarChooser(data);
        Factory<StateReloadTester.Data, StateReloadTester> stateReloadTester = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            return new StateReloadTester(data, stats);
        };
        Factory<BasicShotEndpointSelector.Data, BasicShotEndpointSelector> basicShotEndpointSelector = (provider, data) -> {
            BlockIteration blockIteration = provider.getDependency(data.blockIterationKey());
            return new BasicShotEndpointSelector(data, playerView, blockIteration);
        };
        Factory<RayTraceBlockIteration.Data, RayTraceBlockIteration> rayTraceBlockIteration
                = (provider, data) -> new RayTraceBlockIteration(data);
        Factory<WallshotBlockIteration.Data, WallshotBlockIteration> wallshotBlockIteration
                = (provider, data) -> new WallshotBlockIteration(data);
        Factory<HitScanFirer.Data, HitScanFirer> hitScanFirer = (provider, data) -> {
            ShotEndpointSelector endSelector = provider.getDependency(data.endSelectorKey());
            TargetFinder targetFinder = provider.getDependency(data.targetFinderKey());
            Collection<ShotHandler> shotHandlers = provider.getDependency(data.shotHandlerKeys());

            return new HitScanFirer(data, playerView, endSelector, targetFinder, shotHandlers);
        };
        Factory<ProjectileFirer.Data, ProjectileFirer> projectileFirer = (provider, data) -> {
            ShotEndpointSelector endSelector = provider.getDependency(data.endSelectorKey());
            TargetFinder targetFinder = provider.getDependency(data.targetFinderKey());
            Collection<ShotHandler> shotHandlers = provider.getDependency(data.shotHandlerKeys());

            return new ProjectileFirer(data, playerView, endSelector, targetFinder, shotHandlers);
        };
        Factory<SpreadFirer.Data, SpreadFirer> spreadFirer = (provider, data) -> {
            Collection<Firer> subFirers = provider.getDependency(data.subFirerKeys());
            return new SpreadFirer(data, random, subFirers);
        };
        Factory<ChainShotHandler.Data, ChainShotHandler> chainShotHandler = (provider, data) -> {
            PositionalEntityFinder finder = provider.getDependency(data.finderKey());
            Firer firer = provider.getDependency(data.firerKey());

            return new ChainShotHandler(data, finder, firer);
        };
        Factory<DamageShotHandler.Data, DamageShotHandler> damageShotHandler
                = (provider, data) -> new DamageShotHandler(data);
        Factory<ExplosionShotHandler.Data, ExplosionShotHandler> explosionShotHandler
                = (provider, data) -> new ExplosionShotHandler(data);
        Factory<FeedbackShotHandler.Data, FeedbackShotHandler> feedbackShotHandler
                = (provider, data) -> new FeedbackShotHandler(data);
        Factory<GuardianBeamShotHandler.Data, GuardianBeamShotHandler> guardianBeamShotHandler
                = (provider, data) -> new GuardianBeamShotHandler(data);
        Factory<IgniteShotHandler.Data, IgniteShotHandler> igniteShotHandler
                = (provider, data) -> new IgniteShotHandler(data);
        Factory<KnockbackShotHandler.Data, KnockbackShotHandler> knockbackShotHandler
                = (provider, data) -> new KnockbackShotHandler(data);
        Factory<ParticleTrailShotHandler.Data, ParticleTrailShotHandler> particleTrailShotHandler
                = (provider, data) -> new ParticleTrailShotHandler(data);
        Factory<PotionShotHandler.Data, PotionShotHandler> potionShotHandler
                = (provider, data) -> new PotionShotHandler(data);
        Factory<SoundShotHandler.Data, SoundShotHandler> soundShotHandler
                = (provider, data) -> new SoundShotHandler(data);
        Factory<StateShootTester.Data, StateShootTester> stateShootTester = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            ReloadTester reloadTester = provider.getDependency(data.reloadTesterKey());
            return new StateShootTester(data, stats, reloadTester);
        };
        Factory<AroundEndFinder.Data, AroundEndFinder> aroundEndFinder
                = (provider, data) -> new AroundEndFinder(data);
        Factory<BetweenPointsFinder.Data, BetweenPointsFinder> betweenPointsFinder
                = (provider, data) -> new BetweenPointsFinder(data);
        Factory<NearbyPhantazmMobFinder.Data, NearbyPhantazmMobFinder> nearbyPhantazmMobFinder
                = (provider, data) -> new NearbyPhantazmMobFinder(data, mobStore);
        Factory<EyeHeightHeadshotTester.Data, EyeHeightHeadshotTester> eyeHeightHeadshotTester
                = (provider, data) -> new EyeHeightHeadshotTester(data);
        Factory<StaticHeadshotTester.Data, StaticHeadshotTester> staticHeadshotTester
                = (provider, data) -> new StaticHeadshotTester(data);
        Factory<RayTraceTargetTester.Data, RayTraceTargetTester> rayTraceTargetTester
                = (provider, data) -> new RayTraceTargetTester(data);
        Factory<StaticTargetTester.Data, StaticTargetTester> staticTargetTester
                = (provider, data) -> new StaticTargetTester(data);
        Factory<BasicTargetFinder.Data, BasicTargetFinder> basicTargetFinder = (provider, data) -> {
            DirectionalEntityFinder finder = provider.getDependency(data.finderKey());
            TargetTester targetTester = provider.getDependency(data.targetTesterKey());
            HeadshotTester headshotTester = provider.getDependency(data.headshotTesterKey());
            return new BasicTargetFinder(data, mobStore, finder, targetTester, headshotTester);
        };
        Factory<ClipStackMapper.Data, ClipStackMapper> clipStackMapper = (provider, data) -> {
            ReloadTester reloadTester = provider.getDependency(data.reloadTesterKey());
            return new ClipStackMapper(data, reloadTester);
        };
        Factory<ReloadStackMapper.Data, ReloadStackMapper> reloadStackMapper = (provider, data) -> {
            GunStats stats = provider.getDependency(data.statsKey());
            ReloadTester reloadTester = provider.getDependency(data.reloadTesterKey());
            return new ReloadStackMapper(data, stats, reloadTester);
        };
        Map<Key, Factory<?, ?>> factories = new HashMap<>(37);
        factories.put(GunStats.SERIAL_KEY, gunStats);
        factories.put(GunLevelData.SERIAL_KEY, gunLevel);
        factories.put(AmmoLevelEffect.Data.SERIAL_KEY, ammoLevelEffect);
        factories.put(PlaySoundEffect.Data.SERIAL_KEY, playSoundEffect);
        factories.put(ReloadActionBarEffect.Data.SERIAL_KEY, reloadActionBarEffect);
        factories.put(SendMessageEffect.Data.SERIAL_KEY, sendMessageEffect);
        factories.put(ShootExpEffect.Data.SERIAL_KEY, shootExpEffect);
        factories.put(GradientActionBarChooser.Data.SERIAL_KEY, gradientActionBarChooser);
        factories.put(StaticActionBarChooser.Data.SERIAL_KEY, staticActionBarChooser);
        factories.put(StateReloadTester.Data.SERIAL_KEY, stateReloadTester);
        factories.put(BasicShotEndpointSelector.Data.SERIAL_KEY, basicShotEndpointSelector);
        factories.put(RayTraceBlockIteration.Data.SERIAL_KEY, rayTraceBlockIteration);
        factories.put(WallshotBlockIteration.Data.SERIAL_KEY, wallshotBlockIteration);
        factories.put(HitScanFirer.Data.SERIAL_KEY, hitScanFirer);
        factories.put(ProjectileFirer.Data.SERIAL_KEY, projectileFirer);
        factories.put(SpreadFirer.Data.SERIAL_KEY, spreadFirer);
        factories.put(ChainShotHandler.Data.SERIAL_KEY, chainShotHandler);
        factories.put(DamageShotHandler.Data.SERIAL_KEY, damageShotHandler);
        factories.put(ExplosionShotHandler.Data.SERIAL_KEY, explosionShotHandler);
        factories.put(FeedbackShotHandler.Data.SERIAL_KEY, feedbackShotHandler);
        factories.put(GuardianBeamShotHandler.Data.SERIAL_KEY, guardianBeamShotHandler);
        factories.put(IgniteShotHandler.Data.SERIAL_KEY, igniteShotHandler);
        factories.put(KnockbackShotHandler.Data.SERIAL_KEY, knockbackShotHandler);
        factories.put(ParticleTrailShotHandler.Data.SERIAL_KEY, particleTrailShotHandler);
        factories.put(PotionShotHandler.Data.SERIAL_KEY, potionShotHandler);
        factories.put(SoundShotHandler.Data.SERIAL_KEY, soundShotHandler);
        factories.put(StateShootTester.Data.SERIAL_KEY, stateShootTester);
        factories.put(AroundEndFinder.Data.SERIAL_KEY, aroundEndFinder);
        factories.put(BetweenPointsFinder.Data.SERIAL_KEY, betweenPointsFinder);
        factories.put(NearbyPhantazmMobFinder.Data.SERIAL_KEY, nearbyPhantazmMobFinder);
        factories.put(EyeHeightHeadshotTester.Data.SERIAL_KEY, eyeHeightHeadshotTester);
        factories.put(StaticHeadshotTester.Data.SERIAL_KEY, staticHeadshotTester);
        factories.put(RayTraceTargetTester.Data.SERIAL_KEY, rayTraceTargetTester);
        factories.put(StaticTargetTester.Data.SERIAL_KEY, staticTargetTester);
        factories.put(BasicTargetFinder.Data.SERIAL_KEY, basicTargetFinder);
        factories.put(ClipStackMapper.Data.SERIAL_KEY, clipStackMapper);
        factories.put(ReloadStackMapper.Data.SERIAL_KEY, reloadStackMapper);

        DependencyProvider getter = new FactoryDependencyProvider(complexData.objects(), factories);
        return getter.getDependency(complexData.mainKey());
    }

}
