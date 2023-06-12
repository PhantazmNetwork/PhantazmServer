package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.particle.ParticleWrapper;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@Model("zombies.map.round.action.select_bombed")
@Cache(false)
public class SelectBombedRoom implements Action<Round> {
    private final Data data;
    private final Supplier<? extends MapObjects> supplier;
    private final Random random;
    private final Instance instance;
    private final ParticleWrapper particle;

    private Task tickTask;
    private long startTime;

    @FactoryMethod
    public SelectBombedRoom(@NotNull Data data, @NotNull Supplier<? extends MapObjects> supplier,
            @NotNull Random random, @NotNull Instance instance, @NotNull @Child("particle") ParticleWrapper particle) {
        this.data = data;
        this.supplier = supplier;
        this.random = random;
        this.instance = instance;
        this.particle = particle;
    }

    @Override
    public void perform(@NotNull Round round) {
        MapObjects objects = supplier.get();
        List<Room> openRooms = objects.roomTracker().items().stream().filter(Room::isOpen).toList();
        if (openRooms.size() == 0) {
            return;
        }

        Room chosenRoom = openRooms.get(random.nextInt(openRooms.size()));
        String serialized = MiniMessage.miniMessage().serialize(chosenRoom.getRoomInfo().displayName());
        Component warningMessage =
                MiniMessage.miniMessage().deserialize(String.format(data.warningFormatMessage, serialized));

        instance.sendMessage(warningMessage);

        this.startTime = System.currentTimeMillis();
        this.tickTask = MinecraftServer.getSchedulerManager()
                .scheduleTask(() -> tickBombedRoom(objects.mapOrigin(), chosenRoom, System.currentTimeMillis()),
                        TaskSchedule.immediate(), TaskSchedule.tick(1), ExecutionType.SYNC);
    }

    private void tickBombedRoom(Point origin, Room room, long time) {
        long elapsed = (time - startTime) / MinecraftServer.TICK_MS;
        if (elapsed > data.duration) {
            tickTask.cancel();
            tickTask = null;
            return;
        }

        Vec3D randomPoint = randomPoint(origin, room);
        ParticleWrapper.Data wrapperData = particle.data();
        ServerPacket packet =
                ParticleCreator.createParticlePacket(wrapperData.particle(), wrapperData.distance(), randomPoint.x(),
                        randomPoint.y(), randomPoint.z(), wrapperData.offsetX(), wrapperData.offsetY(),
                        wrapperData.offsetZ(), wrapperData.data(), wrapperData.particleCount(),
                        particle.variantData()::write);

        instance.sendGroupedPacket(packet);
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

    @DataObject
    public record Data(@NotNull String warningFormatMessage,
                       int gracePeriod,
                       int duration,
                       @NotNull @ChildPath("particle") String particle) {
    }
}
