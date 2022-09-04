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
	
	/**
	 * Check if the player is performing a jetpack dash.
	 */
	boolean isDashing();
	
	/**
	 * Get the tick when the player started the last dash.
	 */
	int getDashStart();
	
	/**
	 * Get the progress, from 0 to 1, of the current jetpack dash.
	 */
	float getDashProgress();
	/**
	 * Set the progress, from 0 to 1, of the current jetpack dash.
	 */
	void setDashProgress(float progress);
	
	/**
	 * Check if the dash keybind is pressed.<br>
	 * Used to suppress repeated dashing without unpressing in between.
	 */
	boolean isDashKeyPressed();
	/**
	 * Set the dash keybind press state.
	 */
	void setDashKeyPressed(boolean pressed);
	
	/**
	 * Perform a new jetpack dash, if possible.
	 */
	void startDash(int tick, Vec3f dashVector, int ticks, int cooldown);
	
	/**
	 * Get the number of dashes performed consecutively in this combo.
	 */
	int getConsecutiveDashes();
	/**
	 * Set the number of dashes performed consecutively in this combo.
	 */
	void setConsecutiveDashes(int dashes);
	
	/**
	 * Get the number of ticks the last dash lasts.
	 */
	int getDashTicks();
	
	/**
	 * Get the number of ticks the player is stuck without dashing after
	 * reaching the maximum number of consecutive dashes.
	 */
	int getDashCooldown();
	
	/**
	 * Get the intended displacement vector of the last dash.
	 */
	Vec3f getDashVector();
	
	/**
	 * Get the direction of the last dash.
	 */
	Vec3f getDashDirection();
	
	/**
	 * Get the propulsion vector of the jetpack.
	 */
	Vec3f getPropulsionVector();
	Vec3f getPrevPropulsionVector();
	default void updatePrevPropulsionVector() {
		getPrevPropulsionVector().set(getPropulsionVector());
	}
	
	boolean updatePlayingSound(boolean playing);
}
