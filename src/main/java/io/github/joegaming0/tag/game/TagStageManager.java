package io.github.joegaming0.tag.game;

import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

public class TagStageManager {
    private long closeTime;
    public long finishTime;
    public long startTime;
    private boolean setSpectator = false;

    public TagStageManager() {
    }

    public void onOpen(long time, TagConfig config) {
        this.startTime = time - (time % 20) + (4 * 20) + 19;
        this.finishTime = this.startTime + (config.timeLimitSecs * 20L);
    }

    public IdleTickResult tick(long time, GameSpace space) {
        // Game has finished. Wait a few seconds before finally closing the game.
        if (this.closeTime > 0) {
            if (time >= this.closeTime) {
                return IdleTickResult.GAME_CLOSED;
            }
            return IdleTickResult.TICK_FINISHED;
        }

        // Game hasn't started yet. Display a countdown before it begins.
        if (this.startTime > time) {
            this.tickStartWaiting(time, space);
            return IdleTickResult.TICK_FINISHED;
        }

        // Game has just finished. Transition to the waiting-before-close state.
        if (time > this.finishTime || space.getPlayers().isEmpty()) {
            if (!this.setSpectator) {
                this.setSpectator = true;
                for (ServerPlayerEntity player : space.getPlayers()) {
                    player.changeGameMode(GameMode.SPECTATOR);
                }
            }

            this.closeTime = time + (5 * 20);

            return IdleTickResult.GAME_FINISHED;
        }

        return IdleTickResult.CONTINUE_TICK;
    }

    private void tickStartWaiting(long time, GameSpace space) {
        int secondsUntilStart = (int) Math.floor((this.startTime - time) / 20.0f) - 1;

        if ((this.startTime - time) % 20 == 0) {
            PlayerSet players = space.getPlayers();

            if (secondsUntilStart > 0) {
                players.showTitle(Text.literal(Integer.toString(secondsUntilStart)).formatted(Formatting.BOLD), 70);
                players.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
                players.showTitle(Text.translatable("text.tag.start").formatted(Formatting.BOLD), 70);
                players.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.PLAYERS, 1.0F, 2.0F);
            }
        }
    }

    public enum IdleTickResult {
        CONTINUE_TICK,
        TICK_FINISHED,
        GAME_FINISHED,
        GAME_CLOSED,
    }
}
