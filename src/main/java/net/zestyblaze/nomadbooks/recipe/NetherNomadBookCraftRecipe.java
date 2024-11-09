package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;

public class NetherNomadBookCraftRecipe extends SpecialCraftingRecipe {
    public NetherNomadBookCraftRecipe(/*Identifier resourceLocation, */CraftingRecipeCategory category) {
        super(/*resourceLocation.toString(), */category/*, getCraftResult(),*/
            /*DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofStacks(ModItems.NOMAD_BOOK.getDefaultStack()), Ingredient.ofItems(Items.NETHERITE_INGOT))*/);
    }

    public static ItemStack getCraftResult() {
        ItemStack result = new ItemStack(ModItems.NETHER_NOMAD_BOOK);
        result.getOrCreateSubNbt(Constants.MODID).putInt(Constants.HEIGHT, 3);
        result.getOrCreateSubNbt(Constants.MODID).putInt(Constants.WIDTH, 7);

        return result;
    }

    @Override
    public boolean matches(RecipeInputInventory container, World level) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        ItemStack ingot = findItem(container, item -> item.isOf(Items.NETHERITE_INGOT));
        return book != null && hasNoExtraItems(container, 1) && ingot != null && book.getOrCreateNbt().getFloat(Constants.DEPLOYED) == 0.0f; // Not deployed
    }

    @Override
    public ItemStack craft(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        ItemStack ingot = findItem(container, item -> item.isOf(Items.NETHERITE_INGOT));

        if (book != null && hasNoExtraItems(container, 1) && ingot != null && book.getOrCreateNbt().getFloat(Constants.DEPLOYED) == 0.0f) { // Not deployed
            ItemStack result = getCraftResult(); // perhaps on craft, increase the height and width of the input book ? Might want to wait until after adding "ender page" upgrade
            applyBookDataToResult(book, result);
            return result;
        }

        return ItemStack.EMPTY;
    }

    private void applyBookDataToResult(ItemStack book, ItemStack result) {
        NbtCompound bookTag = book.getOrCreateSubNbt(Constants.MODID);
        NbtCompound resultTag = result.getOrCreateSubNbt(Constants.MODID);

        resultTag.putInt(Constants.HEIGHT, bookTag.getInt(Constants.HEIGHT));
        resultTag.putInt(Constants.WIDTH, bookTag.getInt(Constants.WIDTH));
        resultTag.put(Constants.UPGRADES, bookTag.getList(Constants.UPGRADES, NbtElement.STRING_TYPE));
        resultTag.putString("Structure", bookTag.getString(Constants.STRUCTURE).equals(NomadBookItem.DEFAULT_STRUCTURE_PATH) ?
            NomadBookItem.NETHER_DEFAULT_STRUCTURE_PATH : bookTag.getString(Constants.STRUCTURE));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.CRAFT_NETHER_NOMAD_BOOK;
    }
}
