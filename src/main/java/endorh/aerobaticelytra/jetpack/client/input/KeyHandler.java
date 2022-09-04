package endorh.aerobaticelytra.jetpack.client.input;

import endorh.aerobaticelytra.client.render.AerobaticOverlays;
import endorh.aerobaticelytra.common.capability.IFlightData;
import endorh.aerobaticelytra.common.flight.mode.IFlightMode;
import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModeTags;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModes;
import endorh.aerobaticelytra.jetpack.network.JetpackPackets.DJetpackJumpingPacket;
import endorh.aerobaticelytra.jetpack.network.JetpackPackets.DJetpackSneakingPacket;
import endorh.aerobaticelytra.network.AerobaticPackets.DFlightModePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Type;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;
import static endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability.getJetpackDataOrDefault;
import static net.minecraftforge.client.settings.KeyConflictContext.IN_GAME;

@EventBusSubscriber(value = Dist.CLIENT, modid = AerobaticJetpack.MOD_ID)
@OnlyIn(Dist.CLIENT) public class KeyHandler {
	public static final String AEROBATIC_ELYTRA_CATEGORY = "key.aerobaticelytra.category";
	public static KeyBinding JETPACK_MODE;
	public static KeyBinding JETPACK_DASH;
	
	public static void register() {
		JETPACK_MODE = reg(
		  "key.aerobaticelytrajetpack.hover_mode.desc", IN_GAME,
		  GLFW.GLFW_KEY_V, AEROBATIC_ELYTRA_CATEGORY);
		JETPACK_DASH = reg(
		  "key.aerobaticelytrajetpack.dash.desc", IN_GAME,
		  GLFW.GLFW_KEY_X, AEROBATIC_ELYTRA_CATEGORY);
		AerobaticJetpack.logRegistered("Key Mappings");
	}
	
	@SuppressWarnings("SameParameterValue")
	private static KeyBinding reg(
	  String translation, IKeyConflictContext context, int keyCode, String category
	) {
		final KeyBinding binding = new KeyBinding(translation, context, Type.KEYSYM, keyCode, category);
		ClientRegistry.registerKeyBinding(binding);
		return binding;
	}
	
	@SubscribeEvent
	public static void onKey(InputEvent event) {
		final PlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return;
		final IFlightData fd = getFlightDataOrDefault(player);
		
		if (JETPACK_MODE.isPressed()) {
			IFlightMode mode = fd.getFlightMode().next(
			  m -> m.is(JetpackFlightModeTags.JETPACK));
			fd.setFlightMode(mode);
			new DFlightModePacket(mode).send();
			AerobaticOverlays.showModeToastIfRelevant(player, mode);
		}
		IJetpackData data = getJetpackDataOrDefault(player);
		if (JETPACK_DASH.isPressed()) {
			data.setDashKeyPressed(true);
		} else if (!JETPACK_DASH.isKeyDown()) {
			data.setDashKeyPressed(false);
		}
	}
	
	@SubscribeEvent
	public static void onInputUpdateEvent(InputUpdateEvent event) {
		final PlayerEntity player = event.getPlayer();
		final MovementInput movementInput = event.getMovementInput();
		final IFlightMode mode = getFlightDataOrDefault(player).getFlightMode();
		
		final IJetpackData jet = getJetpackDataOrDefault(player);
		if (jet.updateJumping(movementInput.jump))
			new DJetpackJumpingPacket(jet).send();
		if (mode == JetpackFlightModes.JETPACK_HOVER && jet.isFlying()
		    || mode == JetpackFlightModes.JETPACK_FLIGHT && jet.isJumping()) {
			if (jet.updateSneaking(movementInput.sneaking)) {
				new DJetpackSneakingPacket(jet).send();
			}
		}
	}
}
