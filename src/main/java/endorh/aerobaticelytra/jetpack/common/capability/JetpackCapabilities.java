package endorh.aerobaticelytra.jetpack.common.capability;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class JetpackCapabilities {
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		JetpackDataCapability.register();
		AerobaticJetpack.logRegistered("Capabilities");
	}
}
