package skinsrestorer;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import net.minecraft.util.com.mojang.util.UUIDTypeAdapter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.api.profiles.Profile;

public class HeadGiveCommand implements CommandExecutor {

	private SkinsRestorer plugin;
	public HeadGiveCommand(SkinsRestorer plugin) {
		this.plugin = plugin;
	}

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, final String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Can be used only from console");
			return true;
		}
		if (!sender.hasPermission("skinsrestorer.head")) {
			sender.sendMessage("You don't have permission to do this");
			return true;
		}
		final Player player = (Player) sender;
		if (args.length == 2 && args[0].equalsIgnoreCase("head")) {
			player.sendMessage(ChatColor.BLUE + "Preparing head itemstack. Please wait.");
			executor.execute(
				new Runnable() {
					@Override
					public void run() {
						final ItemStack playerhead = new ItemStack(Material.SKULL_ITEM);
						playerhead.setDurability((short) 3);
						String name = args[1];
						Profile prof = DataUtils.getProfile(name);
						if (prof == null) {
							return;
						}
						Property prop = DataUtils.getProp(prof.getId());
						if (prop == null) {
							return;
						}
						try {
							SkullMeta meta = (SkullMeta) playerhead.getItemMeta();
							if (meta == null) {
								meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
							}
							SkinProfile skinprofile = new SkinProfile(UUIDTypeAdapter.fromString(prof.getId()), prop);
							GameProfile newprofile = new GameProfile(skinprofile.getUUID(), name);
							newprofile.getProperties().clear();
							newprofile.getProperties().put(skinprofile.getHeadSkinData().getName(), skinprofile.getHeadSkinData());
							Field profileField = meta.getClass().getDeclaredField("profile");
							profileField.setAccessible(true);
							profileField.set(meta, newprofile);
							playerhead.setItemMeta(meta);
						} catch (Exception e) {
							player.sendMessage(ChatColor.RED + "Skin wasn't applied to head because of the error: "+e.getMessage());
						}
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, 
							new Runnable() {
								@Override
								public void run() {
									player.getInventory().addItem(playerhead);
									player.sendMessage(ChatColor.BLUE + "Head given.");
								}
							}
						);
					}
				}
			);
			return true;
		}
		return false;
	}

}
