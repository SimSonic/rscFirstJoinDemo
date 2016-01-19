package ru.simsonic.rscFirstJoinDemo.Updater;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.simsonic.rscCommonsLibrary.RestartableThread;
import ru.simsonic.rscFirstJoinDemo.API.Settings;
import ru.simsonic.rscMinecraftLibrary.Bukkit.GenericChatCodes;

public final class BukkitUpdater implements Listener
{
	private final JavaPlugin        plugin;
	private final String            updateInfoURL;
	private final HashSet<Player>   adminsToInform = new HashSet<>();
	private final HashSet<Player>   whoCalls       = new HashSet<>();
	private Latest latest = new Latest();
	public BukkitUpdater(JavaPlugin plugin, String latestJsonURL)
	{
		this.plugin = plugin;
		this.updateInfoURL = latestJsonURL;
	}
	public void onEnable()
	{
		plugin.getServer().getPluginManager().registerEvents(BukkitUpdater.this, plugin);
		checkUpdate(null);
	}
	public void checkUpdate(Player sender)
	{
		if(sender != null)
			whoCalls.add(sender);
		threadCheck.start();
	}
	public void doUpdate(CommandSender sender)
	{
		if(downloadUpdate())
		{
			// SUCCESS
			renameOldFile();
		} else {
			// FAILED
		}
	}
	private final RestartableThread threadCheck = new RestartableThread()
	{
		@Override
		public void run()
		{
			checkForUpdate();
			plugin.getServer().getScheduler().runTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					final ArrayList<String> lines = latestToNotify();
					for(Player online : adminsToInform)
						for(String line : lines)
							online.sendMessage(line);
				}
			});
		}
	};
	private final RestartableThread threadUpdate = new RestartableThread()
	{
		@Override
		public void run()
		{
		}
	};
	private void checkForUpdate()
	{
		try
		{
			this.latest = new Gson().fromJson(downloadJson(updateInfoURL), Latest.class);
			System.out.println(this.latest);
		} catch(IOException ex) {
			this.latest = new Latest();
		}
		if(latest.note == null)
			latest.note = "New version: " + latest.version;
		if(latest.notes == null)
			latest.notes = new String[] { latest.note };
	}
	private ArrayList<String> latestToNotify()
	{
		final ArrayList<String> result = new ArrayList<>();
		if(plugin.getDescription().getVersion().equals(latest.version))
		{
			// THERE IS NO UPDATE
			result.add(GenericChatCodes.processStringStatic(Settings.chatPrefix
				+ "{_LS}You are using the latest version."));
		} else {
			// THERE IS AN UPDATE
			result.add(GenericChatCodes.processStringStatic(Settings.chatPrefix
				+ "{_LS}Newer version {_LG}" + latest.version + "{_LS} is available:"));
			for(String note : latest.notes)
				result.add(GenericChatCodes.processStringStatic(Settings.chatPrefix + note));
			result.add(GenericChatCodes.processStringStatic(Settings.chatPrefix
				+ "{_LS}Apply this update with command {GOLD}/rscfjd update do"));
			// SEND TO ALL ADMINS
			whoCalls.addAll(adminsToInform);
		}
		return result;
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
	private static String downloadJson(String url) throws IOException
	{
		try
		{
			final HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setUseCaches(false);
			final int responseCode = connection.getResponseCode();
			if(responseCode == HttpURLConnection.HTTP_OK)
				return readUnicodeStream(connection.getInputStream());
			throw new IOException(new StringBuilder()
				.append(Integer.toString(responseCode))
				.append("Erroneous result of executing web-method: ")
				.append(connection.getResponseMessage())
				.append("\r\n")
				.append(readUnicodeStream(connection.getErrorStream()))
				.toString());
		} catch(JsonParseException | MalformedURLException ex) {
			throw new IOException(ex);
		} catch(IOException ex) {
			throw ex;
		}
	}
	private static String readUnicodeStream(InputStream is) throws IOException
	{
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{
			final byte[] buffer = new byte[1024];
			for(int length = 0; length != -1; length = is.read(buffer))
				baos.write(buffer, 0, length);
			return new String(baos.toByteArray(), "UTF-8");
		}
	}
	private boolean downloadUpdate()
	{
		final File folder = plugin.getDataFolder().getParentFile();
		final File target = new File(folder, plugin.getName() + "_v" + latest.version + ".jar");
		try(FileOutputStream fos = new FileOutputStream(target))
		{
			final ReadableByteChannel rbc = Channels.newChannel(new URL(latest.url).openStream());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.flush();
			return true;
		} catch(MalformedURLException ex) {
			System.err.println(ex);
		} catch(IOException ex) {
			System.err.println(ex);
		}
		return false;
	}
	private void renameOldFile()
	{
		// RENAME OLD VERSION
		final String outdatedJarPath = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		final File outdatedJarSrc = new File(outdatedJarPath);
		final File outdatedJarDst = new File(outdatedJarPath + "-outdated");
		outdatedJarSrc.renameTo(outdatedJarDst);
	}
}
