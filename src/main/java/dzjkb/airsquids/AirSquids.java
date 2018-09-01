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
	
	public ArrayList<SpongeTree> leTrees;
	private BukkitTask spawnTask;
	private BukkitTask autosave;
	
	// configurable parameters
	public void onEnable() {
		this.getLogger().info("Activating FLYING SQUIDS WHAT!");

		if (!new File(this.getDataFolder(), "config.yml").exists()) {
            this.saveDefaultConfig();
            getLogger().warning("Generated default config");
        }
		
		readConfig();
        
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(this), this);
        
        this.getCommand("airsquids").setExecutor((CommandExecutor)new SpongiesCommandp(this));
        
        this.spawnTask = new SpawnerTask().runTaskTimer(this, spawnInterval * 1200, spawnInterval * 1200);
        this.autosave = new AutoSave().runTaskTimer(this, TimeUnit.HOURS.toSeconds(1) * 20, TimeUnit.HOURS.toSeconds(1) * 20);
        retrieve();
        if (useWorldGuard) fixregions();
        this.getLogger().info("The Sponge Trees have been enabled");
	}
	
	private void fixregions() {
		RegionManager reguns = getWg().getRegionContainer().get(getWorld());
		for (SpongeTree s : leTrees) {
			ProtectedRegion r = reguns.getRegion("Tree" + s.getX() + s.getY() + s.getZ());
			r.setFlag(DefaultFlag.BUILD, State.ALLOW);
			r.setFlag(DefaultFlag.PVP, State.ALLOW);
		}
	}
	
	public void readConfig() {
		this.useWorldGuard = cfg.readBoolean("useWorldGuard");
		if(useWorldGuard) {
	        Plugin p = getServer().getPluginManager().getPlugin("WorldGuard");
	        if(p == null || !(p instanceof WorldGuardPlugin)) {
	        	loadError("Woopsie daisy, failed to find World Guard! Check if it's properly loaded!");
	        	return;
	        }
	        
	        this.wg = (WorldGuardPlugin) p;
		}
		
		String worldname;
		
		try {
			this.gatherToolId = cfg.readMaterial("gatherToolId");
			this.feedMaterial = cfg.readMaterial("feedMaterial");
			this.xMin = cfg.readPositiveInt("xMin");
	        this.xMax = cfg.readPositiveInt("xMax");
	        this.yMin = cfg.readPositiveInt("yMin");
	        this.yMax = cfg.readPositiveInt("yMax");
	        this.zMin = cfg.readPositiveInt("zMin");
	        this.zMax = cfg.readPositiveInt("zMax");
	        this.spawnInterval = cfg.readPositiveInt("spawnInterval");
	        this.lifespan = cfg.readPositiveInt("lifespan");
	        this.targetFertilizeLevel = cfg.readPositiveInt("targetFertilizeLevel");
	        this.growTime = cfg.readPositiveInt("growTime");
	        this.decayTime = cfg.readPositiveInt("decayTime");
	        
	        worldname = cfg.readString("WORLD");

		} catch (IllegalArgumentException e) {
			loadError(e.getMessage());
			return;
		}

		this.debug = cfg.readBoolean("debug");
		this.withFactions = cfg.readBoolean("withFactions");
		
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
		this.spawnTask.cancel();
		this.autosave.cancel();
		this.getLogger().info("Saving current tree state...");
		save();
		this.getLogger().info("Goodbye my beautiful sponges! :(");
	}
	
	private void loadError(String msg) {
		this.getLogger().warning(msg);
    	this.getLogger().warning("Aborting le sponge trees :(");
    	this.setEnabled(false);
	}

	public SpongeTree spawnKekTree() throws CantSpawnException {
		boolean foundSpawnpoint = false;
		int x = 1, y = 1, z = 1;
		
		// search for a good spawning point!
		while(!foundSpawnpoint) {
			x = rand.nextInt(xMax - xMin) + xMin;
			z = rand.nextInt(zMax - zMin) + zMin;
			for(y = this.yMax; y > this.yMin; --y) {
				if(goodSpawningPoint(x, y, z)) {
    				foundSpawnpoint = true;
    				break;
				}
			}
		}
		
		return new SpongeTree(x, y, z, this);
	}
	
	private boolean allowedMaterials(Material m) {
		return (m == Material.GRASS) ||
			   (m == Material.SAND) ||
			   (m == Material.DIRT);
	}
	
	public boolean goodSpawningPoint(int x, int y, int z) {
		boolean res = false;
		if (allowedMaterials(this.world.getBlockAt(x, y - 1, z).getType()) && 
		   this.world.getBlockAt(x - 1, y, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x - 1, y, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x - 1, y, z + 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y, z + 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y, z + 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x - 1, y + 1, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x - 1, y + 1, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x - 1, y + 1, z + 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y + 1, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y + 1, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y + 1, z + 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y + 1, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y + 1, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y + 1, z + 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x - 1, y + 2, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x - 1, y + 2, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x - 1, y + 2, z + 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y + 2, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y + 2, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x, y + 2, z + 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y + 2, z - 1).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y + 2, z).getType() == Material.AIR &&
		   this.world.getBlockAt(x + 1, y + 2, z + 1).getType() == Material.AIR)
			res = true;
		
		if(this.withFactions && res) {
			Location l;
			BoardColl b = BoardColl.get();
			for(int i = 0; i < 3; ++i) {
				for(int j = 0; j < 3; ++j) {
					l = new Location(this.world, x - 16 + (16 * i), y, z - 16 + (16 * j));
					if(debug()) getLogger().info("Checking for faction at " + l.toString());
					if(b.getFactionAt(PS.valueOf(l)).isNormal())
						return false;
				}
			}
			if(debug()) getLogger().info("No factions in the way, spawning");
		}

		return res;
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public ArrayList<SpongeTree> getTrees() {
		return this.leTrees;
	}
	
	public int getLifeSpan() {
		return this.lifespan;
	}
	
	public int getTargetFertilize() {
		return this.targetFertilizeLevel;
	}
	
	public int getGrowTime() {
		return this.growTime;
	}
	
	public int getDecayTime() {
		return this.decayTime;
	}
	
	public WorldGuardPlugin getWg() {
		return this.wg;
	}
	
	public boolean useWg() {
		return this.useWorldGuard;
	}
	
	public Material getFeedingMaterial() {
		return this.feedMaterial;
	}
	
	public boolean debug() {
		return debug;
	}
	
	public Material getTool() {
		return this.gatherToolId;
	}
	
	public class SpawnerTask extends BukkitRunnable {
		public void run() {
			try {
				KekTrees.this.leTrees.add(KekTrees.this.spawnKekTree());
			} catch (CantSpawnException e) {
				getLogger().warning("CantSpawnException while trying to spawn a new tree:");
				e.printStackTrace();
			}
		}
	}
	
	public class AutoSave extends BukkitRunnable {
		public void run() {
			KekTrees.this.save();
			KekTrees.this.getLogger().info("Autosaving le sponge trees");
		}
	}
	
	public void save() {
		FileOutputStream fout = null;
		ObjectOutputStream out = null;
		try {
			fout = 
				new FileOutputStream(new File(this.getDataFolder(), "trees.ser"));
			out = new ObjectOutputStream(fout);
		} catch (FileNotFoundException e) {
			this.getLogger().warning("Woops! Spongies can't access the SpongeTrees/trees.ser file to save the currently state\nAll existing trees will be lost :(");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			this.getLogger().warning("I/O exception while saving KekTrees state, existing trees will be lost :(");
			e.printStackTrace();
			try {
				fout.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return;
		}
		if(!leTrees.isEmpty()) {
			for(SpongeTree s : leTrees) {
				try {
					out.writeObject(s);
				} catch (IOException e) {
					getLogger().warning("Couldn't save tree: " + s);
					e.printStackTrace();
				}
			}
		}
		
		try {
			out.close();
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void retrieve() {
		FileInputStream fin = null;
		ObjectInputStream in = null;
		try {
			fin = new FileInputStream(new File(this.getDataFolder(), "trees.ser"));
			in = new ObjectInputStream(fin);
		} catch (FileNotFoundException e) {
			getLogger().info("No saved sponge trees found");
			return;
		} catch (IOException e) {
			getLogger().warning("I/O exception while trying to retrieve saved trees");
			try {
				fin.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			return;
		}
		int counter = 0;
		while(true) {
			try {
				SpongeTree s = (SpongeTree) in.readObject();
				s.setPlugin(this);
				s.initBlocks();
				this.leTrees.add(s);
				++counter;
			} catch (EOFException e) {
				break;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				getLogger().warning("I/O Exception while retrieving saved trees");
				e.printStackTrace();
			}
		}
		
		getLogger().info(String.format("%d %s loaded from memory:", counter, counter == 1 ? "tree" : "trees"));
		for(SpongeTree s : leTrees)
			getLogger().info(s.toString());
		
		try {
			fin.close();
			in.close();
		} catch (IOException e) {
			getLogger().warning("I/O Exception while retrieving saved trees");
			e.printStackTrace();
		}
	}
}
