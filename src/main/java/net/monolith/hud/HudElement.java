package net.monolith.hud;

import net.minecraft.client.gui.DrawContext;

public abstract class HudElement {
    public int x, y, width, height;
    public boolean dragging;
    private int dragX, dragY;

    public HudElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext context, float tickDelta);

    public void renderEditMode(DrawContext context, int mouseX, int mouseY) {
        if (dragging) {
            this.x = mouseX - dragX;
            this.y = mouseY - dragY;
        }
        // Отрисовка рамки при редактировании (в чате)
        context.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x44FFFFFF);
        render(context, 0);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            dragging = true;
            dragX = (int) (mouseX - x);
            dragY = (int) (mouseY - y);
            return true;
        }
        return false;
    }

    public void mouseReleased(int button) {
        if (button == 0) dragging = false;
    }
}
