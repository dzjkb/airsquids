package dzjkb.airsquids.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import dzjkb.airsquids.AirSquids;

public final class ConfigUtils {
	
	private AirSquids plugin;
	
	public ConfigUtils(AirSquids p) {
		plugin = p;
	}
	
	private FileConfiguration config() {
		return plugin.getConfig();
	}
	
	private void throwIA(String msg) {
		throw new IllegalArgumentException(msg);
	}
	
	public String readString(String key) {
		String res = config().getString(key);
		if (res == null) throwIA("Config: No value for key " + key);
		return res;
	}
	
	public int readPositiveInt(String key) {
		int res = config().getInt(key, -1);
		if (res == -1) throwIA("Config: No value for key " + key);
		return res;
	}
	
	public Material readMaterial(String key) throws IllegalArgumentException {
		Material m = Material.getMaterial(readString(key));
		if (m == null) throwIA("Nonexistent material: " + key);
		return m;
	}
	
	public boolean readBoolean(String key) {
		return config().getBoolean(key);
	}
}
