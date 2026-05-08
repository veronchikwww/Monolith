package net.monolith.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.utils.RenderUtils;
import java.util.List;

public class ModernGuiScreen extends Screen {
    private String currentCategory = "Combat";
    private float openAnim = 0f;
    private float tabSlideY = 50f;
    private final String[] categories = {"Combat", "Movement", "Visuals", "Player", "Misc"};

    public ModernGuiScreen() { super(Text.literal("Monolith Modern")); }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0x99000000, 0xBB000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.2f * delta);
        int w = 400; int h = 260; int x = (this.width - w) / 2; int y = (this.height - h) / 2;

        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(0.95f + (0.05f * openAnim), 0.95f + (0.05f * openAnim), 1f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        RenderUtils.drawRoundedRect(context, x, y, w, h, 10, 0xFA0D0D0F);
        RenderUtils.drawRoundedRect(context, x, y, 110, h, 10, 0xFA121214);
        context.drawText(textRenderer, "MONOLITH", x + 22, y + 18, 0xFFFFFF, true);
        RenderUtils.drawRect(context, x + 15, y + 35, 80, 1, 0x22FFFFFF);

        int catY = y + 50; int targetTabY = catY;
        for (String cat : categories) {
            boolean isCurrent = currentCategory.equals(cat);
            if (isCurrent) targetTabY = catY;
            context.drawText(textRenderer, cat, x + 25, catY + 8, isCurrent ? 0xFFFFFF : (mouseX >= x+10 && mouseX <= x+100 && mouseY >= catY && mouseY <= catY+24 ? 0xDDDDDD : 0x888888), false);
            catY += 30;
        }
        tabSlideY = RenderUtils.lerp(tabSlideY, targetTabY, 0.2f * delta);
        RenderUtils.drawRoundedRect(context, x + 10, (int)tabSlideY, 3, 24, 1, 0xFF8E2DE2);

        int modX = x + 125; int modY = y + 20;
        for (Module mod : ModuleManager.getModulesByCategory(currentCategory)) {
            mod.hoverAnim = RenderUtils.lerp(mod.hoverAnim, mouseX >= modX && mouseX <= x + w - 15 && mouseY >= modY && mouseY <= modY + 30 ? 1f : 0f, 0.15f * delta);
            RenderUtils.drawRoundedRect(context, modX, modY, w - 140, 30, 6, 0xFF151518);
            if (mod.hoverAnim > 0.05f) RenderUtils.drawRoundedRect(context, modX, modY, w - 140, 30, 6, RenderUtils.lerpColor(0x00FFFFFF, 0x15FFFFFF, mod.hoverAnim));
            
            context.drawText(textRenderer, mod.name, modX + 12, modY + 11, mod.enabled ? 0xFF8E2DE2 : 0xCCCCCC, false);
            
            mod.toggleAnim = RenderUtils.lerp(mod.toggleAnim, mod.enabled ? 1f : 0f, 0.2f * delta);
            int toggleX = modX + (w - 140) - 30; int toggleY = modY + 8;
            RenderUtils.drawRoundedRect(context, toggleX, toggleY, 20, 12, 6, RenderUtils.lerpColor(0xFF333333, 0xFF8E2DE2, mod.toggleAnim));
            RenderUtils.drawRoundedRect(context, toggleX + 1 + (int)(10 * mod.toggleAnim), toggleY + 1, 10, 10, 5, 0xFFFFFFFF);
            if (!mod.modes.isEmpty()) context.drawText(textRenderer, mod.currentMode, modX + 100, modY + 11, 0x55FFFFFF, false);
            modY += 36;
        }
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int w = 400; int h = 260; int x = (this.width - w) / 2; int y = (this.height - h) / 2;
        int catY = y + 50;
        for (String cat : categories) {
            if (mouseX >= x+10 && mouseX <= x+100 && mouseY >= catY && mouseY <= catY+24 && button == 0) { currentCategory = cat; return true; }
            catY += 30;
        }
        int modX = x + 125; int modY = y + 20;
        for (Module mod : ModuleManager.getModulesByCategory(currentCategory)) {
            if (mouseX >= modX && mouseX <= x+w-15 && mouseY >= modY && mouseY <= modY+30) {
                if (button == 0) { mod.toggle(); return true; } else if (button == 1 && !mod.modes.isEmpty()) { mod.cycleMode(); return true; }
            }
            modY += 36;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean shouldPause() { return false; }
}
