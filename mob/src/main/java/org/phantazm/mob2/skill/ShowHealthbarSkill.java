package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MathUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.trigger.Trigger;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ShowHealthbarSkill implements SkillComponent {
    private static final char HEX = '#';
    private static final Map<String, TextColor> COLOR_ALIASES =
            Map.of("dark_grey", NamedTextColor.DARK_GRAY, "grey", NamedTextColor.GRAY);

    private final Data data;

    @FactoryMethod
    public ShowHealthbarSkill(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, data);
    }

    @DataObject
    public record Data(@Nullable Trigger trigger, @NotNull String bar, int barWidth) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }
    }

    private static class Internal implements Skill {
        private final Mob self;
        private final Data data;

        private int lastAliveBars;

        private Internal(Mob self, Data data) {
            this.self = self;
            this.data = data;
            this.lastAliveBars = -1;
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void init() {
            self.setCustomNameVisible(true);
        }

        @Override
        public void tick() {
            healthbarFor(self).ifPresent(self::setCustomName);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        @Override
        public void end() {
            self.setCustomName(null);
            self.setCustomNameVisible(false);
        }

        private Optional<Component> healthbarFor(LivingEntity entity) {
            float maxHealth = entity.getMaxHealth();
            float currentHealth = MathUtils.clamp(entity.getHealth(), 0, maxHealth);

            float percentage = maxHealth == 0 ? 0 : currentHealth / maxHealth;
            int aliveBars = Math.round(percentage * data.barWidth);

            if (lastAliveBars == aliveBars) {
                return Optional.empty();
            }

            lastAliveBars = aliveBars;

            TagResolver resolver = TagResolver.resolver("healthbar", ((argumentQueue, context) -> {
                String barElement = null;
                TextColor colorStart = null;
                TextColor colorEnd = null;
                TextColor missingColor = null;
                while (argumentQueue.hasNext()) {
                    Tag.Argument argument = argumentQueue.pop();
                    String value = argument.lowerValue();

                    switch (value) {
                        case "bar_component" ->
                                barElement = argumentQueue.popOr(() -> "Missing bar_component argument value").value();
                        case "color_start" -> colorStart = resolveColor(context,
                                argumentQueue.popOr(() -> "Missing color_start argument value").value());
                        case "color_end" -> colorEnd = resolveColor(context,
                                argumentQueue.popOr(() -> "Missing color_end argument value").value());
                        case "missing_color" -> missingColor = resolveColor(context,
                                argumentQueue.popOr(() -> "Missing missing_color argument value").value());
                    }
                }

                if (barElement == null || colorStart == null || colorEnd == null || missingColor == null) {
                    throw context.newException("Missing required argument");
                }

                TextColor aliveColor = TextColor.lerp(percentage, colorStart, colorEnd);

                String alivePart = barElement.repeat(Math.max(0, aliveBars));
                String missingPart = barElement.repeat(Math.max(0, data.barWidth - aliveBars));

                Component aliveComponent = Component.text(alivePart, aliveColor);
                Component deadComponent =
                        missingPart.isEmpty() ? Component.empty() : Component.text(missingPart, missingColor);

                return Tag.selfClosingInserting(
                        Component.join(JoinConfiguration.noSeparators(), aliveComponent, deadComponent));
            }));

            return Optional.of(MiniMessage.miniMessage().deserialize(data.bar, resolver));
        }

        static @NotNull TextColor resolveColor(@NotNull Context ctx, @NotNull String colorName)
                throws ParsingException {
            TextColor color;
            if (COLOR_ALIASES.containsKey(colorName)) {
                color = COLOR_ALIASES.get(colorName);
            }
            else if (colorName.charAt(0) == HEX) {
                color = TextColor.fromHexString(colorName);
            }
            else {
                color = NamedTextColor.NAMES.value(colorName);
            }

            if (color == null) {
                throw ctx.newException(String.format(
                        "Unable to parse a color from '%s'. Please use named colours or hex (#RRGGBB) colors.",
                        colorName));
            }
            return color;
        }
    }
}
