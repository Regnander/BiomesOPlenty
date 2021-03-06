/*******************************************************************************
 * Copyright 2015-2016, the Biomes O' Plenty Team
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package biomesoplenty.common.biome.overworld;

import com.google.common.base.CaseFormat;

import biomesoplenty.api.biome.BOPBiomes;
import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.block.BlockQueries;
import biomesoplenty.api.enums.BOPClimates;
import biomesoplenty.api.enums.BOPGems;
import biomesoplenty.api.enums.BOPPlants;
import biomesoplenty.api.enums.BOPTrees;
import biomesoplenty.api.generation.GeneratorStage;
import biomesoplenty.common.biome.BOPBiome.PropsBuilder;
import biomesoplenty.common.biome.overworld.BiomeGenRedwoodForest.ForestType;
import biomesoplenty.common.block.BlockBOPDirt;
import biomesoplenty.common.block.BlockBOPGrass;
import biomesoplenty.common.util.biome.GeneratorUtils.ScatterYMethod;
import biomesoplenty.common.world.generator.GeneratorBlobs;
import biomesoplenty.common.world.generator.GeneratorFlora;
import biomesoplenty.common.world.generator.GeneratorGrass;
import biomesoplenty.common.world.generator.GeneratorLakes;
import biomesoplenty.common.world.generator.GeneratorOreSingle;
import biomesoplenty.common.world.generator.GeneratorWaterside;
import biomesoplenty.common.world.generator.GeneratorWeighted;
import biomesoplenty.common.world.generator.tree.GeneratorBush;
import biomesoplenty.common.world.generator.tree.GeneratorTwigletTree;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class BiomeGenTundra extends BOPOverworldBiome
{    
	public enum TundraType {TUNDRA, SNOWY_TUNDRA}
	
	public TundraType type;
	
    public BiomeGenTundra(TundraType type)
    {
        super(type.name().toLowerCase(), new PropsBuilder(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, type.toString())).withGuiColour(0xA09456).withTemperature((type == TundraType.TUNDRA) ? 0.2F : 0.0F).withRainfall(0.5F));

        this.type = type;
        
        // terrain
        this.terrainSettings.avgHeight(64).heightVariation(5, 10).minHeight(59);
        
        this.topBlock = BOPBlocks.grass.getDefaultState().withProperty(BlockBOPGrass.VARIANT, BlockBOPGrass.BOPGrassType.SILTY);
        this.fillerBlock = BOPBlocks.dirt.getDefaultState().withProperty(BlockBOPDirt.VARIANT, BlockBOPDirt.BOPDirtType.SILTY);
        
        if (type == TundraType.TUNDRA)
        {
        	this.addWeight(BOPClimates.TUNDRA, 10);
        }
        
        this.canGenerateRivers = false;
        this.canSpawnInBiome = false;
        this.canGenerateVillages = false;
        
        if (BOPBiomes.gravel_beach.isPresent())
        {
        	this.beachBiomeLocation = ((BOPOverworldBiome)BOPBiomes.gravel_beach.get()).getResourceLocation();
        }
        
        this.spawnableCreatureList.clear();
        this.spawnableCreatureList.add(new SpawnListEntry(EntityRabbit.class, 4, 2, 3));
        
        // boulders
        this.addGenerator("large_boulders", GeneratorStage.SAND, (new GeneratorBlobs.Builder()).amountPerChunk(0.15F).placeOn(this.topBlock).with(Blocks.COBBLESTONE.getDefaultState()).minRadius(1.0F).maxRadius(3.0F).numBalls(3).scatterYMethod(ScatterYMethod.AT_SURFACE).create());
        this.addGenerator("small_boulders", GeneratorStage.SAND, (new GeneratorFlora.Builder()).amountPerChunk(0.3F).replace(Material.AIR).placeOn(this.topBlock).with(Blocks.COBBLESTONE.getDefaultState()).scatterYMethod(ScatterYMethod.AT_SURFACE).create());
        
        // gravel
        this.addGenerator("gravel", GeneratorStage.SAND_PASS2, (new GeneratorWaterside.Builder()).amountPerChunk(12).maxRadius(7).with(Blocks.GRAVEL.getDefaultState()).create());
        
        // lakes
        this.addGenerator("lakes", GeneratorStage.SAND, (new GeneratorLakes.Builder()).amountPerChunk(0.1F).waterLakeForBiome(this).create());        

        // trees
        GeneratorWeighted treeGenerator = new GeneratorWeighted(4.0F);
        this.addGenerator("trees", GeneratorStage.TREE, treeGenerator);
        treeGenerator.add("twiglet", 1, (new GeneratorTwigletTree.Builder()).minHeight(2).maxHeight(2).log(BlockPlanks.EnumType.OAK).leaves(BOPTrees.MAPLE).create());
        treeGenerator.add("small_bush", 2, (new GeneratorFlora.Builder()).placeOn(this.topBlock).replace(Material.AIR).withNonDecayingLeaf(BOPTrees.DEAD).create());
        
        // other plants
        this.addGenerator("dead_leaf_piles", GeneratorStage.FLOWERS,(new GeneratorFlora.Builder()).amountPerChunk(0.3F).placeOn(BlockQueries.fertile).with(BOPPlants.DEADLEAFPILE).generationAttempts(64).create());
 
        // water plants
        this.addGenerator("water_reeds", GeneratorStage.LILYPAD, (new GeneratorFlora.Builder()).amountPerChunk(0.2F).with(BOPPlants.REED).generationAttempts(32).create());
        
        // grasses
        GeneratorWeighted grassGenerator = new GeneratorWeighted(1.5F);
        this.addGenerator("grass", GeneratorStage.GRASS, grassGenerator);
        grassGenerator.add("shortgrass", 4, (new GeneratorGrass.Builder()).with(BOPPlants.SHORTGRASS).create());
        grassGenerator.add("mediumgrass", 2, (new GeneratorGrass.Builder()).with(BOPPlants.MEDIUMGRASS).create());
        grassGenerator.add("tallgrass", 1, (new GeneratorGrass.Builder()).with(BlockTallGrass.EnumType.GRASS).create());

        // gem
        this.addGenerator("tanzanite", GeneratorStage.SAND, (new GeneratorOreSingle.Builder()).amountPerChunk(12).with(BOPGems.TANZANITE).create());
    }
    
    @Override
    public int getGrassColorAtPos(BlockPos pos)
    {
        return getModdedBiomeGrassColor(0xB78658);
    }
    
    @Override
    public int getFoliageColorAtPos(BlockPos pos)
    {
        return getModdedBiomeFoliageColor(0xC1954D);
    }

}