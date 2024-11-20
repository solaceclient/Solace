package huysuh.Utils.Rotation;

import huysuh.Solace;
import huysuh.Utils.Math.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RotationUtils {


        public static float[] getRotations(double posX, double posY, double posZ) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.thePlayer;
            double x = posX - player.posX;
            double y = posY - (player.posY + (double) player.getEyeHeight());
            double z = posZ - player.posZ;
            double dist = (double) MathHelper.sqrt_double(x * x + z * z);
            float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
            float pitch = (float) (-(Math.atan2(y, dist) * 180.0D / Math.PI));
            return new float[]{yaw, pitch};
        }

        public static boolean isMoving() {
            return Solace.mc.thePlayer.movementInput.moveForward != 0.0F || Solace.mc.thePlayer.movementInput.moveStrafe != 0.0F;
        }

        public static float[] getRotationsEntity(EntityLivingBase entity) {
            Minecraft mc = Minecraft.getMinecraft();

            float currentYaw = mc.thePlayer.rotationYaw;
            float currentPitch = mc.thePlayer.rotationPitch;

            return getRotationsEntity(entity, currentYaw, currentPitch);
        }

        public static float[] getRotationsEntity(Entity entity) {
            Minecraft mc = Minecraft.getMinecraft();

            float currentYaw = mc.thePlayer.rotationYaw;
            float currentPitch = mc.thePlayer.rotationPitch;

            return getRotationsEntity(entity, currentYaw, currentPitch);
        }


        public static float[] getRotationsEntity(Entity entity, float currentYaw, float currentPitch) {
            Minecraft mc = Minecraft.getMinecraft();

            List<double[]> points = new ArrayList<>();

            points.add(new double[]{entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ}); // Head
            points.add(new double[]{entity.posX, entity.posY + (entity.height / 2), entity.posZ});   // Center

            if ((int) mc.thePlayer.posY != (int) entity.posY) {
                points.add(new double[]{entity.posX, entity.posY + 0.3, entity.posZ});
            }

            float[] bestRotation = null;
            double smallestAngleDiff = Double.MAX_VALUE;

            for (double[] point : points) {
                float[] rotations = getRotations(
                        point[0] + (isMoving() ? MathUtils.randomNumber(0.03D, -0.03D) : 0),
                        point[1] + (isMoving() ? MathUtils.randomNumber(0.07D, -0.07D) : 0),
                        point[2] + (isMoving() ? MathUtils.randomNumber(0.03D, -0.03D) : 0)
                );

                double yawDiff = Math.abs(angleDifference(rotations[0], currentYaw));
                double pitchDiff = Math.abs(angleDifference(rotations[1], currentPitch));
                double totalDiff = yawDiff + pitchDiff;

                if (totalDiff < smallestAngleDiff) {
                    smallestAngleDiff = totalDiff;
                    bestRotation = rotations;
                }
            }

            return bestRotation;
        }

        public static float angleDifference(float angle1, float angle2) {
            float diff = ((angle1 - angle2) + 180) % 360 - 180;
            return diff < -180 ? diff + 360 : diff;
        }
}
