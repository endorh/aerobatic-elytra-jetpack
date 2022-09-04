package endorh.aerobaticelytra.jetpack.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import endorh.aerobaticelytra.jetpack.common.particle.DashParticleData;
import endorh.util.math.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static endorh.util.math.Interpolator.clampedLerp;
import static java.lang.Math.abs;
import static net.minecraft.util.math.MathHelper.lerp;

public class JetpackDashParticle extends SpriteTexturedParticle {
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
	  ClientWorld world, double x, double y, double z,
	  double speedX, double speedY, double speedZ, Vec3d vector,
	  int duration, int sustain, int fadeOut, boolean ownPlayer
	) {
		super(world, x, y, z, speedX, speedY, speedZ);
		
		this.ownPlayer = ownPlayer;
		this.duration = duration;
		this.sustain = sustain;
		this.fadeOut = fadeOut;
		maxAge = duration + sustain + fadeOut;
		this.vector = vector.copy();
		sub = vector.copy();
		
		horizontalStagger = abs(vector.y) > 0.8 * vector.norm();
		
		particleAlpha = 0.9F;
		particleGravity = 0F;
		
		motionX = speedX;
		motionY = speedY;
		motionY = speedZ;
		
		// Force recalculation of the bounding box
		setBoundingBox(getBoundingBox());
		
		canCollide = true;
	}
	
	// Prevent setPosition and setSize from setting an incorrect bounding box
	@Override public void setBoundingBox(@NotNull AxisAlignedBB bb) {
		if (vector != null) {
			super.setBoundingBox(new AxisAlignedBB(
			  posX, posY, posZ, posX + vector.x, posY + vector.y, posZ + vector.z
			).grow(1F));
		} else super.setBoundingBox(bb);
	}
	
	@Override protected void resetPositionToBB() {
		if (vector != null) {
			AxisAlignedBB bb = getBoundingBox();
			posX = vector.x < 0? bb.maxX - 1F : bb.minX + 1F;
			posY = vector.y < 0? bb.maxY - 1F : bb.minY + 1F;
			posZ = vector.z < 0? bb.maxZ - 1F : bb.minZ + 1F;
		} else super.resetPositionToBB();
	}
	
	@Override public void renderParticle(
	  @NotNull IVertexBuilder b, @NotNull ActiveRenderInfo camera, float partial
	) {
		if (ownPlayer && age < duration + 1 && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
			return;
		Vector3d p = camera.getProjectedView();
		float progress = age > duration? 1F : (age + partial) / duration;
		
		pos.set(
		  lerp(partial, prevPosX, posX) - p.getX(),
		  lerp(partial, prevPosY, posY) - p.getY(),
		  lerp(partial, prevPosZ, posZ) - p.getZ());
		sub.set(vector);
		sub.mul(progress);
		
		perp.set(pos);
		perp.add(vector, 0.5F);
		temp.set(perp);
		
		perp.cross(vector);
		if (perp.isZero()) return;
		perp.unitary();
		if (horizontalStagger) {
			stagger.set(0, 1, 0);
			stagger.cross(temp);
			stagger.unitary();
			stagger.mul(0.5F);
		}
		
		float minU = getMinU();
		float maxU = getMaxU();
		float minV = getMinV();
		float maxV = getMaxV();
		double offset = 0.2F / sub.norm();
		int light = getBrightnessForRender(partial);
		for (int i = -1; i < 2; i++) {
			temp.set(pos);
			if (i != 0) {
				temp.add(stagger, i);
				temp.add(sub, offset);
			}
			vertex(b, temp, minU, minV, light);
			temp.add(sub, i != 0? 1 - 2 * offset : 1);
			temp.add(perp, 0.1F);
			vertex(b, temp, minU, maxV, light);
			temp.sub(perp, 0.2F);
			vertex(b, temp, maxU, maxV, light);
			temp.set(pos);
			if (i != 0) {
				temp.add(stagger, i);
				temp.add(sub, offset);
			}
			vertex(b, temp, maxU, minV, light);
		}
	}
	
	@Override public void tick() {
		super.tick();
		if (age > duration + sustain)
			particleAlpha = clampedLerp(0.9F, 0F, ((float) age - duration - sustain) / fadeOut);
	}
	
	private void vertex(
	  @NotNull IVertexBuilder b, Vec3d pos, float u, float v, int light
	) {
		b.pos(pos.x, pos.y, pos.z).tex(u, v).color(
		  particleRed, particleGreen, particleBlue, particleAlpha
		).lightmap(light).endVertex();
	}
	
	@NotNull @Override public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public static class Factory implements IParticleFactory<DashParticleData> {
		private final IAnimatedSprite sprites;
		public Factory(IAnimatedSprite sprite) {
			sprites = sprite;
		}

		@Nullable @Override public Particle makeParticle(
		  @NotNull DashParticleData data, @NotNull ClientWorld world,
		  double x, double y, double z,
		  double xSpeed, double ySpeed, double zSpeed
		) {
			JetpackDashParticle particle = new JetpackDashParticle(
			  world, x, y, z, xSpeed, ySpeed, zSpeed,
			  data.vector, data.duration, data.sustain, data.fadeOut, data.ownPlayer);
			particle.selectSpriteRandomly(sprites);
			return particle;
		}
	}
}
