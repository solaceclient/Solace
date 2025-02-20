package huysuh.Events.impl;
import huysuh.Events.Event;
import huysuh.Events.impl.Packets.CustomPacket;

public class EventPacketSend extends Event {
    private CustomPacket customPacket;

    public EventPacketSend(CustomPacket customPacket) {
        this.customPacket = customPacket;
    }

    public CustomPacket getCustomPacket() {
        return this.customPacket;
    }

    public void setCustomPacket(CustomPacket customPacket) {
        this.customPacket = customPacket;
    }
}