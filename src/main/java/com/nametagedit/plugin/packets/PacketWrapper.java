package com.nametagedit.plugin.packets;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.utils.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PacketWrapper {

	public String error;
	private Object packet = PacketAccessor.createPacket();

	public PacketWrapper(String name, int param, List<String> members, boolean visible) {
		if (param != 3 && param != 4) {
			throw new IllegalArgumentException("Method must be join or leave for player constructor");
		}
		setupDefaults(name, param, visible);
		setupMembers(members);
	}

	@SuppressWarnings("unchecked")
	public PacketWrapper(String name, String prefix, String suffix, int param, Collection<?> players, boolean visible) {
		setupDefaults(name, param, visible);
		if (param == 0 || param == 2) {
			try {
				if (param == 0) {
					((Collection) PacketAccessor.MEMBERS.get(packet)).addAll(players);
				}
			} catch (Exception e) {
				error = e.getMessage();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setupMembers(Collection<?> players) {
		try {
			players = players == null || players.isEmpty() ? new ArrayList<>() : players;
			((Collection) PacketAccessor.MEMBERS.get(packet)).addAll(players);
		} catch (Exception e) {
			error = e.getMessage();
		}
	}

	private void setupDefaults(String name, int param, boolean visible) {
		try {
			PacketAccessor.TEAM_NAME.set(packet, name);
			PacketAccessor.PARAM_INT.set(packet, param);
			
			if (PacketAccessor.VISIBILITY != null) {
				if (visible) {
					PacketAccessor.VISIBILITY.set(packet, "always");
				}else {
					PacketAccessor.VISIBILITY.set(packet, "never");	
				}
            }
			
			if (NametagHandler.DISABLE_PUSH_ALL_TAGS && PacketAccessor.PUSH != null) {
				PacketAccessor.PUSH.set(packet, "never");
			}
		} catch (Exception e) {
			error = e.getMessage();
		}
	}

	public void send() {
		PacketAccessor.sendPacket(Utils.getOnline(), packet);
	}

	public void send(Player player) {
		PacketAccessor.sendPacket(player, packet);
	}

}
