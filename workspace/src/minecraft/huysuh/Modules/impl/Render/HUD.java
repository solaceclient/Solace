package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventRender2D;
import huysuh.Font.CFontRenderer;
import huysuh.Font.Fonts;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.*;
import huysuh.Solace;
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

    private static class ModulePosition {
        float x;
        float targetX;
        float y;
        float targetY;
        boolean wasEnabled;
        long enableTime;

        ModulePosition(float x, float targetX, float y, float targetY, boolean wasEnabled) {
            this.x = x;
            this.targetX = targetX;
            this.y = y;
            this.targetY = targetY;
            this.wasEnabled = wasEnabled;
            this.enableTime = System.currentTimeMillis();
        }
    }

    // Settings
    private final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting moduleList = new BooleanSetting("Module List", true);
    private final BooleanSetting useMinecraftFont = new BooleanSetting("MC Font", false);
    private final ModeSetting style = new ModeSetting("Style", "Simple", "Simple", "Compact", "Legacy", "Solid");
    private final NumberSetting animSpeed = new NumberSetting("Speed", 200, 50, 500, 10);
    private final ColorSetting primary = new ColorSetting("Primary", new Color(255, 255, 255).getRGB());
    private final ColorSetting secondary = new ColorSetting("Secondary", new Color(128, 128, 128).getRGB());
    private final BooleanSetting separator = new BooleanSetting("Separators", false);
    private final ModeSetting tag = new ModeSetting("Tag Type", "SPACE", "DASH", "PARENTHESIS", "BRACKETS", "NONE");

    public HUD() {
        super("HUD", "Customizable heads-up display", Category.RENDER, Keyboard.KEY_P);
        addSettings(watermark, moduleList, useMinecraftFont, style, animSpeed,
                primary, secondary, tag, separator);
        lastFrameTime = System.nanoTime();
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventRender2D) {
            onRender2D((EventRender2D) e);
        }
    }

    private void onRender2D(EventRender2D event) {
        ScaledResolution sr = new ScaledResolution(mc);
        float deltaTime = getDeltaTime();

        if (watermark.isEnabled()) {
            renderWatermark(sr);
        }

        if (moduleList.isEnabled()) {
            renderModuleList(sr, deltaTime);
        }

        lastFrameTime = System.nanoTime();
    }

    private String getModuleName(Module m){
        return m.getFullName(this.tag.getMode());
    }

    private float getDeltaTime() {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
        return Math.min(deltaTime, 0.1f);
    }

    private void renderWatermark(ScaledResolution sr) {
        String clientName = Solace.name;
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        float x = 4;
        float y = 4;

        switch (style.getMode()) {
            case "Simple":
                drawText(clientName, x, y, primary.getColor());
                drawText(time, x, y + getTextHeight() + 2, secondary.getColor());
                break;

            case "Compact":
                String info = clientName + " | " + time;
                drawText(info, x, y, primary.getColor());
                break;

            case "Legacy":
                drawText("§l" + clientName, x, y, primary.getColor());
                drawText("§o" + time, x, y + getTextHeight() + 2, secondary.getColor());
                break;

            case "Solid":
                float width = Math.max(getTextWidth(clientName), getTextWidth(time)) + 8;
                Gui.drawRect((int)x - 2, (int)y - 2,
                        (int)(x + width), (int)(y + getTextHeight() * 2 + 6),
                        new Color(0, 0, 0, 180).getRGB());
                drawText(clientName, x + 2, y, primary.getColor());
                drawText(time, x + 2, y + getTextHeight() + 2, secondary.getColor());
                break;
        }
    }

    private void renderModuleList(ScaledResolution sr, float deltaTime) {
        List<Module> modules = getEnabledModules()
                .stream()
                .sorted((m1, m2) -> Float.compare(getTextWidth(this.getModuleName(m2)), getTextWidth(this.getModuleName(m1))))
                .collect(Collectors.toList());

        float screenWidth = sr.getScaledWidth();
        float currentY = 2;

        Set<Module> processedModules = new HashSet<>();

        for (Module module : modules) {
            processedModules.add(module);
            String name = this.getModuleName(module);
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
            }

            pos.targetX = targetX;
            pos.targetY = currentY;
            currentY += getTextHeight() + 1;
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

        for (Iterator<Map.Entry<Module, ModulePosition>> it = modulePositions.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Module, ModulePosition> entry = it.next();
            Module module = entry.getKey();
            ModulePosition pos = entry.getValue();
            String name = this.getModuleName(module);

            pos.x = lerp(pos.x, pos.targetX, xAnimSpeed);
            pos.y = lerp(pos.y, pos.targetY, yAnimSpeed);

            if (!pos.wasEnabled && pos.x >= screenWidth + 39) {
                it.remove();
                continue;
            }

            float alpha = 1.0f;
            if (!pos.wasEnabled) {
                long timeSinceDisable = System.currentTimeMillis() - pos.enableTime;
                alpha = Math.max(0, 1 - (timeSinceDisable / 500f));
            }

            if (style.getMode().equals("Solid")) {
                Gui.drawRect((int)pos.x - 2, (int)pos.y,
                        (int)screenWidth, (int)(pos.y + getTextHeight()),
                        new Color(0, 0, 0, (int)(160 * alpha)).getRGB());
            }

            if (separator.isEnabled()) {
                Gui.drawRect((int)pos.x - 3, (int)pos.y,
                        (int)pos.x - 2, (int)(pos.y + getTextHeight()),
                        new Color(
                                (primary.getColor() >> 16) & 0xFF,
                                (primary.getColor() >> 8) & 0xFF,
                                primary.getColor() & 0xFF,
                                (int)(alpha * 255)
                        ).getRGB());
            }

            int color = new Color(
                    (primary.getColor() >> 16) & 0xFF,
                    (primary.getColor() >> 8) & 0xFF,
                    primary.getColor() & 0xFF,
                    (int)(alpha * 255)
            ).getRGB();

            drawText(name, pos.x, pos.y, color);
        }
    }


    private float lerp(float start, float end, float delta) {
        return start + (end - start) * Math.min(1, delta);
    }

    private void drawText(String text, float x, float y, int color) {
        if (useMinecraftFont.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
        } else {
            this.getFont().drawStringWithShadow(text, x, y, color);
        }
    }

    private float getTextWidth(String text) {
        return useMinecraftFont.isEnabled() ?
                mc.fontRendererObj.getStringWidth(text) :
                this.getFont().getStringWidth(text);
    }

    private float getTextHeight() {
        return useMinecraftFont.isEnabled() ?
                mc.fontRendererObj.FONT_HEIGHT :
                this.getFont().getHeight();
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