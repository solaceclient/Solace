package huysuh.Events.impl;

import huysuh.Events.Event;
import net.minecraft.client.gui.ScaledResolution;

public class EventRender2D extends Event {
    private final float partialTicks;
    private final ScaledResolution scaledResolution;

    public EventRender2D(float partialTicks, ScaledResolution scaledResolution) {
        this.partialTicks = partialTicks;
        this.scaledResolution = scaledResolution;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public ScaledResolution getScaledResolution() {
        return scaledResolution;
    }

    public int getWidth() {
        return scaledResolution.getScaledWidth();
    }

    public int getHeight() {
        return scaledResolution.getScaledHeight();
    }

    public double getScaleFactor() {
        return scaledResolution.getScaleFactor();
    }
}