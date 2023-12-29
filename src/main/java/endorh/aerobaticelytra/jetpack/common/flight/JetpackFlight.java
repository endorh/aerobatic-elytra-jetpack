package endorh.aerobaticelytra.jetpack.common.flight;

import endorh.aerobaticelytra.common.AerobaticElytraLogic;
import endorh.aerobaticelytra.common.capability.IElytraSpec;
import endorh.aerobaticelytra.common.capability.IFlightData;
import endorh.aerobaticelytra.common.flight.TravelHandler;
import endorh.aerobaticelytra.common.flight.mode.IFlightMode;
import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.aerobaticelytra.jetpack.client.config.ClientConfig;
import endorh.aerobaticelytra.jetpack.client.sound.JetpackSound;
import endorh.aerobaticelytra.jetpack.client.trail.JetpackTrail;
import endorh.aerobaticelytra.jetpack.common.JetpackLogic;
import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability;
import endorh.aerobaticelytra.jetpack.common.config.Config;
import endorh.aerobaticelytra.jetpack.common.config.Config.flight;
import endorh.aerobaticelytra.jetpack.common.config.Config.flight_modes;
import endorh.aerobaticelytra.jetpack.common.config.Config.height_penalty;
import endorh.aerobaticelytra.jetpack.common.config.Config.network;
import endorh.aerobaticelytra.jetpack.common.item.JetpackAbilities;
import endorh.aerobaticelytra.jetpack.network.JetpackPackets.DJetpackPropulsionVectorPacket;
import endorh.aerobaticelytra.jetpack.network.JetpackPackets.SJetpackFlyingPacket;
import endorh.aerobaticelytra.jetpack.network.JetpackPackets.SJetpackMotionPacket;
import endorh.aerobaticelytra.network.AerobaticPackets.DFlightModePacket;
import endorh.lazulib.math.Vec3f;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;
import static endorh.aerobaticelytra.common.item.IAbility.Ability.FUEL;
import static endorh.lazulib.math.Vec3f.PI_HALF;
import static endorh.lazulib.math.Vec3f.TO_RAD;
import static java.lang.Math.*;

/**
 * Jetpack flight physics
 */
@EventBusSubscriber(modid = AerobaticJetpack.MOD_ID)
public class JetpackFlight {
	private static final Logger LOGGER = LogManager.getLogger();

	// Cache vectors
	private static final Vec3f targetVec = Vec3f.ZERO.get();
	private static final Vec3f motionVec = Vec3f.ZERO.get();
	private static final Vec3f accVec = Vec3f.ZERO.get();
	
	/**
	 * Handle a jetpack flight tick
	 * @param player Player flying
	 * @param travelVector Travel vector from {@link LivingEntity#travel(Vec3)}
	 * @return True if default travel handling should be cancelled
	 */
	public static boolean onJetpackTravel(Player player, Vec3 travelVector) {
		IFlightData fd = getFlightDataOrDefault(player);
		IJetpackData data = JetpackDataCapability.getJetpackDataOrDefault(player);
		IElytraSpec spec = AerobaticElytraLogic.getElytraSpecOrDefault(player);
		if (!JetpackLogic.shouldJetpackFly(player)) {
			Vec3f vec = data.getPropulsionVector();
			data.updatePrevPropulsionVector();
			vec.add(YP);
			vec.unitary();
			if (player instanceof ServerPlayer) {
				grantExtraFloatImmunity((ServerPlayer) player);
			}
			return false;
		}
		boolean cancel = false;
		if (!data.isFlying() && fd.isFlightMode(JetpackFlightModes.JETPACK_HOVER) && !player.onGround()
		    && player.tickCount - data.getLastFlight() > 5) {
			// Keep hover level
			data.setFlying(true);
			if (!player.level().isClientSide)
				new SJetpackFlyingPacket(player, data).sendTracking();
			if (player.tickCount - data.getLastGround() == 1) {
				double y = player.getY() - floor(player.getY());
				double f = player.getDeltaMovement().y;
				if (y >= 0.7D && f <= 0D && f >= -0.2D) {
					data.updateSneaking(false);
					motionVec.set(0F, 1F-(float)y, 0F);
					player.move(MoverType.SELF, motionVec.toVector3d());
					motionVec.set(player.getDeltaMovement());
					motionVec.y = 0F;
					player.setDeltaMovement(motionVec.toVector3d());
				}
			}
		}
		if (data.isFlying()) {
			motionVec.set(player.getDeltaMovement());
			double grav = TravelHandler.travelGravity(player);
			
			if (fd.isFlightMode(JetpackFlightModes.JETPACK_FLIGHT)
			    && spec.getAbility(JetpackAbilities.JETPACK) > 0) {
				cancel = onJetpackFlight(player, travelVector, motionVec, grav);
			} else if (fd.isFlightMode(JetpackFlightModes.JETPACK_HOVER)
			           && spec.getAbility(JetpackAbilities.HOVER) > 0
			           && flight_modes.hover_flight_enabled) {
				cancel = onJetpackHover(player, travelVector, motionVec, grav);
			}
			
			if (cancel) {
				float speed = data.getFallSpeed();
				if (speed >= 0F) {
					player.fallDistance = 0F;
				} else {
					// Approximate fall distance by fall speed (ignores friction)
					player.fallDistance = speed * speed / (2F * (float)grav);
				}
			}
			data.setFallSpeed(motionVec.y);
			
			if (player.onGround()) {
				data.setFlying(false);
				if (!player.level().isClientSide)
					new SJetpackFlyingPacket(player, data).sendTracking();
				data.setSneaking(false);
				if (player.level().isClientSide) {
					if (ClientConfig.disable_hover_when_landing) {
						if (fd.isFlightMode(JetpackFlightModes.JETPACK_HOVER))
							fd.setFlightMode(JetpackFlightModes.JETPACK_FLIGHT);
						new DFlightModePacket(JetpackFlightModes.JETPACK_FLIGHT).send();
					}
				}
			}
		} else if (!player.onGround()) {
			if (player.tickCount - data.getLastGround() > 1) {
				data.setFlying(true);
				if (!player.level().isClientSide)
					new SJetpackFlyingPacket(player, data).sendTracking();
			}
		} else if (player.onGround()) {
			if (player.tickCount - data.getLastGround() == 1)
				onNonFlightTravel(player, travelVector);
			data.setLastGround(player.tickCount);
		}
		return cancel;
	}
	
	/**
	 * Handle a single jetpack flight tick
	 * @param player Player flying
	 * @param travelVector Travel vector from {@link LivingEntity#travel(Vec3)}
	 * @param motionVec Player motion vector to update
	 * @param grav Gravity applied
	 * @return True if default travel handling should be cancelled
	 */
	public static boolean onJetpackFlight(
	  Player player, Vec3 travelVector, Vec3f motionVec, double grav
	) {
		IJetpackData data = JetpackDataCapability.getJetpackDataOrDefault(player);
		IElytraSpec spec = AerobaticElytraLogic.getElytraSpecOrDefault(player);
		
		float heat = data.getHeat();
		heat = Mth.clamp(
		  heat + (data.isJumping() ? flight.charge_per_tick : -flight.cooldown_per_tick),
		  0F, 1F);
		data.setHeat(heat);
		
		Vec3f propVec = data.getPropulsionVector();
		data.updatePrevPropulsionVector();
		if (data.isJumping() && (travelVector.x != 0 || travelVector.z != 0)) {
			float targetPolar = -PI_HALF - flight.tilt_range_rad;
			float targetAzimuth = (float) atan2(
			  Float.compare((float) travelVector.z, 0F),
			  Float.compare((float) travelVector.x, 0F)) + player.getYRot() * TO_RAD + PI_HALF;
			targetVec.set(targetAzimuth, targetPolar, false);
		} else targetVec.set(0F, 1F, 0F);
		propVec.lerp(targetVec, 0.2F);
		propVec.unitary();
		
		if (data.isJumping()) {
			float prop = Mth.lerp(
			  heat, flight.propulsion_base_tick, flight.propulsion_max_tick);
			float y = (float)player.getY();
			if (y > Config.height_penalty.min_height) {
				prop = Mth.lerp(
				  Mth.clamp((y - Config.height_penalty.min_height) /
				                   Config.height_penalty.range, 0F, 1F),
				  prop, prop * height_penalty.penalty
				);
			}
			prop *= spec.getAbility(JetpackAbilities.JETPACK);
			
			float consume = prop / flight.propulsion_max_tick;
			float fuel = consume * Config.fuel.fuel_usage_linear_tick +
			             consume * consume * Config.fuel.fuel_usage_quad_tick +
			             Mth.sqrt(consume) * Config.fuel.fuel_usage_sqrt_tick;
			spec.setAbility(FUEL, max(spec.getAbility(FUEL) - fuel, 0F));
			
			accVec.set(propVec);
			accVec.mul(prop);
			
			//LOGGER.debug("Propulsion: " + propVec + ", " + target);
			motionVec.add(accVec);
			motionVec.y -= grav;
			motionVec.mul(0.99F, 0.98F, 0.99F); // Same as the elytra
			
			player.setDeltaMovement(motionVec.toVector3d());
			player.move(MoverType.SELF, player.getDeltaMovement());
			if (!player.level().isClientSide)
				new SJetpackMotionPacket(player).sendTracking();
			player.awardStat(
			  JetpackStats.JETPACK_FLIGHT_ONE_CM,
			  (int) round(player.getDeltaMovement().length() * 100F));
			if (player instanceof ServerPlayer)
				TravelHandler.resetFloatingTickCount((ServerPlayer) player);
			data.setLastFlight(player.tickCount);
			
			if (player.isLocalPlayer())
				new DJetpackPropulsionVectorPacket(data).send();
			if (player.level().isClientSide) {
				motionVec.set(player.getDeltaMovement());
				JetpackTrail.addParticles(player, propVec, motionVec);
				if (data.isJumping() && data.updatePlayingSound(true))
					new JetpackSound(player).play();
			}
			return true;
		} else if (player instanceof ServerPlayer) {
			grantExtraFloatImmunity((ServerPlayer) player);
		} else if (player.isLocalPlayer())
			new DJetpackPropulsionVectorPacket(data).send();
		// Do not cancel default logic
		return false;
	}
	
	public static void grantExtraFloatImmunity(ServerPlayer player) {
		IJetpackData data = JetpackDataCapability.getJetpackDataOrDefault(player);
		final int flying = player.tickCount - data.getLastFlight();
		if (flying < 0) {
			data.setLastFlight(0);
		} else if (network.allowed_extra_float_ticks == 0
		           || flying < network.allowed_extra_float_ticks) {
			TravelHandler.resetFloatingTickCount(player);
		}
	}
	
	/**
	 * Handle a single jetpack hover flight tick
	 * @param player Player flying
	 * @param travelVector Travel vector from {@link LivingEntity#travel(Vec3)}
	 * @param motionVec Player motion vector to update
	 * @param grav Gravity applied to the player
	 * @return True if default travel handling should be cancelled
	 */
	public static boolean onJetpackHover(
	  Player player, Vec3 travelVector, Vec3f motionVec, double grav
	) {
		IJetpackData data = JetpackDataCapability.getJetpackDataOrDefault(player);
		IElytraSpec spec = AerobaticElytraLogic.getElytraSpecOrDefault(player);
		
		// Vertical propulsion
		float heat = data.getHeat();
		heat = Mth.clamp(heat -flight.cooldown_per_tick, 0F, 1F);
		data.setHeat(heat);
		
		float vProp = data.getHoverPropulsion();
		float vTarget = data.isJumping()? 1F : 0F;
		vTarget -= data.isSneaking()? 1F : 0F;
		vProp = Mth.lerp(signum(vTarget) != signum(vProp)? 0.5F : 0.1F, vProp, vTarget);
		data.setHoverPropulsion(vProp);
		vProp *= spec.getAbility(JetpackAbilities.HOVER);
		
		// Horizontal movement
		Vec3f propVec = data.getPropulsionVector();
		data.updatePrevPropulsionVector();
		if (travelVector.x != 0 || travelVector.z != 0) {
			float targetPolar = -PI_HALF - flight.tilt_range_rad;
			float targetAzimuth = (float) atan2(
			  Float.compare((float) travelVector.z, 0F),
			  Float.compare((float) travelVector.x, 0F)) + player.getYRot() * TO_RAD + PI_HALF;
			targetVec.set(targetAzimuth, targetPolar, false);
		} else
			targetVec.set(0F, 1F, 0F);
		propVec.lerp(targetVec, targetVec.y == 1F? 0.4F : 0.2F);
		propVec.unitary();
		
		float horMax = Mth.sin(flight.tilt_range_rad);
		if (horMax < 1E-5F)
			horMax = 1F;
		
		if (motionVec.hNormSquared() > flight.hover_horizontal_speed_tick * flight.hover_horizontal_speed_tick) {
			final Vec3f brake = motionVec.copy();
			brake.y = 0F;
			brake.mul(flight.hover_horizontal_speed_tick / brake.norm());
			motionVec.sub(brake);
		} else {
			motionVec.x = Mth.lerp(
			  signum(propVec.x) != signum(motionVec.x)? 0.5F : 0.2F, motionVec.x,
			  propVec.x / horMax * flight.hover_horizontal_speed_tick) * spec.getAbility(
			  JetpackAbilities.HOVER);
			motionVec.z = Mth.lerp(
			  signum(propVec.z) != signum(motionVec.z)? 0.5F : 0.2F, motionVec.z,
			  propVec.z / horMax * flight.hover_horizontal_speed_tick) * spec.getAbility(
			  JetpackAbilities.HOVER);
		}
		
		// Vertical movement
		if (abs(motionVec.y) > flight.hover_vertical_speed_tick * 1.01F) {
			float prop = Mth.lerp(
			  heat, flight.propulsion_base_tick, flight.propulsion_max_tick);
			prop *= max(spec.getAbility(JetpackAbilities.JETPACK), spec.getAbility(
			  JetpackAbilities.HOVER));
			
			accVec.set(propVec);
			accVec.mul(prop);
			
			motionVec.y -= grav;
			motionVec.y -= signum(motionVec.y) * min(prop * propVec.y, abs(motionVec.y));
			motionVec.mul(0.98F);
			player.setDeltaMovement(motionVec.toVector3d());
			player.move(MoverType.SELF, player.getDeltaMovement());
			if (!player.level().isClientSide)
				new SJetpackMotionPacket(player).sendTracking();
			player.awardStat(JetpackStats.JETPACK_FLIGHT_ONE_CM,
			               (int) round(player.getDeltaMovement().length() * 100F));
			
			if (AerobaticElytraLogic.isLocalPlayer(player))
				new DJetpackPropulsionVectorPacket(data).send();
			if (player.level().isClientSide) {
				motionVec.set(player.getDeltaMovement());
				JetpackTrail.addParticles(player, propVec, motionVec);
				if (data.updatePlayingSound(true))
					new JetpackSound(player).play();
			}
		} else {
			motionVec.y = round(
			  vProp * flight.hover_vertical_speed_tick * 1E6) * 1E-6F;
			motionVec.mul(0.98F);
			player.setDeltaMovement(motionVec.toVector3d());
			player.move(MoverType.SELF, player.getDeltaMovement());
			if (!player.level().isClientSide)
				new SJetpackMotionPacket(player).sendTracking();
			player.awardStat(JetpackStats.JETPACK_FLIGHT_ONE_CM,
			               (int) round(player.getDeltaMovement().length() * 100F));
			player.awardStat(JetpackStats.JETPACK_HOVER_ONE_SECOND, 1);
			
			if (AerobaticElytraLogic.isLocalPlayer(player))
				new DJetpackPropulsionVectorPacket(data).send();
			if (player.level().isClientSide) {
				motionVec.set(player.getDeltaMovement());
				JetpackTrail.addHoverParticles(player, propVec, motionVec);
				if (data.updatePlayingSound(true))
					new JetpackSound(player).play();
			}
		}
		if (player instanceof ServerPlayer)
			TravelHandler.resetFloatingTickCount((ServerPlayer) player);
		data.setLastFlight(player.tickCount);
		float fuel = Config.fuel.fuel_usage_hover_tick;
		spec.setAbility(FUEL, max(0F, spec.getAbility(FUEL) - fuel));
		
		// TODO: Handle levitation (?)
		
		// Cancel default logic
		return true;
	}
	
	private static final Vec3f YP = Vec3f.YP.get();
	/**
	 * Cool the jetpack
	 */
	public static void onOtherModeFlightTravel(
	  Player player, Vec3 travelVector
	) {
		IFlightData fd = getFlightDataOrDefault(player);
		IJetpackData data = JetpackDataCapability.getJetpackDataOrDefault(player);
		if (fd.getFlightMode().is(JetpackFlightModeTags.JETPACK))
			return;
		float heat = data.getHeat();
		if (heat == 0F)
			return;
		data.setFlying(false);
		data.setHeat(max(0F, heat - 0.05F));
		data.updatePrevPropulsionVector();
		final Vec3f propVec = data.getPropulsionVector();
		propVec.add(YP);
		propVec.unitary();
		data.setHoverPropulsion(0F);
	}
	
	public static void onNonFlightTravel(
	  Player player, Vec3 travelVector
	) {
		IJetpackData data = JetpackDataCapability.getJetpackDataOrDefault(player);
		data.setHoverPropulsion(0F);
		data.updatePrevPropulsionVector();
		final Vec3f propVec = data.getPropulsionVector();
		propVec.add(YP);
		propVec.unitary();
		float heat = data.getHeat();
		if (heat == 0F)
			return;
		data.setHeat(max(0F, heat - 0.05F));
	}
	
	public static void onRemoteJetpackTravel(Player player) {
		IFlightData data = getFlightDataOrDefault(player);
		IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(player);
		IFlightMode mode = data.getFlightMode();
		if (JetpackLogic.shouldJetpackFly(player)) {
			jet.updatePrevPropulsionVector();
			if (mode == JetpackFlightModes.JETPACK_FLIGHT && jet.isJumping()) {
				JetpackTrail.addParticles(
				  player, jet.getPropulsionVector(), new Vec3f(player.getDeltaMovement()));
				if (jet.isJumping() && jet.updatePlayingSound(true))
					new JetpackSound(player).play();
			} else if (mode == JetpackFlightModes.JETPACK_HOVER && jet.isFlying()) {
				JetpackTrail.addHoverParticles(
				  player, jet.getPropulsionVector(), new Vec3f(player.getDeltaMovement()));
				if (jet.updatePlayingSound(true))
					new JetpackSound(player).play();
			}
		}
	}
	
	/**
	 * Apply break speed modifier when flying in hover mode
	 */
	@SubscribeEvent
	public static void onBreakSpeed(BreakSpeed event) {
		final Player player = event.getEntity();
		if (player.onGround())
			return;
		IFlightData data = getFlightDataOrDefault(player);
		if (data.getFlightMode().is(JetpackFlightModeTags.HOVER) && !player.onGround()) {
			IElytraSpec spec = AerobaticElytraLogic.getElytraSpecOrDefault(player);
			final float hover_speed = spec.getAbility(JetpackAbilities.HOVER_MINING);
			event.setNewSpeed(max(event.getNewSpeed(), event.getOriginalSpeed() * 5F * hover_speed));
		}
	}
}
