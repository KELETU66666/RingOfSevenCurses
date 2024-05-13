package keletu.cursedring;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import keletu.cursedring.core.ConfigSCR;
import static keletu.cursedring.core.ConfigSCR.painMultiplier;
import static keletu.cursedring.core.ConfigSCR.ultraHardcore;
import keletu.cursedring.core.CursedRing;
import keletu.cursedring.key.EnderChestRingHandler;
import keletu.cursedring.packet.PacketEnderRingKey;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod(
        modid = CursedRingMod.MODID,
        name = CursedRingMod.MOD_NAME,
        version = CursedRingMod.VERSION
)
public class CursedRingMod {

    public static final String MODID = "cursedring";
    public static final String MOD_NAME = "Ring of Seven Curses";
    public static final String VERSION = "0.1.0";

    public static Item cursedRing = new CursedRing();
    private static final Map<UUID, NonNullList<ItemStack>> playerKeepsMapBaubles = new HashMap<>();
    private static final String SPAWN_WITH_QUEST_BOOK = CursedRingMod.MODID + ".cursedring";

    public static EnumRarity CURSE_RARITY = EnumHelper.addRarity("Curses", TextFormatting.DARK_RED, "Curses");
    public static SimpleNetworkWrapper packetInstance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigSCR.onConfig(event);

        packetInstance = NetworkRegistry.INSTANCE.newSimpleChannel("CursedChannel");
        packetInstance.registerMessage(PacketEnderRingKey.Handler.class, PacketEnderRingKey.class, 0, Side.SERVER);

        if(event.getSide().isClient())
            EnderChestRingHandler.registerKeybinds();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {

        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(cursedRing);
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void modelRegistryEvent(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(cursedRing, 0, new ModelResourceLocation(cursedRing.getRegistryName(), "inventory"));
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
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (!event.isEndConquered()) {
                NonNullList<ItemStack> baubles = playerKeepsMapBaubles.remove(event.player.getUniqueID());
                if (baubles != null) {
                    returnBaubles(event.player, baubles);
                }
            }

            if (!event.player.world.isRemote && BaublesApi.isBaubleEquipped(event.player, cursedRing) != -1) {
                event.player.setHealth(event.player.getMaxHealth() * 0.5F);
            }

        }

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            dropStoredItems(event.player);
        }

        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {

            if (ultraHardcore) {
                NBTTagCompound playerData = event.player.getEntityData();
                NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ? playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();

                if (!data.getBoolean(SPAWN_WITH_QUEST_BOOK)) {
                    IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(event.player);
                    if (BaublesApi.getBaublesHandler(event.player).getStackInSlot(1) == ItemStack.EMPTY)
                        baubles.setStackInSlot(1, new ItemStack(cursedRing));
                    else
                        ItemHandlerHelper.giveItemToPlayer(event.player, new ItemStack(cursedRing));
                    data.setBoolean(SPAWN_WITH_QUEST_BOOK, true);
                    playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
                }
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
    }
}
