package endorh.aerobaticelytra.jetpack.common.flight;

import endorh.aerobaticelytra.common.capability.IElytraSpec;
import endorh.aerobaticelytra.common.capability.IFlightData;
import endorh.aerobaticelytra.common.item.IAbility.Ability;
import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.aerobaticelytra.jetpack.client.config.ClientConfig;
import endorh.aerobaticelytra.jetpack.client.config.ClientConfig.sound;
import endorh.aerobaticelytra.jetpack.client.sound.JetpackSounds;
import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import endorh.aerobaticelytra.jetpack.common.config.Config.dash;
import endorh.aerobaticelytra.jetpack.common.item.JetpackAbilities;
import endorh.aerobaticelytra.jetpack.common.particle.DashParticleData;
import endorh.aerobaticelytra.jetpack.network.JetpackPackets.DJetpackDashPacket;
import endorh.flightcore.events.PlayerTravelEvent;
import endorh.flightcore.events.PlayerTravelEvent.RemotePlayerEntityTravelEvent;
import endorh.util.math.Vec3f;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static endorh.aerobaticelytra.common.AerobaticElytraLogic.getElytraSpecOrDefault;
import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;
import static endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability.getJetpackDataOrDefault;

@EventBusSubscriber(modid = AerobaticJetpack.MOD_ID)
public class JetpackDash {
	private static final Vec3f displacement = Vec3f.ZERO.get();
	
	@SubscribeEvent(receiveCanceled = true)
	public static void onPlayerTravel(PlayerTravelEvent event) {
		PlayerEntity player = event.player;
		dashTick(player);
		IJetpackData jet = getJetpackDataOrDefault(player);
		if (!jet.isDashing() && player.world.isRemote()) {
			if (jet.isDashKeyPressed()) {
				startDashFromDPad(player, event.travelVector);
				if (!ClientConfig.auto_repeat_dash)
					jet.setDashKeyPressed(false);
			}
		}
	}
	
	@SubscribeEvent @OnlyIn(Dist.CLIENT)
	public static void onRemoteTravel(RemotePlayerEntityTravelEvent event) {
		dashTick(event.player);
	}
	
	public static void dashTick(PlayerEntity player) {
		IJetpackData jet = getJetpackDataOrDefault(player);
		if (jet.isDashing()) {
			int tick = player.ticksExisted;
			int dashTick = tick - jet.getDashStart();
			int dashDuration = jet.getDashTicks();
			float dashProgress = MathHelper.clamp(dashTick / (float) dashDuration, 0F, 1F);
			float prevProgress = jet.getDashProgress();
			jet.setDashProgress(dashProgress);
			displacement.set(jet.getDashVector());
			displacement.mul(dashProgress - prevProgress);
			boolean prevCrouch = player.isSneaking();
			player.setSneaking(false);
			player.fallDistance = 0F;
			player.move(MoverType.SELF, displacement.toVector3d());
			player.setSneaking(prevCrouch);
			Vector3d speed = player.getMotion();
			player.setMotion(new Vector3d(speed.x, 0, speed.z));
			player.fallDistance = 0F;
		}
	}
	
	/**
	 * @param travelVector {@link Vector3d}(moveStrafing, moveVertical, moveForward)
	 */
	public static void startDashFromDPad(PlayerEntity player, Vector3d travelVector) {
		IJetpackData data = getJetpackDataOrDefault(player);
		Vec3f dir = Vec3f.ZERO.get();
		dir.x = Math.signum((float) travelVector.x);
		dir.y = (data.isJumping()? 1F : 0F) + (player.isCrouching()? -1F : 0F);
		dir.z = Math.signum((float) travelVector.z);
		if (dir.isZero()) {
			startDashFromView(player);
			return;
		} else dir.unitary();
		dir.rotateAlongVecDegrees(Vec3f.YN.get(), player.rotationYaw);
		startDash(player, dir);
	}
	
	public static void startDashFromView(PlayerEntity player) {
		startDash(player, new Vec3f(player.getLookVec()));
	}
	
	public static boolean startDash(PlayerEntity player, Vec3f direction) {
		Vec3f vec = direction.copy();
		if (!canStartDash(player)) return false;
		if (!canDashVertically(player)) vec.y = 0F;
		if (vec.isZero()) return false;
		IElytraSpec spec = getElytraSpecOrDefault(player);
		IJetpackData data = getJetpackDataOrDefault(player);
		float fuel = spec.getAbility(Ability.FUEL);
		float usage = getDashFuelUsage(player);
		if (fuel < usage) return false;
		if (player.world.isRemote()) {
			if (player instanceof ClientPlayerEntity)
				new DJetpackDashPacket(vec).send();
		}
		player.addStat(JetpackStats.JETPACK_DASHES, 1);
		spec.setAbility(Ability.FUEL, fuel - usage);
		vec.unitary();
		vec.mul(getDashDistance(player));
		if (data.getDashStart() + data.getDashTicks() + data.getDashCooldown() < player.ticksExisted) {
			data.setConsecutiveDashes(1);
		} else data.setConsecutiveDashes(data.getConsecutiveDashes() + 1);
		int duration = getDashDuration(player);
		int cooldown = getDashCooldown(player);
		data.startDash(player.ticksExisted, vec, duration, cooldown);
		if (player.world.isRemote()) {
			Vector3d center = player.getBoundingBox().getCenter();
			player.world.addParticle(
			  new DashParticleData(vec.toVec3d(), duration, !(player instanceof RemoteClientPlayerEntity)),
			  center.x, center.y, center.z, 0D, 0D, 0D);
			player.playSound(JetpackSounds.JETPACK_DASH, sound.dash, 1F);
		}
		return true;
	}
	
	public static boolean canStartDash(PlayerEntity player) {
		IFlightData data = getFlightDataOrDefault(player);
		IJetpackData jet = getJetpackDataOrDefault(player);
		return data.getFlightMode().is(JetpackFlightModeTags.DASH) && !jet.isDashing()
		       && getMaxDashes(player) > 0 && (
					jet.getConsecutiveDashes() < getMaxDashes(player)
					|| jet.getDashStart() + jet.getDashTicks() + jet.getDashCooldown() < player.ticksExisted
		       ) && (player.isOnGround()? canDashFromGround(player) : canDashFromAir(player));
	}
	
	public static int getMaxDashes(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		int extra = Math.round(spec.getAbility(JetpackAbilities.EXTRA_DASHES));
		return dash.base_max_consecutive + extra;
	}
	
	public static float getDashDistance(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		float extra = spec.getAbility(JetpackAbilities.DASH_DISTANCE);
		float distance = dash.base_distance + extra;
		if (player.isInWater() || player.isInLava()) {
			float waterSpeed = getWaterDashSpeed(player);
			distance *= waterSpeed;
		}
		return distance;
	}
	
	public static int getDashDuration(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		float speed = spec.getAbility(JetpackAbilities.DASH_SPEED);
		return Math.round(dash.base_duration_ticks / speed);
	}
	
	public static float getDashFuelUsage(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		float efficiency = spec.getAbility(JetpackAbilities.DASH_EFFICIENCY);
		return dash.base_fuel_usage / efficiency;
	}
	
	public static int getDashCooldown(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		float maneuverability = spec.getAbility(JetpackAbilities.DASH_MANEUVERABILITY);
		return Math.round(dash.base_cooldown_ticks / maneuverability);
	}
	
	public static boolean canDashVertically(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		return dash.base_vertical_dash || spec.getAbility(JetpackAbilities.VERTICAL_DASH) != 0F;
	}
	
	public static boolean canDashFromGround(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		return dash.base_ground_dash || spec.getAbility(JetpackAbilities.GROUND_DASH) != 0F;
	}
	
	public static boolean canDashFromAir(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		return dash.base_air_dash || spec.getAbility(JetpackAbilities.AIR_DASH) != 0F;
	}
	
	public static float getWaterDashSpeed(PlayerEntity player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		return spec.hasAbility(JetpackAbilities.WATER_DASH)
		       ? MathHelper.clamp(spec.getAbility(JetpackAbilities.WATER_DASH), 0F, 1F)
		       : dash.base_water_dash;
	}
	
	@SubscribeEvent public static void onFallDamage(LivingFallEvent event) {
		if (event.getEntity() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntity();
			if (getJetpackDataOrDefault(player).isDashing()) {
				player.fallDistance = 0F;
				event.setCanceled(true);
			}
		}
	}
}
