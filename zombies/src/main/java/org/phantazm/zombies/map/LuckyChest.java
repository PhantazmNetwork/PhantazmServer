package org.phantazm.zombies.map;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.equipment.EquipmentHandler;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.core.sound.SongPlayer;
import org.phantazm.core.tracker.BoundedBase;
import org.phantazm.zombies.map.luckychest.AnimationFrames;
import org.phantazm.zombies.map.luckychest.AnimationTimings;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LuckyChest extends BoundedBase implements Tickable {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuckyChest.class);

    private final Instance instance;
    private final LuckyChestInfo info;
    private final SongPlayer songPlayer;
    private final SongLoader songLoader;
    private final List<Pair<Key, Key>> chestEquipmentEntries;
    private final Random random;
    private final AnimationTimings animationTimings;
    private final AnimationFrames animationFrames;

    private boolean enabled;

    private Hologram hologram;
    private Block block;

    private SongPlayer.Song rollSong;
    private ZombiesPlayer roller;

    public LuckyChest(@NotNull Instance instance, @NotNull SongPlayer songPlayer, @NotNull SongLoader songLoader,
            @NotNull Point mapOrigin, @NotNull LuckyChestInfo info, @NotNull List<Pair<Key, Key>> chestEquipmentEntries,
            @NotNull Random random, @NotNull AnimationTimings animationTimings,
            @NotNull AnimationFrames animationFrames) {
        super(mapOrigin.add(VecUtils.toPoint(info.location())));
        this.instance = Objects.requireNonNull(instance, "instance");
        this.info = info;
        this.songPlayer = Objects.requireNonNull(songPlayer, "songPlayer");
        this.songLoader = Objects.requireNonNull(songLoader, "songLoader");
        this.chestEquipmentEntries = List.copyOf(chestEquipmentEntries);
        this.random = Objects.requireNonNull(random, "random");
        this.animationTimings = Objects.requireNonNull(animationTimings, "animationTimings");
        this.animationFrames = Objects.requireNonNull(animationFrames, "animationFrames");
    }

    public void initialize() {
        Hologram hologram = new InstanceHologram(super.center.add(0, 1.5, 0), 0, Hologram.Alignment.UPPER);
        hologram.setInstance(instance);

        this.hologram = hologram;
    }

    private Optional<Pair<Chunk, Block>> getBlock() {
        return Optional.ofNullable(instance.getChunkAt(super.center)).map(chunk -> {
            return Pair.of(chunk, chunk.getBlock(super.center));
        });
    }

    public void roll(@NotNull ZombiesPlayer roller) {
        if (this.rollSong != null) {
            return;
        }

        animationTimings.start(System.currentTimeMillis());
        this.rollSong = songPlayer.play(this.roller = roller, Sound.Emitter.self(), songLoader.getNotes(info.song()));

        getBlock().ifPresent(pair -> {
            Chunk chunk = pair.first();
            Block block = pair.second();

            chunk.sendPacketToViewers(new BlockActionPacket(super.center, (byte)1, (byte)1, block));
        });
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void tick(long time) {
        SongPlayer.Song rollSong = this.rollSong;
        if (rollSong == null) {
            return;
        }

        if (rollSong.isFinished()) {
            finishRoll(time);

            this.roller = null;
            this.rollSong = null;
            return;
        }

        if (animationTimings.shouldAdvance(time)) {
            ItemStack frame = animationFrames.next();
            this.hologram.set(0, frame.getDisplayName());
        }
    }

    private void finishRoll(long time) {
        if (chestEquipmentEntries.isEmpty()) {
            return;
        }

        EquipmentHandler handler = roller.module().getEquipmentHandler();
        Pair<Key, Key> selection = chestEquipmentEntries.get(random.nextInt(chestEquipmentEntries.size()));

        Key group = selection.first();
        Key equipment = selection.second();

        Collection<Equipment> inGroup = handler.getEquipment(group);
        for (Equipment existingEquipment : inGroup) {
            if (equipment.equals(existingEquipment.key())) {
                handleMatching(equipment, handler, time);
                return;
            }
        }
    }

    private void handleMatching(Key equipment, EquipmentHandler handler, long time) {

    }
}
