package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.CancellableState;
import org.phantazm.commons.TickableTask;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.damage.ZombiesDamageType;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;
import java.util.function.Supplier;

@Model("zombies.map.round.action.select_bombed")
@Cache(false)
public class SelectBombedRoom implements Action<Round> {
    private static final Potion NAUSEA = new Potion(PotionEffect.NAUSEA, (byte)3, 360000);

    private final Data data;
    private final Supplier<? extends MapObjects> supplier;
    private final Random random;
    private final Instance instance;
    private final ParticleWrapper particle;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
    private final UUID stateId;

    private final List<TargetedAttribute> modifiers;

    @FactoryMethod
    public SelectBombedRoom(@NotNull Data data, @NotNull Supplier<? extends MapObjects> supplier,
            @NotNull Random random, @NotNull Instance instance, @NotNull @Child("particle") ParticleWrapper particle,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
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
        this.stateId = UUID.randomUUID();
    }

    @Override
    public void perform(@NotNull Round round) {
        MapObjects objects = supplier.get();
        List<Room> candidateRooms = objects.roomTracker().items().stream()
                .filter(room -> room.isOpen() && !room.flags().hasFlag(Flags.BOMBED_ROOM) &&
                        !data.exemptRooms.contains(room.getRoomInfo().id())).toList();
        if (candidateRooms.size() == 0) {
            return;
        }

        Room room = candidateRooms.get(random.nextInt(candidateRooms.size()));
        String serialized = MiniMessage.miniMessage().serialize(room.getRoomInfo().displayName());
        Component warningMessage =
                MiniMessage.miniMessage().deserialize(String.format(data.warningFormatMessage, serialized));

        instance.sendMessage(warningMessage);

        int startRoundIndex = objects.module().roundHandlerSupplier().get().currentRoundIndex();
        objects.taskScheduler().scheduleTaskAfter(new TickableTask() {
            private boolean flagSet;
            private int ticks;
            private boolean finished;

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public void end() {
                for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                    zombiesPlayer.removeCancellable(stateId);
                }
            }

            @Override
            public void tick(long time) {
                if (!flagSet) {
                    room.flags().setFlag(Flags.BOMBED_ROOM);
                    flagSet = true;
                }

                RoundHandler roundHandler = objects.module().roundHandlerSupplier().get();
                int roundsElapsed = roundHandler.currentRoundIndex() - startRoundIndex;
                if (roundsElapsed >= data.duration) {
                    room.flags().clearFlag(Flags.BOMBED_ROOM);
                    flagSet = false;
                    finished = true;

                    for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                        zombiesPlayer.getPlayer().ifPresent(player -> {
                            if (!zombiesPlayer.canDoGenericActions()) {
                                zombiesPlayer.removeCancellable(stateId);
                            }
                            else {
                                Optional<Room> roomOptional = objects.roomTracker().atPoint(player.getPosition());
                                if (roomOptional.isPresent()) {
                                    Room currentRoom = roomOptional.get();
                                    if (!currentRoom.flags().hasFlag(Flags.BOMBED_ROOM)) {
                                        zombiesPlayer.removeCancellable(stateId);
                                    }
                                }
                                else {
                                    zombiesPlayer.removeCancellable(stateId);
                                }
                            }
                        });
                    }

                    return;
                }

                Vec3D randomPoint = randomPoint(objects.mapOrigin(), room);
                particle.sendTo(instance, randomPoint.x(), randomPoint.y(), randomPoint.z());

                if (++ticks % 4 == 0) {
                    for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
                        if (!zombiesPlayer.canDoGenericActions() || zombiesPlayer.flags().hasFlag(Flags.GODMODE)) {
                            continue;
                        }

                        zombiesPlayer.getPlayer().ifPresent(player -> {
                            Optional<Room> roomOptional = objects.roomTracker().atPoint(player.getPosition());
                            if (roomOptional.isPresent()) {
                                Room currentRoom = roomOptional.get();
                                if (currentRoom == room) {
                                    applyModifiers(zombiesPlayer);

                                    if (ticks % 40 == 0) {
                                        player.sendMessage(data.inAreaMessage);
                                    }

                                    player.damage(ZombiesDamageType.BOMBING, data.damage, true);
                                }
                                else if (!currentRoom.flags().hasFlag(Flags.BOMBED_ROOM)) {
                                    zombiesPlayer.removeCancellable(stateId);
                                }
                            }
                            else {
                                zombiesPlayer.removeCancellable(stateId);
                            }
                        });
                    }
                }
            }
        }, (long)data.gracePeriod * MinecraftServer.TICK_MS);
    }

    private void applyModifiers(ZombiesPlayer player) {
        player.registerCancellable(CancellableState.named(stateId, () -> {
            player.getPlayer().ifPresent(actualPlayer -> {
                for (TargetedAttribute modifier : modifiers) {
                    actualPlayer.getAttribute(
                                    Objects.requireNonNullElse(Attribute.fromKey(modifier.attribute), Attributes.NIL))
                            .addModifier(modifier.modifier);
                }

                for (TimedPotion potion : actualPlayer.getActiveEffects()) {
                    if (potion.getPotion() == NAUSEA) {
                        return;
                    }
                }

                actualPlayer.addEffect(NAUSEA);
            });
        }, () -> {
            player.getPlayer().ifPresent(actualPlayer -> {
                for (TargetedAttribute modifier : modifiers) {
                    actualPlayer.getAttribute(
                                    Objects.requireNonNullElse(Attribute.fromKey(modifier.attribute), Attributes.NIL))
                            .removeModifier(modifier.modifier.getId());
                }

                actualPlayer.removeEffect(PotionEffect.NAUSEA);
            });
        }), false);
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

    public record Modifier(@NotNull String attribute, float amount, @NotNull AttributeOperation attributeOperation) {
    }

    private record TargetedAttribute(@NotNull String attribute, @NotNull AttributeModifier modifier) {
    }

    @DataObject
    public record Data(@NotNull String warningFormatMessage,
                       @NotNull Component inAreaMessage,
                       float damage,
                       int gracePeriod,
                       int duration,
                       @NotNull List<Key> exemptRooms,
                       @NotNull List<Modifier> modifiers,
                       @NotNull @ChildPath("particle") String particle) {
    }
}
