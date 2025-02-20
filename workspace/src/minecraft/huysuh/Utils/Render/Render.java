package huysuh.Utils.Render;

import huysuh.Events.impl.EventRender3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Render {
    public static void drawBoundingBox(Entity entity, Color color) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glColor4d(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.5F);

        Minecraft.getMinecraft().getRenderManager();
        RenderGlobal.drawSelectionBoundingBox(
                new AxisAlignedBB(
                        entity.getEntityBoundingBox().minX
                                - 0.25
                                - entity.posX
                                + (entity.posX - Minecraft.getMinecraft().getRenderManager().renderPosX),
                        entity.getEntityBoundingBox().minY
                                - 0.25
                                - entity.posY
                                + (entity.posY - Minecraft.getMinecraft().getRenderManager().renderPosY),
                        entity.getEntityBoundingBox().minZ
                                - 0.25
                                - entity.posZ
                                + (entity.posZ - Minecraft.getMinecraft().getRenderManager().renderPosZ),
                        entity.getEntityBoundingBox().maxX
                                + 0.25
                                - entity.posX
                                + (entity.posX - Minecraft.getMinecraft().getRenderManager().renderPosX),
                        entity.getEntityBoundingBox().maxY
                                + 0.25
                                - entity.posY
                                + (entity.posY - Minecraft.getMinecraft().getRenderManager().renderPosY),
                        entity.getEntityBoundingBox().maxZ
                                + 0.25
                                - entity.posZ
                                + (entity.posZ - Minecraft.getMinecraft().getRenderManager().renderPosZ)));

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

        private final static Minecraft mc = Minecraft.getMinecraft();
        private final static Frustum frustrum = new Frustum();
        private final static IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
        private final static FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
        private final static FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
        public static double interpolate(double current, double old, double scale) {
            return old + (current - old) * scale;
        }

        public static void renderTag(String name, double pX, double pY, double pZ, int color, String status) {
            float scale = (float) (mc.thePlayer.getDistance(pX + mc.getRenderManager().getRenderPosX(), pY + mc.getRenderManager().getRenderPosY(), pZ + mc.getRenderManager().getRenderPosZ()) * 0.22D);
            //float scale = 1;
            if (scale < 2.65F) {
                scale = 2.65F;
            }
            if (scale > 20F) {
                scale = 20F;
            }
            scale /= 50;
            GL11.glPushMatrix();
            GL11.glTranslatef((float) pX, (float) pY + 1.4F, (float) pZ);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-scale, -scale, scale);
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);

            int width = mc.fontRendererObj.getStringWidth(name) / 2;
            GL11.glPushMatrix();
            GL11.glPopMatrix();
            GL11.glColor4f(1, 1, 1, 1);
            int color2 = 0x50000000;
            switch (status){
                case "Friend":
                    color2 = 0x60006000;
                    break;
                case "Enemy":
                    color2 = 0x60600000;
                    break;
            }
            int x = -(width / 2) - 1;
            Gui.drawRect(x, -(mc.fontRendererObj.FONT_HEIGHT), x + (mc.fontRendererObj.getStringWidth(name) / 2), -(mc.fontRendererObj.FONT_HEIGHT - 1) + 5, color2);
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            mc.fontRendererObj.drawStringWithShadow(name, -(width) , -(mc.fontRendererObj.FONT_HEIGHT + 7), color);
            GL11.glScalef(1f, 1f, 1f);
            GlStateManager.enableTexture2D();
            GL11.glDisable(3042);
            //GL11.glEnable(2896);
            GL11.glEnable(2929);
            GL11.glPopMatrix();
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }

        public static void drawTexturedModalRect(final int x, final int y, final int u, final int v, final int width, final int height, final float zLevel) {
            final float var7 = 0.00390625f;
            final float var8 = 0.00390625f;
            final Tessellator tessellator = Tessellator.getInstance();
            final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(x, y + height, zLevel).tex(u * var7, (v + height) * var8).endVertex();
            worldRenderer.pos(x + width, y + height, zLevel).tex((u + width) * var7, (v + height) * var8).endVertex();
            worldRenderer.pos(x + width, y, zLevel).tex((u + width) * var7, (v) * var8).endVertex();
            worldRenderer.pos(x, y, zLevel).tex(u * var7, v * var8).endVertex();
            tessellator.draw();
        }

        public static ScaledResolution getResolution() {
            return new ScaledResolution(mc);
        }

        public static Vec3 to2D(double x, double y, double z) {
            FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
            IntBuffer viewport = BufferUtils.createIntBuffer(16);
            FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
            FloatBuffer projection = BufferUtils.createFloatBuffer(16);

            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
            GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

            boolean result = GLU.gluProject((float) x, (float) y, (float) z, modelView, projection, viewport, screenCoords);
            if (result) {
                return new Vec3(screenCoords.get(0), Display.getHeight() - screenCoords.get(1), screenCoords.get(2));
            }
            return null;
        }

        public static void drawArrow(float x, float y,boolean up, int hexColor) {
            GL11.glPushMatrix();
            GL11.glScaled(1.3, 1.3, 1.3);

            x /= 1.3;
            y /= 1.3;
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            hexColor(hexColor);
            GL11.glLineWidth(2);

            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(x, y + (up ? 4:0));
            GL11.glVertex2d(x + 3, y + (up ? 0:4));
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(x + 3, y + (up ? 0:4));
            GL11.glVertex2d(x + 6, y+ (up ? 4:0));
            GL11.glEnd();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glPopMatrix();
        }
        public static int darker(int color) {
            Color c = new Color(color);
            float factor = 0.7f;
            return new Color(
                    Math.max((int)(c.getRed() * factor), 0),
                    Math.max((int)(c.getGreen() * factor), 0),
                    Math.max((int)(c.getBlue() * factor), 0),
                    c.getAlpha()
            ).getRGB();
        }

    /**
     * Get color based on percentage (green to red gradient)
     */
    public static int getColorFromPercentage(float percentage) {
        float r = percentage < 0.5 ? 1.0f : 1.0f - 2.0f * (percentage - 0.5f);
        float g = percentage > 0.5 ? 1.0f : 2.0f * percentage;

        return new Color(r, g, 0).getRGB();
    }

    /**
     * Fade between two colors for pulsing effect
     */
    public static int fadeBetween(int color1, int color2) {
        long currentTime = System.currentTimeMillis();
        double time = (currentTime % 2000) / 1000.0;

        // Sine wave oscillation between 0 and 1
        double factor = 0.5 + 0.5 * Math.sin(Math.PI * time);

        Color c1 = new Color(color1);
        Color c2 = new Color(color2);

        int r = (int) (c1.getRed() * factor + c2.getRed() * (1 - factor));
        int g = (int) (c1.getGreen() * factor + c2.getGreen() * (1 - factor));
        int b = (int) (c1.getBlue() * factor + c2.getBlue() * (1 - factor));

        return new Color(r, g, b).getRGB();
    }

        public static void drawTracerPointer(float x, float y, float size, float widthDiv, float heightDiv, int color) {
            boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);

            GL11.glPushMatrix();
            hexColor(color);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x - size / widthDiv, y + size);
            GL11.glVertex2d(x, y + size / heightDiv);
            GL11.glVertex2d(x + size / widthDiv, y + size);
            GL11.glVertex2d(x, y);
            GL11.glEnd();
            GL11.glColor4f(0, 0, 0, 0.8f);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x - size / widthDiv, y + size);
            GL11.glVertex2d(x, y + size / heightDiv);
            GL11.glVertex2d(x + size / widthDiv, y + size);
            GL11.glVertex2d(x, y);
            GL11.glEnd();
            GL11.glPopMatrix();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            if (!blend)
                GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }


        public static void hexColor(int hexColor) {
            float red = (hexColor >> 16 & 0xFF) / 255.0F;
            float green = (hexColor >> 8 & 0xFF) / 255.0F;
            float blue = (hexColor & 0xFF) / 255.0F;
            float alpha = (hexColor >> 24 & 0xFF) / 255.0F;
            GL11.glColor4f(red, green, blue, alpha);
        }

        public static void drawBlockESP(AxisAlignedBB bb, float red, float green, float blue, float alpha, float width) {
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(3553);
            GL11.glEnable(2848);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
            Render.drawBoundingBox(bb);
            GL11.glLineWidth(width);
            GL11.glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
            //Render.drawOutlinedBoundingBox(bb);
            GL11.glDisable(2848);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }

    public static void draw69GodESP(Entity target, Color color, float alpha, boolean outline, EventRender3D e) {
        double width = target.width / 2.5;
        double height = target.height / 4;
        final double x = interpolate(target.posX, target.lastTickPosX, e.getPartialTicks());
        final double y = interpolate(target.posY + 2, target.lastTickPosY + 2, e.getPartialTicks());
        final double z = interpolate(target.posZ, target.lastTickPosZ, e.getPartialTicks());

        double x1 = x - mc.getRenderManager().getRenderPosX();
        double y1 = y + target.height + 0.1 - target.height - mc.getRenderManager().getRenderPosY();
        double z1 = z - mc.getRenderManager().getRenderPosZ();

        double size = 0.17;

        GL11.glPushMatrix();
        GLUtil.setGLCap(3042, true);
        GLUtil.setGLCap(3553, false);
        GLUtil.setGLCap(2896, false);
        GLUtil.setGLCap(2929, false);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1.8f);
        GL11.glBlendFunc(770, 771);
        GLUtil.setGLCap(2848, true);
        GL11.glDepthMask(true);
        Render.BB(new AxisAlignedBB(x1 - (width + size), y1, z1 - (width + size), x1 + (width + size), y1 + height, z1 + (width + size)), new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (alpha / 255f)).getRGB());
        if (outline) {
            Render.OutlinedBB(new AxisAlignedBB(x1 - (width + size), y1, z1 - (width + size), x1 + (width + size), y1 + height, z1 + (width + size)), 1, new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (alpha / 255f)).getRGB());
        }
        GLUtil.revertAllCaps();
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
    }

    public static void drawRotatingEntityESP(Entity target, Color color, float alpha, boolean outline, EventRender3D e) {
        double width = target.width / 2.0;
        double height = target.height;
        final double x = interpolate(target.posX, target.lastTickPosX, e.getPartialTicks());
        final double y = interpolate(target.posY, target.lastTickPosY, e.getPartialTicks());
        final double z = interpolate(target.posZ, target.lastTickPosZ, e.getPartialTicks());

        double x1 = x - mc.getRenderManager().getRenderPosX();
        double y1 = y + target.height + 0.1 - target.height - mc.getRenderManager().getRenderPosY();
        double z1 = z - mc.getRenderManager().getRenderPosZ();

        double size = 0.17;

        GL11.glPushMatrix();
        GLUtil.setGLCap(3042, true);
        GLUtil.setGLCap(3553, false);
        GLUtil.setGLCap(2896, false);
        GLUtil.setGLCap(2929, false);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1.8f);
        GL11.glBlendFunc(770, 771);
        GLUtil.setGLCap(2848, true);
        GL11.glDepthMask(true);

        GL11.glTranslated(x1, y1, z1);
        GL11.glRotatef(-target.rotationYaw, 0.0F, 1.0F, 0.0F);


        Render.BB(new AxisAlignedBB(-width - size, 0, -width - size, width + size, height, width + size), new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha).getRGB());
        if (outline) {
            Render.OutlinedBB(new AxisAlignedBB(-width - size, 0, -width - size, width + size, height, width + size), 1, new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, Math.max(0.0f, Math.min(1.0f, alpha + 0.4f))).getRGB());
        }

        GL11.glPopMatrix();
        GLUtil.revertAllCaps();
        GL11.glColor4f(1, 1, 1, 1);
    }


    public static void drawEntityESP(Entity target, Color color, float alpha, boolean outline, EventRender3D e) {
            double width = target.width / 2.0;
            double height = target.height;
            final double x = interpolate(target.posX, target.lastTickPosX, e.getPartialTicks());
            final double y = interpolate(target.posY, target.lastTickPosY, e.getPartialTicks());
            final double z = interpolate(target.posZ, target.lastTickPosZ, e.getPartialTicks());

            double x1 = x - mc.getRenderManager().getRenderPosX();
            double y1 = y + target.height + 0.1 - target.height - mc.getRenderManager().getRenderPosY();
            double z1 = z - mc.getRenderManager().getRenderPosZ();

            double size = 0.17;

            GL11.glPushMatrix();
            GLUtil.setGLCap(3042, true);
            GLUtil.setGLCap(3553, false);
            GLUtil.setGLCap(2896, false);
            GLUtil.setGLCap(2929, false);
            GL11.glDepthMask(false);
            GL11.glLineWidth(1.8f);
            GL11.glBlendFunc(770, 771);
            GLUtil.setGLCap(2848, true);
            GL11.glDepthMask(true);
            Render.BB(new AxisAlignedBB(x1 - (width + size), y1, z1 - (width + size), x1 + (width + size), y1 + height, z1 + (width + size)), new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (alpha / 255f)).getRGB());
            if (outline) {
                Render.OutlinedBB(new AxisAlignedBB(x1 - (width + size), y1, z1 - (width + size), x1 + (width + size), y1 + height, z1 + (width + size)), 1, new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (alpha / 255f)).getRGB());
            }
            GLUtil.revertAllCaps();
            GL11.glPopMatrix();
            GL11.glColor4f(1, 1, 1, 1);
        }


        public static int getRainbow(int speed, int offset, float s) {
            float hue = (System.currentTimeMillis() + offset) % speed;
            hue /= speed;
            return Color.getHSBColor(hue, s, 1f).getRGB();

        }

        public static float[] getRGBAs(int rgb) {
            return new float[]{((rgb >> 16) & 255) / 255F, ((rgb >> 8) & 255) / 255F, (rgb & 255) / 255F, ((rgb >> 24) & 255) / 255F};
        }

        public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
            GL11.glColor4f(1, 1, 1, 1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(image);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        }

        public static void drawCircle(float x, float y, float r, int c) {
            float f = (c >> 24 & 0xFF) / 255.0f;
            float f2 = (c >> 16 & 0xFF) / 255.0f;
            float f3 = (c >> 8 & 0xFF) / 255.0f;
            float f4 = (c & 0xFF) / 255.0f;
            GL11.glPushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GL11.glEnable(2848);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glColor4f(f2, f3, f4, f);
            GL11.glBegin(6);
            for (int i = 0; i <= 360; ++i) {
                double x2 = Math.sin(i * Math.PI / 180.0) * (r / 2);
                double y2 = Math.cos(i * Math.PI / 180.0) * (r / 2);
                GL11.glVertex2d(x + r / 2 + x2, y + r / 2 + y2);
            }
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (int i = 0; i <= 360; ++i) {
                double x2 = Math.sin(i * Math.PI / 180.0) * ((r / 2));
                double y2 = Math.cos(i * Math.PI / 180.0) * ((r / 2) );
                GL11.glVertex2d(x + ((r / 2)) + x2, y + ((r / 2)) + y2);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GL11.glPopMatrix();
        }

        public static void drawUnfilledCircle(float x, float y, float r, int c) {
            float f = (c >> 24 & 0xFF) / 255.0f;
            float f2 = (c >> 16 & 0xFF) / 255.0f;
            float f3 = (c >> 8 & 0xFF) / 255.0f;
            float f4 = (c & 0xFF) / 255.0f;
            GL11.glPushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glEnable(2848);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glColor4f(f2, f3, f4, f);
            GL11.glLineWidth(1);
            GL11.glBegin(2);
            for (int i = 0; i <= 360; ++i) {
                double x2 = Math.sin(i * Math.PI / 180.0) * (r / 2);
                double y2 = Math.cos(i * Math.PI / 180.0) * (r / 2);
                GL11.glVertex2d(x + r / 2 + x2, y + r / 2 + y2);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
            GL11.glEnable(3553);
            GL11.glDisable(3042);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GL11.glPopMatrix();
        }

        public static void OutlinedBB(AxisAlignedBB bb, float width, int color) {
            enable3D();
            glLineWidth(width);
            color(color);
            drawOutlinedBoundingBox(bb);
            disable3D();
        }

        public static void BB(AxisAlignedBB bb, int color) {
            enable3D();
            color(color);
            drawBoundingBox(bb);
            disable3D();
        }

        public static void enable3D() {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_TEXTURE_2D);
            glEnable(GL_LINE_SMOOTH);
            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
        }

        public static void disable3D() {
            glDisable(GL_LINE_SMOOTH);
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true);
            glDisable(GL_BLEND);
        }

        public static void color(int color) {
            GL11.glColor4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, (color >> 24 & 0xFF) / 255f);
        }

        public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            worldRenderer.begin(3, DefaultVertexFormats.POSITION);
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            tessellator.draw();
            worldRenderer.begin(3, DefaultVertexFormats.POSITION);
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            tessellator.draw();
            worldRenderer.begin(1, DefaultVertexFormats.POSITION);
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
            tessellator.draw();

        }

        public static void drawBoundingBox(AxisAlignedBB aa) {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
            tessellator.draw();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
            worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
            tessellator.draw();
        }

        public static Vector3d project(double x, double y, double z) {
            FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);
            GL11.glGetFloat(2982, modelview);
            GL11.glGetFloat(2983, projection);
            GL11.glGetInteger(2978, viewport);
            if (GLU.gluProject((float) x, (float) y, (float) z, modelview, projection, viewport, vector)) {
                return new Vector3d(vector.get(0) / getResolution().getScaleFactor(), (Display.getHeight() - vector.get(1)) / getResolution().getScaleFactor(), vector.get(2));
            }
            return null;
        }


        public static void drawCheckMark(float x, float y, int width, int color) {
            float f = (color >> 24 & 255) / 255.0f;
            float f1 = (color >> 16 & 255) / 255.0f;
            float f2 = (color >> 8 & 255) / 255.0f;
            float f3 = (color & 255) / 255.0f;
            GL11.glPushMatrix();
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(3553);
            glEnable(2848);
            glBlendFunc(770, 771);
            GL11.glLineWidth(1.5f);
            GL11.glBegin(3);
            GL11.glColor4f(f1, f2, f3, f);
            GL11.glVertex2d(x + width - 6.5, y + 3);
            GL11.glVertex2d(x + width - 11.5, y + 10);
            GL11.glVertex2d(x + width - 13.5, y + 8);
            GL11.glEnd();
            glEnable(3553);
            glDisable(GL_BLEND);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        public static boolean isInViewFrustrum(Entity entity) {
            return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
        }

        public static boolean isInViewFrustrum(AxisAlignedBB bb) {
            Entity current = Minecraft.getMinecraft().getRenderViewEntity();
            frustrum.setPosition(current.posX, current.posY, current.posZ);
            return frustrum.isBoundingBoxInFrustum(bb);
        }

        public static void prepareScissorBox(ScaledResolution sr, float x, float y, float width, float height) {
            float x2 = x + width;
            float y2 = y + height;
            int factor = sr.getScaleFactor();
            GL11.glScissor((int) (x * factor), (int) ((sr.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
        }

        public static void drawBorderedRect(int x, int y, int width, int height, int lineSize, int borderColor, int color) {
            Gui.drawRect(x, y, x + width, y + height, color);
            Gui.drawRect(x, y, x + width, y + lineSize, borderColor);
            Gui.drawRect(x, y, x + lineSize, y + height, borderColor);
            Gui.drawRect(x + width, y, x + width - lineSize, y + height, borderColor);
            Gui.drawRect(x, y + height, x + width, y + height - lineSize, borderColor);
        }

        public static void drawCornerRect(int x, int y, int width, int height, int thickness, int hex, boolean border, int borderwidth) {
            final int w = width / 4;
            final int h = height / 4;
            // Horizontals
            drawRect(x, y, w + (border ? borderwidth : 0), thickness, hex);
            drawRect(x + width - (w + (border ? borderwidth : 0)), y, w, thickness, hex);
            drawRect(x, y + height - thickness, w + (border ? borderwidth : 0), thickness, hex);
            drawRect(x + width - (w + (border ? borderwidth : 0)), y + height - thickness, w, thickness, hex);
            //Verticals
            drawRect(x, y, thickness, h + (border ? borderwidth : 0), hex);
            drawRect(x + width - thickness, y, thickness, h + (border ? borderwidth : 0), hex);
            int y1 = y + height - (h + (border ? borderwidth : 0));
            drawRect(x, y + height - (h + (border ? borderwidth : 0)), thickness, h, hex);
            drawRect(x + width - thickness, y + height - (h + (border ? borderwidth : 0)), thickness, h, hex);
        }

        public static void drawBordered(int x, int y, int x2, int y2, int thickness, int inside, int outline) {
            double fix = 0.0;
            if (thickness < 1.0) {
                fix = 1.0;
            }
            drawRect2(x + thickness, y + thickness, x2 - thickness, y2 - thickness, inside);
            drawRect2(x, (int) (y + 1.0 - fix), x + thickness, y2, outline);
            drawRect2(x, y, (int) (x2 - 1.0 + fix), y + thickness, outline);
            drawRect2(x2 - thickness, y, x2, (int) (y2 - 1.0 + fix), outline);
            drawRect2((int) (x + 1.0 - fix), y2 - thickness, x2, y2, outline);
        }

        public static void drawBar(int x, int y, int width, int height, int max, float value, int color) {
            float f = (color >> 24 & 0xFF) / 255.0F;
            float f1 = (color >> 16 & 0xFF) / 255.0F;
            float f2 = (color >> 8 & 0xFF) / 255.0F;
            float f3 = (color & 0xFF) / 255.0F;
            final int inc = (height / max);
            GL11.glColor4f(f1, f2, f3, f);
            drawBorderedRect(x, y, width, height, 1, 0xff000000, 0x00000000);
            int incY = y + height - inc;
            for (int i = 0; i < value; i++) {
                drawBorderedRect(x + 1, incY, width - 1, inc, 1, 0xff000000, color);
                incY -= inc;
            }
        }

        public static void drawRect(int x, int y, int width, int height, int color) {
            float f = (color >> 24 & 0xFF) / 255.0F;
            float f1 = (color >> 16 & 0xFF) / 255.0F;
            float f2 = (color >> 8 & 0xFF) / 255.0F;
            float f3 = (color & 0xFF) / 255.0F;
            GL11.glColor4f(f1, f2, f3, f);
            Gui.drawRect(x, y, x + width, y + height, color);
        }

        public static void drawRect2(int x, int y, int x2, int y2, int color) {
            float f = (color >> 24 & 0xFF) / 255.0F;
            float f1 = (color >> 16 & 0xFF) / 255.0F;
            float f2 = (color >> 8 & 0xFF) / 255.0F;
            float f3 = (color & 0xFF) / 255.0F;
            GL11.glColor4f(f1, f2, f3, f);
            Gui.drawRect(x, y, x2, y2, color);
        }


        public static void drawBorderedRoundedRect(float x, float y, float width, float height, float radius, float linewidth, int insideC, int borderC) {
            drawRoundedRect(x, y, width, height, radius, borderC);
            drawOutlinedRoundedRect(x, y, width, height, radius, linewidth, insideC);
        }

        public static void drawRoundedRectWithShadow(double x, double y, double width, double height, double radius, int color) {
            drawRoundedRect(x + 2, y + 1, width, height + 1, radius, new Color(0).getRGB());
            drawRoundedRect(x, y, width, height, radius, color);
        }

        public static void drawOutlinedRoundedRect(double x, double y, double width, double height, double radius, float linewidth, int color) {
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            double x1 = x + width;
            double y1 = y + height;
            float f = (color >> 24 & 0xFF) / 255.0F;
            float f1 = (color >> 16 & 0xFF) / 255.0F;
            float f2 = (color >> 8 & 0xFF) / 255.0F;
            float f3 = (color & 0xFF) / 255.0F;
            GL11.glPushAttrib(0);
            GL11.glScaled(0.5, 0.5, 0.5);

            x *= 2;
            y *= 2;
            x1 *= 2;
            y1 *= 2;
            GL11.glLineWidth(linewidth);

            glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(f1, f2, f3, f);
            glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glBegin(2);

            for (int i = 0; i <= 90; i += 3) {
                GL11.glVertex2d(x + radius + +(Math.sin((i * Math.PI / 180)) * (radius * -1)), y + radius + (Math.cos((i * Math.PI / 180)) * (radius * -1)));
            }

            for (int i = 90; i <= 180; i += 3) {
                GL11.glVertex2d(x + radius + (Math.sin((i * Math.PI / 180)) * (radius * -1)), y1 - radius + (Math.cos((i * Math.PI / 180)) * (radius * -1)));
            }

            for (int i = 0; i <= 90; i += 3) {
                GL11.glVertex2d(x1 - radius + (Math.sin((i * Math.PI / 180)) * radius), y1 - radius + (Math.cos((i * Math.PI / 180)) * radius));
            }

            for (int i = 90; i <= 180; i += 3) {
                GL11.glVertex2d(x1 - radius + (Math.sin((i * Math.PI / 180)) * radius), y + radius + (Math.cos((i * Math.PI / 180)) * radius));
            }

            GL11.glEnd();

            glEnable(GL11.GL_TEXTURE_2D);
            glDisable(GL11.GL_LINE_SMOOTH);
            glEnable(GL11.GL_TEXTURE_2D);

            GL11.glScaled(2, 2, 2);

            GL11.glPopAttrib();
            GL11.glColor4f(1, 1, 1, 1);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();

        }

        public static void blockESPBox(BlockPos blockPos) {

            double x =
                    blockPos.getX()
                            - Minecraft.getMinecraft().getRenderManager().renderPosX;
            double y =
                    blockPos.getY()
                            - Minecraft.getMinecraft().getRenderManager().renderPosY;
            double z =
                    blockPos.getZ()
                            - Minecraft.getMinecraft().getRenderManager().renderPosZ;

            GL11.glBlendFunc(770, 771);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glLineWidth(2.0F);
            GL11.glColor4d(0, 0, 1, 0.15F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);

            //Box
            GL11.glColor4d(0, 0, 1, 0.5F);
            RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);



        }

        public static void drawRoundedRect(double x, double y, double width, double height, double radius, int color) {
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            double x1 = x + width;
            double y1 = y + height;
            float f = (color >> 24 & 0xFF) / 255.0F;
            float f1 = (color >> 16 & 0xFF) / 255.0F;
            float f2 = (color >> 8 & 0xFF) / 255.0F;
            float f3 = (color & 0xFF) / 255.0F;
            GL11.glPushAttrib(0);
            GL11.glScaled(0.5, 0.5, 0.5);

            x *= 2;
            y *= 2;
            x1 *= 2;
            y1 *= 2;

            glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(f1, f2, f3, f);
            glEnable(GL11.GL_LINE_SMOOTH);

            GL11.glBegin(GL11.GL_POLYGON);

            for (int i = 0; i <= 90; i += 3) {
                GL11.glVertex2d(x + radius + +(Math.sin((i * Math.PI / 180)) * (radius * -1)), y + radius + (Math.cos((i * Math.PI / 180)) * (radius * -1)));
            }

            for (int i = 90; i <= 180; i += 3) {
                GL11.glVertex2d(x + radius + (Math.sin((i * Math.PI / 180)) * (radius * -1)), y1 - radius + (Math.cos((i * Math.PI / 180)) * (radius * -1)));
            }

            for (int i = 0; i <= 90; i += 3) {
                GL11.glVertex2d(x1 - radius + (Math.sin((i * Math.PI / 180)) * radius), y1 - radius + (Math.cos((i * Math.PI / 180)) * radius));
            }

            for (int i = 90; i <= 180; i += 3) {
                GL11.glVertex2d(x1 - radius + (Math.sin((i * Math.PI / 180)) * radius), y + radius + (Math.cos((i * Math.PI / 180)) * radius));
            }

            GL11.glEnd();

            glEnable(GL11.GL_TEXTURE_2D);
            glDisable(GL11.GL_LINE_SMOOTH);
            glEnable(GL11.GL_TEXTURE_2D);

            GL11.glScaled(2, 2, 2);

            GL11.glPopAttrib();
            GL11.glColor4f(1, 1, 1, 1);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();

        }
}