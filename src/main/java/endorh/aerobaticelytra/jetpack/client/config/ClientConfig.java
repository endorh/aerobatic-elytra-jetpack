package endorh.aerobaticelytra.jetpack.client.config;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.simpleconfig.api.SimpleConfig.Type;
import endorh.simpleconfig.api.annotation.Bind;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import static endorh.simpleconfig.api.ConfigBuilderFactoryProxy.*;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public class ClientConfig {
	public static boolean disable_hover_when_landing;
	
	public static void register() {
		config(AerobaticJetpack.MOD_ID, Type.CLIENT, ClientConfig.class)
		  .withBackground("textures/block/light_blue_shulker_box.png")
		  .add("disable_hover_when_landing", yesNo(false))
		  .n(group("sound")
		       .caption("master", volume(1F))
		       .add("jetpack", volume(1F))
		       .add("hover", volume(1F)))
		  .buildAndRegister();
	}
	
	@Bind public static class sound {
		@Bind public static float master;
		@Bind public static float jetpack;
		@Bind public static float hover;
		
		static void bake() {
			jetpack *= master;
			hover *= master;
		}
	}
}
