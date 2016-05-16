package ru.simsonic.rscFirstJoinDemo.Bukkit;

import java.util.logging.Level;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.bukkit.util.Vector;
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
				// Inform admins about updates
				if(player.hasPermission("rscfjd.admin"))
					plugin.updating.onAdminJoin(player, true);
				// Check if first-join trajectory is enabled for all
				if(plugin.settings.getFirstJoinEnabled())
				{
					// Check if we should show the demo for this player
					if(!player.hasPlayedBefore())
					{
						// Check if the player is admin
						if(player.hasPermission("rscfjd.admin"))
						{
							BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Skipping player {0} due to admin permission.", player.getName());
						} else {
							// Let's start it
							final Trajectory demo = plugin.trajMngr.getFirstJoinTrajectory();
							if(demo != null)
								plugin.trajPlay.beginDemo(player, demo);
						}
					}
				}
				// Check if he is admin so we need to restore his buffer
				if(player.hasPermission("rscfjd.admin"))
					plugin.trajMngr.restorePlayerBuffer(player);
			}
		}, delay);
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		final Player player = event.getPlayer();
		plugin.trajPlay.finishDemo(player);
		if(plugin.playerBuffers.containsKey(player))
		{
			final Trajectory buffer = plugin.getBufferedTrajectory(player);
			plugin.trajMngr.saveBufferTrajectory(player, buffer);
			plugin.playerBuffers.remove(player);
		}
	}
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event)
	{
		final Player player = event.getPlayer();
		plugin.trajPlay.finishDemo(event.getPlayer());
		if(plugin.playerBuffers.containsKey(player))
		{
			final Trajectory buffer = plugin.getBufferedTrajectory(player);
			plugin.trajMngr.saveBufferTrajectory(player, buffer);
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
	@org.bukkit.event.EventHandler
	public void onPlayerDamage(final EntityDamageEvent event)
	{
		final Entity entity = event.getEntity();
		if(entity instanceof Player)
			if(plugin.playStates.containsKey((Player)entity))
				event.setCancelled(true);
	}
	@org.bukkit.event.EventHandler
	public void onSignChange(final SignChangeEvent event)
	{
		if(event.getLine(0).equalsIgnoreCase("[rscfjd]") == false)
			return;
		final Player player = event.getPlayer();
		if(!player.hasPermission("rscfjd.admin"))
		{
			player.sendMessage(GenericChatCodes.processStringStatic(Settings.CHAT_PREFIX + "{_LR}Not enough permissions."));
			event.setCancelled(true);
			return;
		}
		final String caption = event.getLine(1).isEmpty()
			? plugin.settings.getFirstJoinTrajectory()
			: event.getLine(1);
		event.setLine(0, Settings.SIGN_LINE_0);
		event.setLine(1, GenericChatCodes.processStringStatic("{_LG}Click to play demo"));
		event.setLine(2, "");
		event.setLine(3, caption);
		player.sendMessage(GenericChatCodes.processStringStatic(Settings.CHAT_PREFIX + "{_LG}Done."));
	}
	@org.bukkit.event.EventHandler
	public void onSignRBClick(final PlayerInteractEvent event)
	{
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(!(event.getClickedBlock().getState() instanceof Sign))
			return;
		final Sign sign = (Sign)event.getClickedBlock().getState();
		if(!sign.getLine(0).equals(Settings.SIGN_LINE_0))
			return;
		// Load such trajectory
		final String caption = sign.getLine(3);
		final Trajectory trajectory = plugin.trajMngr.loadTrajectory(caption, false);
		if(trajectory.points.length == 0)
			return;
		// Check specific permission
		final Player player = event.getPlayer();
		boolean permSign = plugin.settings.getRequireSignPerms()
			? player.hasPermission("rscfjd.sign." + trajectory.caption.toLowerCase())
			: true;
		boolean permTrajectory = trajectory.requiredPermission != null && !"".equals(trajectory.requiredPermission)
			? player.hasPermission(trajectory.requiredPermission)
			: permSign;
		// Start if allowed
		if(permSign || permTrajectory)
			plugin.trajPlay.beginDemo(player, trajectory);
	}
	private final double distanceMax = 100;
	private void renderRay(Location source, Location target)
	{
		final float colorR = 127 / 255.0f;
		final float colorG = 255 / 255.0f;
		final float colorB = 255 / 255.0f;
		final Location start  = source.clone().add(0.0, 2.5, 0.0);
		final Location stop   = target.clone();
		final double   period = 0.1;
		final World    world  = start.getWorld();
		if(world.equals(stop.getWorld()))
		{
			final double distance = start.distance(stop);
			if(distance <= distanceMax)
			{
				final Vector step = stop.subtract(start).toVector().normalize().multiply(period);
				final int steps = (int)(distance / period);
				for(int stepId = 0; stepId < steps; stepId += 1)
				{
					world.spigot().playEffect(
						start, Effect.COLOURED_DUST,
						0, 0,
						colorR, colorG, colorB,
						0.0f, 0, 200);
					start.add(step);
				}
			}
		}
	}
}
