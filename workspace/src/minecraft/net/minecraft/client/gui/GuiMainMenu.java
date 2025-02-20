package net.minecraft.client.gui;

import huysuh.Font.Fonts;
import huysuh.UI.GuiAlts;
import huysuh.Utils.RainbowUtil;

import java.awt.*;
import java.io.IOException;
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
    private static final int TEXT_GREEN = new Color(124, 194, 91).getRGB();

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
        this.buttonList.add(new ModernButton(1, this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight, I18n.format("menu.singleplayer")));
        yPos += buttonHeight + buttonSpacing;

        this.buttonList.add(new ModernButton(2, this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight, I18n.format("menu.multiplayer")));
        yPos += buttonHeight + buttonSpacing;

        this.buttonList.add(new ModernButton(0, this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight, I18n.format("menu.options")));
        yPos += buttonHeight + buttonSpacing;

        this.buttonList.add(new ModernButton(4, this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight, "Alt Manager"));
    }

    @Override
    public void updateScreen() {
        animationTime += 0.01F;
        rainbowUtil.update();

        // Update each button's rainbow effect
        for (GuiButton button : this.buttonList) {
            if (button instanceof ModernButton) {
                ((ModernButton) button).updateRainbow();
            }
        }
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

    private void drawRainbowLine(int startX, int endX, int y, int thickness) {
        int segments = 20;
        int segmentWidth = (endX - startX) / segments;

        for (int i = 0; i < segments; i++) {
            int x1 = startX + (i * segmentWidth);
            int x2 = x1 + segmentWidth;

            drawRect(x1, y, x2, y + thickness, rainbowUtil.getRainbow());
        }
    }

    private float[] getColorComponents(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new float[]{r, g, b};
    }

    private void drawVersionInfo() {
        // Version text in bottom left
        String version = "1.8.9";
        Fonts.SF.drawStringWithShadow(version, 5, this.height - 12, TEXT_GREEN);

        // Credits text in bottom right
        String credits = "by huys & heart";
        Fonts.SF.drawStringWithShadow(credits,
                this.width - Fonts.SF.getStringWidth(credits) - 5,
                this.height - 12, TEXT_GREEN);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // Modern button implementation with rainbow effect
    public class ModernButton extends GuiButton {
        private float hoverAnimation = 0;
        private boolean wasHovered = false;
        private float outlineAlpha = 0.0f;
        private RainbowUtil buttonRainbow = new RainbowUtil(10.0f, 0.8f, 1.0f);
        private float currentHue = 0f;

        public ModernButton(int buttonId, int x, int y, int width, int height, String buttonText) {
            super(buttonId, x, y, width, height, buttonText);
            // Set different starting hues for each button to create a nice effect
        }

        public void updateRainbow() {
            buttonRainbow.update();
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
                    outlineAlpha = Math.min(1.0F, outlineAlpha + 0.1F);
                } else {
                    hoverAnimation = Math.max(0.0F, hoverAnimation - 0.08F);
                    outlineAlpha = Math.max(0.0F, outlineAlpha - 0.06F);
                }

                wasHovered = this.hovered;

                // Draw button background
                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width,
                        this.yPosition + this.height, DARK_GRAY);

                // Rainbow outline when hovered
                if (outlineAlpha > 0) {
                    drawRainbowOutline(this.xPosition, this.yPosition, this.width, this.height, outlineAlpha);
                }

                // Calculate text color
                int textColor = TEXT_GREEN;

                // Center and draw text
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Fonts.SF.drawCenteredStringWithShadow(this.displayString,
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
            Color rainbow1 = new Color(buttonRainbow.getRainbow());
            int segColor = new Color(rainbow1.getRed(), rainbow1.getGreen(), rainbow1.getBlue(),
                    (int)(alpha * 255)).getRGB();

            // Draw rainbow segments around the button
            int segments = 8;
            int segLength = width / (segments / 2);

            // Top outline with segments
            for (int i = 0; i < segments / 2; i++) {

                int x1 = x + (i * segLength);
                int x2 = Math.min(x + width, x1 + segLength);
                drawRect(x1, y, x2, y + thickness, segColor);
            }

            // Right outline
            for (int i = 0; i < segments / 4; i++) {

                int y1 = y + (i * (height / (segments/4)));
                int y2 = Math.min(y + height, y1 + (height / (segments/4)));
                drawRect(x + width - thickness, y1, x + width, y2, segColor);
            }

            // Bottom outline with segments
            for (int i = segments / 2 - 1; i >= 0; i--) {

                int x1 = x + (i * segLength);
                int x2 = Math.min(x + width, x1 + segLength);
                drawRect(x1, y + height - thickness, x2, y + height, segColor);
            }

            // Left outline
            for (int i = segments / 4 - 1; i >= 0; i--) {

                int y1 = y + (i * (height / (segments/4)));
                int y2 = Math.min(y + height, y1 + (height / (segments/4)));
                drawRect(x, y1, x + thickness, y2, segColor);
            }

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
        }
    }
}