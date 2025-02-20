package huysuh.Modules.impl.Combat;

import huysuh.Events.Event;
import huysuh.Events.impl.EventMotion;
import huysuh.Events.impl.EventRender2D;
import huysuh.Events.impl.EventRender3D;
import huysuh.Events.impl.EventTick;
import huysuh.Events.impl.EventPacketSend;
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
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillAura extends Module {

    public NumberSetting range = new NumberSetting("Range", 3, 3, 8, 1);
    public BooleanSetting hurtIgnore = new BooleanSetting("Hurt Ignore", false);
    public NumberSetting minCps = new NumberSetting("Min CPS", 12, 5, 20, 1);
    public NumberSetting maxCps = new NumberSetting("Max CPS", 14, 5, 20, 1);
    public ModeSetting mode = new ModeSetting("Mode", "Single", "Switch", "Multi");
    public NumberSetting switchDelay = new NumberSetting("Switch Delay", 50, 0, 2000, 0);
    public BooleanSetting render = new BooleanSetting("Show Target", true);

    // Autoblock settings
    public ModeSetting autoblock = new ModeSetting("Autoblock", "Normal", "Vanilla", "None");
    public BooleanSetting mouseDownOnly = new BooleanSetting("Mouse Down Only", false);

    private Timer attackTimer = new Timer();
    private Timer switchTimer = new Timer();
    private Timer targetTimer = new Timer();
    private int currentRandomDelay = 0;
    private int switchIndex = 0;
    private float serverYaw, serverPitch;
    private float fadeAlpha = 1.0f;
    private boolean allowPacketFlag = false;

    public static EntityLivingBase target;
    public enum BlockingState {
        BLOCKING,
        NOT_BLOCKING
    }

    private BlockingState blockState = BlockingState.NOT_BLOCKING;

    public KillAura() {
        super("KillAura", "Attacks nearby players", Category.COMBAT, Keyboard.KEY_R);
        this.addSettings(range, minCps, maxCps, hurtIgnore, mode, switchDelay, render, autoblock, mouseDownOnly);
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
        boolean shouldAttack = !this.hurtIgnore.isEnabled() || target.hurtTime < 5;
        if (shouldAttack) {
            // Unblock first if we're blocking
            if (blockState == BlockingState.BLOCKING) {
                unblock();
            }

            mc.thePlayer.swingItem();
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
            currentRandomDelay = (int) MathUtils.randomNumber(1000 / maxCps.getValue(), 1000 / minCps.getValue());

            // Re-block after attack if needed
            if (shouldBlock() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                block(target);
            }
        }
    }

    private boolean shouldBlock() {
        return !autoblock.getMode().equals("None") &&
                (!mouseDownOnly.isEnabled() || Mouse.isButtonDown(1));
    }

    public void block(Entity target) {
        if (blockState == BlockingState.BLOCKING || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            return;
        }

        if (targetTimer.hasTimeElapsed(90L, false)) {
            if (autoblock.getMode().equals("Normal")) {
                allowPacketFlag = true;

                Object[] raycastResult = Raycast.getEntity(target, range.getValue(), 0, new float[]{serverYaw, serverPitch});
                if (raycastResult != null) {
                    Vec3 hitVec = (Vec3) raycastResult[1];
                    Entity hitEntity = (Entity) raycastResult[0];

                    Vec3 hitVector = new Vec3(
                            (hitVec.xCoord - hitEntity.posX),
                            (hitVec.yCoord - hitEntity.posY),
                            (hitVec.zCoord - hitEntity.posZ)
                    );

                    mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, hitVector));
                    mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                    mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                }

                allowPacketFlag = false;
                blockState = BlockingState.BLOCKING;
            } else if (autoblock.getMode().equals("Vanilla")) {
                // Vanilla blocking just uses the normal game mechanics
                mc.gameSettings.keyBindUseItem.pressed = true;
                blockState = BlockingState.BLOCKING;
            }
        }
    }

    public void unblock() {
        if (blockState == BlockingState.BLOCKING) {
            allowPacketFlag = true;

            if (autoblock.getMode().equals("Normal")) {
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                        new BlockPos(0, 0, 0),
                        EnumFacing.DOWN
                ));
            } else if (autoblock.getMode().equals("Vanilla")) {
                mc.gameSettings.keyBindUseItem.pressed = false;
            }

            allowPacketFlag = false;
            blockState = BlockingState.NOT_BLOCKING;
        }
    }

    @Override
    protected void onDisable() {
        target = null;
        unblock();
    }

    public void sendPacketNoEvent(Packet packet) {
        allowPacketFlag = true;
        mc.thePlayer.sendQueue.addToSendQueue(packet);
        allowPacketFlag = false;
    }

    @Override
    public void onEvent(Event e) {
        if (mc.theWorld == null || mc.thePlayer == null) { return; }

        if (switchIndex != 0 && !(mode.getMode().equals("Switch"))){
            switchIndex = 0;
        }

        List<EntityLivingBase> targets = getTargets(false);
        target = targets.isEmpty() ? null : targets.get(switchIndex % targets.size());

        // Handle packet events for autoblock
        if (e instanceof EventPacketSend && isEnabled()) {
            Packet packet = ((EventPacketSend) e).getCustomPacket().getPacket();

            // When target is valid and we're in range
            if (target != null && mc.thePlayer.getDistanceToEntity(target) < range.getValue()) {
                // Don't interfere with our own packets
                if (!allowPacketFlag && mc.thePlayer.getHeldItem() != null &&
                        mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {

                    // Cancel item switch to prevent unblocking
                    if (packet instanceof C09PacketHeldItemChange) {
                        unblock();
                    }

                    // Handle blocking packets
                    if (autoblock.getMode().equals("Normal")) {
                        if (packet instanceof C07PacketPlayerDigging) {
                            e.setCancelled(true);
                        }
                        if (packet instanceof C08PacketPlayerBlockPlacement) {
                            e.setCancelled(true);
                        }
                        if (packet instanceof C02PacketUseEntity) {
                            e.setCancelled(true);

                            Object[] entity = Raycast.getEntity(target, range.getValue(), 0, new float[]{serverYaw, serverPitch});
                            if (entity != null) {
                                if (((C02PacketUseEntity)packet).getAction() == C02PacketUseEntity.Action.ATTACK) {
                                    sendPacketNoEvent(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                                }
                            }
                        }
                    }
                }
            }
        }

        if (e instanceof EventRender2D && this.isEnabled()){
            this.setTag(mode.getMode() + (autoblock.getMode().equals("None") ? "" : ", " + autoblock.getMode()));
        }

        if (e instanceof EventRender3D && isEnabled()) {
            if (target != null) {
                mc.thePlayer.renderYawOffset = serverYaw;
                mc.thePlayer.rotationYawHead = serverYaw;
                mc.thePlayer.rotationPitchHead = serverPitch;

                if (render.isEnabled()) {
                    Render.drawRotatingEntityESP(target, target.hurtTime > 0 ? new Color(255, 100, 100) : new Color(200, 255, 100), 0.1f, true, (EventRender3D) e);
                }
            }
        }

        if (e instanceof EventMotion && target != null) {
            float[] rotations = RotationUtils.getRotationsEntity(target);
            serverYaw = rotations[0];
            serverPitch = rotations[1];
            ((EventMotion) e).setAngles(serverYaw, serverPitch);
        }

        if (e instanceof EventTick){
            // Handle autoblock timing
            if (target == null && blockState == BlockingState.BLOCKING) {
                unblock();
            } else if (target != null && shouldBlock() && blockState == BlockingState.NOT_BLOCKING &&
                    mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                block(target);
            }

            // Handle attack timing
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