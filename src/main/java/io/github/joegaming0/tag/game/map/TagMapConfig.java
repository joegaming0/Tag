package io.github.joegaming0.tag.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public class TagMapConfig {
    public static final Codec<TagMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("id", null).forGetter(config -> config.id)
    ).apply(instance, TagMapConfig::new));

    public final Identifier id;

    public TagMapConfig(Identifier id) {
        this.id = id;
    }
}
