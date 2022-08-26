package endorh.aerobaticelytra.jetpack.client.trail;

import endorh.aerobaticelytra.jetpack.common.particle.JetpackParticleData;
import endorh.util.math.Vec3d;
import endorh.util.math.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Random;

public class JetpackTrail {
	
	private static final Random random = new Random();
	private static final Vec3d rocketLeft = Vec3d.ZERO.get();
	private static final Vec3d rocketRight = Vec3d.ZERO.get();
	private static final Vec3d rocketCenterLeft = Vec3d.ZERO.get();
	private static final Vec3d rocketCenterRight = Vec3d.ZERO.get();
	private static final Vec3d posVec = Vec3d.ZERO.get();
	private static final Vec3f headVec = Vec3f.ZERO.get();
	private static final Vec3f wingVec = Vec3f.ZERO.get();
	private static final Vec3f particleMotion = Vec3f.ZERO.get();
	private static final Vec3f normalVector = Vec3f.ZERO.get();
	
	/**
	 * Adds jetpack particles
	 * @param player Player flying
	 * @param propulsionVector Propulsion vector
	 * @param motionVec Motion vector
	 */
	public static void addParticles(
	  PlayerEntity player, Vec3f propulsionVector, Vec3f motionVec) {
		posVec.set(player.position());
		//posVec.add(motionVec);
		
		// Offset
		rocketLeft.set(posVec);
		headVec.set(propulsionVector);
		headVec.mul(0.9F);
		rocketLeft.add(headVec);
		rocketRight.set(rocketLeft);
		
		headVec.mul(0.7F / 0.9F);
		rocketCenterLeft.set(posVec);
		rocketCenterLeft.add(headVec);
		rocketCenterRight.set(rocketCenterLeft);
		
		wingVec.set(player.yBodyRot + 90F, 0F, true);
		wingVec.mul(0.55F);
		rocketLeft.sub(wingVec);
		rocketRight.add(wingVec);
		
		wingVec.mul(0.1F / 0.55F);
		rocketCenterLeft.sub(wingVec);
		rocketCenterRight.add(wingVec);
		
		normalVector.set(wingVec);
		normalVector.cross(headVec);
		normalVector.unitary();
		normalVector.mul(0.3F);
		rocketLeft.add(normalVector);
		rocketRight.add(normalVector);
		normalVector.mul(0.2F / 0.3F);
		rocketCenterLeft.add(normalVector);
		rocketCenterRight.add(normalVector);
		
		particleMotion.set(propulsionVector);
		particleMotion.mul(-0.10F);
		particleMotion.add(motionVec);
		
		float x_e;
		float z_e;
		float x_i;
		float z_i;
		
		// Add particles
		final int life = 6;
		final float size = 0.2F;
		final boolean ownPlayer = Minecraft.getInstance().player == player;
		
		final int d = 3;
		final float s = 1F / d;
		
		Vec3f diffVec = propulsionVector.copy();
		diffVec.mul(0.2F);
		diffVec.mul(s);
		rocketLeft.sub(diffVec);
		rocketRight.sub(diffVec);
		rocketCenterLeft.sub(diffVec);
		rocketCenterRight.sub(diffVec);
		final JetpackParticleData particle = new JetpackParticleData(life, size, ownPlayer);
		for (int i = 0; i < d; i++) {
			rocketLeft.add(diffVec);
			rocketRight.add(diffVec);
			rocketCenterLeft.add(diffVec);
			rocketCenterRight.add(diffVec);
			x_e = (float) random.nextGaussian() * 0.005F;
			z_e = (float) random.nextGaussian() * 0.005F;
			x_i = (float) random.nextGaussian() * 0.005F;
			z_i = (float) random.nextGaussian() * 0.005F;
			player.level.addParticle(
			  particle, rocketLeft.x, rocketLeft.y, rocketLeft.z,
			  particleMotion.x + x_e, particleMotion.y, particleMotion.z + z_e);
			player.level.addParticle(
			  particle, rocketRight.x, rocketRight.y, rocketRight.z,
			  particleMotion.x - x_e, particleMotion.y, particleMotion.z + z_e);
			player.level.addParticle(
			  particle, rocketCenterLeft.x, rocketCenterLeft.y, rocketCenterLeft.z,
			  particleMotion.x + x_i, particleMotion.y, particleMotion.z + z_i);
			player.level.addParticle(
			  particle, rocketCenterRight.x, rocketCenterRight.y, rocketCenterRight.z,
			  particleMotion.x - x_i, particleMotion.y, particleMotion.z + z_i);
		}
	}
	
	/**
	 * Adds jetpack hover particles
	 * @param player Player flying
	 * @param propulsionVector Propulsion vector
	 * @param motionVec Motion vector
	 */
	public static void addHoverParticles(
	  PlayerEntity player, Vec3f propulsionVector, Vec3f motionVec) {
		posVec.set(player.position());
		//posVec.add(motionVec);
		
		// Offset
		rocketLeft.set(posVec);
		headVec.set(propulsionVector);
		headVec.mul(0.9F);
		rocketLeft.add(headVec);
		rocketRight.set(rocketLeft);
		
		headVec.mul(0.8F / 0.9F);
		rocketCenterLeft.set(posVec);
		rocketCenterLeft.add(headVec);
		rocketCenterRight.set(rocketCenterLeft);
		
		wingVec.set(player.yBodyRot + 90F, 0F, true);
		wingVec.mul(0.53F);
		rocketLeft.sub(wingVec);
		rocketRight.add(wingVec);
		
		wingVec.mul(0.1F / 0.53F);
		rocketCenterLeft.sub(wingVec);
		rocketCenterRight.add(wingVec);
		
		normalVector.set(wingVec);
		normalVector.cross(headVec);
		normalVector.unitary();
		normalVector.mul(0.3F);
		rocketLeft.add(normalVector);
		rocketRight.add(normalVector);
		normalVector.mul(0.2F / 0.3F);
		rocketCenterLeft.add(normalVector);
		rocketCenterRight.add(normalVector);
		
		float x_e = (float) random.nextGaussian() * 0.005F;
		float z_e = (float) random.nextGaussian() * 0.005F;
		float x_i = (float) random.nextGaussian() * 0.005F;
		float z_i = (float) random.nextGaussian() * 0.005F;
		
		particleMotion.set(propulsionVector);
		particleMotion.mul(-0.1F);
		particleMotion.add(motionVec);
		
		// Add particles
		final int life = 8;
		final float size = 0.2F;
		final boolean hide = Minecraft.getInstance().player == player;
		player.level.addParticle(
		  new JetpackParticleData(life, size, hide),
		  rocketLeft.x, rocketLeft.y, rocketLeft.z,
		  particleMotion.x + x_e, particleMotion.y, particleMotion.z + z_e);
		player.level.addParticle(
		  new JetpackParticleData(life, size, hide),
		  rocketRight.x, rocketRight.y, rocketRight.z,
		  particleMotion.x - x_e, particleMotion.y, particleMotion.z + z_e);
		player.level.addParticle(
		  new JetpackParticleData(life, size, hide),
		  rocketCenterLeft.x, rocketCenterLeft.y, rocketCenterLeft.z,
		  particleMotion.x + x_i, particleMotion.y, particleMotion.z + z_i);
		player.level.addParticle(
		  new JetpackParticleData(life, size, hide),
		  rocketCenterRight.x, rocketCenterRight.y, rocketCenterRight.z,
		  particleMotion.x - x_i, particleMotion.y, particleMotion.z + z_i);
	}
	
	public static void offset() {
	
	}
}
