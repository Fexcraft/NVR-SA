package net.fexcraft.mod.nvr.server.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.common.Log;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.nvr.common.enums.MessageType;
import net.fexcraft.mod.nvr.server.NVR;
import net.fexcraft.mod.nvr.server.util.Sender;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class Message {
	
	public boolean read;
	public JsonObject function;
	public MessageType type;
	public String content, title;
	public UUID receiver, sender;
	public long created;
	
	public Message(){}
	
	public Message(JsonObject obj){
		if(obj == null || !obj.has("read") || !obj.has("receiver")){
			this.read = true;
			return;
		}
		this.read = JsonUtil.getIfExists(obj, "read", false);
		this.function = obj.has("function") ? obj.get("function").getAsJsonObject() : null;
		this.type = MessageType.fromString(JsonUtil.getIfExists(obj, "type", "system"));
		this.content = JsonUtil.getIfExists(obj, "content", "no message content");
		this.title = JsonUtil.getIfExists(obj, "title", "no message title");
		this.receiver = UUID.fromString(JsonUtil.getIfExists(obj, "receiver", NVR.CONSOLE_UUID));
		this.sender = UUID.fromString(JsonUtil.getIfExists(obj, "sender", NVR.CONSOLE_UUID));
		this.created = JsonUtil.getIfExists(obj, "created", 0).longValue();
	}
	
	public void save(){
		try{
			File file = new File(NVR.MESSAGE_DIR, created + "_" + receiver.toString() + ".json");
			JsonObject obj = JsonUtil.get(file);
			obj.addProperty("read", read);
			if(!(function == null)){
				obj.add("function", function);
			}
			obj.addProperty("type", type.name());
			obj.addProperty("receiver", receiver.toString());
			obj.addProperty("sender", sender.toString());
			obj.addProperty("created", created);
			obj.addProperty("content", content);
			obj.addProperty("title", title);
			JsonUtil.write(file, obj);
		}
		catch(Exception e){
			e.printStackTrace();
			Static.stop();
		}
	}
	
	public void setRead(){
		this.read = true;
		this.save();
		NVR.MESSAGES.remove(this);
	}

	public void setRead(String string){
		if(this.function != null){
			this.function.addProperty("removed-due", string);
		}
		this.setRead();
	}
	
	public void processFunction(Log print, ICommandSender sender, String str, EntityPlayer player){
		switch(type){
			case INVITE:{
				if(str == null){
					print.chat(sender, "Available commands: accept, deny.");
				}
				else switch(str){
					case "accept":{
						if(this.function.get("to").getAsString().equals("municipality")){
							Municipality mun = NVR.MUNICIPALITIES.get(this.function.get("toid").getAsInt());
							if(mun != null){
								if(player == null){
									print.chat(sender, "Only executable by an ingame-player.");
									break;
								}
								NVR.getPlayerData(player).municipality = mun;
								NVR.getPlayerData(player).save(player.getGameProfile().getId());
								print.chat(sender, "Done.");
								Sender.serverMessage(NVR.getPlayerData(player).getNick(sender) + " joined the Municipality of " + mun.name);
							}
							else{
								print.chat(sender, "Municipality not found.");
							}
						}
						this.setRead("accepted");
						break;
					}
					case "deny":{
						print.chat(sender, "Diened.");
						this.setRead("denied");
						break;
					}
				}
				break;
			}
			case PRIVATE:{
				
				break;
			}
			case SYSTEM:{
				
				break;
			}
		}
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Message){
			Message msg = ((Message)obj);
			return msg.receiver.equals(receiver) && msg.created == created;
		}
		return false;
	}
	
	public static List<Message> getAsList(UUID uuid, MessageType type){
		return Arrays.asList(NVR.MESSAGES.stream().filter(p -> p.receiver.equals(uuid) && (type == null ? true : p.type == type)).toArray(Message[]::new));
	}
	
	public static long count(List<Message> list, MessageType type){
		return list.stream().filter(pre -> pre.type == type).count();
	}

	public void notifyIfOnline(Log print){
		EntityPlayer player = Static.getServer().getPlayerList().getPlayerByUUID(receiver);
		if(player != null){
			print.chat(player, "You got a new message in your inbox! (/ms)");
		}
	}
	
}