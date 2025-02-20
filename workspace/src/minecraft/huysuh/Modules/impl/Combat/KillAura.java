package huysuh.Modules.impl.Combat;

import huysuh.Events.Event;
import huysuh.Events.impl.EventMotion;
import huysuh.Events.impl.EventRender2D;
import huysuh.Events.impl.EventRender3D;
import huysuh.Events.impl.EventTick;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.BooleanSetting;
import huysuh.Settings.ModeSetting;
import huysuh.Settings.NumberSetting;
import huysuh.Utils.Math.MathUtils;
import huysuh.Utils.Rotation.Raycast;
import huysuh.Utils.Rotation.RotationUtils;
import huysuh.Utils.Timer;
import huysuh.Utils.Wrapper;
import huysuh.Utils.Render.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillAura extends Module {

    public NumberSetting range = new NumberSetting("Range", 3, 3, 8, 1);
    public BooleanSetting hurtIgnore = new BooleanSetting("Hurt Ignore", false);
    public static BooleanSetting autoblock = new BooleanSetting("Autoblock", false);
    public NumberSetting minCps = new NumberSetting("Min CPS", 12, 5, 20, 1);
    public NumberSetting maxCps = new NumberSetting("Max CPS", 14, 5, 20, 1);
    public ModeSetting mode = new ModeSetting("Mode", "Single", "Switch", "Multi");
    public NumberSetting switchDelay = new NumberSetting("Switch Delay", 50, 0, 2000, 0);
    public BooleanSetting render = new BooleanSetting("Show Target", true);

    private Timer attackTimer = new Timer();
    private Timer switchTimer = new Timer();
    private int currentRandomDelay = 0;
    private int switchIndex = 0;
    private float serverYaw, serverPitch;
    private float fadeAlpha = 1.0f;

    public static EntityLivingBase target;

    public KillAura() {
        super("KillAura", "Attacks nearby players", Category.COMBAT, Keyboard.KEY_R);
        this.addSettings(range, minCps, maxCps, autoblock, hurtIgnore, mode, switchDelay, render);
    }

    private List<EntityLivingBase> getTargets(boolean ignoreHurtTime) {
        return mc.theWorld.loadedEntityList.stream()
                .filter(e -> e instanceof EntityLivingBase)
                .map(e -> (EntityLivingBase) e)
                .filter(e -> !e.isDead && e.getHealth() > 0 && e.getMaxHealth() > 0)
                .filter(e -> e.getDistanceToEntity(mc.thePlayer) <= range.getValue())
                .filter(e -> !ignoreHurtTime || e.hurtTime == 0)
                .filter(e -> e != mc.thePlayer)
                .sorted(Comparator.comparingDouble(EntityLivingBase::getHealth))
                .collect(Collectors.toList());
    }

    public void attack(EntityLivingBase target) {
        boolean shouldAttack = !this.hurtIgnore.isEnabled() || target.hurtTime < 3;
        if (shouldAttack){
            mc.thePlayer.swingItem();
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
            currentRandomDelay = (int) MathUtils.randomNumber(1000 / maxCps.getValue(), 1000 / minCps.getValue());
        }
    }

    @Override
    protected void onDisable() {
        target = null;
    }

    @Override
    public void onEvent(Event e) {

        if (mc.theWorld == null || mc.thePlayer == null) { return; }

        if (switchIndex != 0 && !(mode.getMode().equals("Switch"))){
            switchIndex = 0;
        }

        List<EntityLivingBase> targets = getTargets(false);
        target = targets.isEmpty() ? null : targets.get(switchIndex % targets.size());

        if (e instanceof EventRender2D && this.isEnabled()){
            this.setTag(mode.getMode());
        }

        if (e instanceof EventRender3D) {
            if (target != null) {

                mc.thePlayer.renderYawOffset = serverYaw;
                mc.thePlayer.rotationYawHead = serverYaw;
                mc.thePlayer.rotationPitchHead = serverPitch;
                Render.drawRotatingEntityESP(target, target.hurtTime > 0 ? new Color(255, 100, 100) : new Color(200, 255, 100), 0.1f, true, (EventRender3D) e);
            }
        }

        if (e instanceof EventMotion && target != null) {
            float[] rotations = RotationUtils.getRotationsEntity(target);
            serverYaw = rotations[0];
            serverPitch = rotations[1];
            ((EventMotion) e).setAngles(serverYaw, serverPitch);
        }

        if (e instanceof EventTick){
            if (attackTimer.hasTimeElapsed(currentRandomDelay, true)) {
                switch (mode.getMode()) {
                    case "Single":
                        if (target != null) attack(target);
                        break;
                    case "Switch":
                        if (switchTimer.hasTimeElapsed((long) switchDelay.getValue(), true)) {
                            if (target != null) attack(target);
                            switchIndex++;
                        }
                        break;
                    case "Multi":
                        for (EntityLivingBase t : targets) {
                            attack(t);
                        }
                        break;
                }
            }
        }
    }
}
