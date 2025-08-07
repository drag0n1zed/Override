package io.github.drag0n1zed.client.gui;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import io.github.drag0n1zed.client.gui.widget.AIGoalListEntry;
import io.github.drag0n1zed.client.gui.widget.AIGoalListWidget;
import io.github.drag0n1zed.screen.SurgicalStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SurgicalStationScreen extends SpruceScreen implements MenuAccess<SurgicalStationMenu> {

    private final SurgicalStationMenu menu;

    public SurgicalStationScreen(SurgicalStationMenu menu, Inventory playerInventory, Component title) {
        super(title);
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();

        int topOffset = 24;
        int bottomOffset = 32;

        var goalList = new AIGoalListWidget(
                Position.of(this, 0, topOffset),
                this.width,
                this.height - topOffset - bottomOffset,
                AIGoalListEntry.class
        );

        goalList.addEntry(new AIGoalListEntry(0, "FloatGoal", true));
        goalList.addEntry(new AIGoalListEntry(1, "PanicGoal", true));
        goalList.addEntry(new AIGoalListEntry(2, "TemptGoal", true));
        goalList.addEntry(new AIGoalListEntry(3, "FollowParentGoal", true));
        goalList.addEntry(new AIGoalListEntry(4, "WaterAvoidingRandomStrollGoal", false));
        goalList.addEntry(new AIGoalListEntry(5, "LookAtPlayerGoal", true));
        goalList.addEntry(new AIGoalListEntry(6, "RandomLookAroundGoal", true));

        this.addRenderableWidget(goalList);

        this.addRenderableWidget(new SpruceButtonWidget(
                Position.of(this, this.width / 2 - 75, this.height - 25),
                150,
                20,
                SpruceTexts.GUI_DONE,
                btn -> this.onClose()
        ));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public SurgicalStationMenu getMenu() {
        return this.menu;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}