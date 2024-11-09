package net.zestyblaze.nomadbooks.recipe;

import com.google.common.collect.Lists;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

import java.util.List;

import static net.zestyblaze.nomadbooks.NomadBooks.CRAFT_NOMAD_BOOK;

public class NomadBookCraftRecipe extends SpecialCraftingRecipe {

    public static final List<Item> NOMAD_BOOK_RECIPE_1 = Lists.newArrayList(
            Items.AIR, Items.CAMPFIRE, Items.AIR,
            ModItems.GRASS_PAGE, ModItems.GRASS_PAGE, ModItems.GRASS_PAGE,
            Items.AIR, Items.AIR, Items.AIR
    );
    public static final List<Item> NOMAD_BOOK_RECIPE_2 = Lists.newArrayList(
            Items.AIR, Items.AIR, Items.AIR,
            Items.AIR, Items.CAMPFIRE, Items.AIR,
            ModItems.GRASS_PAGE, ModItems.GRASS_PAGE, ModItems.GRASS_PAGE
    );

    public static ItemStack getCraftResult() {
        ItemStack result = new ItemStack(ModItems.NOMAD_BOOK);
        result.getOrCreateSubNbt(Constants.MODID).putInt(Constants.HEIGHT, 1);
        result.getOrCreateSubNbt(Constants.MODID).putInt(Constants.WIDTH, 3);
        result.getOrCreateSubNbt(Constants.MODID).putString(Constants.STRUCTURE, NomadBookItem.DEFAULT_STRUCTURE_PATH);

        return result;
    }

    public NomadBookCraftRecipe(/*Identifier id, */CraftingRecipeCategory category) {
        super(/*id.toString(), */category/*, 3, 2, DefaultedList.copyOf(Ingredient.EMPTY,*/
            /*Ingredient.ofItems(Items.AIR), Ingredient.ofItems(Items.CAMPFIRE), Ingredient.ofItems(Items.AIR),
            Ingredient.ofItems(ModItems.GRASS_PAGE), Ingredient.ofItems(ModItems.GRASS_PAGE), Ingredient.ofItems(ModItems.GRASS_PAGE)), getCraftResult()*/);
    }

    @Override
    public boolean matches(RecipeInputInventory container, World level) {
        return isNomadRecipe(container);
    }

    @Override
    public ItemStack craft(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
        return getCraftResult();
    }

    private boolean isNomadRecipe(RecipeInputInventory container) {
        List<Item> list = Lists.newArrayList();
        for(int i = 0; i < container.size(); ++i) {
            list.add(container.getStack(i).getItem());
        }
        return list.equals(NOMAD_BOOK_RECIPE_1) || list.equals(NOMAD_BOOK_RECIPE_2);
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CRAFT_NOMAD_BOOK;
    }
}
