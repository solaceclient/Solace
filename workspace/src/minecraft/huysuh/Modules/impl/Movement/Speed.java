package huysuh.Modules.impl.Movement;

import huysuh.Events.Event;
import huysuh.Events.impl.EventMotion;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.ModeSetting;
import huysuh.Utils.Timer;
import org.lwjgl.input.Keyboard;

public class Speed extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Motion");

    public Speed() {
        super("Speed", "Increases your movement speed", Category.MOVEMENT, Keyboard.KEY_V);
        this.addSettings(mode);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventMotion) {
            if (mc.thePlayer == null || mc.theWorld == null) return;

            this.setTag(mode.getMode());

            if ((mc.thePlayer.moveStrafing == 0 && mc.thePlayer.moveForward == 0) || mc.thePlayer.isCollidedHorizontally){
                return;
            }

            switch (mode.getMode()){
                case "Motion":
                    if (!mc.thePlayer.onGround){
                        mc.thePlayer.motionY -= 555;
                    }
                    mc.thePlayer.motionX *= 1.25;
                    mc.thePlayer.motionZ *= 1.25;
                    break;
            }
        }
    }
}
