package endorh.aerobaticelytra.jetpack.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import endorh.lazulib.math.Vec3d;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DashParticleData implements ParticleOptions {
	public final Vec3d vector;
	public final int duration;
	public final int sustain;
	public final int fadeOut;
	public final boolean ownPlayer;
	
	public DashParticleData(
	  Vec3d vectorIn, int durationIn,   boolean ownPlayerIn
	) {
		this(vectorIn, durationIn, 15, 5, ownPlayerIn);
	}
	
	public DashParticleData(
	  Vec3d vectorIn, int durationIn, int sustainIn, int fadeOutIn, boolean ownPlayerIn
	) {
		vector = vectorIn;
		duration = durationIn;
		sustain = sustainIn;
		fadeOut = fadeOutIn;
		ownPlayer = ownPlayerIn;
	}
	
	@NotNull @Override
	public ParticleType<DashParticleData> getType() {
		return AerobaticJetpackParticles.DASH_PARTICLE;
	}
	
	@Override public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
		vector.write(buf);
		buf.writeInt(duration);
		buf.writeInt(sustain);
		buf.writeInt(fadeOut);
		buf.writeBoolean(ownPlayer);
	}
	
	@NotNull @Override public String writeToString() {
		return String.format(
		  Locale.ROOT, "%s %.2f %.2f %.2f %d %d %d %b",
		  ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()),
		  vector.x, vector.y, vector.z, duration, sustain, fadeOut, ownPlayer);
	}
	
	public static final Codec<DashParticleData> CODEC = RecordCodecBuilder.create(
	  instance -> instance.group(
		 Vec3d.CODEC.fieldOf("vector").forGetter(d -> d.vector),
		 Codec.INT.fieldOf("duration").forGetter(d -> d.duration),
		 Codec.INT.fieldOf("sustain").forGetter(d -> d.sustain),
		 Codec.INT.fieldOf("fadeOut").forGetter(d -> d.fadeOut),
		 Codec.BOOL.fieldOf("ownPlayer").forGetter(d -> d.ownPlayer)
	  ).apply(instance, DashParticleData::new));
	
	public static final Deserializer<DashParticleData> DESERIALIZER = new Deserializer<>() {
		@NotNull @Override
		public DashParticleData fromCommand(
		  @NotNull ParticleType<DashParticleData> type, @NotNull StringReader reader
		) throws CommandSyntaxException {
			reader.expect(' ');
			Vec3d vector = Vec3d.readCommand(reader);
			
			reader.expect(' ');
			int duration = reader.readInt();
			
			reader.expect(' ');
			int sustain = reader.readInt();
			
			reader.expect(' ');
			int fadeOut = reader.readInt();
			
			reader.expect(' ');
			boolean hide = reader.readBoolean();
			return new DashParticleData(vector, duration, sustain, fadeOut, hide);
		}
		
		@Override
		public @NotNull DashParticleData fromNetwork(
		  @NotNull ParticleType<DashParticleData> type, @NotNull FriendlyByteBuf buf
		) {
			Vec3d vector = Vec3d.read(buf);
			int duration = buf.readInt();
			int sustain = buf.readInt();
			int fadeOut = buf.readInt();
			boolean hide = buf.readBoolean();
			return new DashParticleData(vector, duration, sustain, fadeOut, hide);
		}
	};
	
	public static class DashParticleType extends ParticleType<DashParticleData> {
		private static final boolean alwaysShow = false;
		
		public DashParticleType() {
			this(alwaysShow);
		}
		
		public DashParticleType(boolean alwaysShow) {
			super(alwaysShow, DESERIALIZER);
		}
		
		@NotNull @Override public Codec<DashParticleData> codec() {
			return CODEC;
		}
	}
}
