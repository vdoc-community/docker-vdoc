import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUpdater
{

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
		Properties properties = new Properties();

		try (InputStream input = new FileInputStream(args[0]);)
		{
			// load a properties file
			properties.load(input);
		}

		checkAndUpdateProperty(properties,"vdoc.webapp.name","WEBAPP");


		checkAndUpdateProperty(properties,"vdoc.database.servertype","DB_TYPE");
		String dbContainerPrefix = System.getenv("DB_TYPE").toUpperCase();
		properties.setProperty("vdoc.database.driver", DB_TYPE_DRIVER.get(properties.get("vdoc.database.servertype")));

		checkAndUpdateProperty(properties,"vdoc.database.servername","DB_HOST");
		checkAndUpdateProperty(properties,"vdoc.database.port","DB_PORT");

		checkAndUpdateProperty(properties,"vdoc.database.dbname","DB_NAME",dbContainerPrefix);
		checkAndUpdateProperty(properties,"vdoc.database.username","DB_USER",dbContainerPrefix);
		checkAndUpdateProperty(properties,"vdoc.database.password","DB_PASS",dbContainerPrefix);

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
		properties.setProperty(property, value);
	}

	public static void checkAndUpdateProperty(Properties properties,String property,String env,String containerPrefix) throws IOException
	{
		String value = System.getenv(env);
		if(value != null && !value.isEmpty())
		{
			properties.setProperty(property, value);
			System.out.println("Environment variable '"+env+"' not found try with link '"+containerPrefix+'_'+env+"' ");
		}
		else
		{
			value = System.getenv(containerPrefix+'_'+env);
			if(value == null || value.isEmpty())
			{
				throw new IllegalArgumentException("Environment variable not set "+env);
			}
			properties.setProperty(property, value);
		}

	}
}