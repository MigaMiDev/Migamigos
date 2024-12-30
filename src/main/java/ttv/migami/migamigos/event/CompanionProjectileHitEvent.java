package ttv.migami.migamigos.event;

import net.minecraft.world.phys.HitResult;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import ttv.migami.migamigos.entity.projectile.CustomProjectileEntity;

/**
 * <p>Fired when a projectile hits a block or entity.</p>
 *
 * @author Ocelot
 */
@Cancelable
public class CompanionProjectileHitEvent extends Event
{
    private final HitResult result;
    private final CustomProjectileEntity projectile;

    public CompanionProjectileHitEvent(HitResult result, CustomProjectileEntity projectile)
    {
        this.result = result;
        this.projectile = projectile;
    }

    /**
     * @return The result of the entity's ray trace
     */
    public HitResult getRayTrace()
    {
        return result;
    }

    /**
     * @return The projectile that hit
     */
    public CustomProjectileEntity getProjectile()
    {
        return projectile;
    }
}