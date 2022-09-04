package endorh.aerobaticelytra.jetpack.common.item;

import com.google.common.base.CaseFormat;
import endorh.aerobaticelytra.common.item.IAbility;
import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.Optional;

import static endorh.aerobaticelytra.common.item.IAbility.DisplayType.*;
import static endorh.util.text.TextUtil.ttc;
import static net.minecraft.util.text.TextFormatting.*;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public enum JetpackAbilities implements IAbility {
	JETPACK(DARK_RED, 1F, SCALE_BOOL),
	HOVER(GRAY, 1F, SCALE_BOOL),
	HOVER_MINING(GRAY, 0.2F, new DisplayType() {
		@Override public
		Optional<IFormattableTextComponent> format(IAbility ability, float value) {
			return value != 0.2F? SCALE.format(ability, value) : Optional.empty();
		}
	}),
	DASH_DISTANCE(AQUA, 1F, SCALE_NON_ONE),
	DASH_SPEED(AQUA, 1F, SCALE_NON_ONE),
	DASH_EFFICIENCY(AQUA, 1F, SCALE_NON_ONE),
	DASH_MANEUVERABILITY(AQUA, 1F, SCALE_NON_ONE),
	VERTICAL_DASH(AQUA, 0F, BOOL),
	GROUND_DASH(AQUA, 0F, BOOL),
	AIR_DASH(AQUA, 0F, BOOL),
	WATER_DASH(AQUA, 0F, filter(
	  v -> v != 0, SCALE_NON_ONE, HIDE)),
	EXTRA_DASHES(AQUA, 0F, INTEGER_SUM_NON_ZERO);
	
	private final ResourceLocation registryName;
	private final String jsonName;
	private final String translationKey;
	private final TextFormatting color;
	private final float defaultValue;
	private final DisplayType displayType;
	
	JetpackAbilities(TextFormatting color, float defaultValue, DisplayType type) {
		this.registryName = new ResourceLocation(AerobaticJetpack.MOD_ID, name().toLowerCase());
		this.jsonName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
		this.translationKey = AerobaticJetpack.MOD_ID + ".abilities." + name().toLowerCase();
		this.color = color;
		this.defaultValue = defaultValue;
		this.displayType = type;
	}
	
	@Override public String getName() { return jsonName; }
	@Override public IFormattableTextComponent getDisplayName() { return ttc(translationKey); }
	@Override public TextFormatting getColor() { return color; }
	@Override public float getDefault() { return defaultValue; }
	@Override public DisplayType getDisplayType() { return displayType; }
	
	@Override public ResourceLocation getRegistryName() { return registryName; }
	@Override public Class<IAbility> getRegistryType() { return IAbility.class; }
	@Override public IAbility setRegistryName(ResourceLocation name) {
		throw new IllegalStateException("Cannot set registry name of enum registry entry");
	}
	
	@SubscribeEvent
	public static void onRegisterAbilities(RegistryEvent.Register<IAbility> event) {
		event.getRegistry().registerAll(values());
		AerobaticJetpack.logRegistered("Abilities");
	}
}
