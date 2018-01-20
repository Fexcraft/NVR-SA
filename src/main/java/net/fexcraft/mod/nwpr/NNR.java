package net.fexcraft.mod.nwpr;

import java.io.File;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.common.Log;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.nvr.server.data.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

//@Mod(modid = NNR.MODID, name = "FN-NW", version="xxx.xxx", acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*", dependencies = "required-after:fcl")
public class NNR {
	
	@Mod.Instance(NNR.MODID)
	public static NNR INSTANCE;
	public static final String MODID = "nwpr";
	public static final String DATASTR = "fn-nw";
	public static final String DEF_UUID = "66e70cb7-1d96-487c-8255-5c2d7a2b6a0e";
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	
	public static final Log LOGGER = new Log("FN-NW", "&8[&2FN&0-&9NW&8]&7 ");
	
	@Mod.EventHandler
	public static void preInit(FMLPreInitializationEvent event){
		//
	}
	
	@Mod.EventHandler
	public static void init(FMLInitializationEvent event){
		//
	}
	
	@Mod.EventHandler
	public static void serverLoad(FMLServerStartingEvent event) throws Exception {
		//
	}
	
	@Mod.EventHandler
	public static void postInit(FMLPostInitializationEvent event){
		//
	}
	
	@Mod.EventHandler
	public static void serverStop(FMLServerStoppingEvent event) throws Exception {
		//
	}
	
	public static final Player getPlayerData(EntityPlayer player){
		return PermManager.getPlayerPerms(player).getAdditionalData(Player.class);
	}

	public static final Player getPlayerData(String string){
		return getPlayerData(string, false);
	}
	
	/** @param bool load if offline */
	public static Player getPlayerData(String string, boolean bool){
		EntityPlayerMP player = Static.getServer().getPlayerList().getPlayerByUsername(string);
		if(player == null && bool){
			JsonObject obj = JsonUtil.read(new File(PermManager.userDir, string + ".perm"), false).getAsJsonObject();
			UUID uuid = null;
			try{
				uuid = UUID.fromString(string);
			}
			catch(Exception e){
				uuid = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(string).getId();
			}
			return obj == null ? null : Player.loadOffline(uuid, obj);
		}
		return player == null ? null : getPlayerData(player);
	}
	
}