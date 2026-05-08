package net.monolith.module;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public String name;
    public String category;
    public boolean enabled;
    
    public List<String> modes = new ArrayList<>();
    public String currentMode = "";
    public boolean expanded = false;

    public float toggleAnim = 0f;
    public float expandAnim = 0f;
    public float hoverAnim = 0f;

    public Module(String name, String category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
    }

    public void toggle() { this.enabled = !this.enabled; }

    public void cycleMode() {
        if (modes.isEmpty()) return;
        int idx = modes.indexOf(currentMode);
        idx = (idx + 1) % modes.size();
        currentMode = modes.get(idx);
    }
}
