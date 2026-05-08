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

    public ClickGuiScreen() {
        super(Text.literal("Monolith Gui"));
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            String[] categories = {"Combat", "Movement", "Visuals", "Player", "Misc"};
            int panelWidth = 115;
            int spacing = 12;
            int totalWidth = (categories.length * panelWidth) + ((categories.length - 1) * spacing);
            int startX = (this.width - totalWidth) / 2;
            int startY = 30;

            for (String category : categories) {
                panels.add(new Panel(category, startX, startY, panelWidth));
                startX += panelWidth + spacing;
            }
            initialized = true;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0x88000000, 0x88000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.2f * delta);
        
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(0.85f + (0.15f * openAnim), 0.85f + (0.15f * openAnim), 1f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        for (Panel panel : panels) {
            panel.render(context, mouseX, mouseY, delta);
        }
        
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            panel.mouseReleased(button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private class Panel {
        public String name;
        public int x, y, width;
        public boolean dragging;
        private int dragX, dragY;

        public Panel(String name, int x, int y, int width) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (dragging) {
                x = mouseX - dragX;
                y = mouseY - dragY;
            }

            int headerHeight = 24;
            List<Module> mods = ModuleManager.getModulesByCategory(name);
            
            // Динамический расчет высоты с учетом открытых настроек
            int extraHeight = 0;
            for (Module mod : mods) {
                if (mod.expanded && !mod.modes.isEmpty()) {
                    extraHeight += mod.modes.size() * 14 + 2;
                }
            }
            int totalHeight = headerHeight + (mods.size() * 18) + (mods.isEmpty() ? 0 : 6) + extraHeight;

            RenderUtils.drawRoundedRect(context, x, y, width, totalHeight, 6, 0xEE121215);
            context.drawText(textRenderer, name, x + (width - textRenderer.getWidth(name)) / 2, y + 8, 0xFFFFFF, true);
            context.fillGradient(x + 5, y + headerHeight - 2, x + width - 5, y + headerHeight - 1, 0xFF8E2DE2, 0xFF4A00E0);

            int modY = y + headerHeight + 2;
            for (Module mod : mods) {
                boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= modY && mouseY <= modY + 18;
                
                if (mod.enabled) {
                    RenderUtils.drawRoundedRect(context, x + 4, modY, width - 8, 16, 4, 0xAA8E2DE2);
                } else if (hovered) {
                    RenderUtils.drawRoundedRect(context, x + 4, modY, width - 8, 16, 4, 0x33FFFFFF);
                }
                
                int color = mod.enabled ? 0xFFFFFF : (hovered ? 0xDDDDDD : 0x999999);
                context.drawText(textRenderer, mod.name, x + 12, modY + 4, color, false);
                
                // Индикатор наличия настроек
                if (!mod.modes.isEmpty()) {
                    context.drawText(textRenderer, mod.expanded ? "-" : "+", x + width - 15, modY + 4, 0x777777, false);
                }
                
                modY += 18;

                // Отрисовка режимов если расширено
                if (mod.expanded && !mod.modes.isEmpty()) {
                    RenderUtils.drawRect(context, x + 8, modY, 2, mod.modes.size() * 14, 0xFF8E2DE2); // Линия слева
                    for (String mode : mod.modes) {
                        boolean modeHovered = mouseX >= x && mouseX <= x + width && mouseY >= modY && mouseY <= modY + 14;
                        if (modeHovered) RenderUtils.drawRect(context, x + 10, modY, width - 14, 14, 0x11FFFFFF);
                        
                        boolean isCurrent = mod.currentMode.equals(mode);
                        context.drawText(textRenderer, mode, x + 15, modY + 3, isCurrent ? 0xFF8E2DE2 : 0xFFAAAAAA, false);
                        modY += 14;
                    }
                    modY += 2;
                }
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 24) {
                if (button == 0) {
                    dragging = true;
                    dragX = (int) (mouseX - x);
                    dragY = (int) (mouseY - y);
                    return true;
                }
            }

            int modY = y + 26;
            for (Module mod : ModuleManager.getModulesByCategory(name)) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= modY && mouseY <= modY + 18) {
                    if (button == 0) {
                        mod.toggle();
                        return true;
                    } else if (button == 1) { // ПКМ для открытия настроек
                        if (!mod.modes.isEmpty()) mod.expanded = !mod.expanded;
                        return true;
                    }
                }
                modY += 18;

                if (mod.expanded && !mod.modes.isEmpty()) {
                    for (String mode : mod.modes) {
                        if (mouseX >= x && mouseX <= x + width && mouseY >= modY && mouseY <= modY + 14) {
                            if (button == 0) {
                                mod.currentMode = mode;
                                return true;
                            }
                        }
                        modY += 14;
                    }
                    modY += 2;
                }
            }
            return false;
        }

        public void mouseReleased(int button) {
            if (button == 0) dragging = false;
        }
    }
}
