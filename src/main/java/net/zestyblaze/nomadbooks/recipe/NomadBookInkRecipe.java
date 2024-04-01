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
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

import java.util.List;

public class NomadBookInkRecipe extends ShapelessRecipe {
    public NomadBookInkRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, "", category, new ItemStack(ModItems.NOMAD_BOOK), NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.NOMAD_BOOK.getDefaultInstance(), ModItems.NETHER_NOMAD_BOOK.getDefaultInstance()), Ingredient.of(Items.GHAST_TEAR), Ingredient.of(Items.CHARCOAL), Ingredient.of(Items.BLUE_DYE)));
    }

    // TODO simplify matches and assemble
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        List<Item> ingredients = Lists.newArrayList();
        ItemStack book = null;

        for(int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            Item item = itemStack.getItem();
            if (item instanceof NomadBookItem) {
                book = itemStack;
            } else if (item.equals(Items.GHAST_TEAR) || item.equals(Items.CHARCOAL) || item.equals(Items.BLUE_DYE)) {
                ingredients.add(item);
            }
        }

        return book != null && ingredients.size() == 3 && ingredients.contains(Items.GHAST_TEAR) && ingredients.contains(Items.CHARCOAL) && ingredients.contains(Items.BLUE_DYE);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        List<Item> ingredients = Lists.newArrayList();
        ItemStack book = null;

        for(int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            Item item = itemStack.getItem();
            if (item instanceof NomadBookItem) {
                book = itemStack;
            } else if (item.equals(Items.GHAST_TEAR) || item.equals(Items.CHARCOAL) || item.equals(Items.BLUE_DYE)) {
                ingredients.add(item);
            }
        }

        if (book != null && ingredients.size() == 3 && ingredients.contains(Items.GHAST_TEAR) && ingredients.contains(Items.CHARCOAL) && ingredients.contains(Items.BLUE_DYE)) {
            ItemStack ret = book.copy();
            int width = ret.getOrCreateTagElement(Constants.MODID).getInt(Constants.WIDTH);
            ret.getOrCreateTagElement(Constants.MODID).putBoolean(Constants.INKED, true);
            ret.getOrCreateTagElement(Constants.MODID).putInt(Constants.INK_GOAL, ((width+2)*(width+2) - width*width)/3); // Note: this determines the INK_GOAL
            ret.getOrCreateTagElement(Constants.MODID).putInt(Constants.INK_PROGRESS, 0);

            return ret;
        }

        return ItemStack.EMPTY;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.UPGRADE_NOMAD_BOOK;
    }
}
