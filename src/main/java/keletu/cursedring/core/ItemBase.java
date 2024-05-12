package keletu.cursedring.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public abstract class ItemBase extends Item {
	protected boolean isPlaceholder;
	public ItemBase() {
		this.isPlaceholder = false;
		this.maxStackSize = 64;
	}

	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		// Insert existential void here
	}


}