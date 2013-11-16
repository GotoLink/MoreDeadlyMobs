package deadlymobs;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.src.ModLoader;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityCreature extends EntityLiving {
	//===================
	// START MORE DEADLY MOBS
	//===================
	/**
	 * True if this mob can dig
	 */
	private boolean canDig = true;
	/**
	 * True if this mob class can dig
	 */
	private static boolean mobCanDig = true;
	/**
	 * True if this mob can sprint
	 */
	private static boolean mobCanSprint = true;
	/**
	 * Max sprint counter
	 */
	private static int SPRINT_MAX = 50;
	/**
	 * Sprint counter
	 */
	private double sprintCounter = SPRINT_MAX;
	/**
	 * True when mob is sprinting
	 */
	private boolean isSprinting = false;
	/**
	 * Definition of soft block.
	 */
	public static final float SOFT_BLOCK_UPPER = 1F;
	// Last coordinates dug
	private int lastBlockX = 0;
	private int lastBlockY = 0;
	private int lastBlockZ = 0;
	/**
	 * A sound timer
	 */
	private int soundTimer = 0;
	/**
	 * A 5 tick delay for digging
	 */
	private int digDelay = 5;
	/**
	 * Current block damage for a block
	 */
	private float curBlockDamage = 0.0F;
	// Positions
	private int minI;
	private int maxI;
	private int minJ;
	private int maxJ;
	private int minK;
	private int maxK;
	// Flag to check if we have dug yet
	private boolean dug = false;

	//===================
	// END MORE DEADLY MOBS
	//===================
	public EntityCreature(World world) {
		super(world);
	}

	//===================
	// START MORE DEADLY MOBS
	//===================
	/**
	 * Check if this mob can dig
	 * 
	 * @return True if can dig
	 */
	public boolean canDig() {
		return canDig;
	}

	/**
	 * Get the block damage of this mob against a given block
	 * 
	 * @param block
	 *            Block being attacked
	 * @return Damage counter
	 */
	public float getBlockDamage(Block block) {
		float blockHardness = block.blockHardness;
		float f = (1F / blockHardness) / 100F;
		// A little bit of a hack to avoid editing
		// entity mob
		if (this instanceof EntityMob) {
			f = (((EntityMob) this).attackStrength / blockHardness) / 100F;
			if (inWater) {
				f /= 5F;
			}
			if (!onGround) {
				f /= 5F;
			}
		}
		return f;
	}

	/**
	 * Find the closest creature to this creature
	 * 
	 * @return Closest creature
	 */
	public EntityCreature getClosestCreature() {
		double low = -1D;
		EntityCreature found = null;
		for (Object entity : worldObj.loadedEntityList) {
			if (entity instanceof EntityCreature) {
				EntityCreature creature = (EntityCreature) entity;
				double dist = this.getDistanceToEntity(creature);
				if ((dist != 0) && ((dist < low) || (low == -1D))) {
					low = dist;
					found = creature;
				}
			}
		}
		return found;
	}

	/**
	 * Get this mob's current move speed taking into account sprinting
	 * 
	 * @return float Current move speed
	 */
	public float getCurrentMoveSpeed() {
		if (mobCanSprint() && sprintingEnabled && isSprinting) {
			return (float) (moveSpeed * sprintSpeed);
		} else {
			return moveSpeed;
		}
	}

	/**
	 * Check if a block is in one of the two blacklists or not
	 * 
	 * @param id
	 * @return True if the block is in one of the two blacklists
	 */
	public boolean inBlacklist(int id) {
		for (int disallowBlock : destroyBlacklist) {
			if (id == disallowBlock) {
				return true;
			}
		}
		for (int disallowBlock : globalBlacklist) {
			if (id == disallowBlock) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a block is in the whitelist
	 * 
	 * @param id
	 * @return True if it's in a whitelist
	 */
	public boolean inWhitelist(int id) {
		for (int allowBlock : destroyWhitelist) {
			if (id == allowBlock) {
				return true;
			}
		}
		for (int allowBlock : globalWhitelist) {
			if (id == allowBlock) {
				return true;
			}
		}
		return false;
	}

	//==========
	// CHECKS
	//==========
	/**
	 * Check if this mob class can dig
	 * 
	 * @return True if this mob class can dig
	 */
	public boolean mobCanDig() {
		return mobCanDig;
	}

	/**
	 * Check if this mob class can sprint
	 * 
	 * @return True if this mob class can sprint
	 */
	public boolean mobCanSprint() {
		return mobCanSprint;
	}

	/**
	 * This method should be called "update horizontal movement" Either way, it
	 * needs to be overridden for it to work for this mod
	 * 
	 * @param f
	 *            X speed
	 * @param f1
	 *            Z speed
	 */
	@Override
	public void moveFlying(float f, float f1, float f2) {
		float velocity = MathHelper.sqrt_float(f * f + f1 * f1);
		if (velocity < 0.01F) {
			return;
		}
		velocity = f2 / velocity;
		f *= velocity;
		f1 *= velocity;
		float f4 = MathHelper.sin((rotationYaw * 3.141593F) / 180F);
		float f5 = MathHelper.cos((rotationYaw * 3.141593F) / 180F);
		motionX += f * f5 - f1 * f4;
		motionZ += f1 * f5 + f * f4;
	}

	/**
	 * Destroy a given block
	 * 
	 * @return True if this block exists and was attempted to dig
	 */
	protected boolean attemptDig(int i, int j, int k) {
		int id = worldObj.getBlockId(i, j, k);
		// Remove the top block
		if (id > 0) {
			// If it's a soft block, must be carrying nothing or a relevant tool
			// Random chance to destroy
			if (inWhitelist(id) || canBreakBlock(id) && !inBlacklist(id)) {
				digThroughBlock(id, i, j, k);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * The action an entity does to dig through a block
	 * 
	 * @param id
	 *            ID of block being destroyed
	 * @return Success of digging
	 */
	protected boolean digThroughBlock(int id, int i, int j, int k) {
		boolean flag = false;
		int metadata = worldObj.getBlockMetadata(i, j, k);
		Block block = Block.blocksList[id];
		if ((block != null) && (!partialDig(block, i, j, k))) {
			// Play breaking sound
			flag = worldObj.setBlockToAir(i, j, k);
			if (flag) {
				worldObj.playSoundAtEntity(this, block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
				block.dropBlockAsItem(worldObj, i, j, k, metadata, 0);
			}
		}
		return flag;
	}

	/**
	 * Determine how to dig to the entity given. This algorithm will pick the
	 * quickest direct route to the entity with no work-around routes
	 * 
	 * @param digToEntity
	 *            Entity to dig to
	 */
	protected void digToEntity(Entity digToEntity) {
		boolean headRoom = isHeadRoom();
		if ((canDig()) && (mobCanDig())) {
			// Radians for the X/Z plane bounded by 0 to 2PI
			float radsXZ = mod((float) ((rotationYaw * Math.PI) / 180F), (float) (2 * Math.PI));
			// Current Positions
			int curJ = MathHelper.floor_double(posY);
			// The entities positions
			int entJ = MathHelper.floor_double(digToEntity.posY);
			minI = MathHelper.floor_double(boundingBox.minX);
			maxI = MathHelper.floor_double(boundingBox.maxX);
			minJ = MathHelper.floor_double(boundingBox.minY);
			maxJ = MathHelper.floor_double(boundingBox.maxY);
			minK = MathHelper.floor_double(boundingBox.minZ);
			maxK = MathHelper.floor_double(boundingBox.maxZ);
			// Current rounded bounding boxes
			int curMinI = minI;
			int curMaxI = maxI;
			int curMinJ = minJ;
			int curMaxJ = maxJ;
			int curMinK = minK;
			int curMaxK = maxK;
			// Facing -X
			if ((radsXZ >= Math.PI / 4) && (radsXZ < (3 * Math.PI) / 4)) {
				// Since we're facing the minimum side, we dig just outside our own minI
				minI = minI - 1;
				maxI = minI;
				// Facing -Z
			} else if ((radsXZ >= (3 * Math.PI) / 4) && (radsXZ < (5 * Math.PI) / 4)) {
				// Since we're facing the minimum side, we dig just outside our own minI
				minK = minK - 1;
				maxK = minK;
				// Facing +X
			} else if ((radsXZ >= (5 * Math.PI) / 4) && (radsXZ < (7 * Math.PI) / 4)) {
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
				if (isCollidedHorizontally) {
					// Dig all spots above self first
					dug = digBox(curMinI, curMaxI, minJ - 1, minJ - 1, curMinK, curMaxK);
				}
				// Dig a clearing in front of self to jump onto
				dug = digBox(minI, maxI, minJ - 1, maxJ - 1, minK, maxK);
				// In this case, there may be an obstaclew, like stone, at the feet of the mob
				// try to make a clearing irectly in front instead
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
				if (!dug && isCollidedHorizontally && !headRoom) {
					// Dig space above self
					dug = digBox(curMinI, curMaxI, maxJ + 1, maxJ + 1, curMinK, curMaxK);
					// Dig headroom
					dug = digBox(minI, maxI, maxJ + 1, maxJ + 1, minK, maxK);
				}
			}
		}
		if (!dug) {
			// Reset to all previous values as this is a new block to dig
			renderglobal.removeDamagedBlock(lastBlockX, lastBlockY, lastBlockZ);
			curBlockDamage = 0.0F;
			soundTimer = 0;
		}
	}

	/**
	 * Check if a dig is a partial dig or not
	 * 
	 * @param block
	 *            Block attempting to dig
	 */
	protected boolean partialDig(Block block, int i, int j, int k) {
		Minecraft mc = ModLoader.getMinecraftInstance();
		// Add damage effects
		mc.effectRenderer.addBlockHitEffects(i, j, k, getSideHit(i, j, k));
		// Whether we have fully dug the block or not
		boolean dug = false;
		// Check if we should delay the dig
		if (digDelay > 0) {
			digDelay--;
			// Check if we're still digging the previous block
		} else if ((i == lastBlockX) && (j == lastBlockY) && (k == lastBlockZ)) {
			curBlockDamage += getBlockDamage(block);
			// Play the sound
			if (soundTimer % 4 == 0) {
				mc.sndManager.playSound(block.stepSound.stepSoundName, i + 0.5F, j + 0.5F, k + 0.5F, (block.stepSound.getVolume() + 1.0F) / 8F, block.stepSound.getPitch() * 0.5F);
			}
			soundTimer++;
			// Set that we have removed the block and reset data values
			if (curBlockDamage >= 1.0F) {
				dug = true;
				curBlockDamage = 0.0F;
				soundTimer = 0;
				digDelay = 5;
				renderglobal.removeDamagedBlock(i, j, k);
			} else {
				renderglobal.addDamagedBlock(this, block, curBlockDamage, i, j, k);
			}
		} else {
			// Reset to all previous values as this is a new block to dig
			renderglobal.removeDamagedBlock(lastBlockX, lastBlockY, lastBlockZ);
			curBlockDamage = 0.0F;
			soundTimer = 0;
			lastBlockX = i;
			lastBlockY = j;
			lastBlockZ = k;
		}
		return !dug;
	}

	protected void updatePlayerActionState() {
		//===================
		// START MORE DEADLY MOBS
		//===================
		// Moved into one conditional so MDM can work
		if (pathToEntity == null || rand.nextInt(100) == 0) {
			super.updatePlayerActionState();
			pathToEntity = null;
		} else {
			Vec3D vec3d = pathToEntity.getPosition(this);
			for (double d = width * 2.0F; vec3d != null && vec3d.squareDistanceTo(posX, vec3d.yCoord, posZ) < d * d;) {
				pathToEntity.incrementPathIndex();
				if (pathToEntity.isFinished()) {
					vec3d = null;
					pathToEntity = null;
				} else {
					vec3d = pathToEntity.getPosition(this);
				}
			}
			isJumping = false;
			if (vec3d != null) {
				double d1 = vec3d.xCoord - posX;
				double d2 = vec3d.zCoord - posZ;
				double d3 = vec3d.yCoord - (double) i;
				float f4 = (float) ((Math.atan2(d2, d1) * 180D) / 3.1415927410125732D) - 90F;
				float f5 = f4 - rotationYaw;
				// MORE DEADLY MOBS: Adding sprint function here
				moveForward = getCurrentMoveSpeed();
				for (; f5 < -180F; f5 += 360F) {
				}
				for (; f5 >= 180F; f5 -= 360F) {
				}
				if (f5 > 30F) {
					f5 = 30F;
				}
				if (f5 < -30F) {
					f5 = -30F;
				}
				rotationYaw += f5;
				if (hasAttacked && playerToAttack != null) {
					double d4 = playerToAttack.posX - posX;
					double d5 = playerToAttack.posZ - posZ;
					float f7 = rotationYaw;
					rotationYaw = (float) ((Math.atan2(d5, d4) * 180D) / 3.1415927410125732D) - 90F;
					float f6 = (((f7 - rotationYaw) + 90F) * 3.141593F) / 180F;
					moveStrafing = -MathHelper.sin(f6) * moveForward * 1.0F;
					moveForward = MathHelper.cos(f6) * moveForward * 1.0F;
				}
				// Unecessary, handled by more deadly mobs
				if ((d3 > 0.0D) && (playerToAttack == null)) {
					isJumping = true;
				}
			}
			if (playerToAttack != null) {
				faceEntity(playerToAttack, 30F, 30F);
			}
			// Sprint towards the player
			if ((playerToAttack != null) && (hasPath())) {
				if ((isSprinting) && (sprintCounter > 0)) {
					sprintCounter--;
				} else if (isSprinting) {
					isSprinting = false;
				} else if (sprintCounter >= SPRINT_MAX) {
					isSprinting = true;
				} else if (sprintCounter < SPRINT_MAX) {
					sprintCounter = sprintCounter + 0.5D;
				} else {
					sprintCounter = SPRINT_MAX;
				}
			} else {
				isSprinting = false;
			}
		}
		// When the mob isn't chasing a player
		if (isCollidedHorizontally && !hasPath()) {
			isJumping = true;
		}
		// Entity is in water.
		if (rand.nextFloat() < 0.8F && (flag1 || flag2)) {
			isJumping = true;
		}
		// More deadly mobs doesn't accept the fact that just because
		// the path isn't clear that the mob must stop searching.
		// Instead, if it's close enough (16 blocks) it'll keep digging towards you
		if ((playerToAttack != null) || ((playerToAttack != null) && (getDistanceToEntity(playerToAttack) < 16))) {
			if (playerToAttack == null) {
				faceEntity(playerToAttack, 30F, 30F);
			}
			if (!worldObj.isRemote) {
				digToEntity(playerToAttack);
				if (!dug && isCollidedHorizontally && isHeadRoom()) {
					// Make the zombie jump onto the ledge
					isJumping = true;
				}
			}
		} else {
			if (curBlockDamage > 0.0F) {
				// Reset to all previous values as this is a new block to dig
				renderglobal.removeDamagedBlock(lastBlockX, lastBlockY, lastBlockZ);
				curBlockDamage = 0.0F;
				soundTimer = 0;
			}
		}
		//===================
		// END MORE DEADLY MOBS
		//===================
	}

	/**
	 * Check if the mob can dig through the given blockID
	 * 
	 * @param id
	 * @return True if can dig through
	 */
	private boolean canBreakBlock(int id) {
		return ((isSoftBlock(id) && (getHeldItem() == null)) || (holdingEffectiveTool(id)));
	}

	//==========
	// GETTERS
	//==========
	/**
	 * Dig an area of dirt
	 * 
	 * @param minI
	 * @param maxI
	 * @param minJ
	 * @param maxJ
	 * @param minK
	 * @param maxK
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
	 * Get the side the mob hit Side numbers are based off of those in
	 * EffectRenderer
	 * 
	 * @return An int depicting the side
	 */
	private int getSideHit(int i, int j, int k) {
		int curI = MathHelper.floor_double(posX);
		int curJ = MathHelper.floor_double(posY);
		int curK = MathHelper.floor_double(posZ);
		if (j < curJ) {
			return 0;
		} else if (j > curJ) {
			return 1;
		} else if (k < curK) {
			return 2;
		} else if (k > curK) {
			return 3;
		} else if (i < curI) {
			return 4;
		} else {
			return 5;
		}
	}

	/**
	 * Check if the tool the mob is holding is effective against the block ID
	 * 
	 * @param id
	 * @return True if effective
	 */
	private boolean holdingEffectiveTool(int id) {
		ItemStack itemstack = getHeldItem();
		if ((id > 0) && (id < Block.blocksList.length) && (itemstack instanceof ItemStack) && (itemstack.getItem() instanceof ItemTool)) {
			Block b = Block.blocksList[id];
			if (b != null) {
				ItemTool tool = (ItemTool) itemstack.getItem();
				return (tool.getStrVsBlock(itemstack, b) > 1.0F);
			}
		}
		return false;
	}

	/**
	 * Check if there is headroom
	 * 
	 * @return True if there is head room
	 */
	private boolean isHeadRoom() {
		for (int digI = minI; !dug && digI <= maxI; digI++) {
			for (int digK = minK; !dug && digK <= maxK; digK++) {
				if (worldObj.getBlockId(digI, maxJ + 1, digK) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	//==========
	// SETTERS
	//==========
	/**
	 * Check if a block is an opaque cube
	 * 
	 * @param id
	 *            ID of block
	 */
	private boolean isOpaqueCube(int id) {
		if ((id <= 0) || (id > Block.blocksList.length) || (Block.blocksList[id] == null)) {
			return false;
		} else {
			return Block.blocksList[id].isOpaqueCube();
		}
	}

	//==========
	// DIGGING ALGORITHM
	//==========
	/**
	 * A hacked in "modulo" command, missing from Java for some reason
	 * 
	 * @param x
	 * @param y
	 * @reutrn x mod y
	 */
	private float mod(float x, float y) {
		float result = x % y;
		return result < 0 ? result + y : result;
	}

	/**
	 * Check if a block is soft or not
	 * 
	 * @param id
	 * @return True if the block is considered soft
	 */
	public static boolean isSoftBlock(int id) {
		// Check for hardness too, meaning all soft blocks can be targeted
		if ((id < Block.blocksList.length) && (Block.blocksList[id] != null)) {
			Block b = Block.cobblestone;
			Block block = Block.blocksList[id];
			Float str = block.blockHardness;
			if (str < SOFT_BLOCK_UPPER) {
				return true;
			}
		}
		return false;
	}
	//===================
	// END MORE DEADLY MOBS
	//===================
}
