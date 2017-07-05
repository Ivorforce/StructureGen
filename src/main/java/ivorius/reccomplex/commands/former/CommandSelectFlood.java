/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.commands.former;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ivorius.ivtoolkit.blocks.BlockStates;
import ivorius.ivtoolkit.world.MockWorld;
import ivorius.mcopts.commands.CommandExpecting;
import ivorius.mcopts.commands.parameters.MCP;
import ivorius.mcopts.commands.parameters.NaP;
import ivorius.mcopts.commands.parameters.Parameters;
import ivorius.mcopts.commands.parameters.expect.Expect;
import ivorius.mcopts.commands.parameters.expect.MCE;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.capability.SelectionOwner;
import ivorius.reccomplex.commands.CommandVirtual;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.commands.parameters.RCP;
import ivorius.reccomplex.commands.parameters.expect.RCE;
import ivorius.reccomplex.utils.expression.PositionedBlockExpression;
import ivorius.reccomplex.utils.expression.PreloadedBooleanExpression;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 09.06.14.
 */
public class CommandSelectFlood extends CommandExpecting implements CommandVirtual
{
    public static final int MAX_FLOOD = 50 * 50 * 50;

    @Override
    public String getCommandName()
    {
        return RCConfig.commandPrefix + "flood";
    }

    @Override
    public void expect(Expect expect)
    {
        expect
                .then(MCE::block)
                .then(MCE::block).descriptionU("source expression").optional().repeat()
                .named("direction", "d").then(RCE::directionExpression)
                .named("metadata", "m").then(RCE::metadata)
        ;
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public void execute(MockWorld world, ICommandSender sender, String[] args) throws CommandException
    {
        Parameters parameters = Parameters.of(args, expect()::declare);

        SelectionOwner selectionOwner = RCCommands.getSelectionOwner(sender, null, true);
        RCCommands.assertSize(sender, selectionOwner);

        PreloadedBooleanExpression<EnumFacing> facingExpression = parameters.get("direction").rest(NaP::join).orElse("")
                .to(RCP.expression(PreloadedBooleanExpression.<EnumFacing>with(exp ->
                {
                    exp.addConstants(EnumFacing.values());
                    exp.addEvaluators(axis -> facing -> facing.getAxis() == axis, EnumFacing.Axis.values());
                    exp.addEvaluator("horizontal", f -> f.getHorizontalIndex() >= 0);
                    exp.addEvaluator("vertical", f -> f.getHorizontalIndex() < 0);
                }))).require();

        PositionedBlockExpression matcher = parameters.get(1).rest(NaP::join).orElse("is:air")
                .to(RCP.expression(new PositionedBlockExpression(RecurrentComplex.specialRegistry))).require();

        List<EnumFacing> available = Arrays.stream(EnumFacing.values()).filter(facingExpression).collect(Collectors.toList());

        List<BlockPos> dirty = Lists.newArrayList(selectionOwner.getSelection());
        Set<BlockPos> visited = Sets.newHashSet(dirty);

        Block dstBlock = parameters.get(0).to(MCP.block(sender)).require();
        int[] dstMeta = parameters.get("metadata").to(RCP::metadatas).optional().orElse(new int[1]);
        List<IBlockState> dst = IntStream.of(dstMeta).mapToObj(m -> BlockStates.fromMetadata(dstBlock, m)).collect(Collectors.toList());

        while (!dirty.isEmpty())
        {
            BlockPos pos = dirty.remove(dirty.size() - 1);

            for (EnumFacing facing : available)
            {
                BlockPos offset = pos.offset(facing);
                if (matcher.evaluate(() -> PositionedBlockExpression.Argument.at(world, offset))
                        && visited.add(offset))
                    dirty.add(offset);
            }

            if (visited.size() > MAX_FLOOD)
                throw new CommandException("Area too big to flood!");
        }

        for (BlockPos pos : visited)
        {
            IBlockState state = dst.get(world.rand().nextInt(dst.size()));
            world.setBlockState(pos, state, 2);
        }
    }
}
