package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventRenderEntity;
import huysuh.Events.impl.EventRenderHand;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.BooleanSetting;
import huysuh.Settings.ModeSetting;
import huysuh.Settings.NumberSetting;
import huysuh.Utils.Rotation.Raycast;
import huysuh.Utils.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class Chams extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Normal", "Colored", "Rainbow", "Pulse");
    public BooleanSetting players = new BooleanSetting("Players", true);
    public BooleanSetting mobs = new BooleanSetting("Mobs", false);
    public BooleanSetting hands = new BooleanSetting("Hands", true);
    public NumberSetting red = new NumberSetting("Red", 255, 0, 255, 1);
    public NumberSetting green = new NumberSetting("Green", 0, 0, 255, 1);
    public NumberSetting blue = new NumberSetting("Blue", 0, 0, 255, 1);
    public NumberSetting alpha = new NumberSetting("Alpha", 150, 0, 255, 1);
    public NumberSetting pulseSpeed = new NumberSetting("Pulse Speed", 4, 1, 10, 1);

    private float pulseAlpha = 0;
    private boolean increasing = true;

    public Chams() {
        super("Chams", "See entities through walls", Category.RENDER, Keyboard.KEY_NONE);
        this.addSettings(mode, players, mobs, hands, red, green, blue, alpha, pulseSpeed);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventRenderEntity && isEnabled()) {
            EventRenderEntity event = (EventRenderEntity) e;
            Entity entity = event.getEntity();

            // Check if we should apply chams to this entity
            if (entity instanceof EntityLivingBase &&
                    ((entity instanceof EntityPlayer && players.isEnabled()) ||
                            (!(entity instanceof EntityPlayer) && mobs.isEnabled()))) {

                // Handle the different render phases
                if (event.isPre()) {
                    // Pre-render phase
                    handlePreRender(entity);
                } else {
                    // Post-render phase
                    handlePostRender();
                }
            }
        }
    }

    private void handlePreRender(Entity e) {
        // Save the current GL state
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Disable depth testing to see through walls
        if (Wrapper.hasWallBetweenEntities(e, Minecraft.getMinecraft().thePlayer)){
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }

        // Disable lighting for a flat look
        GL11.glDisable(GL11.GL_LIGHTING);

        // Enable blending for transparency
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Apply color based on selected mode
        applyColor();

        // Disable texture to apply solid color
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Use polygon mode for wireframe if needed
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    private void handlePostRender() {
        // Re-enable textures
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Re-enable depth testing
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // Re-enable lighting
        GL11.glEnable(GL11.GL_LIGHTING);

        // Reset polygon mode
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

        // Restore the previous GL state
        GL11.glPopAttrib();
    }

    private void applyColor() {
        float r, g, b, a;

        switch (mode.getMode()) {
            case "Colored":
                r = (float) red.getValue() / 255f;
                g = (float) green.getValue() / 255f;
                b = (float) blue.getValue() / 255f;
                a = (float) alpha.getValue() / 255f;
                GL11.glColor4f(r, g, b, a);
                break;

            case "Rainbow":
                Color rainbow = getRainbowColor();
                GL11.glColor4f(rainbow.getRed() / 255f, rainbow.getGreen() / 255f,
                        rainbow.getBlue() / 255f, (float) alpha.getValue() / 255f);
                break;

            case "Pulse":
                updatePulseAlpha();
                r = (float) red.getValue() / 255f;
                g = (float) green.getValue() / 255f;
                b = (float) blue.getValue() / 255f;
                GL11.glColor4f(r, g, b, pulseAlpha);
                break;

            default: // "Normal"
                Color ccc = new Color(((HUD)Module.getModuleFromString("HUD")).getAccentColor(0));
                GL11.glColor4f(ccc.getRed()/255f, ccc.getGreen()/255f, ccc.getBlue()/255f, (float) alpha.getValue() / 255f);
                break;
        }
    }

    private Color getRainbowColor() {
        float hue = (System.currentTimeMillis() % 3000) / 3000f;
        return Color.getHSBColor(hue, 0.8f, 1.0f);
    }

    private void updatePulseAlpha() {
        float speed = (float) pulseSpeed.getValue() / 100f;

        if (increasing) {
            pulseAlpha += speed;
            if (pulseAlpha >= 1.0f) {
                pulseAlpha = 1.0f;
                increasing = false;
            }
        } else {
            pulseAlpha -= speed;
            if (pulseAlpha <= 0.3f) {
                pulseAlpha = 0.3f;
                increasing = true;
            }
        }
    }
}