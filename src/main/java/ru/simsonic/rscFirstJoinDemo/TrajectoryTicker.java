package ru.simsonic.rscFirstJoinDemo;

public class TrajectoryTicker implements Runnable
{
	private final BukkitPluginMain plugin;
	private final TrajectoryPlayer player;
	private int   id = -1;
	public TrajectoryTicker(BukkitPluginMain plugin, TrajectoryPlayer player)
	{
		this.plugin = plugin;
		this.player = player;
	}
	@Override
	public void run()
	{
		player.onTick();
	}
	public void start()
	{
		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, (Runnable)this, 1, 1);
	}
	public void cancel()
	{
		if(id != -1)
		{
			plugin.getServer().getScheduler().cancelTask(id);
			id = -1;
		}
	}
}
