package ru.simsonic.rscFirstJoinDemo.API;

import org.bukkit.Location;

public class SightedPoint extends GenericPoint
{
	public float pitch, yaw;
	public SightedPoint()
	{
		super();
	}
	public SightedPoint(Location location)
	{
		super(location);
	}
	public final void updateAngles(Location location)
	{
		this.pitch = location.getPitch();
		this.yaw   = location.getYaw();
	}
}
