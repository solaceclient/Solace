package huysuh.Events.impl;

import huysuh.Events.Event;

public class EventSlowdown extends Event {

    SlowdownType type;

    public enum SlowdownType {
        SPRINT,
        WATER,
        MOTION
    }

    public SlowdownType getType() {
        return type;
    }

    public void setType(SlowdownType type) {
        this.type = type;
    }

    public EventSlowdown(SlowdownType type) {
        this.type = type;
    }
}