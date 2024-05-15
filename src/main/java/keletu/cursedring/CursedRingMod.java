package keletu.cursedring;

import keletu.cursedring.entity.EntityItemIndestructible;
import keletu.cursedring.entity.RenderEntityItemIndestructible;
import keletu.cursedring.item.ItemCursedRing;
import keletu.cursedring.item.ItemSoulCrystal;
import keletu.cursedring.key.EnderChestRingHandler;
import keletu.cursedring.packet.PacketEnderRingKey;
import keletu.cursedring.packet.PacketRecallParticles;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.logging.Logger;

@Mod(
        modid = CursedRingMod.MODID,
        name = CursedRingMod.MOD_NAME,
        version = CursedRingMod.VERSION
)
public class CursedRingMod {

    public static final String MODID = "cursedring";
    public static final String MOD_NAME = "Ring of Seven Curses";
    public static final String VERSION = "1.0.0";

    public static Item cursedRing = new ItemCursedRing();
    public static ItemSoulCrystal soulCrystal = new ItemSoulCrystal();

    public static EnumRarity CURSE_RARITY = EnumHelper.addRarity("Curses", TextFormatting.DARK_RED, "Curses");
    public static SimpleNetworkWrapper packetInstance;
    public static Logger logger;
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigSCR.onConfig(event);

        packetInstance = NetworkRegistry.INSTANCE.newSimpleChannel("CursedChannel");
        packetInstance.registerMessage(PacketRecallParticles.Handler.class, PacketRecallParticles.class, 0, Side.CLIENT);
        packetInstance.registerMessage(PacketEnderRingKey.Handler.class, PacketEnderRingKey.class, 1, Side.SERVER);

        if (event.getSide().isClient())
            EnderChestRingHandler.registerKeybinds();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        EntityRegistry.registerModEntity(new ResourceLocation(MODID + ":" + "permanent_item"), EntityItemIndestructible.class, "permanent_item", 0, MODID, 80, 3, true);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {

        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(cursedRing);
            event.getRegistry().register(soulCrystal);
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void modelRegistryEvent(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(cursedRing, 0, new ModelResourceLocation(cursedRing.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(soulCrystal, 0, new ModelResourceLocation(soulCrystal.getRegistryName(), "inventory"));

            RenderingRegistry.registerEntityRenderingHandler(EntityItemIndestructible.class, manager -> {
                try {
                    return RenderEntityItemIndestructible.class.getConstructor(RenderManager.class).newInstance(manager);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });
        }
    }
}
