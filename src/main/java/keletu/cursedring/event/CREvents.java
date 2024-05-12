package keletu.cursedring.event;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import static keletu.cursedring.CursedRingMod.MODID;
import static keletu.cursedring.CursedRingMod.cursedRing;
import keletu.cursedring.core.ConfigSCR;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import static net.minecraftforge.fml.common.eventhandler.EventPriority.HIGH;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = MODID)
public class CREvents {

    private static List<String> toolTip = new ArrayList<>();

    @SubscribeEvent(priority = HIGH)
    public static void hurtEvent(LivingAttackEvent event) {
        if (!event.isCanceled() && event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
            genericEnforce(event, player, player.getHeldItemMainhand());
        }
    }

    @SubscribeEvent(priority = HIGH)
    public static void leftClick(PlayerInteractEvent.LeftClickBlock event) {
        enforce(event);
        if (event.isCanceled()) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        Block block = state.getBlock();
        ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        if (stack.isEmpty()) {
            stack = block.getItem(event.getWorld(), event.getPos(), state);
        }
        if (block.hasTileEntity(state)) {
            TileEntity te = event.getWorld().getTileEntity(event.getPos());
            if (te != null && !te.isInvalid()) {
                stack.setTagCompound(te.writeToNBT(new NBTTagCompound()));
            }
        }
        genericEnforce(event, event.getEntityPlayer(), stack);
    }

    @SubscribeEvent(priority = HIGH)
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        enforce(event);
        if (event.isCanceled()) {
            return;
        } else if (event.getItemStack().isEmpty()) {
            //Don't let the block get activated just because this hand is empty
            EntityPlayer player = event.getEntityPlayer();
            genericEnforce(event, player, event.getHand().equals(EnumHand.MAIN_HAND) ? player.getHeldItemOffhand() : player.getHeldItemMainhand());
            if (event.isCanceled()) {
                return;
            }
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        Block block = state.getBlock();
        ItemStack stack = new ItemStack(block, 1, state.getBlock().getMetaFromState(state));
        if (stack.isEmpty()) {
            stack = block.getItem(event.getWorld(), event.getPos(), state);
        }
        if (block.hasTileEntity(state)) {
            TileEntity te = event.getWorld().getTileEntity(event.getPos());
            if (te != null && !te.isInvalid()) {
                stack.setTagCompound(te.writeToNBT(new NBTTagCompound()));
            }
        }
        genericEnforce(event, event.getEntityPlayer(), stack);
    }

    @SubscribeEvent(priority = HIGH)
    public static void rightClickItem(PlayerInteractEvent.RightClickItem event) {
        enforce(event);
    }

    @SubscribeEvent(priority = HIGH)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) {
            return;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        if (state.getBlock().hasTileEntity(state)) {
            TileEntity te = event.getWorld().getTileEntity(event.getPos());
            if (te != null && !te.isInvalid()) {
                stack.setTagCompound(te.writeToNBT(new NBTTagCompound()));
            }
        }
        genericEnforce(event, event.getPlayer(), stack);
    }

    @SubscribeEvent
    public static void onArmorEquip(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            if ((!player.isCreative()) && !(player instanceof FakePlayer)) {
                EntityEquipmentSlot slot = event.getSlot();
                if (slot.getSlotType().equals(EntityEquipmentSlot.Type.ARMOR)) {
                    ItemStack stack = player.inventory.armorInventory.get(slot.getIndex());
                    if (isCursed(stack) && !hasCursed(player, cursedRing)) {
                        if (!player.inventory.addItemStackToInventory(stack)) {
                            player.dropItem(stack, false);
                        }
                        player.inventory.armorInventory.set(slot.getIndex(), ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = HIGH)
    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        enforce(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onTooltip(ItemTooltipEvent event) {
        if(event.getEntityPlayer() == null)
            return;

        for(ResourceLocation rl : ConfigSCR.cursedItemList) {
            if (event.getItemStack().getItem() == ForgeRegistries.ITEMS.getValue(rl)) {
                TextFormatting color = !hasCursed(event.getEntityPlayer(), cursedRing) ? TextFormatting.DARK_RED : TextFormatting.GRAY;
                event.getToolTip().add(1, color + I18n.format("tooltip.enigmaticlegacy.cursedOnesOnly1"));
                event.getToolTip().add(2, color + I18n.format("tooltip.enigmaticlegacy.cursedOnesOnly2"));
            }
        }
    }
    public static void genericEnforce(Event event, EntityPlayer player, ItemStack stack) {
        if (!event.isCancelable() || event.isCanceled() || player == null || stack == null || stack.isEmpty() || player.isCreative())
            return;

        if (!hasCursed(player, cursedRing)) {
            if (isCursed(stack))
                event.setCanceled(true);
        }
    }

    public static void enforce(PlayerInteractEvent event) {
        genericEnforce(event, event.getEntityPlayer(), event.getItemStack());
    }

    private static boolean isCursed(ItemStack stack) {
        return ConfigSCR.cursedItemList.contains(stack.getItem().getRegistryName());
    }

    public static boolean hasCursed(EntityPlayer player, Item theBauble) {

        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        List<Item> baubleList = new ArrayList<>();
        if (baubles.getStackInSlot(1) != ItemStack.EMPTY)
            baubleList.add(baubles.getStackInSlot(1).getItem());
        if (baubles.getStackInSlot(2) != ItemStack.EMPTY)
            baubleList.add(baubles.getStackInSlot(2).getItem());

        return baubleList.contains(theBauble);
    }
}
