package dzjkb.airsquids.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dzjkb.AirSquids.AirSquids;

public class AirSquidsCommand implements CommandExecutor {
	private AirSquids kek;

	public SpongiesCommand(AirSquids plugin) {
		this.kek = plugin;
	}

	@Override
	public boolean onCommand(CommandSender player, Command command, String arg2, String[] arguments) {
		if (!command.getName().equalsIgnoreCase("airsquids")) return false;
		if(!player.hasPermission("airsquids.command") && !player.isOp()) {
			player.sendMessage("Uh oh Spaghetti-O's, you can't use this command!");
			return true;
		} else {
			if(arguments.length == 0) {
				help(player);
				return true;
			}
			switch (arguments[0]) {
			case "help":
				help(player);
				return true;
				
			case "reload":
				this.kek.reloadConfig();
				this.kek.readConfig();
				player.sendMessage("Config reloaded");
				return true;

			default:
				help(player);
				return true;
			}
		}
	}
	
	private void help(CommandSender player) {
		player.sendMessage((Object)ChatColor.BLUE + "--------------------" + (Object)ChatColor.DARK_GREEN + "[" + (Object)ChatColor.GREEN + "Kektrees" + (Object)ChatColor.DARK_GREEN + "]" + (Object)ChatColor.BLUE + "------------------");
		player.sendMessage((Object)ChatColor.RED + "Flying squids, WHAT?!");
		player.sendMessage((Object)ChatColor.RED + "./airsquids reload - Reloads the config");
		player.sendMessage((Object)ChatColor.BLUE + "--------------------" + (Object)ChatColor.DARK_GREEN + "[" + (Object)ChatColor.GREEN + "Kektrees" + (Object)ChatColor.DARK_GREEN + "]" + (Object)ChatColor.BLUE + "------------------");
	}

}
