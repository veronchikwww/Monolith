package net.monolith.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.utils.RenderUtils;
import java.util.List;

public class CustomGuiScreen extends Screen {
    private String currentCategory = "Combat";
    private float openAnim = 0f;
    private float slideAnim = 0f;
    private String lastCategory = "Combat";
    private final String[] categories = {"Combat", "Movement", "Visuals", "Player", "Misc"};

    public CustomGuiScreen() { super(Text.literal("Monolith Pillar")); }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xCC050508, 0xEE000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.15f * delta);
        if (!currentCategory.equals(lastCategory)) { slideAnim = 0f; lastCategory = currentCategory; }
        slideAnim = RenderUtils.lerp(slideAnim, 1f, 0.15f * delta);

        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(0.9f + (0.1f * openAnim), 0.9f + (0.1f * openAnim), 1f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        int pillarWidth = 100; int pillarHeight = 240;
        int pX = (this.width - pillarWidth) / 2 - 80; int pY = (this.height - pillarHeight) / 2;

        RenderUtils.drawRoundedRect(context, pX - 1, pY - 1, pillarWidth + 2, pillarHeight + 2, 8, 0x558E2DE2);
        RenderUtils.drawRoundedRect(context, pX, pY, pillarWidth, pillarHeight, 8, 0xFA0A0A0C);
        context.drawText(textRenderer, "MONOLITH", pX + (pillarWidth - textRenderer.getWidth("MONOLITH")) / 2, pY + 15, 0xFFFFFF, true);
        RenderUtils.drawRect(context, pX + 20, pY + 30, pillarWidth - 40, 1, 0x33FFFFFF);

        int catY = pY + 50;
        for (String cat : categories) {
            boolean isCurrent = currentCategory.equals(cat);
            boolean hovered = mouseX >= pX + 10 && mouseX <= pX + pillarWidth - 10 && mouseY >= catY && mouseY <= catY + 25;
            if (isCurrent) RenderUtils.drawRoundedRect(context, pX + 10, catY, pillarWidth - 20, 25, 4, 0xFF8E2DE2);
            else if (hovered) RenderUtils.drawRoundedRect(context, pX + 10, catY, pillarWidth - 20, 25, 4, 0x15FFFFFF);
            context.drawText(textRenderer, cat, pX + (pillarWidth - textRenderer.getWidth(cat)) / 2, catY + 8, isCurrent ? 0xFFFFFF : (hovered ? 0xDDDDDD : 0x777777), false);
            catY += 35;
        }

        List<Module> mods = ModuleManager.getModulesByCategory(currentCategory);
        int modPanelWidth = 180;
        int extraHeight = 0;
        for (Module m : mods) {
            m.expandAnim = RenderUtils.lerp(m.expandAnim, (m.expanded && !m.modes.isEmpty()) ? 1f : 0f, 0.2f * delta);
            extraHeight += (int)(m.modes.size() * 16 * m.expandAnim) + (m.expandAnim > 0.1f ? 5 : 0);
        }
        int modPanelHeight = Math.max(100, (mods.size() * 25) + 30 + extraHeight);
        int targetModX = pX + pillarWidth + 10;
        int currentModX = (int) (pX + (targetModX - pX) * slideAnim);
        int modY = pY + 15;

        context.enableScissor(targetModX, 0, this.width, this.height);
        RenderUtils.drawRoundedRect(context, currentModX, modY, modPanelWidth, modPanelHeight, 6, 0xE6101014);
        RenderUtils.drawRect(context, currentModX, modY + 10, 2, modPanelHeight - 20, 0xFF8E2DE2);

        int mY = modY + 15;
        for (Module mod : mods) {
            boolean hovered = mouseX >= currentModX + 10 && mouseX <= currentModX + modPanelWidth - 10 && mouseY >= mY && mouseY <= mY + 20;
            mod.hoverAnim = RenderUtils.lerp(mod.hoverAnim, hovered ? 1f : 0f, 0.2f * delta);
            
            context.drawText(textRenderer, mod.name, currentModX + 15, mY + 5, mod.enabled ? 0xFF8E2DE2 : RenderUtils.lerpColor(0xFFAAAAAA, 0xFFFFFFFF, mod.hoverAnim), false);
            RenderUtils.drawRect(context, currentModX + modPanelWidth - 25, mY + 10, 10, 2, mod.enabled ? 0xFF8E2DE2 : 0x33FFFFFF);
            if (!mod.modes.isEmpty()) context.drawText(textRenderer, mod.expanded ? "v" : ">", currentModX + modPanelWidth - 15, mY + 4, 0x555555, false);

            mY += 25;
            if (mod.expandAnim > 0.05f) {
                int expHeight = (int)(mod.modes.size() * 16 * mod.expandAnim);
                int subY = mY;
                for (String mode : mod.modes) {
                    context.drawText(textRenderer, mode, currentModX + 30, subY + 3, mod.currentMode.equals(mode) ? 0xFF8E2DE2 : 0x777777, false);
                    subY += 16;
                }
                mY += expHeight + 5;
            }
        }
        context.disableScissor();
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int pX = (this.width - 100) / 2 - 80; int pY = (this.height - 240) / 2;
        int catY = pY + 50;
        for (String cat : categories) {
            if (mouseX >= pX+10 && mouseX <= pX+90 && mouseY >= catY && mouseY <= catY+25 && button == 0) { currentCategory = cat; return true; }
            catY += 35;
        }

        int targetModX = pX + 110; int mY = pY + 30;
        for (Module mod : ModuleManager.getModulesByCategory(currentCategory)) {
            if (mouseX >= targetModX+10 && mouseX <= targetModX+170 && mouseY >= mY && mouseY <= mY+20) {
                if (button == 0) { mod.toggle(); return true; } else if (button == 1 && !mod.modes.isEmpty()) { mod.expanded = !mod.expanded; return true; }
            }
            mY += 25;
            if (mod.expanded && !mod.modes.isEmpty()) {
                for (String mode : mod.modes) {
                    if (mouseX >= targetModX+25 && mouseX <= targetModX+160 && mouseY >= mY && mouseY <= mY+14 && button == 0) { mod.currentMode = mode; return true; }
                    mY += 16;
                }
                mY += 5;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean shouldPause() { return false; }
}
