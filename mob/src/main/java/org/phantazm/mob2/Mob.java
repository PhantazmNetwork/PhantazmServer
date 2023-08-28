package org.phantazm.mob2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.mob2.skill.Skill;
import org.phantazm.proxima.bindings.minestom.Pathfinding;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class Mob extends ProximaEntity {
    private static final AtomicLong MOB_COUNTER = new AtomicLong();
    private static final String TEAM_PREFIX = "m-";

    private final List<Skill> allSkills;
    private final List<Skill> tickableSkills;
    private final List<Skill> useOnTick;
    private final Map<Trigger, List<Skill>> triggeredSkills;
    private final MobData data;

    private final String uniqueTeamName;

    private TeamSettings teamSettings;
    private final CachedPacket cachedCreateTeamPacket;
    private final CachedPacket cachedUpdateTeamPacket;
    private final CachedPacket cachedRemoveTeamPacket;

    private Reference<Entity> lastHitEntity;
    private Reference<Player> lastInteractingPlayer;

    private boolean useStateHolder;

    private record TeamSettings(Component displayName,
        byte friendlyFlags,
        TeamsPacket.NameTagVisibility nameTagVisibility,
        TeamsPacket.CollisionRule collisionRule,
        NamedTextColor teamColor,
        Component teamPrefix,
        Component teamSuffix) {
        private static final TeamSettings DEFAULT = new TeamSettings(Component.empty(), (byte) 0x0,
            TeamsPacket.NameTagVisibility.ALWAYS, TeamsPacket.CollisionRule.ALWAYS, NamedTextColor.WHITE,
            Component.empty(), Component.empty());

        private TeamSettings withCollisionRule(TeamsPacket.CollisionRule collisionRule) {
            return new TeamSettings(displayName, friendlyFlags, nameTagVisibility, collisionRule, teamColor, teamPrefix, teamSuffix);
        }

        private TeamSettings withTeamColor(NamedTextColor teamColor) {
            return new TeamSettings(displayName, friendlyFlags, nameTagVisibility, collisionRule, teamColor, teamPrefix, teamSuffix);
        }

        private TeamSettings withNameTagVisibility(TeamsPacket.NameTagVisibility visibility) {
            return new TeamSettings(displayName, friendlyFlags, visibility, collisionRule, teamColor, teamPrefix, teamSuffix);
        }
    }

    public Mob(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Pathfinding pathfinding,
        @NotNull MobData data) {
        super(entityType, uuid, pathfinding);
        this.allSkills = new ArrayList<>();
        this.tickableSkills = new ArrayList<>();
        this.useOnTick = new ArrayList<>();
        this.triggeredSkills = new EnumMap<>(Trigger.class);
        this.data = Objects.requireNonNull(data);

        this.lastHitEntity = new WeakReference<>(null);
        this.lastInteractingPlayer = new WeakReference<>(null);

        String name = TEAM_PREFIX + Long.toString(MOB_COUNTER.getAndIncrement(), 16);
        if (name.length() > 16) {
            name = name.substring(0, 16);
        }

        this.uniqueTeamName = name;

        this.teamSettings = TeamSettings.DEFAULT;
        this.cachedCreateTeamPacket = new CachedPacket(() -> createTeamPacket(this.teamSettings, false));
        this.cachedUpdateTeamPacket = new CachedPacket(() -> createTeamPacket(this.teamSettings, true));
        this.cachedRemoveTeamPacket = new CachedPacket(() -> new TeamsPacket(this.uniqueTeamName,
            new TeamsPacket.RemoveTeamAction()));
    }

    /**
     * Adds a skill to this mob. This will call its {@link Skill#init()}. Ensure that the skill is not assigned to any
     * other mob.
     * <p>
     * <b>Thread Behavior</b>: It is not safe to call this method by any thread other than the owning's entity's
     * current tick thread, unless proper synchronization is performed.
     * <p>
     * <b>Exception</b>: It is safe to add skills off of the tick thread when the mob has not yet been added to an
     * instance; however, it is never safe to call this method with two or more threads concurrently.
     *
     * @param skill the skill to add
     */
    public void addSkill(@NotNull Skill skill) {
        Objects.requireNonNull(skill);
        addSkill0(skill);
    }

    /**
     * Adds multiple skills to this mob. This will call {@link Skill#init()} for each skill in the collection. Ensure
     * that none of the skills are assigned to any other mobs.
     * <p>
     * <b>Thread Behavior</b>: It is not safe to call this method by any thread other than the owning's entity's
     * current tick thread, unless proper synchronization is performed.
     * <p>
     * <b>Exception</b>: It is safe to add skills off of the tick thread when the mob has not yet been added to an
     * instance; however, it is never safe to call this method with two or more threads concurrently.
     *
     * @param skills the skills to add
     */
    public void addSkills(@NotNull Collection<? extends Skill> skills) {
        Objects.requireNonNull(skills);
        for (Skill skill : skills) {
            addSkill0(skill);
        }
    }

    /**
     * Sets whether this entity should use the state holder or not. May not have any impact if it is changed after the
     * entity has been added to an instance.
     *
     * @param useStateHolder whether this entity should use the state holder
     */
    public void setUseStateHolder(boolean useStateHolder) {
        this.useStateHolder = useStateHolder;
    }

    /**
     * Whether it is necessary to use this object's state holder when modifying attributes or other persistent
     * components of entity state. Defaults to {@code false}, can be changed using
     * {@link Mob#setUseStateHolder(boolean)}.
     *
     * @return true if this entity uses the state holder
     */
    public boolean useStateHolder() {
        return this.useStateHolder;
    }

    public void removeSkill(@NotNull Skill skill) {
        Objects.requireNonNull(skill);
        Trigger trigger = skill.trigger();

        allSkills.removeIf(existing -> {
            boolean remove = existing == skill;
            if (remove) {
                existing.end();
            }

            return remove;
        });
        tickableSkills.removeIf(existing -> existing == skill);
        if (trigger == Trigger.TICK) {
            useOnTick.removeIf(existing -> existing == skill);
        }

        if (trigger == null) {
            return;
        }

        List<Skill> triggers = triggeredSkills.get(trigger);
        if (triggers != null) {
            triggers.removeIf(existing -> existing == skill);
        }
    }

    public @NotNull MobData data() {
        return data;
    }

    public @NotNull Optional<Entity> lastHitEntity() {
        Entity entity = lastHitEntity.get();
        if (entity == null) {
            return Optional.empty();
        }

        if (entity.isRemoved()) {
            lastHitEntity.clear();
            return Optional.empty();
        }

        return Optional.of(entity);
    }

    public void setLastHitEntity(@Nullable Entity entity) {
        if (lastHitEntity.get() != entity) {
            this.lastHitEntity = new WeakReference<>(entity);
        }
    }

    public @NotNull Optional<Entity> lastInteractingPlayer() {
        return Optional.ofNullable(lastInteractingPlayer.get());
    }

    private void addSkill0(Skill skill) {
        skill.init();

        allSkills.add(skill);
        Trigger trigger = skill.trigger();

        if (trigger == Trigger.TICK) {
            useOnTick.add(skill);
        }

        boolean needsTicking = skill.needsTicking();
        if (!needsTicking && trigger == null) {
            return;
        }

        if (needsTicking) {
            tickableSkills.add(skill);
        }

        if (trigger != null && trigger != Trigger.TICK) {
            triggeredSkills.computeIfAbsent(trigger, ignored -> new ArrayList<>()).add(skill);
        }
    }

    private void useIfPresent(Trigger trigger) {
        List<Skill> skills = triggeredSkills.get(trigger);
        if (skills == null) {
            return;
        }

        for (Skill skill : skills) {
            skill.use();
        }
    }

    @Override
    public boolean damage(@NotNull Damage damage, boolean bypassArmor) {
        boolean result = super.damage(damage, bypassArmor);

        if (canUseSkills()) {
            useIfPresent(Trigger.DAMAGED);
        }

        return result;
    }

    @Override
    public void interact(@NotNull Player player, @NotNull Point position) {
        super.interact(player, position);
        if (lastInteractingPlayer.get() != player) {
            lastInteractingPlayer = new WeakReference<>(player);
        }

        if (canUseSkills()) {
            useIfPresent(Trigger.INTERACT);
        }
    }

    @Override
    public void attack(@NotNull Entity target, boolean swingHand) {
        super.attack(target, swingHand);
        if (lastHitEntity.get() != target) {
            lastHitEntity = new WeakReference<>(target);
        }

        if (canUseSkills()) {
            useIfPresent(Trigger.ATTACK);
        }
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        return super.setInstance(instance, spawnPosition).thenRun(() -> {
            getAcquirable().sync(ignored -> {
                if (canUseSkills()) {
                    useIfPresent(Trigger.SPAWN);
                }
            });
        });
    }

    @Override
    public void kill() {
        if (isDead()) {
            return;
        }

        if (canUseSkills()) {
            useIfPresent(Trigger.DEATH);
        }

        super.kill();
    }

    @Override
    public void remove() {
        if (isRemoved()) {
            return;
        }

        for (Skill skill : allSkills) {
            skill.end();
        }
        super.remove();
    }

    @Override
    public void update(long time) {
        if (!canUseSkills()) {
            return;
        }

        super.update(time);

        for (Skill skill : tickableSkills) {
            skill.tick();
        }

        for (Skill skill : useOnTick) {
            skill.use();
        }
    }

    /**
     * Determines if this entity can use skills. Equivalent to {@code !isDead() && !isRemoved()}. This is also queried
     * to determine if the entity's tickable skills should be ticked.
     *
     * @return true if this entity can use skills; false otherwise.
     */
    public boolean canUseSkills() {
        return !isDead && !isRemoved();
    }

    private TeamsPacket createTeamPacket(TeamSettings newSettings, boolean update) {
        if (update) {
            TeamsPacket.Action action = new TeamsPacket.UpdateTeamAction(newSettings.displayName, newSettings.friendlyFlags,
                newSettings.nameTagVisibility, newSettings.collisionRule, newSettings.teamColor, newSettings.teamPrefix,
                newSettings.teamSuffix);
            return new TeamsPacket(this.uniqueTeamName, action);
        }

        TeamsPacket.Action action = new TeamsPacket.CreateTeamAction(newSettings.displayName, newSettings.friendlyFlags,
            newSettings.nameTagVisibility, newSettings.collisionRule, newSettings.teamColor, newSettings.teamPrefix,
            newSettings.teamSuffix, List.of(getUuid().toString()));
        return new TeamsPacket(this.uniqueTeamName, action);
    }

    private void invalidateAndUpdateTeam() {
        this.cachedCreateTeamPacket.invalidate();
        this.cachedUpdateTeamPacket.invalidate();
        sendPacketToViewers(this.cachedUpdateTeamPacket);
    }

    /**
     * Sets the color of this entity's personal team.
     * <p>
     * <b>Thread Behavior</b>: It is necessary to acquire this entity before calling this method.
     *
     * @param namedTextColor the color to set
     */
    public void setTeamColor(@NotNull NamedTextColor namedTextColor) {
        this.teamSettings = this.teamSettings.withTeamColor(namedTextColor);
        invalidateAndUpdateTeam();
    }

    /**
     * Sets the collision rule for this entity's personal team.
     * <p>
     * <b>Thread Behavior</b>: It is necessary to acquire this entity before calling this method.
     *
     * @param collisionRule the color to set
     */
    public void setCollisionRule(@NotNull TeamsPacket.CollisionRule collisionRule) {
        this.teamSettings = this.teamSettings.withCollisionRule(collisionRule);
        invalidateAndUpdateTeam();
    }

    /**
     * Sets the setNameTagVisibility for this entity's personal team.
     * <p>
     * <b>Thread Behavior</b>: It is necessary to acquire this entity before calling this method.
     *
     * @param visibility the visibility rule to set
     */
    public void setNameTagVisibility(@NotNull TeamsPacket.NameTagVisibility visibility) {
        this.teamSettings = this.teamSettings.withNameTagVisibility(visibility);
        invalidateAndUpdateTeam();
    }

    public @NotNull Component name() {
        MobMeta meta = data.meta();
        if (meta != null) {
            Component component = meta.customName();
            if (component != null) {
                return component;
            }
        }

        Component message = getCustomName();
        if (message == null) {
            message = Component.translatable(getEntityType().registry().translationKey());
        }

        return message;
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        player.sendPacket(cachedCreateTeamPacket);
    }

    @Override
    public void updateOldViewer(@NotNull Player player) {
        super.updateOldViewer(player);
        player.sendPacket(cachedRemoveTeamPacket);
    }
}
