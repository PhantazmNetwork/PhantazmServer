package org.phantazm.server;

import com.github.steanky.ethylene.core.AbstractConfigCodec;
import com.github.steanky.ethylene.core.ElementType;
import com.github.steanky.ethylene.core.Graph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.function.Supplier;

/**
 * Provides support for the YAML format.
 */
public class YamlCodec extends AbstractConfigCodec {
    private static final String NAME = "YAML";
    private static final String PREFERRED_EXTENSION = "yml";
    private static final Set<String> EXTENSIONS = Set.of(PREFERRED_EXTENSION, "yaml");

    private static final int ENCODE_OPTIONS = Graph.Options.TRACK_REFERENCES;

    //for YML, it's possible to construct circular references in config, so enable reference tracking
    private static final int DECODE_OPTIONS = Graph.Options.TRACK_REFERENCES;

    private final Supplier<Yaml> yamlSupplier;


    public YamlCodec() {
        this(Yaml::new);
    }


    public YamlCodec(@NotNull Supplier<Yaml> yamlSupplier) {
        super(ENCODE_OPTIONS, DECODE_OPTIONS);
        this.yamlSupplier = yamlSupplier;
    }

    @Override
    protected @NotNull Object readObject(@NotNull InputStream input) throws IOException {
        Yaml yaml = Objects.requireNonNull(yamlSupplier.get(), "yamlSupplier value");
        try (input) {
            Iterable<Object> objectIterable = yaml.loadAll(input);
            List<Object> objectList = new ArrayList<>();
            for (Object object : objectIterable) {
                objectList.add(object);
            }

            //support loading multiple YAML documents from one stream
            if (objectList.size() == 1) {
                return objectList.get(0);
            } else {
                return objectList;
            }
        } catch (Exception exception) {
            throw new IOException(exception);
        }
    }

    @Override
    protected void writeObject(@NotNull Object object, @NotNull OutputStream output) throws IOException {
        Yaml yaml = Objects.requireNonNull(yamlSupplier.get(), "yamlSupplier value");

        try {
            if (object instanceof Iterable<?> objects) {
                yaml.dumpAll(objects.iterator(), new OutputStreamWriter(output));
            } else {
                yaml.dump(object, new OutputStreamWriter(output));
            }
        } catch (Exception exception) {
            throw new IOException(exception);
        }
    }

    @Override
    public @Unmodifiable @NotNull Set<String> getPreferredExtensions() {
        return EXTENSIONS;
    }

    @Override
    public @NotNull String getPreferredExtension() {
        return PREFERRED_EXTENSION;
    }

    @Override
    public @NotNull String getName() {
        return NAME;
    }

    @Override
    public @NotNull Set<ElementType> supportedTopLevelTypes() {
        //always return a new set, EnumSet static methods produce modifiable collections
        return EnumSet.allOf(ElementType.class);
    }
}