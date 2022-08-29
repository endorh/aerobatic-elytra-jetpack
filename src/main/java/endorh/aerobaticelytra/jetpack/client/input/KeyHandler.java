package endorh.aerobaticelytra.jetpack.client.input;

import com.mojang.blaze3d.platform.InputConstants.Type;
import endorh.aerobaticelytra.client.render.AerobaticOverlays;
import endorh.aerobaticelytra.common.capability.IFlightData;
import endorh.aerobaticelytra.common.flight.mode.IFlightMode;
import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModeTags;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModes;
import endorh.aerobaticelytra.jetpack.network.JetpackPackets.DJetpackJumpingPacket;
import endorh.aerobaticelytra.jetpack.network.JetpackPackets.DJetpackSneakingPacket;
import endorh.aerobaticelytra.network.AerobaticPackets.DFlightModePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;

import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;
import static net.minecraftforge.client.settings.KeyConflictContext.IN_GAME;

@EventBusSubscriber(value = Dist.CLIENT, modid = AerobaticJetpack.MOD_ID)
@OnlyIn(Dist.CLIENT) public class KeyHandler {
	public static KeyMapping JETPACK_MODE_KEYBINDING;
	public static final String AEROBATIC_ELYTRA_CATEGORY = "key.aerobaticelytra.category";
	
	@EventBusSubscriber(value = Dist.CLIENT, modid = AerobaticJetpack.MOD_ID, bus = Bus.MOD)
	public static class Registrar {
		@SubscribeEvent public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
			JETPACK_MODE_KEYBINDING = reg(
			  event, "key.aerobaticelytrajetpack.hover_mode.desc", IN_GAME,
			  GLFW.GLFW_KEY_V, AEROBATIC_ELYTRA_CATEGORY);
			AerobaticJetpack.logRegistered("Key Mappings");
		}
	}
	
	@SuppressWarnings("SameParameterValue")
	private static KeyMapping reg(
	  RegisterKeyMappingsEvent event,
	  String translation, IKeyConflictContext context, int keyCode, String category
	) {
		final KeyMapping mapping = new KeyMapping(translation, context, Type.KEYSYM, keyCode, category);
		event.register(mapping);
		return mapping;
	}
	
	@SubscribeEvent
	public static void onKey(InputEvent.Key event) {
		final Player player = Minecraft.getInstance().player;
		if (player == null)
			return;
		final IFlightData fd = getFlightDataOrDefault(player);
		
		if (JETPACK_MODE_KEYBINDING.consumeClick()) {
			IFlightMode mode = fd.getFlightMode().next(
			  m -> m.is(JetpackFlightModeTags.JETPACK));
			fd.setFlightMode(mode);
			new DFlightModePacket(mode).send();
			AerobaticOverlays.showModeToastIfRelevant(player, mode);
		}
	}
	
	@SubscribeEvent
	public static void onInputUpdateEvent(MovementInputUpdateEvent event) {
		final Player player = event.getEntity();
		final Input movementInput = event.getInput();
		final IFlightMode mode = getFlightDataOrDefault(player).getFlightMode();
		
		final IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(player);
		if (mode.is(JetpackFlightModeTags.JETPACK) && !player.isOnGround()) {
			if (jet.updateJumping(movementInput.jumping))
				new DJetpackJumpingPacket(jet).send();
		}
		if (mode == JetpackFlightModes.JETPACK_HOVER && jet.isFlying()
		    || mode == JetpackFlightModes.JETPACK_FLIGHT && jet.isJumping()) {
			if (jet.updateSneaking(movementInput.shiftKeyDown)) {
				new DJetpackSneakingPacket(jet).send();
			}
		}
	}
}
