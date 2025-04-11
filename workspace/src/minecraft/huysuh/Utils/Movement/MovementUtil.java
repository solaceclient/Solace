package huysuh.Utils.Movement;

import net.minecraft.client.Minecraft;

public class MovementUtil {
    public static boolean isMoving() {
        return Minecraft.getMinecraft().thePlayer.movementInput.moveForward != 0.0F || Minecraft.getMinecraft().thePlayer.movementInput.moveStrafe != 0.0F;
    }

    public static double getDirection() {
        float rotationYaw = Minecraft.getMinecraft().thePlayer.rotationYaw;

        if (Minecraft.getMinecraft().thePlayer.moveForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (Minecraft.getMinecraft().thePlayer.moveForward < 0F)
            forward = -0.5F;
        else if (Minecraft.getMinecraft().thePlayer.moveForward > 0F)
            forward = 0.5F;

        if (Minecraft.getMinecraft().thePlayer.moveStrafing > 0F)
            rotationYaw -= 90F * forward;

        if (Minecraft.getMinecraft().thePlayer.moveStrafing < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static void strafe(final float speed) {

        final double yaw = getDirection();
        Minecraft.getMinecraft().thePlayer.motionX = -Math.sin(yaw) * speed;
        Minecraft.getMinecraft().thePlayer.motionZ = Math.cos(yaw) * speed;
    }
}
