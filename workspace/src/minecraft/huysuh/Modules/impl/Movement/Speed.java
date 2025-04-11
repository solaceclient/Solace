package huysuh.Modules.impl.Movement;

import huysuh.Events.Event;
import huysuh.Events.impl.EventMotion;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.ModeSetting;
import huysuh.Utils.Movement.MovementUtil;
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

            if ((mc.thePlayer.moveStrafing == 0 && mc.thePlayer.moveForward == 0)){
                return;
            }

            switch (mode.getMode()){
                case "Motion":
                    if (mc.thePlayer.onGround){
                        mc.thePlayer.jump();
                    } else {
                        if (mc.thePlayer.motionY < 0 && mc.thePlayer.motionY > -0.1){
                            mc.thePlayer.motionY -= 0.25;
                        }
                    }
                    MovementUtil.strafe(1.0f);
                    break;
            }
        }
    }
}
