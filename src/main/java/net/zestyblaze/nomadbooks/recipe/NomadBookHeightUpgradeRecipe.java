package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

import java.util.List;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;

public class NomadBookHeightUpgradeRecipe extends SpecialCraftingRecipe {
    public NomadBookHeightUpgradeRecipe(/*Identifier id, */CraftingRecipeCategory category) {
        // I decided to switch to single page. Multiple pages still works, but it shows just 1 in the Recipe book (BUT IT SHOWS AT ALL)
        super(/*id, "", */category/*, new ItemStack(ModItems.NOMAD_BOOK), DefaultedList.copyOf(Ingredient.EMPTY,
            Ingredient.ofStacks(ModItems.NOMAD_BOOK.getDefaultStack(), ModItems.NETHER_NOMAD_BOOK.getDefaultStack()), Ingredient.ofItems(ModItems.GRASS_PAGE))*/);
    }

    @Override
    public boolean matches(RecipeInputInventory container, World level) {
        ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
        List<ItemStack> pageStacks = container.getHeldStacks().stream()
            .filter(stack -> stack.getItem().equals(ModItems.GRASS_PAGE)).toList();

        return book != null && !pageStacks.isEmpty() && hasNoExtraItems(container, pageStacks.size()) &&
            book.getOrCreateNbt().getFloat(Constants.DEPLOYED) == 0.0f; // Not deployed
    }

    @Override
    public ItemStack craft(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
        ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
        List<Item> pageStacks = container.getHeldStacks().stream()
            .map(ItemStack::getItem)
            .filter(item -> item.equals(ModItems.GRASS_PAGE)).toList();

        if (book != null && !pageStacks.isEmpty() && hasNoExtraItems(container, pageStacks.size()) &&
            book.getOrCreateNbt().getFloat(Constants.DEPLOYED) == 0.0f) { // Not deployed
            ItemStack ret = book.copy();
            int height = ret.getOrCreateSubNbt(Constants.MODID).getInt(Constants.HEIGHT);
            NbtCompound tags = ret.getOrCreateSubNbt(Constants.MODID);
            tags.putInt(Constants.HEIGHT, height + pageStacks.size());
            return ret;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.UPGRADE_HEIGHT_NOMAD_BOOK;
    }
}
