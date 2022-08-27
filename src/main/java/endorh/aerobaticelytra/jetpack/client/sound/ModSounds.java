package endorh.aerobaticelytra.jetpack.client.sound;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class ModSounds {
	public static SoundEvent JETPACK_FLIGHT;
	public static SoundEvent JETPACK_HOVER;
	
	@SubscribeEvent
	public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
		final IForgeRegistry<SoundEvent> r = event.getRegistry();
		JETPACK_FLIGHT = reg(r, AerobaticJetpack.prefix("jetpack.flight"));
		JETPACK_HOVER = reg(r, AerobaticJetpack.prefix("jetpack.hover"));
		
		AerobaticJetpack.logRegistered("Sounds");
	}
	
	public static SoundEvent reg(IForgeRegistry<SoundEvent> registry, ResourceLocation name) {
		SoundEvent event = new SoundEvent(name);
		event.setRegistryName(name);
		registry.register(event);
		return event;
	}
}
