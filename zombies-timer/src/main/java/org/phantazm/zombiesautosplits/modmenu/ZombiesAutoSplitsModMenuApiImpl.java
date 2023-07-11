package org.phantazm.zombiesautosplits.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import org.phantazm.zombiesautosplits.ZombiesAutoSplitsClient;
import org.phantazm.zombiesautosplits.config.ZombiesAutoSplitsConfig;

public class ZombiesAutoSplitsModMenuApiImpl implements ModMenuApi {

    private final ConfigScreenFactory<?> screenFactory = screen -> {
        ZombiesAutoSplitsClient autoSplits = ZombiesAutoSplitsClient.getInstance();
        ZombiesAutoSplitsConfig autoSplitsConfig = autoSplits.getConfig();
        ZombiesAutoSplitsConfig.Builder autoSplitsConfigBuilder = autoSplitsConfig.toBuilder();

        ConfigBuilder configBuilder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(Text.of("Zombies AutoSplits Config"));

        ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();
        ConfigCategory main = configBuilder.getOrCreateCategory(Text.of("Config"));
        main.addEntry(entryBuilder.startStrField(Text.of("Host"), autoSplitsConfig.host())
                .setDefaultValue(ZombiesAutoSplitsConfig.DEFAULT_HOST)
                .setTooltip(Text.of("The host of the LiveSplits server. Most likely localhost."))
                .setSaveConsumer(autoSplitsConfigBuilder::setHost)
                .build());
        main.addEntry(entryBuilder.startIntField(Text.of("Port"), autoSplitsConfig.port())
                .setDefaultValue(ZombiesAutoSplitsConfig.DEFAULT_PORT)
                .setMin(1)
                .setMax(65535)
                .setTooltip(Text.of("The port of the LiveSplits server. Use -1 for the internal splitter."))
                .setSaveConsumer(autoSplitsConfigBuilder::setPort)
                .build());
        main.addEntry(entryBuilder.startBooleanToggle(Text.of("Use LiveSplits"), autoSplitsConfig.useLiveSplits())
                .setDefaultValue(ZombiesAutoSplitsConfig.DEFAULT_USE_LIVE_SPLITS)
                .setTooltip(Text.of("Whether to use the LiveSplits splitter."))
                .setSaveConsumer(autoSplitsConfigBuilder::setUseLiveSplits)
                .build());
        main.addEntry(entryBuilder.startBooleanToggle(Text.of("Use Internal"), autoSplitsConfig.useInternal())
                .setDefaultValue(ZombiesAutoSplitsConfig.DEFAULT_USE_INTERNAL)
                .setTooltip(Text.of("Whether to use the internal splitter."))
                .setSaveConsumer(autoSplitsConfigBuilder::setUseInternal)
                .build());

        configBuilder.setSavingRunnable(() -> {
            autoSplits.setConfig(autoSplitsConfigBuilder.build());
            autoSplits.saveConfig();
        });

        return configBuilder.build();
    };

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screenFactory;
    }
}
