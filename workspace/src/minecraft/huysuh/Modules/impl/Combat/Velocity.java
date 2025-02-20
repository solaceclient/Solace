package huysuh.Modules.impl.Combat;

import huysuh.Events.Event;
import huysuh.Events.impl.EventPacketReceive;
import huysuh.Events.impl.EventRender2D;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.ModeSetting;
import huysuh.Settings.NumberSetting;
import huysuh.Utils.Wrapper;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {
    NumberSetting horizontal = new NumberSetting("Horizontal", 80, 0, 100, 1);
    NumberSetting vertical = new NumberSetting("Vertical", 100, 0, 100, 1);
    NumberSetting autoMin = new NumberSetting("Min Horizontal", 30, 0, 100, 1);
    NumberSetting autoMax = new NumberSetting("Max Horizontal", 30, 0, 100, 1);
    public ModeSetting mode = new ModeSetting("Mode", "Normal", "Cancel", "Hypixel", "Automatic");

    public Velocity() {
        super("Velocity", "Take reduced knockback", Category.COMBAT, Keyboard.KEY_NONE);
        this.addSettings(horizontal, vertical, autoMin, autoMax, mode);
    }

    int hypixelVeloTicks;

    public void onEvent(Event e){
        if (e instanceof EventRender2D){
            this.setTag(mode.getMode());
        }
        if (e instanceof EventPacketReceive && this.isEnabled()){
            EventPacketReceive ev = (EventPacketReceive)e;
            if (ev.getCustomPacket().getPacket() instanceof S27PacketExplosion){ if (!this.mode.getMode().equals("Normal")){ e.setCancelled(true); } }
            if (ev.getCustomPacket().getPacket() instanceof S12PacketEntityVelocity){
                S12PacketEntityVelocity pkt = (S12PacketEntityVelocity)ev.getCustomPacket().getPacket();
                if (pkt.entityID == mc.thePlayer.getEntityId()) {
                    switch (this.mode.getMode()){
                        case "Normal":
                            pkt.motionX *= (this.horizontal.getValue() / 100);
                            pkt.motionZ *= (this.horizontal.getValue() / 100);
                            if (!Wrapper.isOnHypixel()) {
                                pkt.motionY *= (this.vertical.getValue() / 100);
                            }
                            if (horizontal.getValue() == 0){
                                mc.thePlayer.motionY = (double) pkt.motionY / 8000.0;
                                e.setCancelled(true);
                            }
                            break;
                        case "Hypixel":
                            if (mc.thePlayer.onGround){
                                hypixelVeloTicks = 0;
                            } else {
                                hypixelVeloTicks++;
                            }
                            if (hypixelVeloTicks < 4){
                                mc.thePlayer.motionY = (double) pkt.motionY / 8000.0;
                            } else {
                                hypixelVeloTicks = 0;
                            }
                            e.setCancelled(true);
                            break;
                        case "Automatic":
                            float healthRatio = (mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth()) ;
                            if (healthRatio > 1) { healthRatio = 1; }
                            double velocityRange = autoMax.getValue() - autoMin.getValue();
                            double calculatedVelocity = autoMin.getValue() + (velocityRange * healthRatio);

                            if (calculatedVelocity == 0){
                                mc.thePlayer.motionY = (double) pkt.motionY / 8000.0;
                                e.setCancelled(true);
                            }
                            pkt.motionX *= (calculatedVelocity / 100);
                            pkt.motionZ *= (calculatedVelocity / 100);
                            break;
                        case "Cancel":
                            e.setCancelled(true);
                            break;
                    }
                }
            }
        }
    }
}