package endorh.aerobaticelytra.jetpack.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import endorh.aerobaticelytra.client.render.layer.AerobaticRenderData;
import endorh.aerobaticelytra.common.capability.IFlightData;
import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.aerobaticelytra.jetpack.common.JetpackLogic;
import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModeTags;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModes;
import endorh.flightcore.events.SetupRotationsRenderPlayerEvent;
import endorh.util.math.Interpolator;
import endorh.util.math.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;

public class PlayerRendererHandler {
	public static void onCameraSetup(final CameraSetup event) {
		final Camera info = event.getInfo();
		final Entity entity = info.getEntity();
		if (entity instanceof LocalPlayer) {
			LocalPlayer player = (LocalPlayer) entity;
			final IFlightData fd = getFlightDataOrDefault(player);
			final IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(player);
			if (fd.getFlightMode().is(JetpackFlightModeTags.JETPACK) && jet.isFlying()
			    && player.isCrouching() && !info.isDetached()) {
			}
		}
	}
	
	/**
	 * Cancel player limb swing while flying with jetpack
	 */
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Pre event) {
		final PoseStack mStack = event.getMatrixStack();
		mStack.pushPose();
		Player player = event.getPlayer();
		if (player instanceof AbstractClientPlayer) {
			// Cancel limb swing
			AerobaticRenderData smoother = AerobaticRenderData.getAerobaticRenderData(player);
			IFlightData fd = getFlightDataOrDefault(player);
			IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(player);
			float step = (JetpackLogic.canUseJetpack(player) && (
			  fd.isFlightMode(JetpackFlightModes.JETPACK_HOVER) && !player.isOnGround()
			  || jet.isJumping())) ? 0.1F : -0.1F;
			
			float t = 1F - Interpolator.quadInOut(
			  smoother.cancelLimbSwingAmountProgress = Mth.clamp(
				 smoother.cancelLimbSwingAmountProgress + step, 0F, 1F));
			player.animationSpeed = t * player.animationSpeed;
			player.animationSpeedOld = t * player.animationSpeedOld;
			
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
		event.getMatrixStack().popPose();
	}
	
	private static final Vec3f prop = Vec3f.ZERO.get();
	private static final Vec3f prev = Vec3f.ZERO.get();
	
	/**
	 * Tilt the player model when flying with jetpack
	 */
	@SubscribeEvent public static void onSetupRotationsRenderPlayerEvent(
	  SetupRotationsRenderPlayerEvent event
	) {
		IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(event.player);
		if (JetpackLogic.shouldJetpackFly(event.player) && jet.isFlying()) {
			PoseStack mStack = event.matrixStack;
			Vec3f propVec = jet.getPropulsionVector();
			Vec3f prevPropVec = jet.getPrevPropulsionVector();
			if (propVec.normSquared() < 0.2F || prevPropVec.normSquared() < 0.2F)
				return;
			
			prev.set(prevPropVec);
			prop.set(propVec);
			prev.mul(1F - event.partialTicks);
			prop.mul(event.partialTicks);
			prop.add(prev);
			final float yaw = prop.getYaw();
			mStack.mulPose(Vector3f.YP.rotationDegrees(-yaw));
			mStack.mulPose(Vector3f.XP.rotationDegrees(90F + prop.getPitch()));
			mStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
		}
	}
}
