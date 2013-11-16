package deadlymobs;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.world.World;

public class EntityCreeper extends EntityMob {
	public EntityCreeper(World world) {
		super(world);
	}

	/**
	 * Dig to the current target entity
	 */
	public boolean canDig() {
		return (super.canDig() && (mod_DiggingMobs.creepersDig));
	}

	@Override
	public void onUpdate() {
		//===================
		// START DIGGING MOBS
		//===================
		EntityCreature closest = getClosestCreature();
		if ((!isRiding()) && (closest.riddenByEntity == null) && (closest instanceof EntitySpider) && (pathToEntity == null)) {
			if (this.getDistanceToEntity(closest) < 3F) {
				this.mountEntity(closest);
			}
		}
		//===================
		// END DIGGING MOBS
		//===================
	}

	/**
	 * Destroy a given block
	 * 
	 * @return True if this block exists and was attempted to dig
	 */
	protected boolean attemptDig(int i, int j, int k) {
		int id = worldObj.getBlockId(i, j, k);
		if (id != 0) {
			digThroughBlock(id, i, j, k);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * The action an entity does to dig through a block
	 * 
	 * @param id
	 *            ID of block to dig
	 */
	protected boolean digThroughBlock(int id, int i, int j, int k) {
		// Make the creeper explode at the player
		if (timeSinceIgnited == 0) {
			worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
		}
		func_21090_e(1);
		timeSinceIgnited++;
		hasAttacked = true;
		if (timeSinceIgnited >= 30) {
			worldObj.createExplosion(this, i, j, k, 3F, true);
			setEntityDead();
		}
		return true;
	}
}
