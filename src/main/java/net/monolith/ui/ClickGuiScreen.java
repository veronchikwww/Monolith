package net.monolith.ui;

import net.minecraft.client.MinecraftClient;
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
    private final int ACCENT = 0xFF00E5FF;

    public ClickGuiScreen() { super(Text.literal("Monolith DropDown")); }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            String[] categories = {"Combat", "Movement", "Visuals", "Player", "Misc"};
            int panelWidth = 120; int spacing = 15;
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
        context.fillGradient(0, 0, this.width, this.height, 0x88000000, 0xBB000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.15f * delta);
        
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
                modsHeight += 20;
                mod.expandAnim = RenderUtils.lerp(mod.expandAnim, (mod.expanded && !mod.modes.isEmpty()) ? 1f : 0f, 0.2f * delta);
                modsHeight += (int)(mod.modes.size() * 20 * mod.expandAnim); 
            }
            
            int totalHeight = 26 + modsHeight + 4;
            RenderUtils.drawRoundedRect(context, x - 2, y - 2, width + 4, totalHeight + 4, 8, 0x33000000);
            RenderUtils.drawRoundedRect(context, x, y, width, totalHeight, 6, 0xFA08080A);
            
            RenderUtils.drawCenteredText(context, textRenderer, name, x + width / 2, y + 9, 0xFFFFFF, true);
            RenderUtils.drawRect(context, x + 10, y + 24, width - 20, 1, 0x33FFFFFF);

            int currentY = y + 28;
            for (Module mod : mods) {
                boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 20;
                mod.hoverAnim = RenderUtils.lerp(mod.hoverAnim, hovered ? 1f : 0f, 0.2f * delta);
                mod.toggleAnim = RenderUtils.lerp(mod.toggleAnim, mod.enabled ? 1f : 0f, 0.2f * delta);
                
                if (mod.hoverAnim > 0.05f || mod.toggleAnim > 0.05f) {
                    int bgAlpha = (int)(25 * Math.max(mod.hoverAnim, mod.toggleAnim));
                    RenderUtils.drawRoundedRect(context, x + 4, currentY, width - 8, 20, 4, (bgAlpha << 24) | (ACCENT & 0xFFFFFF));
                }
                
                if (mod.toggleAnim > 0.05f) {
                    RenderUtils.drawRoundedRect(context, x + 4, currentY + 4, 2, 12, 1, ACCENT);
                }
                
                int textColor = RenderUtils.lerpColor(0xFFAAAAAA, 0xFFFFFFFF, Math.max(mod.toggleAnim, mod.hoverAnim));
                RenderUtils.drawText(context, textRenderer, mod.name, x + 14, currentY + 6, textColor, false);
                
                if (!mod.modes.isEmpty()) {
                    RenderUtils.drawText(context, textRenderer, mod.expanded ? "-" : "+", x + width - 15, currentY + 6, 0x666666, false);
                }
                
                currentY += 20;

                if (mod.expandAnim > 0.01f) {
                    int modeHeight = (int)(mod.modes.size() * 20 * mod.expandAnim);
                    RenderUtils.drawRect(context, x + 14, currentY, 1, Math.max(0, modeHeight - 2), ACCENT); 
                    
                    context.enableScissor(x, currentY, x + width, currentY + modeHeight);
                    int mY = currentY;
                    int textAlpha = (int)(255 * Math.min(1f, mod.expandAnim * 1.5f)); 
                    
                    for (String mode : mod.modes) {
                        boolean isCurrent = mod.currentMode.equals(mode);
                        int modeColor = isCurrent ? ACCENT : 0xFF777777;
                        modeColor = (modeColor & 0x00FFFFFF) | (textAlpha << 24); 
                        
                        RenderUtils.drawText(context, textRenderer, mode, x + 20, mY + 6, modeColor, false);
                        mY += 20;
                    }
                    context.disableScissor();
                    currentY += modeHeight;
                }
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int currentY = y + 28;
            for (Module mod : ModuleManager.getModulesByCategory(name)) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 20) {
                    if (button == 0) { mod.toggle(); return true; } 
                    else if (button == 1 && !mod.modes.isEmpty()) { mod.expanded = !mod.expanded; return true; }
                }
                currentY += 20;
                if (mod.expanded && !mod.modes.isEmpty()) {
                    for (String mode : mod.modes) {
                        if (mouseX >= x + 10 && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 20 && button == 0) { 
                            mod.currentMode = mode; 
                            if (mod.name.equals("ClickGui")) {
                                if (mode.equals("Cards")) MinecraftClient.getInstance().setScreen(new CardsGuiScreen());
                                else if (mode.equals("CsGui")) MinecraftClient.getInstance().setScreen(new CsGuiScreen());
                            }
                            return true; 
                        }
                        currentY += 20;
                    }
                }
            }
            return false;
        }
    }
}
