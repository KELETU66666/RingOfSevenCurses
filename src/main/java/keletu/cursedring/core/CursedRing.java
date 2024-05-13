package keletu.cursedring.core;

import baubles.api.BaubleType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import static keletu.cursedring.core.ConfigSCR.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CursedRing extends ItemBaseCurio {

	protected final Multimap<String, AttributeModifier> attributeMap = HashMultimap.create();

	public CursedRing() {
		this.setRegistryName("cursed_ring");
		this.setTranslationKey("cursed_ring");

		this.attributeMap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(UUID.fromString("371929FC-4CBC-11E8-842F-0ED5F89F718B"), "generic.armor", -armorDebuff, 2));
		this.attributeMap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(UUID.fromString("22E6BD72-4CBD-11E8-842F-0ED5F89F718B"), "generic.armorToughness", -armorDebuff, 2));
		this.attributeMap.put(SharedMonsterAttributes.LUCK.getName(), new AttributeModifier(UUID.fromString("F34BB326-D435-4B63-8254-0B6CB57A8E6F"), "generic.luck", lootingBonus, 0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (GuiScreen.isShiftKeyDown()) {
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing3"));
			if (painMultiplier == 2.0) {
				list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing4"));
			} else {
				list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing4_alt") + TextFormatting.GOLD + painMultiplier+"%");
			}
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing5"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing6") + TextFormatting.GOLD + Math.round (armorDebuff * 100) + "%" + I18n.format("tooltip.enigmaticlegacy.cursedRing6_1"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing7") + TextFormatting.GOLD + Math.round (monsterDamageDebuff * 100) + "%"  + I18n.format("tooltip.enigmaticlegacy.cursedRing7_1"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing8"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing9"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing10"));
			list.add("");
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing11"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing12") + TextFormatting.GOLD + lootingBonus + I18n.format("tooltip.enigmaticlegacy.cursedRing12_1"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing13"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing14") + TextFormatting.GOLD + experienceBonus+ "%" + I18n.format("tooltip.enigmaticlegacy.cursedRing14_1"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing15") + TextFormatting.GOLD + enchantingBonus + I18n.format("tooltip.enigmaticlegacy.cursedRing15_1"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing16"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing17"));
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing18"));
		} else {
			list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing1"));

			if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative()) {
				list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing2_creative"));
			} else {
				list.add(I18n.format("tooltip.enigmaticlegacy.cursedRing2"));
			}
		}
	}

	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase living) {
		if (living instanceof EntityPlayer && ((EntityPlayer) living).isCreative())
			return super.canUnequip(stack, living);
		else
			return false;
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase player) {
		if (!player.world.isRemote) {
			player.getAttributeMap().applyAttributeModifiers(attributeMap);
		}
	}

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
		if (!player.world.isRemote) {
			player.getAttributeMap().removeAttributeModifiers(attributeMap);
		}
	}

	public boolean isItemDeathPersistent(ItemStack stack) {
		return stack.getItem().equals(this);
	}

	/**
	 * Creates and returns simple bounding box of given radius around the entity.
	 */

	public static AxisAlignedBB getBoundingBoxAroundEntity(final Entity entity, final double radius) {
		return new AxisAlignedBB(entity.posX - radius, entity.posY - radius, entity.posZ - radius, entity.posX + radius, entity.posY + radius, entity.posZ + radius);
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase livingPlayer) {
		if (livingPlayer.world.isRemote || !(livingPlayer instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer) livingPlayer;

		if (player.isCreative() || player.isSpectator())
			return;

		List<EntityLivingBase> genericMobs = livingPlayer.world.getEntitiesWithinAABB(EntityLivingBase.class, getBoundingBoxAroundEntity(player, neutralAngerRange));
		List<EntityEnderman> endermen = livingPlayer.world.getEntitiesWithinAABB(EntityEnderman.class, getBoundingBoxAroundEntity(player, endermenRandomportRange));

		for (EntityEnderman enderman : endermen) {
			if (itemRand.nextDouble() <= (0.002 * endermenRandomportFrequency)) {
				if (enderman.attemptTeleport(player.posX, player.posY, player.posZ) && player.canEntityBeSeen(enderman)) {
					enderman.setAttackTarget(player);
				}
			}

		}

		for (EntityLivingBase checkedEntity : genericMobs) {
			double angerDistance = Math.max(neutralAngerRange, neutralXRayRange);

			if (checkedEntity.getDistanceSq(player.posX, player.posY, player.posZ) > angerDistance * angerDistance) {
				continue;
			}

			if (checkedEntity instanceof EntityLiving) {
				EntityLiving neutral = (EntityLiving) checkedEntity;

				if (neutralAngerBlacklist.contains(checkedEntity.getDisplayName())) {
					continue;
				}

				if (neutral instanceof EntityTameable) {
					if (((EntityTameable)neutral).isTamed()) {
						continue;
					}
				} else if (neutral instanceof EntityIronGolem) {
					if (((EntityIronGolem)neutral).isPlayerCreated()) {
						continue;
					}
				} //else if (neutral instanceof BeeEntity) {
				//	if (saveTheBees || SuperpositionHandler.hasItem(player, EnigmaticLegacy.animalGuide)) {
				//		continue;
				//	}
				//}

				if (neutral.getRevengeTarget() == null || neutral.getRevengeTarget().isDead) {
					if (player.canEntityBeSeen(checkedEntity) || player.getDistance(checkedEntity) <= neutralXRayRange) {
						neutral.setRevengeTarget(player);
					} else {
						continue;
					}
				}
			}
		}

	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		Map<Enchantment, Integer> list = EnchantmentHelper.getEnchantments(book);

		if (list.containsKey(Enchantments.VANISHING_CURSE))
			return false;
		else
			return super.isBookEnchantable(stack, book);
	}

	public double getAngerRange() {
		return neutralAngerRange;
	}

	//TODO
	//@Override
	//public int getFortuneBonus(String identifier, EntityLivingBase EntityLivingBase, ItemStack curio, int index) {
	//	return super.getFortuneBonus(identifier, EntityLivingBase, curio, index) + fortuneBonus;
	//}
//
	//@Override
	//public int getLootingBonus(String identifier, EntityLivingBase EntityLivingBase, ItemStack curio, int index) {
	//	return super.getLootingBonus(identifier, EntityLivingBase, curio, index) + lootingBonus;
	//}

	@Override
	public BaubleType getBaubleType(ItemStack itemStack) {
		return BaubleType.RING;
	}
}