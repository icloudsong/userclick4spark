package com.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

public class ConfigUtil {

	public static String propertyFile = "config.properties";

	private static final HashMap<String, String> configMap = new HashMap<String, String>();

	static {
		readConfig();
	}

	public static void readConfig() {
		InputStream is = null;
		try {
			//is = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + propertyFile));
			is = ConfigUtil.class.getClassLoader().getResourceAsStream(propertyFile);

		} catch (Exception e) {
			e.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		Properties props = new Properties();
		try {
			props.load(br);
			for (Object s : props.keySet()) {
				configMap.put(s.toString().trim(), props.getProperty(s.toString().trim()).trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getValue(String key) {
		readConfig();
		return configMap.get(key);
	}

	public static String get(String key) {
		return configMap.get(key);
	}

	public static int getInt(String key) {
		int result = -1;
		try {
			result = Integer.parseInt(get(key));
		} catch (NumberFormatException exc) {
			result = -1;
		}
		return result;
	}
	
	public static double getDouble(String key) {
		double result = -1d;
		try {
			result = Double.parseDouble(get(key));
		} catch (NumberFormatException exc) {
			result = -1;
		}
		return result;
	}

	public static void main(String[] args) {

		System.out.println(ConfigUtil.get("redisIP"));
	}

}
