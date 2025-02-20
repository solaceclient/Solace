package huysuh.Modules.impl.Movement;

import huysuh.Events.Event;
import huysuh.Events.impl.EventMotion;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.ModeSetting;
import huysuh.Utils.Timer;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Normal", "Normal", "Multi");
    private final Timer sprintTimer = new Timer();

    public Sprint() {
        super("Sprint", "Allows the player to sprint", Category.MOVEMENT, Keyboard.KEY_NONE);
        this.addSettings(mode);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventMotion) {
            if (mc.thePlayer == null || mc.theWorld == null) return;

            this.setTag(mode.getMode());

            boolean shouldSprint = mc.thePlayer.getFoodStats().getFoodLevel() > 6 && !mc.thePlayer.isSneaking() && !mc.thePlayer.isCollidedHorizontally;

            if (mode.getMode().equals("Multi")) {
                shouldSprint = shouldSprint && (mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0);
            } else {
                shouldSprint = shouldSprint && mc.thePlayer.moveForward > 0;
            }

            mc.thePlayer.setSprinting(shouldSprint);
        }
    }
}
