package dev.foxgirl.damagenumbers.client;

import dev.foxgirl.damagenumbers.DamageNumbers;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public final class DamageNumbersImpl implements DamageNumbersHandler, Config.PathProvider {

    public final @NotNull Config config = new Config();
    public final @NotNull Config configDefault = new Config();

    public final @NotNull Path configDirPath;

    @Override
    public @NotNull Path getConfigFilePath() {
        return configDirPath.resolve("damagenumbers.json");
    }
    @Override
    public @NotNull Path getConfigTempPath() {
        return configDirPath.resolve("damagenumbers.json.tmp");
    }

    public DamageNumbersImpl(@NotNull Path configDir) {
        configDirPath = configDir;
        config.readConfig(this);
    }

    @Override
    public @NotNull Screen createConfigScreen(@NotNull Screen parent) {
        try {
            var factory = (ConfigScreenFactory) Class
                .forName("dev.foxgirl.damagenumbers.client.ConfigScreenFactoryImpl")
                .getConstructor(Config.class, Config.class, Config.PathProvider.class)
                .newInstance(config, configDefault, this);
            return factory.createConfigScreen(parent);
        } catch (NoClassDefFoundError cause) {
            DamageNumbers.LOGGER.error("Failed to create config screen due to missing class", cause);
        } catch (ReflectiveOperationException cause) {
            DamageNumbers.LOGGER.error("Failed to create config screen due to reflection error", cause);
        }
        return parent;
    }

    private final Deque<TextParticle> particles = new ArrayDeque<>();
    private int spawnLogCount;

    @Override
    public void tick() {
        particles.removeIf(TextParticle::tick);
    }

    @Override
    public void render(@NotNull LevelRenderContext context) {
        var client = Minecraft.getInstance();
        var font = client.font;
        var poseStack = context.poseStack();
        var camera = context.gameRenderer().getMainCamera();
        for (var particle : particles) {
            particle.render(font, poseStack, context.submitNodeCollector(), camera, context.levelState().cameraRenderState, client.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        }
    }

    public void onEntityHealthChange(@NotNull LivingEntity entity, float oldHealth, float newHealth) {
        if (!config.isEnabled) return;

        float damage = oldHealth - newHealth;
        if (damage <= 0.0F) return;

        var client = Minecraft.getInstance();

        if (entity == client.player && !config.isPlayerDamageShown) return;

        var world = client.level;
        if (world == null || world != entity.level()) return;
        if (client.player == null) return;

        if (entity.distanceToSqr(client.player) > 2304.0) return;

        int particleLimit = switch (client.options.particles().get()) {
            case ALL -> 256;
            case DECREASED -> 64;
            case MINIMAL -> 16;
        };
        while (particles.size() >= particleLimit) {
            var particle = particles.poll();
            if (particle != null) particle.remove();
        }

        Vec3 particlePos = entity.position().add(0.0, entity.getBbHeight() + 0.25, 0.0);

        Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
        Vec3 cameraDirection = cameraPos.subtract(entity.position()).normalize();
        Vec3 particleVelocity = entity.getDeltaMovement()
            .scale(0.1)
            .add(cameraDirection.x * 0.025, 0.08, cameraDirection.z * 0.025);

        var particle = new TextParticle(particlePos, particleVelocity, Config.clampDisplayTicks(config.displayTicks));

        var text = String.format("%.1f", damage);
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }

        particle.setText(text);

        var colorSm = config.useCustomColors ? config.colorSm : configDefault.colorSm;
        var colorMd = config.useCustomColors ? config.colorMd : configDefault.colorMd;
        var colorLg = config.useCustomColors ? config.colorLg : configDefault.colorLg;
        var colorCrit = config.useCustomColors ? config.colorCrit : configDefault.colorLg;

        if (damage > 15.0F) {
            particle.setColor(colorCrit);
        } else if (damage >= 8.0F) {
            particle.setColor(Color.lerp(colorMd, colorLg, (damage - 8.0F) / 8.0F));
        } else if (damage >= 2.0F) {
            particle.setColor(Color.lerp(colorSm, colorMd, (damage - 2.0F) / 6.0F));
        } else {
            particle.setColor(colorSm);
        }

        particles.add(particle);
        if (spawnLogCount < 5) {
            spawnLogCount++;
            DamageNumbers.LOGGER.info("Spawned damage number {} for {}", text, entity.getType().toString());
        }
    }

}
