package com.github.phantazmnetwork.api.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

public class KeyConfigProcessor implements ConfigProcessor<Key> {
    @Override
    public Key dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        @Subst("phantazm") String namespace = element.getStringOrDefault("namespace");
        @Subst("key") String value = element.getStringOrDefault("value");

        return Key.key(namespace, value);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull Key key) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode();
        node.put("namespace", new ConfigPrimitive(key.namespace()));
        node.put("value", new ConfigPrimitive(key.value()));

        return node;
    }
}
