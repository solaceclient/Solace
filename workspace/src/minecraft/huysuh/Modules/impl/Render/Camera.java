package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventTick;
import huysuh.Events.impl.EventTransformFirstPersonItem;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.NumberSetting;
import org.lwjgl.input.Keyboard;

public class Camera extends Module {

    private final NumberSetting translationX = new NumberSetting("Position X", 56, 1, 100, 1);
    private final NumberSetting translationY = new NumberSetting("Position Y", 52, 1, 200, 1); // negative
    private final NumberSetting translationZ = new NumberSetting("Position Z", 72, 1, 200, 1); // negative
    private final NumberSetting scale = new NumberSetting("Scale", 40, 1, 100, 1);
    private final NumberSetting itemSwapTranslationY = new NumberSetting("Swap Y", 60, 0, 200, 1); // negative
    public final NumberSetting swingSpeed = new NumberSetting("Swing Speed", 60, 0, 300, 1);

    public Camera() {
        super("Camera", "Allows you to change held item rendering", Category.RENDER, Keyboard.KEY_NONE);
        this.addSettings(translationX, translationY, translationZ, scale, itemSwapTranslationY, swingSpeed);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventTransformFirstPersonItem){
            ((EventTransformFirstPersonItem) e).setTranslations((float) translationX.getValue() / 100,
                    (float) ((translationY.getValue() >= 100) ? translationY.getValue() : -translationY.getValue()) / 100,
                    (float) ((translationZ.getValue() >= 100) ? translationZ.getValue() : -translationZ.getValue()) / 100);

            ((EventTransformFirstPersonItem) e).setScales((float) scale.getValue() / 100,
                    (float) scale.getValue() / 100,
                    (float) scale.getValue() / 100);

            ((EventTransformFirstPersonItem) e).setItemSwapTranslationY((float) ((itemSwapTranslationY.getValue() >= 100) ? itemSwapTranslationY.getValue() : -itemSwapTranslationY.getValue()) / 100);
        }
    }

}
