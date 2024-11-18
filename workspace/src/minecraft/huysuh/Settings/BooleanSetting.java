package huysuh.Settings;

public class BooleanSetting extends Setting {
    private boolean enabled;

    public BooleanSetting(String name, boolean enabled) {
        this.setName(name);
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

}