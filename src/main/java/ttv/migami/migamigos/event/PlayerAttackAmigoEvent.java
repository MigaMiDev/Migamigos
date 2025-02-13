package ttv.migami.migamigos.event;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.migamigos.Config;
import ttv.migami.migamigos.entity.AmigoEntity;

@Mod.EventBusSubscriber
public class PlayerAttackAmigoEvent {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (event.getEntity() instanceof AmigoEntity amigoEntity && amigoEntity.getPlayer() != null && amigoEntity.getPlayer().equals(player) &&
                    !Config.COMMON.gameplay.friendlyFire.get()) {
                amigoEntity.level().playSound(null, amigoEntity, SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 0.5F, event.getEntity().getRandom().nextFloat() * 0.1F + 0.9F);
                event.setCanceled(true);
            }
        }
    }
}