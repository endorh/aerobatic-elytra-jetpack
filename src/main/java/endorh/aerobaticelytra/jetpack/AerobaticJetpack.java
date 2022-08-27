package endorh.aerobaticelytra.jetpack;

import endorh.aerobaticelytra.jetpack.client.config.ClientConfig;
import endorh.aerobaticelytra.jetpack.client.input.KeyHandler;
import endorh.aerobaticelytra.jetpack.client.render.PlayerRendererHandler;
import endorh.aerobaticelytra.jetpack.common.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Temporarily here to do the extraction
 */
@Mod(AerobaticJetpack.MOD_ID)
@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class AerobaticJetpack {
	public static final String MOD_ID = "aerobaticelytrajetpack";
	
	protected static final Logger LOGGER = LogManager.getLogger();
	protected static final Marker MAIN = MarkerManager.getMarker("MAIN");
	protected static final Marker REGISTER = MarkerManager.getMarker("REGISTER");
	
	public AerobaticJetpack() {
		Config.register();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			ClientConfig.register();
			MinecraftForge.EVENT_BUS.register(PlayerRendererHandler.class);
		});
		LOGGER.debug(MAIN, "Mod loading started");
	}
	
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		KeyHandler.register();
	}
	
	public static void logRegistered(String kind) {
		LOGGER.debug(REGISTER, "Registered " + kind);
	}
	
	public static ResourceLocation prefix(String key) {
		return new ResourceLocation(MOD_ID, key);
	}
}
