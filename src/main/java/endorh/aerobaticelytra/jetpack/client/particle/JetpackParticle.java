package endorh.aerobaticelytra.jetpack.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import endorh.aerobaticelytra.jetpack.common.particle.JetpackParticleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public class JetpackParticle extends SpriteTexturedParticle {
	
	private final IAnimatedSprite sprites;
	private final float size;
	private final boolean ownPlayer;
	
	protected JetpackParticle(
	  ClientWorld world, double x, double y, double z,
	  double speedX, double speedY, double speedZ,
	  Color tint, float size, int life, boolean ownPlayer,
	  IAnimatedSprite sprites) {
		super(world, x, y, z, speedX, speedY, speedZ);
		this.sprites = sprites;
		
		setColor(tint.getRed()/255.0F, tint.getGreen()/255.0F, tint.getBlue()/255.0F);
		this.size = size;
		this.ownPlayer = ownPlayer;
		
		particleScale = size;
		maxAge = life;
		
		particleAlpha = 1F;
		particleGravity = 0.08F;
		
		motionX = speedX;
		motionY = speedY;
		motionZ = speedZ;
		
		canCollide = true;
	}
	
	@Override public void renderParticle(
	  @NotNull IVertexBuilder buffer, @NotNull ActiveRenderInfo renderInfo, float partialTicks
	) {
		if (age < 1) // Weird things
			return;
		if (!ownPlayer || Minecraft.getInstance().gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) {
			//selectSpriteWithAge(sprites);
			selectSpriteRandomly(sprites);
			particleScale = size * (1F - (float)age / maxAge);
			setAlphaF(1F - 0.7F * (age + partialTicks) / maxAge);
			super.renderParticle(buffer, renderInfo, partialTicks);
		}
	}
	
	@Override public void tick() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		if (age++ >= maxAge) {
			setExpired();
		} else {
			motionY -= 0.04D * (double)particleGravity;
			move(motionX, motionY, motionZ);
			final float friction = 0.96F;
			motionX *= friction;
			motionY *= friction;
			motionZ *= friction;
			if (onGround) {
				motionX *= 0.7F;
				motionZ *= 0.7F;
			}
			
		}
	}
	
	@Override public float getScale(float scaleFactor) {
		return super.getScale(scaleFactor);
	}
	
	@NotNull @Override public
	IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	public static class Factory implements IParticleFactory<JetpackParticleData> {
		private final IAnimatedSprite sprites;
		public Factory(IAnimatedSprite sprite) {
			sprites = sprite;
		}
		public Factory() {
			throw new UnsupportedOperationException("Use the Factory(IAnimatedSprite) constructor");
		}
		
		@Nullable @Override
		public Particle makeParticle(
		  @NotNull JetpackParticleData data, @NotNull ClientWorld world,
		  double x, double y, double z,
		  double xSpeed, double ySpeed, double zSpeed
		) {
			JetpackParticle particle = new JetpackParticle(
			  world, x, y, z, xSpeed, ySpeed, zSpeed,
			  data.tint, data.size, data.life, data.ownPlayer, sprites);
			//particle.selectSpriteWithAge(sprites);
			particle.selectSpriteRandomly(sprites);
			return particle;
		}
	}
}
