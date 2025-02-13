package ttv.migami.migamigos.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmokeParticle extends TextureSheetParticle {
    private float maxSize = 30F;
    private boolean freeze = false;

    protected SmokeParticle(ClientLevel level, double xCoord, double yCoord, double zCoord,
                            SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.friction = 0.8F;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize *= this.random.nextFloat() * 1.0F + 0.2F;
        this.lifetime = 60;
        this.setSpriteFromAge(spriteSet);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }

    @Override
    public void tick() {
        if (!this.freeze) {
            fadeIn();
        }
        super.tick();
        fadeOut();
    }

    private void fadeIn() {
        float value = 1;
        this.alpha = ((float)age / 2)-((float)0.2) - 0.2f;
    }
    private void fadeOut() {
        float value = 1;
        if (this.freeze) {
            value = 0.4F;
        }
        this.alpha = (-(value/(float)lifetime) * age + value);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float pPartialTick) {
        float f = ((float)this.age + pPartialTick) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(pPartialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j -= (int)(f * 15.0F * 16.0F);
        if (j < 240) {
            j = 240;
        }

        return 240;
    }

    public float getQuadSize(float pScaleFactor) {
        float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
        return this.quadSize * ((1.0F + f * f)*(maxSize));
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            SmokeParticle smokeParticle = new SmokeParticle(level, x,y,z, this.sprites, dx,dy,dz);
            smokeParticle.pickSprite(this.sprites);
            smokeParticle.maxSize = 30F;
            //smokeParticle.setColor(1.0F, 0.2F, 0.2F);
            return smokeParticle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class BreezeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public BreezeProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            SmokeParticle smokeParticle = new SmokeParticle(level, x,y,z, this.sprites, dx,dy,dz);
            smokeParticle.pickSprite(this.sprites);
            smokeParticle.freeze = true;
            smokeParticle.maxSize = 30F;
            smokeParticle.alpha = 0.4F;
            return smokeParticle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SmallBreezeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SmallBreezeProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            SmokeParticle smokeParticle = new SmokeParticle(level, x,y,z, this.sprites, dx,dy,dz);
            smokeParticle.pickSprite(this.sprites);
            smokeParticle.freeze = true;
            smokeParticle.maxSize = 10F;
            smokeParticle.alpha = 0.4F;
            return smokeParticle;
        }
    }
}