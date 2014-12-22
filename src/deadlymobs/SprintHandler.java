package deadlymobs;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;

/**
 * Created by Olivier on 21/12/2014.
 */
public class SprintHandler {
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
     * Get this mob's current move speed taking into account sprinting
     *
     * @return float Current move speed
     */
    public float getCurrentMoveSpeed(EntityLivingBase entity) {
        double moveSpeed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue();
        if (DiggingMobs.canSprint(EntityList.getEntityString(entity)) && isSprinting) {
            return (float) (moveSpeed * DiggingMobs.sprintMultiplier);
        } else {
            return (float) moveSpeed;
        }
    }

    public void setSprinting(boolean value) {
        isSprinting = value;
    }

    public void sprint() {
        if (isSprinting && sprintCounter > 0) {
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
    }
}
