package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventRender2D;
import huysuh.Font.CFontRenderer;
import huysuh.Font.Fonts;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.*;
import huysuh.Solace;
import huysuh.Utils.Colors;
import huysuh.Utils.AnimationUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HUD extends Module {
    private static CFontRenderer fontRenderer = Fonts.SF;
    private final Map<Module, Float> moduleAnimations = new HashMap<>();
    private long lastFrameTime = System.nanoTime();

    private final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting arrayList = new BooleanSetting("ArrayList", true);
    private final ModeSetting hudMode = new ModeSetting("HUD Style", "Modern", "Clean", "Minimal", "Fade");
    private final ModeSetting arrayListMode = new ModeSetting("ArrayList Style", "Clean", "Fade", "Rainbow");
    private final ModeSetting animationMode = new ModeSetting("Animation", "Slide", "Scale", "Fade");
    private final NumberSetting animationSpeed = new NumberSetting("Anim Speed", 200, 50, 500, 10);
    private final BooleanSetting outline = new BooleanSetting("Outline", false);
    private final BooleanSetting rightLine = new BooleanSetting("Right Line", false);
    private final BooleanSetting leftLine = new BooleanSetting("Left Line", false);
    private final ColorSetting color = new ColorSetting("Color", 0xFF9b59b6);
    private final ColorSetting color2 = new ColorSetting("Color 2", 0xFF3498db);

    public HUD() {
        super("HUD", "Heads up display", Category.RENDER, Keyboard.KEY_H);
        this.addSettings(watermark, arrayList, hudMode, arrayListMode, animationMode,
                animationSpeed, outline, rightLine, leftLine, color, color2);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventRender2D) {
            EventRender2D event = (EventRender2D) e;
            updateAnimations();

            if (watermark.isEnabled()) {
                renderWatermark(event);
            }

            if (arrayList.isEnabled()) {
                renderModuleList(event);
            }

            lastFrameTime = System.nanoTime();
        }
    }

    private float getDeltaTime() {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
        return Math.min(deltaTime, 0.1f);
    }

    private void updateAnimations() {
        float deltaTime = getDeltaTime();
        // Increased base speed for faster animations
        float baseSpeed = (float) (animationSpeed.getValue() / 500f);

        for (Module module : Module.getModules()) {
            float targetValue = module.isEnabled() ? 1.0f : 0.0f;
            float currentValue = moduleAnimations.getOrDefault(module, 0.0f);

            float newValue;
            String mode = animationMode.getMode();

            switch (mode) {
                case "Slide":
                    newValue = lerp(currentValue, targetValue, baseSpeed * deltaTime * 120);
                    break;

                case "Scale":
                    newValue = smoothStep(currentValue, targetValue, baseSpeed * deltaTime * 120);
                    break;

                case "Fade":
                    newValue = lerp(currentValue, targetValue, baseSpeed * deltaTime * 120);
                    break;

                default:
                    newValue = targetValue;
                    break;
            }

            moduleAnimations.put(module, clamp(newValue, 0.0f, 1.0f));
        }
    }

    private float lerp(float start, float end, float factor) {
        return start + (end - start) * clamp(factor, 0.0f, 1.0f);
    }

    private float smoothStep(float start, float end, float factor) {
        factor = clamp(factor, 0.0f, 1.0f);
        factor = factor * factor * (3 - 2 * factor);
        return start + (end - start) * factor;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void renderModuleList(EventRender2D event) {
        List<Module> modules = getModules().stream()
                .filter(module -> {
                    float animation = moduleAnimations.getOrDefault(module, 0.0f);
                    if (animationMode.getMode().equals("Scale")) {
                        return animation > 0.02f;
                    }
                    return animation > 0.01f;
                })
                .sorted(Comparator.comparing(module ->
                        -fontRenderer.getStringWidth(module.getName())))
                .collect(Collectors.toList());

        float targetY = 0;
        Map<Module, Float> moduleYPositions = new HashMap<>();

        for (Module module : modules) {
            moduleYPositions.put(module, targetY);
            targetY += fontRenderer.getHeight();
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            float animation = moduleAnimations.getOrDefault(module, 0.0f);
            String name = module.getName();
            float width = fontRenderer.getStringWidth(name);
            float xPos = screenWidth - width - 2;

            float alpha = 1.0f;
            float scale = 1.0f;
            float xOffset = 0;

            // Get the current and target Y positions with slower transition
            float currentY = module.lastYPosition != null ? module.lastYPosition : moduleYPositions.get(module);
            float targetYPos = moduleYPositions.get(module);

            // Slowed down Y position transition
            float animatedY = lerp(currentY, targetYPos, 0.025f); // Reduced from 0.2f to 0.05f for smoother movement
            module.lastYPosition = animatedY;

            String currentMode = animationMode.getMode();
            if (currentMode.equals("Slide")) {
                xOffset = (1 - animation) * width;
                // Removed vertical element from slide
            } else if (currentMode.equals("Scale")) {
                scale = animation;
                alpha = animation;
                animatedY += (1 - animation) * fontRenderer.getHeight() / 2;
            } else if (currentMode.equals("Fade")) {
                alpha = animation;
                animatedY += (1 - animation) * fontRenderer.getHeight();
            }

            int moduleColor;
            String listMode = arrayListMode.getMode();
            if (listMode.equals("Rainbow")) {
                moduleColor = Colors.getRainbow(2.0f, 0.7f, 1.0f, i * 100L);
            } else if (listMode.equals("Fade")) {
                moduleColor = Colors.getFadingColor(color.getColor(), 0.5f, System.currentTimeMillis(), 2000, i * 0.1f);
            } else {
                moduleColor = Colors.blendColors(color.getColor(), color2.getColor(),
                        (float) (Math.sin(System.currentTimeMillis() / 1000.0 + i * 0.5) + 1) / 2);
            }

            moduleColor = Colors.setOpacity(moduleColor, alpha);

            // Only render if the module is visible enough
            if (scale > 0.02f) {
                Gui.drawRect(
                        (int)(xPos - 2 + xOffset),
                        (int)animatedY,
                        (int)(screenWidth),
                        (int)(animatedY + fontRenderer.getHeight() * scale),
                        new Color(0, 0, 0, (int)(120 * alpha)).getRGB()
                );

                if (outline.isEnabled()) {
                    Gui.drawRect(
                            (int)(xPos - 2 + xOffset),
                            (int)animatedY,
                            (int)(xPos - 1 + xOffset),
                            (int)(animatedY + fontRenderer.getHeight() * scale),
                            moduleColor
                    );
                }

                if (rightLine.isEnabled()) {
                    Gui.drawRect(
                            (int)(screenWidth - 1),
                            (int)animatedY,
                            (int)screenWidth,
                            (int)(animatedY + fontRenderer.getHeight() * scale),
                            moduleColor
                    );
                }

                if (leftLine.isEnabled()) {
                    Gui.drawRect(
                            (int)(xPos - 2 + xOffset),
                            (int)animatedY,
                            (int)(xPos - 1 + xOffset),
                            (int)(animatedY + fontRenderer.getHeight() * scale),
                            moduleColor
                    );
                }

                fontRenderer.drawStringWithShadow(
                        name,
                        xPos + xOffset,
                        animatedY + (fontRenderer.getHeight() * (1 - scale)) / 2,
                        moduleColor
                );
            }
        }
    }

    private void renderWatermark(EventRender2D event) {
        String clientName = Solace.name;
        String timeFormat = new SimpleDateFormat("hh:mm a").format(new Date());
        String version = "" + Solace.version;

        float xPos = 10;
        float yPos = 10;
        float padding = 8;
        float lineSpacing = 6;

        float maxWidth = Math.max(
                Math.max(fontRenderer.getStringWidth(clientName),
                        fontRenderer.getStringWidth(timeFormat)),
                fontRenderer.getStringWidth("v" + version)
        );

        float totalHeight = (fontRenderer.getHeight() * 3) + (lineSpacing * 2);
        float centerX = xPos + maxWidth / 2;

        String currentMode = hudMode.getMode();
        if (currentMode.equals("Modern")) {
            Gui.drawRect(
                    (int)(xPos - padding),
                    (int)(yPos - padding),
                    (int)(xPos + maxWidth + padding),
                    (int)(yPos + totalHeight + padding),
                    new Color(15, 15, 15, 180).getRGB()
            );

            int accentColor = Colors.getFadingColor(color.getColor(), 0.3f, System.currentTimeMillis(), 2000);
            Gui.drawRect(
                    (int)(xPos - padding),
                    (int)(yPos - padding),
                    (int)(xPos + maxWidth + padding),
                    (int)(yPos - padding + 2),
                    accentColor
            );

            float bobbing = (float) (Math.sin(System.currentTimeMillis() / 1000.0) * 2);
            fontRenderer.drawCenteredStringWithShadow(clientName, centerX, yPos + bobbing, accentColor);
        } else if (currentMode.equals("Clean")) {
            Gui.drawRect(
                    (int)(xPos - padding),
                    (int)(yPos - padding),
                    (int)(xPos + maxWidth + padding),
                    (int)(yPos + totalHeight + padding),
                    new Color(0, 0, 0, 150).getRGB()
            );
            fontRenderer.drawCenteredString(clientName, centerX, yPos, color.getColor());
        } else if (currentMode.equals("Minimal")) {
            fontRenderer.drawStringWithShadow(clientName, xPos, yPos, Colors.getRainbow(2.0f, 0.7f, 1.0f));
        } else if (currentMode.equals("Fade")) {
            Gui.drawRect(
                    (int)(xPos - padding),
                    (int)(yPos - padding),
                    (int)(xPos + maxWidth + padding),
                    (int)(yPos + totalHeight + padding),
                    new Color(15, 15, 15, 180).getRGB()
            );
            int fadeColor = Colors.getFadingColor(color.getColor(), 0.5f, System.currentTimeMillis(), 3000);
            fontRenderer.drawCenteredStringWithShadow(clientName, centerX, yPos, fadeColor);
        }

        if (!currentMode.equals("Minimal")) {
            fontRenderer.drawCenteredString(timeFormat, centerX, yPos + fontRenderer.getHeight() + 6,
                    new Color(200, 200, 200).getRGB());
            fontRenderer.drawCenteredString("v" + version, centerX, yPos + (fontRenderer.getHeight() + 6) * 2,
                    new Color(150, 150, 150).getRGB());
        } else {
            fontRenderer.drawString(timeFormat, xPos, yPos + fontRenderer.getHeight() + 2,
                    new Color(200, 200, 200).getRGB());
            fontRenderer.drawString("v" + version, xPos, yPos + (fontRenderer.getHeight() + 2) * 2,
                    new Color(150, 150, 150).getRGB());
        }
    }
}