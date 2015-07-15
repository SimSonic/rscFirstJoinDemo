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
import ru.simsonic.rscFirstJoinDemo.BukkitPluginMain;
import ru.simsonic.rscFirstJoinDemo.Trajectory;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public class BukkitListener implements Listener
{
	private final BukkitPluginMain rscfjd;
	public BukkitListener(BukkitPluginMain plugin)
	{
		this.rscfjd = plugin;
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();
		// Hide all other demo players
		for(Player demo : rscfjd.playing.keySet())
			player.hidePlayer(demo);
		// Check if we should show the demo for him
		if(!player.hasPlayedBefore())
		{
			if(player.hasPermission("rscfjd.admin"))
			{
				player.sendMessage(GenericChatCodes.processStringStatic(
					Settings.chatPrefix + "You have skipped demo due to having admin permission."));
				BukkitPluginMain.consoleLog.log(Level.INFO, "[rscfjd] Skipping player {0} due to admin permission.", player.getName());
				return;
			}
			if(rscfjd.lazyFirstJoinTrajectoryLoading())
				rscfjd.trajectoryPlayer.beginDemo(player, rscfjd.trajectories.get(rscfjd.firstJoinTrajectory));
		}
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if(rscfjd.playing.containsKey(event.getPlayer()))
			event.setCancelled(true);
		else
			event.getRecipients().removeAll(rscfjd.playing.keySet());
	}
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		final Player player = event.getPlayer();
		if(rscfjd.playing.containsKey(player) && !player.hasPermission("rscfjd.admin"))
			event.setCancelled(true);
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		rscfjd.trajectoryPlayer.finishDemo(event.getPlayer());
	}
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event)
	{
		rscfjd.trajectoryPlayer.finishDemo(event.getPlayer());
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
		final String flight = event.getLine(1).isEmpty() ? rscfjd.firstJoinTrajectory : event.getLine(1);
		event.setLine(1, GenericChatCodes.processStringStatic(
			rscfjd.getConfig().getString("settings.signs.note", "{_LG}Start demo")));
		event.setLine(3, flight);
		player.sendMessage(GenericChatCodes.processStringStatic(
			Settings.chatPrefix + "{_LG}Done."));
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
		final Trajectory trajectory = rscfjd.loadTrajectory(trajectoryName);
		rscfjd.trajectoryPlayer.beginDemo(event.getPlayer(), trajectory);
	}
	@org.bukkit.event.EventHandler
	public void onPlayerDamage(final EntityDamageEvent event)
	{
		final Entity entity = event.getEntity();
		if(entity instanceof Player)
			if(rscfjd.playing.containsKey((Player)entity))
				event.setCancelled(true);
	}
}
