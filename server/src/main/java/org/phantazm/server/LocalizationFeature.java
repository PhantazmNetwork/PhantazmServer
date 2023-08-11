package org.phantazm.server;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.phantazm.commons.FileUtils;
import org.phantazm.commons.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.PropertyResourceBundle;

public final class LocalizationFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalizationFeature.class);

    public static final Path LANG_FOLDER = Path.of("./lang");
    public static final Key TRANSLATION_REGISTRY_KEY = Key.key(Namespaces.PHANTAZM, "translation");

    private LocalizationFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize() {
        TranslationRegistry registry = TranslationRegistry.create(TRANSLATION_REGISTRY_KEY);
        registry.defaultLocale(Locale.US);

        try {
            FileUtils.createDirectories(LANG_FOLDER);

            int count = 0;
            try (DirectoryStream<Path> files = Files.newDirectoryStream(LANG_FOLDER, "*.lang")) {
                for (Path path : files) {
                    String filenameString = path.getFileName().toString();
                    String withoutExtension = filenameString.substring(0, filenameString.indexOf('.'));
                    Locale fileLocale = Locale.forLanguageTag(withoutExtension);
                    if (fileLocale == null) {
                        return;
                    }

                    try {
                        PropertyResourceBundle bundle = new PropertyResourceBundle(Files.newBufferedReader(path));
                        registry.registerAll(fileLocale, bundle, true);
                        count++;
                    }
                    catch (IOException e) {
                        LOGGER.warn("Exception when loading localization file " + path, e);
                    }
                }
            }

            LOGGER.info("Loaded {} localization files", count);
        }
        catch (IOException e) {
            LOGGER.warn("Exception when loading localization info", e);
        }

        GlobalTranslator.translator().addSource(registry);
    }
}
