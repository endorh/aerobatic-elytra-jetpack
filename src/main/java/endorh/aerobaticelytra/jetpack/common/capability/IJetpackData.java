package endorh.aerobaticelytra.jetpack.common.capability;

import endorh.util.math.Vec3f;

public interface IJetpackData {
	
	boolean isFlying();
	void setFlying(boolean flying);
	
	float getHeat();
	void setHeat(float heat);
	float getHoverPropulsion();
	void setHoverPropulsion(float prop);
	
	float getFallSpeed();
	void setFallSpeed(float speed);
	
	int getLastFlight();
	void setLastFlight(int tick);
	
	int getLastGround();
	void setLastGround(int last);
	
	boolean isSneaking();
	boolean isJumping();
	void setSneaking(boolean sneaking);
	void setJumping(boolean jumping);
	
	default boolean updateSneaking(boolean sneaking) {
		if (sneaking != isSneaking()) {
			setSneaking(sneaking);
			return true;
		}
		return false;
	}
	default boolean updateJumping(boolean jumping) {
		if (jumping != isJumping()) {
			setJumping(jumping);
			return true;
		}
		return false;
	}
	
	Vec3f getPropulsionVector();
	Vec3f getPrevPropulsionVector();
	default void updatePrevPropulsionVector() {
		getPrevPropulsionVector().set(getPropulsionVector());
	}
	
	boolean updatePlayingSound(boolean playing);
}
