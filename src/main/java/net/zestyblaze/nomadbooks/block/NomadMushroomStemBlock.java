package net.zestyblaze.nomadbooks.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class NomadMushroomStemBlock extends NomadMushroomBlock {
    public NomadMushroomStemBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.getStateManager().getDefaultState().with(NORTH, true).with(EAST, true).with(SOUTH, true).with(WEST, true).with(UP, true).with(DOWN, true));
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView level, BlockPos pos) {
        return true;
    }
}
