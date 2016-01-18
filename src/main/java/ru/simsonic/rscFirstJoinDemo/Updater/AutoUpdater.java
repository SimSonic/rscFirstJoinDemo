package ru.simsonic.rscFirstJoinDemo.Updater;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AutoUpdater
{
	private final String url;
	private final Object parent;
	private Latest latest = new Latest();
	public AutoUpdater(Object parent, String latestJsonURL)
	{
		this.url    = latestJsonURL;
		this.parent = parent;
	}
	public Latest checkForUpdate()
	{
		try
		{
			this.latest = new Gson().fromJson(httpGET(url), Latest.class);
			System.out.println(this.latest);
		} catch(IOException ex) {
		}
		if(latest.note == null)
			latest.note = "New version: " + latest.version;
		if(latest.notes == null)
			latest.notes = new String[] { latest.note };
		return latest;
	}
	public void downloadUpdate()
	{
	}
	public void applyUpdate()
	{
		// RENAME OLD VERSION
		final String outdatedJarPath = parent.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		final File outdatedJarSrc = new File(outdatedJarPath);
		final File outdatedJarDst = new File(outdatedJarPath + "-outdated");
		outdatedJarSrc.renameTo(outdatedJarDst);
	}
	private static String httpGET(String url) throws IOException
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
}
