package net.zestyblaze.nomadbooks.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Override
    @Shadow public abstract void playNotifySound(@NotNull SoundEvent soundEvent, @NotNull SoundSource soundSource, float f, float g);

    @Override
    @Shadow public abstract void displayClientMessage(@NotNull Component chatComponent, boolean actionBar);

    protected ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    /**
     * Implements the camp widening mechanic in which a number of biomes are required to visit
     * @param info CallbackInfo: "the juice that MixIns crave!"
     */
    @Inject(method = "doTick", at = @At(value = "FIELD", target = "Lnet/minecraft/advancements/CriteriaTriggers;LOCATION:Lnet/minecraft/advancements/critereon/PlayerTrigger;"))
    private void enterBiome(CallbackInfo info) {
        this.getInventory().items.stream().filter(itemStack -> itemStack.getItem() instanceof NomadBookItem)
            .forEachOrdered(this::inkHandler);
    }

    private void inkHandler(ItemStack itemStack) {
        CompoundTag tags = itemStack.getOrCreateTagElement(Constants.MODID);
        // if inventory has an inked nomad book
        if (tags.getBoolean(Constants.INKED)) {
            ListTag visitedBiomes = tags.getList(Constants.VISITED_BIOMES, Tag.TAG_STRING);
            String currentBiome = this.level().getBiome(this.blockPosition()).toString();
            if (currentBiome != null && !visitedBiomes.contains(StringTag.valueOf(currentBiome))) {
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
                    BlockPos pos = NbtUtils.readBlockPos(tags.getCompound(Constants.CAMP_POS)).offset(-1, 0, -1);
                    tags.put(Constants.CAMP_POS, NbtUtils.writeBlockPos(pos));
                    // show a chat message to the player
                    this.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.1f, 0.75f);
                    this.displayClientMessage(Component.translatable("info.nomadbooks.itinerant_ink_done", tags.getInt(Constants.WIDTH)).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)), false);
                } else {
                    visitedBiomes.add(StringTag.valueOf(currentBiome));
                    tags.put(Constants.VISITED_BIOMES, visitedBiomes);
                }
            }
        }
    }


}
