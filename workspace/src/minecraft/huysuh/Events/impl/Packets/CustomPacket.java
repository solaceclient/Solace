package huysuh.Events.impl.Packets;

import net.minecraft.network.Packet;

public class CustomPacket {
    private final Packet packet;
    private PacketStatus status;

    public enum PacketStatus {
        PRE, // before packet is sent
        POST // after packet is sent
    }

    public CustomPacket(Packet packet, PacketStatus status) {
        this.packet = packet;
        this.status = status;
    }

    public Packet getPacket() {
        return this.packet;
    }

    public PacketStatus getPacketStatus() {
        return this.status;
    }

    public void setPacketStatus(PacketStatus status) {
        this.status = status;
    }
}