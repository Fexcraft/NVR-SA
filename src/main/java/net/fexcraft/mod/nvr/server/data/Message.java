package net.fexcraft.mod.nvr.server.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.nvr.common.enums.MessageType;
import net.fexcraft.mod.nvr.server.NVR;

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
			File file = new File(NVR.DISTRICT_DIR, receiver.toString() + "_" + created + ".json");
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
	
	public void processFunction(String str){
		switch(type){
			case INVITE:{
				if(str == null){
					//
				}
				else switch(str){
					//
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
	
	public static ArrayList<Message> getAsList(UUID uuid, MessageType type){
		return (ArrayList<Message>)Arrays.asList((Message[])NVR.MESSAGES.stream().filter(p -> p.receiver.equals(uuid) && (type == null ? true : p.type == type)).toArray());
	}
	
	public static long count(ArrayList<Message> list, MessageType type){
		return list.stream().filter(pre -> pre.type == type).count();
	}
	
}