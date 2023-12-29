package endorh.aerobaticelytra.jetpack.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import endorh.aerobaticelytra.client.render.layer.AerobaticRenderData;
import endorh.aerobaticelytra.common.capability.IFlightData;
import endorh.aerobaticelytra.jetpack.common.JetpackLogic;
import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModeTags;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModes;
import endorh.lazulib.events.SetupRotationsRenderPlayerEvent;
import endorh.lazulib.animation.Easing;
import endorh.lazulib.common.ObfuscationReflectionUtil;
import endorh.lazulib.math.Vec3f;
import net.minecraft.client.Camera;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.WeakHashMap;

import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;
import static endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability.getJetpackDataOrDefault;

public class PlayerRendererHandler {
	private static final ObfuscationReflectionUtil.SoftField<WalkAnimationState, Float> WalkAnimationState$speedOld =
		ObfuscationReflectionUtil.getSoftField(WalkAnimationState.class, "f_267406_");

	public record WalkAnimationSpeedSnapshot(float speed, float speedOld) {
		public static WalkAnimationSpeedSnapshot from(WalkAnimationState state) {
			return new WalkAnimationSpeedSnapshot(state.speed(), state.speed(0F));
		}
		public void write(WalkAnimationState state) {
			state.setSpeed(speed);
			WalkAnimationState$speedOld.set(state, speedOld);
		}
	}

	private static void dimAnimationSpeed(WalkAnimationState state, float dimFactor) {
		state.setSpeed(state.speed() * dimFactor);
		WalkAnimationState$speedOld.set(state, state.speed(0F) * dimFactor);
	}

	private static final Map<Player, WalkAnimationSpeedSnapshot> animationSnapshots = new WeakHashMap<>(4);

	public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
		final Camera cam = event.getCamera();
		final Entity entity = cam.getEntity();
		if (entity instanceof LocalPlayer player) {
			final IFlightData fd = getFlightDataOrDefault(player);
			final IJetpackData jet = getJetpackDataOrDefault(player);
			if (
			  fd.getFlightMode().is(JetpackFlightModeTags.JETPACK) && jet.isFlying()
			  && player.isCrouching() && !cam.isDetached()
			) {}
		}
	}
	
	@SubscribeEvent public static void onComputeFov(ViewportEvent.ComputeFov event) {
		double fov = event.getFOV();
		Entity entity = event.getCamera().getEntity();
		if (entity instanceof Player player) {
			IJetpackData jet = getJetpackDataOrDefault(player);
			if (jet.isDashing()) {
				float p = jet.getDashProgress();
				fov *= 1F + p * (1F - p) * 0.25F;
				event.setFOV(fov);
			}
		}
	}
	
	/**
	 * Cancel player limb swing while flying with jetpack
	 */
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Pre event) {
		final PoseStack mStack = event.getPoseStack();
		mStack.pushPose();
		Player player = event.getEntity();
		if (player instanceof AbstractClientPlayer) {
			// Cancel limb swing
			AerobaticRenderData smoother = AerobaticRenderData.getAerobaticRenderData(player);
			IFlightData fd = getFlightDataOrDefault(player);
			IJetpackData jet = getJetpackDataOrDefault(player);
			float step = jet.isDashing()? 0.5F : (JetpackLogic.canUseJetpack(player) && (
			  fd.isFlightMode(JetpackFlightModes.JETPACK_HOVER) && !player.onGround()
			  || jet.isJumping())) ? 0.1F : -0.1F;
			
			float t = 1F - Easing.quadInOut(
			  smoother.cancelLimbSwingAmountProgress = Mth.clamp(
				 smoother.cancelLimbSwingAmountProgress + step, 0F, 1F));
			WalkAnimationState animationState = player.walkAnimation;
			animationSnapshots.put(player, WalkAnimationSpeedSnapshot.from(animationState));
			dimAnimationSpeed(animationState, t);
			
			if (fd.getFlightMode().is(JetpackFlightModeTags.JETPACK) && jet.isFlying()) {
				final PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
				if (jet.isFlying()) {
					if (player.isCrouching() && !player.isSleeping() && !player.isPassenger()) {
						player.setPose(Pose.STANDING);
						model.crouching = false;
						// Cancel PlayerRenderer#getRenderOffset
						mStack.translate(0D, 0.125D, 0D);
					}
				} else if (player.isCrouching()) {
					model.crouching = true;
				}
			}
		}
	}
	
	@SubscribeEvent public static void onRenderPlayerEvent(RenderPlayerEvent.Post event) {
		event.getPoseStack().popPose();
		Player player = event.getEntity();
		WalkAnimationSpeedSnapshot snapshot = animationSnapshots.remove(player);
		if (snapshot != null) snapshot.write(player.walkAnimation);
	}
	
	private static final Vec3f prop = Vec3f.ZERO.get();
	private static final Vec3f prev = Vec3f.ZERO.get();
	
	/**
	 * Tilt the player model when flying with jetpack
	 */
	@SubscribeEvent public static void onSetupRotationsRenderPlayerEvent(
	  SetupRotationsRenderPlayerEvent event
	) {
		IJetpackData jet = getJetpackDataOrDefault(event.player);
		if (JetpackLogic.shouldJetpackFly(event.player) && jet.isFlying()) {
			PoseStack mStack = event.matrixStack;
			Vec3f propVec = jet.getPropulsionVector();
			Vec3f prevPropVec = jet.getPrevPropulsionVector();
			if (propVec.normSquared() < 0.2F || prevPropVec.normSquared() < 0.2F)
				return;
			
			prev.set(prevPropVec);
			prop.set(propVec);
			if (jet.isDashing()) {
				setDashTilt(prop, jet.getDashDirection());
				if (jet.getDashStart() < event.player.tickCount) prev.set(prop);
			} else if (jet.getDashStart() + jet.getDashTicks() == event.player.tickCount) {
				setDashTilt(prev, jet.getDashDirection());
			}
			prev.mul(1F - event.partialTicks);
			prop.mul(event.partialTicks);
			prop.add(prev);
			float yaw = prop.getYaw();
			float pitch = 90F + prop.getPitch();
			mStack.mulPose(Axis.YP.rotationDegrees(-yaw));
			mStack.mulPose(Axis.XP.rotationDegrees(pitch));
			mStack.mulPose(Axis.YP.rotationDegrees(yaw));
		}
	}
	
	private static void setDashTilt(Vec3f vec, Vec3f dashDirection) {
		vec.set(dashDirection);
		vec.mul(0.4F);
		vec.y = 0.6F;
		vec.unitary();
	}
}
