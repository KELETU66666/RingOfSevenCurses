package keletu.cursedring.event;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import keletu.cursedring.CursedRingMod;
import static keletu.cursedring.CursedRingMod.MODID;
import static keletu.cursedring.CursedRingMod.cursedRing;
import keletu.cursedring.core.ConfigSCR;
import static keletu.cursedring.core.ConfigSCR.painMultiplier;
import static keletu.cursedring.core.ConfigSCR.ultraHardcore;
import keletu.cursedring.core.CursedRing;
import keletu.cursedring.core.EntityItemIndestructible;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import static net.minecraftforge.fml.common.eventhandler.EventPriority.HIGH;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.*;

@Mod.EventBusSubscriber(modid = MODID)
public class CREvents {

    private static final Map<UUID, NonNullList<ItemStack>> playerKeepsMapBaubles = new HashMap<>();
    private static final String SPAWN_WITH_CURSE = CursedRingMod.MODID + ".cursedring";

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone evt) {
        EntityPlayer newPlayer = evt.getEntityPlayer();
        EntityPlayer player = evt.getOriginal();

        CursedRingMod.soulCrystal.updatePlayerSoulMap(newPlayer);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isRecentlyHit() && event.getSource() != null && event.getSource().getTrueSource() instanceof EntityPlayer && hasCursed((EntityPlayer) event.getSource().getTrueSource())) {

            EntityLivingBase killed = event.getEntityLiving();

            if (!ConfigSCR.enableSpecialDrops)
                return;

            if (killed.getClass() == EntitySkeleton.class || killed.getClass() == EntityStray.class) {
                addDrop(event, getRandomSizeStack(Items.ARROW, 3, 15));
            } else if (killed.getClass() == EntityZombie.class || killed.getClass() == EntityHusk.class) {
                addDropWithChance(event, getRandomSizeStack(Items.SLIME_BALL, 1, 3), 25);
            } else if (killed.getClass() == EntitySpider.class || killed.getClass() == EntityCaveSpider.class) {
                addDrop(event, getRandomSizeStack(Items.STRING, 2, 12));
            } else if (killed.getClass() == EntityGuardian.class) {
                //addDropWithChance(event, new ItemStack(Items.NAUTILUS_SHELL, 1), 15);
                addDrop(event, getRandomSizeStack(Items.PRISMARINE_CRYSTALS, 2, 5));
            } else if (killed.getClass() == EntityElderGuardian.class) {
                addDrop(event, getRandomSizeStack(Items.PRISMARINE_CRYSTALS, 4, 16));
                addDrop(event, getRandomSizeStack(Items.PRISMARINE_SHARD, 7, 28));
                addOneOf(event,
                        //new ItemStack(guardianHeart, 1),
                        //new ItemStack(Items.HEART_OF_THE_SEA, 1),
                        new ItemStack(Items.GOLDEN_APPLE, 1, 1),
                        new ItemStack(Items.ENDER_EYE, 1));
                //,EnchantmentHelper.addRandomEnchantment(new Random(), new ItemStack(Items.TRIDENT, 1), 25+new Random().nextInt(15), true));
            } else if (killed.getClass() == EntityEnderman.class) {
                addDropWithChance(event, getRandomSizeStack(Items.ENDER_EYE, 1, 2), 40);
            } else if (killed.getClass() == EntityBlaze.class) {
                addDrop(event, getRandomSizeStack(Items.BLAZE_POWDER, 0, 5));
                //addDropWithChance(event, new ItemStack(EnigmaticLegacy.livingFlame, 1), 15);
            } else if (killed.getClass() == EntityPigZombie.class) {
                addDropWithChance(event, getRandomSizeStack(Items.GOLD_INGOT, 1, 3), 40);
                addDropWithChance(event, getRandomSizeStack(Items.GLOWSTONE_DUST, 1, 7), 30);
            } else if (killed.getClass() == EntityWitch.class) {
                addDropWithChance(event, new ItemStack(Items.GHAST_TEAR, 1), 30);
                //addDrop(event, getRandomSizeStack(Items.PHANTOM_MEMBRANE, 1, 3));
                addDropWithChance(event, new ItemStack(Items.GHAST_TEAR, 1), 30);
                //addDropWithChance(event, getRandomSizeStack(Items.PHANTOM_MEMBRANE, 1, 3), 50);
            } else if (/*killed.getClass() == PillagerEntity.class || */killed.getClass() == EntityVindicator.class) {
                addDrop(event, getRandomSizeStack(Items.EMERALD, 0, 4));
            } else if (killed.getClass() == EntityVillager.class) {
                addDrop(event, getRandomSizeStack(Items.EMERALD, 2, 6));
            } else if (killed.getClass() == EntityCreeper.class) {
                addDrop(event, getRandomSizeStack(Items.GUNPOWDER, 4, 12));
            } else if (killed.getClass() == EntityEvoker.class) {
                addDrop(event, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
                addDrop(event, getRandomSizeStack(Items.EMERALD, 5, 20));
                addDropWithChance(event, new ItemStack(Items.GOLDEN_APPLE, 1, 1), 10);
                addDropWithChance(event, getRandomSizeStack(Items.ENDER_PEARL, 1, 3), 30);
                addDropWithChance(event, getRandomSizeStack(Items.BLAZE_ROD, 2, 4), 30);
                addDropWithChance(event, getRandomSizeStack(Items.EXPERIENCE_BOTTLE, 4, 10), 50);
            } else if (killed.getClass() == EntityWitherSkeleton.class) {
                addDrop(event, getRandomSizeStack(Items.BLAZE_POWDER, 0, 3));
                addDropWithChance(event, new ItemStack(Items.GHAST_TEAR, 1), 20);
                //addDropWithChance(event, new ItemStack(Items.NETHERITE_SCRAP, 1), 7);
                //} else if (killed.getClass() == EntityGhast.class) {
                //    addDrop(event, getRandomSizeStack(Items.PHANTOM_MEMBRANE, 1, 4));
                //} else if (killed.getClass() == DrownedEntity.class) {
                //    addDropWithChance(event, getRandomSizeStack(Items.LAPIS_LAZULI, 1, 3), 30);
            } else if (killed.getClass() == EntityVex.class) {
                addDrop(event, getRandomSizeStack(Items.GLOWSTONE_DUST, 0, 2));
                //   addDropWithChance(event, new ItemStack(Items.PHANTOM_MEMBRANE, 1), 30);
                //}  else if (killed.getClass() == PiglinEntity.class) {
                //    addDropWithChance(event, getRandomSizeStack(Items.GOLD_INGOT, 2, 4), 50);
                //} else if (killed.getClass() == RavagerEntity.class) {
                //    addDrop(event, getRandomSizeStack(Items.EMERALD, 3, 10));
                //    addDrop(event, getRandomSizeStack(Items.LEATHER, 2, 7));
                //    addDropWithChance(event, getRandomSizeStack(Items.DIAMOND, 0, 4), 50);
            } else if (killed.getClass() == EntityMagmaCube.class) {
                addDrop(event, getRandomSizeStack(Items.BLAZE_POWDER, 0, 1));
            } else if (killed.getClass() == EntityChicken.class) {
                addDropWithChance(event, new ItemStack(Items.EGG, 1), 50);
            } else if (killed.getClass() == EntityWither.class) {
                addDrop(event, new ItemStack(Items.NETHER_STAR, 1));
            }
        }
    }

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

    @SubscribeEvent
    public static void onEvent(BlockEvent.HarvestDropsEvent event) {
        if (event.getHarvester() != null && !event.getDrops().isEmpty()) {
            EntityPlayer player = event.getHarvester();

            //Copied better survival mod by mujmajnkraft from https://github.com/mujmajnkraft/BetterSurvival, under MIT License.
            if (hasCursed(player)) {
                LootTable loottable = player.world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(MODID, "cursed_drops"));
                LootContext.Builder context = (new LootContext.Builder((WorldServer) player.world).withLuck(ConfigSCR.fortuneBonus));
                event.getDrops().addAll(loottable.generateLootForPools(player.world.rand, context.build()));
            }
        }
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
                    if (isCursed(stack) && !hasCursed(player)) {
                        if (!player.inventory.addItemStackToInventory(stack)) {
                            player.dropItem(stack, false);
                        }
                        player.inventory.armorInventory.set(slot.getIndex(), ItemStack.EMPTY);
                        player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_WITHER_HURT, SoundCategory.PLAYERS, 1.0f, 0.5F);
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
        if (event.getEntityPlayer() == null)
            return;

        for (ResourceLocation rl : ConfigSCR.cursedItemList) {
            if (event.getItemStack().getItem() == ForgeRegistries.ITEMS.getValue(rl)) {
                TextFormatting color = !hasCursed(event.getEntityPlayer()) ? TextFormatting.DARK_RED : TextFormatting.GRAY;
                event.getToolTip().add(1, color + I18n.format("tooltip.cursedring.cursedOnesOnly1"));
                event.getToolTip().add(2, color + I18n.format("tooltip.cursedring.cursedOnesOnly2"));
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void inventoryInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (Minecraft.getMinecraft().player == null)
            return;

        if (event.getGui() instanceof GuiInventory) {
            if (hasCursed(Minecraft.getMinecraft().player))
                event.getButtonList().add(new EnderChestInventoryButton(7501, (event.getGui().width / 2) + ConfigSCR.iconOffset, (event.getGui().height / 2) - 111, ""));
        }
    }

    @SubscribeEvent
    public static void tickHandler(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;

        IBaublesItemHandler baublesHandler = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baublesHandler.getSlots(); i++) {
            ItemStack stack = baublesHandler.getStackInSlot(i);
            if (isCursed(stack) && !hasCursed(player)) {
                if (!player.inventory.addItemStackToInventory(stack)) {
                    player.dropItem(stack, false);
                }
                baublesHandler.setStackInSlot(i, ItemStack.EMPTY);
                player.world.playSound(null, event.player.getPosition(), SoundEvents.ENTITY_WITHER_HURT, SoundCategory.PLAYERS, 1.0f, 0.5F);
            }
        }
    }


    @SubscribeEvent
    public static void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        BlockPos where = event.getPos();
        boolean shouldBoost = false;

        int radius = 16;
        List<EntityPlayer> players = event.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(where.add(-radius, -radius, -radius), where.add(radius, radius, radius)));

        for (EntityPlayer player : players)
            if (BaublesApi.isBaubleEquipped(player, cursedRing) != -1) {
                shouldBoost = true;
            }

        if (shouldBoost) {
            event.setLevel(event.getLevel() + ConfigSCR.enchantingBonus);
        }
    }

    @SubscribeEvent
    public static void onLivingKnockback(LivingKnockBackEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && BaublesApi.isBaubleEquipped((EntityPlayer) event.getEntityLiving(), cursedRing) != -1) {
            event.setStrength(event.getStrength() * ConfigSCR.knockbackDebuff);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (player.isPlayerSleeping() && player.getSleepTimer() > 90 && BaublesApi.isBaubleEquipped(player, cursedRing) != -1) {
                player.sleepTimer = 90;
            }
            if (player.isBurning() && BaublesApi.isBaubleEquipped(player, cursedRing) != -1) {
                player.setFire(player.fire + 2);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        if (event.getAmount() >= Float.MAX_VALUE)
            return;

        if (event.getEntityLiving() instanceof EntityPlayer && BaublesApi.isBaubleEquipped((EntityPlayer) event.getEntityLiving(), cursedRing) != -1) {
            event.setAmount(event.getAmount() * painMultiplier);
        }
        if (event.getEntityLiving() instanceof EntityMob) {
            if (event.getSource().getTrueSource() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
                if (BaublesApi.isBaubleEquipped(player, cursedRing) != -1) {
                    event.setAmount(event.getAmount() * ConfigSCR.monsterDamageDebuff);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        EntityPlayer player = event.getAttackingPlayer();
        int bonusExp = 0;

        if (player != null && BaublesApi.isBaubleEquipped(player, cursedRing) != -1) {
            bonusExp += event.getOriginalExperience() * ConfigSCR.experienceBonus;
        }

        event.setDroppedExperience(event.getDroppedExperience() + bonusExp);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void keepRingCurses(LivingDeathEvent event) {
        EntityLivingBase living = event.getEntityLiving();

        if (!living.world.isRemote && living instanceof EntityPlayer)
            playerKeepsMapBaubles.put(living.getUniqueID(), keepBaubles((EntityPlayer) living));
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
        if (!event.isEndConquered()) {
            NonNullList<ItemStack> baubles = playerKeepsMapBaubles.remove(event.player.getUniqueID());
            if (baubles != null) {
                returnBaubles(event.player, baubles);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        dropStoredItems(event.player);
    }

    @SubscribeEvent
    public static void onPlayerJoin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {

        if (ultraHardcore) {
            NBTTagCompound playerData = event.player.getEntityData();
            NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ? playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();

            if (!data.getBoolean(SPAWN_WITH_CURSE)) {
                IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(event.player);
                if (BaublesApi.getBaublesHandler(event.player).getStackInSlot(1) == ItemStack.EMPTY)
                    baubles.setStackInSlot(1, new ItemStack(cursedRing));
                else
                    ItemHandlerHelper.giveItemToPlayer(event.player, new ItemStack(cursedRing));
                data.setBoolean(SPAWN_WITH_CURSE, true);
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            }
        }

        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            CursedRingMod.soulCrystal.updatePlayerSoulMap(player);
        }
    }

    private static void dropStoredItems(EntityPlayer player) {
        NonNullList<ItemStack> baubles = playerKeepsMapBaubles.remove(player.getUniqueID());
        if (baubles != null) {
            for (ItemStack itemStack : baubles) {
                if (!itemStack.isEmpty()) {
                    player.dropItem(itemStack, true, false);
                }
            }
        }
    }


    public static NonNullList<ItemStack> keepBaubles(EntityPlayer player) {

        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        NonNullList<ItemStack> kept = NonNullList.withSize(baubles.getSlots(), ItemStack.EMPTY);

        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (stack.getItem() instanceof CursedRing) {
                kept.set(i, baubles.getStackInSlot(i).copy());
                baubles.setStackInSlot(i, ItemStack.EMPTY);
                if (player instanceof EntityPlayerMP && CursedRingMod.soulCrystal.getLostCrystals(player) < 9) {
                    ItemStack soulCrystal = CursedRingMod.soulCrystal.createCrystalFrom(player);
                    EntityItemIndestructible droppedSoulCrystal = new EntityItemIndestructible(player.world, player.posX, player.posY + 1.5, player.posZ, soulCrystal);
                    droppedSoulCrystal.setOwnerId(player.getUniqueID());
                    player.world.spawnEntity(droppedSoulCrystal);
                }
            }
        }

        return kept;
    }

    public static void returnBaubles(EntityPlayer player, NonNullList<ItemStack> items) {

        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);

        if (items.size() != baubles.getSlots()) {
            giveItems(player, items);
            return;
        }

        NonNullList<ItemStack> displaced = NonNullList.create();

        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack kept = items.get(i);
            if (!kept.isEmpty()) {
                ItemStack existing = baubles.getStackInSlot(i);
                baubles.setStackInSlot(i, kept);
                if (!existing.isEmpty()) {
                    displaced.add(existing);
                }
            }
        }

        giveItems(player, displaced);
    }

    private static void giveItems(EntityPlayer player, NonNullList<ItemStack> items) {
        for (ItemStack stack : items) {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
    }

    public static void genericEnforce(Event event, EntityPlayer player, ItemStack stack) {
        if (!event.isCancelable() || event.isCanceled() || player == null || stack == null || stack.isEmpty() || player.isCreative())
            return;

        if (!hasCursed(player) && isCursed(stack)) {
            event.setCanceled(true);
            player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_WITHER_HURT, SoundCategory.PLAYERS, 1.0f, 0.5F);

        }
    }

    public static void enforce(PlayerInteractEvent event) {
        genericEnforce(event, event.getEntityPlayer(), event.getItemStack());
    }

    private static boolean isCursed(ItemStack stack) {
        return ConfigSCR.cursedItemList.contains(stack.getItem().getRegistryName());
    }

    public static boolean hasCursed(EntityPlayer player) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        List<Item> baubleList = new ArrayList<>();
        if (baubles.getStackInSlot(1) != ItemStack.EMPTY)
            baubleList.add(baubles.getStackInSlot(1).getItem());
        if (baubles.getStackInSlot(2) != ItemStack.EMPTY)
            baubleList.add(baubles.getStackInSlot(2).getItem());

        return baubleList.contains(cursedRing);
    }

    public static void addDrop(LivingDropsEvent event, ItemStack drop) {
        EntityItem entityitem = new EntityItem(event.getEntityLiving().world, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, drop);
        entityitem.setPickupDelay(10);
        event.getDrops().add(entityitem);
    }

    public static void addDropWithChance(LivingDropsEvent event, ItemStack drop, int chance) {
        if (new Random().nextInt(100) < chance) {
            addDrop(event, drop);
        }
    }

    public static ItemStack getRandomSizeStack(Item item, int minAmount, int maxAmount) {
        return new ItemStack(item, minAmount + new Random().nextInt(maxAmount - minAmount + 1));
    }

    public static void addOneOf(LivingDropsEvent event, ItemStack... itemStacks) {
        int chosenStack = new Random().nextInt(itemStacks.length);
        addDrop(event, itemStacks[chosenStack]);
    }


}
