package com.github.phantazmnetwork.commons.component.annotation;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class that obeys the standard component model.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ComponentModel {
    /**
     * The key string used to uniquely identify this component. This must obey the general syntax of Adventure
     * {@link Key} objects.
     *
     * @return the value of this annotation, which should be a valid key string
     */
    @NotNull String value();
}
