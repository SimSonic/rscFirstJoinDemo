package ru.simsonic.rscFirstJoinDemo.API;

import org.bukkit.Location;

public class GenericPoint
{
	public String world;
	public double x, y, z;
	public transient Location location;
	public GenericPoint()
	{
	}
	public GenericPoint(Location location)
	{
		this.location = location.clone();
		this.world = location.getWorld().getName();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
	}
}
