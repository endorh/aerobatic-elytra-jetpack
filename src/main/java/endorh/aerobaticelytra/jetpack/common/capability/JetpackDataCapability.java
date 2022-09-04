package endorh.aerobaticelytra.jetpack.common.capability;


import endorh.aerobaticelytra.jetpack.AerobaticJetpack;
import endorh.util.capability.CapabilityProviderSerializable;
import endorh.util.math.Vec3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@EventBusSubscriber(modid = AerobaticJetpack.MOD_ID)
public class JetpackDataCapability {
	/**
	 * The {@link Capability} instance
	 */
	@SuppressWarnings("CanBeFinal")
	@CapabilityInject(IJetpackData.class)
	public static Capability<IJetpackData> CAPABILITY = null;
	private static final Storage storage = new Storage();
	public static final ResourceLocation ID =
	  AerobaticJetpack.prefix("jetpack_data");
	
	/**
	 * Registers the capability
	 */
	public static void register() {
		CapabilityManager.INSTANCE.register(
		  IJetpackData.class, storage, () -> new JetpackData(null));
	}
	
	/**
	 * Deserialize an {@link IJetpackData} from NBT
	 */
	public static IJetpackData fromNBT(CompoundNBT nbt) {
		IJetpackData data = new JetpackData(null);
		storage.readNBT(CAPABILITY, data, null, nbt);
		return data;
	}
	
	/**
	 * Serialize an {@link IJetpackData} to NBT
	 */
	public static CompoundNBT asNBT(IJetpackData data) {
		return (CompoundNBT) storage.writeNBT(CAPABILITY, data, null);
	}
	
	/**
	 * @return The {@link IJetpackData} from the player
	 * @throws IllegalStateException if the player doesn't have the capability
	 * @see JetpackDataCapability#getJetpackDataOrDefault
	 * @see JetpackDataCapability#getJetpackData
	 */
	public static IJetpackData demandJetpackData(PlayerEntity player) {
		assert CAPABILITY != null;
		return player.getCapability(CAPABILITY).orElseThrow(
		  () -> new IllegalStateException("Missing IJetpackData capability on player: " + player));
	}
	
	/**
	 * @return The {@link IJetpackData} from the player or a default
	 * if for some reason the player doesn't have the capability or it's
	 * invalid now
	 * @see JetpackDataCapability#getJetpackData
	 * @see JetpackDataCapability#demandJetpackData
	 */
	public static IJetpackData getJetpackDataOrDefault(PlayerEntity player) {
		assert CAPABILITY != null;
		return player.getCapability(CAPABILITY)
		  .orElse(new JetpackData(player));
	}
	
	/**
	 * @return The optional {@link IJetpackData} capability from the player
	 * @see JetpackDataCapability#getJetpackDataOrDefault
	 * @see JetpackDataCapability#demandJetpackData
	 */
	public static Optional<IJetpackData> getJetpackData(PlayerEntity player) {
		assert CAPABILITY != null;
		return player.getCapability(CAPABILITY).resolve();
	}
	
	/**
	 * Create a serializable provider for a player
	 */
	public static ICapabilitySerializable<INBT> createProvider(PlayerEntity player) {
		if (CAPABILITY == null)
			return null;
		return new CapabilityProviderSerializable<>(CAPABILITY, null, new JetpackData(player));
	}
	
	/**
	 * Attach the capability to a player
	 */
	@SubscribeEvent
	public static void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(ID, createProvider((PlayerEntity)event.getObject()));
		}
	}
	
	public static class JetpackData implements IJetpackData {
		@SuppressWarnings({"unused", "FieldCanBeLocal"})
		private final PlayerEntity player;
		
		private boolean isFlying = false;
		
		private float heat = 0F;
		private float hoverProp = 0F;
		private float fallSpeed = 0F;
		
		private final Vec3f propVec = Vec3f.YP.get();
		private final Vec3f prevPropVec = Vec3f.YP.get();
		
		private boolean sneaking = false;
		private boolean jumping = false;
		
		private int lastFlight = 0;
		private int lastGround = 0;
		
		private int lastDash = 0;
		private final Vec3f dashVec = Vec3f.ZERO.get();
		private final Vec3f dashDirection = Vec3f.ZERO.get();
		private int dashTicks = 1;
		private int dashCooldown = 0;
		private int consecutiveDashes = 0;
		private float dashProgress = 1F;
		private boolean dashKeyPressed = false;
		
		private boolean playingSound = false;
		
		public JetpackData(PlayerEntity player) {
			this.player = player;
		}
		
		@Override public boolean isFlying() {
			return isFlying;
		}
		@Override public void setFlying(boolean flying) {
			isFlying = flying;
		}
		
		@Override public float getHeat() {
			return heat;
		}
		@Override public float getHoverPropulsion() {
			return hoverProp;
		}
		@Override public void setHeat(float heat) {
			this.heat = heat;
		}
		@Override public void setHoverPropulsion(float prop) {
			hoverProp = prop;
		}
		
		@Override public float getFallSpeed() {
			return fallSpeed;
		}
		@Override public void setFallSpeed(float speed) {
			fallSpeed = speed;
		}
		
		@Override public int getLastFlight() {
			return lastFlight;
		}
		@Override public void setLastFlight(int tick) {
			lastFlight = tick;
		}
		
		@Override public int getLastGround() {
			return lastGround;
		}
		
		@Override public void setLastGround(int last) {
			lastGround = last;
		}
		
		@Override public Vec3f getPropulsionVector() {
			return propVec;
		}
		@Override public Vec3f getPrevPropulsionVector() {
			return prevPropVec;
		}
		
		@Override public boolean isSneaking() {
			return sneaking;
		}
		@Override public void setSneaking(boolean sneaking) {
			this.sneaking = sneaking;
		}
		@Override public boolean isJumping() {
			return jumping;
		}
		@Override public void setJumping(boolean jumping) {
			this.jumping = jumping;
		}
		
		@Override public boolean isDashing() {
			return getDashProgress() < 1F;
		}
		
		@Override public int getDashStart() {
			return lastDash;
		}
		@Override public float getDashProgress() {
			return dashProgress;
		}
		@Override public void setDashProgress(float progress) {
			dashProgress = progress;
		}
		
		@Override public Vec3f getDashVector() {
			return dashVec;
		}
		
		@Override public Vec3f getDashDirection() {
			return dashDirection;
		}
		
		@Override public boolean isDashKeyPressed() {
			return dashKeyPressed;
		}
		@Override public void setDashKeyPressed(boolean pressed) {
			dashKeyPressed = pressed;
		}
		
		@Override public void startDash(int tick, Vec3f dashVector, int ticks, int cooldown) {
			lastDash = tick;
			dashProgress = 0F;
			dashVec.set(dashVector);
			dashDirection.set(dashVector);
			dashDirection.unitary();
			dashTicks = ticks;
			dashCooldown = cooldown;
		}
		
		@Override public int getConsecutiveDashes() {
			return consecutiveDashes;
		}
		@Override public void setConsecutiveDashes(int dashes) {
			consecutiveDashes = dashes;
		}
		
		@Override public int getDashTicks() {
			return dashTicks;
		}
		@Override public int getDashCooldown() {
			return dashCooldown;
		}
		
		@Override public boolean updatePlayingSound(boolean playing) {
			if (this.playingSound != playing) {
				this.playingSound = playing;
				return true;
			}
			return false;
		}
	}
	
	public static class Storage implements IStorage<IJetpackData> {
		public static final String TAG_FLYING = "Flying";
		public static final String TAG_HEAT = "Heat";
		
		@Nullable @Override
		public INBT writeNBT(Capability<IJetpackData> cap, IJetpackData inst, Direction side) {
			CompoundNBT nbt = new CompoundNBT();
			
			nbt.putBoolean(TAG_FLYING, inst.isFlying());
			nbt.putFloat(TAG_HEAT, inst.getHeat());
			
			return nbt;
		}
		
		@Override
		public void readNBT(
		  Capability<IJetpackData> cap, IJetpackData inst, Direction side, INBT nbt
		) {
			CompoundNBT data = (CompoundNBT) nbt;
			
			inst.setFlying(data.getBoolean(TAG_FLYING));
			inst.setHeat(data.getFloat(TAG_HEAT));
		}
	}
}
