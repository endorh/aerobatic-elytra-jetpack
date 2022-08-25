package endorh.aerobaticelytra.jetpack.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;

@EventBusSubscriber(value = Dist.CLIENT, modid = AerobaticJetpack.MOD_ID)
public class PlayerRendererHandler {
	
	public static void onCameraSetup(final CameraSetup event) {
		final ActiveRenderInfo info = event.getInfo();
		final Entity entity = info.getRenderViewEntity();
		if (entity instanceof ClientPlayerEntity) {
			ClientPlayerEntity player = (ClientPlayerEntity) entity;
			final IFlightData fd = getFlightDataOrDefault(player);
			final IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(player);
			if (fd.getFlightMode().is(JetpackFlightModeTags.JETPACK) && jet.isFlying()
			    && player.isCrouching() && !info.isThirdPerson()) {
			}
		}
	}
	
	/**
	 * Cancel player limb swing while flying with jetpack
	 */
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Pre event) {
		final MatrixStack mStack = event.getMatrixStack();
		mStack.push();
		PlayerEntity player = event.getPlayer();
		if (player instanceof AbstractClientPlayerEntity) {
			// Cancel limb swing
			AerobaticRenderData smoother = AerobaticRenderData.getAerobaticRenderData(player);
			IFlightData fd = getFlightDataOrDefault(player);
			IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(player);
			float step = (JetpackLogic.canUseJetpack(player) && (
			  fd.isFlightMode(JetpackFlightModes.JETPACK_HOVER) && !player.isOnGround()
			  || jet.isJumping())) ? 0.1F : -0.1F;
			
			float t = 1F - Interpolator.quadInOut(
			  smoother.cancelLimbSwingAmountProgress = MathHelper.clamp(
				 smoother.cancelLimbSwingAmountProgress + step, 0F, 1F));
			player.limbSwingAmount = t * player.limbSwingAmount;
			player.prevLimbSwingAmount = t * player.prevLimbSwingAmount;
			
			if (fd.getFlightMode().is(JetpackFlightModeTags.JETPACK) && jet.isFlying()) {
				final PlayerModel<AbstractClientPlayerEntity> model = event.getRenderer().getEntityModel();
				if (jet.isFlying()) {
					if (player.isCrouching() && !player.isSleeping() && !player.isPassenger()) {
						player.setPose(Pose.STANDING);
						model.isSneak = false;
						// Cancel PlayerRenderer#getRenderOffset
						mStack.translate(0D, 0.125D, 0D);
					}
				} else if (player.isCrouching()) {
					model.isSneak = true;
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Post event) {
		event.getMatrixStack().pop();
	}
	
	private static final Vec3f prop = Vec3f.ZERO.get();
	private static final Vec3f prev = Vec3f.ZERO.get();
	
	/**
	 * Tilt the player model when flying with jetpack
	 */
	@SubscribeEvent
	public static void onApplyRotationsRenderPlayerEvent(
	  SetupRotationsRenderPlayerEvent event
	) {
		IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(event.player);
		if (JetpackLogic.shouldJetpackFly(event.player) && jet.isFlying()) {
			MatrixStack mStack = event.matrixStack;
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
			mStack.rotate(Vector3f.YP.rotationDegrees(-yaw));
			mStack.rotate(Vector3f.XP.rotationDegrees(90F + prop.getPitch()));
			mStack.rotate(Vector3f.YP.rotationDegrees(yaw));
		}
	}
}
