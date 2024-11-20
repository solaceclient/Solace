package huysuh.Events;

import huysuh.Modules.Module;
import huysuh.Utils.Wrapper;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private boolean cancelled;
    private Era era = Era.PRE;
    public static List<Event> tickEvents = new ArrayList<>();

    public enum Era {
        PRE, POST
    }

    public Event fire(Era era) {
        this.era = era;
        for (Module module : Module.getModules()) {
            if (module.isEnabled()) {
                //Wrapper.addChatMessage(this.getClass().getSimpleName() + " fired for " + module.getName());
                module.onEvent(this);
            }
        }
        return this;
    }

    public Event fire() {
        //Wrapper.addChatMessage(this.getClass().getSimpleName() + " fired");
        fire(Era.PRE);
        if (!isCancelled()) {
            fire(Era.POST);
        }
        return this;
    }

    public Era getEra() {
        return era;
    }
    public void setEra(Era era) { this.era = era; }

    public boolean isPre() {
        return era == Era.PRE;
    }

    public boolean isPost() {
        return era == Era.POST;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}