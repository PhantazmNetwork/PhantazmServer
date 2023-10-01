package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.mapper.annotation.Default;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.state.CancellableState;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.VecUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.tick.TickableTask;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.Stages;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Model("zombies.map.round.action.select_bombed")
@Cache(false)
public class SelectBombedRoom implements Action<Round> {
    private static final Potion NAUSEA = new Potion(PotionEffect.NAUSEA, (byte) 3, 360000);

    private final Data data;
    private final Supplier<? extends MapObjects> supplier;
    private final Random random;
    private final Instance instance;
    private final ParticleWrapper particle;
    private final Map<PlayerView, ZombiesPlayer> playerMap;

    private final List<TargetedAttribute> modifiers;

    @FactoryMethod
    public SelectBombedRoom(@NotNull Data data, @NotNull Supplier<? extends MapObjects> supplier,
        @NotNull Random random, @NotNull Instance instance, @NotNull @Child("particle") ParticleWrapper particle,
        @NotNull Map<PlayerView, ZombiesPlayer> playerMap) {
        this.data = data;
        this.supplier = supplier;
        this.random = random;
        this.instance = instance;
        this.particle = particle;
        this.playerMap = playerMap;

        List<TargetedAttribute> modifiers = new ArrayList<>(data.modifiers.size());
        for (Modifier modifier : data.modifiers) {
            UUID uuid = UUID.randomUUID();
            modifiers.add(new TargetedAttribute(modifier.attribute,
                new AttributeModifier(uuid, uuid.toString(), modifier.amount, modifier.attributeOperation)));
        }

        this.modifiers = List.copyOf(modifiers);
    }

    private @Nullable Room targetRoom(MapObjects mapObjects) {
        if (data.specificRoom != null) {
            for (Room room : mapObjects.roomTracker().items()) {
                if (room.getRoomInfo().id().equals(data.specificRoom) && !room.flags().hasFlag(Flags.BOMBED_ROOM)) {
                    return room;
                }
            }

            return null;
        }

        List<Room> candidateRooms = mapObjects.roomTracker().items().stream()
            .filter(room -> room.isOpen() && !room.flags().hasFlag(Flags.BOMBED_ROOM) &&
                !data.exemptRooms.contains(room.getRoomInfo().id())).toList();
        if (candidateRooms.isEmpty()) {
            return null;
        }

        return candidateRooms.get(random.nextInt(candidateRooms.size()));
    }

    @Override
    public void perform(@NotNull Round round) {
        MapObjects objects = supplier.get();
        Room room = targetRoom(objects);

        if (room == null) {
            return;
        }

        TagResolver roomPlaceholder = Placeholder.component("room", room.getRoomInfo().displayName());
        if (data.warningFormatMessage != null) {
            instance.sendMessage(MiniMessage.miniMessage().deserialize(data.warningFormatMessage, roomPlaceholder));
        }

        int startRoundIndex = objects.module().roundHandlerSupplier().get().currentRoundIndex();

        Damage bombDamage = new Damage(DamageType.GENERIC, null, null, null, data.damage);
        bombDamage.tagHandler().setTag(Tags.DAMAGE_NAME, data.bombingDamageName);

        objects.taskScheduler().scheduleTaskAfter(new TickableTask() {
            private final List<CancellableState<Entity>> states = new ArrayList<>();

            private boolean flagSet;
            private int ticks;
            private boolean finished;

            private void removeStateFor(ZombiesPlayer zombiesPlayer, boolean clearBombTag) {
                Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                if (playerOptional.isEmpty()) {
                    return;
                }

                Player player = playerOptional.get();
                Iterator<CancellableState<Entity>> iterator = states.iterator();
                while (iterator.hasNext()) {
                    CancellableState<Entity> state = iterator.next();
                    if (state.self() == player) {
                        player.stateHolder().removeState(Stages.ZOMBIES_GAME, state);
                        iterator.remove();
                    }
                }

                if (clearBombTag) {
                    player.removeTag(Tags.LAST_ENTER_BOMBED_ROOM);
                }
            }

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public void end() {
                for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                    removeStateFor(zombiesPlayer, true);
                }

                states.clear();
            }

            @Override
            public void tick(long time) {
                if (!flagSet) {
                    room.flags().setFlag(Flags.BOMBED_ROOM);
                    flagSet = true;
                }

                RoundHandler roundHandler = objects.module().roundHandlerSupplier().get();
                int currentRound = roundHandler.currentRoundIndex();
                if (currentRound < startRoundIndex || currentRound - startRoundIndex >= data.duration) {
                    if (data.bombingCompleteFormat != null) {
                        instance.sendMessage(MiniMessage.miniMessage().deserialize(data.bombingCompleteFormat,
                            roomPlaceholder));
                    }

                    room.flags().clearFlag(Flags.BOMBED_ROOM);
                    flagSet = false;
                    finished = true;

                    for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                        if (playerOptional.isEmpty()) {
                            continue;
                        }

                        Player player = playerOptional.get();
                        if (!zombiesPlayer.canDoGenericActions()) {
                            removeStateFor(zombiesPlayer, true);
                        } else {
                            Optional<Room> roomOptional = objects.roomTracker().atPoint(player.getPosition());
                            if (roomOptional.isPresent()) {
                                Room currentRoom = roomOptional.get();
                                if (!currentRoom.flags().hasFlag(Flags.BOMBED_ROOM)) {
                                    removeStateFor(zombiesPlayer, true);
                                }
                            } else {
                                removeStateFor(zombiesPlayer, true);
                            }
                        }
                    }

                    return;
                }

                Vec3D randomPoint = randomPoint(objects.mapOrigin(), room);
                particle.sendTo(instance, randomPoint.x(), randomPoint.y(), randomPoint.z());

                if (data.sound != null && (data.soundInterval == 0 || ticks % data.soundInterval == 0)) {
                    long seed = ThreadLocalRandom.current().nextLong();
                    instance.playSound(Sound.sound(data.sound).seed(seed).build(), VecUtils.toPoint(randomPoint));
                }

                ++ticks;
                if (data.damageInterval != 0 && ticks % data.damageInterval != 0) {
                    return;
                }

                for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                    if (!zombiesPlayer.canDoGenericActions() || zombiesPlayer.flags().hasFlag(Flags.GODMODE)) {
                        removeStateFor(zombiesPlayer, false);
                        continue;
                    }

                    Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                    if (playerOptional.isEmpty()) {
                        continue;
                    }

                    Player player = playerOptional.get();
                    Optional<Room> roomOptional = objects.roomTracker().atPoint(player.getPosition());
                    if (roomOptional.isEmpty()) {
                        removeStateFor(zombiesPlayer, true);
                        continue;
                    }

                    Room currentRoom = roomOptional.get();
                    if (currentRoom == room) {
                        if (data.damageInterval == 0 || data.warningMessageHits == 0 ||
                            ticks % (data.damageInterval * data.warningMessageHits) == 0) {
                            player.sendMessage(data.inAreaMessage);
                        }

                        long lastEnterBombedRoom = player.getTag(Tags.LAST_ENTER_BOMBED_ROOM);
                        if (lastEnterBombedRoom == -1) {
                            player.setTag(Tags.LAST_ENTER_BOMBED_ROOM, lastEnterBombedRoom =
                                MinecraftServer.currentTick());
                        }

                        long ticksSinceEnter = MinecraftServer.currentTick() - lastEnterBombedRoom;
                        if (ticksSinceEnter >= data.damageDelay) {
                            player.damage(bombDamage, true);
                        }

                        if (ticksSinceEnter >= data.effectDelay) {
                            applyModifiers(zombiesPlayer);
                        }
                    } else if (!currentRoom.flags().hasFlag(Flags.BOMBED_ROOM)) {
                        removeStateFor(zombiesPlayer, true);
                    }
                }
            }

            private void applyModifiers(ZombiesPlayer zombiesPlayer) {
                Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                if (playerOptional.isEmpty()) {
                    return;
                }

                Player actualPlayer = playerOptional.get();
                CancellableState<Entity> state = CancellableState.state(actualPlayer,
                    entity -> {
                        for (TargetedAttribute modifier : modifiers) {
                            ((Player) entity).getAttribute(Objects.requireNonNullElse(Attribute.fromKey(modifier.attribute),
                                Attributes.NIL)).addModifier(modifier.modifier);
                        }

                        for (TimedPotion potion : entity.getActiveEffects()) {
                            if (potion.getPotion() == NAUSEA) {
                                return;
                            }
                        }

                        entity.addEffect(NAUSEA);
                    }, entity -> {
                        Player player = (Player) entity;
                        for (TargetedAttribute modifier : modifiers) {
                            player.getAttribute(
                                    Objects.requireNonNullElse(Attribute.fromKey(modifier.attribute), Attributes.NIL))
                                .removeModifier(modifier.modifier.getId());
                        }

                        player.removeEffect(PotionEffect.NAUSEA);
                    });

                states.add(state);
                actualPlayer.stateHolder().registerState(Stages.ZOMBIES_GAME, state);
            }
        }, data.gracePeriod);
    }

    private Vec3D randomPoint(Point origin, Room room) {
        List<Bounds3I> bounds = room.getRoomInfo().regions();
        int sum = 0;
        for (Bounds3I bound : bounds) {
            sum += bound.volume();
        }

        int target = random.nextInt(sum);
        sum = 0;
        Bounds3I selection = null;
        for (Bounds3I bound : bounds) {
            sum += bound.volume();
            if (sum >= target) {
                selection = bound;
                break;
            }
        }

        assert selection != null;

        double xOffset = selection.originX() + random.nextDouble(selection.lengthX());
        double yOffset = selection.originY() + random.nextDouble(selection.lengthY());
        double zOffset = selection.originZ() + random.nextDouble(selection.lengthZ());

        return Vec3D.immutable(xOffset + origin.x(), yOffset + origin.y(), zOffset + origin.z());
    }

    public record Modifier(@NotNull String attribute,
        float amount,
        @NotNull AttributeOperation attributeOperation) {
    }

    private record TargetedAttribute(@NotNull String attribute,
        @NotNull AttributeModifier modifier) {
    }

    @DataObject
    public record Data(
        @Nullable String warningFormatMessage,
        @Nullable String bombingCompleteFormat,
        @NotNull Component inAreaMessage,
        @NotNull Component bombingDamageName,
        @Nullable Sound sound,
        int soundInterval,
        float damage,
        int gracePeriod,
        int warningMessageHits,
        long damageInterval,
        long damageDelay,
        long effectDelay,
        int duration,
        @Nullable Key specificRoom,
        @NotNull List<Key> exemptRooms,
        @NotNull List<Modifier> modifiers,
        @NotNull @ChildPath("particle") String particle) {
        @Default("warningFormatMessage")
        public static @NotNull ConfigElement defaultWarningFormatMessage() {
            return ConfigPrimitive.NULL;
        }

        @Default("warningMessageHits")
        public static @NotNull ConfigElement defaultWarningMessageHits() {
            return ConfigPrimitive.of(10);
        }

        @Default("bombingCompleteFormat")
        public static @NotNull ConfigElement defaultBombingCompleteFormat() {
            return ConfigPrimitive.NULL;
        }

        @Default("damageInterval")
        public static @NotNull ConfigElement defaultDamageInterval() {
            return ConfigPrimitive.of(4L);
        }

        @Default("bombingDamageName")
        public static @NotNull ConfigElement defaultBombingDamageName() {
            return ConfigPrimitive.of("<red>Bombing");
        }

        @Default("sound")
        public static @NotNull ConfigElement defaultSound() {
            return ConfigPrimitive.NULL;
        }

        @Default("soundInterval")
        public static @NotNull ConfigElement defaultSoundInterval() {
            return ConfigPrimitive.of(3);
        }

        @Default("specificRoom")
        public static @NotNull ConfigElement defaultSpecificRoom() {
            return ConfigPrimitive.NULL;
        }

        @Default("damageDelay")
        public static @NotNull ConfigElement defaultDamageDelay() {
            return ConfigPrimitive.of(100L);
        }

        @Default("effectDelay")
        public static @NotNull ConfigElement defaultEffectDelay() {
            return ConfigPrimitive.of(50L);
        }

        @Default("exemptRooms")
        public static @NotNull ConfigElement defaultExemptRooms() {
            return ConfigList.of();
        }
    }
}