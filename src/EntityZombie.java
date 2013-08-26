package net.minecraft.src;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

public class EntityZombie extends EntityMob
{

    public EntityZombie(World world)
    {
        super(world);
		
		//===================
		// START DIGGING MOBS
		//===================
		// Items zombies can destroy
		int[] canDestroy = { 2,  3,  6, 12, 13, 17, 18, 19, 20, 35, 
							37, 38, 39, 40, 46, 50, 54, 55, 59, 60,
							64, 69, 70, 71, 72, 75, 76, 77, 78, 79,
							80, 81, 82, 83, 85, 86, 87, 88, 89, 91, 
							92};
		this.canDestroy = canDestroy;
		//===================
		// END DIGGING MOBS
		//===================
    }
	
	//===================
	// START DIGGING MOBS
	//===================
	/**
	* Dig to the current target entity
	*/
	protected void digToEntity(Entity digToEntity) {
		if(mod_DiggingMobs.zombiesDig) {
			super.digToEntity(digToEntity, canDestroy);
		}
	}
	//===================
	// END DIGGING MOBS
	//===================
}
