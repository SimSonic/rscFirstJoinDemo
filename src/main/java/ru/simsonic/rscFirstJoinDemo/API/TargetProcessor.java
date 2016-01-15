package ru.simsonic.rscFirstJoinDemo.API;

import org.bukkit.Location;

public interface TargetProcessor
{
	public void onBegin();
	public void onPointReached(TrajectoryPoint point);
	public void onTeleport(Location location);
	public void onFinish();
}
