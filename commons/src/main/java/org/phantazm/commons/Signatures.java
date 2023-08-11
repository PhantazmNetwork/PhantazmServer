package org.phantazm.commons;

import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.signature.ScalarSignature;
import com.github.steanky.ethylene.mapper.signature.Signature;
import com.github.steanky.ethylene.mapper.signature.SignatureParameter;
import com.github.steanky.ethylene.mapper.type.Token;
import com.github.steanky.vector.Bounds3D;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3D;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.TitlePart;
import net.kyori.adventure.util.RGBLike;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Signatures {
    private Signatures() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull MappingProcessorSource.Builder core(@NotNull MappingProcessorSource.Builder builder) {
        return builder.withScalarSignature(component()).withCustomSignature(sound())
                .withScalarSignature(titlePartComponent()).withScalarSignature(namedTextColor())
                .withScalarSignature(namedTextColorDirect()).withCustomSignature(rgbLike())
                .withCustomSignature(textColor()).withCustomSignature(style()).withCustomSignature(bounds3I())
                .withCustomSignature(bounds3D()).withCustomSignature(vec3I()).withCustomSignature(vec3D())
                .withScalarSignature(uuid());
    }

    private static ScalarSignature<Component> component() {
        return ScalarSignature.of(Token.ofClass(Component.class), element -> {
            return MiniMessage.miniMessage().deserialize(element.asString());
        }, component -> {
            return component == null
                   ? ConfigPrimitive.NULL
                   : ConfigPrimitive.of(MiniMessage.miniMessage().serialize(component));
        });
    }

    private static Signature<Sound> sound() {
        return Signature.builder(Token.ofClass(Sound.class), (ignored, args) -> {
                            double volume = args.get(2);
                            double pitch = args.get(3);
                            return Sound.sound((Key)args.get(0), (Sound.Source)args.get(1), (float)volume, (float)pitch);
                        }, sound -> List.of(sound.name(), sound.source(), sound.volume(), sound.pitch()),
                        Map.entry("name", SignatureParameter.parameter(Token.ofClass(Key.class))),
                        Map.entry("source", SignatureParameter.parameter(Token.ofClass(Sound.Source.class))),
                        Map.entry("volume", SignatureParameter.parameter(Token.DOUBLE)),
                        Map.entry("pitch", SignatureParameter.parameter(Token.DOUBLE))).matchingNames().matchingTypeHints()
                .build();
    }

    private static ScalarSignature<TitlePart<Component>> titlePartComponent() {
        ConfigPrimitive title = ConfigPrimitive.of("TITLE");
        ConfigPrimitive subtitle = ConfigPrimitive.of("SUBTITLE");

        return ScalarSignature.of(new Token<>() {
        }, element -> {
            String value = element.asString();
            return switch (value) {
                case "TITLE" -> TitlePart.TITLE;
                case "SUBTITLE" -> TitlePart.SUBTITLE;
                default -> throw new IllegalArgumentException("Unexpected TitlePart '" + value + "'");
            };
        }, titlePart -> titlePart == null ? ConfigPrimitive.NULL : titlePart == TitlePart.TITLE ? title : subtitle);
    }

    private static ScalarSignature<TextColor> namedTextColor() {
        return ScalarSignature.of(Token.ofClass(TextColor.class), element -> textColorFromString(element.asString()),
                textColor -> configFromTextColor(textColor, true));
    }

    private static ScalarSignature<NamedTextColor> namedTextColorDirect() {
        return ScalarSignature.of(Token.ofClass(NamedTextColor.class),
                element -> namedTextColorFromString(element.asString()),
                textColor -> configFromTextColor(textColor, false));
    }

    private static ConfigPrimitive configFromTextColor(TextColor textColor, boolean allowFullRGB) {
        if (textColor instanceof NamedTextColor namedTextColor) {
            return ConfigPrimitive.of(namedTextColor.toString());
        }

        NamedTextColor named = NamedTextColor.namedColor(textColor.value());
        if (named != null) {
            return ConfigPrimitive.of(named.toString());
        }

        if (!allowFullRGB) {
            return ConfigPrimitive.NULL;
        }

        return ConfigPrimitive.of(textColor.asHexString());
    }

    private static TextColor textColorFromString(String string) {
        if (string.startsWith("#")) {
            return Objects.requireNonNullElse(TextColor.fromHexString(string), NamedTextColor.WHITE);
        }

        return namedTextColorFromString(string);
    }

    private static NamedTextColor namedTextColorFromString(String string) {
        return NamedTextColor.NAMES.valueOr(string.toLowerCase(Locale.ROOT), NamedTextColor.WHITE);
    }

    private static Signature<RGBLike> rgbLike() {
        return Signature.builder(Token.ofClass(RGBLike.class), (ignored, args) -> {
                            int r = args.get(0);
                            int g = args.get(1);
                            int b = args.get(2);
                            return TextColor.color(r, g, b);
                        }, color -> List.of(color.red(), color.green(), color.blue()),
                        Map.entry("r", SignatureParameter.parameter(Token.INTEGER)),
                        Map.entry("g", SignatureParameter.parameter(Token.INTEGER)),
                        Map.entry("b", SignatureParameter.parameter(Token.INTEGER))).matchingNames().matchingTypeHints()
                .build();
    }

    private static Signature<TextColor> textColor() {
        return Signature.builder(Token.ofClass(TextColor.class), (ignored, args) -> {
                            int r = args.get(0);
                            int g = args.get(1);
                            int b = args.get(2);
                            return TextColor.color(r, g, b);
                        }, textColor -> List.of(textColor.red(), textColor.green(), textColor.blue()),
                        Map.entry("r", SignatureParameter.parameter(Token.INTEGER)),
                        Map.entry("g", SignatureParameter.parameter(Token.INTEGER)),
                        Map.entry("b", SignatureParameter.parameter(Token.INTEGER))).matchingNames().matchingTypeHints()
                .build();
    }

    private static Signature<Style> style() {
        return Signature.builder(Token.ofClass(Style.class), (ignored, args) -> {
                            TextColor textColor = args.get(0);
                            TextDecoration[] decorations = args.get(1);
                            return Style.style(textColor, decorations);
                        }, style -> {
                            Collection<Object> textColors = new ArrayList<>(1);
                            textColors.add(style.color());
                            return textColors;
                        }, Map.entry("textColor", SignatureParameter.parameter(Token.ofClass(TextColor.class))),
                        Map.entry("textDecorations", SignatureParameter.parameter(Token.ofClass(TextDecoration[].class))))
                .matchingNames().matchingTypeHints().build();
    }

    private static Signature<Bounds3I> bounds3I() {
        return Signature.builder(Token.ofClass(Bounds3I.class), (ignored, args) -> {
                            return Bounds3I.immutable(args.get(0), args.get(1));
                        }, bounds -> List.of(bounds.immutableOrigin(), bounds.immutableLengths()),
                        Map.entry("origin", SignatureParameter.parameter(Token.ofClass(Vec3I.class))),
                        Map.entry("lengths", SignatureParameter.parameter(Token.ofClass(Vec3I.class)))).matchingNames()
                .matchingTypeHints().build();
    }

    private static Signature<Bounds3D> bounds3D() {
        return Signature.builder(Token.ofClass(Bounds3D.class), (ignored, args) -> {
                            return Bounds3D.immutable(args.get(0), args.get(1));
                        }, bounds -> List.of(bounds.immutableOrigin(), bounds.immutableLengths()),
                        Map.entry("origin", SignatureParameter.parameter(Token.ofClass(Vec3D.class))),
                        Map.entry("lengths", SignatureParameter.parameter(Token.ofClass(Vec3D.class)))).matchingNames()
                .matchingTypeHints().build();
    }

    private static Signature<Vec3I> vec3I() {
        return Signature.builder(Token.ofClass(Vec3I.class),
                        (ignored, args) -> Vec3I.immutable(args.get(0), args.get(1), args.get(2)),
                        vec -> List.of(vec.x(), vec.y(), vec.z()), Map.entry("x", SignatureParameter.parameter(Token.INTEGER)),
                        Map.entry("y", SignatureParameter.parameter(Token.INTEGER)),
                        Map.entry("z", SignatureParameter.parameter(Token.INTEGER))).matchingNames().matchingTypeHints()
                .build();
    }

    private static Signature<Vec3D> vec3D() {
        return Signature.builder(Token.ofClass(Vec3D.class),
                (ignored, args) -> Vec3D.immutable(args.get(0), args.get(1), args.get(2)),
                vec -> List.of(vec.x(), vec.y(), vec.z()), Map.entry("x", SignatureParameter.parameter(Token.DOUBLE)),
                Map.entry("y", SignatureParameter.parameter(Token.DOUBLE)),
                Map.entry("z", SignatureParameter.parameter(Token.DOUBLE))).matchingNames().matchingTypeHints().build();
    }

    private static ScalarSignature<UUID> uuid() {
        return ScalarSignature.of(Token.ofClass(UUID.class), element -> UUID.fromString(element.asString()),
                uuid -> uuid == null ? ConfigPrimitive.NULL : ConfigPrimitive.of(uuid.toString()));
    }
}
