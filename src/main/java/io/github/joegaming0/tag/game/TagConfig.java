package io.github.joegaming0.tag.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.joegaming0.tag.game.map.TagMapConfig;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public class TagConfig {
    public static final MapCodec<TagConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            TagMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs),
            Codec.BOOL.optionalFieldOf("darkness", false).forGetter(config -> config.darkness),
            Codec.BOOL.optionalFieldOf("bow", false).forGetter(config -> config.bow),
            Codec.BOOL.optionalFieldOf("snowballs", false).forGetter(config -> config.snowballs),
            Codec.BOOL.optionalFieldOf("night_vision", false).forGetter(config -> config.nightVision),
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(config -> config.scale),
            Codec.FLOAT.optionalFieldOf("movementMultiplier", 1.0f).forGetter(config -> config.scale)
    ).apply(instance, TagConfig::new));

    public final WaitingLobbyConfig playerConfig;
    public final TagMapConfig mapConfig;
    public final int timeLimitSecs;
    public final boolean darkness;
    public final boolean bow;
    public final boolean snowballs;
    public final boolean nightVision;
    public final float scale;
    public final float movementMultiplier;

    public TagConfig(WaitingLobbyConfig players, TagMapConfig mapConfig, int timeLimitSecs, boolean darkness, boolean bow, boolean snowballs, boolean nightVision, float scale, float movementMultiplier) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimitSecs = timeLimitSecs;
        this.darkness = darkness;
        this.bow = bow;
        this.snowballs = snowballs;
        this.nightVision = nightVision;
        this.scale = scale;
        this.movementMultiplier = movementMultiplier;
    }
}
