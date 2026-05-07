package dev.foxgirl.damagenumbers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class DamageNumbersInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        new DamageNumbers(FabricLoader.getInstance().getConfigDir());
        ClientTickEvents.END_CLIENT_TICK.register(client -> DamageNumbers.getHandler().tick());
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> DamageNumbers.getHandler().render(context));
    }

}
