package net.zestyblaze.nomadbooks.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.zestyblaze.nomadbooks.client.NomadBooksClient;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    protected LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    /**
     * Render the bounds of the deployed structure using a wireframe of particles
     * @param info org.spongepowered.asm.mixin.injection.callback.CallbackInfo: "the juice that MixIns crave!"
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void displayBoundaries(CallbackInfo info) {
        if (level().getGameTime() % 10 != 0) return;

        this.getInventory().items.stream()
            .filter(itemStack -> itemStack.getItem() instanceof NomadBookItem)
            .forEach(stack -> {
                CompoundTag tags = stack.getOrCreateTagElement(Constants.MODID);
                if (tags.getBoolean(Constants.DISPLAY_BOUNDARIES)) {
                    int height = tags.getInt(Constants.HEIGHT);
                    int width = tags.getInt(Constants.WIDTH);
                    BlockPos pos = NbtUtils.readBlockPos(tags.getCompound(Constants.CAMP_POS));
                    // Aight the minX, minY, and minZ seem to detect 1 block too far in the negative(west/down/north) so I fudged it
                    // fix for Invalid bounding box when 1 height camp
                    BoundingBox campVolume;
                    if (pos.getY() + 1 < pos.getY() + height - 1) {
                        campVolume = new BoundingBox(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                            pos.getX() + width - 1, pos.getY() + height - 1, pos.getZ() + width - 1);
                    } else {
                        campVolume = new BoundingBox(pos.getX() + 1, pos.getY(), pos.getZ() + 1,
                            pos.getX() + width - 1, pos.getY() + height - 1, pos.getZ() + width - 1);
                    }
                    BoundingBox burritoCube = new BoundingBox(pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + width, pos.getY() + height, pos.getZ() + width);

                    BlockPos.betweenClosedStream(burritoCube).filter(bp -> burritoCube.isInside(bp) && !campVolume.isInside(bp))
                        .forEach(bp -> level().addParticle(NomadBooksClient.CAMP_LIMIT, true, bp.getX(), bp.getY() + 0.02, bp.getZ(), 0, 0, 0));
                }
            });
    }

}
