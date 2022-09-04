package endorh.aerobaticelytra.jetpack.client.sound;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;

import static endorh.aerobaticelytra.jetpack.AerobaticJetpack.prefix;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class JetpackSounds {
	public static SoundEvent JETPACK_FLIGHT;
	public static SoundEvent JETPACK_HOVER;
	public static SoundEvent JETPACK_DASH;
	
	@SubscribeEvent
	public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
		final IForgeRegistry<SoundEvent> r = event.getRegistry();
		JETPACK_FLIGHT = reg(r, prefix("jetpack.flight"));
		JETPACK_HOVER = reg(r, prefix("jetpack.hover"));
		JETPACK_DASH = reg(r, prefix("jetpack.dash"));
		AerobaticJetpack.logRegistered("Sounds");
	}
	
	public static SoundEvent reg(IForgeRegistry<SoundEvent> registry, ResourceLocation name) {
		SoundEvent event = new SoundEvent(name);
		event.setRegistryName(name);
		registry.register(event);
		return event;
	}
}
