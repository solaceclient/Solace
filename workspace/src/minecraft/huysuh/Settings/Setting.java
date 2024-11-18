package huysuh.Settings;

public class Setting {
    private String name;
    private boolean focused;
    private boolean visible = true;

    public boolean isFocused() {
        return this.focused;
    }

    public boolean isVisible() { return this.visible;}

    public void setVisible(boolean hidden) { this.visible = hidden;}

    public String getName() {
        return this.name;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setName(String name) {
        this.name = name;
    }
}