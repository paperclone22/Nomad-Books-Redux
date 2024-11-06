package net.zestyblaze.nomadbooks.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
        public record DefaultFactory(SpriteProvider spriteProvider) implements ParticleFactory<DefaultParticleType> {

            @Override
            public Particle createParticle(DefaultParticleType type, ClientWorld level, double d, double e, double f, double g, double h, double i) {
                return new CampfireLimitParticle(level, new Vec3d(d, e, f), new Vec3d(g, h, i), this.spriteProvider);
            }
        }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d cameraPos = camera.getPos();
        float offsetX = (float)(MathHelper.lerp(tickDelta, this.prevPosX, this.x) - cameraPos.getX());
        float offsetY = (float)(MathHelper.lerp(tickDelta, this.prevPosY, this.y) - cameraPos.getY());
        float offsetZ = (float)(MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - cameraPos.getZ());

        Quaternionf quaternion = (this.angle == 0.0f) ? camera.getRotation() : new Quaternionf(camera.getRotation()).mul(new Quaternionf().rotationZ(MathHelper.lerp(tickDelta, this.prevAngle, this.angle)));

        Vector3f[] vertices = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F,  1.0F, 0.0F),
            new Vector3f( 1.0F,  1.0F, 0.0F),
            new Vector3f( 1.0F, -1.0F, 0.0F)
        };

        float scale = this.getSize(tickDelta);

        for(int i = 0; i < 4; ++i) {
            vertices[i].set(transform(quaternion, vertices[i]).mul(scale).add(offsetX, offsetY, offsetZ));
        }

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int light = 15728880;

        vertexConsumer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).texture(maxU, maxV).color(255, 255, 255, 255).light(light).next();
        vertexConsumer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).texture(maxU, minV).color(255, 255, 255, 255).light(light).next();
        vertexConsumer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).texture(minU, minV).color(255, 255, 255, 255).light(light).next();
        vertexConsumer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).texture(minU, maxV).color(255, 255, 255, 255).light(light).next();
    }

    public Vector3f transform(Quaternionf quaternion, Vector3f vector) {
        Quaternionf tempQuat = new Quaternionf(quaternion);
        tempQuat.mul(new Quaternionf(vector.x(), vector.y(), vector.z(), 0.0F));
        Quaternionf inverse = new Quaternionf(quaternion).mul(new Quaternionf(-1, -1, -1, 1));
        tempQuat.mul(inverse);
        vector.set(tempQuat.x(), tempQuat.y(), tempQuat.z());
        return vector;
    }

    @Override
    public void tick() {
        if(this.age++ >= this.maxAge) {
            this.markDead();
        }
    }
}
