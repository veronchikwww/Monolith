package net.monolith.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.monolith.utils.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class MonolithMainMenu extends Screen {
    private static final Identifier LOGO_TEXTURE = Identifier.of("monolith", "textures/ui/logo.png");
    private final List<MenuButton> buttons = new ArrayList<>();
    private final int ACCENT = 0xFF00E5FF;
    private float openAnim = 0f;

    public MonolithMainMenu() {
        super(Text.literal("Monolith Main Menu"));
    }

    @Override
    protected void init() {
        super.init();
        buttons.clear();
        
        int btnWidth = 200;
        int btnHeight = 35;
        
        int startX = this.width - btnWidth - 25;
        int startY = (this.height - (4 * (btnHeight + 15))) / 2;

        // ИСПРАВЛЕНИЕ: Перевели на английский, чтобы шрифт Intel Mono работал без квадратиков
        buttons.add(new MenuButton("Singleplayer", startX, startY, btnWidth, btnHeight, () -> {
            this.client.setScreen(new SelectWorldScreen(this));
        }));
        buttons.add(new MenuButton("Multiplayer", startX, startY + 50, btnWidth, btnHeight, () -> {
            this.client.setScreen(new MultiplayerScreen(this));
        }));
        buttons.add(new MenuButton("Settings", startX, startY + 100, btnWidth, btnHeight, () -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        }));
        buttons.add(new MenuButton("Quit", startX, startY + 150, btnWidth, btnHeight, () -> {
            this.client.scheduleStop();
        }));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xFF050508, 0xFF000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        openAnim = RenderUtils.lerp(openAnim, 1f, 0.1f * delta);

        // --- Логотип ---
        int logoSize = (int)(320 * openAnim);
        int logoX = (this.width / 2) - 200 - (logoSize / 2);
        int logoY = (this.height / 2) - (logoSize / 2);
        
        context.getMatrices().push();
        float breathe = (float)Math.sin(System.currentTimeMillis() / 1000.0) * 8;
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, LOGO_TEXTURE, logoX, (int)(logoY + breathe), 0.0f, 0.0f, logoSize, logoSize, logoSize, logoSize);
        
        // --- Боковая панель ---
        int panelWidth = 260;
        int panelX = this.width - panelWidth;
        RenderUtils.drawRect(context, panelX, 0, panelWidth, this.height, 0x880A0A0C);

        // --- Кнопки ---
        for (MenuButton btn : buttons) {
            btn.render(context, mouseX, mouseY, delta);
        }
        
        RenderUtils.drawText(context, textRenderer, "Monolith Client v1.0", 10, this.height - 15, 0x33FFFFFF, false);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (MenuButton btn : buttons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private class MenuButton {
        String textString;
        int x, y, width, height;
        Runnable action;
        float hoverAnim = 0f;

        public MenuButton(String text, int x, int y, int width, int height, Runnable action) {
            this.textString = text; this.x = x; this.y = y; this.width = width; this.height = height; this.action = action;
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            hoverAnim = RenderUtils.lerp(hoverAnim, hovered ? 1f : 0f, 0.2f * delta);

            int bg = RenderUtils.lerpColor(0xFF15151A, 0xFF222228, hoverAnim);
            RenderUtils.drawRoundedRect(context, x, y, width, height, 6, bg);
            
            if (hoverAnim > 0.05f) {
                RenderUtils.drawRoundedRect(context, x, y + 6, 3, height - 12, 1, ACCENT);
            }

            int textColor = RenderUtils.lerpColor(0xFFAAAAAA, 0xFFFFFFFF, hoverAnim);
            int textOffset = (int)(8 * hoverAnim);
            
            // ИСПРАВЛЕНИЕ: Вызываем отрисовку строго через наш RenderUtils, чтобы наложился шрифт
            RenderUtils.drawText(context, textRenderer, textString, x + 20 + textOffset, y + (height - 8) / 2, textColor, false);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height && button == 0) {
                action.run();
                return true;
            }
            return false;
        }
    }
}
