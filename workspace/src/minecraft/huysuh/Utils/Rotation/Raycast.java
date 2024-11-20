package huysuh.Utils.Rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.List;

public class Raycast {

    public static Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public static Object[] getEntity2(double reach, double expand, float[] rotations) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewerEntity = mc.getRenderViewEntity();
        Entity entity = null;
        if (viewerEntity == null) {
            return null;
        } else {
            mc.mcProfiler.startSection("pick");
            Vec3 viewerPos = viewerEntity.getPositionEyes(1.0F);
            Vec3 viewerLook;
            if (rotations != null) {
                viewerLook = getVectorForRotation(rotations[1], rotations[0]);
            } else {
                viewerLook = viewerEntity.getLook(1.0F);
            }
            Vec3 reachPoint = viewerPos.addVector(viewerLook.xCoord * reach, viewerLook.yCoord * reach, viewerLook.zCoord * reach);
            Vec3 hitVec = null;
            List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(viewerEntity, viewerEntity.getEntityBoundingBox().addCoord(viewerLook.xCoord * reach, viewerLook.yCoord * reach, viewerLook.zCoord * reach).expand(1.0D, 1.0D, 1.0D));
            double closestDistance = reach;

            for (Entity potentialTarget : entities) {
                if (potentialTarget.canBeCollidedWith()) {
                    float borderSize = (float) ((double) potentialTarget.getCollisionBorderSize());
                    AxisAlignedBB entityBoundingBox = potentialTarget.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
                    entityBoundingBox = entityBoundingBox.expand(expand, expand, expand);
                    MovingObjectPosition intercept = entityBoundingBox.calculateIntercept(viewerPos, reachPoint);
                    if (entityBoundingBox.isVecInside(viewerPos)) {
                        if (0.0D < closestDistance || closestDistance == 0.0D) {
                            entity = potentialTarget;
                            hitVec = intercept == null ? viewerPos : intercept.hitVec;
                            closestDistance = 0.0D;
                        }
                    } else if (intercept != null) {
                        double distanceToHitVec = viewerPos.distanceTo(intercept.hitVec);
                        if (distanceToHitVec < closestDistance || closestDistance == 0.0D) {
                            entity = potentialTarget;
                            hitVec = intercept.hitVec;
                            closestDistance = distanceToHitVec;
                        }
                    }
                }
            }

            if (closestDistance < reach && !(entity instanceof EntityLivingBase) && !(entity instanceof EntityItemFrame)) {
                entity = null;
            }

            mc.mcProfiler.endSection();
            if (entity != null && hitVec != null) {
                return new Object[]{entity, hitVec};
            } else {
                return null;
            }
        }
    }


    public static Object[] getEntity(Entity target, double reach, double expand, float[] rotations) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewerEntity = mc.getRenderViewEntity();
        Entity entity = null;
        if (viewerEntity == null || target == null) {
            return null;
        } else {
            mc.mcProfiler.startSection("pick");
            Vec3 viewerPos = viewerEntity.getPositionEyes(1.0F);
            Vec3 viewerLook;
            if (rotations != null) {
                viewerLook = getVectorForRotation(rotations[1], rotations[0]);
            } else {
                viewerLook = viewerEntity.getLook(1.0F);
            }
            Vec3 reachPoint = viewerPos.addVector(viewerLook.xCoord * reach, viewerLook.yCoord * reach, viewerLook.zCoord * reach);
            Vec3 hitVec = null;
            List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(viewerEntity, viewerEntity.getEntityBoundingBox().addCoord(viewerLook.xCoord * reach, viewerLook.yCoord * reach, viewerLook.zCoord * reach).expand(1.0D, 1.0D, 1.0D));
            double closestDistance = reach;

            for (Entity potentialTarget : entities) {
                if (potentialTarget.canBeCollidedWith() && potentialTarget.equals(target)) {
                    float borderSize = (float) ((double) potentialTarget.getCollisionBorderSize());
                    AxisAlignedBB entityBoundingBox = potentialTarget.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
                    entityBoundingBox = entityBoundingBox.expand(expand, expand, expand);
                    MovingObjectPosition intercept = entityBoundingBox.calculateIntercept(viewerPos, reachPoint);
                    if (entityBoundingBox.isVecInside(viewerPos)) {
                        if (0.0D < closestDistance || closestDistance == 0.0D) {
                            entity = potentialTarget;
                            hitVec = intercept == null ? viewerPos : intercept.hitVec;
                            closestDistance = 0.0D;
                        }
                    } else if (intercept != null) {
                        double distanceToHitVec = viewerPos.distanceTo(intercept.hitVec);
                        if (distanceToHitVec < closestDistance || closestDistance == 0.0D) {
                            entity = potentialTarget;
                            hitVec = intercept.hitVec;
                            closestDistance = distanceToHitVec;
                        }
                    }
                }
            }

            if (closestDistance < reach && !(entity instanceof EntityLivingBase) && !(entity instanceof EntityItemFrame)) {
                entity = null;
            }

            mc.mcProfiler.endSection();
            if (entity != null && hitVec != null) {
                return new Object[]{entity, hitVec};
            } else {
                return null;
            }
        }
    }

}