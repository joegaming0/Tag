package io.github.joegaming0.tag;


import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.api.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.joegaming0.tag.game.TagConfig;
import io.github.joegaming0.tag.game.phase.TagWaiting;

public class Tag implements ModInitializer {
    public static final String ID = "tag";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<TagConfig> TYPE = GameType.register(
            Identifier.of(ID, "tag"),
            TagConfig.CODEC,
            TagWaiting::open
    );

    @Override
    public void onInitialize() {
    }
}
