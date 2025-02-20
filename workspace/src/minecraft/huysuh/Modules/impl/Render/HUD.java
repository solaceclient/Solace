package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventRender2D;
import huysuh.Font.CFontRenderer;
import huysuh.Font.Fonts;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.*;
import huysuh.Solace;
import huysuh.Utils.RainbowUtil;
import huysuh.Utils.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HUD extends Module {
    private final Map<Module, ModulePosition> modulePositions = new HashMap<>();
    private long lastFrameTime;
    private RainbowUtil rainbowUtil = new RainbowUtil(4, 0.5f, 1.0f);
    private float scrollOffset = 0.0f;
    private long startTime = System.currentTimeMillis();

    private static class ModulePosition {
        float x;
        float targetX;
        float y;
        float targetY;
        boolean wasEnabled;
        long enableTime;
        float animationProgress;

        ModulePosition(float x, float targetX, float y, float targetY, boolean wasEnabled) {
            this.x = x;
            this.targetX = targetX;
            this.y = y;
            this.targetY = targetY;
            this.wasEnabled = wasEnabled;
            this.enableTime = System.currentTimeMillis();
            this.animationProgress = 0.0f;
        }
    }

    // Settings
    private final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting moduleList = new BooleanSetting("Module List", true);
    private final BooleanSetting useCustomFont = new BooleanSetting("Custom Font", true);
    private final ModeSetting style = new ModeSetting("Style", "Skeet", "Solid", "Simple", "Legacy");
    private final ModeSetting colorMode = new ModeSetting("Color", "Static", "Rainbow", "Gradient", "Fade");
    private final NumberSetting animSpeed = new NumberSetting("Speed", 200, 50, 500, 10);
    private final NumberSetting spacing = new NumberSetting("Spacing", 1, 0, 3, 0.5);
    private final ColorSetting primaryColor = new ColorSetting("Primary", new Color(124, 194, 91).getRGB());
    private final ColorSetting secondaryColor = new ColorSetting("Secondary", new Color(255, 255, 255).getRGB());
    private final ColorSetting backgroundColor = new ColorSetting("Background", new Color(10, 10, 10, 180).getRGB());
    private final ModeSetting tagStyle = new ModeSetting("Tag Style", "SPACE", "DASH", "PARENTHESIS", "BRACKETS", "NONE");
    private final NumberSetting scrollSpeed = new NumberSetting("Scroll Speed", 30, 5, 100, 5);
    private final NumberSetting colorSpeed = new NumberSetting("Color Speed", 2, 0.5, 10, 0.5);
    private final NumberSetting colorOffset = new NumberSetting("Y Offset", 0.1, 0.01, 0.5, 0.01);

    public HUD() {
        super("HUD", "Customizable heads-up display", Category.RENDER, Keyboard.KEY_P);
        addSettings(watermark, moduleList, useCustomFont, style, colorMode, animSpeed, spacing,
                primaryColor, secondaryColor, backgroundColor, tagStyle, scrollSpeed,
                colorSpeed, colorOffset);
        lastFrameTime = System.nanoTime();
    }

    @Override
    protected void onEnable() {
        rainbowUtil = new RainbowUtil((float)colorSpeed.getValue(), 0.5f, 1.0f);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventRender2D) {
            this.setTag(style.getMode());
            onRender2D((EventRender2D) e);
        }
    }

    private void onRender2D(EventRender2D event) {
        ScaledResolution sr = new ScaledResolution(mc);
        float deltaTime = getDeltaTime();

        rainbowUtil.update(50);

        scrollOffset += deltaTime * (scrollSpeed.getValue() / 100f);
        if (scrollOffset > 1.0f) scrollOffset -= 1.0f;

        if (watermark.isEnabled()) {
            renderWatermark(sr);
        }

        if (moduleList.isEnabled()) {
            renderModuleList(sr, deltaTime);
        }

        lastFrameTime = System.nanoTime();
    }

    private String getModuleName(Module m) {
        return m.getFullName(tagStyle.getMode());
    }

    private float getDeltaTime() {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
        return Math.min(deltaTime, 0.1f);
    }

    private void renderWatermark(ScaledResolution sr) {
        String clientName = Solace.name;
        String version = "beta " + Solace.version;
        float x = 4;
        float y = 4;

        switch (style.getMode()) {
            case "Skeet":
                float nameWidth = getTextWidth(clientName);
                float versionWidth = getTextWidth(" " + version);
                float totalWidth = nameWidth + versionWidth;
                float height = getTextHeight();

                RenderUtil.drawRect(x - 3, y - 3, x + totalWidth + 5, y + height + 3,
                        new Color(12, 12, 12, 220).getRGB());

                int nameRGB = getAccentColor(0);

                drawText(clientName.toLowerCase(), x, y, nameRGB);

                drawText(" " + version, x + nameWidth, y, new Color(170, 170, 170).getRGB());
                break;

            case "Solid":
                float width = Math.max(getTextWidth(clientName + " " + version), getTextWidth(new SimpleDateFormat("HH:mm:ss").format(new Date()))) + 8;
                Gui.drawRect((int)x - 2, (int)y - 2,
                        (int)(x + width), (int)(y + getTextHeight() * 2 + 6),
                        backgroundColor.getColor());

                drawText(clientName, x + 2, y, getAccentColor(0));
                drawText(" " + version, x + 2 + getTextWidth(clientName), y, secondaryColor.getColor());
                drawText(new SimpleDateFormat("HH:mm:ss").format(new Date()), x + 2, y + getTextHeight() + (float)spacing.getValue(), secondaryColor.getColor());
                break;

            default:
                drawText(clientName, x, y, getAccentColor(0));
                drawText(" " + version, x + getTextWidth(clientName), y, secondaryColor.getColor());
                break;
        }
    }

    /**
     * Draws a horizontally scrolling gradient for the top bar of the watermark or module entries
     * @param y1Pos The y-position in the screen for color calculation (for vertical gradients)
     */
    private void drawScrollingTopGradient(float x1, float y1, float x2, float y2, float y1Pos) {
        float width = x2 - x1;

        int segments = 200;

        if (colorMode.getMode().equals("Rainbow")) {

            float segmentWidth = width / segments;

            for (int i = 0; i < segments; i++) {
                float startX = x1 + (i * segmentWidth);
                float endX = startX + segmentWidth;

                float yOffset = y1Pos * (float)colorOffset.getValue();
                float startHue = ((float)i / segments + scrollOffset + yOffset) % 1.0f;
                float endHue = ((float)(i + 1) / segments + scrollOffset + yOffset) % 1.0f;

                Color startColor = Color.getHSBColor(startHue, 0.7f, 1.0f);
                Color endColor = Color.getHSBColor(endHue, 0.7f, 1.0f);

                RenderUtil.drawGradientRect(
                        startX, y1,
                        endX, y2,
                        startColor.getRGB(),
                        endColor.getRGB()
                );
            }
        } else if (colorMode.getMode().equals("Fade")) {
            float scrollPos = scrollOffset * 2;
            float yOffset = y1Pos * (float)colorOffset.getValue();
            float effectivePos = (scrollPos + yOffset) % 1.0f;

            Color baseColor = new Color(primaryColor.getColor());
            float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);

            float brightnessLow = Math.max(0.4f, hsb[2] - 0.3f);
            float brightnessHigh = Math.min(1.0f, hsb[2] + 0.1f);
            Color lightColor = Color.getHSBColor(hsb[0], hsb[1], brightnessHigh);
            Color darkColor = Color.getHSBColor(hsb[0], hsb[1], brightnessLow);

            float gradientWidth = width * 2;
            float gradientPos = effectivePos * gradientWidth;

            float segmentWidth = width / segments;

            for (int i = 0; i < segments; i++) {
                float startX = x1 + (i * segmentWidth);
                float endX = startX + segmentWidth;

                float cyclePos = (float)((effectivePos + (float)i/segments) % 1.0);

                Color startSegColor, endSegColor;
                if (cyclePos < 0.5f) {
                    float t = cyclePos / 0.5f;
                    startSegColor = interpolateColor(lightColor, darkColor, t);
                    endSegColor = interpolateColor(lightColor, darkColor, Math.min(1.0f, t + 0.2f));
                } else {
                    float t = (cyclePos - 0.5f) / 0.5f;
                    startSegColor = interpolateColor(darkColor, lightColor, t);
                    endSegColor = interpolateColor(darkColor, lightColor, Math.min(1.0f, t + 0.2f));
                }

                RenderUtil.drawGradientRect(
                        startX, y1,
                        endX, y2,
                        startSegColor.getRGB(),
                        endSegColor.getRGB()
                );
            }
        } else {
            RenderUtil.drawGradientRect(
                    x1, y1,
                    x2, y2,
                    getAccentColor(y1Pos),
                    new Color(30, 30, 30).getRGB()
            );
        }
    }

    private Color interpolateColor(Color c1, Color c2, float fraction) {
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * fraction);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * fraction);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * fraction);
        return new Color(r, g, b);
    }

    private void renderModuleList(ScaledResolution sr, float deltaTime) {
        List<Module> modules = getEnabledModules()
                .stream()
                .sorted((m1, m2) -> Float.compare(getTextWidth(getModuleName(m2)), getTextWidth(getModuleName(m1))))
                .collect(Collectors.toList());

        float screenWidth = sr.getScaledWidth();
        float currentY = 2;

        Set<Module> processedModules = new HashSet<>();

        for (Module module : modules) {
            processedModules.add(module);
            String name = getModuleName(module);
            float targetX = screenWidth - getTextWidth(name) - 4;

            ModulePosition pos = modulePositions.get(module);
            if (pos == null) {
                pos = new ModulePosition(screenWidth + 40, targetX, currentY, currentY, false);
                modulePositions.put(module, pos);
            }

            if (!pos.wasEnabled) {
                pos.x = screenWidth + 40;
                pos.targetY = currentY;
                pos.wasEnabled = true;
                pos.enableTime = System.currentTimeMillis();
                pos.animationProgress = 0.0f;
            }

            pos.targetX = targetX;
            pos.targetY = currentY;
            currentY += getTextHeight() + (float)spacing.getValue();
        }

        for (Map.Entry<Module, ModulePosition> entry : modulePositions.entrySet()) {
            Module module = entry.getKey();
            ModulePosition pos = entry.getValue();
            if (!processedModules.contains(module) && pos.wasEnabled) {
                pos.targetX = screenWidth + 40;
                pos.wasEnabled = false;
                pos.enableTime = System.currentTimeMillis();
            }
        }

        float baseSpeed = (float) (animSpeed.getValue() / 1000f);
        float xAnimSpeed = baseSpeed * deltaTime * 30f;
        float yAnimSpeed = baseSpeed * deltaTime * 20f;

        List<Map.Entry<Module, ModulePosition>> sortedEntries = new ArrayList<>(modulePositions.entrySet());
        sortedEntries.sort((e1, e2) -> Float.compare(e1.getValue().y, e2.getValue().y));

        Iterator<Map.Entry<Module, ModulePosition>> it = modulePositions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Module, ModulePosition> entry = it.next();
            Module module = entry.getKey();
            ModulePosition pos = entry.getValue();

            pos.x = lerp(pos.x, pos.targetX, xAnimSpeed);
            pos.y = lerp(pos.y, pos.targetY, yAnimSpeed);
            pos.animationProgress = Math.min(1.0f, pos.animationProgress + deltaTime * 3.0f);

            if (!pos.wasEnabled && pos.x >= screenWidth + 39) {
                it.remove();
                continue;
            }

            String name = getModuleName(module);

            float alpha = 1.0f;
            if (!pos.wasEnabled) {
                long timeSinceDisable = System.currentTimeMillis() - pos.enableTime;
                alpha = Math.max(0, 1 - (timeSinceDisable / 500f));
            }

            if (style.getMode().equals("Skeet") || style.getMode().equals("Solid")) {
                //RenderUtil.drawRect(
                        //pos.x - 4, pos.y - 1,
                        //screenWidth + 2, pos.y + getTextHeight() + 1,
                        //withAlpha(backgroundColor.getColor(), (int)(180 * alpha))
                //);
            }

            int textColor = withAlpha(getAccentColor(pos.y), (int)(255 * alpha));
            drawText(name, pos.x, pos.y, textColor);
        }
    }

    /**
     * Gets the accent color based on position and current mode
     * @param yPos Y position for gradient/rainbow calculations
     */
    private int getAccentColor(float yPos) {
        switch (colorMode.getMode()) {
            case "Rainbow":
                float yOffset = yPos * (float)colorOffset.getValue();
                return rainbowUtil.getRainbowWithOffset((int)(yOffset * 40));

            case "Gradient":
                float screenHeight = new ScaledResolution(mc).getScaledHeight();
                float normalizedY = yPos / screenHeight;
                return rainbowUtil.getGradientColor(normalizedY);

            case "Fade":
                float basePhase = (System.currentTimeMillis() - startTime) / 2000f;
                float yPhase = yPos * (float)colorOffset.getValue();
                float combinedPhase = (basePhase + yPhase) % 1.0f;

                Color baseColor = new Color(primaryColor.getColor());
                float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);

                float brightnessVariation = 0.3f;
                float minBrightness = Math.max(0.4f, hsb[2] - brightnessVariation);
                float maxBrightness = Math.min(1.0f, hsb[2] + 0.1f);
                float newBrightness = minBrightness + (float)((maxBrightness - minBrightness)
                        * (0.5 + 0.5 * Math.sin(combinedPhase * Math.PI * 2)));

                return Color.HSBtoRGB(hsb[0], hsb[1], newBrightness);

            default:
                return primaryColor.getColor();
        }
    }

    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * Math.min(1, delta);
    }

    private void drawText(String text, float x, float y, int color) {
        if (useCustomFont.isEnabled()) {
            this.getFont().drawStringWithShadow(text, x, y, color);
        } else {
            mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
        }
    }

    private float getTextWidth(String text) {
        return useCustomFont.isEnabled() ?
                this.getFont().getStringWidth(text) :
                mc.fontRendererObj.getStringWidth(text);
    }

    private float getTextHeight() {
        return useCustomFont.isEnabled() ?
                this.getFont().getHeight() :
                mc.fontRendererObj.FONT_HEIGHT;
    }

    private List<Module> getEnabledModules() {
        return getModules()
                .stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toList());
    }

    private CFontRenderer getFont() {
        return Fonts.SF;
    }
}