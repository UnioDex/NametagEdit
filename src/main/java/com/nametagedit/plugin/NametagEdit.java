package com.nametagedit.plugin;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nametagedit.plugin.api.INametagApi;
import com.nametagedit.plugin.api.NametagAPI;
import com.nametagedit.plugin.hooks.HookGroupManager;
import com.nametagedit.plugin.hooks.HookLibsDisguise;
import com.nametagedit.plugin.hooks.HookLuckPerms;
import com.nametagedit.plugin.hooks.HookPermissionsEX;
import com.nametagedit.plugin.hooks.HookZPermissions;
import com.nametagedit.plugin.packets.PacketWrapper;

import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

/**
 * TODO:
 * - Better uniform message format + more messages
 * - Code cleanup
 * - Add language support
 */
@Getter
public class NametagEdit extends JavaPlugin {

	private static INametagApi api;

	private NametagHandler handler;
	private NametagManager manager;
	public boolean citizens = false;

	public static INametagApi getApi() {
		return api;
	}

	@Override
	public void onEnable() {
		testCompat();
		if (!isEnabled()) return;

		manager = new NametagManager(this);
		handler = new NametagHandler(this, manager);

		PluginManager pluginManager = Bukkit.getPluginManager();
		if (checkShouldRegister("zPermissions")) {
			pluginManager.registerEvents(new HookZPermissions(handler), this);
		} else if (checkShouldRegister("PermissionsEx")) {
			pluginManager.registerEvents(new HookPermissionsEX(handler), this);
		} else if (checkShouldRegister("GroupManager")) {
			pluginManager.registerEvents(new HookGroupManager(handler), this);
		} else if (checkShouldRegister("LuckPerms")) {
			pluginManager.registerEvents(new HookLuckPerms(handler), this);
		}

		if (pluginManager.getPlugin("LibsDisguises") != null) {
			pluginManager.registerEvents(new HookLibsDisguise(this), this);
		}

		getCommand("ne").setExecutor(new NametagCommand(handler));

		if (api == null) {
			api = new NametagAPI(handler, manager);
		}

		Plugin citizensPlugin = pluginManager.getPlugin("Citizens");
		if (citizensPlugin != null) {
			citizens = true;
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().startsWith("NPC-")) {
				handler.getIsTagVisible().put(p, false);
				handler.applyTagToPlayer(p, false, false);
			}else {
				handler.getIsTagVisible().put(p, true);
			}
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				if (citizens) {
					NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
					if (npcRegistry == null) return;
					for (NPC npc : npcRegistry) {
						Entity npcEntity = npc.getEntity();
						if (npcEntity == null) continue;
						if (npcEntity.getType().equals(EntityType.PLAYER)) {
							Player p = (Player) npcEntity;
							if (p != null) { 
								if (p.getName().startsWith("NPC-")) {
									handler.getIsTagVisible().put(p, false);
									handler.applyTagToPlayer(p, false, false);
								}
							}
						}
					}
				}
			}

		}, 10L);
	}

	@Override
	public void onDisable() {
		manager.reset();
		handler.getAbstractConfig().shutdown();
	}

	void debug(String message) {
		if (handler != null && handler.debug()) {
			getLogger().info("[DEBUG] " + message);
		}
	}

	private boolean checkShouldRegister(String plugin) {
		if (Bukkit.getPluginManager().getPlugin(plugin) == null) return false;
		getLogger().info("Found " + plugin + "! Hooking in.");
		return true;
	}

	private void testCompat() {
		PacketWrapper wrapper = new PacketWrapper("TEST", "&f", "", 0, new ArrayList<>(), true);
		wrapper.send();
		if (wrapper.error == null) return;
		Bukkit.getPluginManager().disablePlugin(this);
		getLogger().severe(new StringBuilder()
				.append("\n------------------------------------------------------\n")
				.append("[WARNING] NametagEdit v").append(getDescription().getVersion()).append(" Failed to load! [WARNING]")
				.append("\n------------------------------------------------------")
				.append("\nThis might be an issue with reflection. REPORT this:\n> ")
				.append(wrapper.error)
				.append("\nThe plugin will now self destruct.\n------------------------------------------------------")
				.toString());
	}

}