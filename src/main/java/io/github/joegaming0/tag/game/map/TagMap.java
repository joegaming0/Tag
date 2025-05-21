package io.github.joegaming0.tag.game.map;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;

public class TagMap {
    private final MapTemplate template;
    private final TagMapConfig config;
    public BlockBounds playerSpawn;

    public TagMap(MapTemplate template, TagMapConfig config, @Nullable BlockBounds playerSpawn) {
        this.template = template;
        this.config = config;
        this.playerSpawn = playerSpawn;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    public MapTemplate getTemplate() {
        return template;
    }
}
