package net.zestyblaze.nomadbooks.item;

import net.minecraft.item.Item;

public class BookUpgradeItem extends Item {
    private final String upgrade;

    public BookUpgradeItem(Settings properties, String upgrade) {
        super(properties);
        this.upgrade = upgrade;
    }

    public String getUpgrade() {
        return upgrade;
    }
}
