package dev.foxgirl.damagenumbers.client;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public interface ConfigScreenFactory {

    @NotNull Screen createConfigScreen(@NotNull Screen parent);

}
