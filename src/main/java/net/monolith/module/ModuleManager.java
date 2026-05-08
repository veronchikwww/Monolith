package net.monolith.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();

    public static void init() {
        modules.add(new Module("AttackAura", "Combat"));
        
        Module hudModule = new Module("HUD", "Visuals");
        hudModule.enabled = true;
        modules.add(hudModule);
        
        modules.add(new Module("Fullbright", "Visuals"));
        
        Module clickGui = new Module("ClickGui", "Visuals");
        clickGui.modes = Arrays.asList("DropDown", "TestCustom");
        clickGui.currentMode = "DropDown";
        modules.add(clickGui);
    }

    public static List<Module> getModulesByCategory(String category) {
        List<Module> result = new ArrayList<>();
        for (Module m : modules) {
            if (m.category.equals(category)) {
                result.add(m);
            }
        }
        return result;
    }

    public static Module getModule(String name) {
        for (Module m : modules) {
            if (m.name.equalsIgnoreCase(name)) return m;
        }
        return null;
    }
}
