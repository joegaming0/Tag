package io.github.joegaming0.tag.game;

import net.minecraft.entity.boss.BossBar;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import net.minecraft.text.Text;

public final class TagTimerBar {
    private static final Text WAITING_TITLE = Text.translatable("text.tag.bar.waiting");

    private final BossBarWidget widget;

    public TagTimerBar(GlobalWidgets widgets) {
        this.widget = widgets.addBossBar(WAITING_TITLE, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
    }

    public void update(long ticksUntilEnd, long totalTicksUntilEnd) {
        if (ticksUntilEnd % 20 == 0) {
            this.widget.setTitle(this.getTimeRemainingText(ticksUntilEnd));
            this.widget.setProgress((float) ticksUntilEnd / totalTicksUntilEnd);
        }
    }

    private Text getTimeRemainingText(long ticksUntilEnd) {
        long secondsUntilEnd = ticksUntilEnd / 20;

        long minutes = secondsUntilEnd / 60;
        long seconds = secondsUntilEnd % 60;

        return Text.translatable("text.tag.bar.ingame", String.format("%02d:%02d", minutes, seconds));
    }
}
