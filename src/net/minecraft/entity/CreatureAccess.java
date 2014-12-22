package net.minecraft.entity;

/**
 * Created by Olivier on 21/12/2014.
 */
public final class CreatureAccess {
    public static boolean hasAttacked(EntityCreature creature){
        return creature.hasAttacked;
    }

    public static void setHasAttacked(EntityCreature creature){
        creature.hasAttacked = true;
    }
}
