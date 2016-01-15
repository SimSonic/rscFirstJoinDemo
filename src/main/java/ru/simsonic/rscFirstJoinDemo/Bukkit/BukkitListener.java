package ru.simsonic.rscFirstJoinDemo.Bukkit;

import java.util.logging.Level;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscFirstJoinDemo.API.Trajectory;
import ru.simsonic.rscFirstJoinDemo.BukkitPluginMain;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public class BukkitListener implements Listener
{
	private final BukkitPluginMain plugin;
	public BukkitListener(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();
		// Hide all other demo players
		for(Player demo : plugin.playStates.keySet())
			player.hidePlayer(demo);
		// Plan other actions after some little delay
		final int delay = plugin.settings.getFirstJoinDelay();
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				// Check if first-join trajectory is enabled for all
				if(plugin.settings.getFirstJoinEnabled())
				{
					// Check if we should show the demo for this player
					if(!player.hasPlayedBefore())
					{
						// Check if the player is admin
						if(player.hasPermission("rscfjd.admin"))
						{
							player.sendMessage(GenericChatCodes.processStringStatic(
								Settings.chatPrefix + "You have skipped demo due to having admin permission."));
							BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Skipping player {0} due to admin permission.", player.getName());
						} else {
							// Let's start it
							final Trajectory demo = plugin.trajMngr.getFirstJoinTrajectory();
							if(demo != null)
								plugin.trajectoryPlayer.beginDemo(player, demo);
						}
					}
				}
				// Check if he is admin so we need to restore his buffer
				if(player.hasPermission("rscfjd.admin"))
				{
					final Trajectory buffer = plugin.trajMngr.loadBufferTrajectory(player);
					if(buffer.points.length > 0)
					{
						plugin.setBufferedTrajectory(player, buffer);
						plugin.commands.setSelectedPoint(player, buffer, buffer.points.length - 1, false);
						player.sendMessage(GenericChatCodes.processStringStatic(Settings.chatPrefix
							+ "Your buffer has been restored from file, the last one is selected."));
					} else
						player.sendMessage(GenericChatCodes.processStringStatic(Settings.chatPrefix
							+ "Your buffer file contains no points."));
				}
			}
		}, delay);
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		final Player player = event.getPlayer();
		plugin.trajectoryPlayer.finishDemo(player);
		if(plugin.playerBuffers.containsKey(player))
		{
			final Trajectory buffer = plugin.getBufferedTrajectory(player);
			plugin.trajMngr.saveBufferTrajectory(buffer, player);
			plugin.playerBuffers.remove(player);
		}
	}
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event)
	{
		final Player player = event.getPlayer();
		plugin.trajectoryPlayer.finishDemo(event.getPlayer());
		if(plugin.playerBuffers.containsKey(player))
		{
			final Trajectory buffer = plugin.getBufferedTrajectory(player);
			plugin.trajMngr.saveBufferTrajectory(buffer, player);
			plugin.playerBuffers.remove(player);
		}
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if(plugin.playStates.containsKey(event.getPlayer()))
			event.setCancelled(true);
		else
			event.getRecipients().removeAll(plugin.playStates.keySet());
	}
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		final Player player = event.getPlayer();
		if(plugin.playStates.containsKey(player) && !player.hasPermission("rscfjd.admin"))
			event.setCancelled(true);
	}
	private final String signFirstLine = GenericChatCodes.processStringStatic("{_DG}[rscFJD]");
	@org.bukkit.event.EventHandler
	public void onSignChange(final SignChangeEvent event)
	{
		if(event.getLine(0).equalsIgnoreCase("[rscfjd]") == false)
			return;
		final Player player = event.getPlayer();
		if(!player.hasPermission("rscfjd.admin"))
		{
			player.sendMessage(GenericChatCodes.processStringStatic(
				Settings.chatPrefix + "{_LR}Not enough permissions."));
			event.setCancelled(true);
			return;
		}
		event.setLine(0, signFirstLine);
		final String flight = event.getLine(1).isEmpty() ? plugin.settings.getFirstJoinTrajectory() : event.getLine(1);
		event.setLine(1, GenericChatCodes.processStringStatic("{_LG}Click to play demo"));
		event.setLine(3, flight);
		player.sendMessage(GenericChatCodes.processStringStatic(Settings.chatPrefix + "{_LG}Done."));
	}
	@org.bukkit.event.EventHandler
	public void onSignRBClick(final PlayerInteractEvent event)
	{
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(!(event.getClickedBlock().getState() instanceof Sign))
			return;
		final Sign sign = (Sign)event.getClickedBlock().getState();
		if(!sign.getLine(0).equals(signFirstLine))
			return;
		final String trajectoryName = sign.getLine(3);
		final Trajectory trajectory = plugin.trajMngr.loadTrajectory(trajectoryName);
		plugin.trajectoryPlayer.beginDemo(event.getPlayer(), trajectory);
	}
	@org.bukkit.event.EventHandler
	public void onPlayerDamage(final EntityDamageEvent event)
	{
		final Entity entity = event.getEntity();
		if(entity instanceof Player)
			if(plugin.playStates.containsKey((Player)entity))
				event.setCancelled(true);
	}
}
