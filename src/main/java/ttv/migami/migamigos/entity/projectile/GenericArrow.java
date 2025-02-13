package ttv.migami.migamigos.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ttv.migami.migamigos.init.ModEntities;

public class GenericArrow extends CustomProjectileEntity {
    public GenericArrow(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public GenericArrow(Level pLevel, LivingEntity pShooter, float damage, float speed) {
        super(ModEntities.GENERIC_ARROW.get(), pLevel, pShooter, damage, speed);
        this.affectedByGravity = true;
        this.checkForCollisions = true;
    }
}
