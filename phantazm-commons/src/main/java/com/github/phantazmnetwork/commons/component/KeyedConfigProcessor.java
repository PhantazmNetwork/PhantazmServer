package com.github.phantazmnetwork.commons.component;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Keyed;

public interface KeyedConfigProcessor<TData extends Keyed> extends ConfigProcessor<TData>, Keyed { }
