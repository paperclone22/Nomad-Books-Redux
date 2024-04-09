package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Helper;

import java.util.List;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;

public class DyeNomadBookRecipe extends CustomRecipe {
	// see https://github.com/Lythom/capsule/blob/a1881ed43d9445ed48c31caa7710e2979359ed19/src/main/java/capsule/recipes/DyeCapsuleRecipe.java#L98
	public DyeNomadBookRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
		List<DyeItem> dyes = container.getItems().stream()
				.map(ItemStack::getItem)
				.filter(DyeItem.class::isInstance).map(DyeItem.class::cast).toList();
		return book != null && !dyes.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
		ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
		List<DyeItem> dyes = container.getItems().stream()
				.map(ItemStack::getItem)
				.filter(DyeItem.class::isInstance).map(DyeItem.class::cast).toList();
		ItemStack ret = book != null ? book.copy() : ItemStack.EMPTY;
		return !ret.isEmpty() && !dyes.isEmpty() ? DyeableLeatherItem.dyeArmor(ret, dyes) : ItemStack.EMPTY;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NomadBooks.DYE_NOMAD_BOOK;
	}
}
