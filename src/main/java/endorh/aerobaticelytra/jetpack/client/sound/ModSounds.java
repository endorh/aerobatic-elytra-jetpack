package endorh.aerobaticelytra.jetpack.client.sound;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;

import static endorh.aerobaticelytra.jetpack.AerobaticJetpack.prefix;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class ModSounds {
	public static SoundEvent JETPACK_FLIGHT;
	public static SoundEvent JETPACK_HOVER;
	public static SoundEvent JETPACK_DASH;
	
	@SubscribeEvent
	public static void onRegisterSounds(RegisterEvent event) {
		event.register(ForgeRegistries.SOUND_EVENTS.getRegistryKey(), r -> {
			JETPACK_FLIGHT = reg(r, prefix("jetpack.flight"));
			JETPACK_HOVER = reg(r, prefix("jetpack.hover"));
			JETPACK_DASH = reg(r, prefix("jetpack.dash"));
			AerobaticJetpack.logRegistered("Sounds");
		});
	}
	
	public static SoundEvent reg(RegisterHelper<SoundEvent> registry, ResourceLocation name) {
		SoundEvent event = new SoundEvent(name);
		registry.register(name, event);
		return event;
	}
}
