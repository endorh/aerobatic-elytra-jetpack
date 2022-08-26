package endorh.aerobaticelytra.jetpack.common.particle;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.aerobaticelytra.jetpack.client.particle.JetpackParticle;
import endorh.aerobaticelytra.jetpack.common.particle.JetpackParticleData.JetpackParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class ModParticles {
	public static JetpackParticleType JETPACK_PARTICLE;
	
	@SubscribeEvent
	public static void onParticleTypeRegistration(RegistryEvent.Register<ParticleType<?>> event) {
		final IForgeRegistry<ParticleType<?>> r = event.getRegistry();
		JETPACK_PARTICLE = reg(r, JetpackParticleType::new, "jetpack_particle");
		
		AerobaticJetpack.logRegistered("Particles");
	}
	
	private static <T extends ParticleType<?>> T reg(
	  IForgeRegistry<ParticleType<?>> registry, Supplier<T> constructor,
	  @SuppressWarnings("SameParameterValue") String name
	) {
		T particleType = constructor.get();
		particleType.setRegistryName(AerobaticJetpack.prefix(name));
		registry.register(particleType);
		return particleType;
	}
	
	@SubscribeEvent
	public static void onParticleFactoryRegistration(ParticleFactoryRegisterEvent event) {
		ParticleManager p = Minecraft.getInstance().particleEngine;
		p.register(JETPACK_PARTICLE, JetpackParticle.Factory::new);
		
		AerobaticJetpack.logRegistered("Particle Factories");
	}
}
