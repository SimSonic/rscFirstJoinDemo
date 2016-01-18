package ru.simsonic.rscFirstJoinDemo.Updater;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitUpdater extends AutoUpdater implements Listener
{
	private final HashSet<Player> adminsToInform = new HashSet<>();
	private final JavaPlugin plugin;
	private Thread thread;
	public BukkitUpdater(JavaPlugin plugin, String latestJsonURL)
	{
		super(plugin, latestJsonURL);
		this.plugin = plugin;
	}
	public void onEnable()
	{
		final Runnable checkForUpdates = new Runnable()
		{
			@Override
			public void run()
			{
				final Latest latest = BukkitUpdater.super.checkForUpdate();
				if(!plugin.getDescription().getVersion().equals(latest.version))
				{
					plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
					{
						@Override
						public void run()
						{
						}
					}, 5 * 20);
				}
			}
		};
		thread = new Thread(checkForUpdates);
		thread.start();
	}
	public void onDoUpdate(CommandSender sender)
	{
		super.downloadUpdate();
		super.applyUpdate();
	}
	public Set<Player> getAdmins()
	{
		return new HashSet<>(adminsToInform);
	}
	public void onAdminJoin(Player player)
	{
		adminsToInform.add(player);
	}
	@EventHandler
	protected void onPlayerQuit(PlayerQuitEvent event)
	{
		adminsToInform.add(event.getPlayer());
	}
	@EventHandler
	protected void onPlayerKick(PlayerKickEvent event)
	{
		adminsToInform.add(event.getPlayer());
	}
}
