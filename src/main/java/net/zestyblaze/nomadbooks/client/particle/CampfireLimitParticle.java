package net.zestyblaze.nomadbooks.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CampfireLimitParticle extends TextureSheetParticle {

    private CampfireLimitParticle(ClientLevel world, Vec3 pos, Vec3 velocity, SpriteSet spriteProvider) {
        super(world, pos.x(), pos.y(), pos.z(), velocity.x(), velocity.y(), velocity.z());
        this.gravity = 0.0f;
        this.lifetime = 9;
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteProvider);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return 0.05f + (float)Math.sin(level.getGameTime()/10f)/50f;
    }

    @Environment(EnvType.CLIENT)
        public record DefaultFactory(SpriteSet spriteProvider) implements ParticleProvider<SimpleParticleType> {

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double d, double e, double f, double g, double h, double i) {
                return new CampfireLimitParticle(level, new Vec3(d, e, f), new Vec3(g, h, i), this.spriteProvider);
            }
        }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3 cameraPos = camera.getPosition();
        float offsetX = (float)(Mth.lerp(tickDelta, this.xo, this.x) - cameraPos.x());
        float offsetY = (float)(Mth.lerp(tickDelta, this.yo, this.y) - cameraPos.y());
        float offsetZ = (float)(Mth.lerp(tickDelta, this.zo, this.z) - cameraPos.z());

        Quaternionf quaternion = (this.roll == 0.0f) ? camera.rotation() : new Quaternionf(camera.rotation()).mul(new Quaternionf().rotationZ(Mth.lerp(tickDelta, this.oRoll, this.roll)));

        Vector3f[] vertices = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F,  1.0F, 0.0F),
            new Vector3f( 1.0F,  1.0F, 0.0F),
            new Vector3f( 1.0F, -1.0F, 0.0F)
        };

        float scale = this.getQuadSize(tickDelta);

        for(int i = 0; i < 4; ++i) {
            vertices[i].set(transform(quaternion, vertices[i]).mul(scale).add(offsetX, offsetY, offsetZ));
        }

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int light = 15728880;

        vertexConsumer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(maxU, maxV).color(255, 255, 255, 255).uv2(light).endVertex();
        vertexConsumer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(maxU, minV).color(255, 255, 255, 255).uv2(light).endVertex();
        vertexConsumer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(minU, minV).color(255, 255, 255, 255).uv2(light).endVertex();
        vertexConsumer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(minU, maxV).color(255, 255, 255, 255).uv2(light).endVertex();
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
        if(this.age++ >= this.lifetime) {
            this.remove();
        }
    }
}
