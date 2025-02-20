package huysuh.Utils;

import net.minecraft.client.gui.Gui;
import java.awt.*;

public class Colors {

    public static String color(String message) {
        return message.replaceAll("&", "\u00A7");
    }
    public static String uncolor(String input) {return input.replaceAll("(§|&)[0-9A-FK-ORa-fk-or]", "");}

    public static int blendColors(int color1, int color2, double t) {
        t = Math.max(0, Math.min(1, t));

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int r = (int) (r1 * (1 - t) + r2 * t);
        int g = (int) (g1 * (1 - t) + g2 * t);
        int b = (int) (b1 * (1 - t) + b2 * t);
        int a = (int) (a1 * (1 - t) + a2 * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int darkenColor(int color, float factor) {
        if (factor < 0 || factor > 1) {
            throw new IllegalArgumentException("Factor must be between 0 and 1");
        }

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.max(0, Math.round(r * (1 - factor)));
        g = Math.max(0, Math.round(g * (1 - factor)));
        b = Math.max(0, Math.round(b * (1 - factor)));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getFadingColor(int baseColor, float fadeFactor, long time, int cycleDuration, float offset) {
        float progress = (float) (((time % cycleDuration) / (double) cycleDuration) + offset) % 1.0f;

        float oscillation = (float) Math.abs(Math.sin(progress * Math.PI));

        return blendColors(baseColor, darkenColor(baseColor, fadeFactor), oscillation);
    }

    public static int getFadingColor(int baseColor, float fadeFactor, long time, int cycleDuration) {
        float progress = (float) ((time % cycleDuration) / (double) cycleDuration);

        float oscillation = (float) Math.abs(Math.sin(progress * Math.PI));

        return blendColors(baseColor, darkenColor(baseColor, fadeFactor), oscillation);
    }


    public static int setOpacity(int hexColor, double opacity) {
        int red = (hexColor >> 16) & 0xFF;
        int green = (hexColor >> 8) & 0xFF;
        int blue = hexColor & 0xFF;

        int alpha = (int) (opacity * 255);

        int newColor = (alpha << 24) | (red << 16) | (green << 8) | blue;

        return newColor;
    }

    public static int getRainbow(float seconds, float saturation, float brightness, long index) {
        float hue = ((System.currentTimeMillis() + index) % (int)(seconds * 1000)) / (float)(seconds * 1000);
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return color;
    }

}