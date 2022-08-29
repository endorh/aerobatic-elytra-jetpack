package endorh.aerobaticelytra.jetpack.common.particle;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.aerobaticelytra.jetpack.client.particle.JetpackParticle;
import endorh.aerobaticelytra.jetpack.common.particle.JetpackParticleData.JetpackParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;

import java.util.function.Supplier;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class ModParticles {
	public static JetpackParticleType JETPACK_PARTICLE;
	
	@SubscribeEvent
	public static void onParticleTypeRegistration(RegisterEvent event) {
		event.register(ForgeRegistries.PARTICLE_TYPES.getRegistryKey(), r -> {
			JETPACK_PARTICLE = reg(r, JetpackParticleType::new, "jetpack_particle");
			AerobaticJetpack.logRegistered("Particles");
		});
	}
	
	private static <T extends ParticleType<?>> T reg(
	  RegisterHelper<ParticleType<?>> r, Supplier<T> constructor, String name
	) {
		T particleType = constructor.get();
		r.register(name, particleType);
		return particleType;
	}
	
	@SubscribeEvent
	public static void onParticleFactoryRegistration(RegisterParticleProvidersEvent event) {
		event.register(JETPACK_PARTICLE, JetpackParticle.Factory::new);
		AerobaticJetpack.logRegistered("Particle Factories");
	}
}
