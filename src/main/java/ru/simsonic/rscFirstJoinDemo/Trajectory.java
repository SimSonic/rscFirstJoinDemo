package ru.simsonic.rscFirstJoinDemo;
import org.bukkit.Location;

public class Trajectory
{
	public transient String caption;
	public static class TrajectoryPoint
	{
		public String  world;
		public double  x,y,z;
		public float   yaw, pitch;
		public float   speedAfter; // Blocks per second
		public String  messageOnReach;
		public boolean fly;
		public int     freezeTicks;
		public boolean timeUpdate;
		public long    timeUpdateValue;
		public boolean timeUpdateLock;
		public boolean timeReset;
		public boolean weatherUpdate;
		public boolean weatherUpdateStormy;
		public boolean weatherReset;
		public transient Location location;
		public TrajectoryPoint()
		{
		}
		public TrajectoryPoint(Location location)
		{
			this.world = location.getWorld().getName();
			this.x = location.getX();
			this.y = location.getY();
			this.z = location.getZ();
			this.yaw = location.getYaw();
			this.pitch = location.getPitch();
			// transient
			this.location = location.clone();
		}
	}
	public TrajectoryPoint[] points = new TrajectoryPoint[] {};
	public static class TrajectoryPlayState
	{
		public Trajectory trajectory;
		public boolean started;
		public int currentPoint;
		public long currentPointStartTick;
		public long currentSegmentFlightTime;
		public int playTask;
		public boolean originalFlightAllow;
		public boolean originalFlightState;
		public float deltaYaw;
		public long localTick;
	}
	public Trajectory()
	{
	}
	public TrajectoryPlayState newPlayState()
	{
		final TrajectoryPlayState result = new TrajectoryPlayState();
		result.trajectory = this;
		return result;
	}
}