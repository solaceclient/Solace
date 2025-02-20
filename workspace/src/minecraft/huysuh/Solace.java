package huysuh;

import huysuh.Events.Event;
import huysuh.Modules.Module;
import huysuh.Modules.impl.Combat.KillAura;
import huysuh.Modules.impl.Combat.Velocity;
import huysuh.Modules.impl.Movement.KeepSprint;
import huysuh.Modules.impl.Movement.NoSlow;
import huysuh.Modules.impl.Movement.Speed;
import huysuh.Modules.impl.Movement.Sprint;
import huysuh.Modules.impl.Render.*;
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
            new Camera(),
            new Sprint(),
            new NoSlow(),
            new KeepSprint(),
            new Speed(),
            new TargetHUD(),
            new Velocity()
    ));

    private static final List<Event> tickEvents = new ArrayList<>(Collections.emptyList());

    public static void start() {
        Display.setTitle(name + " " + version);

        mc = Minecraft.getMinecraft();

        System.out.println("Total modules to register: " + modules.size());

        modules.forEach(module -> {
            Module.registerModule(module);
            System.out.println("Registered module: " + module.getName() + " in category: " + module.getCategory().getName());
            if (module instanceof HUD || module instanceof Sprint || module instanceof Animations){
                module.setEnabled(true);
            }
        });

        System.out.println("Total registered modules: " + Module.getModules().size());

        Event.tickEvents.addAll(tickEvents);
    }
}
