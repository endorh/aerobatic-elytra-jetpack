package endorh.aerobaticelytra.jetpack.common.flight;

import endorh.aerobaticelytra.client.render.model.IElytraPose;
import endorh.aerobaticelytra.common.capability.IFlightData;
import endorh.aerobaticelytra.common.config.Const;
import endorh.aerobaticelytra.common.flight.mode.IFlightMode;
import endorh.aerobaticelytra.common.flight.mode.IFlightMode.IEnumFlightMode;
import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.aerobaticelytra.jetpack.client.render.model.AerobaticJetpackPoses;
import endorh.aerobaticelytra.jetpack.common.JetpackLogic;
import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;
import static endorh.aerobaticelytra.jetpack.AerobaticJetpack.prefix;
import static endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability.getJetpackDataOrDefault;

@EventBusSubscriber(bus = Bus.MOD, modid=AerobaticJetpack.MOD_ID)
public enum JetpackFlightModes implements IEnumFlightMode {
	JETPACK_FLIGHT(
	  false, -2000,
	  0, 0, JetpackFlight::onJetpackTravel, JetpackFlight::onOtherModeFlightTravel,
	  JetpackFlight::onRemoteJetpackTravel, null,
	  JetpackFlightModeTags.JETPACK, JetpackFlightModeTags.DASH),
	JETPACK_HOVER(
	  false, -1000,
	  Const.FLIGHT_MODE_TOAST_WIDTH, 0,
	  JetpackFlight::onJetpackTravel, JetpackFlight::onOtherModeFlightTravel,
	  JetpackFlight::onRemoteJetpackTravel, null,
	  JetpackFlightModeTags.JETPACK, JetpackFlightModeTags.HOVER, JetpackFlightModeTags.DASH);
	
	private final boolean shouldCycle;
	private final int order;
	private final int u;
	private final int v;
	
	private final Set<ResourceLocation> tags = new HashSet<>();
	
	private final BiPredicate<Player, Vec3> flightHandler;
	private final BiConsumer<Player, Vec3> nonFlightHandler;
	private final Consumer<Player> remoteFlightHandler;
	private final Consumer<Player> remoteNonFlightHandler;
	
	JetpackFlightModes(
	  boolean shouldCycle, int order, int u, int v,
	  BiPredicate<Player, Vec3> flightHandler,
	  @Nullable BiConsumer<Player, Vec3> nonFlightHandler,
	  @Nullable Consumer<Player> remoteFlightHandler,
	  @Nullable Consumer<Player> remoteNonFlightHandler,
	  ResourceLocation... tags
	) {
		this.shouldCycle = shouldCycle;
		this.order = order;
		this.flightHandler = flightHandler;
		this.nonFlightHandler = nonFlightHandler;
		this.remoteFlightHandler = remoteFlightHandler;
		this.remoteNonFlightHandler = remoteNonFlightHandler;
		this.u = u;
		this.v = v;
		Collections.addAll(this.tags, tags);
	}
	
	
	
	@Override public boolean shouldCycle() {
		return shouldCycle;
	}
	
	@Override public boolean is(ResourceLocation tag) {
		return tags.contains(tag);
	}
	
	@Override public int getRegistryOrder() {
		return order;
	}
	
	@Override public BiPredicate<Player, Vec3> getFlightHandler() {
		return flightHandler;
	}
	@Override public @Nullable BiConsumer<Player, Vec3> getNonFlightHandler() {
		return nonFlightHandler;
	}
	@Override public @Nullable Consumer<Player> getRemoteFlightHandler() {
		return remoteFlightHandler;
	}
	@Override public @Nullable Consumer<Player> getRemoteNonFlightHandler() {
		return remoteNonFlightHandler;
	}
	
	@Override public ResourceLocation getToastIconLocation() {
		return prefix("textures/gui/jetpack_icons.png");
	}
	
	@Override public int getToastIconU() { return u; }
	@Override public int getToastIconV() { return v; }
	
	@SubscribeEvent
	public static void onRegisterFlightModes(RegistryEvent.Register<IFlightMode> event) {
		final IForgeRegistry<IFlightMode> reg = event.getRegistry();
		reg.registerAll(JetpackFlightModes.values());
		AerobaticJetpack.logRegistered("Flight Modes");
	}
	
	@Override
	public IElytraPose getElytraPose(Player player) {
		IFlightData fd = getFlightDataOrDefault(player);
		final IJetpackData jet = getJetpackDataOrDefault(player);
		return !player.isOnGround() && JetpackLogic.canUseJetpack(player)
		       && (!player.isCrouching() || jet.isFlying())
		       ? AerobaticJetpackPoses.JETPACK_POSE : null;
	}
}
