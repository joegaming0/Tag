package io.github.joegaming0.tag.game.map;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.impl.Plasmid;

import java.io.IOException;

public class TagMapGenerator {

    private final TagMapConfig config;

    public TagMapGenerator(TagMapConfig config) {
        this.config = config;
    }

    public @NotNull TagMap build(MinecraftServer server) {
        MapTemplate template = null;
        BlockBounds spawnRegionBounds;

        try  {
            template = MapTemplateSerializer.loadFromResource(server, this.config.id);
        } catch (GameOpenException | IOException err) {
            Plasmid.LOGGER.error(err.getMessage());
        }
        TemplateRegion spawnRegion = template.getMetadata().getFirstRegion("Spawn");

        if (spawnRegion == null) {
            Plasmid.LOGGER.error("Spawn region for map {} is null", this.config.id);
        }

        spawnRegionBounds = spawnRegion.getBounds();

        return new TagMap(template, config, spawnRegionBounds);
    }
}
