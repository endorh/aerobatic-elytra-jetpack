package endorh.aerobaticelytra.jetpack.common.capability;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class JetpackCapabilities {
	@SubscribeEvent
	public static void onCommonSetup(RegisterCapabilitiesEvent event) {
		event.register(IJetpackData.class);
		AerobaticJetpack.logRegistered("Capabilities");
	}
}
