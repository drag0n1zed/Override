package io.github.drag0n1zed.client.gui.widget;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.widget.container.SpruceEntryListWidget;

public class AIGoalListWidget extends SpruceEntryListWidget<AIGoalListEntry> {

    public AIGoalListWidget(Position position, int width, int height, Class<AIGoalListEntry> entryClass) {
        super(position, width, height, 0, entryClass);
    }

    /**
     * Overridden to change the access level from protected to public,
     * allowing our screen to add entries to this list.
     *
     * @param entry The AIGoalListEntry to add.
     * @return The index of the added entry.
     */
    @Override
    public int addEntry(AIGoalListEntry entry) {
        return super.addEntry(entry);
    }
}