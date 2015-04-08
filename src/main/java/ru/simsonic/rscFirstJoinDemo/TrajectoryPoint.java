package ru.simsonic.rscFirstJoinDemo;

import org.bukkit.Location;

public class TrajectoryPoint
{
	public String  world;
	public double  x;
	public double  y;
	public double  z;
	public float   yaw;
	public float   pitch;
	public float   speedAfter; // [Blocks per second]
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
		this.x     = location.getX();
		this.y     = location.getY();
		this.z     = location.getZ();
		this.yaw   = location.getYaw();
		this.pitch = location.getPitch();
		// transient
		this.location = location.clone();
	}
}
