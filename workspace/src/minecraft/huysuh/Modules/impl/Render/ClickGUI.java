package huysuh.Modules.impl.Render;

import huysuh.Font.CFontRenderer;
import huysuh.Font.Fonts;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.*;
import huysuh.Utils.Wrapper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ClickGUI extends Module {
    private ClickGuiScreen guiScreen;
    private static CFontRenderer fontRenderer = Fonts.SF;

    public ClickGUI() {
        super("ClickGUI", "Modern interface for managing modules", Category.RENDER, Keyboard.KEY_RSHIFT);
        this.guiScreen = new ClickGuiScreen();
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(guiScreen);
        setEnabled(false);
    }

    public static class ClickGuiScreen extends GuiScreen {
        private Map<Category, CategoryPanel> categoryPanels;
        private CategoryPanel draggingPanel = null;
        private int dragX, dragY;
        private long lastFrame = System.currentTimeMillis();

        private static final int PANEL_WIDTH = 120;
        private static final int HEADER_HEIGHT = 25;
        private static final int MODULE_HEIGHT = 20;
        private static final int SETTING_HEIGHT = 20;
        private static final int PADDING = 5;

        private static final int BACKGROUND_COLOR = new Color(20, 20, 20, 220).getRGB();
        private static final int HEADER_COLOR = new Color(30, 30, 30, 255).getRGB();
        private static final int HOVER_COLOR = new Color(40, 40, 40, 255).getRGB();
        private static final int ENABLED_COLOR = new Color(60, 60, 60, 255).getRGB();
        private static final int TEXT_COLOR = new Color(220, 220, 220, 255).getRGB();
        private static final int SETTING_COLOR = new Color(25, 25, 25, 255).getRGB();

        public ClickGuiScreen() {
            this.categoryPanels = new HashMap<>();
        }

        // List of root categories
        private static final List<Category> ROOT_CATEGORIES = Arrays.asList(
                Category.COMBAT,
                Category.MOVEMENT,
                Category.PLAYER,
                Category.RENDER,
                Category.WORLD
        );

        @Override
        public void initGui() {
            super.initGui();
            if (categoryPanels.isEmpty()) {
                int startX = 10;
                for (Category category : ROOT_CATEGORIES) {
                    categoryPanels.put(category, new CategoryPanel(category, startX, 10));
                    startX += PANEL_WIDTH + 10;
                }
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawDefaultBackground();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            long currentTime = System.currentTimeMillis();
            float delta = (currentTime - lastFrame) / 50f;
            lastFrame = currentTime;

            for (CategoryPanel panel : categoryPanels.values()) {
                panel.draw(mouseX, mouseY, delta);
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);


            if (draggingPanel != null) {
                draggingPanel.x = mouseX - dragX;
                draggingPanel.y = mouseY - dragY;
            }
        }

        private class CategoryPanel {
            private final Category category;
            private int x, y;
            private boolean expanded = true;
            private int scrollOffset = 0;
            private final List<Module> modules;
            private float expandProgress = 0f;
            private Map<Module, Boolean> moduleExpandStates = new HashMap<>();
            private static final float ANIMATION_SPEED = 0.3f;

            public CategoryPanel(Category category, int x, int y) {
                this.category = category;
                this.x = x;
                this.y = y;
                this.modules = Module.getModulesFromExactCategory(category);
                for (Module module : modules) {
                    moduleExpandStates.put(module, false);
                }
            }

            public boolean onHeaderClick(int mouseX, int mouseY, int mouseButton) {
                if (mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                        mouseY >= y && mouseY <= y + HEADER_HEIGHT) {
                    if (mouseButton == 1) {
                        expanded = !expanded;
                        return false;
                    }
                    return true;
                }
                return false;
            }

            public boolean onSettingClick(int mouseX, int mouseY, int mouseButton) {
                if (!expanded || mouseX < x || mouseX > x + PANEL_WIDTH) {
                    return false;
                }

                int currentY = y + HEADER_HEIGHT - scrollOffset;
                for (Module module : modules) {
                    // Module click handling remains the same
                    if (mouseY >= currentY && mouseY <= currentY + MODULE_HEIGHT) {
                        if (mouseButton == 0) {
                            module.toggle();
                            return true;
                        } else if (mouseButton == 1 && !module.getSettings().isEmpty()) {
                            moduleExpandStates.put(module, !moduleExpandStates.get(module));
                            return true;
                        }
                    }

                    currentY += MODULE_HEIGHT;

                    // Settings clicks
                    if (moduleExpandStates.get(module)) {
                        for (Setting setting : module.getSettings()) {
                            if (!setting.isVisible()) continue;

                            // Check if click is within the setting's base area
                            boolean isSettingClicked = mouseY >= currentY && mouseY <= currentY + SETTING_HEIGHT;

                            // For ColorSetting, also check expanded area if it's expanded
                            boolean isExpandedColorArea = false;
                            if (setting instanceof ColorSetting) {
                                ColorSetting colorSetting = (ColorSetting) setting;
                                if (colorSetting.isExpanded()) {
                                    int expandedHeight = SETTING_HEIGHT + 80 + 8 + 8 + PADDING * 3; // Color picker height
                                    isExpandedColorArea = mouseY >= currentY && mouseY <= currentY + expandedHeight;
                                }
                            }

                            if (isSettingClicked || isExpandedColorArea) {
                                if (setting instanceof BooleanSetting) {
                                    if (mouseButton == 0) {
                                        ((BooleanSetting) setting).toggle();
                                        return true;
                                    }
                                } else if (setting instanceof ModeSetting) {
                                    if (mouseButton == 0) {
                                        ((ModeSetting) setting).cycle();
                                        return true;
                                    }
                                } else if (setting instanceof NumberSetting) {
                                    if (mouseButton == 0) {
                                        int sliderWidth = 60;
                                        int sliderX = x + PANEL_WIDTH - sliderWidth - PADDING;
                                        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth) {
                                            setting.setFocused(true);
                                            return true;
                                        }
                                    }
                                } else if (setting instanceof ColorSetting) {
                                    ColorSetting colorSetting = (ColorSetting) setting;

                                    // Handle the expand/collapse click
                                    if (isSettingClicked && mouseButton == 1) {
                                        colorSetting.setExpanded(!colorSetting.isExpanded());
                                        //Wrapper.addChatMessage(""+colorSetting.isExpanded());
                                        return true;
                                    }

                                    // Only handle color picker interactions if expanded
                                    if (colorSetting.isExpanded() && mouseButton == 0) {
                                        final int COLOR_PICKER_SIZE = 80;
                                        final int HUE_HEIGHT = 8;
                                        final int ALPHA_HEIGHT = 8;

                                        int pickerY = currentY + SETTING_HEIGHT;
                                        int pickerX = x + PADDING;

                                        // Handle color picker area clicks
                                        if (mouseY >= pickerY + PADDING &&
                                                mouseY <= pickerY + PADDING + COLOR_PICKER_SIZE &&
                                                mouseX >= pickerX &&
                                                mouseX <= pickerX + COLOR_PICKER_SIZE) {
                                            float saturation = (mouseX - pickerX) / (float)COLOR_PICKER_SIZE;
                                            float brightness = 1f - (mouseY - (pickerY + PADDING)) / (float)COLOR_PICKER_SIZE;
                                            float[] hsb = colorSetting.getHSB();
                                            colorSetting.setHSB(hsb[0],
                                                    Math.max(0f, Math.min(1f, saturation)),
                                                    Math.max(0f, Math.min(1f, brightness)));
                                            return true;
                                        }

                                        // Handle hue slider clicks
                                        int hueY = pickerY + PADDING + COLOR_PICKER_SIZE + PADDING;
                                        if (mouseY >= hueY && mouseY <= hueY + HUE_HEIGHT &&
                                                mouseX >= pickerX && mouseX <= pickerX + COLOR_PICKER_SIZE) {
                                            float hue = (mouseX - pickerX) / (float)COLOR_PICKER_SIZE;
                                            colorSetting.setHue(Math.max(0f, Math.min(1f, hue)));
                                            return true;
                                        }

                                        // Handle alpha slider clicks
                                        int alphaY = hueY + HUE_HEIGHT + PADDING;
                                        if (mouseY >= alphaY && mouseY <= alphaY + ALPHA_HEIGHT &&
                                                mouseX >= pickerX && mouseX <= pickerX + COLOR_PICKER_SIZE) {
                                            float alpha = (mouseX - pickerX) / (float)COLOR_PICKER_SIZE;
                                            colorSetting.setAlpha(Math.max(0f, Math.min(1f, alpha)));
                                            return true;
                                        }
                                    }
                                }
                            }

                            // Increment currentY based on setting height
                            int settingHeight = SETTING_HEIGHT;
                            if (setting instanceof ColorSetting && ((ColorSetting) setting).isExpanded()) {
                                settingHeight += 80 + 8 + 8 + PADDING * 3; // Color picker expanded height
                            }
                            currentY += settingHeight;
                        }
                    }
                }
                return false;
            }

            public void draw(int mouseX, int mouseY, float delta) {
                if (expanded && expandProgress < 1f) {
                    expandProgress = Math.min(1f, expandProgress + ANIMATION_SPEED * delta);
                } else if (!expanded && expandProgress > 0f) {
                    expandProgress = Math.max(0f, expandProgress - ANIMATION_SPEED * delta);
                }

                // Calculate total height including settings
                int totalHeight = HEADER_HEIGHT;
                if (expandProgress > 0) {
                    for (Module module : modules) {
                        totalHeight += MODULE_HEIGHT;
                        if (moduleExpandStates.get(module)) {
                            totalHeight += module.getSettings().size() * SETTING_HEIGHT;
                        }
                    }
                }
                totalHeight = HEADER_HEIGHT + (int)(expandProgress * Math.min(300, totalHeight - HEADER_HEIGHT));

                // Scissor test setup
                ScaledResolution sr = new ScaledResolution(mc);
                int scaleFactor = sr.getScaleFactor();
                GL11.glScissor(x * scaleFactor,
                        mc.displayHeight - (y + totalHeight) * scaleFactor,
                        PANEL_WIDTH * scaleFactor,
                        totalHeight * scaleFactor);

                // Draw panel background
                Gui.drawRect(x, y, x + PANEL_WIDTH, y + totalHeight, HEADER_COLOR);

                // Draw header
                fontRenderer.drawStringWithShadow(category.getName(),
                        x + PADDING,
                        y + (HEADER_HEIGHT - fontRenderer.getHeight()) / 2f,
                        category.getColor());

                // Draw modules and settings
                if (expandProgress > 0) {
                    int currentY = y + HEADER_HEIGHT - scrollOffset;
                    for (Module module : modules) {
                        // Draw module
                        boolean moduleHovered = mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                                mouseY >= currentY && mouseY <= currentY + MODULE_HEIGHT;

                        int backgroundColor = module.isEnabled() ? ENABLED_COLOR :
                                moduleHovered ? HOVER_COLOR : BACKGROUND_COLOR;

                        Gui.drawRect(x, currentY, x + PANEL_WIDTH, currentY + MODULE_HEIGHT, backgroundColor);

                        fontRenderer.drawStringWithShadow(module.getName(),
                                x + PADDING,
                                currentY + (MODULE_HEIGHT - fontRenderer.getHeight()) / 2f,
                                TEXT_COLOR);

                        // Draw settings expand indicator if module has settings
                        if (!module.getSettings().isEmpty()) {
                            String indicator = moduleExpandStates.get(module) ? "-" : "+";
                            fontRenderer.drawStringWithShadow(indicator,
                                    x + PANEL_WIDTH - PADDING - fontRenderer.getStringWidth(indicator),
                                    currentY + (MODULE_HEIGHT - fontRenderer.getHeight()) / 2f,
                                    TEXT_COLOR);
                        }

                        currentY += MODULE_HEIGHT;

                        // Draw settings if module is expanded
                        if (moduleExpandStates.get(module)) {
                            for (Setting setting : module.getSettings()) {
                                if (!setting.isVisible()) continue;

                                Gui.drawRect(x, currentY, x + PANEL_WIDTH, currentY + SETTING_HEIGHT, SETTING_COLOR);

                                drawSetting(setting, currentY, mouseX, mouseY);

                                currentY += SETTING_HEIGHT;
                            }
                        }
                    }
                }
            }

            private void drawSetting(Setting setting, int currentY, int mouseX, int mouseY) {
                if (setting instanceof BooleanSetting) {
                    drawBooleanSetting((BooleanSetting) setting, currentY, mouseX, mouseY);
                } else if (setting instanceof ModeSetting) {
                    drawModeSetting((ModeSetting) setting, currentY, mouseX, mouseY);
                } else if (setting instanceof NumberSetting) {
                    drawNumberSetting((NumberSetting) setting, currentY, mouseX, mouseY);
                } else if (setting instanceof ColorSetting) {
                    drawColorSetting((ColorSetting) setting, currentY, mouseX, mouseY);
                }
            }

            private void drawColorSetting(ColorSetting setting, int y, int mouseX, int mouseY) {
                final int COLOR_PREVIEW_SIZE = 12;

                // Draw setting name
                fontRenderer.drawString(setting.getName(),
                        x + PADDING,
                        y + (SETTING_HEIGHT - fontRenderer.getHeight()) / 2f,
                        TEXT_COLOR);

                // Draw color preview box
                int previewX = x + PANEL_WIDTH - PADDING - COLOR_PREVIEW_SIZE;
                int previewY = y + (SETTING_HEIGHT - COLOR_PREVIEW_SIZE) / 2;

                // Draw checkered background for transparency
                for (int i = 0; i < COLOR_PREVIEW_SIZE; i += 4) {
                    for (int j = 0; j < COLOR_PREVIEW_SIZE; j += 4) {
                        boolean isWhite = ((i + j) / 4) % 2 == 0;
                        Gui.drawRect(previewX + i, previewY + j,
                                Math.min(previewX + i + 4, previewX + COLOR_PREVIEW_SIZE),
                                Math.min(previewY + j + 4, previewY + COLOR_PREVIEW_SIZE),
                                isWhite ? Color.WHITE.getRGB() : Color.LIGHT_GRAY.getRGB());
                    }
                }

                // Draw actual color with alpha
                Gui.drawRect(previewX, previewY,
                        previewX + COLOR_PREVIEW_SIZE,
                        previewY + COLOR_PREVIEW_SIZE,
                        setting.getColorWithAlpha());

                // Draw expanded color picker
                if (setting.isExpanded()) {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);  // Disable clipping
                    final int COLOR_PICKER_SIZE = 80;
                    final int HUE_HEIGHT = 8;
                    final int ALPHA_HEIGHT = 8;

                    int pickerY = y + SETTING_HEIGHT;
                    int pickerX = x + 5;

                    // Draw color picker background
                    Gui.drawRect(x, pickerY, x + PANEL_WIDTH,
                            pickerY + COLOR_PICKER_SIZE + HUE_HEIGHT + ALPHA_HEIGHT + PADDING * 3, SETTING_COLOR);

                    // Draw main color picker area
                    drawColorPickerRect(pickerX, pickerY + PADDING,
                            COLOR_PICKER_SIZE, COLOR_PICKER_SIZE,
                            setting.getHSB()[0]);

                    // Draw current color indicator
                    float[] hsb = setting.getHSB();
                    int indicatorX = pickerX + (int)(hsb[1] * COLOR_PICKER_SIZE);
                    int indicatorY = pickerY + PADDING + (int)((1f - hsb[2]) * COLOR_PICKER_SIZE);
                    Gui.drawRect(indicatorX - 2, indicatorY - 2,
                            indicatorX + 2, indicatorY + 2,
                            Color.WHITE.getRGB());

                    // Draw hue slider
                    int hueY = pickerY + PADDING + COLOR_PICKER_SIZE + PADDING;
                    drawHueSlider(pickerX, hueY, COLOR_PICKER_SIZE, HUE_HEIGHT);

                    // Draw hue indicator
                    int hueIndicatorX = pickerX + (int)(hsb[0] * COLOR_PICKER_SIZE);
                    Gui.drawRect(hueIndicatorX - 1, hueY - 1,
                            hueIndicatorX + 1, hueY + HUE_HEIGHT + 1,
                            Color.WHITE.getRGB());

                    // Draw alpha slider
                    int alphaY = hueY + HUE_HEIGHT + PADDING;
                    drawAlphaSlider(pickerX, alphaY, COLOR_PICKER_SIZE, ALPHA_HEIGHT,
                            setting.getColor());

                    // Draw alpha indicator
                    int alphaIndicatorX = pickerX + (int)(setting.getAlpha() * COLOR_PICKER_SIZE);
                    Gui.drawRect(alphaIndicatorX - 1, alphaY - 1,
                            alphaIndicatorX + 1, alphaY + ALPHA_HEIGHT + 1,
                            Color.WHITE.getRGB());
                }
            }

            private void drawColorPickerRect(int x, int y, int width, int height, float hue) {
                // Draw saturation/brightness gradient
                for (int i = 0; i < width; i++) {
                    float saturation = i / (float)width;
                    for (int j = 0; j < height; j++) {
                        float brightness = 1f - (j / (float)height);
                        int color = Color.HSBtoRGB(hue, saturation, brightness);
                        Gui.drawRect(x + i, y + j, x + i + 1, y + j + 1, color);
                    }
                }
            }

            private void drawHueSlider(int x, int y, int width, int height) {
                // Draw hue gradient
                for (int i = 0; i < width; i++) {
                    float hue = i / (float)width;
                    int color = Color.HSBtoRGB(hue, 1f, 1f);
                    Gui.drawRect(x + i, y, x + i + 1, y + height, color);
                }
            }

            private void drawAlphaSlider(int x, int y, int width, int height, int baseColor) {
                // Draw checkered background
                for (int i = 0; i < width; i += 4) {
                    for (int j = 0; j < height; j += 4) {
                        boolean isWhite = ((i + j) / 4) % 2 == 0;
                        Gui.drawRect(x + i, y + j,
                                Math.min(x + i + 4, x + width),
                                Math.min(y + j + 4, y + height),
                                isWhite ? Color.WHITE.getRGB() : Color.LIGHT_GRAY.getRGB());
                    }
                }

                // Draw alpha gradient
                for (int i = 0; i < width; i++) {
                    float alpha = i / (float)width;
                    int color = (baseColor & 0xFFFFFF) | ((int)(alpha * 255) << 24);
                    Gui.drawRect(x + i, y, x + i + 1, y + height, color);
                }
            }

            private void drawBooleanSetting(BooleanSetting setting, int y, int mouseX, int mouseY) {
                fontRenderer.drawString(setting.getName(),
                        x + PADDING,
                        y + (SETTING_HEIGHT - fontRenderer.getHeight()) / 2f,
                        TEXT_COLOR);

                String state = setting.isEnabled() ? "On" : "Off";
                fontRenderer.drawString(state,
                        x + PANEL_WIDTH - PADDING - fontRenderer.getStringWidth(state),
                        y + (SETTING_HEIGHT - fontRenderer.getHeight()) / 2f,
                        setting.isEnabled() ? new Color(120, 255, 120).getRGB() :
                                new Color(255, 120, 120).getRGB());
            }

            private void drawModeSetting(ModeSetting setting, int y, int mouseX, int mouseY) {
                fontRenderer.drawString(setting.getName(),
                        x + PADDING,
                        y + (SETTING_HEIGHT - fontRenderer.getHeight()) / 2f,
                        TEXT_COLOR);

                String mode = setting.getMode();
                fontRenderer.drawString(mode,
                        x + PANEL_WIDTH - PADDING - fontRenderer.getStringWidth(mode),
                        y + (SETTING_HEIGHT - fontRenderer.getHeight()) / 2f,
                        new Color(180, 180, 255).getRGB());
            }

            private void drawNumberSetting(NumberSetting setting, int y, int mouseX, int mouseY) {
                // Constants for layout
                final int SLIDER_WIDTH = 60;
                final int VALUE_GAP = 8;
                final int SLIDER_X = x + PANEL_WIDTH - SLIDER_WIDTH - PADDING;
                final int SLIDER_Y = y + SETTING_HEIGHT / 2;

                // Calculate the maximum width available for the setting name
                String value = String.format("%.1f", setting.getValue());
                if (setting.getIcon() != null) {
                    value += setting.getIcon().icon;
                }
                int valueWidth = fontRenderer.getStringWidth(value);
                int maxNameWidth = SLIDER_X - x - PADDING - VALUE_GAP - valueWidth;

                // Draw setting name (truncated if necessary)
                String name = setting.getName();
                if (fontRenderer.getStringWidth(name) > maxNameWidth) {
                    while (fontRenderer.getStringWidth(name + "...") > maxNameWidth && name.length() > 0) {
                        name = name.substring(0, name.length() - 1);
                    }
                    name += "...";
                }

                fontRenderer.drawString(name,
                        x + PADDING,
                        y + (SETTING_HEIGHT - fontRenderer.getHeight()) / 2f,
                        TEXT_COLOR);

                // Draw value before slider
                fontRenderer.drawString(value,
                        SLIDER_X - VALUE_GAP - fontRenderer.getStringWidth(value),
                        y + (SETTING_HEIGHT - fontRenderer.getHeight()) / 2f,
                        TEXT_COLOR);

                // Draw slider background
                Gui.drawRect(SLIDER_X, SLIDER_Y - 1, SLIDER_X + SLIDER_WIDTH, SLIDER_Y + 1,
                        new Color(60, 60, 60).getRGB());

                // Draw slider
                double percent = (setting.getValue() - setting.getMinimum()) /
                        (setting.getMaximum() - setting.getMinimum());
                int sliderPos = (int)(SLIDER_X + (SLIDER_WIDTH * percent));
                Gui.drawRect(SLIDER_X, SLIDER_Y - 2, sliderPos, SLIDER_Y + 2,
                        new Color(180, 180, 255).getRGB());

                // Handle dragging
                if (setting.isFocused() && Mouse.isButtonDown(0)) {
                    double percent2 = Math.min(1, Math.max(0,
                            (mouseX - SLIDER_X) / (double)SLIDER_WIDTH));
                    setting.setValue(setting.getMinimum() +
                            (setting.getMaximum() - setting.getMinimum()) * percent2);
                }
            }

            public boolean onClick(int mouseX, int mouseY, int mouseButton) {
                if (mouseX >= x && mouseX <= x + PANEL_WIDTH) {
                    // Header click
                    if (mouseY >= y && mouseY <= y + HEADER_HEIGHT) {
                        if (mouseButton == 1) {
                            expanded = !expanded;
                        }
                        return true;
                    }

                    // Module and settings clicks
                    if (expanded) {
                        int currentY = y + HEADER_HEIGHT - scrollOffset;
                        for (Module module : modules) {
                            // Module click
                            if (mouseY >= currentY && mouseY <= currentY + MODULE_HEIGHT) {
                                if (mouseButton == 0) {
                                    module.toggle();
                                    return true;
                                } else if (mouseButton == 1 && !module.getSettings().isEmpty()) {
                                    moduleExpandStates.put(module, !moduleExpandStates.get(module));
                                    return true;
                                }
                            }

                            currentY += MODULE_HEIGHT;

                            // Settings clicks
                            if (moduleExpandStates.get(module)) {
                                for (Setting setting : module.getSettings()) {
                                    if (!setting.isVisible()) continue;

                                    if (mouseY >= currentY && mouseY <= currentY + SETTING_HEIGHT) {
                                        if (setting instanceof BooleanSetting) {
                                            if (mouseButton == 0) {
                                                ((BooleanSetting) setting).toggle();
                                                return true;
                                            }
                                        } else if (setting instanceof ModeSetting) {
                                            if (mouseButton == 0) {
                                                ((ModeSetting) setting).cycle();
                                                return true;
                                            }
                                        } else if (setting instanceof NumberSetting) {
                                            if (mouseButton == 0) {
                                                setting.setFocused(true);
                                                return true;
                                            }
                                        }
                                    }
                                    currentY += SETTING_HEIGHT;
                                }
                            }
                        }
                    }
                }
                return false;
            }

            public void scroll(int amount) {
                if (expanded) {
                    scrollOffset += amount;
                    int totalHeight = modules.stream()
                            .mapToInt(m -> MODULE_HEIGHT +
                                    (moduleExpandStates.get(m) ?
                                            m.getSettings().size() * SETTING_HEIGHT : 0))
                            .sum();
                    scrollOffset = Math.max(0, Math.min(scrollOffset,
                            Math.max(0, totalHeight - 300)));
                }
            }

            public boolean isHovered(int mouseX, int mouseY) {
                return mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                        mouseY >= y && mouseY <= y + HEADER_HEIGHT +
                        (expanded ? Math.min(300, modules.stream()
                                .mapToInt(m -> MODULE_HEIGHT +
                                        (moduleExpandStates.get(m) ?
                                                m.getSettings().size() * SETTING_HEIGHT : 0))
                                .sum()) : 0);
            }
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            // Reset focus for all settings
            categoryPanels.values().forEach(panel ->
                    panel.modules.forEach(module ->
                            module.getSettings().forEach(setting ->
                                    setting.setFocused(false))));

            // Handle panel clicks in reverse order (top to bottom)
            ArrayList<CategoryPanel> panels = new ArrayList<>(categoryPanels.values());
            Collections.reverse(panels);

            boolean settingClicked = false;
            for (CategoryPanel panel : panels) {
                // First check if we clicked on a setting
                settingClicked = panel.onSettingClick(mouseX, mouseY, mouseButton);
                if (settingClicked) break;
            }

            // Only handle panel dragging if we didn't click a setting
            if (!settingClicked) {
                for (CategoryPanel panel : panels) {
                    if (panel.onHeaderClick(mouseX, mouseY, mouseButton)) {
                        // Move clicked panel to front
                        draggingPanel = panel;
                        dragX = mouseX - panel.x;
                        dragY = mouseY - panel.y;
                        return;
                    }
                }
            }
        }

        @Override
        protected void mouseReleased(int mouseX, int mouseY, int state) {
            super.mouseReleased(mouseX, mouseY, state);
            draggingPanel = null;

            // Reset focus for number settings when mouse is released
            categoryPanels.values().forEach(panel ->
                    panel.modules.forEach(module ->
                            module.getSettings().forEach(setting -> {
                                if (setting instanceof NumberSetting) {
                                    setting.setFocused(false);
                                }
                            })));
        }

        @Override
        public void handleMouseInput() throws IOException {
            super.handleMouseInput();

            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            // Handle scrolling for the hovered panel
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                ArrayList<CategoryPanel> panels = new ArrayList<>(categoryPanels.values());
                Collections.reverse(panels);

                for (CategoryPanel panel : panels) {
                    if (panel.isHovered(mouseX, mouseY)) {
                        panel.scroll(scroll > 0 ? -20 : 20);
                        break;
                    }
                }
            }
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            super.keyTyped(typedChar, keyCode);

            // Handle keyboard input for number settings
            for (CategoryPanel panel : categoryPanels.values()) {
                for (Module module : panel.modules) {
                    for (Setting setting : module.getSettings()) {
                        if (setting instanceof NumberSetting && setting.isFocused()) {
                            NumberSetting numSetting = (NumberSetting) setting;

                            switch (keyCode) {
                                case Keyboard.KEY_LEFT:
                                    numSetting.increment(false);
                                    break;
                                case Keyboard.KEY_RIGHT:
                                    numSetting.increment(true);
                                    break;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean doesGuiPauseGame() {
            return true;
        }
    }
}