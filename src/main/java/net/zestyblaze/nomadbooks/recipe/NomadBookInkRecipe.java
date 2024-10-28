package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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

import static net.zestyblaze.nomadbooks.util.Helper.findItem;

public class NomadBookInkRecipe extends ShapelessRecipe {
    public NomadBookInkRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, "", category, new ItemStack(ModItems.NOMAD_BOOK),
            NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.NOMAD_BOOK.getDefaultInstance(), ModItems.NETHER_NOMAD_BOOK.getDefaultInstance()), Ingredient.of(Items.GHAST_TEAR), Ingredient.of(Items.CHARCOAL), Ingredient.of(Items.BLUE_DYE)));
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        List<Item> ingredients = container.getItems().stream()
            .map(ItemStack::getItem)
            .filter(item -> item.equals(Items.GHAST_TEAR) || item.equals(Items.CHARCOAL) || item.equals(Items.BLUE_DYE)).toList();

        ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);

        int emptySlots = container.getItems().stream()
            .filter(ItemStack::isEmpty)
            .toList().size();
        int allSlotsMinusFilledSlots = container.getContainerSize() - 1 - ingredients.size();
        boolean hasNoExtraItems = emptySlots == allSlotsMinusFilledSlots;

        return book != null && ingredients.size() == 3 &&
            ingredients.contains(Items.GHAST_TEAR) && ingredients.contains(Items.CHARCOAL) && ingredients.contains(Items.BLUE_DYE)
            && hasNoExtraItems;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        List<Item> ingredients = container.getItems().stream()
            .map(ItemStack::getItem)
            .filter(item -> item.equals(Items.GHAST_TEAR) || item.equals(Items.CHARCOAL) || item.equals(Items.BLUE_DYE)).toList();

        ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);

        int emptySlots = container.getItems().stream()
            .filter(ItemStack::isEmpty)
            .toList().size();
        int allSlotsMinusFilledSlots = container.getContainerSize() - 1 - ingredients.size();
        boolean hasNoExtraItems = emptySlots == allSlotsMinusFilledSlots;

        if (book != null && ingredients.size() == 3 &&
            ingredients.contains(Items.GHAST_TEAR) && ingredients.contains(Items.CHARCOAL) && ingredients.contains(Items.BLUE_DYE)
            && hasNoExtraItems) {
            ItemStack ret = book.copy();
            int width = ret.getOrCreateTagElement(Constants.MODID).getInt(Constants.WIDTH);
            CompoundTag tags = ret.getOrCreateTagElement(Constants.MODID);
            tags.putBoolean(Constants.INKED, true);
            tags.putInt(Constants.INK_GOAL, ((width + 2) * (width + 2) - width * width) / 3); // Determines the INK_GOAL
            tags.putInt(Constants.INK_PROGRESS, 0);
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
