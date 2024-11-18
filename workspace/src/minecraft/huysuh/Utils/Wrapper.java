package huysuh.Utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

public final class Wrapper {

    public static float returnNegative(float number) {
        return Math.abs(number)*-1;
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void playerSendMessage(String message) {
        Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
    }

    public static boolean isOnHypixel(){
        return (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel"));
    }

    public static EnumFacing getFacingFromYaw(float yaw) {
        yaw = MathHelper.wrapAngleTo180_float(yaw); // Ensures yaw is within -180 to 180 degrees
        if (yaw >= -45 && yaw < 45) {
            return EnumFacing.SOUTH; // Facing towards negative Z (south)
        } else if (yaw >= 45 && yaw < 135) {
            return EnumFacing.WEST; // Facing towards negative X (west)
        } else if (yaw >= 135 || yaw < -135) {
            return EnumFacing.NORTH; // Facing towards positive Z (north)
        } else if (yaw >= -135 && yaw < -45) {
            return EnumFacing.EAST; // Facing towards positive X (east)
        } else {
            return EnumFacing.NORTH; // Default to north if yaw somehow doesn't fit into the above ranges
        }
    }


    public static AxisAlignedBB getBB(BlockPos block){
        double posX = block.getX() - mc.getRenderManager().getRenderPosX();
        double posY = block.getY() -mc.getRenderManager().getRenderPosY();
        double posZ = block.getZ() - mc.getRenderManager().getRenderPosZ();
        return new AxisAlignedBB(Wrapper.getBlock(block).getBlockBoundsMinX(), Wrapper.getBlock(block).getBlockBoundsMinY(), Wrapper.getBlock(block).getBlockBoundsMinZ(), Wrapper.getBlock(block).getBlockBoundsMaxX(), Wrapper.getBlock(block).getBlockBoundsMaxY(), Wrapper.getBlock(block).getBlockBoundsMaxZ()).offset(posX, posY, posZ);
    }

    public static List<BlockPos> findNearestAirExposed(Block targetBlock, int radius) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);

        int startX = playerPos.getX() - radius;
        int startY = playerPos.getY() - radius;
        int startZ = playerPos.getZ() - radius;

        int endX = playerPos.getX() + radius;
        int endY = playerPos.getY() + radius;
        int endZ = playerPos.getZ() + radius;

        List<BlockPos> foundBlocks = new ArrayList<>();

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    if (mc.theWorld.getBlockState(currentPos).getBlock() == targetBlock && Wrapper.isBlockExposedToAir(currentPos)) {
                        foundBlocks.add(currentPos);
                    }
                }
            }
        }

        return foundBlocks;
    }

    public static List<BlockPos> findNearest(Block targetBlock, int radius) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);

        int startX = playerPos.getX() - radius;
        int startY = playerPos.getY() - radius;
        int startZ = playerPos.getZ() - radius;

        int endX = playerPos.getX() + radius;
        int endY = playerPos.getY() + radius;
        int endZ = playerPos.getZ() + radius;

        List<BlockPos> foundBlocks = new ArrayList<>();

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    if (mc.theWorld.getBlockState(currentPos).getBlock() == targetBlock) {
                        foundBlocks.add(currentPos);
                    }
                }
            }
        }

        return foundBlocks;
    }


    public static boolean isBot(EntityPlayer player){
        for (NetworkPlayerInfo npi : getTablist()){
            if (npi.getGameProfile().getName().equals(player.getName())){
                return false;
            }
        }
        return true;
    }

    public static List<NetworkPlayerInfo> getTablist(){
        NetHandlerPlayClient nethandlerplayclient = mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = GuiPlayerTabOverlay.field_175252_a.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());
        return list;
    }

    public static int findEmpty(EntityPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack == null || stack.getItem() == null || stack.stackSize < 1) {
                return i;
            }
        }
        return -1;
    }

    public static int findItemInHotbar(EntityPlayer player, Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static List<String> getLore(ItemStack item){
        return item.getTooltip(null, false);
    }

    public static int getEnchantLevel(ItemStack item, String enchant) {
        if (item != null) {
            List<String> lore = getLore(item);
            for (String line : lore){
                if (Colors.uncolor(line).toLowerCase().contains(enchant.toLowerCase())){
                    if (Colors.uncolor(line).contains(" III")){
                        return 3;
                    } else if (Colors.uncolor(line).contains(" II")){
                        return 2;
                    } else {
                        return 1;
                    }
                }
            }
        }
        return -1;
    }

    public static boolean hasEnchant(ItemStack item, String enchant) {
        if (item != null) {
            List<String> lore = getLore(item);
            for (String line : lore){
                if (Colors.uncolor(line).toLowerCase().contains(enchant.toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }

    public static int findEnchant(EntityPlayer player, String enchant) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null) {
                List<String> lore = getLore(stack);
                for (String line : lore){
                    if (Colors.uncolor(line).toLowerCase().contains(enchant.toLowerCase())){
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static int findNameInGUI(GuiChest gui, String item) {
        for (int i = 0; i < gui.lowerChestInventory.getSizeInventory(); i++) {
            ItemStack stack = gui.lowerChestInventory.getStackInSlot(i);
            if (stack != null && stack.getItem() != null) {
                //Wrapper.addChatMessage(i + ": " + stack.getDisplayName());
                if (Colors.uncolor(stack.getDisplayName()).equalsIgnoreCase(item)) {
                    return i;
                }
            }
        }
        return -1;
    }


    public static int findItem(EntityPlayer player, Item item) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int findBlock(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                return i;
            }
        }
        return -1;
    }


    public static String getPlayerName(Object playerInfo) {
        if (playerInfo instanceof NetworkPlayerInfo) {
            return ((NetworkPlayerInfo) playerInfo).getGameProfile().getName();
        }
        return null;
    }

    public static boolean hasItem(EntityPlayer player, Item item) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }


    public static ItemStack getStackInSlot(int index) {
        return mc.thePlayer.inventoryContainer.getSlot(index).getStack();
    }

    public static boolean isBlockExposedToAir(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacentPos = pos.offset(facing);
            if (mc.theWorld.getBlockState(adjacentPos).getBlock() == Blocks.air || mc.theWorld.getBlockState(adjacentPos).getBlock() == null) {
                return true;
            }
        }
        return false;
    }

    public static Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }

    public static void addChatMessage(String message) {
        mc.thePlayer.addChatMessage(new ChatComponentText(Colors.color("&7[&fS&7] " + message)));
    }

    public static GuiScreen getCurrentScreen() {
        return mc.currentScreen;
    }

    public static List<EntityPlayer> getLoadedPlayers() {
        return mc.theWorld.playerEntities;
    }

}