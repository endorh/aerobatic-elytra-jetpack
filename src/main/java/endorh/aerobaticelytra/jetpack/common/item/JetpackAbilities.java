package endorh.aerobaticelytra.jetpack.common.item;

import com.google.common.base.CaseFormat;
import endorh.aerobaticelytra.common.item.IAbility;
import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.Optional;

import static endorh.aerobaticelytra.common.item.IAbility.DisplayType.SCALE_BOOL;
import static endorh.util.text.TextUtil.ttc;

@EventBusSubscriber(bus = Bus.MOD, modid = AerobaticJetpack.MOD_ID)
public enum JetpackAbilities implements IAbility {
	JETPACK(ChatFormatting.DARK_RED, 1F, SCALE_BOOL),
	HOVER(ChatFormatting.GRAY, 1F, SCALE_BOOL),
	HOVER_MINING(ChatFormatting.GRAY, 0.2F, new DisplayType() {
		@Override public Optional<MutableComponent> format(IAbility ability, float value) {
			return value != 0.2F? SCALE.format(ability, value) : Optional.empty();
		}
	});
	
	private final ResourceLocation registryName;
	private final String jsonName;
	private final String translationKey;
	private final ChatFormatting color;
	private final float defaultValue;
	private final DisplayType displayType;
	
	JetpackAbilities(ChatFormatting color, float defaultValue, DisplayType type) {
		this.registryName = new ResourceLocation(AerobaticJetpack.MOD_ID, name().toLowerCase());
		this.jsonName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
		this.translationKey = AerobaticJetpack.MOD_ID + ".abilities." + name().toLowerCase();
		this.color = color;
		this.defaultValue = defaultValue;
		this.displayType = type;
	}
	
	@Override public String getName() { return jsonName; }
	@Override public MutableComponent getDisplayName() { return ttc(translationKey); }
	@Override public ChatFormatting getColor() { return color; }
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
