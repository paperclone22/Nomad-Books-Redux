package net.zestyblaze.nomadbooks.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.MushroomBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class NomadMushroomBlock extends MushroomBlock {
    public NomadMushroomBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.getStateManager().getDefaultState().with(NORTH, true).with(EAST, true).with(SOUTH, true).with(WEST, true).with(UP, true).with(DOWN, true));
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView level, BlockPos pos) {
        return true;
    }
}
