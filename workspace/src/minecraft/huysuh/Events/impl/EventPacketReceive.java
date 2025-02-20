package huysuh.Events.impl;

import huysuh.Events.Event;
import huysuh.Events.impl.Packets.CustomPacket;

public class EventPacketReceive extends Event {
    private CustomPacket customPacket;

    public EventPacketReceive(CustomPacket customPacket) {
        this.customPacket = customPacket;
    }

    public CustomPacket getCustomPacket() {
        return this.customPacket;
    }

    public void setCustomPacket(CustomPacket customPacket) {
        this.customPacket = customPacket;
    }
}