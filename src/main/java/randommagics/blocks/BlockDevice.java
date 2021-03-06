package randommagics.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import randommagics.Main;
import randommagics.gui.GUIHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import randommagics.Init;
import thaumcraft.common.lib.utils.InventoryUtils;

public class BlockDevice extends Block implements ITileEntityProvider {

	public BlockDevice() {
		super(Material.rock);
		setHardness(2.5F);
		setBlockName("Device");
		setCreativeTab(Init.TabRandomMagics);
		setResistance(25F);
        setStepSound(Block.soundTypeStone);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        setBlockTextureName("randommagics:Device");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		if (metadata == 0)
			return new TileInfusionMatrixExtended();
		if (metadata == 1)
			return new TileVisCell();
		if (metadata == 2)
			return new TileInventoryAccess();
		if (metadata == 3)
			return new TileAlchemyPlant();
		if (metadata == 4)
			return new TileTimeExpander();
		if (metadata == 5)
			return new TileAuraInfusionMatrix();
		if (metadata == 6)
			return new TileReactor();
		return null;
	}
	
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
        par3List.add(new ItemStack(par1, 1, 3));
        par3List.add(new ItemStack(par1, 1, 4));
        par3List.add(new ItemStack(par1, 1, 5));
        par3List.add(new ItemStack(par1, 1, 6));
    }
	
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta == 0) {
			InventoryUtils.dropItems(world, x, y, z);
        	TileEntity tileEntity = world.getTileEntity(x, y, z);
        	if(tileEntity != null && (tileEntity instanceof TileInfusionMatrixExtended) && ((TileInfusionMatrixExtended)tileEntity).crafting)
        		world.createExplosion(null, (double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, 20.0F, true);
		}
		super.breakBlock(world, x, y, z, par5, par6);
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		return willHarvest || super.removedByPlayer(world, player, x, y, z, willHarvest);
	}
	
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		int meta = world.getBlockMetadata(x, y, z);
        ItemStack ret = new ItemStack(this, 1, meta);
        if (meta == 0) {
        	ret.setTagCompound(new NBTTagCompound());
        	ret.getTagCompound().setInteger("speedMod", ((TileInfusionMatrixExtended)world.getTileEntity(x, y, z)).speedMod);
        }
        return ret;
    }
	
	@Override
	public void harvestBlock(World world, EntityPlayer e, int x, int y,
			int z, int meta) {
		super.harvestBlock(world, e, x, y, z, meta);
		world.setBlockToAir(x, y, z);
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		if (metadata == 0) {
			ItemStack ret = new ItemStack(this, 1, metadata);
			ret.setTagCompound(new NBTTagCompound());
			TileInfusionMatrixExtended tile = (TileInfusionMatrixExtended)world.getTileEntity(x, y, z);
			if (tile != null)
				ret.getTagCompound().setInteger("speedMod", tile.speedMod);
			ArrayList<ItemStack> rett = new ArrayList<ItemStack>();
			rett.add(ret);
			return rett;
		}
		return super.getDrops(world, x, y, z, metadata, fortune);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te != null) {
			if (te instanceof TileInventoryAccess) {
				TileInventoryAccess tile = (TileInventoryAccess)te;
				if (player.isSneaking() && tile.player == null) {
					tile.player = player;
					tile.playerID = player.getUniqueID();
					tile.slotid = 0;
					return true;
				}
				if (!world.isRemote && player == tile.player) {
					player.openGui(Main.instance, 2, world, x, y, z);
					return true;
				}
			}
		}
		return false;
	}
	
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (stack.getItemDamage() == 3) {
			int l = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			byte b0 = 2;
			if (l == 0)
	        {
	            b0 = 3;
	        }

	        if (l == 1)
	        {
	            b0 = 2;
	        }

	        if (l == 2)
	        {
	            b0 = 5;
	        }

	        if (l == 3)
	        {
	            b0 = 4;
	        }
	        ((TileAlchemyPlant)world.getTileEntity(x, y, z)).side = b0;
		}
		if (stack.getItemDamage() == 0) {
			if (stack.hasTagCompound() && stack.getTagCompound().getInteger("speedMod") > 0) {
				TileInfusionMatrixExtended te = (TileInfusionMatrixExtended)world.getTileEntity(x, y, z);
				te.speedMod = stack.getTagCompound().getInteger("speedMod");
				if (entity instanceof EntityPlayer) {
					te.playerPlacedName = ((EntityPlayer)entity).getCommandSenderName(); 
				}
				else {
					te.playerPlacedName = "";
				}
			}
		}
		if (stack.getItemDamage() == 5) {
			TileAuraInfusionMatrix te = (TileAuraInfusionMatrix)world.getTileEntity(x, y, z);
			if (entity instanceof EntityPlayer) {
				te.playerPlacedName = ((EntityPlayer)entity).getCommandSenderName(); 
			}
			else {
				te.playerPlacedName = "";
			}
		}
	}
	
	
	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		if (world.getTileEntity(x, y, z) instanceof TileInfusionMatrixExtended) {
			TileInfusionMatrixExtended tile = (TileInfusionMatrixExtended)world.getTileEntity(x, y, z);
			return tile.speedMod >= 10;
		}
		if (world.getTileEntity(x, y, z) instanceof TileAuraInfusionMatrix) {
			return true;
		}
		return false;
	}
	
	/**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */
	@Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileInfusionMatrixExtended) {
        	((TileInfusionMatrixExtended)tile).onRedstoneSignal(world, x, y, z, world.isBlockIndirectlyGettingPowered(x, y, z));
        }
        if (tile instanceof TileAuraInfusionMatrix) {
        	((TileAuraInfusionMatrix)tile).onRedstoneSignal(world, x, y, z, world.isBlockIndirectlyGettingPowered(x, y, z));
        }
    }
	
	public int damageDropped(int meta)
    {
        return meta;
    }
	
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		switch(meta) {
		case 0: return 10;
		}
		return 0;
	}
	
	public int getRenderType()
    {
        return Init.RenderID;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

    public boolean renderAsNormalBlock()
    {
        return false;
    }
}
