package org.phantazm.zombies;

import net.minestom.server.attribute.Attribute;

public final class Attributes {
    public static final Attribute HITBOX_EXPANSION = new Attribute("phantazm.hitbox_expand", 0.35F, false);

    public static final Attribute FIRE_RATE_MULTIPLIER = new Attribute("phantazm.fire_rate", 1F, false);

    public static final Attribute REVIVE_TICKS = new Attribute("phantazm.revive_ticks", 30F, false);

    public static final Attribute HEAL_TICKS = new Attribute("phantazm.heal_rate", 20F, false);

    public static final Attribute GUN_DAMAGE = new Attribute("phantazm.gun_damage", 0F, false);

    public static final Attribute BULLET_PENETRATION = new Attribute("phantazm.bullet_penetration", 0F, false);

    public static final Attribute ATTACK_SPEED_MULTIPLIER =
        new Attribute("phantazm.attack_speed_multiplier", 1F, false);

    public static final Attribute HEADSHOT_DAMAGE_RECEIVED =
        new Attribute("phantazm.gun_headshot_damage_received", 0F, false);

    public static final Attribute FIRE_DAMAGE_APPLY_INTERVAL = new Attribute("phantazm.fire_damage_apply_interval", 0F, false);

    // damage type API
    public static final Attribute FIRE_DAMAGE = new Attribute("damage.fire", 0F, false);

    public static final Attribute EXPLOSION_DAMAGE = new Attribute("damage.explosion", 0F, false);

    public static final Attribute POISON_DAMAGE = new Attribute("damage.poison", 0F, false);


    public static final Attribute FIRE_APPLY_DURATION = new Attribute("damage.fire.duration", 0F, false);

    public static final Attribute FIRE_APPLY_DAMAGE = new Attribute("damage.fire.apply", 0F, false);

    public static final Attribute POWERUP_KILL_ALL_IN_RADIUS_INSTAKILL_DISTANCE_SCALING =
        new Attribute("phantazm.powerup.kill_all_in_radius.instakill_distance_scaling", 0F, false);

    public static final Attribute POWERUP_KILL_ALL_IN_RADIUS_DAMAGE_DISTANCE_START_SCALING =
        new Attribute("phantazm.powerup.kill_all_in_radius.damage_distance_start_scaling", 0F, false);

    public static final Attribute POWERUP_KILL_ALL_IN_RADIUS_DAMAGE_DISTANCE_END_SCALING =
        new Attribute("phantazm.powerup.kill_all_in_radius.damage_distance_end_scaling", 0F, false);

    public static final Attribute POWERUP_OVERCLOCK_DURATION =
        new Attribute("phantazm.powerup.overclock.duration", 0F, false);

    public static final Attribute NIL = new Attribute("phantazm.nil", 0F, false);

    private Attributes() {
    }

    public static void registerAll() {
        HITBOX_EXPANSION.register();
        FIRE_RATE_MULTIPLIER.register();
        REVIVE_TICKS.register();
        HEAL_TICKS.register();
        GUN_DAMAGE.register();
        BULLET_PENETRATION.register();
        ATTACK_SPEED_MULTIPLIER.register();
        HEADSHOT_DAMAGE_RECEIVED.register();
        FIRE_DAMAGE_APPLY_INTERVAL.register();
        FIRE_DAMAGE.register();
        EXPLOSION_DAMAGE.register();
        POISON_DAMAGE.register();
        POWERUP_KILL_ALL_IN_RADIUS_DAMAGE_DISTANCE_START_SCALING.register();
        POWERUP_KILL_ALL_IN_RADIUS_INSTAKILL_DISTANCE_SCALING.register();
        POWERUP_KILL_ALL_IN_RADIUS_DAMAGE_DISTANCE_END_SCALING.register();
        POWERUP_OVERCLOCK_DURATION.register();

        FIRE_APPLY_DURATION.register();
        FIRE_APPLY_DAMAGE.register();
        NIL.register();
    }
}
