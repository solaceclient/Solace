package huysuh.Modules.impl.Movement;

import huysuh.Events.Event;
import huysuh.Events.impl.EventMotion;
import huysuh.Events.impl.EventRender2D;
import huysuh.Events.impl.EventSlowdown;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.ModeSetting;
import huysuh.Utils.Timer;
import huysuh.Utils.Wrapper;
import org.lwjgl.input.Keyboard;

public class NoSlow extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla");

    public NoSlow() {
        super("NoSlow", "Removes slowdown", Category.MOVEMENT, Keyboard.KEY_NONE);
        this.addSettings(mode);
    }

    @Override
    public void onEvent(Event e) {
        if (this.isEnabled()){
            if (e instanceof EventRender2D){
                this.setTag(mode.getMode());
            }
            if (e instanceof EventSlowdown){
                if (((EventSlowdown)e).getType().equals(EventSlowdown.SlowdownType.MOTION)){
                    e.setCancelled(true);
                }
            }
        }
    }
}
