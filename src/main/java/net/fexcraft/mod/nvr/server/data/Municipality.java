package net.fexcraft.mod.nvr.server.data;

import java.io.File;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.account.AccountManager.Account;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.lang.ArrayList;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.nvr.common.enums.MunicipalityType;
import net.fexcraft.mod.nvr.server.NVR;
import net.minecraft.entity.player.EntityPlayer;

public class Municipality {
	
	public int id;
	public String name, icon, colour;
	public MunicipalityType type;
	public Province province;
	public ArrayList<UUID> management = new ArrayList<UUID>();
	public ArrayList<Integer> neighbors = new ArrayList<Integer>();
	public UUID creator;
	public long created;
	public long changed;
	public double previncome;
	public ArrayList<UUID> citizens = new ArrayList<UUID>();
	public double citizentax;
	public Account account;
	public boolean open;
	
	public Municipality(){}
	
	public static final Municipality load(JsonObject obj){
		if(obj == null || !obj.has("id")){
			return null;
		}
		Municipality mun = new Municipality();
		mun.id = obj.get("id").getAsInt();
		mun.name = JsonUtil.getIfExists(obj, "name", "Unnamed Place");
		mun.type = MunicipalityType.fromString(JsonUtil.getIfExists(obj, "type", MunicipalityType.ABANDONED.name()));
		mun.province = NVR.getProvince(JsonUtil.getIfExists(obj, "province", -1).intValue());
		mun.management = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "management", new JsonArray()).getAsJsonArray());
		mun.neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		mun.creator = UUID.fromString(JsonUtil.getIfExists(obj, "creator", NVR.DEF_UUID));
		mun.created = JsonUtil.getIfExists(obj, "created", 0).longValue();
		mun.changed = JsonUtil.getIfExists(obj, "changed", 0).longValue();
		mun.previncome = JsonUtil.getIfExists(obj, "previncome", 0).doubleValue();
		mun.citizens = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "citizens", new JsonArray()).getAsJsonArray());
		mun.citizentax = JsonUtil.getIfExists(obj, "citizentax", 0).doubleValue();
		mun.icon = JsonUtil.getIfExists(obj, "icon", "");
		mun.colour = JsonUtil.getIfExists(obj, "color", "#f0f0f0");
		mun.open = JsonUtil.getIfExists(obj, "open", false);
		//
		mun.account = Account.getAccountManager().getAccountOf("municipality", "municipality:" + mun.id);
		//
		return mun;
	}
	
	public static final File getFile(int id){
		return new File(NVR.MUNICIPALITY_DIR, id + ".json");
	}
	
	public final void save(){
		Account.getAccountManager().saveAccount(account);
		try{
			File file = getFile(id);
			JsonObject obj = JsonUtil.get(file);
			obj.addProperty("id", id);
			obj.addProperty("name", name);
			obj.addProperty("type", type.name());
			obj.addProperty("province", province.id);
			obj.add("management", JsonUtil.getArrayFromUUIDList(management));
			obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
			obj.addProperty("created", created);
			obj.addProperty("changed", changed);
			obj.addProperty("previncome", previncome);
			obj.add("citizens", JsonUtil.getArrayFromUUIDList(citizens));
			obj.addProperty("citizentax", citizentax);
			obj.addProperty("icon", icon == null ? "" : icon);
			obj.addProperty("color", colour);
			obj.addProperty("open", open);
			//
			obj.addProperty("last_save", Time.getDate());
			JsonUtil.write(file, obj);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public long districts(){
		return NVR.DISTRICTS.values().stream().filter(dis -> dis.municipality.id == this.id).count();
	}

	public boolean canEdit(EntityPlayer entityplayer){
		Player player = NVR.getPlayerData(entityplayer);
		return player == null ? false : (management.contains(player.uuid) || province.ruler.equals(player.uuid) || province.nation.canEditMunicipality(player.uuid));
	}
	
}