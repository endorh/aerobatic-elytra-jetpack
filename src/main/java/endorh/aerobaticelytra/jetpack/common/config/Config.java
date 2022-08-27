package endorh.aerobaticelytra.jetpack.common.config;

import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.simpleconfig.api.SimpleConfig.Type;
import endorh.simpleconfig.api.annotation.Bind;
import endorh.simpleconfig.api.entry.FloatEntryBuilder;
import net.minecraft.util.Mth;

import static endorh.simpleconfig.api.ConfigBuilderFactoryProxy.*;
import static endorh.util.math.Vec3f.TO_RAD;

public class Config {
	
	public static void register() {
		config(AerobaticJetpack.MOD_ID, Type.SERVER, Config.class)
		  .n(group("flight_modes")
			    .add("hover_flight_enabled", enable(true)))
		  .n(group("flight")
			    .add("tilt_range", number(20.0F, 180).add_field_scale("rad", TO_RAD))
			    .add("propulsion_base", tick(2.24F))
			    .add("propulsion_max", tick(2.8F))
			    .add("charge_time", number(2.0F).min(0).field("charge_per_tick", t -> 0.05F / t, Float.class))
			    .add("cooldown_time", number(4.0F).min(0).field("cooldown_per_tick", t -> 0.05F / t, Float.class))
			    .add("hover_horizontal_speed", tick(4.2F).min(0))
			    .add("hover_vertical_speed", tick(8.4F).min(0)))
		  .n(group("fuel")
			    .add("fuel_usage_linear", tick(0.04F))
			    .add("fuel_usage_quad", tick(0.0F))
			    .add("fuel_usage_sqrt", tick(0.04F))
			    .add("fuel_usage_hover", tick(0.01F)))
		  .n(group("height_penalty")
			    .add("min_height", number(128))
			    .add("max_height", number(192))
			    .add("penalty", number(0.7F)))
		  .n(group("network")
			    .add("allowed_extra_float_time", number(0.0F).min(0)
			      .field("allowed_extra_float_ticks", t -> {
						long ticks = (long) (t * 20F);
						return ticks == 0 && t != 0F? 1L : ticks;
					}, Long.class)))
		  .buildAndRegister();
	}
	
	private static FloatEntryBuilder tick(float value) {
		return number(value).add_field_scale("tick", 0.05F);
	}
	
	@Bind public static class flight_modes {
		@Bind public static boolean hover_flight_enabled = true;
	}
	
	@Bind public static class flight {
		@Bind public static float tilt_range_rad;
		@Bind public static float propulsion_base_tick;
		@Bind public static float propulsion_max_tick;
		@Bind public static float charge_per_tick;
		@Bind public static float cooldown_per_tick;
		@Bind public static float hover_horizontal_speed_tick;
		@Bind public static float hover_vertical_speed_tick;
		public static float horizontal_projection_range;
		
		static void bake() {
			horizontal_projection_range = Mth.sin(tilt_range_rad);
		}
	}
	
	@Bind public static class fuel {
		@Bind public static float fuel_usage_linear_tick;
		@Bind public static float fuel_usage_quad_tick;
		@Bind public static float fuel_usage_sqrt_tick;
		@Bind public static float fuel_usage_hover_tick;
	}
	
	@Bind public static class height_penalty {
		@Bind public static int min_height;
		@Bind public static int max_height;
		@Bind public static float penalty;
		public static int range;
		
		static void bake() {
			range = max_height - min_height;
		}
	}
	
	@Bind public static class network {
		@Bind public static long allowed_extra_float_ticks;
	}
}
