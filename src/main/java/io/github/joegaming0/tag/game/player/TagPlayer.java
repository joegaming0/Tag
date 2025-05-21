package io.github.joegaming0.tag.game.player;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class TagPlayer implements Comparable<TagPlayer> {
    private final ServerPlayerEntity player;
    private int ticksAsTagger = 0;

    public TagPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public void incrementTicksAsTagger() {
        this.ticksAsTagger++;
    }

    public int getTicksAsTagger() {
        return this.ticksAsTagger;
    }

    @Override
    public int compareTo(@NotNull TagPlayer tagPlayer) {
        return Integer.compare(this.getTicksAsTagger(), tagPlayer.getTicksAsTagger());
    }
}
