package huysuh.Modules.impl.Movement;

import huysuh.Events.Event;
import huysuh.Events.impl.EventMotion;
import huysuh.Events.impl.EventRender2D;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.BooleanSetting;
import huysuh.Settings.ModeSetting;
import huysuh.Settings.NumberSetting;
import huysuh.Utils.Movement.MovementUtil;
import org.lwjgl.input.Keyboard;

public class Flight extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Motion");
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
    private final BooleanSetting stop = new BooleanSetting("Stop Motion", true);

    public Flight() {
        super("Flight", "Allows you to fly", Category.MOVEMENT, Keyboard.KEY_F);
        this.addSettings(mode, speed, stop);
    }

    @Override
    protected void onDisable() {
        if (stop.isEnabled()){
            MovementUtil.strafe(0);
        }
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventRender2D) {
            if (mc.thePlayer == null || mc.theWorld == null) return;

            this.setTag(mode.getMode());

            switch (mode.getMode()){
                case "Motion":
                    if (mc.thePlayer.onGround){
                        mc.thePlayer.jump();
                    } else {
                        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
                            mc.thePlayer.motionY = speed.getValue();
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
                            mc.thePlayer.motionY = speed.getValue() - (speed.getValue() * 2);
                        } else {
                            mc.thePlayer.motionY = 0;
                            if (stop.isEnabled()){
                                MovementUtil.strafe(0);
                            }
                        }
                    }
                    if (mc.thePlayer.moveStrafing != 0 || mc.thePlayer.moveForward != 0){
                        MovementUtil.strafe(speed.getValueFloat());
                    }
                    break;
            }
        }
    }
}
