package net.fexcraft.mod.nvr.server.data;

import java.io.File;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.nvr.common.enums.MessageType;
import net.fexcraft.mod.nvr.server.NVR;

public class Message {
	
	public boolean read;
	public JsonObject function;
	public MessageType type;
	public String content;
	public UUID receiver;
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
		this.receiver = UUID.fromString(JsonUtil.getIfExists(obj, "receiver", NVR.CONSOLE_UUID));
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
			obj.addProperty("created", created);
			obj.addProperty("content", content);
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
	
	public void processFunction(String str){
		if(str == null){
			
		}
		else switch(str){
			case "":{
				
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
	
}