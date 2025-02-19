package huysuh.Modules;

import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Category implements Serializable {
    private final String name;
    private final int color;
    private final Category parent;
    private final List<Category> subcategories = new ArrayList<>();

    // Root categories
    public static final Category COMBAT = new Category("Combat", 0xFF7aff9e);
    public static final Category MOVEMENT = new Category("Movement", 0xFFff7a7a);
    public static final Category PLAYER = new Category("Player", 0xFFffd77a);
    public static final Category RENDER = new Category("Render", 0xFFd17aff);
    public static final Category WORLD = new Category("World", 0xFF7ac4ff);

    // Sub categories
    public static final Category SCREEN = new Category("Screen", 0xFFe17aff, RENDER);

    // Root categories
    private Category(String name, int color) {
        this(name, color, null);
    }

    // Sub categories
    public Category(String name, int color, @Nullable Category parent) {
        this.name = name;
        this.color = color;
        this.parent = parent;
        if (parent != null) {
            parent.subcategories.add(this);
        }
    }

    private static final List<Category> ALL_CATEGORIES = new ArrayList<>();

    static {
        ALL_CATEGORIES.add(COMBAT);
        ALL_CATEGORIES.add(MOVEMENT);
        ALL_CATEGORIES.add(PLAYER);
        ALL_CATEGORIES.add(RENDER);
        ALL_CATEGORIES.add(WORLD);
        ALL_CATEGORIES.add(SCREEN);
    }

    public static Category[] values() {
        return ALL_CATEGORIES.toArray(new Category[0]);
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    @Nullable
    public Category getParent() {
        return parent;
    }

    public List<Category> getSubcategories() {
        return Collections.unmodifiableList(subcategories);
    }

    public boolean hasSubcategories() {
        return !subcategories.isEmpty();
    }

    public boolean isSubcategory() {
        return parent != null;
    }

    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(name, category.name) &&
                Objects.equals(parent, category.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parent);
    }
}
