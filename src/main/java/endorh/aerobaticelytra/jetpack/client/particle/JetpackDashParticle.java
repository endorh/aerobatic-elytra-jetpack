package endorh.aerobaticelytra.jetpack.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import endorh.aerobaticelytra.jetpack.common.particle.DashParticleData;
import endorh.lazulib.math.Vec3d;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.*;

public class JetpackDashParticle extends TextureSheetParticle {
	private final Vec3d vector;
	private final int duration;
	private final boolean ownPlayer;
	private final int sustain;
	private final int fadeOut;
	
	private final boolean horizontalStagger;
	private final Vec3d pos = Vec3d.ZERO.get();
	private final Vec3d sub;
	private final Vec3d perp = Vec3d.ZERO.get();
	private final Vec3d stagger = new Vec3d(0, 0.8, 0);
	private final Vec3d temp = Vec3d.ZERO.get();
	
	protected JetpackDashParticle(
	  ClientLevel world, double x, double y, double z,
	  double speedX, double speedY, double speedZ, Vec3d vector,
	  int duration, int sustain, int fadeOut, boolean ownPlayer
	) {
		super(world, x, y, z, speedX, speedY, speedZ);
		
		this.ownPlayer = ownPlayer;
		this.duration = duration;
		this.sustain = sustain;
		this.fadeOut = fadeOut;
		lifetime = duration + sustain + fadeOut;
		this.vector = vector.copy();
		sub = vector.copy();
		
		horizontalStagger = abs(vector.y) > 0.8 * vector.norm();
		
		alpha = 0.9F;
		gravity = 0F;
		
		xd = speedX;
		yd = speedY;
		zd = speedZ;
		
		// Force recalculation of the bounding box
		setBoundingBox(getBoundingBox());
		
		hasPhysics = true;
	}
	
	// Prevent setPosition and setSize from setting an incorrect bounding box
	@Override public void setBoundingBox(@NotNull AABB bb) {
		if (vector != null) {
			super.setBoundingBox(new AABB(
			  x, y, z, x + vector.x, y + vector.y, z + vector.z
			).inflate(1F));
		} else super.setBoundingBox(bb);
	}
	
	@Override protected void setLocationFromBoundingbox() {
		if (vector != null) {
			AABB bb = getBoundingBox();
			x = vector.x < 0? bb.maxX - 1F : bb.minX + 1F;
			y = vector.y < 0? bb.maxY - 1F : bb.minY + 1F;
			z = vector.z < 0? bb.maxZ - 1F : bb.minZ + 1F;
		} else super.setLocationFromBoundingbox();
	}
	
	@Override public void render(
	  @NotNull VertexConsumer b, @NotNull Camera camera, float partial
	) {
		if (ownPlayer && age < duration + 1 && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON)
			return;
		Vec3 p = camera.getPosition();
		float progress = age > duration? 1F : (age + partial) / duration;
		
		pos.set(
		  Mth.lerp(partial, xo, x) - p.x,
		  Mth.lerp(partial, yo, y) - p.y,
		  Mth.lerp(partial, zo, z) - p.z);
		sub.set(vector);
		sub.mul(progress);
		
		perp.set(pos);
		perp.add(vector, 0.5);
		temp.set(perp);
		
		perp.cross(vector);
		if (perp.isZero()) return;
		perp.unitary();
		if (horizontalStagger) {
			stagger.set(0, 1, 0);
			stagger.cross(temp);
			stagger.unitary();
			stagger.mul(0.5);
		}
		
		float u0 = getU0();
		float u1 = getU1();
		float v0 = getV0();
		float v1 = getV1();
		double offset = 0.2F / sub.norm();
		for (int i = -1; i < 2; i++) {
			temp.set(pos);
			if (i != 0) {
				temp.add(stagger, i);
				temp.add(sub, offset);
			}
			vertex(b, temp, u0, v0, partial);
			temp.add(sub, i != 0? 1 - 2 * offset : 1);
			temp.add(perp, 0.1F);
			vertex(b, temp, u0, v1, partial);
			temp.sub(perp, 0.2F);
			vertex(b, temp, u1, v1, partial);
			temp.set(pos);
			if (i != 0) {
				temp.add(stagger, i);
				temp.add(sub, offset);
			}
			vertex(b, temp, u1, v0, partial);
		}
	}
	
	@Override public void tick() {
		super.tick();
		if (age > duration + sustain)
			alpha = Mth.clampedLerp(0.9F, 0F, ((float) age - duration - sustain) / fadeOut);
	}
	
	private void vertex(
	  @NotNull VertexConsumer b, Vec3d pos, float u, float v, float partial
	) {
		b.vertex(pos.x, pos.y, pos.z).uv(u, v).color(rCol, gCol, bCol, alpha).uv2(getLightColor(partial)).endVertex();
	}
	
	@NotNull @Override public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public static class Factory implements ParticleProvider<DashParticleData> {
		private final SpriteSet sprites;
		public Factory(SpriteSet sprite) {
			sprites = sprite;
		}

		@Nullable @Override public Particle createParticle(
		  @NotNull DashParticleData data, @NotNull ClientLevel world,
		  double x, double y, double z,
		  double xSpeed, double ySpeed, double zSpeed
		) {
			JetpackDashParticle particle = new JetpackDashParticle(
			  world, x, y, z, xSpeed, ySpeed, zSpeed,
			  data.vector, data.duration, data.sustain, data.fadeOut, data.ownPlayer);
			particle.pickSprite(sprites);
			return particle;
		}
	}
}
