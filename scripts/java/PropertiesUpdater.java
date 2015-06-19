import sun.misc.BASE64Encoder;

import java.io.*;
import java.io.UnsupportedEncodingException;
import java.lang.Boolean;
import java.lang.String;
import java.lang.System;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesUpdater
{

	private static final Logger LOGGER = Logger.getLogger(PropertiesUpdater.class.getName());

	public static final Map<String,String> DB_TYPE_DRIVER;
	static {
		Map<String,String> map = new HashMap<>();
		map.put("mysql","com.mysql.jdbc.Driver");
		map.put("mssql","none");
		map.put("oracle","none");
		DB_TYPE_DRIVER = Collections.unmodifiableMap(map);
	}

	public static void main(String[] args) throws IOException
	{
		LOGGER.setLevel(Level.FINE);
		Properties properties = new Properties();

		try (InputStream input = new FileInputStream(args[0]);)
		{
			// load a properties file
			properties.load(input);
		}

		LOGGER.info("properties file read.");
		checkAndUpdateProperty(properties, "vdoc.webapp.name", "WEBAPP");


		checkAndUpdateProperty(properties, "vdoc.database.servertype", "DB_TYPE");

		String dbContainerPrefix = System.getenv("DB_TYPE").toUpperCase();
		properties.setProperty("vdoc.database.driver", DB_TYPE_DRIVER.get(properties.get("vdoc.database.servertype")));

		LOGGER.info("Set main database properties.");
		checkAndUpdateProperty(properties, "vdoc.database.servername", "DB_HOST");
		checkAndUpdateProperty(properties,"vdoc.database.port","DB_PORT");
		checkAndUpdateProperty(properties, "vdoc.database.dbname", "DB_NAME", dbContainerPrefix);
		checkAndUpdateProperty(properties,"vdoc.database.username","DB_USER",dbContainerPrefix);
		checkAndUpdateProperty(properties, "vdoc.database.password", "DB_PASS", dbContainerPrefix);

		properties.setProperty("vdoc.database.password",encodePassword(properties.getProperty("vdoc.database.password")));

		if(Boolean.valueOf(System.getenv("ENABLE_HISTORY")))
		{
			LOGGER.info("Set history database properties.");
			checkAndUpdateProperty(properties, "vdoc.databasehistory.servertype", "HISTORY_DB_TYPE");
			properties.setProperty("vdoc.databasehistory.driver", DB_TYPE_DRIVER.get(properties.get("vdoc.databasehistory.servertype")));
			checkAndUpdateProperty(properties, "vdoc.databasehistory.servername", "HISTORY_DB_HOST");
			checkAndUpdateProperty(properties, "vdoc.databasehistory.instancename", "HISTORY_DB_INSTANCE");
			checkAndUpdateProperty(properties, "vdoc.databasehistory.dbname", "HISTORY_DB_NAME");
			checkAndUpdateProperty(properties, "vdoc.databasehistory.port","HISTORY_DB_PORT");
			checkAndUpdateProperty(properties, "vdoc.databasehistory.username","HISTORY_DB_USER");
			checkAndUpdateProperty(properties, "vdoc.databasehistory.password","HISTORY_DB_PASS");
			properties.setProperty("vdoc.databasehistory.password", encodePassword(properties.getProperty("vdoc.databasehistory.password")));
		}

		try(OutputStream output = new FileOutputStream(args[0]);)
		{
			properties.store(output, null);
		}
	}

	public static void checkAndUpdateProperty(Properties properties,String property,String env) throws IOException
	{
		String value = System.getenv(env);
		if(value == null || value.isEmpty())
		{
			throw new IllegalArgumentException("Environment variable not set "+env);
		}
		LOGGER.info("Property '"+property+"' set with value '"+value+"'.");
		properties.setProperty(property, value);
	}

	public static void checkAndUpdateProperty(Properties properties,String property,String env,String containerPrefix) throws IOException
	{
		try{
			// first try with container environement variable
			checkAndUpdateProperty(properties, property,env);
		}
		catch (IllegalArgumentException e)
		{
			// if fail try with linked container environement variable
			checkAndUpdateProperty(properties,property, containerPrefix +'_'+env);
		}
	}

	/**
	 * Encodage de l'object en Base64
	 * @param object
	 * @return Base64 String of object.toString()
	 */
	public static String encodePassword( String object )
	{
		BASE64Encoder encoder = new BASE64Encoder();
		if ( object != null ) {
			try {
				String encodedPassword = encoder.encode(object.getBytes("UTF-8"));
				LOGGER.info("encode '" + object + "' password as base64 -> '" + encodedPassword + "'");
				return encodedPassword;
			} catch (UnsupportedEncodingException e) {
				LOGGER.severe("UTF-8 not supported");
			}
		}


		return "";
	}

}