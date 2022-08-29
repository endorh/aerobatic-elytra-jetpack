package endorh.aerobaticelytra.jetpack.common.capability;

import endorh.util.capability.ISerializableCapability;
import endorh.util.math.Vec3f;

/**
 * Capability containing the state of an Aerobatic Elytra Jetpack for a player.
 */
public interface IJetpackData extends ISerializableCapability {
	
	/**
	 * Check if the player is jetpack flying
	 */
	boolean isFlying();
	/**
	 * Set the player jetpack flying.
	 */
	void setFlying(boolean flying);
	
	/**
	 * Get the heat of the jetpack, between 0 and 1.<br>
	 * Once it reaches 1, the jetpack reaches max propulsion.
	 */
	float getHeat();
	/**
	 * Set the heat of the jetpack, between 0 and 1.<br>
	 * Once it reaches 1, the jetpack reaches max propulsion.
	 */
	void setHeat(float heat);
	
	/**
	 * Get the propulsion of the jetpack in hover mode, between -1 and 1.
	 */
	float getHoverPropulsion();
	/**
	 * Set the propulsion of the jetpack in hover mode, between -1 and 1.
	 */
	void setHoverPropulsion(float prop);
	
	/**
	 * Get the fall speed of the player.
	 */
	float getFallSpeed();
	/**
	 * Set the fall speed of the player.
	 */
	void setFallSpeed(float speed);

	/**
	 * Get the last tick the player was in flight.
	 */
	int getLastFlight();
	/**
	 * Set the last tick the player was in flight.
	 */
	void setLastFlight(int tick);

	/**
	 * Get the last tick the player was on ground.
	 */
	int getLastGround();
	/**
	 * Set the last tick the player was on ground.
	 */
	void setLastGround(int last);

	/**
	 * Check if the player is sneaking while jetpack flying.
	 */
	boolean isSneaking();
	/**
	 * Check if the player is jumping while jetpack flying.
	 */
	boolean isJumping();
	/**
	 * Set the player sneaking while jetpack flying.
	 */
	void setSneaking(boolean sneaking);
	/**
	 * Set the player jumping while jetpack flying.
	 */
	void setJumping(boolean jumping);
	
	/**
	 * Set the player sneaking while jetpack flying.
	 * @param sneaking Whether the player is sneaking.
	 * @return {@code true} if the state of the player changed as a result.
	 */
	default boolean updateSneaking(boolean sneaking) {
		if (sneaking != isSneaking()) {
			setSneaking(sneaking);
			return true;
		}
		return false;
	}
	/**
	 * Set the player jumping while jetpack flying.
	 * @param jumping Whether the player is jumping.
	 * @return {@code true} if the state of the player changed as a result.
	 */
	default boolean updateJumping(boolean jumping) {
		if (jumping != isJumping()) {
			setJumping(jumping);
			return true;
		}
		return false;
	}
	
	/**
	 * Get the propulsion vector of the jetpack.
	 */
	Vec3f getPropulsionVector();
	/**
	 * Get the propulsion vector of the jetpack for the previous tick.
	 */
	Vec3f getPrevPropulsionVector();
	
	/**
	 * Update the propulsion vector of the previous tick with this tick's value.
	 */
	default void updatePrevPropulsionVector() {
		getPrevPropulsionVector().set(getPropulsionVector());
	}
	
	/**
	 * Set the playing sound state for the jetpack.
	 * @param playing Whether the jetpack is playing its sound.
	 * @return {@code true} if the state of the sound changed as a result.
	 */
	boolean updatePlayingSound(boolean playing);
}
