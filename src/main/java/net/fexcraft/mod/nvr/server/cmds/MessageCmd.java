package net.fexcraft.mod.nvr.server.cmds;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.fexcraft.mod.lib.util.common.Log;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.nvr.common.enums.MessageType;
import net.fexcraft.mod.nvr.server.NVR;
import net.fexcraft.mod.nvr.server.data.Message;
import net.fexcraft.mod.nvr.server.util.Sender;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class MessageCmd extends CommandBase {
	
	private static final Log print = InfoCmd.print;

	@Override
	public String getName(){
		return "ms";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return sender.getCommandSenderEntity() instanceof EntityPlayer ? "/ms <args>" : "/ms <args>";
	}
	
	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return !(sender == null || server == null);
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 1){
			print.chat(sender, InfoCmd.space);
			print.chat(sender, "Message Command");
			print.chat(sender, "/ms view <type/all>");
			print.chat(sender, "/ms inbox");
			print.chat(sender, "/ms types");
			print.chat(sender, "/ms read <type> <list-id>");
			print.chat(sender, "/ms send <uuid/ign> <message...>");
			print.chat(sender, InfoCmd.space);
			print.chat(sender, "/ms func <type> <list-id> <command/task/entry>");
			print.chat(sender, "/ms sysm <title> <message...>");
			return;
		}
		boolean isp = sender.getCommandSenderEntity() instanceof EntityPlayer;
		EntityPlayer player = isp ? (EntityPlayer)sender.getCommandSenderEntity() : null;
		switch(args[0]){
			case "view":{
				if(args.length < 2){
					print.chat(sender, "Missing Argument.");
					return;
				}
				MessageType type = MessageType.fromUnknown(args[1]);
				List<Message> list = Message.getAsList(isp ? player.getGameProfile().getId() : UUID.fromString(NVR.CONSOLE_UUID), type);
				print.chat(sender, InfoCmd.space);
				Message message = null;
				for(int i = 0; i < list.size(); i++){
					message = list.get(i);
					print.chat(sender, "&8T: &7" + message.title + " &8B: &7" + Static.getPlayerNameByUUID(message.sender) + "\n&7ID: &9" + i + " &7<|> &5" + Time.getAsString(message.created));
				}
				print.chat(sender, "&3Total: &5" + list.size());
				return;
			}
			case "inbox":{
				List<Message> list = Message.getAsList(isp ? player.getGameProfile().getId() : UUID.fromString(NVR.CONSOLE_UUID), null);
				long sysmsgs = Message.count(list, MessageType.SYSTEM);
				long invmsgs = Message.count(list, MessageType.INVITE);
				long primsgs = Message.count(list, MessageType.PRIVATE);
				String str =
						  (sysmsgs <= 0 ? "" : "&a" + sysmsgs + " &7new System messages.\n")
						+ (invmsgs <= 0 ? "" : "&a" + invmsgs + " &7new Invites.\n")
						+ (primsgs <= 0 ? "" : "&a" + primsgs + " &7new Private Messages.");
				if(str.length() > 0){
					print.chat(sender, "&8/ms view <type/all>\n" + str);
				}
				else{
					print.chat(sender, "&7Inbox is empty.");
				}
				return;
			}
			case "types":{
				print.chat(sender, InfoCmd.space);
				print.chat(sender, "Message Types:");
				for(MessageType type : MessageType.values()){
					print.chat(sender, "&6> &9" + type.name());
				}
				return;
			}
			case "read":{
				if(args.length < 3){
					print.chat(sender, "&7Missing Arguments!");
					print.chat(sender, "&a&o/ms read <type> <list-id>");
					return;
				}
				try{
					Message message = Message.getAsList(isp ? player.getGameProfile().getId() : UUID.fromString(NVR.CONSOLE_UUID), MessageType.fromUnknown(args[1])).get(Integer.parseInt(args[2]));
					if(message.function != null && message.function.has("expires")){
						if(message.function.get("expires").getAsLong() <= Time.getDate()){
							print.chat(sender, "&6Message expired and removed.");
							print.chat(sender, "&9&oSorry for the bother! :)");
							message.setRead("expired");
							print.chat(sender, InfoCmd.space);
							print.chat(sender, "&9From: " + Static.getPlayerNameByUUID(message.sender));
							return;
						}
					}
					print.chat(sender, InfoCmd.space);
					print.chat(sender, "&9Created: &6" + Time.getAsString(message.created));
					print.chat(sender, "&9Title: &6" + message.title);
					print.chat(sender, "&9From: &6" + Static.getPlayerNameByUUID(message.sender));
					print.chat(sender, message.content);
					if(message.function != null && message.function.has("usage")){
						print.chat(sender, InfoCmd.space);
						print.chat(sender, message.function.get("usage").getAsString());
					}
					print.chat(sender, InfoCmd.space);
					if(message.function == null){
						message.setRead();
						print.chat(sender, "&6&oMessage set as 'read', and removed.");
					}
				}
				catch(Exception e){
					print.chat(sender, e.getMessage());
				}
				return;
			}
			case "send":{
				if(args.length < 3){
					print.chat(sender, "&7Missing Arguments!");
					print.chat(sender, "&a&o/ms send <uuid/ign> <message...>");
					return;
				}
				UUID id = Sender.getUUID(print, sender, args[1], null);
				if(id == null){
					print.chat(sender, "&7UUID not found.");
					return;
				}
				try{
					String str = args[2];
					if(args.length > 3){
						for(int i = 3; i < args.length; i++){
							str += " " + args[i];
						}
					}
					Message message = new Message();
					message.content = str;
					message.created = Time.getDate();
					message.function = null;
					message.read = false;
					message.receiver = id;
					message.sender = isp ? player.getGameProfile().getId() : NVR.getConsoleUUID();
					message.title = "Private message";
					message.type = MessageType.PRIVATE;
					message.save();
					NVR.MESSAGES.add(message);
					print.chat(sender, "Message sent!");
					message.notifyIfOnline(print);
				}
				catch(Exception e){
					print.chat(sender, e.getMessage());
					print.stacktrace(sender, e);
				}
				return;
			}
			case "func": case "function": {
				if(args.length < 4){
					print.chat(sender, "&7Missing Arguments!");
					print.chat(sender, "&a&o/ms func <type> <list-id> <cmd>");
					return;
				}
				try{
					Message message = Message.getAsList(isp ? player.getGameProfile().getId() : UUID.fromString(NVR.CONSOLE_UUID), MessageType.fromUnknown(args[1])).get(Integer.parseInt(args[2]));
					if(message.function != null && message.function.has("expires")){
						if(message.function.get("expires").getAsLong() <= Time.getDate()){
							print.chat(sender, "&6Message expired and removed.");
							print.chat(sender, "&9&oSorry for the bother! :)");
							message.setRead("expired");
							print.chat(sender, InfoCmd.space);
							print.chat(sender, "&9From: " + Static.getPlayerNameByUUID(message.sender));
							return;
						}
					}
					if(message.function == null){
						print.chat(sender, "This message has no embedded function(s).");
						return;
					}
					print.chat(sender, "&oprocessing...");
					message.processFunction(print, sender, args[3], player);
				}
				catch(Exception e){
					print.chat(sender, e.getMessage());
				}
				return;
			}
			case "sysm": case "alert": case "toall": case "notifyall":{
				if(isp && !NVR.getPlayerData(player).perms.hasPermission("nvr.admin")){
					print.chat(sender, "No permission.");
					return;
				}
				if(args.length < 3){
					print.chat(sender, "Missing arguments.");
				}
				String[] con = new String[]{args[2]};
				if(args.length > 3){
					for(int i = 3; i < args.length; i++){
						con[0] += " " + args[i];
					}
				}
				long time = Time.getDate();
				UUID from = isp ? player.getGameProfile().getId() : NVR.getConsoleUUID();
				Static.getServer().addScheduledTask(new Runnable(){
					@Override
					public void run(){
						for(String str : Static.getServer().getPlayerProfileCache().getUsernames()){
							GameProfile prf = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(str);
							UUID id = prf == null ? null : prf.getId();
							if(id != null){
								Message message = new Message();
								message.content = con[0];
								message.created = time;
								message.function = null;
								message.read = false;
								message.receiver = id;
								message.sender = from;
								message.title = args[1];
								message.type = MessageType.SYSTEM;
								message.save();
								message.notifyIfOnline(print);
								NVR.MESSAGES.add(message);
							}
						}
					}
				});
				print.chat(sender, "Message queued.");
				return;
			}
		}
		return;
	}
	
}