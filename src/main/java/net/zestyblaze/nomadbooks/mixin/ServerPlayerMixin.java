package net.zestyblaze.nomadbooks.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.NomadBooksComponent;
import net.zestyblaze.nomadbooks.util.NomadInkComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends PlayerEntity {

	@Override
	@Shadow
	public abstract void playSoundToPlayer(@NotNull SoundEvent soundEvent, @NotNull SoundCategory soundSource, float f, float g);

	@Override
	@Shadow
	public abstract void sendMessage(@NotNull Text chatComponent, boolean actionBar);

	protected ServerPlayerMixin(World level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	/**
	 * Implements the camp widening mechanic in which a number of biomes are required to visit
	 *
	 * @param info CallbackInfo: "the juice that MixIns crave!"
	 */
	@Inject(method = "playerTick", at = @At(value = "FIELD", target = "Lnet/minecraft/advancement/criterion/Criteria;LOCATION:Lnet/minecraft/advancement/criterion/TickCriterion;"))
	private void enterBiome(CallbackInfo info) {
		for (ItemStack itemStack : this.getInventory().main) {
			if (itemStack.getItem() instanceof NomadBookItem) {
				inkHandler(itemStack);
			}
		}
	}

	@Unique
	private void inkHandler(ItemStack itemStack) {
		NomadBooksComponent tags = itemStack.get(NomadBooks.NOMAD_BOOK_DATA);
		NomadInkComponent ink = itemStack.get(NomadBooks.NOMAD_INK_DATA);
		LodestoneTrackerComponent campTracker = itemStack.get(DataComponentTypes.LODESTONE_TRACKER);
		// if inventory has an inked nomad book
		if (campTracker == null || tags == null || ink == null || !ink.isInked()) {
			return;
		}
		if (ink.inkGoal() == 0) { // This is for the extend_book_width_smithing recipe. Set the goal if 0
			ink = new NomadInkComponent(true, ink.inkProgress(), ((tags.width() + 2) * (tags.width() + 2) - tags.width() * tags.width()) / 3, ink.visitedBiomes());
			itemStack.set(NomadBooks.NOMAD_INK_DATA, ink);
		}
		if (!ink.visitedBiomes().contains(this.getWorld().getBiome(this.getBlockPos()).getIdAsString())) {
			String currentBiome = this.getWorld().getBiome(this.getBlockPos()).getIdAsString();
			List<String> visitedBiomes = new java.util.ArrayList<>(ink.visitedBiomes());
			int inkProgress = ink.inkProgress();
			if (!visitedBiomes.isEmpty()) {
				// if not first biome (just crafted), increment progress
				inkProgress += 1;
				itemStack.set(NomadBooks.NOMAD_INK_DATA, new NomadInkComponent(true, inkProgress, ink.inkGoal(), ink.visitedBiomes()));
			}
			// remove the bottom of the pile of the excluded biomes
			if (visitedBiomes.size() > 8) {
				visitedBiomes.remove(0);
			}
			if (inkProgress >= ink.inkGoal()) {
				itemStack.set(NomadBooks.NOMAD_INK_DATA, new NomadInkComponent(false, 0, 0, List.of()));
				itemStack.set(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(tags.isDeployed(), tags.doDisplayBoundaries(), tags.height(), tags.width() + 2, tags.structure(), tags.upgrades())); // TODO I should add checks for if the player upgraded the camp size while deployed. needed for preserving terrain With Spacial Displacer. I need to prevent the camp from expanding if it's deployed... it is an uncommon edge-case so I'm going to procrastinate and leave this for later
				// if camp is deployed, move the camp pos
				BlockPos pos = new BlockPos(0, 0, 0);
				if (campTracker.target().isPresent()) {
					pos = campTracker.target().get().pos().add(-1, 0, -1); // NOSONAR
				}
				itemStack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(new GlobalPos(campTracker.target().get().dimension(), pos)), false)); // NOSONAR
				// show a chat message to the player
				this.playSoundToPlayer(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.1f, 0.75f);
				this.sendMessage(Text.translatable("info.nomadbooks.itinerant_ink_done", tags.width()+2).setStyle(Style.EMPTY.withColor(Formatting.BLUE)), false);
			} else {
					visitedBiomes.add(currentBiome);
					itemStack.set(NomadBooks.NOMAD_INK_DATA, new NomadInkComponent(true, inkProgress, ink.inkGoal(), visitedBiomes));
			}
		}
	}

}
