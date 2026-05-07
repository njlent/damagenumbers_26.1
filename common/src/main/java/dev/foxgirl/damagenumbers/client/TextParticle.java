package dev.foxgirl.damagenumbers.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class TextParticle {

    private static final int FULL_BRIGHT = 0xF000F0;

    private Component text = Component.empty();

    private Vec3 previousPos;
    private Vec3 pos;
    private Vec3 velocity;

    private Color color = Color.valueOf("#FFFFFFFF");

    private int age;
    private final int lifetime;

    public TextParticle(@NotNull Vec3 pos, @NotNull Vec3 velocity, int lifetime) {
        this.previousPos = pos;
        this.pos = pos;
        this.velocity = velocity;
        this.lifetime = Config.clampDisplayTicks(lifetime);
    }

    public void setText(@NotNull String text) {
        this.text = Component.literal(text);
    }

    public void setColor(@NotNull Color color) {
        this.color = color;
    }

    public boolean tick() {
        previousPos = pos;
        velocity = velocity.add(0.0, -0.03, 0.0);
        pos = pos.add(velocity);
        velocity = velocity.scale(0.99);
        age++;
        return age >= lifetime;
    }

    public void remove() {
        age = lifetime;
    }

    public void render(
        @NotNull PoseStack poseStack,
        @NotNull SubmitNodeCollector submitNodeCollector,
        @NotNull Camera camera,
        @NotNull CameraRenderState cameraRenderState,
        float tickDelta
    ) {
        Vec3 cameraPos = camera.position();
        Vec3 renderPos = previousPos.lerp(pos, tickDelta).subtract(cameraPos);

        submitNodeCollector.submitNameTag(
            poseStack,
            renderPos.add(0.0, -0.5, 0.0),
            0,
            text,
            true,
            FULL_BRIGHT,
            renderPos.lengthSqr(),
            cameraRenderState
        );
    }

}
