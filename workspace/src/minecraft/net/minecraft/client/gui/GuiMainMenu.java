package net.minecraft.client.gui;

import huysuh.Font.Fonts;
import huysuh.Modules.Module;
import huysuh.Modules.impl.Render.HUD;
import huysuh.UI.GuiAlts;
import huysuh.Utils.RainbowUtil;

import java.awt.*;
import java.io.IOException;

import huysuh.Utils.Render.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class GuiMainMenu extends GuiScreen {
    // Animation variables
    private float animationTime = 0;
    private RainbowUtil rainbowUtil = new RainbowUtil(3.0f, 0.7f, 1.0f);

    // Button positions
    private int buttonWidth = 120;
    private int buttonHeight = 20;
    private int buttonSpacing = 4;
    private int baseButtonY;

    // Color constants
    private static final int BLACK = 0xFF000000;
    private static final int DARK_GRAY = 0xFF101010;
    private static final int GREEN = new Color(124, 194, 91).getRGB();
    private static final int DARK_GREEN = new Color(60, 92, 44).getRGB();
    private static final int TEXT_COLOR = ((HUD)(Module.getModuleFromString("HUD"))).getAccentColor(0);

    // Grid settings
    private float gridSize = 25;
    private float scrollSpeed = 0.1f;
    private float gridAlpha = 0.07f;

    @Override
    public void initGui() {
        baseButtonY = this.height / 2 - 20;
        this.buttonList.clear();
        this.addMainButtons();
    }

    private void addMainButtons() {
        int yPos = baseButtonY;

        // Main menu buttons
        this.buttonList.add(new ModernButton(1, this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight, "Singleplayer"));
        yPos += buttonHeight + buttonSpacing;

        this.buttonList.add(new ModernButton(2, this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight, "Multiplayer"));
        yPos += buttonHeight + buttonSpacing;

        this.buttonList.add(new ModernButton(0, this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight, "Options"));
        yPos += buttonHeight + buttonSpacing;

        this.buttonList.add(new ModernButton(4, this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight, "Alt Manager"));
    }

    @Override
    public void updateScreen() {
        animationTime += 0.01F;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 4) {
            this.mc.displayGuiScreen(new GuiAlts(this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Base background
        drawRect(0, 0, this.width, this.height, BLACK);

        // Ambient background
        drawAnimatedBackground(partialTicks);

        // Version and credits
        drawVersionInfo();

        // Draw buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawAnimatedBackground(float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        // Calculate grid offset based on animation time
        float offset = (animationTime * scrollSpeed) % gridSize;

        // Draw grid lines with fading effect
        GlStateManager.color(0.0F, 0.5F, 0.1F, gridAlpha);

        // Horizontal lines
        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        for (float y = offset; y < this.height; y += gridSize) {
            worldrenderer.pos(0, y, 0).endVertex();
            worldrenderer.pos(this.width, y, 0).endVertex();
        }
        tessellator.draw();

        // Vertical lines
        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        for (float x = offset; x < this.width; x += gridSize) {
            worldrenderer.pos(x, 0, 0).endVertex();
            worldrenderer.pos(x, this.height, 0).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawVersionInfo() {
        // Version text in bottom left
        String version = "1.8.9";
        Fonts.Verdana.drawStringWithShadow(version, 5, this.height - 12, TEXT_COLOR);

        // Credits text in bottom right
        String credits = "by huys & heart";
        Fonts.Verdana.drawStringWithShadow(credits,
                this.width - Fonts.Verdana.getStringWidth(credits) - 5,
                this.height - 12, TEXT_COLOR);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public class ModernButton extends GuiButton {
        private float hoverAnimation = 0;
        private boolean wasHovered = false;
        private float outlineAlpha = 0.0f;

        public ModernButton(int buttonId, int x, int y, int width, int height, String buttonText) {
            super(buttonId, x, y, width, height, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                        mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

                // Smooth hover transition
                if (this.hovered && !wasHovered) {
                    outlineAlpha = 0.0f;
                }

                if (this.hovered) {
                    hoverAnimation = Math.min(1.0F, hoverAnimation + 0.08F);
                    outlineAlpha = Math.min(1.0F, outlineAlpha + 0.2F);
                } else {
                    hoverAnimation = Math.max(0.0F, hoverAnimation - 0.08F);
                    outlineAlpha = Math.max(0.0F, outlineAlpha - 0.2F);
                }

                wasHovered = this.hovered;

                // Draw button background
                Render.drawBorderedGradientRect(this.xPosition, this.yPosition, this.width,
                        this.height, 1, 0xFF121218, this.hovered ? 0xFF28282c : 0xFF18181c, this.hovered ? 0xFF44444b : 0xFF24242b, true);

                // Rainbow outline when hovered
                if (outlineAlpha > 0) {
                    drawRainbowOutline(this.xPosition, this.yPosition, this.width, this.height, outlineAlpha);
                }

                // Calculate text color
                int textColor = TEXT_COLOR;

                // Center and draw text
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Fonts.Verdana.drawCenteredStringWithShadow(this.displayString,
                        this.xPosition + this.width / 2,
                        this.yPosition + (this.height - 8) / 2, textColor);
            }
        }

        private void drawRainbowOutline(int x, int y, int width, int height, float alpha) {
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            int thickness = 1;
            Color rainbow1 = new Color(((HUD)(Module.getModuleFromString("HUD"))).getAccentColor(0));
            int segColor = new Color(rainbow1.getRed(), rainbow1.getGreen(), rainbow1.getBlue(),
                    (int)(alpha * 255)).getRGB();

            // Draw rainbow segments around the button
            int segments = 8;
            int segLength = width / (segments / 2);


            // Bottom outline with segments
            for (int i = segments / 2 - 1; i >= 0; i--) {

                int x1 = x + (i * segLength);
                int x2 = Math.min(x + width, x1 + segLength);
                drawRect(x1, y + height - thickness, x2, y + height, segColor);
            }


            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
        }
    }
}