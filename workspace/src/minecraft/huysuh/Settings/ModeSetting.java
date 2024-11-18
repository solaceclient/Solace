package huysuh.Settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    private int index;
    private List<String> modes;

    public ModeSetting(String name, String... modes) {
        this.setName(name);

        this.index = 0;
        this.modes = Arrays.asList(modes);
    }

    public String getMode() {
        return this.modes.get(this.index);
    }

    public void setMode(String mode) {
        if (!this.modes.contains(mode)) { return; }
        this.index = this.modes.indexOf(mode);
    }

    public boolean is(String mode) {
        return this.index == this.modes.indexOf(mode);
    }

    public void cycle() {
        if (this.index < this.modes.size() - 1) {
            this.index++;
        } else {
            this.index = 0;
        }
    }

    public List<String> getModes() {
        return this.modes;
    }

    public void setModes(List<String> modes) {
        this.modes = modes;
    }
}