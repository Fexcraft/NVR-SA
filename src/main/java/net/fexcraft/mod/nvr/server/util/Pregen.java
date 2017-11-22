package net.fexcraft.mod.nvr.server.util;

import java.io.File;
import java.util.List;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.nvr.server.NVR;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class Pregen implements LoadingCallback {
	
	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event){
		if(event.phase == Phase.START){
			Static.getServer().addScheduledTask(new Runnable(){
				@Override
				public void run(){
					continueGen();
				}
			});
		}
	}
	
	public static Ticket ticket;
	private static int x, z, l = 8;
	private static ChunkPos[] cpos = new ChunkPos[l], opos = new ChunkPos[l];
	private static boolean done = false;
	
	public void continueGen(){
		if(done){
			return;
		}
		if(ticket == null){
			ticket = ForgeChunkManager.requestTicket(NVR.INSTANCE, Static.getServer().worlds[0], ForgeChunkManager.Type.NORMAL);
		}
		if(cpos[0] == null){
			for(int i = 0; i < l; i++){
				cpos[i] = new ChunkPos(x + i, z);
			}
		}
		else{
			opos = cpos;
			for(ChunkPos pos : opos){
				ForgeChunkManager.unforceChunk(ticket, pos);
			}
			for(int i = 0; i < l; i++){
				cpos[i] = new ChunkPos(x + i, z);
			}
		}
		for(ChunkPos pos : cpos){
			ForgeChunkManager.forceChunk(ticket, pos);
		}
		if(x <= 640){
			x += l;
		}
		if(x > 640){
			x = -640;
			z++;
			save();
		}
		if(z > 640){
			done = true;
		}
		NVR.LOGGER.log("Generating chunks " + x + " - " + (x + l) + "x | " + z + "z;");
	}
	
	public void load(){
		File file = new File(NVR.PATH, "/pregen.data");
		if(!file.exists()){
			x = z = -640;
		}
		else{
			JsonObject obj = JsonUtil.get(file);
			x = obj.get("x").getAsInt();
			z = obj.get("z").getAsInt();
			done = JsonUtil.getIfExists(obj, "done", true);
			return;
		}
	}
	
	public void save(){
		File file = new File(NVR.PATH, "/pregen.data");
		JsonObject obj = new JsonObject();
		obj.addProperty("x", x);
		obj.addProperty("z", z);
		obj.addProperty("done", done);
		JsonUtil.write(file, obj);
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world){
		//
	}
	
}