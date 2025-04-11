package huysuh.UI.Notification;

import huysuh.Font.CFontRenderer;
import huysuh.Font.Fonts;
import huysuh.Modules.impl.Render.HUD;
import huysuh.Utils.RainbowUtil;
import huysuh.Utils.Render.Render;
import huysuh.Utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Notifications {
    private static final List<Notification> notifications = new ArrayList<>();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final RainbowUtil rainbowUtil = new RainbowUtil(4, 0.5f, 1.0f);
    private static long lastFrameTime = System.nanoTime();
    private static float scrollOffset = 0.0f;
    private static final long startTime = System.currentTimeMillis();

    // Settings (can be moved to separate settings system if needed)
    private static String style = "Skeet"; // "Skeet", "Solid", "Simple", "Legacy"
    private static String colorMode = "Fade"; // "Fade", "Rainbow", "Gradient", "Static"
    private static boolean useCustomFont = true;
    private static float animSpeed = 350; // 50-500
    private static float spacing = 1; // 0-3
    private static int primaryColor = 0xFFCE7388;
    private static int secondaryColor = new Color(255, 255, 255).getRGB();
    private static int backgroundColor = new Color(10, 10, 10, 80).getRGB();
    private static float scrollSpeed = 30; // 5-100
    private static float colorSpeed = 2; // 0.5-10
    private static float colorOffset = 0.1f; // 0.01-0.5
    private static final int MAX_NOTIFICATIONS = 15;
    private static final int DEFAULT_DURATION = 3000; // in milliseconds

    public static class Notification {
        private final String title;
        private final String message;
        private final NotificationType type;
        private final long startTime;
        private final long duration;

        private float x;
        private float targetX;
        private float y;
        private float targetY;
        private float alpha = 1.0f;
        private boolean isClosing;
        private long closeStartTime;

        public Notification(String title, String message, NotificationType type, long duration) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
            this.closeStartTime = 0;

            ScaledResolution sr = new ScaledResolution(mc);
            this.x = sr.getScaledWidth() + 40;
            this.targetX = sr.getScaledWidth() - getMaxWidth() - 5;
            this.y = sr.getScaledHeight(); // Will be positioned properly in updatePositions
            this.targetY = 0; // Will be set in updatePositions
            this.isClosing = false;
        }

        public float getMaxWidth() {
            CFontRenderer font = useCustomFont ? Fonts.Verdana : null;
            float titleWidth = getTextWidth(title, font);
            float messageWidth = getTextWidth(message, font);
            return Math.max(titleWidth, messageWidth) + 28; // 28 = icon (16) + padding
        }

        public float getHeight() {
            return getTextHeight() * 2 + 10; // Title + message + padding
        }

        public boolean isExpired() {
            // Only consider fully expired when it's off screen after closing animation
            return isClosing && x >= new ScaledResolution(mc).getScaledWidth() + 20;
        }

        public boolean shouldStartClosing() {
            return !isClosing && System.currentTimeMillis() - startTime > duration;
        }

        public float getProgress() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;

            // Return percentage of time remaining (1.0 = just started, 0.0 = time's up)
            return Math.max(0.0f, Math.min(1.0f, 1.0f - ((float) elapsedTime / duration)));
        }

        public void startClosing() {
            if (!isClosing) {
                isClosing = true;
                closeStartTime = System.currentTimeMillis();
                ScaledResolution sr = new ScaledResolution(mc);
                targetX = sr.getScaledWidth() + 40;
            }
        }
    }

    public enum NotificationType {
        INFO(0xFF94e4ff),    // blue
        SUCCESS(0xFF66ff73), // green
        WARNING(0xFFf4ff59), // yellow
        ERROR(0xFFff5454);   // red

        private final int color;

        NotificationType(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    /**
     * Add a notification to be displayed
     *
     * @param title Title of the notification
     * @param message Message content
     * @param type Type of notification
     * @param duration Duration in milliseconds
     */
    public static void add(String title, String message, NotificationType type, long duration) {
        Notification notification = new Notification(title, message, type, duration);

        // If we have too many notifications, remove the oldest one
        if (notifications.size() >= MAX_NOTIFICATIONS) {
            for (Notification n : notifications) {
                if (!n.isClosing) {
                    n.startClosing();
                    break;
                }
            }
        }

        notifications.add(notification);
        updatePositions();
    }

    /**
     * Add a notification with default duration
     */
    public static void add(String title, String message, NotificationType type) {
        add(title, message, type, DEFAULT_DURATION);
    }

    /**
     * Clear all notifications
     */
    public static void clear() {
        for (Notification notification : notifications) {
            notification.startClosing();
        }
    }

    /**
     * Set system settings from HUD module
     */
    public static void updateSettings(HUD hud) {
        style = hud.style.getMode();
        colorMode = hud.colorMode.getMode();
        useCustomFont = hud.useCustomFont.isEnabled();
        animSpeed = (float) hud.animSpeed.getValue();
        spacing = (float) hud.spacing.getValue();
        primaryColor = hud.primaryColor.getColor();
        secondaryColor = hud.secondaryColor.getColor();
        backgroundColor = hud.backgroundColor.getColor();
        scrollSpeed = (float) hud.scrollSpeed.getValue();
        colorSpeed = (float) hud.colorSpeed.getValue();
        colorOffset = (float) hud.colorOffset.getValue();
    }

    /**
     * Called by the client to render notifications
     */
    public static void render() {
        if (notifications.isEmpty()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        float deltaTime = getDeltaTime();

        rainbowUtil.update(50);

        scrollOffset += deltaTime * (scrollSpeed / 100f);
        if (scrollOffset > 1.0f) scrollOffset -= 1.0f;

        float baseSpeed = animSpeed / 1000f;
        float xAnimSpeed = baseSpeed * deltaTime * 30f;
        float yAnimSpeed = baseSpeed * deltaTime * 20f;

        boolean positionsNeedUpdate = false;

        Iterator<Notification> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            Notification notification = iterator.next();

            // Check if notification should start closing
            if (notification.shouldStartClosing()) {
                notification.startClosing();
                positionsNeedUpdate = true;
            }

            // Update position with animation
            notification.x = lerp(notification.x, notification.targetX, xAnimSpeed);
            notification.y = lerp(notification.y, notification.targetY, yAnimSpeed);

            // Update alpha for fading
            if (notification.isClosing) {
                long elapsed = System.currentTimeMillis() - notification.closeStartTime;
                notification.alpha = Math.max(0.0f, 1.0f - (elapsed / 500.0f));
            }

            // Remove expired notifications
            if (notification.isExpired()) {
                iterator.remove();
                positionsNeedUpdate = true;
                continue;
            }

            // Draw notification
            drawNotification(notification);
        }

        if (positionsNeedUpdate) {
            updatePositions();
        }

        lastFrameTime = System.nanoTime();
    }

    private static void updatePositions() {
        ScaledResolution sr = new ScaledResolution(mc);
        float baseY = sr.getScaledHeight() - 5; // Start from bottom

        // Sort notifications - active ones first, then closing ones
        List<Notification> sortedNotifications = new ArrayList<>(notifications);
        sortedNotifications.sort((n1, n2) -> {
            if (n1.isClosing && !n2.isClosing) return 1;
            if (!n1.isClosing && n2.isClosing) return -1;
            return 0;
        });

        // Position from bottom to top
        for (Notification notification : sortedNotifications) {
            baseY -= notification.getHeight() + spacing;
            notification.targetY = baseY;
        }
    }

    private static void drawNotification(Notification notification) {
        float x = notification.x;
        float y = notification.y;
        float width = notification.getMaxWidth();
        float height = notification.getHeight();
        float progress = notification.getProgress();

        // Apply alpha to all colors for fading effect
        int alpha = (int)(notification.alpha * 255);
        if (alpha <= 0) return;

        // Use notification type color
        int typeColor = notification.type.getColor();

        // Draw notification background
        //Render.drawBorderedGradientRect((int)x, (int)y, (int)width, (int)height, 1, new Color(95, 95, 95, 220).getRGB(), withAlpha((new Color(17, 17, 17, 220).getRGB()), alpha), withAlpha(new Color(35, 35, 35, 220).getRGB(), alpha), true);
        Render.drawRect((int)x, (int)y, (int)width, (int)height, withAlpha(new Color(0, 0, 0).getRGB(), 140));


        // Draw progress bar at the bottom using notification type color
        float progressWidth = width * progress;
        if (colorMode.equals("Static")) {
            // Use notification type color instead of colorMode effects
            RenderUtil.drawRect(x, y + height - 1, x + progressWidth, y + height,
                    withAlpha(typeColor, alpha));
        } else {
            drawScrollingTopGradient(x, y + height - 1, x + progressWidth, y + height, y, alpha, typeColor);
        }

        // Draw title with notification type color
        drawText(notification.title, x + 16, y + 3, withAlpha(typeColor, alpha));

        // Draw message with secondary color
        drawText(notification.message, x + 16, y + getTextHeight() + 5, withAlpha(secondaryColor, alpha));
    }

    private static void drawScrollingTopGradient(float x1, float y1, float x2, float y2, float y1Pos, int alpha, int typeColor) {

        // Use notification type color as base with a gradient
        Color startColor = new Color(typeColor);
        Color endColor = new Color(typeColor).darker();

        RenderUtil.drawGradientRect(
                x1, y1,
                x2, y2,
                withAlpha(startColor.getRGB(), alpha),
                withAlpha(endColor.getRGB(), alpha)
        );
    }

    private static int getAccentColor(float yPos) {
        switch (colorMode) {
            case "Rainbow":
                float yOffset = yPos * colorOffset;
                return rainbowUtil.getRainbowWithOffset((int)(yOffset * 40));

            case "Gradient":
                float screenHeight = new ScaledResolution(mc).getScaledHeight();
                float normalizedY = yPos / screenHeight;
                return rainbowUtil.getGradientColor(normalizedY);

            case "Fade":
                float basePhase = (System.currentTimeMillis() - startTime) / 2000f;
                float yPhase = yPos * colorOffset;
                float combinedPhase = (basePhase + yPhase) % 1.0f;

                Color baseColor = new Color(primaryColor);
                float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);

                float brightnessVariation = 0.3f;
                float minBrightness = Math.max(0.4f, hsb[2] - brightnessVariation);
                float maxBrightness = Math.min(1.0f, hsb[2] + 0.1f);
                float newBrightness = minBrightness + (float)((maxBrightness - minBrightness)
                        * (0.5 + 0.5 * Math.sin(combinedPhase * Math.PI * 2)));

                return Color.HSBtoRGB(hsb[0], hsb[1], newBrightness);

            default:
                return primaryColor;
        }
    }

    private static Color interpolateColor(Color c1, Color c2, float fraction) {
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * fraction);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * fraction);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * fraction);
        return new Color(r, g, b);
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static float lerp(float start, float end, float delta) {
        return start + (end - start) * Math.min(1, delta);
    }

    private static float getDeltaTime() {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
        return Math.min(deltaTime, 0.1f);
    }

    private static void drawText(String text, float x, float y, int color) {
        if (useCustomFont) {
            Fonts.Verdana.drawStringWithShadow(text, x, y, color);
        } else {
            mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
        }
    }

    private static float getTextWidth(String text, CFontRenderer font) {
        return useCustomFont ?
                (font != null ? font.getStringWidth(text) : Fonts.Verdana.getStringWidth(text)) :
                mc.fontRendererObj.getStringWidth(text);
    }

    private static float getTextHeight() {
        return useCustomFont ?
                Fonts.Verdana.getHeight() :
                mc.fontRendererObj.FONT_HEIGHT;
    }
}