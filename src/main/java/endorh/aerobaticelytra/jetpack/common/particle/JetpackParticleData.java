package endorh.aerobaticelytra.jetpack.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.Locale;

public class JetpackParticleData implements IParticleData {
	
	public final Color tint;
	public final float size;
	public final int life;
	public final boolean ownPlayer;
	
	@NotNull @Override
	public ParticleType<JetpackParticleData> getType() {
		return ModParticles.JETPACK_PARTICLE;
	}
	
	@Override public void write(@NotNull PacketBuffer buf) {
		buf.writeInt(tint.getRGB());
		buf.writeInt(life);
		buf.writeFloat(size);
		buf.writeBoolean(ownPlayer);
	}
	
	@NotNull @Override public String getParameters() {
		return String.format(
		  Locale.ROOT, "%s %.2f %d %d %d %b",
		  this.getType().getRegistryName(), size,
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
		size = MathHelper.clamp(sizeIn, 0F, 1F);
		this.ownPlayer = ownPlayer;
	}
	public JetpackParticleData(int lifeIn, float sizeIn, boolean ownPlayer) {
		this(Color.WHITE, lifeIn, sizeIn, ownPlayer);
	}
	
	@SuppressWarnings("deprecation")
	public static final IDeserializer<JetpackParticleData> DESERIALIZER =
	  new IDeserializer<JetpackParticleData>() {
		@NotNull @Override
		public JetpackParticleData deserialize(
		  @NotNull ParticleType<JetpackParticleData> type, @NotNull StringReader reader
		) throws CommandSyntaxException {
			reader.expect(' ');
			float size = MathHelper.clamp(reader.readFloat(), 0F, 1F);
			
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
		public JetpackParticleData read(
		  @NotNull ParticleType<JetpackParticleData> type, PacketBuffer buf
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
		
		@NotNull @Override public Codec<JetpackParticleData> func_230522_e_() {
			return CODEC;
		}
	}
}
