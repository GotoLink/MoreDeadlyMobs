package deadlymobs;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAccess;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySpider;

public class EntityCreeperAI extends EntityAIDig {
    private static final String[] fields = {"timeSinceIgnited", "field_70833_d"};
    private final EntityCreeper creeper;

    public EntityCreeperAI(EntityCreeper creeper) {
        super(creeper);
        this.creeper = creeper;
    }

    @Override
    public void updateTask() {
        super.updateTask();
        EntityCreature closest = getClosestCreature();
        if (!mob.isRiding() && closest instanceof EntitySpider && closest.riddenByEntity == null && !mob.hasPath()) {
            if (mob.getDistanceToEntity(closest) < 3F) {
                mob.mountEntity(closest);
            }
        }
    }

    /**
     * Destroy a given block
     *
     * @return True if this block exists and was attempted to dig
     */
    @Override
    protected boolean attemptDig(int i, int j, int k) {
        Block id = mob.worldObj.getBlock(i, j, k);
        if (id.getMaterial() != Material.air) {
            digThroughBlock(id, i, j, k);
            return true;
        } else {
            return false;
        }
    }

    /**
     * The action an entity does to dig through a block
     *
     * @param id type of block to dig
     */
    @Override
    protected boolean digThroughBlock(Block id, int i, int j, int k) {
        int timer = ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, fields);
        // Make the creeper explode at the player
        if (timer == 0) {
            mob.worldObj.playSoundAtEntity(mob, "random.fuse", 1.0F, 0.5F);
        }
        creeper.setCreeperState(1);
        timer++;
        ObfuscationReflectionHelper.setPrivateValue(EntityCreeper.class, creeper, timer, fields);
        CreatureAccess.setHasAttacked(mob);
        if (timer >= 30) {
            mob.worldObj.createExplosion(mob, i, j, k, 3F, true);
            mob.setDead();
        }
        return true;
    }
}
