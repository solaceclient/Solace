package huysuh.Events.impl;

import huysuh.Events.Event;
import net.minecraft.client.gui.ScaledResolution;

public class EventRender3D extends Event {
    private final float partialTicks;

    public EventRender3D(float partialTicks){

        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
