package net.zestyblaze.nomadbooks.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.zestyblaze.nomadbooks.client.NomadBooksClient;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayerEntity {
    protected LocalPlayerMixin(ClientWorld clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    /**
     * Render the bounds of the deployed structure using a wireframe of particles
     * @param info org.spongepowered.asm.mixin.injection.callback.CallbackInfo: "the juice that MixIns crave!"
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void displayBoundaries(CallbackInfo info) {
        if (getWorld().getTime() % 10 != 0) return;

        this.getInventory().main.stream()
            .filter(itemStack -> itemStack.getItem() instanceof NomadBookItem)
            .forEach(stack -> {
                NbtCompound tags = stack.getOrCreateSubNbt(Constants.MODID);
                if (tags.getBoolean(Constants.DISPLAY_BOUNDARIES)) {
                    int height = tags.getInt(Constants.HEIGHT);
                    int width = tags.getInt(Constants.WIDTH);
                    BlockPos pos = NbtHelper.toBlockPos(tags.getCompound(Constants.CAMP_POS));
                    // Aight the minX, minY, and minZ seem to detect 1 block too far in the negative(west/down/north) so I fudged it
                    // fix for Invalid bounding box when 1 height camp
                    BlockBox campVolume;
                    if (pos.getY() + 1 < pos.getY() + height - 1) {
                        campVolume = new BlockBox(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                            pos.getX() + width - 1, pos.getY() + height - 1, pos.getZ() + width - 1);
                    } else {
                        campVolume = new BlockBox(pos.getX() + 1, pos.getY(), pos.getZ() + 1,
                            pos.getX() + width - 1, pos.getY() + height - 1, pos.getZ() + width - 1);
                    }
                    BlockBox burritoCube = new BlockBox(pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + width, pos.getY() + height, pos.getZ() + width);

                    BlockPos.stream(burritoCube).filter(bp -> burritoCube.contains(bp) && !campVolume.contains(bp))
                        .forEach(bp -> getWorld().addParticle(NomadBooksClient.CAMP_LIMIT, true, bp.getX(), bp.getY() + 0.02, bp.getZ(), 0, 0, 0));
                }
            });
    }

}
