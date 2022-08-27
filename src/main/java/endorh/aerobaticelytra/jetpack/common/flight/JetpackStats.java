package endorh.aerobaticelytra.jetpack.common.flight;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static endorh.aerobaticelytra.jetpack.AerobaticJetpack.prefix;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class JetpackStats {
	
	public static ResourceLocation JETPACK_FLIGHT_ONE_CM;
	public static ResourceLocation JETPACK_HOVER_ONE_SECOND;
	
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		register();
	}
	
	public static void register() {
		JETPACK_FLIGHT_ONE_CM =
		  reg("jetpack_flight_one_cm", StatFormatter.DISTANCE);
		JETPACK_HOVER_ONE_SECOND =
		  reg("jetpack_hover_one_second", StatFormatter.TIME);
		AerobaticJetpack.logRegistered("Stats");
	}
	
	private static ResourceLocation reg(
	  String key, StatFormatter formatter
	) {
		ResourceLocation location = prefix(key);
		Registry.register(Registry.CUSTOM_STAT, key, location);
		Stats.CUSTOM.get(location, formatter);
		return location;
	}
}
