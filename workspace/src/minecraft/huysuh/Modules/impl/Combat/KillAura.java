package huysuh.Modules.impl.Combat;

import huysuh.Events.Event;
import huysuh.Events.impl.EventMotion;
import huysuh.Events.impl.EventRender2D;
import huysuh.Events.impl.EventTick;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.BooleanSetting;
import huysuh.Settings.NumberSetting;
import huysuh.Utils.Math.MathUtils;
import huysuh.Utils.Rotation.Raycast;
import huysuh.Utils.Rotation.RotationUtils;
import huysuh.Utils.Timer;
import huysuh.Utils.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillAura extends Module {

    public NumberSetting range = new NumberSetting("Range", 3, 3, 8, 1);
    public BooleanSetting hurtIgnore = new BooleanSetting("Hurt Ignore", true);
    public static BooleanSetting autoblock = new BooleanSetting("Autoblock", false);
    public NumberSetting minCps = new NumberSetting("Min CPS", 12, 5, 20, 1);
    public NumberSetting maxCps = new NumberSetting("Max CPS", 14, 5, 20, 1);

    private Timer attackTimer = new Timer();
    private int currentRandomDelay = 0;

    public KillAura() {
        super("KillAura", "Attacks nearby players", Category.COMBAT, Keyboard.KEY_R);
        this.addSettings(range, minCps, maxCps, autoblock, hurtIgnore);
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

    private EntityLivingBase getRaycastEntity(double range, float yaw, float pitch) {
        Object[] raycastResult = Raycast.getEntity2(range, 0, new float[]{yaw, pitch});
        if (raycastResult == null) {
            return null;
        }
        return (EntityLivingBase) raycastResult[0];
    }

    public void attack(Entity target) {
        float[] rotations = RotationUtils.getRotationsEntity(target);
        EntityLivingBase localTarget = getRaycastEntity(range.getValue(), rotations[0], rotations[1]);

        mc.thePlayer.swingItem();

        if (localTarget != null) {
            currentRandomDelay = (int) MathUtils.randomNumber(1000 / maxCps.getValue(), 1000 / minCps.getValue());
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(localTarget, C02PacketUseEntity.Action.ATTACK));
        }
    }

    public static EntityLivingBase target;

    @Override
    public void onEvent(Event e) {

        if (e instanceof EventRender2D){
            if (!this.isEnabled()){
                return;
            }
            this.setTag("Single");
        }

        List<EntityLivingBase> targets = getTargets(hurtIgnore.isEnabled());

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        target = targets.get(0);
        float[] rotations = RotationUtils.getRotationsEntity(target);

        if (e instanceof EventMotion) {
            ((EventMotion) e).setAngles(rotations[0], rotations[1]);
        }

        if (e instanceof EventTick) {
            if (attackTimer.hasTimeElapsed(currentRandomDelay, true) && target != null) {
                attack(target);
            }
        }
    }
}
