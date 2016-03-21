package ru.simsonic.rscFirstJoinDemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import ru.simsonic.rscFirstJoinDemo.API.TranslationProvider;

public enum Phrases
{
	PLUGIN_ENABLED    ("generic.enabled"),
	PLUGIN_DISABLED   ("generic.disabled"),
	PLUGIN_RELOADED   ("generic.reloaded"),
	PLUGIN_PLIB_YES   ("generic.plib-y"),
	PLUGIN_PLIB_NO    ("generic.plib-n"),
	PLUGIN_METRICS    ("generic.metrics"),
	PLUGIN_NOCHEATPLUS("generic.nocheatplus"),
	DEMO_EMPTY        ("logging.demo-empty"),
	DEMO_STARTING     ("logging.demo-start"),
	DEMO_STOPPING     ("logging.demo-stop"),
	NCP_EXEMPT        ("logging.ncp-exempt"),
	NCP_RESTORE       ("logging.ncp-restore"),
	POINT_REACHED     ("logging.on-reach"),
	ERR_NO_PERMISSIONS("errors.no-perms"),
	ERR_NOT_A_PLAYER  ("errors.player-only"),
	ERR_NOT_A_COMMAND ("errors.unknown-cmd"),
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
	public static void applyTranslation(TranslationProvider provider)
	{
		for(Phrases value : Phrases.values())
			value.phrase = provider.getString(value.node);
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
