package net.fexcraft.mod.nvr.server.cmds;

import java.util.UUID;

import net.fexcraft.mod.lib.util.common.Log;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.lang.ArrayList;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.nvr.common.enums.DistrictType;
import net.fexcraft.mod.nvr.server.NVR;
import net.fexcraft.mod.nvr.server.data.Chunk;
import net.fexcraft.mod.nvr.server.data.District;
import net.fexcraft.mod.nvr.server.data.Municipality;
import net.fexcraft.mod.nvr.server.util.Sender;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class DistrictCmd extends CommandBase {
	
	private static final Log print = InfoCmd.print;

	@Override
	public String getName(){
		return "dis";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return sender.getCommandSenderEntity() instanceof EntityPlayer ? "/dis <args>" : "/dis <args>";
	}
	
	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return !(sender == null || server == null);
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1){
			print.chat(sender, InfoCmd.space);
			print.chat(sender, "District Command");
			print.chat(sender, "/dis <id> set ?");
			print.chat(sender, "/dis <id> set <option> <value>");
			print.chat(sender, "/dis <id> reset <option>");
			print.chat(sender, "/dis <id> types");
			print.chat(sender, InfoCmd.space);
			print.chat(sender, "/dis create <municipalityid> <name...>");
			return;
		}
		boolean isp = sender.getCommandSenderEntity() instanceof EntityPlayer;
		EntityPlayer player = isp ? (EntityPlayer)sender.getCommandSenderEntity() : null;
		if(args.length < 2){
			print.chat(sender, "Missing Arguments.");
			return;
		}
		if(args[0].equals("create")){
			Municipality mun = NVR.getMunicipality(args, 1);
			if(mun == null){
				print.chat(sender, "Municipality not found.");
				return;
			}
			if(isp && !mun.management.contains(player.getGameProfile().getId())){
				print.chat(sender, "No Permission.");
				return;
			}
			if(mun.districts() >= mun.type.districtLimit()){
				print.chat(sender, "Current District limit reached, your Municipality needs more citizen!");
				return;
			}
			if(args.length < 3){
				print.chat(sender, "Missing District Name.");
				return;
			}
			String name = args[2];
			if(args.length > 3){
				for(int i = 3; i < args.length; i++){
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
			if(ck.district.municipality.id != mun.id){
				print.chat(sender, "Chunk's current district is part of another Municipality.");
				return;
			}
			//
			District dis = new District();
			dis.id = NVR.DISTRICTS.lastKey() + 1;
			dis.type = DistrictType.UNSPECIFIED;
			dis.name = name;
			dis.municipality = mun;
			dis.manager = null;
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
			Sender.serverMessage("&7" + NVR.getPlayerData(player).getNick(sender) + " created &9'" + name + "' &7(district:" + dis.id + ")");
			//
			ck.district = dis;
			ck.changed = Time.getDate();
			return;
		}
		District dis = NVR.getDistrict(args, 0);
		switch(args[1]){
			case "set":{
				if(args.length < 3){
					print.chat(sender, "&8Missing &7'option' &8Argument!");
					return;
				}
				if(args[2].equals("?") || args[2].equals("help")){
					print.chat(sender, InfoCmd.space);
					print.chat(sender, "Available Options:");
					print.chat(sender, "&9id: forsale | &6value: number");
					print.chat(sender, "&9id: manager | &6value: uuid/ign");
					print.chat(sender, "&9id: color | &6value: hex color code");
					print.chat(sender, "&9id: name | &6value: string");
					print.chat(sender, "&9id: type | &6value: district type");
					print.chat(sender, "&8(see &7'/dis <id> types'&8)");
					print.chat(sender, "&9id: tax | &6value: number");
				}
				else{
					if(isp && !dis.canEdit(player)){
						print.chat(sender, "No Permission.");
						return;
					}
					if(args.length < 4){
						print.chat(sender, "&8Missing &7'value' &8Argument!");
						return;
					}
					switch(args[2]){
						case "forsale":{
							dis.price = Sender.getDouble(print, sender, args[3], 0);
							print.chat(sender, "&7Price updated! &8(&6" + dis.price + "&8);");
							print.chat(sender, "&7Note that a price of &6'0' &7makes the district not buy-able!");
							break;
						}
						case "manager":{
							if(args[3].equals("null")){
								dis.manager = null;
							}
							else{
								dis.manager = Sender.getUUID(print, sender, args[3], null);
							}
							if(dis.manager != null){
								print.chat(sender, "&7Assigned new Manager!");
								print.chat(sender, dis.manager == null ? "" : "&8" + Static.getPlayerNameByUUID(dis.manager) + " &7(" + dis.manager.toString() + ")");
							}
							else{
								print.chat(sender, "&7Deassigned Manager!");
							}
							break;
						}
						case "color":{
							dis.colour = Sender.getColor(print, sender, args[3], "#0f0f0f");
							print.chat(sender, "&7Colour Updated! &9(&6" + dis.colour + "&9);");
							break;
						}
						case "name":{
							String str = args[3];
							if(args.length > 4){
								for(int i = 4; i < args.length; i++){
									str += " " + args[i];
								}
							}
							dis.name = str;
							print.chat(sender, "&7Name Updated!");
							print.chat(sender, "&6" + dis.name);
							break;
						}
						case "type":{
							dis.type = DistrictType.fromString(args[3]);
							print.chat(sender, "&7Type Updated!");
							print.chat(sender, "&6" + dis.type.name());
							break;
						}
						case "tax":{
							dis.tax = Sender.getDouble(print, sender, args[3], 0);
							print.chat(sender, "&7Chunk tax updated! &8(&6" + dis.tax + "&8);");
							break;
						}
					}
					dis.changed = Time.getDate();
					dis.save();
					//TODO add logging
				}
				break;
			}
			case "reset":{
				if(isp && !dis.canEdit(player)){
					print.chat(sender, "No Permission.");
					return;
				}
				switch(args[2]){
					case "forsale":{
						dis.price = 0;
						break;
					}
					case "manager":{
						dis.manager = null;
						break;
					}
					case "color":{
						dis.colour = "#0f0f0f";
						break;
					}
					case "name":{
						dis.name = "Unnamed District";
						break;
					}
					case "type":{
						dis.type = DistrictType.UNSPECIFIED;
						break;
					}
					case "tax":{
						dis.tax = 0;
						break;
					}
				}
				dis.changed = Time.getDate();
				dis.save();
				print.chat(sender, "Value resseted!");
				//TODO add logging
				break;
			}
			case "types":{
				print.chat(sender, InfoCmd.space);
				print.chat(sender, "District Types:");
				for(DistrictType type : DistrictType.values()){
					print.chat(sender, "&6> &9" + type.name());
				}
				break;
			}
		}
	}
	
}