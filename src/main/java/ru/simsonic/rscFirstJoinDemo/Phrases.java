package ru.simsonic.rscFirstJoinDemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Phrases
{
	PLUGIN_ENABLED    ("generic.enabled"),
	PLUGIN_DISABLED   ("generic.disabled"),
	PLUGIN_RELOADED   ("generic.reloaded"),
	PROTOCOLLIB_YES   ("generic.plib-y"),
	PROTOCOLLIB_NO    ("generic.plib-n"),
	;
	private final String node;
	private String phrase;
	private Phrases(String node)
	{
		this.node = node;
	}
	@Override
	public String toString()
	{
		return phrase;
	}
	public static void fill(BukkitPluginMain plugin, String langName)
	{
		final File langFile = new File(plugin.getDataFolder(), langName + ".yml");
		final YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
		for(Phrases value : Phrases.values())
			value.phrase = langConfig.getString(value.node, value.node);
	}
	public static void extractTranslations(File workingDir)
	{
		extractTranslation(workingDir, "english");
		// extractTranslation(workingDir, "russian");
	}
	private static void extractTranslation(File workingDir, String langName)
	{
		try
		{
			final File langFile = new File(workingDir, langName + ".yml");
			if(langFile.isFile())
				langFile.delete();
			final FileChannel fileChannel = new FileOutputStream(langFile).getChannel();
			fileChannel.force(true);
			final InputStream langStream = BukkitPluginMain.class.getResourceAsStream("/languages/" + langName + ".yml");
			fileChannel.transferFrom(Channels.newChannel(langStream), 0, Long.MAX_VALUE);
		} catch(IOException ex) {
		}
	}
}
