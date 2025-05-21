package io.github.joegaming0.tag.game.phase;

import io.github.joegaming0.tag.game.TagConfig;
import io.github.joegaming0.tag.game.player.TagSpawnLogic;
import io.github.joegaming0.tag.game.map.TagMap;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.event.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import io.github.joegaming0.tag.game.map.TagMapGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class TagWaiting {
    private final GameSpace gameSpace;
    private final TagMap map;
    private final ServerWorld world;
    private final TagConfig config;
    private final TagSpawnLogic spawnLogic;
    private TagWaiting(GameSpace gameSpace, ServerWorld world, TagMap map, TagConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.world = world;
        this.config = config;
        this.spawnLogic = new TagSpawnLogic(gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<TagConfig> context) {
        TagConfig config = context.config();
        TagMapGenerator generator = new TagMapGenerator(config.mapConfig);
        TagMap map = generator.build(context.server());

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()));

        return context.openWithWorld(worldConfig, (activity, world) -> {
            TagWaiting waiting = new TagWaiting(activity.getGameSpace(), world, map, context.config());

            GameWaitingLobby.addTo(activity, config.playerConfig);

            activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            activity.listen(GamePlayerEvents.ACCEPT, waiting::offerPlayer);
            activity.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
        });
    }

    private GameResult requestStart() {
        TagActive.open(this.gameSpace, this.map, this.world, this.config);
        return GameResult.ok();
    }

    private JoinAcceptorResult offerPlayer(JoinAcceptor acceptor) {
        return acceptor.teleport(this.world, this.map.playerSpawn.center()).thenRun((players) ->
                players.forEach((player) -> player.changeGameMode(GameMode.ADVENTURE)));
    }

    private EventResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return EventResult.DENY;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player, this.world);
    }
}
