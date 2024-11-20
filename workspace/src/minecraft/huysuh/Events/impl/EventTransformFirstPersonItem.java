package huysuh.Events.impl;

import huysuh.Events.Event;

public class EventTransformFirstPersonItem extends Event {
    public final float[] defaultTranslations = new float[]{0.56f, -0.52f, -0.71999997f};
    public final float[] defaultScales = new float[]{0.4f, 0.4f, 0.4f};
    public final float defaultItemSwapTranslationY = -0.6f;

    private final float[] translations = defaultTranslations;
    private final float[] scales = defaultScales;
    private float itemSwapTranslationY = defaultItemSwapTranslationY;

    public EventTransformFirstPersonItem() {}

    public float getScaleX () {
        return this.scales[0];
    }

    public float getScaleY() {
        return this.scales[1];
    }

    public float getScaleZ() {
        return this.scales[2];
    }

    public float getTranslationX() {
        return this.translations[0];
    }

    public float getTranslationY() {
        return this.translations[1];
    }

    public float getTranslationZ() {
        return this.translations[2];
    }

    public float getItemSwapTranslationY() {
        return this.itemSwapTranslationY;
    }

    public void setScales(Float... axes) {
        if (axes.length != this.scales.length) { return; }

        for (int i = 0; i < this.scales.length; i++) {
            this.scales[i] = (axes[i] != null) ? axes[i] : this.scales[i];
        }
    }

    public void setTranslations(Float... axes) {
        if (axes.length != this.translations.length) { return; }

        for (int i = 0; i < this.translations.length; i++) {
            this.translations[i] = (axes[i] != null) ? axes[i] : this.translations[i];
        }
    }

    public void setItemSwapTranslationY(float y) {
        this.itemSwapTranslationY = y;
    }
}