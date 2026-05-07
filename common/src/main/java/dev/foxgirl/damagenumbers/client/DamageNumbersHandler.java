package dev.foxgirl.damagenumbers.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface DamageNumbersHandler {

    @NotNull Screen createConfigScreen(@NotNull Screen parent);

    void tick();

    void render(@NotNull LevelRenderContext context);

    void onEntityHealthChange(@NotNull LivingEntity entity, float oldHealth, float newHealth);

}
