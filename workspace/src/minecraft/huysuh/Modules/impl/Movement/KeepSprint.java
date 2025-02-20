package huysuh.Modules.impl.Movement;

import huysuh.Events.Event;
import huysuh.Events.impl.EventSlowdown;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.ModeSetting;
import org.lwjgl.input.Keyboard;

public class KeepSprint extends Module {

    public KeepSprint() {
        super("KeepSprint", "Stops your sprint from being reset", Category.MOVEMENT, Keyboard.KEY_NONE);
    }

    @Override
    public void onEvent(Event e) {
        if (this.isEnabled()) {
            if (e instanceof EventSlowdown) {
                if (((EventSlowdown) e).getType().equals(EventSlowdown.SlowdownType.SPRINT)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
