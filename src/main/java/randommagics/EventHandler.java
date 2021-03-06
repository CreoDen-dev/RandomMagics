package randommagics;

import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockOre;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import randommagics.curses.CurseBadLuck;
import randommagics.curses.CurseOutOfTime;
import randommagics.curses.CurseRegistry;
import randommagics.customs.BigExplosion;
import randommagics.customs.CustomExtendedEntityProperties;
import randommagics.customs.DemonBossFight;
import randommagics.customs.RandomUtils;
import randommagics.customs.Sound;
import randommagics.dimensions.CustomTeleporter;
import randommagics.enchantments.EnchantmentsHelper;
import randommagics.entities.EntityBigExplosion;
import randommagics.gui.GuiEldArmor;
import randommagics.packets.PacketDemonChooseAbility;
import randommagics.packets.PacketSyncOverlordArmorAnim;
import randommagics.packets.PacketUseDemonAbility;
import randommagics.packets.RmNetworkRegistry;
import randommagics.render.BlockDeviceRenderer;
import randommagics.render.ModShaders;
import randommagics.render.ShaderProgram;
import thaumcraft.common.entities.monster.EntityCultistCleric;
import thaumcraft.common.entities.projectile.EntityBottleTaint;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.utils.BlockUtils;

public class EventHandler {
	
	private static final UUID ID_demondamage = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ea00d");
	private static final UUID ID_demonknockback = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ba00d");
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		if (event.getPlayer() != null) {
			if (CurseRegistry.isPlayerCursed(event.getPlayer(), "badluck")) {
				if (event.world.rand.nextFloat() >= 0.5f) {
					if (event.block instanceof BlockOre) {
						event.world.setBlock(event.x, event.y, event.z, Blocks.stone);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.entity;
			CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(player);
			if (ex.demonLevel >= 9) {
				event.setCanceled(true);
				player.setHealth(1);
			}
		}
		if (!event.isCanceled() && event.source.getSourceOfDamage() != null && event.source.getSourceOfDamage() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.source.getSourceOfDamage();
			CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(player);
			if (ex.demonLevel == 2) {
				if (event.entityLiving.getMaxHealth() >= 100) {
					ex.nextDemonLvlProgress++;
				}
			}
			if (ex.demonLevel == 9) {
				ex.nextDemonLvlProgress += event.entityLiving.getMaxHealth();
			}
		}
		if (!event.isCanceled()) {
			if (event.entity instanceof EntityCultistCleric && event.source.getEntity() != null && !event.entity.worldObj.isRemote) {
				Random rand = new Random();
				if (rand.nextFloat() < 0.01f)
					RandomUtils.spawnItemInWorldAt(event.entity.worldObj, new ItemStack(Init.ItemCursedScroll), event.entity);
			}
		}
	}
	/*
	@SubscribeEvent
	public void onAttackEntityEvent(AttackEntityEvent event) {
	}*/
	
	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event) {
		if (event.source.getSourceOfDamage() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.source.getSourceOfDamage();
			CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(player);
			if (ex.demonLevel == 1) {
				if (event.ammount > 20)
					ex.nextDemonLvlProgress++;
			}
		}
		
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.entity;
			CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(player);
			if (player.getCurrentArmor(0) != null && player.getCurrentArmor(1) != null && player.getCurrentArmor(2) != null && player.getCurrentArmor(3) != null
					&& player.getCurrentArmor(3).getItem() == Init.OverlordHelmet && player.getCurrentArmor(2).getItem() == Init.OverlordChestplate
					&& player.getCurrentArmor(1).getItem() == Init.OverlordLeggins && player.getCurrentArmor(0).getItem() == Init.OverlordBoots) {
				
				if (event.source.isMagicDamage() || event.source.isFireDamage() || event.source.isExplosion() || event.ammount <= 5) {
					event.setCanceled(true);
				}
				if (event.source.isUnblockable() || (player.worldObj.rand.nextBoolean() && !event.isCanceled() && event.ammount > 2f && !player.capabilities.isCreativeMode && !player.capabilities.disableDamage && event.source.getSourceOfDamage() != null)) {
					event.setCanceled(true);
					player.hurtResistantTime = 40;
					RmNetworkRegistry.NETWORK.sendToAll(new PacketSyncOverlordArmorAnim(player.getCommandSenderName(), 10));
				}
			}
			if (ex.demonLevel >= 4) {
				if (event.source.isFireDamage()) {
					event.setCanceled(true);
				}
			}
			if (!event.isCanceled() && !player.worldObj.isRemote) {
				if (event.source.getSourceOfDamage() instanceof EntityLivingBase) {
					ItemStack itemInHand = ((EntityLivingBase)event.source.getSourceOfDamage()).getHeldItem();
					if (itemInHand != null && itemInHand.getItem() == Init.ItemClawsOfExile) {
						if (player.worldObj.rand.nextInt(100) < 5) {
							if (ex.demonLevel > 0 && ex.demonLevel < 7) {
								CustomTeleporter.teleportToDimension(player, -1, player.posX, player.posY, player.posZ);
							}
						}
					}
				}
			}
		}
		
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onLivingHurtEvent(LivingHurtEvent event) {
		System.out.println("On hurt HIGHEST");
		if (event.entity instanceof EntityPlayer) {
			System.out.println("entity is player");
			System.out.println("initial amount: " + event.ammount);
			EntityPlayer player = (EntityPlayer)event.entity;
			System.out.println("player health: " + player.getHealth() + "/" + player.getMaxHealth());
			CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(player);
			if (ex.demonLevel == 1 && event.source != DamageSource.starve) {
				event.ammount *= event.ammount;
			}
			if (ex.demonLevel > 2) {
				event.ammount /= ex.demonLevel * ex.demonLevel;
				if (event.ammount < Math.pow(ex.demonLevel / 2, ex.demonLevel)) {
					event.ammount = 0;
					event.setCanceled(true);
				}
			}
			if (ex.demonLevel >= 7) {
				if (event.source != DamageSource.outOfWorld || event.source != DamageSource.magic || event.source != DamageSource.generic) {
					event.ammount = 0;
					event.setCanceled(true);
				}
			}
			if (ex.demonLevel >= 4) {
				event.ammount /= Math.pow(10, ex.demonLevel);
				if (event.source == DamageSource.inFire || event.source == DamageSource.onFire) {
					event.ammount = 0;
					event.setCanceled(true);
				}
			}
			
			if (ex.demonLevel == 4) {
				ex.nextDemonLvlProgress += (int)Math.pow(event.ammount, 0.8D);
			}
			//Molecular armor
			if (player.getCurrentArmor(0) != null && player.getCurrentArmor(1) != null && player.getCurrentArmor(2) != null && player.getCurrentArmor(3) != null
					&& player.getCurrentArmor(3).getItem() == Init.OverlordHelmet && player.getCurrentArmor(2).getItem() == Init.OverlordChestplate
					&& player.getCurrentArmor(1).getItem() == Init.OverlordLeggins && player.getCurrentArmor(0).getItem() == Init.OverlordBoots) {
				if (event.source == DamageSource.magic) {
					event.ammount = 0;
					event.setCanceled(true);
				}
				if (event.source == DamageSource.inFire || event.source == DamageSource.onFire) {
					event.ammount = 0;
					event.setCanceled(true);
				}
				
				if (event.ammount < 80000f) {
					event.ammount = RandomUtils.lerp(event.ammount, event.ammount / 200f, event.ammount / 80000f);
				}
				else {
					event.ammount /= 1000f;
				}
				if (event.ammount > 100f && event.source.getSourceOfDamage() != null) {
					event.source.getSourceOfDamage().attackEntityFrom(DamageSource.magic, event.ammount * 10f);
					event.ammount /= 10f;
				}
				event.ammount /= 100f;
				event.ammount -= player.experienceTotal;
				if (event.ammount < 0) {
					event.ammount = 0;
					event.setCanceled(true);
				}
				if (event.ammount < 5f) {
					event.ammount = 0;
					event.setCanceled(true);
				}
			}
			//Eldritch armor
			if (player.getCurrentArmor(0) != null && player.getCurrentArmor(1) != null && player.getCurrentArmor(2) != null && player.getCurrentArmor(3) != null
					&& player.getCurrentArmor(3).getItem() == Init.EldHelmet && player.getCurrentArmor(2).getItem() == Init.EldChestplate
					&& player.getCurrentArmor(1).getItem() == Init.EldLeggins && player.getCurrentArmor(0).getItem() == Init.EldBoots) {
				System.out.println("is wearing armor");
				event.ammount *= 0.2f;
				if (event.ammount < player.getMaxHealth()) {
					event.ammount /= 10;
				}
				else {
					event.ammount /= 100;
				}
				if (event.ammount < 1f) {
					event.ammount = 0;
					event.setCanceled(true);
				}
			}
		}
		System.out.println("result amount: " + event.ammount + "result cancelled: " + event.isCanceled());
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void onLivingHurtEventLowest(LivingHurtEvent event) {
		if (event.source.damageType.contentEquals("disintegration")) {
			event.entityLiving.setHealth(0);
			event.entityLiving.onDeath(event.source);
		}
	}
	
	/*
	@SubscribeEvent
	public void EntityInteractEvent(PlayerInteractEvent event) {
		if (event.entityPlayer != null && event.entityPlayer.getCurrentEquippedItem() != null) {
			if (event.entityPlayer.getCurrentEquippedItem() != null && event.entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemWandCasting) {
				ItemStack foc = ((ItemWandCasting)event.entityPlayer.getCurrentEquippedItem().getItem()).getFocusItem(event.entityPlayer.getCurrentEquippedItem());
				if (foc != null && foc.hasTagCompound()) {
					short mode = foc.getTagCompound().getShort("Mode");
					
					ItemStack stackList[] = new ItemStack[6];
					
			        NBTTagCompound compound = foc.getTagCompound();
			        NBTTagList items = compound.getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
		    		for (int i = 0; i < items.tagCount(); ++i)
		    		{

		    			NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
		    			int slot = item.getInteger("Slot");

		    			if (slot >= 0 && slot < stackList.length) {
		    				stackList[slot] = ItemStack.loadItemStackFromNBT(item);
		    			}
		    		}
		    		
		    		if (mode == 6 && stackList[mode - 1] != null) {
		    			event.entityLiving.setDead();
		    		}
				}
			}
		
	}
	*/
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (DemonBossFight.getCurrentFight() != null) {
			DemonBossFight.getCurrentFight().onUpdate(DemonBossFight.getCurrentFight().getPlayer().worldObj);
		}
	}
	
	static boolean grey_active;
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (DemonBossFight.getCurrentFight() != null && Minecraft.getMinecraft().theWorld != null) {
			DemonBossFight.getCurrentFight().onUpdate(Minecraft.getMinecraft().theWorld);
		}
		for (String i : dodgeAnim.keySet()) {
			dodgeAnim.put(i, dodgeAnim.get(i) > 0 ? dodgeAnim.get(i) - 1 : 0);
		}
		if (Minecraft.getMinecraft().thePlayer != null) {
			if (CurseRegistry.isPlayerCursed(Minecraft.getMinecraft().thePlayer, "outoftime")) {
				if (!Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(Sound.heartbeat)) {
					Minecraft.getMinecraft().getSoundHandler().playSound(Sound.heartbeat);
				}
			}
			else {
				if (Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(Sound.heartbeat)) {
					Minecraft.getMinecraft().getSoundHandler().stopSound(Sound.heartbeat);
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent//(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void onWorldRender(RenderWorldLastEvent event) {
		if (CurseRegistry.isPlayerCursed(Minecraft.getMinecraft().thePlayer, "outoftime")) {
			
			//if (ShaderProgram.currentShaderID != ModShaders.grey.programID) {
				ModShaders.grey.start();
				disableShaders = false;
			//}
		}
		else {
			if (ShaderProgram.currentShaderID == ModShaders.grey.programID) {
				ModShaders.grey.stop();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void onWorldRenderLowest(EntityViewRenderEvent.FogDensity event) {
		if (CurseRegistry.isPlayerCursed(Minecraft.getMinecraft().thePlayer, "outoftime")) {
			//if (ShaderProgram.currentShaderID != ModShaders.grey.programID) {
				ModShaders.grey.start();
				disableShaders = false;
			//}
		}
		else {
			if (ShaderProgram.currentShaderID == ModShaders.grey.programID) {
				ModShaders.grey.stop();
			}
		}
	}
	
	
	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		//Don't update if Stuck In Time
		if (event.entityLiving.isPotionActive(Init.EffectStuckInTime.id)) {
			if (event.entityLiving instanceof EntityPlayer) {
				CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get((EntityPlayer)event.entityLiving);
				if (ex.demonLevel >= 9) {
					if (!ex.cursedWith("outoftime")) {
						ex.addCurse(new CurseOutOfTime());
					}
				}
				else {
					if (event.entityLiving.getActivePotionEffect(Init.EffectStuckInTime).onUpdate(event.entityLiving)) {
						event.setCanceled(true);
					}
					return;
				}
			}
			else {
				if (event.entityLiving.getActivePotionEffect(Init.EffectStuckInTime).onUpdate(event.entityLiving)) {
					event.setCanceled(true);
				}
				return;
			}
		}
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.entityLiving;
			CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(player);
			ex.doCurses();
			AttributeModifier demondamage = new AttributeModifier(ID_demondamage, "demondamage", Math.pow(ex.demonLevel, ex.demonLevel), 1);
			AttributeModifier demonknockback = new AttributeModifier(ID_demonknockback, "demonknockback", 2D, 0);
			if (ex.demonLevel == 3) {
				if (player.dimension == -1) {
					if (player.ticksExisted % 100 == 0) {
						ex.nextDemonLvlProgress++;
					}
				}
				else {
					ex.nextDemonLvlProgress = 0;
				}
			}
			if (ex.demonLevel == 6) {
				ex.nextDemonLvlProgress += player.experienceTotal;
				player.experienceTotal = 0;
				player.experience = 0;
				player.experienceLevel = 0;
			}
			if (ex.demonLevel == 7) {
				for (PotionEffect it : (Collection<PotionEffect>)player.getActivePotionEffects()) {
					ex.nextDemonLvlProgress += it.getAmplifier() * Math.max(it.getDuration() / 20 / 60, 1);
				}
			}
			if (ex.demonLevel == 8) {
				for (int i = 0; i < 9; i++) {
					ItemStack slotStack = player.inventory.getStackInSlot(i);
					if (slotStack != null && slotStack.getItem() == Items.clock) {
						ex.nextDemonLvlProgress += 20;
					}
				}
			}
			if (ex.nextDemonLvlProgress >= ex.nextDemonLevelRequres()) {
				ex.nextDemonLevel();
			}
			if (ex.demonLevel == 1) {
				player.addPotionEffect(new PotionEffect(Potion.weakness.id, 50, 20));
			}
			if (ex.demonLevel >= 2) {
				if (!player.capabilities.allowFlying) {
					player.capabilities.allowFlying = true;
				}
				if (player.isBurning()) {
					player.extinguish();
				}
				if (ex.demonLevel >= 10) {
					
				}
				if (ex.demonLevel >= 5) {
					if (player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getModifier(ID_demonknockback) == null) {
						player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).applyModifier(demonknockback);
					}
					
				}
				else {
					if (player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getModifier(ID_demonknockback) != null) {
						player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).removeModifier(demonknockback);
					}
					
				}
				
				if (player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getModifier(ID_demondamage) == null) {
					player.getEntityAttribute(SharedMonsterAttributes.attackDamage).applyModifier(demondamage);
				}
				else
					if ((int)player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getModifier(ID_demondamage).getAmount() != (int)Math.pow(ex.demonLevel, ex.demonLevel)) {
						player.getEntityAttribute(SharedMonsterAttributes.attackDamage).removeModifier(demondamage);
						player.getEntityAttribute(SharedMonsterAttributes.attackDamage).applyModifier(demondamage);
					}
			}
			else {
				if (player.getEntityAttribute(SharedMonsterAttributes.attackDamage).getModifier(ID_demondamage) != null) {
					player.getEntityAttribute(SharedMonsterAttributes.attackDamage).removeModifier(demondamage);
					player.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).removeModifier(demonknockback);
					if (!player.capabilities.isCreativeMode) {
						player.capabilities.allowFlying = false;
						player.capabilities.isFlying = false;
					}
					
				}
			}
			
			//MagicianHat crafting
			List<EntityItem> itemsAround = player.worldObj.getEntitiesWithinAABB(EntityItem.class,
					AxisAlignedBB.getBoundingBox(player.posX - 2, player.posY - 2, player.posZ - 2, player.posX + 2, player.posY + 2, player.posZ + 2));
			if (itemsAround.size() == 3) {
				boolean f1 = false, f2 = false, f3 = false;
				for (EntityItem it : itemsAround) {
					if (it.getEntityItem().getItem() == Items.wooden_shovel) {
						f1 = true;
					}
					if (it.getEntityItem().getItem() == Items.stone_hoe) {
						f2 = true;
					}
					if (it.getEntityItem().getItem() == Items.boat) {
						f3 = true;
					}
				}
				if (f1 && f2 && f3) {
					ItemStack hatStack = new ItemStack(Init.ItemMagicianHat);
					hatStack.setTagCompound(new NBTTagCompound());
					hatStack.getTagCompound().setBoolean("active", true);
					itemsAround.get(0).setEntityItemStack(hatStack);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerUseItemEvent(PlayerUseItemEvent.Start event) {
		if (event.entityPlayer.isPotionActive(Config.potionStuckInTimeId)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.entityPlayer.isPotionActive(Config.potionStuckInTimeId)) {
			event.setCanceled(true);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void event(MouseEvent event) {
		if (Minecraft.getMinecraft().thePlayer.isPotionActive(Config.potionStuckInTimeId))
			event.setCanceled(true);
		if (KeyHandler.keyDemonAbilityMenu.getIsKeyPressed()) {
			CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(Minecraft.getMinecraft().thePlayer);
			if (ex.demonLevel < 1)
				return;
			//event.setCanceled(true);
			int x = event.x - FMLClientHandler.instance().getClient().displayWidth / 2;
			int y = -(FMLClientHandler.instance().getClient().displayHeight / 2 - event.y);
			int ang = (int)Math.toDegrees(Math.atan2(x, y));
			ang += 23;
			if (ang < 0)
				ang += 360;
			//System.out.println(x + "  " + y + "     " + ang + "   " + angsin + " " + angcos);
			RmNetworkRegistry.NETWORK.sendToServer(new PacketDemonChooseAbility(Minecraft.getMinecraft().thePlayer.getCommandSenderName(), Math.round(ang / 45)));
		}
	}
	
	// EXTENDED
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event) {
		if (event.entity instanceof EntityPlayer && event.entity.getExtendedProperties(CustomExtendedEntityProperties.ID) == null)
			CustomExtendedEntityProperties.register((EntityPlayer) event.entity);
	}
	
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.entity;
			CustomExtendedEntityProperties.sync(player);
		}
	}
	
	@SubscribeEvent
	public void onPlayerCloneEvent(PlayerEvent.Clone event) {
		NBTTagCompound tag = new NBTTagCompound();
		CustomExtendedEntityProperties oldEx = CustomExtendedEntityProperties.get(event.original);
		oldEx.saveNBTData(tag);
		CustomExtendedEntityProperties newEx = CustomExtendedEntityProperties.get(event.entityPlayer);
		newEx.loadNBTData(tag);
	} //EXTENDED FINISH
	
	//@SideOnly(Side.CLIENT)
	private static boolean disableShaders = false;
	
	//@SideOnly(Side.CLIENT)
	public static HashMap<String, Integer> dodgeAnim = new HashMap<String, Integer>();
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
		EntityPlayer player = event.entityPlayer;
		if (ShaderProgram.currentShaderID == 0 && player.getCurrentArmor(0) != null && player.getCurrentArmor(1) != null && player.getCurrentArmor(2) != null && player.getCurrentArmor(3) != null
				&& player.getCurrentArmor(3).getItem() == Init.OverlordHelmet && player.getCurrentArmor(2).getItem() == Init.OverlordChestplate
				&& player.getCurrentArmor(1).getItem() == Init.OverlordLeggins && player.getCurrentArmor(0).getItem() == Init.OverlordBoots) {
			disableShaders = true;
			ModShaders.glitch.start();
			ARBShaderObjects.glUniform1fARB(ModShaders.glitch.getUniform("randomUniform"), event.entity.worldObj.rand.nextFloat());
			if (Minecraft.getMinecraft().currentScreen != null && player.getCommandSenderName().contentEquals(Minecraft.getMinecraft().thePlayer.getCommandSenderName())) {
				ARBShaderObjects.glUniform1fARB(ModShaders.glitch.getUniform("dislocForce"), 0.01f);
			}
			else {
				if (dodgeAnim.containsKey(player.getCommandSenderName()) && dodgeAnim.get(player.getCommandSenderName()) > 0) {
					ARBShaderObjects.glUniform1fARB(ModShaders.glitch.getUniform("dislocForce"), 2.5f);
				}
				else {
					ARBShaderObjects.glUniform1fARB(ModShaders.glitch.getUniform("dislocForce"), 0.2f);
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderPlayerSpecialsPre(RenderPlayerEvent.Specials.Pre event) {
		if (disableShaders) {
			ModShaders.glitch.stop();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRenderLivingEventPre(RenderLivingEvent.Pre event) {
		CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(Minecraft.getMinecraft().thePlayer);
		if (ex.madnessLvl >= 1000) {
			GL11.glRotatef(90, 1, 1, 1);
			//event.setCanceled(true);
		}
	}
	
	private static int rot = 1;
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onEntityViewRenderEventCameraSetup(EntityViewRenderEvent.RenderFogEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.entity;
			CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(player);
			if (ex.madnessLvl >= 1000) {
				
				GL11.glRotatef(rot, 1, 1, 0);
				if ((int)(Math.random() * 100) >= 99) {
					GL11.glTranslated(Math.random(), Math.random(), Math.random());
				}
				if (player.ticksExisted % rot < 10)
					rot++;
			}
		}
	}
	
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) && !event.isCanceled()) {
			if (event.entityPlayer.getCurrentEquippedItem() == null) {
				CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(event.entityPlayer);
				if (ex.demonLevel > ex.demonability + 2 && ex.usesdemonability) {
					event.setCanceled(true);
					EntityPlayer player = event.entityPlayer;
					player.swingItem();
					if (event.world.isRemote) {
						CustomExtendedEntityProperties.get(player).useDemonAbility();
						RmNetworkRegistry.NETWORK.sendToServer(new PacketUseDemonAbility(event.entityPlayer.getCommandSenderName()));
					}
				}
			}
		}
	}
}
