package huysuh;

import huysuh.Events.Event;
import huysuh.Modules.Module;
import huysuh.Modules.impl.Combat.KillAura;
import huysuh.Modules.impl.Render.Animations;
import huysuh.Modules.impl.Render.Camera;
import huysuh.Modules.impl.Render.ClickGUI;
import huysuh.Modules.impl.Render.HUD;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Solace {
    public static final String name = "Solace";
    public static final double version = 0.1;

    public static Minecraft mc;

    private static final List<Module> modules = new ArrayList<Module>(Arrays.asList(
            new HUD(),
            new ClickGUI(),
            new KillAura(),
            new Animations(),
            new Camera()
    ));

    private static final List<Event> tickEvents = new ArrayList<>(Collections.emptyList());

    public static void start() {
        Display.setTitle(name + " " + version);

        mc = Minecraft.getMinecraft();

        System.out.println("Total modules to register: " + modules.size());

        modules.forEach(module -> {
            Module.registerModule(module);
            System.out.println("Registered module: " + module.getName() + " in category: " + module.getCategory().getName());
        });

        System.out.println("Total registered modules: " + Module.getModules().size());

        Event.tickEvents.addAll(tickEvents);
    }
}
