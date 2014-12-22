package deadlymobs;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

/**
 * @author Clinton Alexander
 */
@Mod(modid = "diggingmobs", name = "More Deadly Mobs", version = "beta")
public final class DiggingMobs {
    private static String[] diggers;
    private static String[] sprinters;
    public static int sprintMultiplier;
    private final// Items zombies can destroy
            Block[] canDestroy = {Blocks.grass, Blocks.dirt, Blocks.sapling, Blocks.sand, Blocks.gravel, Blocks.log, Blocks.leaves, Blocks.sponge, Blocks.glass, Blocks.wool,
            Blocks.yellow_flower, Blocks.red_flower, Blocks.brown_mushroom, Blocks.red_mushroom, Blocks.tnt, Blocks.torch, Blocks.chest, Blocks.redstone_wire, Blocks.wheat, Blocks.farmland,
            Blocks.wooden_door, Blocks.lever, Blocks.stone_pressure_plate, Blocks.iron_door, Blocks.wooden_pressure_plate, Blocks.unlit_redstone_torch, Blocks.redstone_torch, Blocks.stone_button,
            Blocks.snow_layer, Blocks.ice, Blocks.snow, Blocks.cactus, Blocks.clay, Blocks.reeds, Blocks.fence, Blocks.pumpkin, Blocks.netherrack, Blocks.soul_sand, Blocks.glowstone, Blocks.lit_pumpkin, Blocks.cake};

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        diggers = config.getStringList("Diggers", "General", new String[]{"Zombie", "Creeper", "Spider"}, "Use mob names to define which can dig");
        sprinters = config.getStringList("Sprinters", "General", new String[]{"Zombie", "Creeper", "Spider"}, "Use mob names to define which can sprint");
        sprintMultiplier = config.getInt("Sprint speed multiplier", "general", 2, 0, 10, "Higher for more sprint speed.");
        if (config.hasChanged()) {
            config.save();
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityMob) {
            EntityAIDig taskDig;
            if (event.entity instanceof EntityCreeper) {
                taskDig = new EntityCreeperAI((EntityCreeper) event.entity);
            } else {
                taskDig = new EntityAIDig((EntityMob) event.entity).addToBlackList(Blocks.golden_rail, Blocks.detector_rail, Blocks.jukebox, Blocks.glowstone, Blocks.stained_glass);
                if (event.entity instanceof EntityZombie) {
                    taskDig.addToWhiteList(canDestroy);
                }
            }
            ((EntityMob) event.entity).tasks.addTask(3, taskDig);
        }
    }

    public static boolean canDig(String mobType) {
        for (String name : diggers) {
            if (mobType.equals(name))
                return true;
        }
        return false;
    }

    public static boolean canSprint(String mobType) {
        for (String name : sprinters) {
            if (mobType.equals(name))
                return true;
        }
        return false;
    }
}