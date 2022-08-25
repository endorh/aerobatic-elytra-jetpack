package endorh.aerobaticelytra.jetpack.client.render.model;

import endorh.aerobaticelytra.client.render.model.IElytraPose;
import endorh.aerobaticelytra.client.render.model.IElytraPose.ElytraPose;

import static endorh.aerobaticelytra.client.render.model.AerobaticElytraModelPose.ModelRotation.*;

public class AerobaticJetpackPoses {
	public static final IElytraPose JETPACK_POSE = new ElytraPose() {
		@Override protected void build() {
			pose.leftWing.x = DEG_20;
			pose.leftWing.y = -DEG_5;
			pose.leftWing.z = -DEG_60;
			pose.leftWing.origin.x = 5F;
			pose.leftWing.origin.z = -2F;
			
			pose.rightWing.x = pose.leftWing.x;
			pose.rightWing.y = -pose.leftWing.y;
			pose.rightWing.z = -pose.leftWing.z;
			pose.rightWing.origin.x = -pose.leftWing.origin.x;
			pose.rightWing.origin.z = pose.leftWing.origin.z;
			
			pose.leftRocket.x = DEG_5;
			pose.leftRocket.z = DEG_60;
			
			pose.rightRocket.x = pose.leftRocket.x;
			pose.rightRocket.z = -pose.leftRocket.z;
		}
	};
}
