package net.fexcraft.mod.nvr.server.cmds;

import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.account.AccountManager.Account;
import net.fexcraft.mod.lib.util.common.Log;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.lang.ArrayList;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.nvr.common.enums.DistrictType;
import net.fexcraft.mod.nvr.common.enums.MessageType;
import net.fexcraft.mod.nvr.common.enums.MunicipalityType;
import net.fexcraft.mod.nvr.server.NVR;
import net.fexcraft.mod.nvr.server.data.Chunk;
import net.fexcraft.mod.nvr.server.data.District;
import net.fexcraft.mod.nvr.server.data.Message;
import net.fexcraft.mod.nvr.server.data.Municipality;
import net.fexcraft.mod.nvr.server.data.Player;
import net.fexcraft.mod.nvr.server.util.Sender;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class MunicipalityCmd extends CommandBase {
	
	private static final Log print = InfoCmd.print;

	@Override
	public String getName(){
		return "mun";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return sender.getCommandSenderEntity() instanceof EntityPlayer ? "/mun <args>" : "/mun <args>";
	}
	
	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return !(sender == null || server == null);
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1){
			print.chat(sender, InfoCmd.space);
			print.chat(sender, "Municipality Command");
			print.chat(sender, "/mun <id> set ?");
			print.chat(sender, "/mun <id> set <option> <value>");
			print.chat(sender, "/mun <id> reset <option>");
			print.chat(sender, "/mun <id> types");
			print.chat(sender, InfoCmd.space);
			print.chat(sender, "/mun create <name...>");
			print.chat(sender, "/mun <id> invite <playername>");
			print.chat(sender, "/mun <id> kick <playername> <optional:time-in-minutes>");
			return;
		}
		boolean isp = sender.getCommandSenderEntity() instanceof EntityPlayer;
		EntityPlayer player = isp ? (EntityPlayer)sender.getCommandSenderEntity() : null;
		if(args.length < 2){
			print.chat(sender, "Missing Arguments.");
			return;
		}
		if(args[0].equals("create")){
			if(!isp){
				print.chat(sender, "Currently only usable ingame.");
				return;
			}
			Player playerdata = NVR.getPlayerData(player);
			if(playerdata.municipality.id >= 0){
				print.chat(sender, "You need to leave your current municipality first!");
				return;
			}
			if(args.length < 2){
				print.chat(sender, "Missing Municipality Name.");
				return;
			}
			String name = args[1];
			if(args.length > 2){
				for(int i = 2; i < args.length; i++){
					name = " " + args[i];
				}
			}
			if(name == null){
				print.chat(sender, "NAME NULL, ERROR;");
				return;
			}
			//
			Chunk ck = isp ? NVR.getChunk(player) : null;
			if(ck == null){
				print.chat(sender, "CHUNK, NULL;");
				return;
			}
			if(ck.district.municipality.id >= 0){
				print.chat(sender, "Chunk's current district is part of another Municipality.");
				return;
			}
			Municipality mun = new Municipality();
			mun.id = NVR.MUNICIPALITIES.lastKey() + 1;
			mun.name = name;
			mun.type = MunicipalityType.HAMLET;
			mun.province = NVR.getProvince(-1);
			mun.management = new ArrayList<UUID>();
			mun.management.add(player.getGameProfile().getId());
			mun.neighbors = new ArrayList<Integer>();
			mun.creator = player.getGameProfile().getId();
			mun.created = Time.getDate();
			mun.changed = Time.getDate();
			mun.previncome = 0;
			mun.citizens = new ArrayList<UUID>();
			mun.citizens.add(player.getGameProfile().getId());
			mun.citizentax = 0;
			mun.icon = "icon";
			mun.colour = "#f0f0f0";
			mun.account = Account.getAccountManager().loadAccount("municipality", "municipality:" + mun.id);
			mun.open = false;
			playerdata.municipality = mun;
			print.chat(sender, "Municipality created with ID '" + mun.id + "'!");
			Sender.serverMessage("&7" + playerdata.getNick(sender) + " created &9'" + name + "' &7(municipality:" + mun.id + ")");
			//
			District dis = new District();
			dis.id = NVR.DISTRICTS.lastKey() + 1;
			dis.type = DistrictType.UNSPECIFIED;
			dis.name = name;
			dis.municipality = mun;
			dis.manager = player.getGameProfile().getId();
			dis.creator = isp ? player.getGameProfile().getId() : UUID.fromString(NVR.CONSOLE_UUID);
			dis.created = Time.getDate();
			dis.changed = Time.getDate();
			dis.neighbors = new ArrayList<Integer>();
			dis.previncome = 0;
			dis.tax = 0;
			dis.colour = "#f0f0f0";
			dis.price = 0;
			NVR.DISTRICTS.put(dis.id, dis);
			print.chat(sender, "District created with ID '" + dis.id + "'!");
			Sender.serverMessage("&7" + playerdata.getNick(sender) + " created &9'" + name + "' &7(district:" + dis.id + ")");
			//
			ck.district = dis;
			ck.changed = Time.getDate();
			return;
		}
		Municipality mun = NVR.getMunicipality(args, 0);
		if(mun == null){
			print.chat(sender, "Municipality not found.");
			return;
		}
		switch(args[1]){
			case "set":{
				if(args.length < 3){
					print.chat(sender, "&8Missing &7'option' &8Argument!");
					return;
				}
				if(args[2].equals("?") || args[2].equals("help")){
					print.chat(sender, InfoCmd.space);
					print.chat(sender, "Available Options:");
					print.chat(sender, "&9id: icon | &6value: url/etc");
					print.chat(sender, "&9id: open | &6value: boolean");
					print.chat(sender, "&9id: color | &6value: hex color code");
					print.chat(sender, "&9id: name | &6value: string");
					print.chat(sender, "&9id: citizentax | &6value: number");
					print.chat(sender, "&9id: management <add/rem> | &6value: uuid/ign");
				}
				else{
					if(isp && !mun.canEdit(player)){
						print.chat(sender, "No Permission.");
						return;
					}
					if(args.length < 4){
						print.chat(sender, "&8Missing &7'value' &8Argument!");
						return;
					}
					switch(args[2]){
						case "icon":{
							mun.icon = args[3];
							print.chat(sender, "&7Icon updated! &8(&6" + mun.icon + "&8);");
							break;
						}
						case "management":{
							if(args.length < 5){
								print.chat(sender, "&8Missing &7'uuid/ign' &8Argument!");
							}
							switch(args[3]){
								case "add":{
									UUID id = Sender.getUUID(print, sender, args[4], null);
									if(id == null){
										print.chat(sender, "&8Invalid UUID or Username.");
									}
									else{
										mun.management.add(id);
										print.chat(sender, "&8Added &7'" + args[4] + "' &8!");
									}
									break;
								}
								case "rem": case "remove":{
									UUID id = Sender.getUUID(print, sender, args[4], null);
									if(id == null){
										print.chat(sender, "&8Invalid UUID or Username.");
									}
									else{
										mun.management.remove(id);
										print.chat(sender, "&8Removed &7'" + args[4] + "' &8!");
									}
									break;
								}
							}
							break;
						}
						case "color":{
							mun.colour = Sender.getColor(print, sender, args[3], "#0f0f0f");
							print.chat(sender, "&7Colour Updated! &9(&6" + mun.colour + "&9);");
							break;
						}
						case "name":{
							String str = args[3];
							if(args.length > 4){
								for(int i = 4; i < args.length; i++){
									str += " " + args[i];
								}
							}
							mun.name = str;
							print.chat(sender, "&7Name Updated!");
							print.chat(sender, "&6" + mun.name);
							break;
						}
						case "open":{
							mun.open = Boolean.parseBoolean(args[3]);
							print.chat(sender, "&7Municipality is now: " + (mun.open ? "&aopen" : "&cclosed") + "&7.");
							break;
						}
						case "citizentax": case "tax":{
							mun.citizentax = Sender.getDouble(print, sender, args[3], 0);
							print.chat(sender, "&7Chunk tax updated! &8(&6" + mun.citizentax + "&8);");
							break;
						}
					}
					mun.changed = Time.getDate();
					mun.save();
					//TODO add logging
				}
				break;
			}
			case "reset":{
				if(isp && !mun.canEdit(player)){
					print.chat(sender, "No Permission.");
					return;
				}
				switch(args[2]){
					case "icon":{
						mun.icon = "null";
						break;
					}
					case "color":{
						mun.colour = "#0f0f0f";
						break;
					}
					case "name":{
						mun.name = "Unnamed Place";
						break;
					}
					case "citizentax": case "tax":{
						mun.citizentax = 0;
						break;
					}
				}
				mun.changed = Time.getDate();
				mun.save();
				print.chat(sender, "Value resseted!");
				//TODO add logging
				break;
			}
			case "types":{
				print.chat(sender, InfoCmd.space);
				print.chat(sender, "Municipality Types:");
				for(DistrictType type : DistrictType.values()){
					print.chat(sender, "&6> &9" + type.name());
				}
				break;
			}
			case "kick":{
				if(args.length < 3){
					print.chat(sender, "&8Missing &7'uuid/ign' &8Argument!");
				}
				UUID id = Sender.getUUID(print, sender, args[2], null);
				if(id == null){
					print.chat(sender, "UUID not found.");
					break;
				}
				mun.citizens.remove(id);
				EntityPlayer player2 = Static.getServer().getPlayerList().getPlayerByUUID(id);
				if(!(player2 == null)){
					NVR.getPlayerData(player2).municipality = NVR.getMunicipality(-1);
					Sender.serverMessage(NVR.getPlayerData(player).getNick(sender) + "&7 kicked " + player2.getName() + " from the municipality!");
				}
				else{
					Sender.serverMessage(NVR.getPlayerData(player).getNick(sender) + "&7 kicked " + args[2] + " from the municipality!");
				}
			}
			case "invite":{
				if(args.length < 3){
					print.chat(sender, "&8Missing &7'uuid/ign' &8Argument!");
				}
				UUID id = Sender.getUUID(print, sender, args[2], null);
				if(id == null){
					print.chat(sender, "UUID not found.");
					break;
				}
				
				//TODO make Message.newInvite(type, string/id);
				
				JsonObject obj = new JsonObject();
				obj.addProperty("type", "invite");
				obj.addProperty("to", "municipality");
				obj.addProperty("toid", mun.id);
				obj.addProperty("expires", args.length >= 4 ? (Integer.parseInt(args[3]) * 1000) : 0);
				obj.addProperty("usage", "Reply with either `accept` or `deny`.\n&o/ms func invite <list-id> <accept/deny>");
				Message msg = new Message();
				msg.read = false;
				msg.content = "&7You are invited to join the Municipality of &9" + mun.name;
				msg.title = "Invite (M)";
				msg.function = obj;
				msg.receiver = id;
				msg.sender = isp ? player.getGameProfile().getId() : NVR.getConsoleUUID();
				msg.type = MessageType.INVITE;
				msg.created = Time.getDate();
				msg.save();
				NVR.MESSAGES.add(msg);
				msg.notifyIfOnline(print);
				print.chat(sender, "Invite sent.");
				if(Static.getServer().getPlayerList().getPlayerByUUID(id) != null){
					print.chat(Static.getServer().getPlayerList().getPlayerByUUID(id), "&7You got a new invitation! &8/ms");
				}
			}
			
		}
		
	}
	
}