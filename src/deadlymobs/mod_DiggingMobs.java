package deadlymobs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.monster.EntityMob;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * @author Clinton Alexander
 */
@Mod(modid = "diggingmobs", name = "More Deadly Mobs", version = "alpha")
public class mod_DiggingMobs {
	public static boolean zombiesDig;
	public static boolean creepersDig;
	public static boolean spidersDig;
	public static boolean mobsSprint;
	public static int mobSprintSpeed;//("Sprint multiplier", 2, 10);
	/**
	 * A whitelist of blocks a mob can destroy without tools
	 */
	private static Map<Integer, List<Integer>> destroyWhitelist = new HashMap();
	/**
	 * A whitelist of blocks a mob class can destroy without tools
	 */
	private static Map<String, List<Integer>> globalWhitelist = new HashMap();
	/**
	 * A blacklist of blocks a mob do not destroy
	 */
	private static Map<Integer, List<Integer>> destroyBlacklist = new HashMap();
	/**
	 * Global blacklist of blocks to not destroy
	 */
	private static Map<String, List<Integer>> globalBlacklist = new HashMap();

	@EventHandler
	public void load(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		zombiesDig = config.get("general", "Enable Zombies digging", true).getBoolean(true);
		creepersDig = config.get("general", "Enable Creepers digging", true).getBoolean(true);
		spidersDig = config.get("general", "Enable Spiders digging", true).getBoolean(true);
		mobsSprint = config.get("general", "Enable Mobs sprinting", true).getBoolean(true);
		mobSprintSpeed = config.get("general", "Sprint speed multiplier", 2).getInt();
		if (config.hasChanged()) {
			config.save();
		}
		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void onMobUpdate(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityMob) {
			if (!destroyBlacklist.keySet().contains(event.entityLiving.entityId)) {
				destroyBlacklist.put(event.entityLiving.entityId, addToList(new LinkedList<Integer>(), 27, 28, 84, 89, 95));
			}
		}
	}

	private static List addToList(List list, int... data) {
		for (int d : data) {
			list.add(d);
		}
		return list;
	}
}