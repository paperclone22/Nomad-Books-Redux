package net.zestyblaze.nomadbooks.recipe;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

import java.util.List;

public class NomadBookCraftRecipe extends ShapedRecipe {
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
        result.getOrCreateTagElement(Constants.MODID).putInt(Constants.HEIGHT, 1);
        result.getOrCreateTagElement(Constants.MODID).putInt(Constants.WIDTH, 3);
        result.getOrCreateTagElement(Constants.MODID).putString(Constants.STRUCTURE, NomadBookItem.DEFAULT_STRUCTURE_PATH);

        return result;
    }

    public NomadBookCraftRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, "", category, 3, 2, NonNullList.of(Ingredient.EMPTY,
            Ingredient.of(Items.AIR), Ingredient.of(Items.CAMPFIRE), Ingredient.of(Items.AIR),
            Ingredient.of(ModItems.GRASS_PAGE), Ingredient.of(ModItems.GRASS_PAGE), Ingredient.of(ModItems.GRASS_PAGE)), getCraftResult());
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if(!super.matches(container, level)) {
            return false;
        }
        return isNomadRecipe(container);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        if (isNomadRecipe(container)) {
            return getCraftResult();
        } else {
            return ItemStack.EMPTY;
        }
    }

    private boolean isNomadRecipe(CraftingContainer container) {
        List<Item> list = Lists.newArrayList();
        for(int i = 0; i < container.getContainerSize(); ++i) {
            list.add(container.getItem(i).getItem());
        }
        return list.equals(NOMAD_BOOK_RECIPE_1) || list.equals(NOMAD_BOOK_RECIPE_2);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
    }
}
