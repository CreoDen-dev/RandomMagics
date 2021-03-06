package randommagics;

import java.util.HashMap;
import java.util.Random;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.config.Configuration;
import randommagics.items.ItemInfinityStone;
import randommagics.items.ItemWandRodPrim;
import randommagics.packets.RmNetworkRegistry;
import randommagics.render.BlockDeviceRenderer;
import randommagics.render.EntitySpecialRenderer;
import randommagics.render.ModShaders;
import randommagics.rituals.PowerRitualReciepe;
import randommagics.rituals.RitualHelper;
import randommagics.Init;
import randommagics.blocks.BlockManaPool;
import randommagics.blocks.TileAlchemyPlant;
import randommagics.blocks.TileCloud;
import randommagics.blocks.TileInfusionMatrixExtended;
import randommagics.blocks.TileInventoryAccess;
import randommagics.blocks.TileManaPool;
import randommagics.blocks.TileVisCell;
import randommagics.commands.comRm;
import randommagics.curses.Curse;
import randommagics.entities.EntityBlockProjectile;
import randommagics.entities.EntityEntityProjectile;
import randommagics.entities.EntityExpulosion;
import randommagics.entities.EntityPotionProjectile;
import randommagics.entities.EntityPowerBlast;
import randommagics.entities.EntityStealingOrb;
import randommagics.gui.GUIHandler;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.wands.WandRod;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileCentrifuge;
import thaumicenergistics.api.ThEApi;

@Mod(modid="randommagics",name="RandomMagics",version="0.4.1", dependencies = "after:thaumicenergistics,Botania")
public class Main {

	@Mod.Instance
	public static Main instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.config(event);
		EnchantmentInit.init();
		Init.init();
		EntityInit.init();
		
		if (Loader.isModLoaded("Botania")) {
			GameRegistry.registerTileEntity(TileManaPool.class, "TileManaPool");
			GameRegistry.registerBlock(new BlockManaPool(), "ManaPool");
		}
    }

	@EventHandler
    public void init(FMLInitializationEvent event) {
		RmNetworkRegistry.registerMessages();
    }
	
	@SideOnly(Side.CLIENT)
    @EventHandler
    public void preInitClient(FMLInitializationEvent event) {
		RenderInit.init();
		ModShaders.register();
    }

	@EventHandler
    public void postInit(FMLPostInitializationEvent event) {
		BookTab.setup();
		Researches.Research();
		
		if (Loader.isModLoaded("thaumicenergistics")) {
			ThEApi.instance().transportPermissions().addAspectContainerTileToExtractPermissions(TileAlchemyPlant.class, 0);
		}
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new comRm());
	}
}
