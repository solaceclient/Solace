package huysuh.Modules.impl.Render;

import huysuh.Events.Event;
import huysuh.Events.impl.EventRender2D;
import huysuh.Events.impl.EventTick;
import huysuh.Font.CFontRenderer;
import huysuh.Font.Fonts;
import huysuh.Modules.Category;
import huysuh.Modules.Module;
import huysuh.Settings.BooleanSetting;
import huysuh.Settings.ColorSetting;
import huysuh.Settings.ModeSetting;
import huysuh.Solace;
import huysuh.Utils.Colors;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Animations extends Module {

    public ModeSetting animation = new ModeSetting("Animations", "Virtue", "1.8", "1.7", "Slash", "Tap", "Spin",
            "Swong", "Push", "Stab", "Swank", "Exhibition", "Summer", "Autumn", "Winter", "Spring",
            "Punch", "Zoom", "Swing", "Slide", "Wave");

    public String getBlockAnimation(){
        if (this.isEnabled()){ return animation.getMode(); }
        return "1.7";
    }

    public Animations() {
        super("Animations", "Heads up display", Category.RENDER, Keyboard.KEY_H);
        this.addSettings(animation);
    }

    @Override
    public void onEvent(Event e) {
        if (e instanceof EventRender2D){
            if (!this.isEnabled()){
                return;
            }
            this.setTag(animation.getMode());
        }
    }
}