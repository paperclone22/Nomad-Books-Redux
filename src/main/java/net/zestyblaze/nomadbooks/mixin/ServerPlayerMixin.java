package net.zestyblaze.nomadbooks.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends PlayerEntity {

    @Override
    @Shadow public abstract void playSound(@NotNull SoundEvent soundEvent, @NotNull SoundCategory soundSource, float f, float g);

    @Override
    @Shadow public abstract void sendMessage(@NotNull Text chatComponent, boolean actionBar);

    protected ServerPlayerMixin(World level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    /**
     * Implements the camp widening mechanic in which a number of biomes are required to visit
     * @param info CallbackInfo: "the juice that MixIns crave!"
     */
    @Inject(method = "playerTick", at = @At(value = "FIELD", target = "Lnet/minecraft/advancement/criterion/Criteria;LOCATION:Lnet/minecraft/advancement/criterion/TickCriterion;"))
    private void enterBiome(CallbackInfo info) {
        this.getInventory().main.stream().filter(itemStack -> itemStack.getItem() instanceof NomadBookItem)
            .forEachOrdered(this::inkHandler);
    }

    private void inkHandler(ItemStack itemStack) {
        NbtCompound tags = itemStack.getOrCreateSubNbt(Constants.MODID);
        // if inventory has an inked nomad book
        if (tags.getBoolean(Constants.INKED)) {
            NbtList visitedBiomes = tags.getList(Constants.VISITED_BIOMES, NbtElement.STRING_TYPE);
            String currentBiome = this.getWorld().getBiome(this.getBlockPos()).toString();
            if (currentBiome != null && !visitedBiomes.contains(NbtString.of(currentBiome))) {
                int inkProgress = tags.getInt(Constants.INK_PROGRESS);
                int inkGoal = tags.getInt(Constants.INK_GOAL);
                if (!visitedBiomes.isEmpty()) {
                    // if not first biome (just crafted), increment progress
                    tags.putInt(Constants.INK_PROGRESS, inkProgress + 1);
                }
                // remove the bottom of the pile of the excluded biomes
                if (visitedBiomes.size() > 9) {
                    visitedBiomes.remove(0);
                }
                  if (inkProgress >= inkGoal-1) {
                    tags.putBoolean(Constants.INKED, false);
                    tags.remove(Constants.INK_PROGRESS);
                    tags.remove(Constants.INK_GOAL);
                    tags.remove(Constants.VISITED_BIOMES);
                    tags.putInt(Constants.WIDTH, tags.getInt(Constants.WIDTH) + 2);
                    // if camp is deployed, move the camp pos
                    BlockPos pos = NbtHelper.toBlockPos(tags.getCompound(Constants.CAMP_POS)).add(-1, 0, -1);
                    tags.put(Constants.CAMP_POS, NbtHelper.fromBlockPos(pos));
                    // show a chat message to the player
                    this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.1f, 0.75f);
                    this.sendMessage(Text.translatable("info.nomadbooks.itinerant_ink_done", tags.getInt(Constants.WIDTH)).setStyle(Style.EMPTY.withColor(Formatting.BLUE)), false);
                } else {
                    visitedBiomes.add(NbtString.of(currentBiome));
                    tags.put(Constants.VISITED_BIOMES, visitedBiomes);
                }
            }
        }
    }

}
