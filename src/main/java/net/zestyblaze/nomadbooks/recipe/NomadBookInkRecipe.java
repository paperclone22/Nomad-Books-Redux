package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

public class NomadBookInkRecipe extends SpecialCraftingRecipe {
    public NomadBookInkRecipe(/*Identifier resourceLocation, */CraftingRecipeCategory category) {
        super(/*resourceLocation.toString(), */category/*, new ItemStack(ModItems.NOMAD_BOOK),*/
            /*DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofStacks(ModItems.NOMAD_BOOK.getDefaultStack(), ModItems.NETHER_NOMAD_BOOK.getDefaultStack()), Ingredient.ofItems(Items.GHAST_TEAR), Ingredient.ofItems(Items.CHARCOAL), Ingredient.ofItems(Items.BLUE_DYE))*/);
    }

    @Override
    public boolean matches(RecipeInputInventory container, World level) {
        List<Item> ingredients = container.getInputStacks().stream()
            .map(ItemStack::getItem)
            .filter(item -> item.equals(Items.GHAST_TEAR) || item.equals(Items.CHARCOAL) || item.equals(Items.BLUE_DYE)).toList();

        ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);

        int emptySlots = container.getInputStacks().stream()
            .filter(ItemStack::isEmpty)
            .toList().size();
        int allSlotsMinusFilledSlots = container.size() - 1 - ingredients.size();
        boolean hasNoExtraItems = emptySlots == allSlotsMinusFilledSlots;

        return book != null && ingredients.size() == 3 &&
            ingredients.contains(Items.GHAST_TEAR) && ingredients.contains(Items.CHARCOAL) && ingredients.contains(Items.BLUE_DYE)
            && hasNoExtraItems;
    }

    @Override
    public ItemStack craft(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
        List<Item> ingredients = container.getInputStacks().stream()
            .map(ItemStack::getItem)
            .filter(item -> item.equals(Items.GHAST_TEAR) || item.equals(Items.CHARCOAL) || item.equals(Items.BLUE_DYE)).toList();

        ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);

        int emptySlots = container.getInputStacks().stream()
            .filter(ItemStack::isEmpty)
            .toList().size();
        int allSlotsMinusFilledSlots = container.size() - 1 - ingredients.size();
        boolean hasNoExtraItems = emptySlots == allSlotsMinusFilledSlots;

        if (book != null && ingredients.size() == 3 &&
            ingredients.contains(Items.GHAST_TEAR) && ingredients.contains(Items.CHARCOAL) && ingredients.contains(Items.BLUE_DYE)
            && hasNoExtraItems) {
            ItemStack ret = book.copy();
            int width = ret.getOrCreateSubNbt(Constants.MODID).getInt(Constants.WIDTH);
            NbtCompound tags = ret.getOrCreateSubNbt(Constants.MODID);
            tags.putBoolean(Constants.INKED, true);
            tags.putInt(Constants.INK_GOAL, ((width + 2) * (width + 2) - width * width) / 3); // Determines the INK_GOAL
            tags.putInt(Constants.INK_PROGRESS, 0);
            return ret;
        }

        return ItemStack.EMPTY;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.UPGRADE_NOMAD_BOOK;
    }
}
