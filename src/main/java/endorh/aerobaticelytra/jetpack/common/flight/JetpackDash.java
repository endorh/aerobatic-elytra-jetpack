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
import endorh.lazulib.events.PlayerTravelEvent;
import endorh.lazulib.events.PlayerTravelEvent.RemotePlayerTravelEvent;
import endorh.lazulib.math.Vec3f;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
		Player player = event.player;
		dashTick(player);
		IJetpackData jet = getJetpackDataOrDefault(player);
		if (!jet.isDashing() && player.level().isClientSide()) {
			if (jet.isDashKeyPressed()) {
				startDashFromDPad(player, event.travelVector);
				if (!ClientConfig.auto_repeat_dash)
					jet.setDashKeyPressed(false);
			}
		}
	}
	
	@SubscribeEvent @OnlyIn(Dist.CLIENT)
	public static void onRemoteTravel(RemotePlayerTravelEvent event) {
		dashTick(event.player);
	}
	
	public static void dashTick(Player player) {
		IJetpackData jet = getJetpackDataOrDefault(player);
		if (jet.isDashing()) {
			int tick = player.tickCount;
			int dashTick = tick - jet.getDashStart();
			int dashDuration = jet.getDashTicks();
			float dashProgress = Mth.clamp(dashTick / (float) dashDuration, 0F, 1F);
			float prevProgress = jet.getDashProgress();
			jet.setDashProgress(dashProgress);
			displacement.set(jet.getDashVector());
			displacement.mul(dashProgress - prevProgress);
			boolean prevCrouch = player.isShiftKeyDown();
			player.setShiftKeyDown(false);
			player.resetFallDistance();
			player.move(MoverType.SELF, displacement.toVector3d());
			player.setShiftKeyDown(prevCrouch);
			Vec3 speed = player.getDeltaMovement();
			player.setDeltaMovement(new Vec3(speed.x, 0, speed.z));
			player.resetFallDistance();
		}
	}
	
	/**
	 * @param travelVector {@link Vec3}(moveStrafing, moveVertical, moveForward)
	 */
	public static void startDashFromDPad(Player player, Vec3 travelVector) {
		IJetpackData data = getJetpackDataOrDefault(player);
		Vec3f dir = Vec3f.ZERO.get();
		dir.x = Math.signum((float) travelVector.x);
		dir.y = (data.isJumping()? 1F : 0F) + (player.isCrouching()? -1F : 0F);
		dir.z = Math.signum((float) travelVector.z);
		if (dir.isZero()) {
			startDashFromView(player);
			return;
		} else dir.unitary();
		dir.rotateAlongVecDegrees(Vec3f.YN.get(), player.getYRot());
		startDash(player, dir);
	}
	
	public static void startDashFromView(Player player) {
		startDash(player, new Vec3f(player.getViewVector(1F)));
	}
	
	public static boolean startDash(Player player, Vec3f direction) {
		Vec3f vec = direction.copy();
		if (!canStartDash(player)) return false;
		if (!canDashVertically(player)) vec.y = 0F;
		if (vec.isZero()) return false;
		IElytraSpec spec = getElytraSpecOrDefault(player);
		IJetpackData data = getJetpackDataOrDefault(player);
		float fuel = spec.getAbility(Ability.FUEL);
		float usage = getDashFuelUsage(player);
		if (fuel < usage) return false;
		if (player.level().isClientSide()) {
			if (player instanceof LocalPlayer)
				new DJetpackDashPacket(vec).send();
		}
		player.awardStat(JetpackStats.JETPACK_DASHES, 1);
		spec.setAbility(Ability.FUEL, fuel - usage);
		vec.unitary();
		vec.mul(getDashDistance(player));
		if (data.getDashStart() + data.getDashTicks() + data.getDashCooldown() < player.tickCount) {
			data.setConsecutiveDashes(1);
		} else data.setConsecutiveDashes(data.getConsecutiveDashes() + 1);
		int duration = getDashDuration(player);
		int cooldown = getDashCooldown(player);
		data.startDash(player.tickCount, vec, duration, cooldown);
		if (player.level().isClientSide()) {
			Vec3 center = player.getBoundingBox().getCenter();
			player.level().addParticle(
			  new DashParticleData(vec.toVec3d(), duration, !(player instanceof RemotePlayer)),
			  center.x, center.y, center.z, 0D, 0D, 0D);
			player.playSound(JetpackSounds.JETPACK_DASH, sound.dash, 1F);
		}
		return true;
	}
	
	public static boolean canStartDash(Player player) {
		IFlightData data = getFlightDataOrDefault(player);
		IJetpackData jet = getJetpackDataOrDefault(player);
		return data.getFlightMode().is(JetpackFlightModeTags.DASH) && !jet.isDashing()
		       && getMaxDashes(player) > 0 && (
					jet.getConsecutiveDashes() < getMaxDashes(player)
					|| jet.getDashStart() + jet.getDashTicks() + jet.getDashCooldown() < player.tickCount
		       ) && (player.onGround()? canDashFromGround(player) : canDashFromAir(player));
	}
	
	public static int getMaxDashes(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		int extra = Math.round(spec.getAbility(JetpackAbilities.EXTRA_DASHES));
		return dash.base_max_consecutive + extra;
	}
	
	public static float getDashDistance(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		float extra = spec.getAbility(JetpackAbilities.DASH_DISTANCE);
		float distance = dash.base_distance + extra;
		if (player.isInWater() || player.isInLava() || player.isInPowderSnow) {
			float waterSpeed = getWaterDashSpeed(player);
			distance *= waterSpeed;
		}
		return distance;
	}
	
	public static int getDashDuration(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		float speed = spec.getAbility(JetpackAbilities.DASH_SPEED);
		return Math.round(dash.base_duration_ticks / speed);
	}
	
	public static float getDashFuelUsage(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		float efficiency = spec.getAbility(JetpackAbilities.DASH_EFFICIENCY);
		return dash.base_fuel_usage / efficiency;
	}
	
	public static int getDashCooldown(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		float maneuverability = spec.getAbility(JetpackAbilities.DASH_MANEUVERABILITY);
		return Math.round(dash.base_cooldown_ticks / maneuverability);
	}
	
	public static boolean canDashVertically(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		return dash.base_vertical_dash || spec.getAbility(JetpackAbilities.VERTICAL_DASH) != 0F;
	}
	
	public static boolean canDashFromGround(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		return dash.base_ground_dash || spec.getAbility(JetpackAbilities.GROUND_DASH) != 0F;
	}
	
	public static boolean canDashFromAir(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		return dash.base_air_dash || spec.getAbility(JetpackAbilities.AIR_DASH) != 0F;
	}
	
	public static float getWaterDashSpeed(Player player) {
		IElytraSpec spec = getElytraSpecOrDefault(player);
		return spec.hasAbility(JetpackAbilities.WATER_DASH)
		       ? Mth.clamp(spec.getAbility(JetpackAbilities.WATER_DASH), 0F, 1F)
		       : dash.base_water_dash;
	}
	
	@SubscribeEvent public static void onFallDamage(LivingFallEvent event) {
		if (event.getEntity() instanceof Player player) {
			if (getJetpackDataOrDefault(player).isDashing()) {
				player.resetFallDistance();
				event.setCanceled(true);
			}
		}
	}
}
