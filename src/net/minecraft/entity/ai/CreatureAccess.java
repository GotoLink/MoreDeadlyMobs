package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by Olivier on 21/12/2014.
 */
public final class CreatureAccess {

    public static EntityPlayer findPlayerToAttack(EntityCreature creature){
        if(!creature.canAttackClass(EntityPlayer.class)){
            return null;
        }
        for(Object object : creature.targetTasks.taskEntries){
            if(object instanceof EntityAITasks.EntityAITaskEntry && ((EntityAITasks.EntityAITaskEntry) object).action instanceof EntityAINearestAttackableTarget){
                EntityAINearestAttackableTarget attackable = ((EntityAINearestAttackableTarget) ((EntityAITasks.EntityAITaskEntry) object).action);
                if(attackable.targetEntity instanceof EntityPlayer){
                    return (EntityPlayer) attackable.targetEntity;
                }
                if(attackable.targetClass.equals(EntityPlayer.class)){
                    if(attackable.shouldExecute()){
                        return (EntityPlayer) attackable.targetEntity;
                    }
                    return null;
                }
            }
        }
        return null;
    }
}
