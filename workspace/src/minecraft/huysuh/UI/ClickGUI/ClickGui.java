package huysuh.UI.ClickGUI;

import huysuh.Font.CFontRenderer;
import huysuh.Font.Fonts;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.BooleanSetting;
import huysuh.Settings.ColorSetting;
import huysuh.Settings.ModeSetting;
import huysuh.Settings.NumberSetting;
import huysuh.Settings.Setting;
import huysuh.Utils.Colors;
import huysuh.Utils.Render.Render;
import huysuh.Utils.Wrapper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ClickGui extends GuiScreen {

    public List<Category> categories = Arrays.asList(Category.COMBAT, Category.RENDER, Category.MOVEMENT, Category.PLAYER, Category.WORLD, Category.CONFIG);
    private static CFontRenderer verdana = Fonts.Verdana;
    private static CFontRenderer verdanaBold = Fonts.VerdanaBold;

    // Animation properties
    private float opacity = 0.0f;
    private boolean opening = true;
    private long lastTime;
    private static final float ANIMATION_SPEED = 0.1f; // Animation speed (0.0-1.0)

    // Position and scroll tracking
    private int guiX = 50;
    private int guiY = 50;
    private float currentScroll = 0.0f; // Current scroll value
    private float targetScroll = 0.0f;  // Target scroll value for smooth scrolling
    private int maxScrollOffset = 0;
    private boolean isDragging = false;
    private int dragX, dragY;

    // Smooth scrolling
    private static final float SCROLL_SMOOTHING = 0.1f; // Lower = smoother but slower

    // Color picker
    private boolean colorPickerOpen = false;
    private ColorSetting activeColorSetting = null;
    private int colorPickerX, colorPickerY;
    private boolean colorPickerDragging = false;
    private int colorPickerWidth = 120;
    private int colorPickerHeight = 120;

    public ClickGui() {
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void initGui() {
        super.initGui();
        // Reset animation state when GUI is opened
        opening = true;
        opacity = 0.0f;
        lastTime = System.currentTimeMillis();
        currentScroll = 0.0f;
        targetScroll = 0.0f;
    }

    public Category selectedCategory = Category.COMBAT;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Update animation
        updateAnimation();

        // Update smooth scrolling
        updateSmoothScroll();

        // Draw the GUI with current opacity
        drawClickGUI(guiX, guiY, mouseX, mouseY);

        // Draw color picker if open
        if (colorPickerOpen && activeColorSetting != null) {
            drawColorPicker(mouseX, mouseY);
        }

        // Handle mouse input for scrolling
        handleScroll();
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;

        if (opening) {
            opacity += ANIMATION_SPEED * deltaTime * 60;
            if (opacity >= 1.0f) {
                opacity = 1.0f;
                opening = false;
            }
        } else if (!opening && !isVisible()) {
            opacity -= ANIMATION_SPEED * deltaTime * 60;
            if (opacity <= 0.0f) {
                opacity = 0.0f;
                opening = true;
                mc.displayGuiScreen(null);
            }
        }
    }

    private void updateSmoothScroll() {
        // Smooth scroll interpolation
        currentScroll += (targetScroll - currentScroll) * SCROLL_SMOOTHING;

        // Ensure scrolling doesn't exceed boundaries
        if (currentScroll > 0) {
            currentScroll = 0;
            targetScroll = 0;
        } else if (currentScroll < -maxScrollOffset && maxScrollOffset > 0) {
            currentScroll = -maxScrollOffset;
            targetScroll = -maxScrollOffset;
        }
    }

    private boolean isVisible() {
        return opacity > 0.0f;
    }

    private void handleScroll() {
        int mouseScroll = Mouse.getDWheel();
        if (mouseScroll != 0) {
            // Adjust scroll speed
            float scrollSpeed = 40;
            targetScroll += (mouseScroll > 0) ? scrollSpeed : -scrollSpeed;

            // Ensure targetScroll stays within bounds
            if (targetScroll > 0) {
                targetScroll = 0;
            } else if (targetScroll < -maxScrollOffset && maxScrollOffset > 0) {
                targetScroll = -maxScrollOffset;
            }
        }
    }

    public int moduleWidth = 130;
    public int moduleHeight = 90;

    public void drawClickGUI(int x, int y, int mouseX, int mouseY) {
        int CATEGORY_SIZE = 80;
        int HEADING_HEIGHT = 25;
        int width = CATEGORY_SIZE * categories.size();
        int height = 350;
        int expand = 10;
        int altExpand = 5;
        int padding = 2;

        // Calculate GUI boundaries for click detection and rendering clipping
        int guiLeft = x - expand - altExpand;
        int guiTop = y - expand - altExpand;
        int guiRight = guiLeft + width + (expand * 2) + (altExpand * 2);
        int guiBottom = guiTop + height + (expand * 2) + (altExpand * 2);

        // Apply opacity to colors
        int opacityHex = Math.min(255, Math.max(0, (int)(opacity * 255))) << 24;
        int borderColor = (0xFF606070 & 0x00FFFFFF) | opacityHex;
        int farBackColor = (0xFF37373F & 0x00FFFFFF) | opacityHex;
        int darkColor = (0xFF17171E & 0x00FFFFFF) | opacityHex;
        int frontColor = (0xFF202026 & 0x00FFFFFF) | opacityHex;
        int accentColor = (0xFFCE7388 & 0x00FFFFFF) | opacityHex;

        // Draw GUI with applied opacity
        Render.drawBorderedRect(guiLeft, guiTop, (width + (expand * 2)) + (altExpand * 2), (height + (expand * 2)) + (altExpand * 2), 1, borderColor, farBackColor); // far back
        Render.drawBorderedRect(x - expand, y - expand, width + (expand * 2), height + (expand * 2), 1, borderColor, darkColor); // luh darkness ts ts
        Render.drawBorderedRect(x, y, width, height, 1, borderColor, frontColor); // front gradient rect
        Render.drawRect((x - expand) + padding, (y - expand) + padding, (width + (expand * 2)) - (padding * 2), 2, accentColor);

        // rendering category headers
        for (int i = 0; i < categories.size(); i++) {
            int categoryX = x + (i * CATEGORY_SIZE);
            int categoryHeaderColor1, categoryHeaderColor2;

            if (selectedCategory == categories.get(i)) {
                categoryHeaderColor1 = (0xFF24242b & 0x00FFFFFF) | opacityHex;
                categoryHeaderColor2 = (0xFF3f3f4a & 0x00FFFFFF) | opacityHex;
                Render.drawGradientRect(categoryX, y, CATEGORY_SIZE, HEADING_HEIGHT, categoryHeaderColor1, categoryHeaderColor2, true);
                Render.drawRect(categoryX, y + HEADING_HEIGHT - 1, CATEGORY_SIZE, 1, accentColor);
            } else {
                categoryHeaderColor1 = (0xFF18181D & 0x00FFFFFF) | opacityHex;
                categoryHeaderColor2 = (0xFF2D2D35 & 0x00FFFFFF) | opacityHex;
                Render.drawGradientRect(categoryX, y, CATEGORY_SIZE, HEADING_HEIGHT, categoryHeaderColor1, categoryHeaderColor2, true);
            }

            Render.drawRect(categoryX, y, CATEGORY_SIZE, 1, borderColor); // top
            Render.drawRect(categoryX, y + HEADING_HEIGHT, CATEGORY_SIZE, 1, borderColor); // bottom
            Render.drawRect(categoryX, y, 1, HEADING_HEIGHT, borderColor);
            if (i == categories.size() - 1) {
                Render.drawRect(x + (i * (CATEGORY_SIZE) + CATEGORY_SIZE) - 1, y, 1, HEADING_HEIGHT, borderColor);
            }

            // Apply opacity to text
            int textColor = (0xFFFFFFFF & 0x00FFFFFF) | opacityHex;
            verdana.drawCenteredStringWithShadow(categories.get(i).getName(), categoryX + (CATEGORY_SIZE / 2f), y + (HEADING_HEIGHT / 2f) - (verdana.getHeight() / 2f), textColor);
        }

        // Content area for modules
        int contentAreaTop = y + HEADING_HEIGHT;
        int contentAreaBottom = y + height;
        int contentAreaWidth = width;

        // Reset maxScrollOffset for this frame
        maxScrollOffset = 0;

        // Enable GL scissor to prevent rendering outside the content area
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        // Calculate scissor dimensions
        // Note: Minecraft screen coordinates start from top-left,
        // but OpenGL scissor coordinates start from bottom-left
        int scaleFactor = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        GL11.glScissor(
                x * scaleFactor,
                mc.displayHeight - (contentAreaBottom * scaleFactor),
                contentAreaWidth * scaleFactor,
                (contentAreaBottom - contentAreaTop) * scaleFactor
        );

        // Render modules in a grid layout that flows horizontally
        List<Module> modules = Module.getModulesFromExactCategory(selectedCategory);
        int moduleMargin = 10;
        int modulesPerRow = Math.max(1, (contentAreaWidth - moduleMargin) / (moduleWidth + moduleMargin));

        // Layout modules in a grid
        int totalRows = (int) Math.ceil((double) modules.size() / modulesPerRow);
        int rowHeight = 0;
        int maxRowHeightPerRow = 0;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);

            int row = i / modulesPerRow;
            int col = i % modulesPerRow;

            // Calculate module position
            int moduleX = x + moduleMargin + col * (moduleWidth + moduleMargin);
            int moduleY = contentAreaTop + moduleMargin + (int) currentScroll;

            // Add previous rows' heights
            for (int j = 0; j < row; j++) {
                int rowModules = Math.min(modulesPerRow, modules.size() - j * modulesPerRow);
                int tallestInRow = 0;

                // Find the tallest module in this row
                for (int k = 0; k < rowModules; k++) {
                    int idx = j * modulesPerRow + k;
                    if (idx < modules.size()) {
                        int moduleHeight = getEstimatedModuleHeight(modules.get(idx));
                        tallestInRow = Math.max(tallestInRow, moduleHeight);
                    }
                }

                moduleY += tallestInRow + moduleMargin;
            }

            // Render module
            int moduleHeight = renderModule(module, moduleX, moduleY, contentAreaTop, contentAreaBottom, mouseX, mouseY);

            // Update max height in current row
            maxRowHeightPerRow = Math.max(maxRowHeightPerRow, moduleHeight);

            // If this is the last module in the row or the last module overall
            if (col == modulesPerRow - 1 || i == modules.size() - 1) {
                rowHeight += maxRowHeightPerRow + moduleMargin;
                maxRowHeightPerRow = 0;
            }
        }

        // Calculate max scroll offset based on total height of all modules
        int totalContentHeight = contentAreaTop + moduleMargin + rowHeight;
        maxScrollOffset = Math.max(0, totalContentHeight - contentAreaBottom + moduleMargin);

        // Disable GL scissor
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    // Draw the color picker
    public void drawColorPicker(int mouseX, int mouseY) {
        if (activeColorSetting == null) return;

        int opacityHex = Math.min(255, Math.max(0, (int)(opacity * 255))) << 24;
        int textColor = (0xFFFFFFFF & 0x00FFFFFF) | opacityHex;
        int borderColor = (0xFF121218 & 0x00FFFFFF) | opacityHex;
        int backgroundColor = (0xFF202026 & 0x00FFFFFF) | opacityHex;

        // Draw color picker background
        Render.drawBorderedRect(colorPickerX, colorPickerY, colorPickerWidth, colorPickerHeight, 1, borderColor, backgroundColor);

        // Draw title
        verdana.drawStringWithShadow("Color Picker", colorPickerX + 5, colorPickerY + 5, textColor);

        int padding = 10;
        int colorPreviewSize = 40;
        int hueBarHeight = 12;
        int alphaBarHeight = 12;
        int satValBoxSize = 100;

        // Draw current color preview
        Color currentColor = activeColorSetting.getJavaColor();
        int currentColorWithOpacity = (currentColor.getRGB() & 0x00FFFFFF) | opacityHex;
        Render.drawBorderedRect(colorPickerX + padding, colorPickerY + 20, colorPreviewSize, colorPreviewSize, 1, borderColor, currentColorWithOpacity);

        // Draw saturation-value box
        int satValX = colorPickerX + padding;
        int satValY = colorPickerY + 20 + colorPreviewSize + 5;

        // Draw saturation-value gradient box
        float hue = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null)[0];

        for (int x = 0; x < satValBoxSize; x++) {
            for (int y = 0; y < satValBoxSize; y++) {
                float sat = (float) x / satValBoxSize;
                float val = 1.0f - (float) y / satValBoxSize;
                int rgb = Color.HSBtoRGB(hue, sat, val);
                int rgbWithOpacity = (rgb & 0x00FFFFFF) | opacityHex;

                Render.drawRect(satValX + x, satValY + y, 1, 1, rgbWithOpacity);
            }
        }

        // Draw saturation-value box border
        Render.drawBorderedRect(satValX, satValY, satValBoxSize, satValBoxSize, 1, borderColor, 0x00000000);

        // Draw hue bar
        int hueBarX = satValX;
        int hueBarY = satValY + satValBoxSize + 5;

        for (int x = 0; x < satValBoxSize; x++) {
            float barHue = (float) x / satValBoxSize;
            int rgb = Color.HSBtoRGB(barHue, 1.0f, 1.0f);
            int rgbWithOpacity = (rgb & 0x00FFFFFF) | opacityHex;

            Render.drawRect(hueBarX + x, hueBarY, 1, hueBarHeight, rgbWithOpacity);
        }

        // Draw hue bar border
        Render.drawBorderedRect(hueBarX, hueBarY, satValBoxSize, hueBarHeight, 1, borderColor, 0x00000000);

        // Draw markers for current values
        float[] hsb = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null);

        // Hue marker
        int hueMarkerPos = (int)(hsb[0] * satValBoxSize);
        Render.drawRect(hueBarX + hueMarkerPos - 1, hueBarY - 1, 3, hueBarHeight + 2, textColor);

        // Saturation-Value marker
        int satPos = (int)(hsb[1] * satValBoxSize);
        int valPos = (int)((1.0f - hsb[2]) * satValBoxSize);
        Render.drawRect(satValX + satPos - 2, satValY + valPos - 2, 5, 5, borderColor);
        Render.drawRect(satValX + satPos - 1, satValY + valPos - 1, 3, 3, textColor);

        // Draw hex value
        String hexColor = String.format("#%02X%02X%02X",
                currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
        verdana.drawStringWithShadow(hexColor, colorPickerX + padding + colorPreviewSize + 5, colorPickerY + 20 + (colorPreviewSize / 2), textColor);

        // Draw close button
        int closeButtonSize = 12;
        int closeX = colorPickerX + colorPickerWidth - closeButtonSize - 5;
        int closeY = colorPickerY + 5;

        Render.drawRect(closeX, closeY, closeButtonSize, closeButtonSize, 0xFFEE4444 | opacityHex);
        verdana.drawCenteredStringWithShadow("X", closeX + closeButtonSize / 2f, closeY + 1, textColor);
    }
    // Helper method to check if mouse is over a specific area
    private boolean isMouseOver(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Check if color picker is active and being interacted with
        if (activeColorSetting != null) {
            handleColorPickerClick(mouseX, mouseY, mouseButton);
            return;
        }

        // Handle category selection
        int CATEGORY_SIZE = 80;
        int HEADING_HEIGHT = 25;

        for (int i = 0; i < categories.size(); i++) {
            int categoryX = guiX + (i * CATEGORY_SIZE);
            if (isMouseOver(mouseX, mouseY, categoryX, guiY, categoryX + CATEGORY_SIZE, guiY + HEADING_HEIGHT)) {
                selectedCategory = categories.get(i);
                currentScroll = 0;
                targetScroll = 0;
                return;
            }
        }

        // Handle GUI dragging (if clicked on header area but not on a category)
        if (isMouseOver(mouseX, mouseY, guiX, guiY, guiX + (CATEGORY_SIZE * categories.size()), guiY + HEADING_HEIGHT)) {
            isDragging = true;
            dragX = mouseX - guiX;
            dragY = mouseY - guiY;
            return;
        }

        // Handle module and setting clicks
        int contentAreaTop = guiY + HEADING_HEIGHT;
        int contentAreaWidth = CATEGORY_SIZE * categories.size();
        int moduleMargin = 10;

        List<Module> modules = Module.getModulesFromExactCategory(selectedCategory);
        int modulesPerRow = Math.max(1, (contentAreaWidth - moduleMargin) / (moduleWidth + moduleMargin));

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);

            int row = i / modulesPerRow;
            int col = i % modulesPerRow;

            // Calculate module position
            int moduleX = guiX + moduleMargin + col * (moduleWidth + moduleMargin);
            int moduleY = contentAreaTop + moduleMargin + (int) currentScroll;

            // Add previous rows' heights
            for (int j = 0; j < row; j++) {
                int rowModules = Math.min(modulesPerRow, modules.size() - j * modulesPerRow);
                int tallestInRow = 0;

                // Find the tallest module in this row
                for (int k = 0; k < rowModules; k++) {
                    int idx = j * modulesPerRow + k;
                    if (idx < modules.size()) {
                        int moduleHeight = getEstimatedModuleHeight(modules.get(idx));
                        tallestInRow = Math.max(tallestInRow, moduleHeight);
                    }
                }

                moduleY += tallestInRow + moduleMargin;
            }

            int moduleHeight = getEstimatedModuleHeight(module);

            // Check if module is clicked
            if (isMouseOver(mouseX, mouseY, moduleX, moduleY, moduleX + moduleWidth, moduleY + moduleHeight)) {
                // Handle settings clicks
                handleModuleSettingsClick(module, moduleX, moduleY, mouseX, mouseY);
                return;
            }
        }
    }

    // New method to handle color picker interactions
    private void handleColorPickerClick(int mouseX, int mouseY, int mouseButton) {
        // Handle close button click
        int closeButtonSize = 12;
        int closeX = colorPickerX + colorPickerWidth - closeButtonSize - 5;
        int closeY = colorPickerY + 5;

        if (isMouseOver(mouseX, mouseY, closeX, closeY, closeX + closeButtonSize, closeY + closeButtonSize)) {
            activeColorSetting = null;
            return;
        }

        // Check if clicked outside the color picker
        if (!isMouseOver(mouseX, mouseY, colorPickerX, colorPickerY,
                colorPickerX + colorPickerWidth, colorPickerY + colorPickerHeight)) {
            activeColorSetting = null;
            return;
        }

        int padding = 10;
        int satValBoxSize = 100;
        int hueBarHeight = 12;

        // Handle saturation-value box click
        int satValX = colorPickerX + padding;
        int satValY = colorPickerY + 20 + 40 + 5; // 40 is colorPreviewSize

        if (isMouseOver(mouseX, mouseY, satValX, satValY, satValX + satValBoxSize, satValY + satValBoxSize)) {
            // Calculate new saturation and value
            float saturation = (float)(mouseX - satValX) / satValBoxSize;
            float value = 1.0f - (float)(mouseY - satValY) / satValBoxSize;

            // Clamp values
            saturation = Math.max(0, Math.min(1, saturation));
            value = Math.max(0, Math.min(1, value));

            // Get current HSB values and update
            Color currentColor = activeColorSetting.getJavaColor();
            float[] hsb = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null);

            // Create new color with updated saturation and value
            Color newColor = new Color(Color.HSBtoRGB(hsb[0], saturation, value));
            activeColorSetting.setColor(newColor.getRGB());
            return;
        }

        // Handle hue bar click
        int hueBarX = satValX;
        int hueBarY = satValY + satValBoxSize + 5;

        if (isMouseOver(mouseX, mouseY, hueBarX, hueBarY, hueBarX + satValBoxSize, hueBarY + hueBarHeight)) {
            // Calculate new hue
            float hue = (float)(mouseX - hueBarX) / satValBoxSize;
            hue = Math.max(0, Math.min(1, hue));

            // Get current HSB values and update
            Color currentColor = activeColorSetting.getJavaColor();
            float[] hsb = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null);

            // Create new color with updated hue
            Color newColor = new Color(Color.HSBtoRGB(hue, hsb[1], hsb[2]));
            activeColorSetting.setColor(newColor.getRGB());
            return;
        }
    }

    // ===== CHANGE 1: Fix the drawBooleanSetting method =====
    public int drawBooleanSetting(String setting, int x, int y, boolean enabled, boolean isHovered) {
        int yOffset = verdana.getHeight();

        int opacityHex = Math.min(255, Math.max(0, (int)(opacity * 255))) << 24;
        int borderColor = (0xFF121218 & 0x00FFFFFF) | opacityHex;
        int enabledColor = (0xFFCE7388 & 0x00FFFFFF) | opacityHex;
        int disabledColor = (0xFF4B4B56 & 0x00FFFFFF) | opacityHex;
        int textColor = (0xFFFFFFFF & 0x00FFFFFF) | opacityHex;

        // Draw the checkbox with a highlight effect if hovered
        Render.drawBorderedRect(x, y, verdana.getHeight() - 1, verdana.getHeight() - 1, 1,
                isHovered ? (0xFF606070 & 0x00FFFFFF) | opacityHex : borderColor,
                enabled ? enabledColor : disabledColor);

        verdana.drawStringWithShadow(setting, x + verdana.getHeight() + 4, y, textColor);

        return yOffset + 6; // Keep the spacing for boolean settings
    }

    // ===== CHANGE 2: Fix the drawModeSetting method =====
    public int drawModeSetting(ModeSetting setting, int x, int y, boolean isHovered) {
        int yOffset = verdana.getHeight();
        int textWidth = moduleWidth - 16;

        int opacityHex = Math.min(255, Math.max(0, (int)(opacity * 255))) << 24;
        int borderColor = (0xFF121218 & 0x00FFFFFF) | opacityHex;
        int darkColor = (0xFF202026 & 0x00FFFFFF) | opacityHex;
        int highlightColor = (0xFF3D3D45 & 0x00FFFFFF) | opacityHex;
        int textColor = (0xFFFFFFFF & 0x00FFFFFF) | opacityHex;
        int accentColor = (0xFFCE7388 & 0x00FFFFFF) | opacityHex;

        // Draw setting name
        verdana.drawStringWithShadow(setting.getName(), x, y, textColor);

        // Draw the current selected mode
        Render.drawBorderedRect(x, y + yOffset, textWidth, verdana.getHeight() + 4, 1,
                isHovered ? (0xFF606070 & 0x00FFFFFF) | opacityHex : borderColor,
                isHovered ? highlightColor : darkColor);

        verdana.drawStringWithShadow(setting.getMode(), x + 4, y + yOffset + 2, accentColor);

        mc.fontRendererObj.drawStringWithShadow("▼", x + textWidth - 6, y + yOffset + 2, 0xFF707070);

        // Increased spacing by returning more height
        return yOffset + verdana.getHeight() + 10;  // Better spacing to match screenshot
    }

    // ===== CHANGE 3: Fix the drawNumberSetting method =====
    public int drawNumberSetting(NumberSetting setting, int x, int y, boolean isHovered, int mouseX) {
        int yOffset = verdana.getHeight() + 2;  // Added +2 for better spacing from label
        int sliderWidth = moduleWidth - 16;
        int sliderHeight = 6;

        int opacityHex = Math.min(255, Math.max(0, (int)(opacity * 255))) << 24;
        int borderColor = (0xFF121218 & 0x00FFFFFF) | opacityHex;
        int darkColor = (0xFF202026 & 0x00FFFFFF) | opacityHex;
        int highlightColor = (0xFF3D3D45 & 0x00FFFFFF) | opacityHex;
        int accentColor = (0xFFCE7388 & 0x00FFFFFF) | opacityHex;
        int textColor = (0xFFFFFFFF & 0x00FFFFFF) | opacityHex;

        // Draw setting name and value
        verdana.drawStringWithShadow(setting.getName() + ": " + setting.getValue(), x, y, textColor);

        // Draw slider background
        Render.drawBorderedRect(x, y + yOffset, sliderWidth, sliderHeight, 1,
                borderColor, isHovered ? highlightColor : darkColor);

        // Calculate the position of the slider handle
        float range = (float) (setting.getMaximum() - setting.getMinimum());
        float percentage = (float) ((setting.getValue() - setting.getMinimum()) / range);
        int handlePos = (int)(percentage * (sliderWidth - 4));

        // Draw slider filled part (accent color gradient)
        Render.drawRect(x + 1, y + yOffset + 1, handlePos, sliderHeight - 2, accentColor);

        // Draw slider handle
        Render.drawRect(x + handlePos, y + yOffset - 1, 3, sliderHeight + 2, textColor);

        // Improved spacing to match screenshot
        return yOffset + sliderHeight + 10;
    }

    // ===== CHANGE 4: Fix the drawColorSetting method =====
    public int drawColorSetting(ColorSetting setting, int x, int y, boolean isHovered) {
        int yOffset = verdana.getHeight() + 2;  // Added +2 for better spacing from label
        int colorBoxSize = 12;

        int opacityHex = Math.min(255, Math.max(0, (int)(opacity * 255))) << 24;
        int borderColor = (0xFF121218 & 0x00FFFFFF) | opacityHex;
        int textColor = (0xFFFFFFFF & 0x00FFFFFF) | opacityHex;

        // Draw setting name
        verdana.drawStringWithShadow(setting.getName(), x, y, textColor);

        // Draw color preview box
        Color color = setting.getJavaColor();
        int colorWithOpacity = (color.getRGB() & 0x00FFFFFF) | opacityHex;

        Render.drawBorderedRect(x, y + yOffset, colorBoxSize, colorBoxSize, 1,
                isHovered ? (0xFF606070 & 0x00FFFFFF) | opacityHex : borderColor, colorWithOpacity);

        // Draw hex value
        String hexColor = String.format("#%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue());
        verdana.drawStringWithShadow(hexColor, x + colorBoxSize + 4, y + yOffset + 2, textColor);

        // Improved spacing to match screenshot
        return yOffset + colorBoxSize + 6;
    }

    // ===== CHANGE 5: Fix the getEstimatedModuleHeight method =====
    private int getEstimatedModuleHeight(Module module) {
        int height = verdana.getHeight() + 4; // Module name height with some padding

        // Add height for enabled setting (with better spacing)
        height += verdana.getHeight() + 6;

        // Add height for each setting with proper spacing
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) {
                height += verdana.getHeight() + 6;
            } else if (setting instanceof ModeSetting) {
                height += verdana.getHeight() + verdana.getHeight() + 10; // Mode text + dropdown + spacing
            } else if (setting instanceof NumberSetting) {
                height += verdana.getHeight() + 2 + 6 + 10; // Number text + spacing + slider + bottom spacing
            } else if (setting instanceof ColorSetting) {
                height += verdana.getHeight() + 2 + 12 + 6; // Color text + spacing + preview box + bottom spacing
            }
        }

        // Add some padding at the bottom of the module
        height += 4;

        return height;
    }

    // ===== CHANGE 6: Update renderModule to improve module styling =====
    public int renderModule(Module module, int x, int y, int minY, int maxY, int mouseX, int mouseY) {
        int settingX = x + 8;
        int settingY = y;
        int startY = y; // Save initial y position

        // First pass to calculate the total height
        settingY += verdana.getHeight() + 4; // Account for module name height with some padding

        List<Setting> moduleSettings = module.getSettings();
        settingY += verdana.getHeight() + 6; // For Enabled setting

        for (Setting setting : moduleSettings) {
            if (setting instanceof BooleanSetting) {
                settingY += verdana.getHeight() + 6;
            } else if (setting instanceof ModeSetting) {
                settingY += verdana.getHeight() + verdana.getHeight() + 10; // Name + dropdown
            } else if (setting instanceof NumberSetting) {
                settingY += verdana.getHeight() + 2 + 6 + 10; // Name + slider + spacing
            } else if (setting instanceof ColorSetting) {
                settingY += verdana.getHeight() + 2 + 12 + 6; // Name + color preview + spacing
            }
        }

        // Calculate the total height correctly
        int totalHeight = settingY - startY + 4; // Added padding at the bottom

        // Apply opacity to colors
        int opacityHex = Math.min(255, Math.max(0, (int)(opacity * 255))) << 24;
        int bgColor = (0xFF12121A & 0x00FFFFFF) | opacityHex;
        int borderColor = (0xFF33333A & 0x00FFFFFF) | opacityHex;
        int innerColor = (0xFF1B1B22 & 0x00FFFFFF) | opacityHex;
        int textColor = (0xFFFFFFFF & 0x00FFFFFF) | opacityHex;

        // Draw background with correct dimensions
        Render.drawRect(x, y, moduleWidth, totalHeight, bgColor);
        Render.drawBorderedRect(x + 1, y + 1, moduleWidth - 2, totalHeight - 2, 1, borderColor, innerColor);

        // Draw module name
        verdanaBold.drawStringWithShadow(module.getName(), x + 10, y + 4, textColor);  // Added +4 to center the title better

        // Reset y to start drawing actual settings
        settingY = y + verdana.getHeight() + 8;  // Added +4 more spacing after the title

        // Check if enabled checkbox is visible and if it's being hovered
        boolean enabledBtnHovered = isMouseOver(mouseX, mouseY, settingX, settingY,
                settingX + verdana.getHeight() - 1, settingY + verdana.getHeight() - 1);

        // Draw settings
        settingY += drawBooleanSetting("Enabled", settingX, settingY, module.isEnabled(), enabledBtnHovered);

        for (Setting setting : moduleSettings) {
            if (setting instanceof BooleanSetting) {
                boolean btnHovered = isMouseOver(mouseX, mouseY, settingX, settingY,
                        settingX + verdana.getHeight() - 1, settingY + verdana.getHeight() - 1);

                settingY += drawBooleanSetting(setting.getName(), settingX, settingY,
                        ((BooleanSetting) setting).isEnabled(), btnHovered);
            } else if (setting instanceof ModeSetting) {
                boolean modeHovered = isMouseOver(mouseX, mouseY, settingX, settingY + verdana.getHeight(),
                        settingX + (moduleWidth - 16), settingY + verdana.getHeight() * 2 + 4);

                settingY += drawModeSetting((ModeSetting) setting, settingX, settingY, modeHovered);
            } else if (setting instanceof NumberSetting) {
                boolean sliderHovered = isMouseOver(mouseX, mouseY, settingX, settingY + verdana.getHeight() + 2,
                        settingX + (moduleWidth - 16), settingY + verdana.getHeight() + 8);

                settingY += drawNumberSetting((NumberSetting) setting, settingX, settingY, sliderHovered, mouseX);
            } else if (setting instanceof ColorSetting) {
                boolean colorHovered = isMouseOver(mouseX, mouseY, settingX, settingY + verdana.getHeight() + 2,
                        settingX + 12, settingY + verdana.getHeight() + 14);

                settingY += drawColorSetting((ColorSetting) setting, settingX, settingY, colorHovered);
            }
        }

        return totalHeight;
    }

    // ===== CHANGE 7: Update handleModuleSettingsClick to match new spacing =====
    private void handleModuleSettingsClick(Module module, int moduleX, int moduleY, int mouseX, int mouseY) {
        int settingX = moduleX + 8;
        int settingY = moduleY + verdana.getHeight() + 8;  // Added +4 more spacing after the title

        // Handle enabled setting click
        if (isMouseOver(mouseX, mouseY, settingX, settingY,
                settingX + verdana.getHeight() - 1, settingY + verdana.getHeight() - 1)) {
            module.toggle();
            return;
        }

        // Move to the first setting position
        settingY += verdana.getHeight() + 6;

        // Handle other settings clicks
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) {
                if (isMouseOver(mouseX, mouseY, settingX, settingY,
                        settingX + verdana.getHeight() - 1, settingY + verdana.getHeight() - 1)) {
                    ((BooleanSetting) setting).toggle();
                    return;
                }
                settingY += verdana.getHeight() + 6;
            }
            else if (setting instanceof ModeSetting) {
                int dropdownHeight = verdana.getHeight() + 4;
                if (isMouseOver(mouseX, mouseY, settingX, settingY + verdana.getHeight(),
                        settingX + (moduleWidth - 16), settingY + verdana.getHeight() + dropdownHeight)) {
                    // Toggle dropdown menu or cycle to next mode
                    ModeSetting modeSetting = (ModeSetting) setting;
                    modeSetting.cycle();
                    return;
                }
                settingY += verdana.getHeight() + verdana.getHeight() + 10; // Updated to match new spacing
            }
            else if (setting instanceof NumberSetting) {
                int sliderHeight = 6;
                if (isMouseOver(mouseX, mouseY, settingX, settingY + verdana.getHeight() + 2,
                        settingX + (moduleWidth - 16), settingY + verdana.getHeight() + 2 + sliderHeight)) {
                    // Update number based on mouse position
                    NumberSetting numberSetting = (NumberSetting) setting;
                    int sliderWidth = moduleWidth - 16;
                    float percentage = (float)(mouseX - settingX) / sliderWidth;
                    percentage = Math.max(0, Math.min(1, percentage));

                    float range = (float)(numberSetting.getMaximum() - numberSetting.getMinimum());
                    float newValue = (float) (numberSetting.getMinimum() + (percentage * range));

                    // Apply the increment step if any
                    if (numberSetting.getIncrement() > 0) {
                        newValue = (float) (Math.round(newValue / numberSetting.getIncrement()) * numberSetting.getIncrement());
                    }

                    numberSetting.setValue(newValue);
                    return;
                }
                settingY += verdana.getHeight() + 2 + 6 + 10; // Updated to match new spacing
            }
            else if (setting instanceof ColorSetting) {
                int colorBoxSize = 12;
                if (isMouseOver(mouseX, mouseY, settingX, settingY + verdana.getHeight() + 2,
                        settingX + colorBoxSize, settingY + verdana.getHeight() + 2 + colorBoxSize)) {
                    // Open color picker
                    activeColorSetting = (ColorSetting) setting;
                    colorPickerX = mouseX + 10;
                    colorPickerY = mouseY;

                    // Make sure the color picker stays within screen bounds
                    colorPickerX = Math.max(5, Math.min(colorPickerX, mc.displayWidth - colorPickerWidth - 5));
                    colorPickerY = Math.max(5, Math.min(colorPickerY, mc.displayHeight - colorPickerHeight - 5));
                    return;
                }
                settingY += verdana.getHeight() + 2 + 12 + 6; // Updated to match new spacing
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        isDragging = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (isDragging) {
            guiX = mouseX - dragX;
            guiY = mouseY - dragY;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Start closing animation
        opening = false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}