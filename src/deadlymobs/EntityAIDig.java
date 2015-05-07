package deadlymobs;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.CreatureAccess;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by GotoLink on 21/12/2014.
 */
public class EntityAIDig extends EntityAIBase {
    /**
     * Definition of soft block.
     */
    private static final float SOFT_BLOCK_UPPER = 1F;
    private static final int DIG_RANGE = 16, DIG_SFX = 2001, DIG_DELAY = 5;
    private static final Collection<Block> globalWhitelist = new ArrayList<Block>(), globalBlacklist = new ArrayList<Block>();
    protected final EntityCreature mob;
    private final Collection<Block> canDigUnequipped;
    private final Collection<Block> cantDig;
    /**
     * Sound timer
     */
    private int soundTimer;
    /**
     * If we have dug yet
     */
    private boolean dug;
    /**
     * Tick delay for digging
     */
    private int digDelay = DIG_DELAY;
    /**
     * Last coordinates dug
     */
    private BlockPos lastBlock = BlockPos.ORIGIN;
    /**
     * Current block damage for a block
     */
    private float curBlockDamage;
    // Positions
    private int minI;
    private int maxI;
    private int maxJ;
    private int minK;
    private int maxK;

    public EntityAIDig(EntityCreature entityMob) {
        mob = entityMob;
        canDigUnequipped = new ArrayList<Block>();
        cantDig = new ArrayList<Block>();
    }

    public static void addToGlobalWhiteList(Block... blocks) {
        Collections.addAll(globalWhitelist, blocks);
    }

    public static void addToGlobalBlackList(Block... blocks) {
        Collections.addAll(globalBlacklist, blocks);
    }

    public EntityAIDig addToWhiteList(Block... blocks) {
        Collections.addAll(canDigUnequipped, blocks);
        return this;
    }

    /**
     * Check if a block is in the whitelist
     *
     * @return True if it's in a whitelist
     */
    public boolean inWhitelist(Block id) {
        return canDigUnequipped.contains(id) || globalWhitelist.contains(id);
    }

    public EntityAIDig addToBlackList(Block... blocks) {
        Collections.addAll(cantDig, blocks);
        return this;
    }

    /**
     * Check if a block is in one of the two blacklists or not
     *
     * @return True if the block is in one of the two blacklists
     */
    public boolean inBlacklist(Block id) {
        return cantDig.contains(id) || globalBlacklist.contains(id);
    }

    @Override
    public void resetTask() {
        mob.worldObj.sendBlockBreakProgress(mob.getEntityId(), lastBlock, -1);
        curBlockDamage = 0.0F;
        soundTimer = 0;
    }

    @Override
    public boolean shouldExecute() {
        return mob.isEntityAlive() && mob.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
    }

    @Override
    public void updateTask() {
        EntityLivingBase target;
        if (mob.getNavigator().getPath() != null) {
            target = mob.getAttackTarget();
        } else {
            target = CreatureAccess.findPlayerToAttack(mob);
            if (target != null) {
                mob.setAttackTarget(target);
                mob.getNavigator().tryMoveToEntityLiving(target, 1.0D);
            }
        }
        // More deadly mobs doesn't accept the fact that just because
        // the path isn't clear that the mob must stop searching.
        // Instead, if it's close enough (16 blocks) it'll keep digging towards its target
        if (target != null && mob.getDistanceToEntity(target) < DIG_RANGE) {
            digToEntity(target);
            if (!dug && mob.isCollidedHorizontally && isHeadRoom()) {
                // Make the mob jump onto the ledge
                mob.setJumping(true);
            }
        } else  if (curBlockDamage > 0.0F) {
            // Reset to all previous values as this is a new block to dig
            resetTask();
        }
    }

    /**
     * Determine how to dig to the entity given. This algorithm will pick the
     * quickest direct route to the entity with no work-around routes
     *
     * @param digToEntity Entity to dig to
     */
    protected void digToEntity(Entity digToEntity) {
        boolean headRoom = isHeadRoom();
        // Radians for the X/Z plane bounded by 0 to 2PI
        float radsXZ = mod((float) ((mob.rotationYaw * Math.PI) / 180F), (float) (2 * Math.PI));
        // Current Positions
        int curJ = MathHelper.floor_double(mob.posY);
        // The entities positions
        int entJ = MathHelper.floor_double(digToEntity.posY);
        minI = MathHelper.floor_double(mob.getEntityBoundingBox().minX);
        maxI = MathHelper.floor_double(mob.getEntityBoundingBox().maxX);
        int minJ = MathHelper.floor_double(mob.getEntityBoundingBox().minY);
        maxJ = MathHelper.floor_double(mob.getEntityBoundingBox().maxY);
        minK = MathHelper.floor_double(mob.getEntityBoundingBox().minZ);
        maxK = MathHelper.floor_double(mob.getEntityBoundingBox().maxZ);
        // Current rounded bounding boxes
        int curMinI = minI;
        int curMaxI = maxI;
        int curMinK = minK;
        int curMaxK = maxK;
        // Facing -X
        if (radsXZ >= Math.PI / 4 && radsXZ < (3 * Math.PI) / 4) {
            // Since we're facing the minimum side, we dig just outside our own minI
            minI = minI - 1;
            maxI = minI;
            // Facing -Z
        } else if (radsXZ >= (3 * Math.PI) / 4 && radsXZ < (5 * Math.PI) / 4) {
            // Since we're facing the minimum side, we dig just outside our own minI
            minK = minK - 1;
            maxK = minK;
            // Facing +X
        } else if (radsXZ >= (5 * Math.PI) / 4 && radsXZ < (7 * Math.PI) / 4) {
            // Since we're facing the minimum side, we dig just outside our own minI
            minI = maxI;
            maxI = minI;
            // Facing +Z
        } else {
            // Since we're facing the minimum side, we dig just outside our own minI
            minK = maxK;
            maxK = minK;
        }
        // A dug to say if we have successfully dug this turn
        dug = false;
        // Create an opening diagonally above enough to jump to
        // the next ledge
        if (entJ > curJ) {
            // Dig all spots above self first
            dug = digBox(curMinI, curMaxI, maxJ + 1, maxJ + 1, curMinK, curMaxK);
            // Dig a clearing in front of self to jump onto
            dug = digBox(minI, maxI, minJ + 1, maxJ + 1, minK, maxK);
            // Dig down, diagonal down and in front
        } else if (entJ < curJ) {
            if (mob.isCollidedHorizontally) {
                // Dig all spots above self first
                dug = digBox(curMinI, curMaxI, minJ - 1, minJ - 1, curMinK, curMaxK);
            }
            // Dig a clearing in front of self to jump onto
            dug = digBox(minI, maxI, minJ - 1, maxJ - 1, minK, maxK);
            // In this case, there may be an obstacle, like stone, at the feet of the mob
            // try to make a clearing directly in front instead
            dug = digBox(minI, maxI, maxJ + 1, maxJ + 1, minK, maxK);
            // Dig in front
        } else {
            // Dig all blocks above the mob first
            // And leave ground level as a special case
            // Dig a clearing in front of self to jump onto
            dug = digBox(minI, maxI, minJ + 1, maxJ, minK, maxK);
            // Try ground level, if there is no headroom, keep digging forwards
            if (!dug && !headRoom) {
                dug = digBox(minI, maxI, minJ, minJ, minK, maxK);
            }
            // This means we're stuck and need to dig up and jump
            if (!dug && mob.isCollidedHorizontally && !headRoom) {
                // Dig space above self
                dug = digBox(curMinI, curMaxI, maxJ + 1, maxJ + 1, curMinK, curMaxK);
                // Dig headroom
                dug = digBox(minI, maxI, maxJ + 1, maxJ + 1, minK, maxK);
            }
        }
        if (!dug) {
            // Reset to all previous values as this is a new block to dig
            resetTask();
        }
    }

    /**
     * Dig an area of dirt
     *
     * @return True if a block was dug
     */
    private boolean digBox(int minI, int maxI, int minJ, int maxJ, int minK, int maxK) {
        boolean dug = this.dug;
        // Dig a clearing in front of self to jump onto
        for (int digI = minI; !dug && digI <= maxI; digI++) {
            for (int digK = minK; !dug && digK <= maxK; digK++) {
                for (int digJ = maxJ; !dug && digJ >= minJ; digJ--) {
                    dug = attemptDig(digI, digJ, digK);
                }
            }
        }
        return dug;
    }

    /**
     * Destroy a given block
     *
     * @return True if this block exists and was attempted to dig
     */
    protected boolean attemptDig(int i, int j, int k) {
        BlockPos pos = new BlockPos(i, j, k);
        IBlockState state = mob.worldObj.getBlockState(pos);
        // Remove the top block
        if (state.getBlock().getMaterial() != Material.air) {
            // If it's a soft block, must be carrying nothing or a relevant tool
            // Random chance to destroy
            if (inWhitelist(state.getBlock()) || canBreakBlock(state, pos) && !inBlacklist(state.getBlock())) {
                digThroughBlock(state, pos);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * The action an entity does to dig through a block
     *
     * @param state type of block being destroyed
     * @return Success of digging
     */
    protected boolean digThroughBlock(IBlockState state, BlockPos pos) {
        boolean flag = false;
        if (!partialDig(state.getBlock(), pos)) {
            flag = mob.worldObj.setBlockToAir(pos);
            if (flag) {
                // Play breaking sound
                mob.worldObj.playAuxSFX(DIG_SFX, pos, Block.getIdFromBlock(state.getBlock()));
                state.getBlock().dropBlockAsItem(mob.worldObj, pos, state, 0);
            }
        }
        return flag;
    }

    /**
     * Check if a dig is a partial dig or not
     *
     * @param block Block attempting to dig
     */
    protected boolean partialDig(Block block, BlockPos pos) {
        // Whether we have fully dug the block or not
        boolean dug = false;
        // Check if we should delay the dig
        if (digDelay > 0) {
            digDelay--;
            // Check if we're still digging the previous block
        } else if (pos.equals(lastBlock)) {
            curBlockDamage += getBlockDamage(block, pos);
            // Play the sound
            if (soundTimer % 4 == 0) {
                mob.worldObj.playAuxSFX(DIG_SFX, pos, Block.getIdFromBlock(block));
            }
            soundTimer++;
            // Set that we have removed the block and reset data values
            if (curBlockDamage >= 1.0F) {
                dug = true;
                digDelay = DIG_DELAY;
                resetTask();
            } else {
                mob.worldObj.sendBlockBreakProgress(mob.getEntityId(), pos, MathHelper.floor_float(curBlockDamage));
            }
        } else {
            // Reset to all previous values as this is a new block to dig
            resetTask();
            lastBlock = pos;
        }
        return !dug;
    }

    /**
     * Check if the mob can dig through the given blockID
     *
     * @return True if can dig through
     */
    private boolean canBreakBlock(IBlockState id, BlockPos pos) {
        return (mob.getHeldItem() == null && isSoftBlock(id.getBlock(), mob.worldObj, pos)) || holdingEffectiveTool(id);
    }

    /**
     * Check if the tool the mob is holding is effective against the block type
     *
     * @param state  the block state
     * @return True if effective
     */
    private boolean holdingEffectiveTool(IBlockState state) {
        ItemStack itemstack = mob.getHeldItem();
        return state.getBlock().getMaterial() != Material.air && itemstack != null && itemstack.getItem().getDigSpeed(itemstack, state) > 1.0F;
    }

    /**
     * Check if there is headroom
     *
     * @return True if there is head room
     */
    private boolean isHeadRoom() {
        for (int digI = minI; !dug && digI <= maxI; digI++) {
            for (int digK = minK; !dug && digK <= maxK; digK++) {
                if (!mob.worldObj.isAirBlock(new BlockPos(digI, maxJ + 1, digK))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Find the closest creature to this creature
     *
     * @return Closest creature
     */
    public final EntityCreature getClosestCreature() {
        double low = -1D;
        EntityCreature found = null;
        for (Object entity : mob.worldObj.loadedEntityList) {
            if (entity instanceof EntityCreature) {
                EntityCreature creature = (EntityCreature) entity;
                double dist = mob.getDistanceToEntity(creature);
                if (dist != 0 && (dist < low || low == -1D)) {
                    low = dist;
                    found = creature;
                }
            }
        }
        return found;
    }

    /**
     * Get the block damage of this mob against a given block
     *
     * @param block Block being attacked
     * @return Damage counter
     */
    public float getBlockDamage(Block block, BlockPos pos) {
        float blockHardness = block.getBlockHardness(mob.worldObj, pos);
        float f = (1F / blockHardness) / 100F;
        // A little bit of a hack to avoid editing entity mob
        if (mob instanceof EntityMob) {
            float attackStrength = (float) mob.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
            f = (attackStrength / blockHardness) / 100F;
            if (mob.isInWater()) {
                f /= 5F;
            }
            if (!mob.onGround) {
                f /= 5F;
            }
        }
        return f;
    }

    /**
     * Check if a block is soft or not
     *
     * @return True if the block is considered soft
     */
    public static boolean isSoftBlock(Block block, World world, BlockPos pos) {
        return block.getBlockHardness(world, pos) < SOFT_BLOCK_UPPER;
    }

    /**
     * A hacked in "modulo" command
     *
     * @return x mod y
     */
    public static float mod(float x, float y) {
        float result = x % y;
        return result < 0 ? result + y : result;
    }
}
