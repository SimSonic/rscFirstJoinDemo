package ru.simsonic.rscFirstJoinDemo.Bukkit;

import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.simsonic.rscFirstJoinDemo.BukkitPluginMain;

public class IntegrationMan
{
	private final BukkitPluginMain plugin;
	private Plugin protocolLib;
	private Plugin placeholderAPI;
	private Plugin nocheatplus;
	public IntegrationMan(BukkitPluginMain plugin)
	{
		this.plugin = plugin;
	}
	public boolean isProtocolLib()
	{
		protocolLib = plugin.getServer().getPluginManager().getPlugin("ProtocolLib");
		return protocolLib != null && protocolLib.isEnabled();
	}
	public boolean isPlaceholderAPI()
	{
		placeholderAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
		return placeholderAPI != null && placeholderAPI.isEnabled();
	}
	public boolean isNoCheatPlus()
	{
		nocheatplus = plugin.getServer().getPluginManager().getPlugin("NoCheatPlus");
		return nocheatplus != null && nocheatplus.isEnabled();
	}
	public static String processPlaceholders(Player player, String text)
	{
		return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
	}
	public static void sendTitles(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) throws Exception
	{
		final com.comphenix.protocol.ProtocolManager protocolMan
			= com.comphenix.protocol.ProtocolLibrary.getProtocolManager();
		final com.comphenix.protocol.PacketType packetType
			= com.comphenix.protocol.PacketType.Play.Server.TITLE;
		final com.comphenix.protocol.events.PacketContainer pTimeTitle
			= protocolMan.createPacket(packetType);
		if(stay <= 0)
			stay = 20;
		pTimeTitle.getIntegers().write(0, fadeIn).write(1, stay).write(2, fadeOut);
		pTimeTitle.getTitleActions().write(0, com.comphenix.protocol.wrappers.EnumWrappers.TitleAction.TIMES);
		protocolMan.sendServerPacket(player, pTimeTitle);
		if("".equals(subtitle))
			subtitle = ChatColor.RESET.toString();
		final com.comphenix.protocol.events.PacketContainer pSubTitle
			= protocolMan.createPacket(packetType);
		pSubTitle.getChatComponents().write(0, com.comphenix.protocol.wrappers.WrappedChatComponent.fromText(subtitle));
		pSubTitle.getTitleActions().write(0, com.comphenix.protocol.wrappers.EnumWrappers.TitleAction.SUBTITLE);
		protocolMan.sendServerPacket(player, pSubTitle);
		if("".equals(title))
			title = ChatColor.RESET.toString();
		final com.comphenix.protocol.events.PacketContainer pTitle
			= protocolMan.createPacket(packetType);
		pTitle.getChatComponents().write(0, com.comphenix.protocol.wrappers.WrappedChatComponent.fromText(title));
		pTitle.getTitleActions().write(0, com.comphenix.protocol.wrappers.EnumWrappers.TitleAction.TITLE);
		protocolMan.sendServerPacket(player, pTitle);
	}
	private final HashSet<Player> exemptedNCP = new HashSet<>();
	public void doExemptNCP(Player player)
	{
		if(isNoCheatPlus())
		{
			fr.neatmonster.nocheatplus.hooks.NCPExemptionManager.exemptPermanently(player);
			this.exemptedNCP.add(player);
		}
	}
	public void cancelExemptNCP(Player player)
	{
		if(isNoCheatPlus() && this.exemptedNCP.remove(player))
		{
			fr.neatmonster.nocheatplus.hooks.NCPExemptionManager.unexempt(player);
		}
	}
}
