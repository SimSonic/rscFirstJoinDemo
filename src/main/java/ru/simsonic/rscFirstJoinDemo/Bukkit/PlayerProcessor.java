package ru.simsonic.rscFirstJoinDemo.Bukkit;

import org.bukkit.Location;
import ru.simsonic.rscFirstJoinDemo.API.TargetProcessor;
import ru.simsonic.rscFirstJoinDemo.API.TrajectoryPoint;

public class PlayerProcessor implements TargetProcessor
{
	private final TrajectoryPlayState tps;
	public PlayerProcessor(TrajectoryPlayState tps)
	{
		this.tps = tps;
	}
	@Override
	public void onBegin()
	{
	}
	@Override
	public void onPointReached(TrajectoryPoint point)
	{
	}
	@Override
	public void onTeleport(Location location)
	{
	}
	@Override
	public void onFinish()
	{
	}
}
