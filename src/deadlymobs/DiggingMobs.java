package deadlymobs;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Clinton Alexander
 */
@Mod(modid = "deadlymobs", name = "More Deadly Mobs", version = "${version}")
public final class DiggingMobs {
    private static final UUID sprintingSpeedBoostModifierUUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");//From EntityLivingBase
    private static AttributeModifier sprintingSpeedBoostModifier;
    private static String[] diggers;
    private static String[] sprinters;
    private static final HashMap<String, ArrayList<Block>> canDestroy = new HashMap<String, ArrayList<Block>>();

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        diggers = config.getStringList("Diggers", "General", new String[]{"Zombie", "Creeper", "Spider"}, "Use mob names to define which can dig");
        String[] global = config.getStringList("Global", "Blacklist", new String[]{"golden_rail", "detector_rail", "jukebox", "glowstone", "stained_glass"}, "Blocks that will never be dug.");
        for (String name : global) {
            Block temp = GameData.getBlockRegistry().getObject(name);
            if (temp != Blocks.air) {
                EntityAIDig.addToGlobalBlackList(temp);
            }
        }
        global = config.getStringList("Global", "Whitelist", new String[0], "Blocks that can be dug at any time.");
        for (String name : global) {
            Block temp = GameData.getBlockRegistry().getObject(name);
            if (temp != Blocks.air) {
                EntityAIDig.addToGlobalWhiteList(temp);
            }
        }
        for (String text : diggers) {
            String[] defaults = !text.equals("Zombie") ? new String[0] : new String[]{"grass", "dirt", "sapling", "sand",
                    "gravel", "log", "leaves", "sponge", "glass", "wool", "yellow_flower", "red_flower", "brown_mushroom",
                    "red_mushroom", "tnt", "torch", "chest", "redstone_wire", "wheat", "farmland", "wooden_door", "lever",
                    "stone_pressure_plate", "iron_door", "wooden_pressure_plate", "unlit_redstone_torch", "redstone_torch",
                    "stone_button", "snow_layer", "ice", "snow", "cactus", "clay", "reeds", "fence", "pumpkin", "netherrack",
                    "soul_sand", "glowstone", "lit_pumpkin", "cake"};
            global = config.getStringList(text, "Whitelist", defaults, "Blocks that can be dug by this entity at any time.");
            ArrayList<Block> whites = new ArrayList<Block>();
            for (String name : global) {
                Block temp = GameData.getBlockRegistry().getObject(name);
                if (temp != Blocks.air) {
                    whites.add(temp);
                }
            }
            canDestroy.put(text, whites);
        }
        sprinters = config.getStringList("Sprinters", "General", new String[]{"Zombie", "Creeper", "Spider"}, "Use mob names to define which can sprint");
        float sprintMultiplier = config.getFloat("Sprint speed multiplier", "General", 2.0F, 0.0F, 10.0F, "Higher for more sprint speed.");
        sprintingSpeedBoostModifier = new AttributeModifier(sprintingSpeedBoostModifierUUID, "Sprinting speed boost", sprintMultiplier - 1, 2).setSaved(false);
        if (config.hasChanged()) {
            config.save();
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityCreature && canDig(event.entity)) {
            EntityAIDig taskDig;
            if (event.entity instanceof EntityCreeper) {
                taskDig = new EntityCreeperAI((EntityCreeper) event.entity);
            } else {
                taskDig = new EntityAIDig((EntityCreature) event.entity);
                for (Block temp : canDestroy.get(EntityList.getEntityString(event.entity))) {
                    if (temp != null) {
                        taskDig.addToWhiteList(temp);
                    }
                }
            }
            ((EntityCreature) event.entity).tasks.addTask(3, taskDig);
        }
    }

    @SubscribeEvent
    public void onMobLiving(LivingEvent.LivingUpdateEvent event) {
        if (event.entity instanceof EntityCreature && canSprint(event.entity)) {
            IAttributeInstance iattributeinstance = ((EntityCreature) event.entity).getEntityAttribute(SharedMonsterAttributes.movementSpeed);
            if (((EntityCreature) event.entity).hasPath() && ((EntityCreature)event.entity).getAttackTarget()!=null) {
                if (!event.entity.isSprinting()) {
                    if (iattributeinstance.getModifier(sprintingSpeedBoostModifierUUID) != null) {
                        iattributeinstance.removeModifier(sprintingSpeedBoostModifier);
                    }
                    iattributeinstance.applyModifier(sprintingSpeedBoostModifier);
                    event.entity.setSprinting(true);
                }

            } else if (iattributeinstance.getModifier(sprintingSpeedBoostModifierUUID) != null) {
                iattributeinstance.removeModifier(sprintingSpeedBoostModifier);
                if (event.entity.isSprinting()) {
                    event.entity.setSprinting(false);
                }
            }
        }
    }

    public static boolean canDig(Entity entity) {
        return canDig(EntityList.getEntityString(entity));
    }

    private static boolean canDig(String mobType) {
        for (String name : diggers) {
            if (mobType.equals(name))
                return true;
        }
        return false;
    }

    public static boolean canSprint(Entity entity) {
        return canSprint(EntityList.getEntityString(entity));
    }

    private static boolean canSprint(String mobType) {
        for (String name : sprinters) {
            if (mobType.equals(name))
                return true;
        }
        return false;
    }
}