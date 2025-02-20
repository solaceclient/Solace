package huysuh.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class RenderUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawOutline(float left, float top, float right, float bottom, float width, float color) {
        drawOutline((int) left, (int) top, (int) right, (int) bottom, (int) width, (int) color);
    }

    public static void drawRect(float left, float top, float right, float bottom, float color) {
        Gui.drawRect((int) left, (int) top, (int) right, (int) bottom, (int) color);
    }

    public static void drawOutline(int left, int top, int right, int bottom, int width, int color) {
        Gui.drawRect(left, top, right, top + width, color); // Top
        Gui.drawRect(left, bottom - width, right, bottom, color); // Bottom
        Gui.drawRect(left, top + width, left + width, bottom - width, color); // Left
        Gui.drawRect(right - width, top + width, right, bottom - width, color); // Right
    }

    public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float alpha1 = (float)(startColor >> 24 & 255) / 255.0F;
        float red1 = (float)(startColor >> 16 & 255) / 255.0F;
        float green1 = (float)(startColor >> 8 & 255) / 255.0F;
        float blue1 = (float)(startColor & 255) / 255.0F;

        float alpha2 = (float)(endColor >> 24 & 255) / 255.0F;
        float red2 = (float)(endColor >> 16 & 255) / 255.0F;
        float green2 = (float)(endColor >> 8 & 255) / 255.0F;
        float blue2 = (float)(endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(red1, green1, blue1, alpha1);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glColor4f(red2, green2, blue2, alpha2);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glEnd();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}