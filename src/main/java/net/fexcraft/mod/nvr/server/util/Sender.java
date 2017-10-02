package net.fexcraft.mod.nvr.server.util;

import java.awt.Color;
import java.util.List;
import java.util.UUID;

import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Log;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.nvr.server.NVR;
import net.fexcraft.mod.nvr.server.data.Player;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import scala.actors.threadpool.Arrays;

public class Sender {
	
	public static final void serverMessage(String string){
		Static.getServer().getPlayerList().sendMessage(new TextComponentString(string));
	}
	
	public static final void sendLocationUpdate(EntityPlayer player, String ms, String mg, String color, Integer time){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("target_listener", "nvr-cl");
		nbt.setString("task", "show");
		Player data = NVR.getPlayerData(player);
		writeIcon(nbt, data.lastseen.municipality.icon, 0, color);
		writeIcon(nbt, data.lastseen.municipality.province.icon, 1, color);
		writeIcon(nbt, data.lastseen.municipality.province.nation.icon, 2, color);
		nbt.setString(  "up", ms == null ? "" : ms);
		nbt.setString("down", mg == null ? "" : mg);
		if(time != null){
			nbt.setInteger("time", time);
		}
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(nbt), (EntityPlayerMP)player);
	}
	
	@SuppressWarnings("rawtypes")
	private static final List colours = Arrays.asList(new String[]{"green", "yellow", "red", "blue"});

	private static final void writeIcon(NBTTagCompound compound, String icon, int id, String color){
		if(icon != null && !icon.equals("")){
			if(colours.contains(icon)){
				compound.setString("color_" + id, icon);
			}
			else{
				compound.setString("icon_" + id, icon);
			}
		}
		else if(color == null){
			compound.setInteger("x_" + id, 64);
			compound.setInteger("y_" + id, 224);
		}
		else{
			compound.setString("color_" + id, color);
		}
	}
	
	public static final double getDouble(Log log, ICommandSender sender, String string, double def){
		try{
			return Double.parseDouble(string);
		}
		catch(Exception e){
			log.chat(sender, e.getMessage());
			return def;
		}
	}

	public static UUID getUUID(Log log, ICommandSender sender, String string, String def){
		try{
			return UUID.fromString(string);
		}
		catch(Exception e){
			try{
				UUID uuid = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(string).getId();
				return uuid;
			}
			catch(Exception ex){
				log.chat(sender, e.getMessage());
				log.chat(sender, ex.getMessage());
				return UUID.fromString(def.toString());
			}
			//log.chat(sender, e.getMessage());
			//return UUID.fromString(def.toString());
		}
	}

	public static String getColor(Log log, ICommandSender sender, String string, String def){
		if(string.contains("#") && string.length() <= 7){
			try{
				Color color = new Color(Integer.decode(string));
				if(color == null || color.equals(Color.BLACK)){
					log.chat(sender, "Parse Error.");
					return def;
				}
				return string;
			}
			catch(Exception e){
				log.chat(sender, e.getMessage());
				return def;
			}
		}
		log.chat(sender, "Invalid Hex Color Code.");
		return def;
	}
	
}