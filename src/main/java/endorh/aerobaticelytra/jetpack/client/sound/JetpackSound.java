package endorh.aerobaticelytra.jetpack.client.sound;

import endorh.aerobaticelytra.client.sound.FadingTickableSound;
import endorh.aerobaticelytra.common.flight.mode.IFlightMode;
import endorh.aerobaticelytra.jetpack.client.config.ClientConfig;
import endorh.aerobaticelytra.jetpack.common.JetpackLogic;
import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackFlightModes;
import endorh.util.sound.AudioUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class JetpackSound extends FadingTickableSound {
	
	private static final int FADE_IN = 3;
	private static final int FADE_OUT = 8;
	private static final int MIN_LEN = 0;
	private static final IAttenuation ATTENUATION = IAttenuation.exponential(96F);
	
	protected final IJetpackData jetpackData;
	private final PlayerTickableSubSound hover;
	private int crossFadeLeft = 0;
	private IFlightMode mode;
	private static final float crossFadeLen = 10F;
	
	public JetpackSound(Player player) {
		super(player, JetpackSounds.JETPACK_FLIGHT, SoundSource.PLAYERS,
		      FADE_IN, FADE_OUT, MIN_LEN, ATTENUATION);
		jetpackData = JetpackDataCapability.getJetpackDataOrDefault(player);
		hover = new PlayerTickableSubSound(
		  player, JetpackSounds.JETPACK_HOVER, SoundSource.PLAYERS, ATTENUATION);
		mode = flightData.getFlightMode();
	}
	
	@Override public boolean shouldFadeOut() {
		return !JetpackLogic.shouldJetpackFly(player) || !jetpackData.isFlying()
		       || flightData.isFlightMode(JetpackFlightModes.JETPACK_FLIGHT) && !jetpackData.isJumping();
	}
	
	@Override protected void onStart() {
		hover.play();
	}
	@Override protected void onFinish() {
		hover.finish();
	}
	@Override protected void onFadeOut() {
		jetpackData.updatePlayingSound(false);
	}
	
	@Override public void tick(float fade_factor) {
		float s = (float)player.getDeltaMovement().length();
		float vol = Mth.lerp(Mth.clamp(s, 0F, 0.5F), 0.5F, 1.0F) * fade_factor;
		if (!flightData.isFlightMode(mode)) {
			mode = flightData.getFlightMode();
			crossFadeLeft = (int)crossFadeLen - crossFadeLeft;
		}
		if (crossFadeLeft > 0)
			crossFadeLeft--;
		final boolean isHover = mode == JetpackFlightModes.JETPACK_HOVER;
		final float[] c = AudioUtil.crossFade((crossFadeLen - crossFadeLeft) / crossFadeLen);
		final float j = isHover? c[0] : c[1];
		final float h = isHover? c[1] : c[0];
		volume = j * vol * ClientConfig.sound.jetpack;
		hover.setVolume(h * vol * ClientConfig.sound.hover);
	}
}
