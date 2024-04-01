package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.BookUpgradeItem;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

public class NomadBookUpgradeRecipe extends ShapelessRecipe {
    public NomadBookUpgradeRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, "", category, new ItemStack(ModItems.NOMAD_BOOK), NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.NOMAD_BOOK.getDefaultInstance(), ModItems.NETHER_NOMAD_BOOK.getDefaultInstance()), Ingredient.of(ModItems.AQUATIC_MEMBRANE_PAGE, ModItems.MYCELIUM_PAGE)));
    }

    // TODO simplify matches and assemble
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack book = null;
        String upgrade = null;

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            if (book == null && itemStack.getItem() instanceof NomadBookItem) {
                book = itemStack;
            } else if (upgrade == null && itemStack.getItem() instanceof BookUpgradeItem) {
                upgrade = ((BookUpgradeItem) itemStack.getItem()).getUpgrade();
            } else if (!itemStack.isEmpty()) {
                return false;
            }
        }

        return book != null && upgrade != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack book = null;
        String upgrade = null;

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack itemStack = container.getItem(i);
            if (book == null && itemStack.getItem() instanceof NomadBookItem) {
                book = itemStack;
            } else if (upgrade == null && itemStack.getItem() instanceof BookUpgradeItem) {
                upgrade = ((BookUpgradeItem) itemStack.getItem()).getUpgrade();
            } else if (!itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        if (book != null && upgrade != null) {
            ItemStack ret = book.copy();
            ListTag upgradeList = ret.getOrCreateTagElement(Constants.MODID).getList(Constants.UPGRADES, Tag.TAG_STRING);
            if (!upgradeList.contains(StringTag.valueOf(upgrade))) {
                upgradeList.add(StringTag.valueOf(upgrade));
            }
            ret.getOrCreateTagElement(Constants.MODID).put(Constants.UPGRADES, upgradeList);

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
