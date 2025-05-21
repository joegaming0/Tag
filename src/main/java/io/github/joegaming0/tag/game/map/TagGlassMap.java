package io.github.joegaming0.tag.game.map;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.TemplateRegion;

public class TagGlassMap {
    private static final Block MAIN_BLOCK = Blocks.LIGHT_GRAY_STAINED_GLASS;
    private static final Block SWITCHING_BLOCK = Blocks.RED_STAINED_GLASS;

    private static BlockBounds glassBounds;

    private int ticksUntilSwitch = 0;

    public TagGlassMap(MapTemplate template) {
        TemplateRegion glassRegion = template.getMetadata().getFirstRegion("Glass");
        if (glassRegion != null) {
            glassBounds = glassRegion.getBounds();
        }
    }

    public void tick(ServerWorld world) {
        if (glassBounds == null) return;

        if (ticksUntilSwitch == 30 || ticksUntilSwitch == 10) {
            replace(world, MAIN_BLOCK, SWITCHING_BLOCK);
        } else if (ticksUntilSwitch == 20) {
            replace(world, SWITCHING_BLOCK, MAIN_BLOCK);
        } else if (ticksUntilSwitch == 0) {
            ticksUntilSwitch = 30 * 20;

            for (BlockPos pos : BlockPos.iterate(glassBounds.asBox())) {
                float percentage = 0.94f;
                if (pos.getY() > 85) {
                    int y = pos.getY() - 85;
                    percentage += y / 10.0f * 0.06f;
                }
                if (world.getRandom().nextFloat() > percentage) {
                    world.setBlockState(pos, MAIN_BLOCK.getDefaultState());
                } else {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
            }
        }

        ticksUntilSwitch -= 1;
    }

    private void replace(ServerWorld world, Block initialBlock, Block newBlock) {
        for (BlockPos pos : BlockPos.iterate(glassBounds.asBox())) {
            if (world.getBlockState(pos).isOf(initialBlock)) {
                world.setBlockState(pos, newBlock.getDefaultState());
            }
        }
    }
}
