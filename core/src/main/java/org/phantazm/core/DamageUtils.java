package org.phantazm.core;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;

public final class DamageUtils {
    private DamageUtils() {
    }

    public static float computeDamageWithArmor(float baseDamage, float defense, float toughness) {
        return baseDamage * (1F - (Math.max(defense / 5F, defense - ((4F * baseDamage) / (toughness + 8F))) / 25F));
    }

    public static float computeDamageWithArmor(@NotNull LivingEntity entity, float baseDamage) {
        float defense = entity.getAttributeValue(Attribute.ARMOR);
        float toughness = entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS);
        return computeDamageWithArmor(baseDamage, defense, toughness);
    }

    public static boolean damageWithArmor(@NotNull LivingEntity entity,
        @NotNull Float2ObjectFunction<? extends Damage> damageFunction, float baseDamage) {
        return entity.damage(damageFunction.get(computeDamageWithArmor(entity, baseDamage)));
    }

    public static boolean damage(@NotNull LivingEntity entity,
        @NotNull Float2ObjectFunction<? extends Damage> damageFunction, float baseDamage, boolean bypassArmor) {
        if (bypassArmor) {
            return entity.damage(damageFunction.get(baseDamage));
        }

        return damageWithArmor(entity, damageFunction, baseDamage);
    }

    public static boolean damage(@NotNull LivingEntity entity,
        @NotNull DamageType damageType, float baseDamage, boolean bypassArmor) {
        if (bypassArmor) {
            return entity.damage(damageType, baseDamage);
        }

        float actualDamage = computeDamageWithArmor(entity, baseDamage);
        return entity.damage(damageType, actualDamage);
    }

    public static boolean damage(@NotNull LivingEntity entity,
        @NotNull Entity source, float baseDamage, boolean bypassArmor) {
        if (bypassArmor) {
            return entity.damage(Damage.fromEntity(source, baseDamage));
        }

        float actualDamage = computeDamageWithArmor(entity, baseDamage);
        return entity.damage(Damage.fromEntity(source, actualDamage));
    }
}
