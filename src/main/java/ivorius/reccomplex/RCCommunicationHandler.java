/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import cpw.mods.fml.common.event.FMLInterModComms;
import ivorius.ivtoolkit.tools.IvFMLIntercommHandler;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.worldgen.StructureSelector;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.helpers.Strings;

import java.util.Arrays;

/**
 * Created by lukas on 07.06.14.
 */
public class RCCommunicationHandler extends IvFMLIntercommHandler
{
    public RCCommunicationHandler(Logger logger, String modOwnerID, Object modInstance)
    {
        super(logger, modOwnerID, modInstance);
    }

    @Override
    protected boolean handleMessage(FMLInterModComms.IMCMessage message, boolean server, boolean runtime)
    {
        if (isMessage("loadStructure", message, NBTTagCompound.class))
        {
            // Note that this is not required for default loading, using the correct directories.
            // Only use this if you want to load it conditionally.

            NBTTagCompound cmp = message.getNBTValue();
            String structurePath = cmp.getString("structurePath");
            String structureID = cmp.getString("structureID");
            boolean generates = cmp.getBoolean("generates");

            if (!StructureRegistry.registerStructure(new ResourceLocation(structurePath), structureID, generates))
                getLogger().warn(String.format("Could not find structure with path '%s and id '%s'", structurePath, structureID));

            return true;
        }
        else if (isMessage("loadInventoryGenerator", message, NBTTagCompound.class))
        {
            // Note that this is not required for default loading, using the correct directories.
            // Only use this if you want to load it conditionally.

            NBTTagCompound cmp = message.getNBTValue();
            String genPath = cmp.getString("genPath");
            String genID = cmp.getString("genID");
            boolean generates = cmp.getBoolean("generates");

            if (!GenericItemCollectionRegistry.register(new ResourceLocation(genPath), genID, generates))
                getLogger().warn(String.format("Could not find inventory generator with path '%s and id '%s'", genPath, genID));

            return true;
        }
        else if (isMessage("registerDimension", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();
            int dimensionID = cmp.getInteger("dimensionID");
            String[] types = IvNBTHelper.readNBTStrings("types", cmp); // NBTTagList of NBTTagString

            if (types != null)
                DimensionDictionary.registerDimensionTypes(dimensionID, Arrays.asList(types));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'types' key!");

            return true;
        }
        else if (isMessage("unregisterDimension", message, NBTTagCompound.class))
        {
            NBTTagCompound cmp = message.getNBTValue();
            int dimensionID = cmp.getInteger("dimensionID");
            String[] types = IvNBTHelper.readNBTStrings("types", cmp); // NBTTagList of NBTTagString

            if (types != null)
                DimensionDictionary.unregisterDimensionTypes(dimensionID, Arrays.asList(types));
            else
                DimensionDictionary.unregisterDimensionTypes(dimensionID, null);

            return true;
        }
        else if (isMessage("registerDimensionType", message, String.class))
        {
            DimensionDictionary.registerType(message.getStringValue());
            return true;
        }
        else if (isMessage("registerDimensionSubtypes", message, String.class))
        {
            NBTTagCompound cmp = message.getNBTValue();
            String type = cmp.getString("type");
            String[] subtypes = IvNBTHelper.readNBTStrings("subtypes", cmp); // NBTTagList of NBTTagString

            if (!Strings.isEmpty(type))
                DimensionDictionary.registerSubtypes(type, Arrays.asList(subtypes));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'subtypes' key!");

            return true;
        }
        else if (isMessage("registerDimensionSupertypes", message, String.class))
        {
            NBTTagCompound cmp = message.getNBTValue();
            String type = cmp.getString("type");
            String[] subtypes = IvNBTHelper.readNBTStrings("supertypes", cmp); // NBTTagList of NBTTagString

            if (!Strings.isEmpty(type))
                DimensionDictionary.registerSupertypes(type, Arrays.asList(subtypes));
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'supertypes' key!");

            return true;
        }
        else if (isMessage("registerSimpleSpawnCategory", message, String.class))
        {
            NBTTagCompound cmp = message.getNBTValue();
            String id = cmp.getString("id");

            // If no biome selector matches, this value will be returned.
            float defaultSpawnChance = cmp.getFloat("defaultSpawnChance");
            boolean selectableInGui = cmp.getBoolean("selectableInGui");

            // If less structures than this cap are registered, the overall spawn chance will decrease so not to spam the same structures over and over.
            int structureMinCap = cmp.getInteger("structureMinCap");

            // List of {chance}:{ID}. These selectors work the same as structure biome selectors.
            // e.g. 0.232:Type:PLAINS,COLD
            // e.g. 1:Ocean
            String[] biomeTypes = IvNBTHelper.readNBTStrings("biomeTypes", cmp); // NBTTagList of NBTTagString

            if (!Strings.isEmpty(id))
            {
                StructureSelector.GenerationInfo[] biomeInfos = new StructureSelector.GenerationInfo[biomeTypes.length];
                for (int i = 0; i < biomeTypes.length; i++)
                {
                    String[] parts = biomeTypes[i].split(":", 2);
                    biomeInfos[i] = new StructureSelector.GenerationInfo(Float.valueOf(parts[0]), new BiomeMatcher(parts[1]));
                }

                StructureSelector.registerCategory(id, new StructureSelector.SimpleCategory(defaultSpawnChance,
                        Arrays.asList(biomeInfos), selectableInGui, structureMinCap));
            }
            else
                getLogger().warn("Could not handle message with key '" + message.key + "' - missing 'id' key!");

            return true;
        }

        return false;
    }
}
