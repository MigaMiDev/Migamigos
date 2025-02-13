package ttv.migami.migamigos.entity.amigo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ttv.migami.migamigos.common.amigo.Action;
import ttv.migami.migamigos.entity.AmigoEntity;
import ttv.migami.migamigos.entity.ai.AmigoRangedAttackGoal;
import ttv.migami.migamigos.entity.projectile.GenericArrow;
import ttv.migami.migamigos.entity.projectile.cocogoat.IceCone;
import ttv.migami.migamigos.entity.summon.HailShower;
import ttv.migami.migamigos.entity.summon.IceLotus;
import ttv.migami.migamigos.init.ModParticleTypes;
import ttv.migami.migamigos.init.ModSounds;

import java.util.List;
import java.util.function.Consumer;

public class Cocogoat extends AmigoEntity {
    public Cocogoat(EntityType<? extends AmigoEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setDefaultItem(Items.BOW);
        this.chime = ModSounds.COCOGOAT_CHIME.get();
    }

    @Override
    public void tick() {
        super.tick();

        // Particle Tick
        if (--this.particleTick > 0) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 2; i++) {
                    serverLevel.sendParticles(ModParticleTypes.FROST_GLINT.get(),
                            this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth(),
                            1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public Action basicAction() {
        List<Integer> keyframeTimings = List.of(10, 30, 48, 76, 96, 128);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> basicAttack(),
                attacker -> basicAttack(),
                attacker -> basicAttack(),
                attacker -> basicAttack(),
                attacker -> basicAttack(),
                attacker -> basicAttack()
        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void basicAttack() {
        if (this.isDeadOrDying()) return;

        LivingEntity target = this.getTarget();
        if (target == null) return;

        this.setTarget(target);

        Vec3 direction = target.position().subtract(this.position()).normalize();
        GenericArrow arrow = new GenericArrow(this.level(), this, this.getAmigo().getAttackCombo().getPower() + this.getExtraPower(), 3.5F);

        this.level().playSound(null, this, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        this.level().addFreshEntity(arrow);

        Vec3 knockbackDirection = direction.scale(this.getAmigo().getAttackCombo().getRecoil());
        this.setDeltaMovement(knockbackDirection.x, knockbackDirection.y, knockbackDirection.z);
        this.hasImpulse = true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.goalSelector.addGoal(5, new AmigoRangedAttackGoal<>(this, 1.6));
    }

    @Override
    public Action specialAction() {
        List<Integer> keyframeTimings = List.of(10);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> specialAttack()
        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void specialAttack() {
        if (this.isDeadOrDying()) return;

        this.particleTick = 10;
        this.invulnerableTime = 20;

        IceLotus entity = new IceLotus(this.level(), this, this.getAmigo().getAttackSpecial().getPower() + this.getExtraPower());
        entity.setPos(this.getX(), this.getY(), this.getZ());
        this.level().addFreshEntity(entity);

        this.level().playSound(null, this, SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

        Vec3 lookAngle = this.getLookAngle();
        Vec3 knockbackDirection = lookAngle.scale(this.getAmigo().getAttackSpecial().getRecoil());
        this.setDeltaMovement(knockbackDirection.x, 0.1, knockbackDirection.z);
        this.hasImpulse = true;
    }

    public void summonIceCone(Vec3 pos) {
        IceCone projectile = new IceCone(this.level(), this, this.getAmigo().getAttackSpecial().getPower(), 0.0F);
        projectile.setPos(pos);
        this.level().addFreshEntity(projectile);
    }

    @Override
    public Action ultimateAction() {
        List<Integer> keyframeTimings = List.of(10);
        List<Consumer<AmigoEntity>> attackActions = List.of(
                attacker -> ultimateAttack()
        );
        return new Action(keyframeTimings, attackActions);
    }

    @Override
    public void ultimateAttack() {
        if (this.isDeadOrDying()) return;

        this.invulnerableTime = 60;

        HailShower entity = new HailShower(this.level(), this, this.getAmigo().getAttackUltimate().getPower() + this.getExtraPower());
        entity.setPos(this.getX(), this.getY(), this.getZ());
        this.level().addFreshEntity(entity);
    }
}
