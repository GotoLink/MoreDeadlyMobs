package net.minecraft.src;

import net.minecraft.src.modoptionsapi.*;
import net.minecraft.client.Minecraft;

/**
* A mod which using the Digging Mobs AI API to make the default MC mobs dig
* 
* @author	Clinton Alexander
* @version	0.3.1
*/
public class mod_DiggingMobs extends BaseMod {
	public static final ModBooleanOption zombiesDig  = new ModBooleanOption("Zombies Dig");
	public static final ModBooleanOption creepersDig = new ModBooleanOption("Creepers Dig");
	public static final ModBooleanOption spidersDig = new ModBooleanOption("Spiders Dig");
	
	/**
	* Status of the renderer
	*/
	private static boolean setRenderer = false;
	
	/**
	* The renderer for this mob
	*/
	public static RenderDeadlierMobs renderer;
	
	public mod_DiggingMobs() {
		ModOptions mod = new ModOptions("More Deadly Mobs");
		
		mod.addOption(creepersDig);
		mod.addOption(spidersDig);
		mod.addOption(zombiesDig);
		
		ModOptionsAPI.addMod(mod);
		
	}
	
	/**
	* Set the renderer for the mod
	*/
	public static void setRenderer() {
		if(!setRenderer) {
			Minecraft mc = ModLoader.getMinecraftInstance();
			renderer = new RenderDeadlierMobs(mc);
			renderer.func_946_a(mc.theWorld);
			ModLoader.getMinecraftInstance().renderGlobal = renderer;
			
			setRenderer = true;
		}
	}
	
	public String Version() {
		return "v0.4";
	}
}