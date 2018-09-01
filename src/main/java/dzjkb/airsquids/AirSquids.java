package dzjkb.airsquids;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import dzjkb.airsquids.commands.AirSquidsCommand;
import dzjkb.airsquids.utils.ConfigUtils;

public final class AirSquids extends JavaPlugin {
	
	private ConfigUtils cfg = new ConfigUtils(this);
	
	// configurable parameters
    private World world;

	public void onEnable() {
		this.getLogger().info("Activating FLYING SQUIDS, WHAT?!");

		if (!new File(this.getDataFolder(), "config.yml").exists()) {
            this.saveDefaultConfig();
            getLogger().warning("Generated default config");
        }
		
		readConfig();
        
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(this), this);
        this.getCommand("airsquids").setExecutor((CommandExecutor)new SpongiesCommandp(this));
        this.getLogger().info("Flying squids enabled");
	}
	
	public void readConfig() {
		String worldname;
		
		try {
	        worldname = cfg.readString("WORLD");
		} catch (IllegalArgumentException e) {
			loadError(e.getMessage());
			return;
		}

		List<World> worlds = this.getServer().getWorlds();
		this.world = null;
		for (World w : worlds) {
			if (w.getName().equalsIgnoreCase(worldname)) {
				this.world = w;
				break;
			}
		}
		
		if (this.world == null) {
			getLogger().warning("In world list:");
			for (World w : worlds) {
				getLogger().warning(w.getName());
			}
			loadError("Couldn't find world " + worldname);
			return;
		}
	}
	
	public void onDisable() {
		this.getLogger().info("Disabled airsquids");
	}
	
	private void loadError(String msg) {
		this.getLogger().warning(msg);
    	this.getLogger().warning("Aborting airsquids");
    	this.setEnabled(false);
	}
	
	public World getWorld() {
		return this.world;
	}
}
