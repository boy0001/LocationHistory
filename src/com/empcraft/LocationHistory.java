package com.empcraft;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LocationHistory extends JavaPlugin implements Listener {
	LocationHistory plugin;
	public Map<String, Object[]> lastcommand = new HashMap<String, Object[]>();
	public Map<String, Integer> lastloc = new HashMap<String, Integer>();
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Object[] data = new Object[2];
		data[0] = event.getMessage();
		data[1] = System.currentTimeMillis();
		lastcommand.put(event.getPlayer().getName().toLowerCase(),data);
	}
	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
		try {
			lastcommand.remove(event.getPlayer().getName().toLowerCase());
		}
		catch (Exception e) {
			
		}
	}
	public void msg(Player player,String mystring) {
    	if (player==null) {
    		getServer().getConsoleSender().sendMessage(colorise(mystring));
    	}
    	else if (player instanceof Player==false) {
    		getServer().getConsoleSender().sendMessage(colorise(mystring));
    	}
    	else {
    		player.sendMessage(colorise(mystring));
    	}

    }
    @Override
    public void onDisable() {
    	
    }
    public Location getloc(String string,Player user) {
		if (string.contains(",")==false) {
			Player player = Bukkit.getPlayer(string);
			if (player!=null) {
				return player.getLocation();
			}
			else {
				World world = Bukkit.getWorld(string);
				if (world!=null) {
					return world.getSpawnLocation();
				}
			}
			
		}
		else {
			String[] mysplit = string.split(",");
			World world = Bukkit.getWorld(mysplit[0]);
			if (world!=null) {
				double x;double y;double z;
				if (mysplit.length==4) {
					try { x = Double.parseDouble(mysplit[1]);} catch (Exception e) {x=world.getSpawnLocation().getX();}
					try { y = Double.parseDouble(mysplit[2]);} catch (Exception e) {y=world.getSpawnLocation().getY();}
					try { z = Double.parseDouble(mysplit[3]);} catch (Exception e) {z=world.getSpawnLocation().getZ();}
					return new Location(world, x, y, z);
				}
			}
			else {
				return null;
			}
		}
		return null;
	}
    public Location getpast(Player player, Integer last) {
    	try {
    	List<String> myhistory = gethistory(player.getName().toLowerCase());
    	int second = 1;
    	if (last%2==0) {
    		second = 0;
    	}
    	last = last/2;
    	String myloc = getdata(myhistory.get(myhistory.size()-1+last))[2+second];
    	return getloc(myloc, player);
    	}
    	catch (Exception e) {
    		return null;
    	}
    }
    
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	Player player = null;
		try { player = (Player) sender; }catch (Exception e) {}
    	String line = "";
    	for (String i:args) {
    		line+=i+" ";
    	}
    	if(cmd.getName().equalsIgnoreCase("previous")){
    		if (checkperm(player,"locationhistory.previous")) {
	    		Object[] data = new Object[2];
	    		data[0] = cmd.getName();
	    		data[1] = System.currentTimeMillis();
	    		lastcommand.put(player.getName().toLowerCase(),data);
	    		Integer last = lastloc.get(player.getName().toLowerCase());
	    		if (last!=null) {
	    			if (Math.abs(last)>=gethistory(player.getName().toLowerCase()).size()) {
	        			msg(player,"&cHistory doesn't go that far back");
	        			return false;
	        		}
	    			lastloc.put(player.getName().toLowerCase(),last-1);
	    		}
	    		else {
	    			last=-1;
	    			lastloc.put(player.getName().toLowerCase(),-1);
	    		}
	    		boolean same = false;
	    		Location loc = getpast(player, last);
	    		if (loc!=null) {
	    		if (loc.distanceSquared(player.getLocation()) < 4&&loc!=null) {
	    			same = true;
	    		}
	    		else {
	    			msg(player,getpast(player, last+1).toString() + " | " + loc.toString());
	    		}
	    		while (same) {
	    			if (Math.abs(last)>=gethistory(player.getName().toLowerCase()).size()) {
	        			msg(player,"&cHistory doesn't go that far back");
	        			return false;
	        		}
	    			last-=1;
					lastloc.put(player.getName().toLowerCase(),last);
					loc = getpast(player, last);
					if (loc.distanceSquared(player.getLocation()) < 4 && loc!=null) {
		    			same = true;
		    		}
					else {
						same = false;
					}
	    		}
	    			player.teleport(loc);
	    		}
	    		else {
	    			msg(player,"&clocation does not exist");
	    		}
    		}
    		else {
    			msg(player,"&cYou do not have permission to perform this command");
    		}
    	}
    	else if(cmd.getName().equalsIgnoreCase("next")){
    		if (checkperm(player,"locationhistory.next")) {
	    		Object[] data = new Object[2];
	    		data[0] = cmd.getName();
	    		data[1] = System.currentTimeMillis();
	    		lastcommand.put(player.getName().toLowerCase(),data);
	    		Integer last = lastloc.get(player.getName().toLowerCase());
	    		if (last!=null) {
	    			lastloc.put(player.getName().toLowerCase(),last+1);
	    		}
	    		else {
	    			last = 1;
	    		}
	    		if (last>0) {
	    			msg(player,"&7A time machine is required to get your &cnext location&7.");
	    		}
	    		else {
	    			Location loc = getpast(player, last);
	        		if (loc!=null) {
	        			player.teleport(loc);
	        		}
	        		else {
	        			msg(player,"&clocation does not exist");
	        		}
	    		}
    		}
    		else {
    			msg(player,"&cYou do not have permission to perform this command");
    		}
    	}
    	else if(cmd.getName().equalsIgnoreCase("history")){
    		if (checkperm(player,"locationhistory.history.self")) {
		    		List<String> myhistory = gethistory(player.getName().toLowerCase());
		    		int page = 1;
		    		int total = myhistory.size();
		    		
		    		
		    		
		    		if (args.length > 0) {
		    			if (args.length>1) {
		    				if (checkperm(player,"locationhistory.history.other")) {
			    				myhistory = gethistory(args[0]);
			    				if (myhistory==null) {
			    					total = 0;
			    					msg(player,"&cNo history found for: "+args[0]);
			    				}
			    				else {
			    					total=myhistory.size();
			    					args[0] = args[1];
			    				}
		    				}
		    			}
		    			List<String> test = gethistory(args[0]);
						if (test!=null) {
							if (checkperm(player,"locationhistory.history.self")) {
								myhistory=test;
								total = test.size();
								args[0] = "1";
							}
						}
		    			try {
		    				page = Integer.parseInt(args[0]);
		    				myhistory = myhistory.subList(Math.min(Math.max(myhistory.size()-(page)*16,0), myhistory.size()),Math.max(myhistory.size()-(page-1)*16,0));
		    				if (myhistory.size()==0) {
		    					msg(player,"&cYour history doesn't go back that far.");
		    				}
		    			}
		    			catch (Exception e) {
		    				myhistory=null;
		    			}
		    		}
		    		else {
		    			myhistory = myhistory.subList(0, Math.min(16,myhistory.size()));
		    		}
		    		if (myhistory!=null) {
		    			String lastdate = "";
		    			for (String current:myhistory) {
		    				String toprint = "";
		    				String[] data = getdata(current);
		    				if (data[0].equals(lastdate)==false) {
		    					lastdate = data[0];
		    					toprint += "(&6"+data[0].replace("/", "&7/&6")+"&7) | &3Time &7 | &cFrom&7 | &aTo &7| &9Command &7\n";
		    				}
		    				toprint += "&7 - &3"+data[1].replace(":","&7:&3")+" ";
		    				toprint += "&c"+data[2].replace(",","&7,&c")+" ";
		    				toprint += "&a"+data[3].replace(",","&7,&a")+" ";
		    				toprint += "&9"+data[4];
		    				msg(player,toprint);
		    			}
		    			msg(player,"&7Showing results &8"+(total-Math.max(total-(page-1)*16,0))+" &7to&8 "+(total-Math.min(Math.max(total-(page)*16,0), total))+"&7 of &8"+total+"&7 - /history <#>");
		    		}
		    		else {
		    			msg(player,"&cNo teleportation history found");
		    		}
    		}
    		else {
    			msg(player,"&cYou do not have permission to perform this command");
    		}
    	}
    	else {
    	}
    	return false;
	}
	
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		String playername = player.getName().toLowerCase();
		File yamlFile = new File(getDataFolder()+File.separator+"data"+File.separator+playername+".yml");
		if (yamlFile.exists()==false) {
			try { 
				yamlFile.createNewFile();
			} catch (IOException e) { }
		}
		String reason = "";
		if (lastcommand.containsKey(playername)) {
			Object[] lastcmd = lastcommand.get(playername);
			if (System.currentTimeMillis()-((long) lastcmd[1])<60) {
				reason = ""+lastcmd[0];
				if (reason.trim().equalsIgnoreCase("previous")) {
					return;
				}
				if (reason.trim().equalsIgnoreCase("next")) {
					return;
				}
			}
			lastcommand.remove(playername);
		}
		YamlConfiguration myhistory = YamlConfiguration.loadConfiguration(yamlFile);
		List<String> mylist;
		if (myhistory.contains("history")) {
			mylist = myhistory.getStringList("history");
		}
		else {
			mylist = new ArrayList<String>();
		}
		Location myloc = player.getLocation();
		Location loc = event.getTo();
		DateFormat dateFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
		Calendar calender = Calendar.getInstance();
		dateFormat.format(calender.getTime());
		mylist.add(dateFormat.format(calender.getTime())+" "+myloc.getWorld().getName()+","+Math.round(myloc.getX())+","+Math.round(myloc.getY())+","+Math.round(myloc.getZ())+" "+loc.getWorld().getName()+","+Math.round(loc.getX())+","+Math.round(loc.getY())+","+Math.round(loc.getZ())+ " " + reason);
		if (mylist.size()>getConfig().getInt("max-history-size")) {
			mylist = mylist.subList(mylist.size()-getConfig().getInt("max-history-size"), mylist.size());
		}
		try {
		if (getdata(mylist.get(mylist.size()-1))[1].equals(getdata(mylist.get(mylist.size()-2))[1])==false) {
			if ((getdata(mylist.get(mylist.size()-1))[2].equals(getdata(mylist.get(mylist.size()-2))[2])==false)&&(getdata(mylist.get(mylist.size()-1))[3].equals(getdata(mylist.get(mylist.size()-2))[3])==false)) {
				if (reason.contains("/previous")==false&&reason.contains("/next")==false) {
					myhistory.set("history", mylist);
					lastloc.remove(player.getName().toLowerCase());
				}
			}
		}
		}
		catch (Exception e) {
			myhistory.set("history", mylist);
		}
		try {
			myhistory.save(yamlFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String[] getdata(String string) {
		String[] data = new String[5];
		data[0] = string.substring(0,8);
		data[1] = string.substring(9,17);
		String[] list = string.substring(18,string.length()).split(" ");
		data[2] = list[0];
		data[3] = list[1];
		try {
		data[4] = "&7/&9"+string.split("/")[3].trim();
		}
		catch (Exception e) {
			data[4] = "";
		}
		return data;
	}
	public List<String> gethistory(String key) {
		File yamlFile = new File(getDataFolder()+File.separator+"data"+File.separator+key.toLowerCase()+".yml");
		YamlConfiguration.loadConfiguration(yamlFile);
		try {
		List<String> myconfig = YamlConfiguration.loadConfiguration(yamlFile).getStringList("history");
		if (myconfig.size()>0) {
			return myconfig;
		}
		return null;
		}
		catch (Exception e) {
			return null;
		}
	}
	@Override
    public void onEnable(){
		plugin = this;
		File f3 = new File(getDataFolder() + File.separator+"english.yml");
		saveResource("data"+File.separator+"notch.yml", false);
        if(f3.exists()!=true) {  saveResource("english.yml", false); }
        getConfig().options().copyDefaults(true);
        final Map<String, Object> OPTIONS = new HashMap<String, Object>();
        getConfig().set("version", "0.0.1");
        OPTIONS.put("max-history-size",10);
        for (final Entry<String, Object> node : OPTIONS.entrySet()) {
       	 	if (!getConfig().contains(node.getKey())) {
       	 		getConfig().set(node.getKey(), node.getValue());
       	 	}
        }
        saveConfig();
        this.saveDefaultConfig();
    	getServer().getPluginManager().registerEvents(this, this);
	}
	public String getmsg(String key) {
		File yamlFile = new File(getDataFolder(), getConfig().getString("language").toLowerCase()+".yml"); 
		YamlConfiguration.loadConfiguration(yamlFile);
		try {
			return colorise(YamlConfiguration.loadConfiguration(yamlFile).getString(key));
		}
		catch (Exception e){
			return "";
		}
	}
	
    public boolean checkperm(Player player,String perm) {
    	boolean hasperm = false;
    	String[] nodes = perm.split("\\.");
    	
    	String n2 = "";
    	if (player==null) {
    		return true;
    	}
    	else if (player.hasPermission(perm)) {
    		hasperm = true;
    	}
    	else if (player.hasPermission("*")) {
			return true;
    	}
    	else if (player.isOp()==true) {
    			hasperm = true;
    	}
    	else {
    		for(int i = 0; i < nodes.length-1; i++) {
    			n2+=nodes[i]+".";
            	if (player.hasPermission(n2+"*")) {
            		hasperm = true;
            	}
    		}
    	}
		return hasperm;
    }
    public String colorise(String mystring) {
    	String[] codes = {"&1","&2","&3","&4","&5","&6","&7","&8","&9","&0","&a","&b","&c","&d","&e","&f","&r","&l","&m","&n","&o","&k"};
    	for (String code:codes) {
    		mystring = mystring.replace(code, "§"+code.charAt(1));
    	}
    	return mystring;
    }
}
