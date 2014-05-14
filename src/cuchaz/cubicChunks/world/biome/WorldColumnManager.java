package cuchaz.cubicChunks.world.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class WorldColumnManager
{
    private GenLayer genBiomes;

    /** A GenLayer containing the indices into BiomeGenBase.biomeList[] */
    private GenLayer biomeIndexLayer;

    /** The BiomeCache object for this world. */
    private BiomeCache biomeCache;

    /** A list of biomes that the player can spawn in. */
    private List<BiomeGenBase> biomesToSpawnIn;

    protected WorldColumnManager()
    {
        this.biomeCache = new BiomeCache(this);
        this.biomesToSpawnIn = new ArrayList<BiomeGenBase>();
        this.biomesToSpawnIn.add(BiomeGenBase.forest);
        this.biomesToSpawnIn.add(BiomeGenBase.plains);
        this.biomesToSpawnIn.add(BiomeGenBase.taiga);
        this.biomesToSpawnIn.add(BiomeGenBase.taigaHills);
        this.biomesToSpawnIn.add(BiomeGenBase.forestHills);
        this.biomesToSpawnIn.add(BiomeGenBase.jungle);
        this.biomesToSpawnIn.add(BiomeGenBase.jungleHills);
    }

    public WorldColumnManager(long par1, WorldType worldType)
    {
        this();
        GenLayer[] genLayer = GenLayer.initializeAllBiomeGenerators(par1, worldType);
        this.genBiomes = genLayer[0];
        this.biomeIndexLayer = genLayer[1];
    }

    public WorldColumnManager(World world)
    {
        this(world.getSeed(), world.getWorldInfo().getTerrainType());
    }

    /**
     * Gets the list of valid biomes for the player to spawn in.
     */
    public List<BiomeGenBase> getBiomesToSpawnIn()
    {
        return this.biomesToSpawnIn;
    }

    /**
     * Returns the BiomeGenBase related to the x, z position on the world.
     */
    public BiomeGenBase getBiomeGenAt(int xAbs, int zAbs)
    {
        return this.biomeCache.getBiomeGenAt(xAbs, zAbs);
    }

    /**
     * Returns a list of rainfall values for the specified blocks. Args: listToReuse, x, z, width, length.
     */
    public float[] getRainfall(float[] downfall, int cubeX, int cubeZ, int width, int length)
    {
        IntCache.resetIntCache();

        if (downfall == null || downfall.length < width * length)
        {
            downfall = new float[width * length];
        }

        int[] var6 = this.biomeIndexLayer.getInts(cubeX, cubeZ, width, length);

        for (int i = 0; i < width * length; ++i)
        {
            try
            {
                float rainfall = (float)BiomeGenBase.getBiome(var6[i]).getIntRainfall() / 65536.0F;

                if (rainfall > 1.0F)
                {
                    rainfall = 1.0F;
                }

                downfall[i] = rainfall;
            }
            catch (Throwable var11)
            {
                CrashReport var9 = CrashReport.makeCrashReport(var11, "Invalid Biome id");
                CrashReportCategory var10 = var9.makeCategory("DownfallBlock");
                var10.addCrashSection("biome id", Integer.valueOf(i));
                var10.addCrashSection("downfalls[] size", Integer.valueOf(downfall.length));
                var10.addCrashSection("x", Integer.valueOf(cubeX));
                var10.addCrashSection("z", Integer.valueOf(cubeZ));
                var10.addCrashSection("w", Integer.valueOf(width));
                var10.addCrashSection("h", Integer.valueOf(length));
                throw new ReportedException(var9);
            }
        }

        return downfall;
    }

    /**
     * Return an adjusted version of a given temperature based on the y height. (not really).
     */
    public float getTemperatureAtHeight(float temp, int height)
    {
        return temp;
    }

    /**
     * Returns an array of biomes for the location input.
     */
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomes, int cubeX, int cubeZ, int width, int length)
    {
        IntCache.resetIntCache();

        if (biomes == null || biomes.length < width * length)
        {
            biomes = new BiomeGenBase[width * length];
        }

        int[] intArray = this.genBiomes.getInts(cubeX, cubeZ, width, length);

        try
        {
            for (int i = 0; i < width * length; ++i)
            {
                biomes[i] = BiomeGenBase.getBiome(intArray[i]);
            }

            return biomes;
        }
        catch (Throwable var10)
        {
            CrashReport var8 = CrashReport.makeCrashReport(var10, "Invalid Biome id");
            CrashReportCategory var9 = var8.makeCategory("RawBiomeBlock");
            var9.addCrashSection("biomes[] size", Integer.valueOf(biomes.length));
            var9.addCrashSection("x", Integer.valueOf(cubeX));
            var9.addCrashSection("z", Integer.valueOf(cubeZ));
            var9.addCrashSection("w", Integer.valueOf(width));
            var9.addCrashSection("h", Integer.valueOf(length));
            throw new ReportedException(var8);
        }
    }

    /**
     * Returns biomes to use for the blocks and loads the other data like temperature and humidity onto the
     * WorldChunkManager Args: oldBiomeList, x, z, width, depth
     */
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomes, int cubeX, int cubeZ, int width, int length)
    {
        return this.getBiomeGenAt(biomes, cubeX, cubeZ, width, length, true);
    }

    /**
     * Return a list of biomes for the specified blocks. Args: listToReuse, x, y, width, length, cacheFlag (if false,
     * don't check biomeCache to avoid infinite loop in BiomeCacheBlock)
     */
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] biomes, int cubeX, int cubeZ, int width, int length, boolean flag)
    {
        IntCache.resetIntCache();

        if (biomes == null || biomes.length < width * length)
        {
            biomes = new BiomeGenBase[width * length];
        }

        if (flag && width == 16 && length == 16 && (cubeX & 15) == 0 && (cubeZ & 15) == 0)
        {
            BiomeGenBase[] cachedBiomes = this.biomeCache.getCachedBiomes(cubeX, cubeZ);
            System.arraycopy(cachedBiomes, 0, biomes, 0, width * length);
            return biomes;
        }
        else
        {
            int[] aInt = this.biomeIndexLayer.getInts(cubeX, cubeZ, width, length);

            for (int i = 0; i < width * length; ++i)
            {
                biomes[i] = BiomeGenBase.getBiome(aInt[i]);
            }

            return biomes;
        }
    }

    /**
     * checks given Chunk's Biomes against List of allowed ones
     */
    public boolean areBiomesViable(int par1, int par2, int par3, List par4List)
    {
        IntCache.resetIntCache();
        int var5 = par1 - par3 >> 2;
        int var6 = par2 - par3 >> 2;
        int var7 = par1 + par3 >> 2;
        int var8 = par2 + par3 >> 2;
        int width = var7 - var5 + 1;
        int length = var8 - var6 + 1;
        int[] aInt = this.genBiomes.getInts(var5, var6, width, length);

        try
        {
            for (int i = 0; i < width * length; ++i)
            {
                BiomeGenBase biome = BiomeGenBase.getBiome(aInt[i]);

                if (!par4List.contains(biome))
                {
                    return false;
                }
            }

            return true;
        }
        catch (Throwable var15)
        {
            CrashReport var13 = CrashReport.makeCrashReport(var15, "Invalid Biome id");
            CrashReportCategory var14 = var13.makeCategory("Layer");
            var14.addCrashSection("Layer", this.genBiomes.toString());
            var14.addCrashSection("x", Integer.valueOf(par1));
            var14.addCrashSection("z", Integer.valueOf(par2));
            var14.addCrashSection("radius", Integer.valueOf(par3));
            var14.addCrashSection("allowed", par4List);
            throw new ReportedException(var13);
        }
    }

    public ChunkPosition func_150795_a(int p_150795_1_, int p_150795_2_, int p_150795_3_, List list, Random rand)
    {
        IntCache.resetIntCache();
        int var6 = p_150795_1_ - p_150795_3_ >> 2;
        int var7 = p_150795_2_ - p_150795_3_ >> 2;
        int var8 = p_150795_1_ + p_150795_3_ >> 2;
        int var9 = p_150795_2_ + p_150795_3_ >> 2;
        int var10 = var8 - var6 + 1;
        int var11 = var9 - var7 + 1;
        int[] aInt = this.genBiomes.getInts(var6, var7, var10, var11);
        ChunkPosition chunkPosition = null;
        int var14 = 0;

        for (int i = 0; i < var10 * var11; ++i)
        {
            int cubeX = var6 + i % var10 << 2;
            int cubeZ = var7 + i / var10 << 2;
            BiomeGenBase biome = BiomeGenBase.getBiome(aInt[i]);

            if (list.contains(biome) && (chunkPosition == null || rand.nextInt(var14 + 1) == 0))
            {
                chunkPosition = new ChunkPosition(cubeX, 0, cubeZ);
                ++var14;
            }
        }

        return chunkPosition;
    }

    /**
     * Calls the WorldChunkManager's biomeCache.cleanupCache()
     */
    public void cleanupCache()
    {
        this.biomeCache.cleanupCache();
    }
}
