package io.github.joegaming0.tag.game;

import io.github.joegaming0.tag.game.player.TagPlayer;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;

public class TagScoreboard {
    private final SidebarWidget sidebar;

    public TagScoreboard(GlobalWidgets widgets) {
        this.sidebar = widgets.addSidebar(
                Text.translatable("game.tag.tag").formatted(Formatting.GOLD, Formatting.BOLD)
        );
    }

    public void render(TagPlayer tagger, ObjectCollection<TagPlayer> leaderboard) {
        this.sidebar.set(content ->
                leaderboard.stream().sorted().limit(15).forEach(entry -> {
                    Style style;
                    if (entry == tagger) {
                        style = Style.EMPTY.withColor(Formatting.RED).withBold(true);
                    } else {
                        style = Style.EMPTY.withColor(TextColor.fromRgb(0xdddddd));
                    }

                    content.add(Text.literal(entry.getPlayer().getName().getString())
                            .setStyle(style)
                            .append(Text.literal(String.format(": %ds", entry.getTicksAsTagger() / 20))
                            .setStyle(Style.EMPTY.withColor(Formatting.WHITE))));

                }));
    }
}