package org.phantazm.core;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DamageUtils {
    private DamageUtils() {
    }

    public static float computeDamageWithResistances(@NotNull LivingEntity entity, @Nullable String damageType, float incoming) {
        if (damageType == null) {
            return incoming;
        }

        Attribute attribute = Attribute.fromKey(damageType);
        if (attribute == null) {
            return incoming;
        }

        return Math.max(incoming * entity.getAttributeValue(attribute), 0);
    }

    public static float computeDamageWithArmor(float baseDamage, float defense, float toughness) {
        return baseDamage * (1F - (Math.max(defense / 5F, defense - ((4F * baseDamage) / (toughness + 8F))) / 25F));
    }

    public static float computeDamageWithArmor(@NotNull LivingEntity entity, float baseDamage) {
        float defense = entity.getAttributeValue(Attribute.ARMOR);
        float toughness = entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS);
        return computeDamageWithArmor(baseDamage, defense, toughness);
    }

    public static float computeDamageWithArmorAndResistances(@Nullable String damageType, @NotNull LivingEntity entity,
        float baseDamage) {
        float defense = entity.getAttributeValue(Attribute.ARMOR);
        float toughness = entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS);
        float withArmorReduction = computeDamageWithArmor(baseDamage, defense, toughness);

        return computeDamageWithResistances(entity, damageType, withArmorReduction);
    }

    public static boolean damageWithArmor(@NotNull LivingEntity entity,
        @NotNull Float2ObjectFunction<? extends Damage> damageFunction, float baseDamage) {
        return entity.damage(damageFunction.get(computeDamageWithArmor(entity, baseDamage)));
    }

    public static boolean damageWithArmorAndResistances(@Nullable String damageType, @NotNull LivingEntity entity,
        @NotNull Float2ObjectFunction<? extends Damage> damageFunction, float baseDamage) {
        return entity.damage(damageFunction.get(computeDamageWithArmorAndResistances(damageType, entity, baseDamage)));
    }

    public static boolean damage(@NotNull LivingEntity entity,
        @NotNull Float2ObjectFunction<? extends Damage> damageFunction, float baseDamage, boolean bypassArmor) {
        if (bypassArmor) {
            return entity.damage(damageFunction.get(baseDamage));
        }

        return damageWithArmor(entity, damageFunction, baseDamage);
    }

    public static boolean damage(@Nullable String damageType, @NotNull LivingEntity entity,
        @NotNull Float2ObjectFunction<? extends Damage> damageFunction, float baseDamage, boolean bypassArmor) {
        if (bypassArmor) {
            return entity.damage(damageFunction.get(computeDamageWithResistances(entity, damageType, baseDamage)));
        }

        return damageWithArmorAndResistances(damageType, entity, damageFunction, baseDamage);
    }

    public static boolean damage(@NotNull LivingEntity entity,
        @NotNull Entity source, float baseDamage, boolean bypassArmor) {
        if (bypassArmor) {
            return entity.damage(Damage.fromEntity(source, baseDamage));
        }

        float actualDamage = computeDamageWithArmor(entity, baseDamage);
        return entity.damage(Damage.fromEntity(source, actualDamage));
    }

    public static boolean damage(@Nullable String damageType, @NotNull LivingEntity entity,
        @NotNull Entity source, float baseDamage, boolean bypassArmor) {
        if (bypassArmor) {
            return entity.damage(Damage.fromEntity(source, computeDamageWithResistances(entity, damageType, baseDamage)));
        }

        float actualDamage = computeDamageWithArmorAndResistances(damageType, entity, baseDamage);
        return entity.damage(Damage.fromEntity(source, actualDamage));
    }
}
