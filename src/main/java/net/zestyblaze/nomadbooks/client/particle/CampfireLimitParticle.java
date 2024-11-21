package net.zestyblaze.nomadbooks.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.Vec3d;

public class CampfireLimitParticle extends SpriteBillboardParticle {

    private CampfireLimitParticle(ClientWorld world, Vec3d pos, Vec3d velocity, SpriteProvider spriteProvider) {
        super(world, pos.getX(), pos.getY(), pos.getZ(), velocity.getX(), velocity.getY(), velocity.getZ());
        this.gravityStrength = 0.0f;
        this.maxAge = 9;
        this.collidesWithWorld = false;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_LIT;
    }

    @Override
    public float getSize(float scaleFactor) {
        return 0.05f + (float)Math.sin(world.getTime()/10f)/50f;
    }

    @Environment(EnvType.CLIENT)
        public record DefaultFactory(SpriteProvider spriteProvider) implements ParticleFactory<SimpleParticleType> {

            @Override
            public Particle createParticle(SimpleParticleType type, ClientWorld level, double d, double e, double f, double g, double h, double i) {
                return new CampfireLimitParticle(level, new Vec3d(d, e, f), new Vec3d(g, h, i), this.spriteProvider);
            }
        }

    @Override
    public void tick() {
        if(this.age++ >= this.maxAge) {
            this.markDead();
        }
    }
}
