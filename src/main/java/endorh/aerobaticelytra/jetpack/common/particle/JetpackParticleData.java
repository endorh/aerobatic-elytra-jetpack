package endorh.aerobaticelytra.jetpack.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.Locale;

import net.minecraft.core.particles.ParticleOptions.Deserializer;

public class JetpackParticleData implements ParticleOptions {
	
	public final Color tint;
	public final float size;
	public final int life;
	public final boolean ownPlayer;
	
	@NotNull @Override
	public ParticleType<JetpackParticleData> getType() {
		return ModParticles.JETPACK_PARTICLE;
	}
	
	@Override public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
		buf.writeInt(tint.getRGB());
		buf.writeInt(life);
		buf.writeFloat(size);
		buf.writeBoolean(ownPlayer);
	}
	
	@NotNull @Override public String writeToString() {
		return String.format(
		  Locale.ROOT, "%s %.2f %d %d %d %b",
		  ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()), size,
		  tint.getRed(), tint.getGreen(), tint.getBlue(), ownPlayer);
	}
	
	public static final Codec<JetpackParticleData> CODEC = RecordCodecBuilder.create(
	  instance -> instance.group(
		 Codec.INT.fieldOf("tint").forGetter(d -> d.tint.getRGB()),
		 Codec.INT.fieldOf("life").forGetter(d -> d.life),
		 Codec.FLOAT.fieldOf("size").forGetter(d -> d.size),
		 Codec.BOOL.fieldOf("ownPlayer").forGetter(d -> d.ownPlayer)
	  ).apply(instance, JetpackParticleData::new)
	);
	
	private JetpackParticleData(int tintRGB, int lifeIn, float sizeIn, boolean ownPlayer) {
		this(new Color(tintRGB), lifeIn, sizeIn, ownPlayer);
	}
	public JetpackParticleData(Color tintIn, int lifeIn, float sizeIn, boolean ownPlayer) {
		tint = tintIn;
		life = lifeIn;
		size = Mth.clamp(sizeIn, 0F, 1F);
		this.ownPlayer = ownPlayer;
	}
	public JetpackParticleData(int lifeIn, float sizeIn, boolean ownPlayer) {
		this(Color.WHITE, lifeIn, sizeIn, ownPlayer);
	}
	
	@SuppressWarnings("deprecation")
	public static final Deserializer<JetpackParticleData> DESERIALIZER =
	  new Deserializer<JetpackParticleData>() {
		@NotNull @Override
		public JetpackParticleData fromCommand(
		  @NotNull ParticleType<JetpackParticleData> type, @NotNull StringReader reader
		) throws CommandSyntaxException {
			reader.expect(' ');
			float size = Mth.clamp(reader.readFloat(), 0F, 1F);
			
			reader.expect(' ');
			int life = reader.readInt();
			
			reader.expect(' ');
			int red = reader.readInt() & 0xFF;
			reader.expect(' ');
			int green = reader.readInt() & 0xFF;
			reader.expect(' ');
			int blue = reader.readInt() & 0xFF;
			Color color = new Color(red, green, blue);
			reader.expect(' ');
			boolean hide = reader.readBoolean();
			
			return new JetpackParticleData(color, life, size, hide);
		}
		
		@Override
		public JetpackParticleData fromNetwork(
		  @NotNull ParticleType<JetpackParticleData> type, FriendlyByteBuf buf
		) {
			int rgb = buf.readInt();
			int life = buf.readInt();
			float size = buf.readFloat();
			boolean hide = buf.readBoolean();
			return new JetpackParticleData(new Color(rgb), life, size, hide);
		}
	};
	
	public static class JetpackParticleType extends ParticleType<JetpackParticleData> {
		private static final boolean alwaysShow = false;
		
		public JetpackParticleType() {
			this(alwaysShow);
		}
		
		public JetpackParticleType(boolean alwaysShow) {
			super(alwaysShow, DESERIALIZER);
		}
		
		@NotNull @Override public Codec<JetpackParticleData> codec() {
			return CODEC;
		}
	}
}
