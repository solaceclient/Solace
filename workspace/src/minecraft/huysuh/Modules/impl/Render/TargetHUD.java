package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventRender2D;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Modules.impl.Combat.KillAura;
import huysuh.Utils.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class TargetHUD extends Module {

    private float animationProgress = 0;
    private float previousHealth = 0;
    private float previousAbsorption = 0;
    private float previousArmor = 0;
    private float previousHeight = 40;

    public TargetHUD() {
        super("TargetHUD", "Displays your target", Category.RENDER, Keyboard.KEY_NONE);
    }

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * Math.min(1, delta);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventRender2D) {
            EntityLivingBase target = KillAura.target;
            if (target == null) {
                animationProgress = lerp(animationProgress, 0, 0.01f);
                previousHeight = lerp(previousHeight, 40, 0.1f);
                return;
            }

            ScaledResolution sr = new ScaledResolution(mc);
            animationProgress = lerp(animationProgress, 1, 0.01f);
            if (animationProgress < 0.05) return;

            float x = sr.getScaledWidth() / 2f + 50;
            float y = sr.getScaledHeight() / 2f - 20;
            float width = 120 * animationProgress;
            int baseHeight = 40;
            int extraHeight = 0;

            float health = target.getHealth();
            float maxHealth = target.getMaxHealth();
            float absorption = target.getAbsorptionAmount();
            int armorValue = target.getTotalArmorValue();

            if (armorValue > 0) extraHeight += 4;
            if (absorption > 0) extraHeight += 4;

            float targetHeight = baseHeight + extraHeight;
            previousHeight = lerp(previousHeight, targetHeight, 0.01f);

            float height = previousHeight;

            RenderUtil.drawRect(x, y, x + width, y + height, new Color(20, 20, 20, 200).getRGB());

            // Interpolation for bars
            previousHealth = lerp(previousHealth, health, 0.005f);
            previousAbsorption = lerp(previousAbsorption, absorption, 0.005f);
            previousArmor = lerp(previousArmor, armorValue, 0.05f);

            float healthWidth = (previousHealth / maxHealth) * (width - 8);
            float healthWidthNoLerp = (health / maxHealth) * (width - 8);
            float absorptionWidth = Math.min((previousAbsorption / maxHealth) * (width - 8), width - 8);
            float armorWidth = (previousArmor / 20f) * (width - 8);

            float barY = y + height - 7; // Start from the bottom

            // Health bar
            RenderUtil.drawRect(x + 4, barY, x + width - 4, barY + 2, new Color(56, 56, 56).getRGB());
            RenderUtil.drawRect(x + 4, barY, x + 4 + healthWidth, barY + 2, new Color(155, 40, 40).getRGB());
            RenderUtil.drawRect(x + 4, barY, x + 4 + healthWidthNoLerp, barY + 2, new Color(255, 60, 60).getRGB());

            // Centered health text
            FontRenderer fr = mc.fontRendererObj;
            String healthText = String.format("%.1f", health);
            float healthTextWidth = fr.getStringWidth(healthText) * 0.75f; // Scale down text size
            float healthTextX = x + (width / 2f) - (healthTextWidth / 2f);
            float healthTextY = barY - 3; // Move up by 2 pixels

            GL11.glPushMatrix();
            GL11.glScalef(0.75f, 0.75f, 1f);
            fr.drawStringWithShadow(healthText, healthTextX / 0.75f, healthTextY / 0.75f, -1);
            GL11.glPopMatrix();

            barY -= 4;

            // Absorption bar
            if (absorption > 0) {
                RenderUtil.drawRect(x + 4, barY, x + width - 4, barY + 1, new Color(56, 56, 56).getRGB());
                RenderUtil.drawRect(x + 4, barY, x + 4 + absorptionWidth, barY + 1, new Color(255, 204, 0).getRGB());
                barY -= 4; // Move up (smaller gap)
            }

            // Armor bar
            if (armorValue > 0) {
                RenderUtil.drawRect(x + 4, barY, x + width - 4, barY + 1, new Color(56, 56, 56).getRGB());
                RenderUtil.drawRect(x + 4, barY, x + 4 + armorWidth, barY + 1, new Color(60, 60, 255).getRGB());
            }

            // Name
            fr.drawStringWithShadow(target.getName(), x + 4, y + 4, -1);
        }
    }
}
