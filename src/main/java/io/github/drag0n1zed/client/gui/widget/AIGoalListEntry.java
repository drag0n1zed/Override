package io.github.drag0n1zed.client.gui.widget;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.background.Background;
import dev.lambdaurora.spruceui.background.SimpleColorBackground;
import dev.lambdaurora.spruceui.border.Border;
import dev.lambdaurora.spruceui.border.SimpleBorder;
import dev.lambdaurora.spruceui.navigation.NavigationDirection;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.SpruceToggleSwitch;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceEntryListWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class AIGoalListEntry extends SpruceEntryListWidget.Entry {

    private static final Background BACKGROUND = new SimpleColorBackground(new Color(40, 40, 40, 200).getRGB());
    private static final Border BORDER = new SimpleBorder(1, new Color(120, 120, 120, 255).getRGB());

    private final List<SpruceWidget> children = new ArrayList<>();
    @Nullable
    private SpruceWidget focused;

    private final int priority;
    private final String goalName;
    private final boolean enabled;

    public AIGoalListEntry(int priority, String goalName, boolean enabled) {
        super();
        this.priority = priority;
        this.goalName = goalName;
        this.enabled = enabled;
    }

    private void lazyInit() {
        if (this.children.isEmpty()) {
            int width = this.getWidth();

            var priorityLabel = new SpruceLabelWidget(Position.of(this, 5, 5),
                    Component.literal("[" + this.priority + "]").withStyle(ChatFormatting.YELLOW), width);
            this.children.add(priorityLabel);

            var nameLabel = new SpruceLabelWidget(Position.of(this, 30, 5),
                    Component.literal(this.goalName).withStyle(ChatFormatting.WHITE), width);
            this.children.add(nameLabel);

            var toggle = new SpruceToggleSwitch(Position.of(this, width - 85, 5), 75, 20,
                    Component.literal("Enabled"), (btn, val) -> {}, this.enabled, true);
            this.children.add(toggle);
        }
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        lazyInit();
        BACKGROUND.render(guiGraphics, this, 0, mouseX, mouseY, delta);
        for (SpruceWidget child : this.children) {
            child.render(guiGraphics, mouseX, mouseY, delta);
        }
        BORDER.render(guiGraphics, this, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.children.stream().anyMatch(child -> child.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.children.stream().anyMatch(child -> child.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.children.stream().anyMatch(child -> child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
    }

    @Override
    public boolean onNavigation(NavigationDirection direction, boolean tab) {
        if (tab) return false;
        int i = this.children.indexOf(this.focused);
        if (i == -1) i = direction.isLookingForward() ? -1 : this.children.size();

        int next = i + (direction.isLookingForward() ? 1 : -1);
        if (next >= 0 && next < this.children.size()) {
            this.setFocused(this.children.get(next));
            return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.setFocused(null);
        }
    }

    private void setFocused(@Nullable SpruceWidget focused) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }
        if (focused != null) {
            focused.setFocused(true);
        }
        this.focused = focused;
    }
}