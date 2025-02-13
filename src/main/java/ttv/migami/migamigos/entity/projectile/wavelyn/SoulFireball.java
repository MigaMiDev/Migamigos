package ttv.migami.migamigos.entity.projectile.wavelyn;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ttv.migami.migamigos.entity.projectile.CustomProjectileEntity;
import ttv.migami.migamigos.init.ModEntities;
import ttv.migami.migamigos.init.ModParticleTypes;

import static ttv.migami.migamigos.common.network.ServerPlayHandler.sendParticlesToAll;

public class SoulFireball extends CustomProjectileEntity {
    public SoulFireball(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SoulFireball(Level pLevel, LivingEntity pShooter, float damage, float speed) {
        super(ModEntities.SOUL_FIREBALL.get(), pLevel, pShooter, damage, speed);
        this.checkForCollisions = true;
    }

    @Override
    protected void onProjectileTick() {
        if(this.level() instanceof ServerLevel serverLevel && (this.tickCount < this.life)){

            SimpleParticleType lava;
            if (this.tickCount % 2 == 0) {
                lava = ModParticleTypes.SOUL_LAVA_PARTICLE.get();
            } else {
                lava = ParticleTypes.LAVA;
            }

            for(int i = 0; i < 3; i++) {
                sendParticlesToAll(
                        serverLevel,
                        lava,
                        true,
                        this.getX() - this.getDeltaMovement().x(),
                        this.getY() - this.getDeltaMovement().y(),
                        this.getZ() - this.getDeltaMovement().z(),
                        1,
                        0, 0, 0,
                        0
                );
            }
            sendParticlesToAll(
                    serverLevel,
                    ParticleTypes.ASH,
                    true,
                    this.getX() - this.getDeltaMovement().x(),
                    this.getY() - this.getDeltaMovement().y(),
                    this.getZ() - this.getDeltaMovement().z(),
                    1,
                    0, 0, 0,
                    0
            );
            sendParticlesToAll(
                    serverLevel,
                    ParticleTypes.WHITE_ASH,
                    true,
                    this.getX() - this.getDeltaMovement().x(),
                    this.getY() - this.getDeltaMovement().y(),
                    this.getZ() - this.getDeltaMovement().z(),
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    @Override
    protected void onHitEntity(Entity entity) {
        super.onHitEntity(entity);

        entity.setSecondsOnFire(2);
    }
}
