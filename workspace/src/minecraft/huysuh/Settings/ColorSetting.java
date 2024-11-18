package huysuh.Settings;

import huysuh.Utils.Colors;
import net.minecraft.util.MathHelper;
import java.awt.Color;

/**
 * Represents a color setting with support for RGB, HSB, and alpha channels.
 * This class provides thread-safe color manipulation with proper validation.
 */
public final class ColorSetting extends Setting {
    private static final float MAX_ALPHA = 1.0f;
    private static final int ALPHA_MASK = 0xFF;
    private static final int RGB_MASK = 0xFFFFFF;

    private volatile int color;      // RGB color without alpha
    private volatile float alpha;    // Alpha value [0.0, 1.0]
    private volatile boolean expanded;
    private final float[] hsb;       // Hue, Saturation, Brightness
    private volatile boolean dirty;

    /**
     * Creates a new ColorSetting with the specified name and initial color.
     * @param name The setting name
     * @param color The initial color (ARGB format)
     */
    public ColorSetting(String name, int color) {
        setName(name);
        this.hsb = new float[3];
        setColorInternal(color);
    }

    /**
     * Internal method to set color values and update HSB
     */
    private void setColorInternal(int color) {
        this.alpha = ((color >> 24) & ALPHA_MASK) / 255f;
        this.color = color & RGB_MASK;
        Color.RGBtoHSB(
                (color >> 16) & ALPHA_MASK,
                (color >> 8) & ALPHA_MASK,
                color & ALPHA_MASK,
                this.hsb
        );
        this.dirty = true;
    }

    /**
     * Creates a cached Color object for better performance when needed repeatedly
     */
    private static class ColorCache {
        private static final ThreadLocal<Color> colorCache = new ThreadLocal<>();

        static Color getColor(int rgb, float alpha) {
            Color color = colorCache.get();
            if (color == null) {
                color = new Color(255, 255, 255);
                colorCache.set(color);
            }
            return new Color((rgb << 8) | (int)(alpha * 255), true);
        }
    }

    // Getters and setters with validation
    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = MathHelper.clamp_float(alpha, 0f, MAX_ALPHA);
        this.dirty = true;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        setColorInternal(color);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public int getOpacity() {
        return (int)(alpha * 255) & ALPHA_MASK;
    }

    public void setOpacity(int opacity) {
        setAlpha((opacity & ALPHA_MASK) / 255f);
    }

    // HSB manipulation methods
    public float[] getHSB() {
        return hsb.clone(); // Return a copy to prevent external modification
    }

    public void setHue(float hue) {
        hsb[0] = MathHelper.clamp_float(hue, 0f, MAX_ALPHA);
        updateColorFromHSB();
    }

    public void setSaturation(float saturation) {
        hsb[1] = MathHelper.clamp_float(saturation, 0f, MAX_ALPHA);
        updateColorFromHSB();
    }

    public void setBrightness(float brightness) {
        hsb[2] = MathHelper.clamp_float(brightness, 0f, MAX_ALPHA);
        updateColorFromHSB();
    }

    public void setHSB(float hue, float saturation, float brightness) {
        hsb[0] = MathHelper.clamp_float(hue, 0f, MAX_ALPHA);
        hsb[1] = MathHelper.clamp_float(saturation, 0f, MAX_ALPHA);
        hsb[2] = MathHelper.clamp_float(brightness, 0f, MAX_ALPHA);
        updateColorFromHSB();
    }

    private void updateColorFromHSB() {
        color = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & RGB_MASK;
        dirty = true;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    // Color conversion methods
    public Color getJavaColor() {
        return new Color(color);
    }



    public Color getJavaColorWithAlpha() {
        return ColorCache.getColor(color, alpha);
    }

    public int getColorWithAlpha() {
        return Colors.setOpacity(this.getColor(), this.alpha);
    }

    /**
     * Creates a new ColorSetting with the same values as this one
     */
    public ColorSetting clone() {
        ColorSetting clone = new ColorSetting(getName(), getColorWithAlpha());
        clone.setExpanded(expanded);
        return clone;
    }

    @Override
    public String toString() {
        return String.format("ColorSetting[name=%s, color=#%06X, alpha=%.2f]",
                getName(), color, alpha);
    }
}