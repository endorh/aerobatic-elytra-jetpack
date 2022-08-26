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
		
		quadSize = size;
		lifetime = life;
		
		alpha = 1F;
		gravity = 0.08F;
		
		xd = speedX;
		yd = speedY;
		zd = speedZ;
		
		hasPhysics = true;
	}
	
	@Override public void render(
	  @NotNull IVertexBuilder buffer, @NotNull ActiveRenderInfo renderInfo, float partialTicks
	) {
		if (age < 1) // Weird things
			return;
		if (!ownPlayer || Minecraft.getInstance().options.getCameraType() != PointOfView.FIRST_PERSON) {
			//selectSpriteWithAge(sprites);
			pickSprite(sprites);
			quadSize = size * (1F - (float)age / lifetime);
			setAlpha(1F - 0.7F * (age + partialTicks) / lifetime);
			super.render(buffer, renderInfo, partialTicks);
		}
	}
	
	@Override public void tick() {
		xo = x;
		yo = y;
		zo = z;
		if (age++ >= lifetime) {
			remove();
		} else {
			yd -= 0.04D * (double)gravity;
			move(xd, yd, zd);
			final float friction = 0.96F;
			xd *= friction;
			yd *= friction;
			zd *= friction;
			if (onGround) {
				xd *= 0.7F;
				zd *= 0.7F;
			}
			
		}
	}
	
	@Override public float getQuadSize(float scaleFactor) {
		return super.getQuadSize(scaleFactor);
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
		public Particle createParticle(
		  @NotNull JetpackParticleData data, @NotNull ClientWorld world,
		  double x, double y, double z,
		  double xSpeed, double ySpeed, double zSpeed
		) {
			JetpackParticle particle = new JetpackParticle(
			  world, x, y, z, xSpeed, ySpeed, zSpeed,
			  data.tint, data.size, data.life, data.ownPlayer, sprites);
			//particle.selectSpriteWithAge(sprites);
			particle.pickSprite(sprites);
			return particle;
		}
	}
}
