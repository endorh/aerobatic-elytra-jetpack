package endorh.aerobaticelytra.jetpack.network;

import endorh.aerobaticelytra.jetpack.common.capability.IJetpackData;
import endorh.aerobaticelytra.jetpack.common.capability.JetpackDataCapability;
import endorh.aerobaticelytra.jetpack.common.config.Config;
import endorh.aerobaticelytra.jetpack.common.flight.JetpackDash;
import endorh.util.math.Vec3f;
import endorh.util.network.DistributedPlayerPacket;
import endorh.util.network.ServerPlayerPacket;
import endorh.util.network.ValidatedDistributedPlayerPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class JetpackPackets {
	protected static void registerAll() {
		DistributedPlayerPacket.with(NetworkHandler.CHANNEL, NetworkHandler.ID_GEN)
		  .registerLocal(DJetpackSneakingPacket::new)
		  .registerLocal(DJetpackJumpingPacket::new)
		  .registerLocal(DJetpackPropulsionVectorPacket::new)
		  .registerLocal(DJetpackDashPacket::new);
		ServerPlayerPacket.with(NetworkHandler.CHANNEL, NetworkHandler.ID_GEN)
		  .register(SJetpackFlyingPacket::new)
		  .register(SJetpackMotionPacket::new);
	}
	
	/**
	 * Input update packet<br>
	 * Sent when the player sneaking input changes
	 */
	public static class DJetpackSneakingPacket extends DistributedPlayerPacket {
		boolean sneaking;
		public DJetpackSneakingPacket() {}
		public DJetpackSneakingPacket(IJetpackData data) {
			sneaking = data.isSneaking();
		}
		
		@Override public void onCommon(PlayerEntity sender, Context ctx) {
			IJetpackData target = JetpackDataCapability.getJetpackDataOrDefault(sender);
			target.setSneaking(sneaking);
		}
		@Override public void serialize(PacketBuffer buf) {
			buf.writeBoolean(sneaking);
		}
		@Override public void deserialize(PacketBuffer buf) {
			sneaking = buf.readBoolean();
		}
	}
	
	/**
	 * Input update packet<br>
	 * Sent when the player jumping input changes
	 */
	public static class DJetpackJumpingPacket extends DistributedPlayerPacket {
		boolean jumping;
		public DJetpackJumpingPacket() {}
		public DJetpackJumpingPacket(IJetpackData data) {
			jumping = data.isJumping();
		}
		
		@Override public void onCommon(PlayerEntity sender, Context ctx) {
			IJetpackData target = JetpackDataCapability.getJetpackDataOrDefault(sender);
			target.setJumping(jumping);
		}
		@Override public void serialize(PacketBuffer buf) {
			buf.writeBoolean(jumping);
		}
		@Override public void deserialize(PacketBuffer buf) {
			jumping = buf.readBoolean();
		}
	}
	
	public static class DJetpackPropulsionVectorPacket extends ValidatedDistributedPlayerPacket {
		Vec3f propVec;
		public DJetpackPropulsionVectorPacket() {}
		public DJetpackPropulsionVectorPacket(IJetpackData data) {
			propVec = data.getPropulsionVector();
		}
		
		@Override public void onServer(PlayerEntity sender, Context ctx) {
			IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(sender);
			validateClamp(propVec.x,
			              -Config.flight.horizontal_projection_range - 0.01F,
			              Config.flight.horizontal_projection_range + 0.01F);
			validateClamp(propVec.z,
			              -Config.flight.horizontal_projection_range - 0.01F,
			              Config.flight.horizontal_projection_range + 0.01F);
			if (isInvalid()) {
				propVec.set(jet.getPropulsionVector());
			} else jet.getPropulsionVector().set(propVec);
		}
		@Override public void onClient(PlayerEntity target, Context ctx) {
			IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(target);
			jet.getPropulsionVector().set(propVec);
		}
		@Override public void serialize(PacketBuffer buf) {
			propVec.write(buf);
		}
		@Override public void deserialize(PacketBuffer buf) {
			propVec = Vec3f.read(buf);
		}
	}
	
	public static class DJetpackDashPacket extends ValidatedDistributedPlayerPacket {
		Vec3f vector;
		
		public DJetpackDashPacket() {}
		public DJetpackDashPacket(Vec3f vector) {
			this.vector = vector;
		}
		
		@Override protected void onServer(PlayerEntity sender, Context ctx) {
			if (!JetpackDash.startDash(sender, vector)) invalidate();
		}
		@Override protected void onClient(PlayerEntity sender, Context ctx) {
			JetpackDash.startDash(sender, vector);
		}
		
		@Override protected void serialize(PacketBuffer buf) {
			vector.write(buf);
		}
		@Override protected void deserialize(PacketBuffer buf) {
			vector = Vec3f.read(buf);
		}
	}
	
	public static class SJetpackMotionPacket extends ServerPlayerPacket {
		private Vec3f motion;
		public SJetpackMotionPacket() {}
		public SJetpackMotionPacket(PlayerEntity player) {
			super(player);
			motion = new Vec3f(player.getMotion());
		}
		@Override protected void onClient(PlayerEntity player, Context ctx) {
			player.setMotion(motion.toVector3d());
		}
		@Override protected void serialize(PacketBuffer buf) {
			motion.write(buf);
		}
		@Override protected void deserialize(PacketBuffer buf) {
			motion = Vec3f.read(buf);
		}
	}
	
	public static class SJetpackFlyingPacket extends ServerPlayerPacket {
		boolean flying;
		private SJetpackFlyingPacket() {}
		public SJetpackFlyingPacket(PlayerEntity player, IJetpackData data) {
			super(player);
			flying = data.isFlying();
		}
		@Override public void onClient(PlayerEntity player, Context ctx) {
			IJetpackData jet = JetpackDataCapability.getJetpackDataOrDefault(player);
			jet.setFlying(flying);
		}
		@Override public void serialize(PacketBuffer buf) {
			buf.writeBoolean(flying);
		}
		@Override public void deserialize(PacketBuffer buf) {
			flying = buf.readBoolean();
		}
	}
}
