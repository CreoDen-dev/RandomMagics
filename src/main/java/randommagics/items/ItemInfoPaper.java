package randommagics.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import randommagics.Init;
import randommagics.curses.Curse;
import randommagics.curses.CurseRegistry;
import randommagics.customs.CustomExtendedEntityProperties;
import randommagics.customs.RandomUtils;

public class ItemInfoPaper extends Item {
	
	public ItemInfoPaper() {
		super();
		this.setTextureName("randommagics:InfoPaper");
		this.setCreativeTab(Init.TabRandomMagics);
		this.setUnlocalizedName("ItemInfoPaper");
		this.setMaxStackSize(1);
	}
	
	public static void addInfo(ItemStack stack, String... data) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		int count = stack.getTagCompound().getInteger("i");
		for (int i = 0; i < data.length; i++) {
			stack.getTagCompound().setString("" + count++, data[i]);
		}
		stack.getTagCompound().setInteger("i", count);
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		stack.setTagCompound(null);
		CustomExtendedEntityProperties ex = CustomExtendedEntityProperties.get(player);
		if (ex.madnessLvl > 0) {
			addInfo(stack, "Madness level: " + ex.madnessLvl);
		}
		addInfo(stack, "Demon level: " + ex.demonLevel + "; progress: " + ex.nextDemonLvlProgress + "/" + ex.nextDemonLevelRequres());
		for (Curse c : ex.curses) {
			addInfo(stack, RandomUtils.translateCurse(c.getUniqueID()) + " " + (c.isStackable() ? c.Lvl() : ""));
		}
		player.swingItem();
		return stack;
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List l, boolean p) {
		if (stack.hasTagCompound()) {
			int count = stack.getTagCompound().getInteger("i");
			for (int i = 0; i < count; i++) {
				l.add(stack.getTagCompound().getString("" + i));
			}
		}
	}
}
