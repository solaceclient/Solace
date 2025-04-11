package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventTick;
import huysuh.Events.impl.EventTransformFirstPersonItem;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.BooleanSetting;
import huysuh.Settings.NumberSetting;
import huysuh.UI.Notification.Notifications;
import org.lwjgl.input.Keyboard;

public class Notifs extends Module {

    public static BooleanSetting toggleNotifications = new BooleanSetting("Toggle Notifications", false);

    public Notifs() {
        super("Notifications", "Allows customization of the notifications", Category.RENDER, Keyboard.KEY_NONE);
        this.addSettings(toggleNotifications);
    }

    @Override
    protected void onEnable() {
        this.enabled = false;
        Notifications.add(this.getName(), "This module does not turn on!", Notifications.NotificationType.INFO);
    }
}
