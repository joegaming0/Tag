package io.github.joegaming0.tag.game.player;

import io.github.joegaming0.tag.Tag;
import io.github.joegaming0.tag.game.map.TagMap;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class TagSpawnLogic {
    private final GameSpace gameSpace;
    private final TagMap map;

    public TagSpawnLogic(GameSpace gameSpace, TagMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));
    }

    public void spawnPlayer(ServerPlayerEntity player, ServerWorld world) {
        BlockPos pos = this.map.playerSpawn.max();
        if (pos == null) {
            Tag.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        player.teleport(pos.getX(), pos.getY(), pos.getZ(), false);
    }
}
