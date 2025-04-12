package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventRender2D;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.ModeSetting;
import org.lwjgl.input.Keyboard;

public class NoHurtCam extends Module {

    public NoHurtCam() {
        super("NoHurtCam", "NoHurtCam", Category.RENDER, Keyboard.KEY_NONE);
    }
}