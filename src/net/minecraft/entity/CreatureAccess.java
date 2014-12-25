package net.minecraft.entity;

/**
 * Created by Olivier on 21/12/2014.
 */
public final class CreatureAccess {
    public static void setHasAttacked(EntityCreature creature){
        creature.hasAttacked = true;
    }

    public static Entity findPlayerToAttack(EntityCreature creature){
        return creature.findPlayerToAttack();
    }
}
