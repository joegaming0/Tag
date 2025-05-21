package io.github.joegaming0.tag.game.phase;

import io.github.joegaming0.tag.game.TagConfig;
import io.github.joegaming0.tag.game.TagScoreboard;
import io.github.joegaming0.tag.game.TagStageManager;
import io.github.joegaming0.tag.game.TagTimerBar;
import io.github.joegaming0.tag.game.map.TagGlassMap;
import io.github.joegaming0.tag.game.map.TagMap;
import io.github.joegaming0.tag.game.player.TagPlayer;
import io.github.joegaming0.tag.game.player.TagSpawnLogic;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.DroppedItemsResult;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.entity.EntityDropItemsEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.*;

public class TagActive {
    public final GameSpace gameSpace;
    private final TagConfig config;
    private final ServerWorld world;
    private final TagMap gameMap;
    private final TeamManager teams;
    private final Object2ObjectMap<UUID, TagPlayer> participants;
    private final TagSpawnLogic spawnLogic;
    private final TagStageManager stageManager;
    private final boolean ignoreWinState;
    private final TagGlassMap tagGlassMap;
    private final TagScoreboard scoreboard;
    private TagPlayer tagger;

    private static final GameTeamKey PLAYERS_KEY = new GameTeamKey("players");
    private static final GameTeam PLAYERS_TEAM = new GameTeam(PLAYERS_KEY, GameTeamConfig.builder()
            .setNameTagVisibility(AbstractTeam.VisibilityRule.NEVER)
            .build());

    private final ItemStack BOW;
    private final TagTimerBar timerBar;

    private TagActive(GameSpace gameSpace, TagMap map, GlobalWidgets widgets, TagConfig config, ServerWorld world, TeamManager teams) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.world = world;
        this.gameMap = map;
        this.spawnLogic = new TagSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>(this.gameSpace.getPlayers().size());
        this.teams = teams;
        this.BOW = ItemStackBuilder.of(Items.BOW)
                .addEnchantment(world.getServer(), Enchantments.INFINITY, 1)
                .setUnbreakable()
                .build();

        this.tagGlassMap = new TagGlassMap(map.getTemplate());

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            this.participants.put(player.getUuid(), new TagPlayer(player));
        }

        this.stageManager = new TagStageManager();
        this.ignoreWinState = this.participants.size() <= 1;

        this.scoreboard = new TagScoreboard(widgets);
        this.timerBar = new TagTimerBar(widgets);
    }

    public static void open(GameSpace gameSpace, TagMap map, ServerWorld world, TagConfig config) {
        gameSpace.setActivity(activity -> {
            TeamManager teams = TeamManager.addTo(activity);
            teams.addTeam(PLAYERS_TEAM);

            GlobalWidgets widgets = GlobalWidgets.addTo(activity);
            TagActive active = new TagActive(gameSpace, map, widgets, config, world, teams);

            activity.deny(GameRuleType.CRAFTING);
            activity.deny(GameRuleType.PORTALS);
            activity.allow(GameRuleType.PVP);
            activity.deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.FALL_DAMAGE);
            activity.allow(GameRuleType.INTERACTION);
            activity.deny(GameRuleType.BLOCK_DROPS);
            activity.deny(GameRuleType.THROW_ITEMS);
            activity.deny(GameRuleType.UNSTABLE_TNT);
            activity.deny(GameRuleType.CRAFTING);

            activity.listen(GameActivityEvents.ENABLE, active::onOpen);
            activity.listen(GameActivityEvents.DISABLE, active::onClose);

            activity.listen(GamePlayerEvents.ACCEPT, active::onOfferPlayer);
            activity.listen(GamePlayerEvents.ADD, active::addPlayer);
            activity.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            activity.listen(GameActivityEvents.TICK, active::tick);

            activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
            activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
            activity.listen(EntityDropItemsEvent.EVENT, active::onItemDrop);
        });

        
    }

    public JoinAcceptorResult onOfferPlayer(JoinAcceptor acceptor) {
        return acceptor.teleport(this.world, this.gameMap.playerSpawn.center()).thenRun((players) ->
                players.forEach((player) -> player.changeGameMode(GameMode.ADVENTURE)));
    }

    private DroppedItemsResult onItemDrop(LivingEntity livingEntity, List<ItemStack> itemStacks) {
        return DroppedItemsResult.deny();
    }

    private void onOpen() {
        ServerWorld world = this.world;

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            spawnParticipant(player);
            teams.addPlayerTo(player, PLAYERS_KEY);
            if (config.scale != 1.0f) {
                player.getAttributes().getCustomInstance(EntityAttributes.SCALE).setBaseValue(config.scale);
            }

            if (config.movementMultiplier != 1.0f) {
                player.getAttributes().getCustomInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(player.getAttributeBaseValue(EntityAttributes.MOVEMENT_SPEED)*config.movementMultiplier);
                player.getAttributes().getCustomInstance(EntityAttributes.JUMP_STRENGTH).setBaseValue(player.getAttributeBaseValue(EntityAttributes.JUMP_STRENGTH)*config.movementMultiplier);
                player.getAttributes().getCustomInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(player.getAttributeBaseValue(EntityAttributes.STEP_HEIGHT)*config.movementMultiplier);
                player.getAttributes().getCustomInstance(EntityAttributes.GRAVITY).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GRAVITY)*config.movementMultiplier);
            }

            if (config.nightVision) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 255, true, false));
            }
        }
        this.stageManager.onOpen(world.getTime(), this.config);
    }

    private void onClose() {
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnSpectator(player);
    }

    private void removePlayer(ServerPlayerEntity player) {
        participants.remove(player.getUuid());
        if (!this.participants.isEmpty() && this.tagger.getPlayer() == player) {
            this.setTagger(this.pickRandomPlayer());
        }
    }

    private TagPlayer getPlayerEntry(ServerPlayerEntity player) {
        for (TagPlayer entry : this.participants.values()) {
            if (player == entry.getPlayer()) {
                return entry;
            }
        }
        return null;
    }

    private TagPlayer pickRandomPlayer() {
        int item = this.world.getRandom().nextInt(this.participants.size());
        int i = 0;
        for (TagPlayer entry : this.participants.values()) {
            if (i == item) { return entry; }
            i++;
        }
        return null;
    }

    private void setTagger(TagPlayer entry) {
        this.tagger = entry;
        ServerPlayerEntity newTagger = entry.getPlayer();

        Text taggedText = Text.translatable("text.tag.tagged").formatted(Formatting.RED);
        newTagger.sendMessage(taggedText, false);
        newTagger.networkHandler.sendPacket(new TitleFadeS2CPacket(3, 15, 10));
        newTagger.networkHandler.sendPacket(new TitleS2CPacket(taggedText));

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            if (player != newTagger) {
                player.sendMessage(Text.translatable("text.tag.tagged.other", newTagger.getName().getString()).formatted(Formatting.RED), false);
                if (!player.isSpectator()) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, StatusEffectInstance.INFINITE, 0, true, false, false));
                    if (this.config.darkness) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, StatusEffectInstance.INFINITE, 0, true, false, false));
                    }
                }
            }
        }
        newTagger.removeStatusEffect(StatusEffects.GLOWING);
        newTagger.removeStatusEffect(StatusEffects.DARKNESS);
    }

    private EventResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            player.heal(amount);
            if (getPlayerEntry(attacker) == this.tagger) {
                setTagger(getPlayerEntry(player));
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40,1, true, false, true));
            }
        }
        return EventResult.ALLOW;
    }

    private EventResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnParticipant(player);
        return EventResult.DENY;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player, this.world);
        player.getInventory().clear();
        if (config.bow) {
            player.giveItemStack(BOW.copy());
            player.getInventory().insertStack(27, Items.ARROW.getDefaultStack());
        }
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player, this.world);
    }

    private void tick() {
        ServerWorld world = this.world;
        long time = world.getTime();

        tagGlassMap.tick(world);

        if (time == this.stageManager.startTime) {
            this.setTagger(this.pickRandomPlayer());
        }

        if (config.snowballs && (time % 20 == 0)) {
            for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
                if (player.getInventory().count(Items.SNOWBALL) < 16) {
                    player.giveItemStack(Items.SNOWBALL.getDefaultStack());
                }
            }
        }

        TagStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
                return;
        }

        this.tagger.incrementTicksAsTagger();
        this.scoreboard.render(this.tagger, this.participants.values());

        this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs * 20L);
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = Text.translatable("text.tag.winner", winningPlayer.getDisplayName()).formatted(Formatting.GOLD);
        } else {
            message = Text.translatable("text.tag.no_winner").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerPlayerEntity winningPlayer = this.participants.values().stream().min(Comparator.naturalOrder()).get().getPlayer();

        return WinResult.win(winningPlayer);
    }

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
