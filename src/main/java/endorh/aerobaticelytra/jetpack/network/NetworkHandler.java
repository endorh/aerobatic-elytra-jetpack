package endorh.aerobaticelytra.jetpack.network;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.function.Supplier;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class NetworkHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
	  AerobaticJetpack.prefix("main"),
	  () -> PROTOCOL_VERSION,
	  PROTOCOL_VERSION::equals,
	  PROTOCOL_VERSION::equals
	);
	private static int ID_COUNT = 0;
	protected static Supplier<Integer> ID_GEN = () -> ID_COUNT++;
	
	// All packets must be registered in sequential order in both sides
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		JetpackPackets.registerAll();
		AerobaticJetpack.logRegistered("Packets");
	}
}
