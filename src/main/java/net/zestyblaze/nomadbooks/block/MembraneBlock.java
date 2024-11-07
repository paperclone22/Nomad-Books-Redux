package net.zestyblaze.nomadbooks.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class MembraneBlock extends StainedGlassBlock { // NOSONAR
    public MembraneBlock(Settings properties) {
        super(DyeColor.PURPLE, properties);
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated(since = "2024-03-18")
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) { // NOSONAR
        if (entity instanceof ProjectileEntity) {
            entity.setVelocity(entity.getVelocity().getX()/2, entity.getVelocity().getY()/2, entity.getVelocity().getZ()/2);
        }
        if (world.getTime() % 15 == 0 && entity.getType() != EntityType.ITEM) {
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_HONEY_BLOCK_SLIDE, SoundCategory.BLOCKS, 1f, 1f);
        }
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView level, BlockPos pos) {
        return true;
    }

    @Override
    public void onBroken(WorldAccess level, BlockPos pos, BlockState state) {
        super.onBroken(level, pos, state);
        level.setBlockState(pos, Blocks.WATER.getDefaultState(), 3);
    }
}
