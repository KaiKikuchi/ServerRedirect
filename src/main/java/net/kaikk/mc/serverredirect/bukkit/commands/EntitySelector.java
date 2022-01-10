package net.kaikk.mc.serverredirect.bukkit.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import net.kaikk.mc.serverredirect.bukkit.ServerRedirect;

/**
 * A cross-version implementation of Minecraft target selectors.
 * 
 * @author KaiNoMood
 *
 */
public class EntitySelector {
    /**
     * Selects entities using the given Vanilla selector.
     * <br>
     * No guarantees are made about the selector format, other than they match
     * the Vanilla format for the active Minecraft version.
     * <br>
     * Usually a selector will start with '@', unless selecting a Player in
     * which case it may simply be the Player's name or UUID.
     * <br>
     * Note that in Vanilla, elevated permissions are usually required to use
     * '@' selectors, but this method will not check such permissions from the
     * sender.
     *
     * @param sender the sender to execute as, must be provided
     * @param selector the selection string
     * @return a list of the selected entities. The list will not be null, but
     * no further guarantees are made.
     * @throws IllegalArgumentException if the selector is malformed in any way
     * or a parameter is null
     */
	public static List<Entity> selectEntities(CommandSender sender, String selector) {
		return Internal.entitySelectorWrapper.selectEntities(sender, selector);
	}
	
	private interface EntitySelectorWrapper {
		public List<Entity> selectEntities(CommandSender sender, String selector);
	}
	
	private static class Internal {
		private final static EntitySelectorWrapper entitySelectorWrapper;
		
		static {
			EntitySelectorWrapper w = null;
			try {
				Bukkit.class.getMethod("selectEntities", CommandSender.class, String.class);
				w = new EntitySelectorWrapper() {
					@Override
					public List<Entity> selectEntities(CommandSender sender, String selector) {
						return Bukkit.selectEntities(sender, selector);
					}
				};
				ServerRedirect.instance().getLogger().info("Using native target selector implementation");
			} catch (Exception e) {

			}
			
			if (w == null) {
				w = new EntitySelectorWrapper() {
					@Override
					public List<Entity> selectEntities(CommandSender sender, String selector) {
						return EntitySelectorImpl.selectEntities(sender, selector);
					}
				};
				ServerRedirect.instance().getLogger().info("Using own target selector implementation");
			}
			
			entitySelectorWrapper = w;
		}
	}

	/**
	 * Older versions of Bukkit are missing a public implementation of target selectors.
	 * <br>
	 * So here's mine. Not perfect, but it can do the job.
	 * 
	 * @author KaiNoMood
	 *
	 */
	private static class EntitySelectorImpl {
		private static final Pattern TOKEN_PATTERN = Pattern.compile("^@([pares])(?:\\[([^ ]*)\\])?$");
		private static final Pattern COMMA_PATTERN = Pattern.compile(",");
		private static final Map<String, EntitySelectorArgument> filterArguments = new LinkedHashMap<>();
		private static final String[] notFilteringArguments = {"c", "dx", "dy", "dz", "x", "y", "z"};
		private static final GameMode[] gameModes = {GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE, GameMode.SPECTATOR};
		
		static {
			filterArguments.put("r", (e, v, s, l) -> e.getLocation().distanceSquared(l) <= squared(Double.valueOf(v)));
			filterArguments.put("rm", (e, v, s, l) -> e.getLocation().distanceSquared(l) >= squared(Double.valueOf(v)));
			filterArguments.put("l", (e, v, s, l) -> e instanceof Player && ((Player) e).getLevel() <= Integer.valueOf(v));
			filterArguments.put("lm", (e, v, s, l) -> e instanceof Player && ((Player) e).getLevel() >= Integer.valueOf(v));
			filterArguments.put("rx", (e, v, s, l) -> e.getLocation().getYaw() <= Float.valueOf(v));
			filterArguments.put("rxm", (e, v, s, l) -> e.getLocation().getYaw() >= Float.valueOf(v));
			filterArguments.put("ry", (e, v, s, l) -> e.getLocation().getPitch() <= Float.valueOf(v));
			filterArguments.put("rym", (e, v, s, l) -> e.getLocation().getPitch() >= Float.valueOf(v));
			filterArguments.put("name", (e, v, s, l) -> e.getName().equals(v));
			filterArguments.put("type", (e, v, s, l) -> e.getType().name().equals(v.toUpperCase(Locale.ROOT)));
			filterArguments.put("m", (e, v, s, l) -> {
				if (e instanceof Player) {
					boolean not = v.charAt(0) == '!';
					if (not) {
						v = v.substring(1);
					}
					
					try {
						return (gameModes[Integer.valueOf(v)] == ((Player) e).getGameMode()) != not;
					} catch (Exception e1) {
						// not a number
					}
					
					try {
						return (GameMode.valueOf(v.toUpperCase(Locale.ROOT)) == ((Player) e).getGameMode()) != not;
					} catch (Exception e1) {
						throw new IllegalArgumentException(v + " is not a valid game mode");
					}
				}
				return false;
			});
			filterArguments.put("team", (e, v, s, l) -> {
				boolean not = v.charAt(0) == '!';
				if (not) {
					v = v.length() == 1 ? "" : v.substring(1);
				}
				
				String identifier = e instanceof Player ? e.getName() : e.getUniqueId().toString();
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(identifier);
				if (team == null) {
					return v.isEmpty() != not;
				}
				
				return team.getName().equals(v) != not;
			});
		}
	
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static List<Entity> selectEntities(CommandSender sender, String selector) {
			if (sender == null || selector == null) {
				throw new IllegalArgumentException("Arguments cannot be null");
			}
			
			List<Entity> list = new ArrayList<Entity>();
			Matcher matcher = TOKEN_PATTERN.matcher(selector);
			if (matcher.matches()) {
				Location loc = getLocationFor(sender);
				String sel = matcher.group(1);
	
				String argMatch = matcher.group(2);
				List<String> args = argMatch == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(COMMA_PATTERN.split(argMatch)));
				args.replaceAll(s -> s.trim());
				args.removeIf(s -> s.isEmpty());
				
				if (args.isEmpty()) { // skip arguments filtering
					switch(sel.charAt(0)) {
					case 'p':
						// nearest player
						list.add(closestTo(loc, loc.getWorld().getPlayers()));
						break;
					case 'a':
						// all players
						list.addAll(Bukkit.getOnlinePlayers());
						break;
					case 'r':
						// random player
						list.add(random(Bukkit.getOnlinePlayers()));
						break;
					case 'e':
						// all entities
						list.addAll(loc.getWorld().getEntities());
						break;
					case 's':
						// entity that ran this command
						if (sender instanceof Entity) {
							list.add((Entity) sender);
						}
						break;
					}
					
					return list;
				}
				
				// arguments will filter the entities
				// collect the entities
				Collection<Entity> entities;
				char selChar = sel.charAt(0);
				switch(selChar) {
				case 'p':
				case 'a':
				case 'r':
					// players only
					entities = (Collection) Bukkit.getOnlinePlayers();
					break;
				case 'e':
					// entities
					entities = loc.getWorld().getEntities();
					break;
				case 's':
					// entity that ran this command
					if (sender instanceof Entity) {
						entities = new ArrayList<Entity>();
						entities.add((Entity) sender);
					} else {
						return list;
					}
					break;
				default:
					return list;
				}
				
				Iterator<String> argIt = args.iterator();
				
				// coordinates argument handling
				while (argIt.hasNext()) {
					String arg = argIt.next();
					if (arg.length() >= 3) {
						if (arg.startsWith("x=")) {
							loc.setX(Double.valueOf(arg.substring(2)));
							argIt.remove();
						} else if (arg.startsWith("y=")) {
							loc.setY(Double.valueOf(arg.substring(2)));
							argIt.remove();
						} else if (arg.startsWith("z=")) {
							loc.setZ(Double.valueOf(arg.substring(2)));
							argIt.remove();
						}
					}
				}
				
				// add entities to the list if they pass the test of the first selector argument
				argIt = args.iterator();
				boolean foundFilterArg = false;
				do {
					String arg = argIt.next();
					if (handleFilterArgs(arg, (entitySelector, argVal) -> {
						
						for (Entity e : entities) {
							try {
								if (entitySelector.test(e, argVal, sender, loc)) {
									list.add(e);
								}
							} catch (Exception e1) {
								throw new IllegalArgumentException("Invalid selector argument " + arg, e1);
							}
						}
					})) {
						foundFilterArg = true;
						argIt.remove();
						break;
					}
				} while (argIt.hasNext());
				
				if (!foundFilterArg) {
					// no entities have been added because no filter arg was found
					list.addAll(entities);
				}
				
				if (list.isEmpty()) {
					return list;
				}
				
				// remove entities from the list if they do not pass the test of the selector arguments
				while (argIt.hasNext()) {
					final String arg = argIt.next();
					if (handleFilterArgs(arg, (entitySelector, argVal) -> {
						Iterator<Entity> it = list.iterator();
						while (it.hasNext()) {
							try {
								if (!entitySelector.test(it.next(), argVal, sender, loc)) {
									it.remove();
								}
							} catch (Exception e1) {
								throw new IllegalArgumentException("Invalid selector argument " + arg, e1);
							}
						}
					})) {
						argIt.remove();
					}
	
					if (list.isEmpty()) {
						return list;
					}
				}
	
				if (selChar == 's') {
					return list;
				}
				
				// distance argument handling
				{
					double dx = 0, dy = 0, dz = 0;
					boolean flag = false;
					for (final String arg : args) {
						if (arg.length() >= 4) {
							if (arg.startsWith("dx=")) {
								dx = Double.valueOf(arg.substring(3));
								flag = true;
							} else if (arg.startsWith("dy=")) {
								dy = Double.valueOf(arg.substring(3));
								flag = true;
							} else if (arg.startsWith("dz=")) {
								dz = Double.valueOf(arg.substring(3));
								flag = true;
							}
						}
					}
					
					if (flag) {
						double lx, gx, ly, gy, lz, gz;
						if (dx < 0) {
							lx = loc.getX() + dx;
							gx = loc.getX();
						} else {
							gx = loc.getX() + dx;
							lx = loc.getX();
						}
						if (dy < 0) {
							ly = loc.getY() + dy;
							gy = loc.getY();
						} else {
							gy = loc.getY() + dy;
							ly = loc.getY();
						}
						if (dz < 0) {
							lz = loc.getZ() + dz;
							gz = loc.getZ();
						} else {
							gz = loc.getZ() + dz;
							lz = loc.getZ();
						}
						
						Iterator<Entity> it = list.iterator();
						while (it.hasNext()) {
							Entity e = it.next();
							if (e.getWorld() != loc.getWorld()) {
								it.remove();
							} else {
								double ex = e.getLocation().getX();
								double ey = e.getLocation().getY();
								double ez = e.getLocation().getZ();
								// not a bounding box, but better than nothing...
								if (ex < lx || ex > gx || ey < ly || ey > gy || ez < lz || ez > gz) {
									it.remove();
								}
							}
						}
					}
				}
				
				// counter argument handling
				boolean handledCounter = false;
				for (String arg : args) {
					int pos = arg.indexOf('=');
					if (pos <= -1 || pos + 1 == arg.length()) {
						throw new IllegalArgumentException("Invalid selector argument " + arg);
					}
					
					String argKey = arg.substring(0, pos);
					if (argKey.equals("c")) {
						handledCounter = true;
						String argVal = arg.substring(pos + 1);
						int c = Integer.valueOf(argVal);
						if (c > 0) {
							if (selChar == 'r') {
								Collections.shuffle(list);
								while (list.size() > c) {
									list.remove(list.size() - 1);
								}
							} else if (selChar == 'p') {
								Entity e = closestTo(loc, list);
								list.clear();
								if (e != null) {
									list.add(e);
								}
							} else {
								Collections.sort(list, entityDistanceComparator(loc));
								while (list.size() > c) {
									list.remove(list.size() - 1);
								}
							}
						} else if (c < 0) {
							c *= -1;
							if (selChar == 'r') {
								Collections.shuffle(list);
								while (list.size() > c) {
									list.remove(list.size() - 1);
								}
							} else if (selChar == 'p') {
								Entity e = furthestTo(loc, list);
								list.clear();
								if (e != null) {
									list.add(e);
								}
							} else {
								Collections.sort(list, Collections.reverseOrder(entityDistanceComparator(loc)));
								while (list.size() > c) {
									list.remove(list.size() - 1);
								}
							}
						} else {
							list.clear();
						}
						break;
					}
				}
	
				if (list.isEmpty()) {
					return list;
				}
				
				if (!handledCounter && list.size() > 1) {
					if (selChar == 'p') {
						Entity e = closestTo(loc, list);
						list.clear();
						if (e != null) {
							list.add(e);
						}
					} else if (selChar == 'r') {
						Entity e = random(list);
						list.clear();
						if (e != null) {
							list.add(e);
						}
					}
				}
			}
			
			return list;
		}
		
		private static boolean handleFilterArgs(String arg, BiConsumer<EntitySelectorArgument, String> handler) {
			int pos = arg.indexOf('=');
			if (pos <= -1 || pos + 1 == arg.length()) {
				throw new IllegalArgumentException("Invalid selector argument " + arg);
			}
			
			String argKey = arg.substring(0, pos);
			String argVal = arg.substring(pos + 1);
			if (argVal.length() == 1 && argVal.charAt(0) == '!' && !argKey.equals("team")) {
				throw new IllegalArgumentException("Invalid selector argument " + arg);
			}
			
			if (Arrays.binarySearch(notFilteringArguments, argKey) >= 0) {
				return false;
			}
	
			EntitySelectorArgument entitySelector = filterArguments.get(argKey);
			if (entitySelector == null) {
				throw new IllegalArgumentException("Invalid selector argument " + arg);
			}
			
			handler.accept(entitySelector, argVal);
			return true;
		}
		
		private static Location getLocationFor(CommandSender sender) {
			if (sender instanceof Player) {
				return ((Player) sender).getLocation().clone();
			} else if (sender instanceof BlockCommandSender) {
				return ((BlockCommandSender) sender).getBlock().getLocation().clone();
			} else {
				return Bukkit.getWorlds().get(0).getSpawnLocation().clone();
			}
		}
		
		private static Entity closestTo(Location loc, Collection<? extends Entity> entities) {
			Entity closestEntity = null;
			double closestDistance = Double.MAX_VALUE;
			for (Entity entity : entities) {
				if (entity.getWorld() == loc.getWorld()) {
				double entityDistance = entity.getLocation().distanceSquared(loc);
					if (entityDistance < closestDistance) {
						closestDistance = entityDistance;
						closestEntity = entity;
					}
				}
			}
			return closestEntity;
		}
		
		private static Entity furthestTo(Location loc, Collection<? extends Entity> entities) {
			Entity furthestEntity = null;
			double furthestDistance = Double.MIN_NORMAL;
			for (Entity entity : entities) {
				if (entity.getWorld() == loc.getWorld()) {
					double entityDistance = entity.getLocation().distanceSquared(loc);
					if (entityDistance > furthestDistance) {
						furthestDistance = entityDistance;
						furthestEntity = entity;
					}
				}
			}
			return furthestEntity;
		}
		
		private static <T> T random(Collection<T> col) {
			int chosen = (int) Math.random() * col.size();
			for (T t : col) {
				if (chosen-- == 0) {
					return t;
				}
			}
			return null;
		}
		
		private static Comparator<Entity> entityDistanceComparator(Location l) {
			return (e1, e2) -> Double.compare(e1.getLocation().distanceSquared(l), e2.getLocation().distanceSquared(l));
		}
		
		private static double squared(double n) {
			return n * n;
		}
		
		public interface EntitySelectorArgument {
			boolean test(Entity e, String v, CommandSender s, Location l);
		}
	}
}
