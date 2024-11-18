package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventRender2D;
import huysuh.Font.CFontRenderer;
import huysuh.Font.Fonts;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.BooleanSetting;
import huysuh.Settings.ModeSetting;
import huysuh.Settings.NumberSetting;
import huysuh.Settings.ColorSetting;
import huysuh.Solace;
import huysuh.Utils.Colors;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {

    private static CFontRenderer fontRenderer = Fonts.SF;

    private final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting arrayList = new BooleanSetting("ArrayList", true);
    private final ModeSetting mode = new ModeSetting("Style", "Modern", "Clean");
    private final ColorSetting color = new ColorSetting("Color", 0xFF8adaff);

    public HUD() {
        super("HUD", "Heads up display", Category.RENDER, Keyboard.KEY_H);
        this.addSettings(watermark, arrayList, mode, color);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventRender2D) {
            EventRender2D event = (EventRender2D) e;

            if (watermark.isEnabled()) {
                renderWatermark(event);
            }

            if (arrayList.isEnabled()) {
                renderModuleList(event);
            }
        }
    }

    private void renderWatermark(EventRender2D event) {
        String clientName = Solace.name;
        String timeFormat = new SimpleDateFormat("hh:mm a").format(new Date());
        String version = ""+Solace.version;

        float xPos = 10;
        float yPos = 10;
        float padding = 8;
        float lineSpacing = 6;

        float maxWidth = Math.max(
                Math.max(
                        fontRenderer.getStringWidth(clientName),
                        fontRenderer.getStringWidth(timeFormat)
                ),
                fontRenderer.getStringWidth("v" + version)
        );

        float totalHeight = (fontRenderer.getHeight() * 3) + (lineSpacing * 2);

        // background
        Gui.drawRect(
                (int)(xPos - padding),
                (int)(yPos - padding),
                (int)(xPos + maxWidth + padding),
                (int)(yPos + totalHeight + padding),
                new Color(15, 15, 15, 180).getRGB()
        );

        int gradientHeight = 2;
        float timeOffset = (System.currentTimeMillis() % 3000) / 3000f;
        float width = maxWidth + padding * 2;

        for (int i = 0; i < width; i++) {
            float ratio = (float)i / width;
            float offsetRatio = (ratio + timeOffset) % 1.0f;
            float gradientRatio = (float) Math.pow(Math.sin(offsetRatio * Math.PI), 2);

            int gradientColor = Colors.blendColors(
                    Colors.darkenColor(color.getColor(), 0.4f),
                    color.getColor(),
                    gradientRatio
            );

            Gui.drawRect(
                    (int)(xPos - padding + i),
                    (int)(yPos - padding),
                    (int)(xPos - padding + i + 1),
                    (int)(yPos - padding + gradientHeight),
                    Colors.setOpacity(gradientColor, 1)
            );
        }

        float clientX = xPos + (maxWidth - fontRenderer.getStringWidth(clientName)) / 2;
        float timeX = xPos + (maxWidth - fontRenderer.getStringWidth(timeFormat)) / 2;
        float versionX = xPos + (maxWidth - fontRenderer.getStringWidth("v" + version)) / 2;

        float animationOffset = (float) (Math.sin(System.currentTimeMillis() / 1000.0) * 2);

        int clientColor = new Color(255, 255, 255).getRGB();

        fontRenderer.drawStringWithShadow(
                clientName,
                clientX,
                yPos + animationOffset,
                clientColor
        );

        // Draw time
        fontRenderer.drawStringWithShadow(
                timeFormat,
                timeX,
                yPos + fontRenderer.getHeight() + lineSpacing,
                new Color(200, 200, 200).getRGB()
        );

        // Draw version
        fontRenderer.drawStringWithShadow(
                "v" + version,
                versionX,
                yPos + (fontRenderer.getHeight() + lineSpacing) * 2,
                new Color(150, 150, 150).getRGB()
        );
    }

    private void renderModuleList(EventRender2D event) {
        List<Module> modules = getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparing(module ->
                        -fontRenderer.getStringWidth(module.getName())))
                .collect(Collectors.toList());

        float yOffset = 2;
        float screenWidth = event.getWidth();

        for (Module module : modules) {
            String name = module.getName();
            float width = fontRenderer.getStringWidth(name);
            float xPos = screenWidth - width - 4;

            // Draw module background
            drawRect(screenWidth - width - 6, yOffset - 2,
                    screenWidth - 2, yOffset + fontRenderer.getHeight(),
                    new Color(0, 0, 0, 120).getRGB());

            fontRenderer.drawStringWithShadow(name,
                    xPos, yOffset,
                    mode.is("Modern") ? color.getColor() : -1);

            yOffset += fontRenderer.getHeight() + 1;
        }
    }

    private void drawRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float temp = left;
            left = right;
            right = temp;
        }
        if (top < bottom) {
            float temp = top;
            top = bottom;
            bottom = temp;
        }

        float alpha = (color >> 24 & 0xFF) / 255.0f;
        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        // Your rendering implementation here using the color components
        // This would typically use GL11 calls but depends on your rendering system
    }
}