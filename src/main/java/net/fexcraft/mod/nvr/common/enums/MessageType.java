package net.fexcraft.mod.nvr.common.enums;

public enum MessageType {
	
	SYSTEM,
	INVITE,
	PRIVATE;
	
	public static MessageType fromString(String str){
		for(MessageType type : values()){
			if(str.equals(type.name())){
				return type;
			}
		}
		return SYSTEM;
	}

	public static MessageType fromUnknown(String string){
		switch(string){
			case "s": case "sys": case "system":{
				return SYSTEM;
			}
			case "i": case "inv": case "invite":{
				return INVITE;
			}
			case "m": case "msg": case "messages":{
				return PRIVATE;
			}
			case "*": case "a": case "all":
			default: return null;
		}
	}
	
}