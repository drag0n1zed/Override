package io.github.drag0n1zed.client.gui;

import io.github.drag0n1zed.screen.SurgicalStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;


public class SurgicalStationScreen extends AbstractContainerScreen<SurgicalStationMenu> {

    private final SurgicalStationMenu menu;

    public SurgicalStationScreen(SurgicalStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.menu = menu;

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // In the future, buttons and other widgets for the new GUI will be initialized here.
    }

    /**
     * Renders the background layer of the screen. For now, it's just a dark rectangle.
     */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // Draw a simple dark background as a placeholder. No texture asset is needed.
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF373737);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // Renders tooltips if any widgets have them.
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * Renders the text labels on top of the background.
     */
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFFFF, true);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFFFF, true);
    }
}