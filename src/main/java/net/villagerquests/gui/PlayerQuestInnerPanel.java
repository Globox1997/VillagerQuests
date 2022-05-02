package net.villagerquests.gui;

import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;

public class PlayerQuestInnerPanel extends WPlainPanel {

    @Override
    public void add(WWidget w, int x, int y, int width, int height) {
        children.add(0, w);
        w.setParent(this);
        w.setLocation(insets.left() + x, insets.top() + y);
        if (w.canResize()) {
            w.setSize(width, height);
        }

        expandToFit(w, insets);
    }
}
