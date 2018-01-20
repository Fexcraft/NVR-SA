package net.fexcraft.mod.nvr.server;

import java.io.File;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.perms.PermissionNode;
import net.fexcraft.mod.lib.perms.player.PlayerPerms;
import net.fexcraft.mod.lib.util.common.Log;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.lang.ArrayList;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.nvr.common.enums.DistrictType;
import net.fexcraft.mod.nvr.common.enums.MunicipalityType;
import net.fexcraft.mod.nvr.common.enums.NationType;
import net.fexcraft.mod.nvr.server.cmds.ClaimCmd;
import net.fexcraft.mod.nvr.server.cmds.DistrictCmd;
import net.fexcraft.mod.nvr.server.cmds.InfoCmd;
import net.fexcraft.mod.nvr.server.cmds.MessageCmd;
import net.fexcraft.mod.nvr.server.cmds.MunicipalityCmd;
import net.fexcraft.mod.nvr.server.data.Chunk;
import net.fexcraft.mod.nvr.server.data.District;
import net.fexcraft.mod.nvr.server.data.DoubleKey;
import net.fexcraft.mod.nvr.server.data.Message;
import net.fexcraft.mod.nvr.server.data.Municipality;
import net.fexcraft.mod.nvr.server.data.Nation;
import net.fexcraft.mod.nvr.server.data.Player;
import net.fexcraft.mod.nvr.server.data.Province;
import net.fexcraft.mod.nvr.server.events.ChatEvents;
import net.fexcraft.mod.nvr.server.events.ChunkEvents;
import net.fexcraft.mod.nvr.server.events.PlayerEvents;
import net.fexcraft.mod.nvr.server.util.Permissions;
import net.fexcraft.mod.nvr.server.util.Sender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = NVR.MODID, name = "NVR Standalone", version="xxx.xxx", acceptableRemoteVersions = "*", acceptedMinecraftVersions = "*", serverSideOnly = true, dependencies = "required-after:fcl")
public class NVR {
	
	@Mod.Instance(NVR.MODID)
	public static NVR INSTANCE;
	public static final String MODID = "nvr";
	public static final String DATASTR = "nvr-sa";
	public static final String DEF_UUID = "66e70cb7-1d96-487c-8255-5c2d7a2b6a0e";
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	//public static Sql SQL;
	public static File PATH, CHUNK_DIR, DISTRICT_DIR, MUNICIPALITY_DIR, PROVINCE_DIR, NATION_DIR, IMAGE_DIR, MESSAGE_DIR;
	public static final Log LOGGER = new Log("NVR", "&0[&4NVR&0]&7 ");
	//public static Server webserver;
	//private static Pregen pregen = new Pregen();

	public static final TreeMap<DoubleKey, Chunk> CHUNKS = new TreeMap<DoubleKey, Chunk>();
	public static final TreeMap<Integer, District> DISTRICTS = new TreeMap<Integer, District>();
	public static final TreeMap<Integer, Municipality> MUNICIPALITIES = new TreeMap<Integer, Municipality>();
	public static final TreeMap<Integer, Province> PROVINCES = new TreeMap<Integer, Province>();
	public static final TreeMap<Integer, Nation> NATIONS = new TreeMap<Integer, Nation>();
	public static final ArrayList<Message> MESSAGES = new ArrayList<Message>();
	
	@Mod.EventHandler
	public static void preInit(FMLPreInitializationEvent event){
		PATH = new File(event.getModConfigurationDirectory().getParentFile(), "/nvr/");
		LOGGER.debug(PATH, event.getModConfigurationDirectory());
		if(!PATH.exists()){
			PATH.mkdirs();
		}
		CHUNK_DIR = cine(new File(PATH, "chunks/"));
		DISTRICT_DIR = cine(new File(PATH, "districts/"));
		MUNICIPALITY_DIR = cine(new File(PATH, "municipalities/"));
		PROVINCE_DIR = cine(new File(PATH, "provinces/"));
		NATION_DIR = cine(new File(PATH, "nations/"));
		NATION_DIR = cine(new File(PATH, "nations/"));
		IMAGE_DIR = cine(new File(PATH, "image-cache/"));
		MESSAGE_DIR = cine(new File(PATH, "messages/"));
		//
		PermManager.setEnabled(MODID);
		PermManager.add("nvr.admin", PermissionNode.Type.BOOLEAN, false, false);
	}
	
	private static final File cine(File file){
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}

	@Mod.EventHandler
	public static void init(FMLInitializationEvent event){
		MinecraftForge.EVENT_BUS.register(new ChatEvents());
		MinecraftForge.EVENT_BUS.register(new ChunkEvents());
		MinecraftForge.EVENT_BUS.register(new PlayerEvents());
		//
		//MinecraftForge.EVENT_BUS.register(pregen);
		//ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, pregen);
		//
		Permissions.register();
		PlayerPerms.addAdditionalData(Player.class);
		//
		//NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
	}
	
	@Mod.EventHandler
	public static void serverLoad(FMLServerStartingEvent event) throws Exception {
		event.registerServerCommand(new InfoCmd());
		event.registerServerCommand(new ClaimCmd());
		event.registerServerCommand(new DistrictCmd());
		event.registerServerCommand(new MunicipalityCmd());
		event.registerServerCommand(new MessageCmd());
		//
		/*webserver = new Server();
		ServerConnector http = new ServerConnector(webserver);
		http.setHost("0.0.0.0");
		http.setPort(8912);
		http.setIdleTimeout(30000);
		webserver.addConnector(http);
		//
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase(System.getProperty("java.io.tmpdir"));
		webserver.setHandler(context);
		//context.addServlet(Main.class, "/webgames/mbeh");
		//context.addServlet(MBEHSocketServlet.class, "/webgames/mbeh/socket");
		//
		context.getSessionHandler().addEventListener(new SessionListener());
		webserver.start();
		webserver.join();*/
	}
	
	@Mod.EventHandler
	public static void postInit(FMLPostInitializationEvent event){
		Arrays.asList(NATION_DIR.listFiles()).forEach((nationfile) -> {
			JsonElement obj = JsonUtil.read((File)nationfile, false);
			if(obj != null){
				Nation nation = Nation.load(obj.getAsJsonObject());
				if(nation != null){
					NATIONS.put(nation.id, nation);
				}
			}
		});
		Arrays.asList(PROVINCE_DIR.listFiles()).forEach((provincefile) -> {
			JsonElement obj = JsonUtil.read((File)provincefile, false);
			if(obj != null){
				Province prov = Province.load(obj.getAsJsonObject());
				if(prov != null){
					PROVINCES.put(prov.id, prov);
				}
			}
		});
		Arrays.asList(MUNICIPALITY_DIR.listFiles()).forEach((municipalitiesfile) -> {
			JsonElement obj = JsonUtil.read((File)municipalitiesfile, false);
			if(obj != null){
				Municipality mun = Municipality.load(obj.getAsJsonObject());
				if(mun != null){
					MUNICIPALITIES.put(mun.id, mun);
				}
			}
		});
		Arrays.asList(DISTRICT_DIR.listFiles()).forEach((districtsfile) -> {
			JsonElement obj = JsonUtil.read((File)districtsfile, false);
			if(obj != null){
				District dis = District.load(obj.getAsJsonObject());
				if(dis != null ){
					DISTRICTS.put(dis.id, dis);
				}
			}
		});
		//Check for existence of default Stuff.
		if(!NATIONS.containsKey(-1)){
			Nation nat = new Nation();
			nat.id = -1;
			nat.account = AccountManager.INSTANCE.getAccount("nation", "-1", true);
			nat.name = "No Nation";
			nat.icon = "https://i.imgur.com/8z76Cbr.png";
			nat.type = NationType.ANARCHY;
			nat.gov_title = "Finest Anarchy";
			nat.gov_name = "Anarchist";
			nat.incharge = null;
			nat.incharge_title = "Leader";
			nat.creator = UUID.fromString(DEF_UUID);
			nat.created = Time.getDate();
			nat.changed = Time.getDate();
			nat.prev_income = 0;
			nat.parent = null;
			nat.save();
			NATIONS.put(-1, nat);
		}
		if(!NATIONS.containsKey(0)){
			Nation nat = new Nation();
			nat.id = 0;
			nat.account = AccountManager.INSTANCE.getAccount("nation", "0", true);
			nat.name = "Testarian Union";
			nat.icon = "";
			nat.type = NationType.MONARCHY;
			nat.gov_title = "Union";
			nat.gov_name = "Unionist";
			nat.incharge = UUID.fromString(DEF_UUID);
			nat.incharge_title = "Selected One";
			nat.creator = UUID.fromString(DEF_UUID);
			nat.created = Time.getDate();
			nat.changed = Time.getDate();
			nat.prev_income = 0;
			nat.parent = null;
			nat.save();
			NATIONS.put(0, nat);
		}
		if(!PROVINCES.containsKey(-1)){
			Province prov = new Province();
			prov.id = -1;
			prov.name = "Neutral Territory";
			prov.icon = "https://i.imgur.com/oxJw52L.png";
			prov.nation = NATIONS.get(-1);
			prov.ruler = null;
			prov.ruler_title = "Landlord";
			prov.creator = UUID.fromString(DEF_UUID);
			prov.created = Time.getDate();
			prov.changed = Time.getDate();
			prov.previncome = 0;
			prov.save();
			PROVINCES.put(-1, prov);
		}
		if(!PROVINCES.containsKey(0)){
			Province prov = new Province();
			prov.id = 0;
			prov.name = "Spawn";
			prov.icon = "";
			prov.nation = NATIONS.get(0);
			prov.ruler = UUID.fromString(DEF_UUID);
			prov.ruler_title = "Area Director";
			prov.creator = UUID.fromString(DEF_UUID);
			prov.created = Time.getDate();
			prov.changed = Time.getDate();
			prov.previncome = 0;
			prov.save();
			PROVINCES.put(0, prov);
		}
		if(!MUNICIPALITIES.containsKey(-1)){
			Municipality mun = new Municipality();
			mun.id = -1;
			mun.name = "Unnamed Place";
			mun.account = AccountManager.INSTANCE.getAccount("municipality", "-1", true);
			mun.icon = "https://i.imgur.com/RFGyyOD.png";
			mun.type = MunicipalityType.ABANDONED;
			mun.province = PROVINCES.get(-1);
			mun.creator = UUID.fromString(DEF_UUID);
			mun.created = Time.getDate();
			mun.changed = Time.getDate();
			mun.previncome = 0;
			mun.citizentax = 0;
			mun.save();
			MUNICIPALITIES.put(-1, mun);
		}
		if(!MUNICIPALITIES.containsKey(0)){
			Municipality mun = new Municipality();
			mun.id = 0;
			mun.name = "Spawn";
			mun.account = AccountManager.INSTANCE.getAccount("municipality", "0", true);
			mun.icon = "";//
			mun.type = MunicipalityType.TOO_LARGE;
			mun.province = PROVINCES.get(0);
			mun.creator = UUID.fromString(DEF_UUID);
			mun.created = Time.getDate();
			mun.changed = Time.getDate();
			mun.previncome = 0;
			mun.citizentax = 0;
			mun.save();
			MUNICIPALITIES.put(0, mun);
		}
		if(!DISTRICTS.containsKey(-1)){
			District dis = new District();
			dis.id = -1;
			dis.type = DistrictType.UNSPECIFIED;
			dis.name = "Unclaimed Area";
			dis.municipality = MUNICIPALITIES.get(-1);
			dis.manager = null;
			dis.creator = UUID.fromString(DEF_UUID);
			dis.created = Time.getDate();
			dis.changed = Time.getDate();
			dis.previncome = 0;
			dis.tax = -10;
			dis.save();
			DISTRICTS.put(-1, dis);
		}
		if(!DISTRICTS.containsKey(0)){
			District dis = new District();
			dis.id = 0;
			dis.type = DistrictType.CENTER;
			dis.name = "TPP";
			dis.municipality = MUNICIPALITIES.get(0);
			dis.manager = UUID.fromString(DEF_UUID);
			dis.creator = UUID.fromString(DEF_UUID);
			dis.created = Time.getDate();
			dis.changed = Time.getDate();
			dis.previncome = 0;
			dis.tax = 0;
			dis.save();
			DISTRICTS.put(0, dis);
		}
		//
		//ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, pregen);
		//pregen.load();
		//
		Arrays.asList(MESSAGE_DIR.listFiles()).forEach((messagefile) -> {
			JsonElement obj = JsonUtil.read((File)messagefile, false);
			if(obj != null){
				Message msg = new Message(obj.getAsJsonObject());
				if(!msg.read){
					MESSAGES.add(msg);
				}
			}
		});
		//
	}
	
	@Mod.EventHandler
	public static void serverStop(FMLServerStoppingEvent event) throws Exception {
		NATIONS.values().forEach((nat) -> {
			nat.save();
		});
		PROVINCES.values().forEach((pro) -> {
			pro.save();
		});
		MUNICIPALITIES.values().forEach((mun) -> {
			mun.save();
		});
		DISTRICTS.values().forEach((dis) -> {
			dis.save();
		});
		MESSAGES.forEach(msg -> msg.save());
		/*CHUNKS.values().forEach((chunk) -> {
			chunk.save();
		});*/ //Actually they should be unloaded on server stop, so another event handles this.
		//webserver.stop();
		//pregen.save();
	}
	
	public static final Player getPlayerData(EntityPlayer player){
		return PermManager.getPlayerPerms(player).getAdditionalData(Player.class);
	}

	public static final Player getPlayerData(String string){
		return getPlayerData(string, false);
		//EntityPlayerMP player = Static.getServer().getPlayerList().getPlayerByUsername(string);
		//return player == null ? null : getPlayerData(player);
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
	
	public static final void save(){
		Sender.serverMessage("&5Saving data. Expect a short lag.");
		new Runnable(){ @Override public void run(){ NATIONS.forEach((key, pro) -> { pro.save(); Sender.serverMessage("&3Done saving nation data.");});}};
		new Runnable(){ @Override public void run(){ PROVINCES.forEach((key, pro) -> { pro.save(); Sender.serverMessage("&3Done saving province data.");});}};
		new Runnable(){ @Override public void run(){ MUNICIPALITIES.forEach((key, dis) -> { dis.save(); Sender.serverMessage("&3Done saving municipality data.");});}};
		new Runnable(){ @Override public void run(){ DISTRICTS.forEach((key, dis) -> { dis.save(); Sender.serverMessage("&3Done saving district data.");});}};
		//new Runnable(){ @Override public void run(){ CHUNKS.forEach((key, chunk) -> { chunk.save(); Sender.serverMessage("&3Done saving chunk data.");});}}; //Should be handled on chunk unload
		Sender.serverMessage("&5Done saving all data.");
	}

	public static Nation getNation(int i){
		Nation nat = NATIONS.get(i);
		return nat == null ? NATIONS.get(-1) : nat;
	}

	public static Province getProvince(int i){
		Province pro = PROVINCES.get(i);
		return pro == null ? PROVINCES.get(-1) : pro;
	}
	
	public static Municipality getMunicipality(int i){
		Municipality mun = MUNICIPALITIES.get(i);
		return mun == null ? MUNICIPALITIES.get(-1) : mun;
	}

	public static District getDistrict(int i){
		District dis = DISTRICTS.get(i);
		return dis == null ? DISTRICTS.get(-1) : dis;
	}
	
	public static Chunk getChunk(int x, int z){
		return CHUNKS.get(new DoubleKey(x, z));
	}

	public static Chunk getChunk(World world, BlockPos pos){
		int x = world.getChunkFromBlockCoords(pos).x;
		int z = world.getChunkFromBlockCoords(pos).z;
		return getChunk(x, z);
	}

	public static Chunk getChunk(EntityPlayer player){
		return getChunk(player.world, player.getPosition());
	}

	public static Nation getNation(String[] args, int off){
		if(NumberUtils.isCreatable(args[off])){
			return getNation(Integer.parseInt(args[off]));
		}
		String str = args[off];
		if(args.length > off + 1){
			for(int i = 2; i < args.length; i++){
				str += " " + args[i];
			}
		}
		Nation nat = null;
		for(Nation n : NATIONS.values()){
			if(n.name.equals(str)){
				nat = n;
			}
		}
		return nat;
	}

	public static Province getProvince(String[] args, int off){
		if(NumberUtils.isCreatable(args[off])){
			return getProvince(Integer.parseInt(args[off]));
		}
		String str = args[off];
		if(args.length > off + 1){
			for(int i = 2; i < args.length; i++){
				str += " " + args[i];
			}
		}
		Province prov = null;
		for(Province p : PROVINCES.values()){
			if(p.name.equals(str)){
				prov = p;
			}
		}
		return prov;
	}

	public static Municipality getMunicipality(String[] args, int off){
		if(NumberUtils.isCreatable(args[off])){
			return getMunicipality(Integer.parseInt(args[off]));
		}
		String str = args[off];
		if(args.length > off + 1){
			for(int i = 2; i < args.length; i++){
				str += " " + args[i];
			}
		}
		Municipality mun = null;
		for(Municipality m : MUNICIPALITIES.values()){
			if(m.name.equals(str)){
				mun = m;
			}
		}
		return mun;
	}

	public static District getDistrict(String[] args, int off){
		if(NumberUtils.isCreatable(args[off])){
			return getDistrict(Integer.parseInt(args[off]));
		}
		String str = args[off];
		if(args.length > off + 1){
			for(int i = 2; i < args.length; i++){
				str += " " + args[i];
			}
		}
		District dis = null;
		for(District d : DISTRICTS.values()){
			if(d.name.equals(str)){
				dis = d;
			}
		}
		return dis;
	}
	
	public static NVR getInstance(){
		return INSTANCE;
	}

	public static UUID getConsoleUUID(){
		return UUID.fromString(CONSOLE_UUID);
	}
	
}