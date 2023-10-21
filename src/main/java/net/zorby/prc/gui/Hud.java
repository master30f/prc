package net.zorby.prc.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.zorby.prc.Finder;
import net.zorby.prc.PRC;

public class Hud {
    private boolean enabled = false;

    public Hud() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!this.enabled) return;

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            int width = drawContext.getScaledWindowWidth();
            int fontHeight = textRenderer.fontHeight;

            assert MinecraftClient.getInstance().player != null;
            BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
            ColumnPos col = PRC.getInstance().getFinder().getElytraCol();

            int distance = MathHelper.floor(getDistance(playerPos.getX(), playerPos.getZ(), col.x(), col.z()));

            drawContext.drawCenteredTextWithShadow(textRenderer, Text.translatable("hud.navigating", col.x(), col.z()), width / 2, 10, 0xffffff);
            drawContext.drawCenteredTextWithShadow(textRenderer, Text.translatable("hud.distance", distance), width / 2, 10 + fontHeight + 5, 0xffffff);
        });
    }

    public void toggle() {
        if (this.enabled) {
            this.enabled = false;
        } else {
            PRC.getInstance().getFinder().findApproximate();
            this.enabled = true;
        }
    }

    public void drawXRay(MatrixStack matrixStack, Camera camera) {
        Vec3d cameraPos = camera.getPos();
        Finder finder = PRC.getInstance().getFinder();

        ColumnPos elytraCol = finder.getElytraCol();

        if (elytraCol == null) return;

        boolean foundExact = finder.foundExact();
        int elytraY;
        if (foundExact) {
            elytraY = finder.getElytraY();
        } else {
            elytraY = (int) cameraPos.getY();
        }

        Vec3d start = new Vec3d(cameraPos.getX(), cameraPos.getY() - 1, cameraPos.getZ());
        Vec3d end = Vec3d.ofCenter(new Vec3i(elytraCol.x(), elytraY, elytraCol.z()));


        // Prepare rendering


        matrixStack.push();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.disableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO
        );
        RenderSystem.lineWidth(2.0f);

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);


        // Render


        drawLine(matrixStack, buffer, cameraPos, start, end, 1, 0, 0, 1);

        if (foundExact) {
            drawBlockOfCenter(matrixStack, buffer, cameraPos, end, 0, 1, 0, 1);
        }


        // Clean up


        if (buffer.isBuilding()) {
            tessellator.draw();
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    private static void drawLine(MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d cameraPos, Vec3d start, Vec3d end, float r, float g, float b, float a) {
        putVertex(matrixStack, vertexConsumer, cameraPos, start, r, g, b, a);
        putVertex(matrixStack, vertexConsumer, cameraPos,   end, r, g, b, a);
    }

    private static void drawCube(MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d cameraPos, Vec3d start, Vec3d end, float r, float g, float b, float a) {
        drawLine(matrixStack, vertexConsumer, cameraPos, start, start.withAxis(Direction.Axis.X, end.getX()), r, g, b, a);
        drawLine(matrixStack, vertexConsumer, cameraPos, start, start.withAxis(Direction.Axis.Y, end.getY()), r, g, b, a);
        drawLine(matrixStack, vertexConsumer, cameraPos, start, start.withAxis(Direction.Axis.Z, end.getZ()), r, g, b, a);

        drawLine(matrixStack, vertexConsumer, cameraPos,   end, end.withAxis(Direction.Axis.X, start.getX()), r, g, b, a);
        drawLine(matrixStack, vertexConsumer, cameraPos,   end, end.withAxis(Direction.Axis.Y, start.getY()), r, g, b, a);
        drawLine(matrixStack, vertexConsumer, cameraPos,   end, end.withAxis(Direction.Axis.Z, start.getZ()), r, g, b, a);

        drawLine(matrixStack, vertexConsumer, cameraPos, start.withAxis(Direction.Axis.Y, end.getY()), end.withAxis(Direction.Axis.X, start.getX()), r, g, b, a);
        drawLine(matrixStack, vertexConsumer, cameraPos, start.withAxis(Direction.Axis.Y, end.getY()), end.withAxis(Direction.Axis.Z, start.getZ()), r, g, b, a);

        drawLine(matrixStack, vertexConsumer, cameraPos, end.withAxis(Direction.Axis.Y, start.getY()), start.withAxis(Direction.Axis.X, end.getX()), r, g, b, a);
        drawLine(matrixStack, vertexConsumer, cameraPos, end.withAxis(Direction.Axis.Y, start.getY()), start.withAxis(Direction.Axis.Z, end.getZ()), r, g, b, a);

        drawLine(matrixStack, vertexConsumer, cameraPos, start.withAxis(Direction.Axis.X, end.getX()), end.withAxis(Direction.Axis.Z, start.getZ()), r, g, b, a);
        drawLine(matrixStack, vertexConsumer, cameraPos, start.withAxis(Direction.Axis.Z, end.getZ()), end.withAxis(Direction.Axis.X, start.getX()), r, g, b, a);
    }

    private static void drawBlockOfCenter(MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d cameraPos, Vec3d center, float r, float g, float b, float a) {
        drawCube(matrixStack, vertexConsumer, cameraPos, center.add(-.5, -.5, -.5), center.add(.5, .5, .5), r, g, b, a);
    }

    private static void putVertex(MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d cameraPos, Vec3d pos, float r, float g, float b, float a) {
        vertexConsumer.vertex(
                matrixStack.peek().getPositionMatrix(),
                (float) (pos.x - cameraPos.x),
                (float) (pos.y - cameraPos.y),
                (float) (pos.z - cameraPos.z)
        ).color(r, g, b, a).next();
    }

    private static float getDistance(int x1, int y1, int x2, int y2) {
        int i = x2 - x1;
        int j = y2 - y1;
        return MathHelper.sqrt((float)(i * i + j * j));
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}
