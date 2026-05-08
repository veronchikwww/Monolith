package net.monolith.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.utils.RenderUtils;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final List<Panel> panels = new ArrayList<>();
    private boolean initialized = false;
    private float openAnim = 0f;

    public ClickGuiScreen() { super(Text.literal("Monolith DropDown")); }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            String[] categories = {"Combat", "Movement", "Visuals", "Player", "Misc"};
            int panelWidth = 110; int spacing = 15;
            int totalWidth = (categories.length * panelWidth) + ((categories.length - 1) * spacing);
            int startX = (this.width - totalWidth) / 2;
            for (String category : categories) {
                panels.add(new Panel(category, startX, 40, panelWidth));
                startX += panelWidth + spacing;
            }
            initialized = true;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xAA000000, 0xAA000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.2f * delta);
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(0.85f + (0.15f * openAnim), 0.85f + (0.15f * openAnim), 1f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);
        for (Panel panel : panels) panel.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) if (panel.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean shouldPause() { return false; }

    private class Panel {
        public String name; public int x, y, width;
        public Panel(String name, int x, int y, int width) { this.name = name; this.x = x; this.y = y; this.width = width; }
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            List<Module> mods = ModuleManager.getModulesByCategory(name);
            int modsHeight = 0;
            for (Module mod : mods) {
                modsHeight += 18;
                mod.expandAnim = RenderUtils.lerp(mod.expandAnim, (mod.expanded && !mod.modes.isEmpty()) ? 1f : 0f, 0.2f * delta);
                modsHeight += (int)(mod.modes.size() * 14 * mod.expandAnim);
            }
            RenderUtils.drawRoundedRect(context, x, y, width, 24 + modsHeight + 4, 6, 0xFA0F0F11);
            context.drawText(textRenderer, name, x + (width - textRenderer.getWidth(name)) / 2, y + 8, 0xFFFFFF, true);
            context.fillGradient(x + 2, y + 23, x + width - 2, y + 24, 0xFF8E2DE2, 0xFF4A00E0);
            int currentY = y + 26;
            for (Module mod : mods) {
                boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 18;
                mod.hoverAnim = RenderUtils.lerp(mod.hoverAnim, hovered ? 1f : 0f, 0.2f * delta);
                if (mod.enabled || mod.hoverAnim > 0.05f) RenderUtils.drawRoundedRect(context, x + 4, currentY, width - 8, 16, 4, RenderUtils.lerpColor(mod.enabled ? 0xFF8E2DE2 : 0x00000000, mod.enabled ? 0xFF9F3EFF : 0x22FFFFFF, mod.hoverAnim));
                context.drawText(textRenderer, mod.name, x + 12, currentY + 4, RenderUtils.lerpColor(0xFFAAAAAA, 0xFFFFFFFF, mod.enabled ? 1f : mod.hoverAnim), false);
                currentY += 18;
                if (mod.expandAnim > 0.05f) {
                    int modeHeight = (int)(mod.modes.size() * 14 * mod.expandAnim);
                    RenderUtils.drawRect(context, x + 8, currentY, 2, modeHeight, 0xFF8E2DE2);
                    int mY = currentY;
                    for (String mode : mod.modes) {
                        context.drawText(textRenderer, mode, x + 15, mY + 3, mod.currentMode.equals(mode) ? 0xFF8E2DE2 : 0xFFAAAAAA, false);
                        mY += 14;
                    }
                    currentY += modeHeight;
                }
            }
        }
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int currentY = y + 26;
            for (Module mod : ModuleManager.getModulesByCategory(name)) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 18) {
                    if (button == 0) { mod.toggle(); return true; } else if (button == 1 && !mod.modes.isEmpty()) { mod.expanded = !mod.expanded; return true; }
                }
                currentY += 18;
                if (mod.expanded && !mod.modes.isEmpty()) {
                    for (String mode : mod.modes) {
                        if (mouseX >= x + 10 && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 14 && button == 0) { mod.currentMode = mode; return true; }
                        currentY += 14;
                    }
                }
            }
            return false;
        }
    }
}
