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
	
}