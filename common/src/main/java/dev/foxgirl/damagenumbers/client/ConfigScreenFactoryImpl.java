package dev.foxgirl.damagenumbers.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class ConfigScreenFactoryImpl implements ConfigScreenFactory {

    private final @NotNull Config config;
    private final @NotNull Config.PathProvider configPathProvider;

    public ConfigScreenFactoryImpl(
        @NotNull Config config,
        @NotNull Config configDefault,
        @NotNull Config.PathProvider configPathProvider
    ) {
        this.config = config;
        this.configPathProvider = configPathProvider;
    }

    @Override
    public @NotNull Screen createConfigScreen(@NotNull Screen parent) {
        return new ConfigScreen(parent, config, configPathProvider);
    }

    private static final class ConfigScreen extends Screen {

        private final @NotNull Screen parent;
        private final @NotNull Config config;
        private final @NotNull Config.PathProvider paths;

        private EditBox displayTicks;
        private EditBox colorSm;
        private EditBox colorMd;
        private EditBox colorLg;

        private ConfigScreen(@NotNull Screen parent, @NotNull Config config, @NotNull Config.PathProvider paths) {
            super(Component.literal("Damage Numbers"));
            this.parent = parent;
            this.config = config;
            this.paths = paths;
        }

        @Override
        protected void init() {
            int center = width / 2;
            int left = center - 120;
            int y = 52;

            addRenderableWidget(Button.builder(toggleLabel("Enabled", () -> config.isEnabled), button -> {
                config.isEnabled = !config.isEnabled;
                button.setMessage(toggleLabel("Enabled", () -> config.isEnabled));
            }).bounds(left, y, 240, 20).build());
            y += 26;

            addRenderableWidget(Button.builder(toggleLabel("Player Damage", () -> config.isPlayerDamageShown), button -> {
                config.isPlayerDamageShown = !config.isPlayerDamageShown;
                button.setMessage(toggleLabel("Player Damage", () -> config.isPlayerDamageShown));
            }).bounds(left, y, 240, 20).build());
            y += 32;

            displayTicks = addTextField(left, y, String.valueOf(config.displayTicks), 4);
            y += 26;
            colorSm = addColorField(left, y, "Small", config.colorSm);
            y += 26;
            colorMd = addColorField(left, y, "Medium", config.colorMd);
            y += 26;
            colorLg = addColorField(left, y, "Large", config.colorLg);
            y += 34;

            addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(center - 50, y, 100, 20)
                .build());
        }

        private @NotNull EditBox addColorField(int left, int y, @NotNull String label, @NotNull Color color) {
            return addTextField(left, y, color.toString(), 9);
        }

        private @NotNull EditBox addTextField(int left, int y, @NotNull String value, int maxLength) {
            var editBox = new EditBox(font, left + 82, y, 158, 20, Component.empty());
            editBox.setMaxLength(maxLength);
            editBox.setValue(value);
            return addRenderableWidget(editBox);
        }

        @Override
        public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            super.extractRenderState(graphics, mouseX, mouseY, delta);
            int left = width / 2 - 120;
            graphics.centeredText(font, title, width / 2, 24, 0xFFFFFF);
            graphics.text(font, "Ticks", left, 110, 0xA0A0A0);
            graphics.text(font, "Small", left, 136, 0xA0A0A0);
            graphics.text(font, "Medium", left, 162, 0xA0A0A0);
            graphics.text(font, "Large", left, 188, 0xA0A0A0);
        }

        @Override
        public void onClose() {
            applyColors();
            config.writeConfig(paths);
            if (minecraft != null) {
                minecraft.setScreen(parent);
            }
        }

        private void applyColors() {
            config.displayTicks = parseTicks(displayTicks, config.displayTicks);
            config.colorSm = parseColor(colorSm, config.colorSm);
            config.colorMd = parseColor(colorMd, config.colorMd);
            config.colorLg = parseColor(colorLg, config.colorLg);
        }

        private static @NotNull Color parseColor(@NotNull EditBox editBox, @NotNull Color fallback) {
            try {
                return Color.valueOf(editBox.getValue());
            } catch (IllegalArgumentException ignored) {
                return fallback;
            }
        }

        private static int parseTicks(@NotNull EditBox editBox, int fallback) {
            try {
                return Config.clampDisplayTicks(Integer.parseInt(editBox.getValue()));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }

        private static @NotNull Component toggleLabel(@NotNull String label, @NotNull Supplier<Boolean> value) {
            return Component.literal(label + ": " + (value.get() ? "On" : "Off"));
        }

    }

}
