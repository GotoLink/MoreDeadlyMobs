package deadlymobs;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.BlockPos;

import java.lang.reflect.Field;

public class EntityCreeperAI extends EntityAIDig {
    private static final Field timeSinceIgnited = EntityCreeper.class.getDeclaredFields()[1];
    static {
        timeSinceIgnited.setAccessible(true);
    }
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
        BlockPos pos = new BlockPos(i, j, k);
        IBlockState id = mob.worldObj.getBlockState(pos);
        if (id.getBlock().getMaterial() != Material.air) {
            digThroughBlock(id, pos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * The action an entity does to dig through a block
     *
     * @param state type of block to dig
     */
    @Override
    protected boolean digThroughBlock(IBlockState state, BlockPos pos) {
        try {
            int timer = (Integer)timeSinceIgnited.get(creeper);
            // Make the creeper explode at the player
            if (timer == 0) {
                mob.playSound("creeper.primed", 1.0F, 0.5F);
            }
            creeper.setCreeperState(1);
            timer++;
            timeSinceIgnited.set(creeper, timer);
            if (timer >= 30) {
                mob.worldObj.createExplosion(mob, pos.getX(), pos.getY(), pos.getZ(), 3F, true);
                mob.setDead();
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
