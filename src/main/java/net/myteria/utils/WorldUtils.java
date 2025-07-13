package net.myteria.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_21_R5.CraftServer;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;

import com.google.common.collect.ImmutableList;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.WorldProviderNormal;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.datafix.DataConverterRegistry;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.MobSpawnerCat;
import net.minecraft.world.entity.npc.MobSpawnerTrader;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.MobSpawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.WorldDimension;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.MobSpawnerPatrol;
import net.minecraft.world.level.levelgen.MobSpawnerPhantom;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.level.storage.IWorldDataServer;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.SaveData;
import net.minecraft.world.level.storage.WorldDataServer;
import net.minecraft.world.level.storage.WorldInfo;
import net.minecraft.world.level.validation.ContentValidationException;
import net.myteria.PlayerHousing;
import net.myteria.objects.PlayerWorld;

public class WorldUtils {
	
	public org.bukkit.World createWorld(WorldCreator creator) {
		CraftServer server = DedicatedServer.getServer().server;
        String levelName;
        WorldDataServer worlddata;
        Dynamic<?> dynamic;
        Convertable.ConversionSession worldSession;

        String name = creator.name();
        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        org.bukkit.World world = Bukkit.getWorld(name);
        if (world != null) {
            return world;
        }
        if (biomeProvider == null && generator != null) {
            biomeProvider = server.getWorld("world").getBiomeProvider();
         }
        ResourceKey<WorldDimension> actualDimension = WorldDimension.b;
        try {
            worldSession = Convertable.b(Bukkit.getWorldContainer().toPath()).validateAndCreateAccess(name, actualDimension);
        }
        catch (IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }
        WorldInfo worldinfo;
        if (worldSession.m()) {
            try {
                dynamic = worldSession.h();
                worldinfo = worldSession.a(dynamic);
            }
            catch (IOException ioexception) {
                Convertable.b convertable_b = worldSession.e();
                MinecraftServer.l.warn("Failed to load world data from {}", (Object)convertable_b.b(), ioexception);
                MinecraftServer.l.info("Attempting to use fallback");
                try {
                    dynamic = worldSession.i();
                    worldinfo = worldSession.a(dynamic);
                }
                catch (IOException ioexception1) {
                    MinecraftServer.l.error("Failed to load world data from {}", (Object)convertable_b.c(), ioexception1);
                    MinecraftServer.l.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", (Object)convertable_b.b(), (Object)convertable_b.c());
                    return null;
                }
                worldSession.n();
            }
            if (worldinfo.d()) {
                MinecraftServer.l.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return null;
            }
            if (!worldinfo.r()) {
                MinecraftServer.l.info("This world was created by an incompatible version.");
                return null;
            }
        } else {
            dynamic = null;
        }
        boolean hardcore = creator.hardcore();
        WorldLoader.a worldloader = server.getServer().worldLoader;
        IRegistryCustom.Dimension iregistrycustom_dimension = worldloader.d();
        IRegistry<WorldDimension> iregistry = iregistrycustom_dimension.f(Registries.bv);
        if (dynamic != null) {
            LevelDataAndDimensions leveldataanddimensions = Convertable.a(dynamic, worldloader.b(), iregistry, worldloader.c());
            worlddata = (WorldDataServer)leveldataanddimensions.a();
            iregistrycustom_dimension = leveldataanddimensions.b().b();
        } else {
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(ChatDeserializer.a((String)(creator.generatorSettings().isEmpty() ? "{}" : creator.generatorSettings())), creator.type().name().toLowerCase(Locale.ROOT));
            WorldSettings worldsettings = new WorldSettings(name, EnumGamemode.a((int)server.getDefaultGameMode().getValue()), hardcore, EnumDifficulty.b, false, new GameRules(worldloader.b().b()), worldloader.b());
            WorldDimensions worlddimensions = properties.a(worldloader.c());
            WorldDimensions.b worlddimensions_b = worlddimensions.a(iregistry);
            Lifecycle lifecycle = worlddimensions_b.a().add(worldloader.c().d());
            worlddata = new WorldDataServer(worldsettings, worldoptions, worlddimensions_b.d(), lifecycle);
            iregistrycustom_dimension = worlddimensions_b.b();
        }
        worlddata.customDimensions = iregistry = iregistrycustom_dimension.f(Registries.bv);
        worlddata.checkName(name);
        worlddata.a(server.getServer().getServerModName(), server.getServer().Q().a());

        long seed = BiomeManager.a((long)creator.seed());
        List<MobSpawner> list = ImmutableList.of(new MobSpawnerPhantom(), new MobSpawnerPatrol(), new MobSpawnerCat(), new VillageSiege(),new MobSpawnerTrader((IWorldDataServer)worlddata));
        WorldDimension worlddimension = (WorldDimension)iregistry.c(actualDimension);
        ResourceKey<World> worldKey = name.equals(((levelName = server.getServer().a().l) + "_nether")) ? World.j : (name.equals((Object)(levelName + "_the_end")) ? net.minecraft.world.level.World.k : ResourceKey.a(Registries.bu, MinecraftKey.b(name.toLowerCase(Locale.ROOT))));
        
        if (!creator.keepSpawnInMemory()) {
            ((GameRules.GameRuleInt)worlddata.o().b(GameRules.ac)).set(0, null);
        }
        
        WorldServer internal = new WorldServer(
        		server.getServer(), 
        		server.getServer().ay, 
        		worldSession, 
        		worlddata, 
        		worldKey, 
        		worlddimension, 
        		server.getServer().H.create(worlddata.o().d(GameRules.ac)), 
        		worlddata.A(), 
        		seed, 
        		(creator.environment() == Environment.NORMAL ? list : ImmutableList.of()), 
        		true, 
        		server.getServer().J().O(), 
        		creator.environment(), 
        		generator, 
        		biomeProvider);
        initWorld(internal, worlddata, worlddata, worlddata.y());
        
        server.getServer().prepareLevels(internal.n().a.F, internal);
        
        server.getServer().addLevel(internal);
        server.getServer().ag().a(internal);
        if (worlddata.E() != null) {
            server.getServer().aM().a(worlddata.E(), server.getServer().ba());
         }
        server.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
        return internal.getWorld();
    }

	public void initWorld(WorldServer worldserver, IWorldDataServer iworlddataserver, SaveData saveData, WorldOptions worldoptions) {
		  CraftServer server = DedicatedServer.getServer().server;
	      boolean flag = saveData.A();
	      if (worldserver.generator != null) {
	         worldserver.getWorld().getPopulators().addAll(worldserver.generator.getDefaultPopulators(worldserver.getWorld()));
	      }

	      WorldBorder worldborder = worldserver.F_();
	      worldborder.a(iworlddataserver.p());
	      server.getPluginManager().callEvent(new WorldInitEvent(worldserver.getWorld()));
	      if (!iworlddataserver.n()) {
	         try {
	            setInitialSpawn(worldserver, iworlddataserver, flag);
	            iworlddataserver.c(true);
	         } catch (Throwable var11) {
	            CrashReport crashreport = CrashReport.a(var11, "Exception initializing level");

	            try {
	               worldserver.a(crashreport);
	            } catch (Throwable var10) {}

	            throw new ReportedException(crashreport);
	         }

	         iworlddataserver.c(true);
	      }

	   }
	public void unloadWorld(PlayerWorld world) {
	    org.bukkit.World bukkitWorld = world.getWorld();
	    PlayerHousing.getAPI().removeWorld(world);
		if (bukkitWorld.getPlayerCount() != 0) {
			for (Player player : bukkitWorld.getPlayers()) {
				 Location spawn = Bukkit.getWorld("world").getSpawnLocation();
			     player.teleportAsync(spawn);
			 }
		}
		CraftWorld craftWorld = (CraftWorld) bukkitWorld;
		WorldServer worldServer = craftWorld.getHandle();
		Bukkit.getRegionScheduler().execute(PlayerHousing.getInstance(), bukkitWorld.getSpawnLocation(), () -> {
			worldServer.n().d();
			try {
				worldServer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		MinecraftServer server = DedicatedServer.getServer();
		server.removeLevel(worldServer);
		Bukkit.getServer().getPluginManager().callEvent(new WorldUnloadEvent(bukkitWorld));
	}
	
	/**
     * Finds a random location in the given world, constrained by min/max X/Z,
     * then finds a safe Y position so that the block is air
     * (and the one below is solid, so the player won’t fall).
     *
     * @param world The Bukkit world object
     * @param minX  Minimum X coordinate (e.g., -1000)
     * @param maxX  Maximum X coordinate (e.g., 1000)
     * @param minZ  Minimum Z coordinate (e.g., -1000)
     * @param maxZ  Maximum Z coordinate (e.g., 1000)
     * @return A safe random spawn location, or null if none found
     */
    public static Location getRandomSafeLocation(org.bukkit.World world, int min, int max) {
    	Random RANDOM = new Random();
        if (world == null) {
            Bukkit.getLogger().warning("World is null!");
            return null;
        }
        
        int x = RANDOM.nextInt(max - min + 1) + min;
        int z = RANDOM.nextInt(max - min + 1) + min;

        int highestY = Math.min(world.getMaxHeight(), world.getHighestBlockYAt(x, z) + 5);
        for (int y = highestY; y > world.getMinHeight(); y--) {
            Material block = world.getBlockAt(x, y, z).getType();
            Material belowBlock = world.getBlockAt(x, y - 1, z).getType();
            
            if (block == Material.AIR && belowBlock.isSolid()) {
                return new Location(world, x + 0.5, y, z + 0.5);
            }
        }
        int fallbackY = world.getHighestBlockYAt(x, z) + 1;
        if (fallbackY >= world.getMinHeight() && fallbackY <= world.getMaxHeight()) {
            return new Location(world, x + 0.5, fallbackY, z + 0.5);
        }
        Bukkit.getLogger().warning("No suitable random spawn found at (" + x + ", *, " + z + ")");
        return null;
    }
    
    private void setInitialSpawn(WorldServer worldserver, IWorldDataServer levelData, boolean debug) {
    	Bukkit.getGlobalRegionScheduler().execute(PlayerHousing.getInstance(), () -> {
    		Bukkit.getRegionScheduler().execute(PlayerHousing.getInstance(), worldserver.getWorld(), 0, 0, () -> {
    		Location loc = getRandomSafeLocation(worldserver.getWorld(), 0, 64);
           	   levelData.a(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.blockZ()), 0.0F);
              });
         	Bukkit.getLogger().info("Set spawn for world.");
            levelData.c(true);
    	});

     }

}
