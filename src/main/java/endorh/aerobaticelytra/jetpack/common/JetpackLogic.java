package endorh.aerobaticelytra.jetpack.common;

import endorh.aerobaticelytra.common.AerobaticElytraLogic;
import endorh.aerobaticelytra.common.capability.ElytraSpecCapability;
import endorh.aerobaticelytra.common.capability.IElytraSpec;
import endorh.aerobaticelytra.common.capability.IFlightData;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static endorh.aerobaticelytra.common.capability.FlightDataCapability.getFlightDataOrDefault;
import static endorh.aerobaticelytra.common.item.IAbility.Ability.FUEL;


public class JetpackLogic {
	public static boolean shouldJetpackFly(Player player) {
		IFlightData fd = getFlightDataOrDefault(player);
		if (!AerobaticElytraLogic.hasAerobaticElytra(player) ||
		    !fd.getFlightMode().is(JetpackFlightModeTags.JETPACK)
		    || player.getAbilities().flying)
			return false;
		ItemStack elytra = AerobaticElytraLogic.getAerobaticElytra(player);
		if (elytra.isEmpty())
			return false;
		IElytraSpec spec = ElytraSpecCapability.getElytraSpecOrDefault(elytra);
		if (player.isInWater() || player.isInLava())
			return false;
		if (player.isCreative())
			return true;
		if (elytra.getDamageValue() >= elytra.getMaxDamage() - 1 || spec.getAbility(FUEL) <= 0)
			return false;
		return true;
	}
	
	public static boolean canUseJetpack(Player player) {
		IFlightData fd = getFlightDataOrDefault(player);
		if (!AerobaticElytraLogic.hasAerobaticElytra(player)
		    || !fd.getFlightMode().is(JetpackFlightModeTags.JETPACK))
			return false;
		ItemStack elytra = AerobaticElytraLogic.getAerobaticElytra(player);
		if (elytra.isEmpty())
			return false;
		IElytraSpec spec = ElytraSpecCapability.getElytraSpecOrDefault(elytra);
		if (player.isCreative())
			return true;
		return (elytra.getDamageValue() < elytra.getMaxDamage() - 1 || spec.getAbility(FUEL) > 0)
		       || player.isCreative();
	}
}
