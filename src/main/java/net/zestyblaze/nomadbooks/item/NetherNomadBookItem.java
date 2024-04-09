package net.zestyblaze.nomadbooks.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.zestyblaze.nomadbooks.util.Constants;

public class NetherNomadBookItem extends NomadBookItem implements DyeableLeatherItem {
	public NetherNomadBookItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		super.getDefaultInstance();
		ItemStack itemStack = new ItemStack(this);
		CompoundTag tags = itemStack.getOrCreateTagElement(Constants.MODID);
		tags.putInt(Constants.HEIGHT, 3);
		tags.putInt(Constants.WIDTH, 7);
		tags.putString(Constants.STRUCTURE, NETHER_DEFAULT_STRUCTURE_PATH);
		return itemStack;
	}
}
