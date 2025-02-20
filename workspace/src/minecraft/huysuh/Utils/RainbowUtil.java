package huysuh.Utils;

import java.awt.Color;

public class RainbowUtil {
    private float speed;
    private float saturation;
    private float brightness;
    private long lastUpdateTime;
    private float hue;
    private float multiplier = 20.0f; // Increased multiplier for faster rainbow cycling

    public RainbowUtil(float speed, float saturation, float brightness) {
        this.speed = speed;
        this.saturation = saturation;
        this.brightness = brightness;
        this.lastUpdateTime = System.currentTimeMillis();
        this.hue = 0;
    }

    /**
     * Updates the hue value based on elapsed time.
     * The multiplier accelerates the rainbow cycle.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        hue += speed * deltaTime * multiplier; // Apply multiplier to speed up the effect
        hue %= 360;
        lastUpdateTime = currentTime;
    }

    /**
     * Updates the hue value based on elapsed time.
     * The multiplier accelerates the rainbow cycle.
     */
    public void update(float multiplier) {
        this.multiplier = multiplier;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        hue += speed * deltaTime * multiplier; // Apply multiplier to speed up the effect
        hue %= 360;
        lastUpdateTime = currentTime;
    }

    /**
     * Get the current rainbow color
     */
    public int getRainbow() {
        return Color.HSBtoRGB(hue / 360.0f, saturation, brightness);
    }

    /**
     * Get rainbow color with an offset
     * Increased offset scaling for more dramatic effect between modules
     */
    public int getRainbowWithOffset(int offset) {
        float h = (hue + offset * 0.3f) % 360; // Increased offset scaling
        return Color.HSBtoRGB(h / 360.0f, saturation, brightness);
    }

    /**
     * Get a position-based gradient color
     * Enhanced to create more pronounced gradients
     */
    public int getGradientColor(float position) {
        float h = (hue + position * 180) % 360; // Reduced from 360 to 180 for faster color cycling
        return Color.HSBtoRGB(h / 360.0f, Math.min(1.0f, saturation * 1.2f), brightness);
    }

    /**
     * Gets a properly faded color that only varies brightness while maintaining hue
     * @param phase Value between 0-1 representing the fade phase
     * @return Color with preserved hue but varying brightness
     */
    public int getFadeColor(Color baseColor, float phase) {

        // Convert to HSB (Hue, Saturation, Brightness)
        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);

        // Only modify the brightness component based on the phase
        // This preserves the original hue and saturation
        float brightnessVariation = 0.4f; // How much the brightness should vary
        float minBrightness = Math.max(0.3f, hsb[2] - brightnessVariation);
        float maxBrightness = Math.min(1.0f, hsb[2] + brightnessVariation/2);

        // Calculate new brightness using a sin wave for smooth transition
        float newBrightness = minBrightness + (float)((maxBrightness - minBrightness)
                * (0.5 + 0.5 * Math.sin(phase * Math.PI * 2)));

        // Create new color with same hue/saturation but adjusted brightness
        return Color.HSBtoRGB(hsb[0], hsb[1], newBrightness);
    }

    /**
     * Set the speed multiplier for all rainbow effects
     * @param multiplier The speed multiplier (higher = faster rainbow)
     */
    public void setSpeedMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Get the current speed multiplier
     */
    public float getSpeedMultiplier() {
        return multiplier;
    }
}