package keletu.cursedring.core;

import baubles.api.IBauble;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Map;

public abstract class ItemBaseCurio extends ItemBase implements IBauble {

	public ItemBaseCurio() {
		this.maxStackSize = 1;
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase entityLivingBase) {
		// Insert existential void here
	}

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase entityLivingBase) {
		// Insert existential void here
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entityLivingBase) {
		// Insert existential void here
	}

	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		// Insert existential void here
	}

	@Override
	public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
		return true;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		Map<Enchantment, Integer> list = EnchantmentHelper.getEnchantments(book);

		if (list.size() == 1 && list.containsKey(Enchantments.BINDING_CURSE))
			return true;
		else
			return super.isBookEnchantable(stack, book);
	}
}