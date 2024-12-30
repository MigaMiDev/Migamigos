package ttv.migami.migamigos.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GlintParticle extends RisingParticle {
   GlintParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.hasPhysics = false;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @Override
   public float getQuadSize(float pScaleFactor) {
      float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
      return this.quadSize * (1.0F - f * f);
   }

   public int getLightColor(float pPartialTick) {
      return 240;

   }

   @OnlyIn(Dist.CLIENT)
   public static class LightBlueProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public LightBlueProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         GlintParticle glint = new GlintParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         glint.setColor(1.0F, 1.0F, 1.0F);
         glint.pickSprite(this.sprite);
         glint.setLifetime(20 + pLevel.getRandom().nextInt(20));
         glint.scale(2.0F);
         return glint;
      }
   }
}