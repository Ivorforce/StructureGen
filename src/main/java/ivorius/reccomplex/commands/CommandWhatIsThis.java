/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.commands;

import com.google.common.collect.Lists;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.BlockPos;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.ServerTranslations;
import ivorius.reccomplex.worldgen.StructureGenerationData;
import joptsimple.internal.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class CommandWhatIsThis extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "whatisthis";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender var1)
    {
        return ServerTranslations.usage("commands.whatisthis.usage");
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) throws NumberInvalidException
    {
        World world = commandSender.getEntityWorld();

        BlockPos pos = commandSender.getPosition();

        if (args.length >= 3)
            pos = parseBlockPos(commandSender, args, 0, false);

        Collection<StructureGenerationData.Entry> entries = StructureGenerationData.get(world).getEntriesAt(pos);
        if (entries.size() > 0)
        {
            List<StructureGenerationData.Entry> ordered = Lists.newArrayList(entries);
            if (ordered.size() > 1)
                commandSender.addChatMessage(ServerTranslations.format("commands.whatisthis.many", Strings.join(Lists.transform(ordered, StructureGenerationData.Entry::getStructureID), ", ")));
            else
                commandSender.addChatMessage(ServerTranslations.format("commands.whatisthis.one", ordered.get(0).getStructureID()));
        }
        else
            commandSender.addChatMessage(ServerTranslations.format("commands.whatisthis.none"));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos pos)
    {
        if (args.length == 1 || args.length == 2 || args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }

        return null;
    }
}
