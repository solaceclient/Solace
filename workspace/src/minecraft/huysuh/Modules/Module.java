package huysuh.Modules;

import huysuh.Events.Event;
import huysuh.Settings.Setting;
import net.minecraft.client.Minecraft;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a module in the client with associated settings and state
 */
public class Module implements Serializable {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    private static final Set<Module> modules = new HashSet<>();

    // Basic module properties
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting> settings = new ArrayList<>();

    // State management
    private boolean enabled;
    private boolean expanded;
    private int keyCode;

    public Module(String name, Category category, int keyCode) {
        this(name, null, category, keyCode);
    }

    public Module(String name, String description, Category category, int keyCode) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyCode = keyCode;
    }

    public Float lastYPosition;

    // Module event handlers
    protected void onEnable() {}
    protected void onDisable() {}
    protected void onHold() {}
    public void onEvent(Event e) {}

    // Toggle logic
    public void toggle() {
        setEnabled(!enabled);
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    // Static module management
    public static void registerModule(Module module) {
        System.out.println("Attempting to register module: " + module.getName());
        modules.add(module);
        System.out.println("Current module count: " + modules.size());
    }

    public static void keyPressed(int keyCode, boolean pressed) {
        for (Module module : modules) {
            if (module.getKeyCode() == keyCode && pressed) {
                module.toggle();
            }
            module.onHold();
        }
    }

    // Module queries
    public static List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    public static List<Module> getModulesCopy() {
        return new ArrayList<>(modules);
    }

    public static Module getModuleFromModule(Module module) {
        if (module == null) return null;
        return modules.stream()
                .filter(mod -> mod.getName().equals(module.getName()))
                .findFirst()
                .orElse(null);
    }

    public static Module getModuleFromString(String string) {
        if (string == null) return null;
        String normalizedString = string.toLowerCase().replace(" ", "");
        return modules.stream()
                .filter(module -> module.getName().replace(" ", "").toLowerCase().equals(normalizedString))
                .findFirst()
                .orElse(null);
    }

    // Category queries
    public static List<Module> getModulesFromCategories(Category category) {
        return modules.stream()
                .filter(module -> {
                    Category moduleCategory = module.getCategory();
                    while (moduleCategory != null) {
                        if (moduleCategory.equals(category)) {
                            return true;
                        }
                        moduleCategory = moduleCategory.getParent();
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public static List<Module> getModulesFromExactCategory(Category category) {
        System.out.println("Searching for modules in category: " + category.getName());
        modules.forEach(module -> {
            System.out.println("Comparing module: " + module.getName()
                    + " category: " + module.getCategory().getName()
                    + " (" + module.getCategory().hashCode() + ") with "
                    + category.getName() + " (" + category.hashCode() + ")"
                    + " equals: " + module.getCategory().equals(category));
        });

        return modules.stream()
                .filter(module -> module.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    // Getters and setters
    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Setting> getSettings() {
        return this.settings;
    }

    public void addSetting(Setting setting) {
        this.settings.add(setting);
    }

    public void addSettings(Setting... settings) {
        Collections.addAll(this.settings, settings);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Module)) return false;
        Module module = (Module) o;
        return Objects.equals(getName(), module.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}