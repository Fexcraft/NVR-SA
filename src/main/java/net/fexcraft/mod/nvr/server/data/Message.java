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
	public UUID receiver;
	public long created;
	
	public Message(){}
	
	public Message(JsonObject obj){
		//TODO
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
			JsonUtil.write(file, obj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void setRead(){
		//TODO
	}
	
}