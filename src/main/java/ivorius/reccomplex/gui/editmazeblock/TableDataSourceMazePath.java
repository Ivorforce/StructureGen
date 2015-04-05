/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.maze.MazePath;
import ivorius.reccomplex.gui.TableDirections;
import ivorius.reccomplex.gui.table.*;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazePath extends TableDataSourceSegmented implements TableElementPropertyListener
{
    private MazePath mazePath;
    private int[] boundsLower;
    private int[] boundsHigher;

    public TableDataSourceMazePath(MazePath mazePath, int[] boundsLower, int[] boundsHigher)
    {
        this.mazePath = mazePath;
        this.boundsLower = boundsLower;
        this.boundsHigher = boundsHigher;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? boundsLower.length : 1;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            String id = "pos" + index;
            String title = String.format("Position: %s", index == 0 ? "X" : index == 1 ? "Y" : index == 2 ? "Z" : "" + index);
            TableElementInteger element = new TableElementInteger(id, title, mazePath.sourceRoom.coordinates[index], boundsLower[index], boundsHigher[index]);
            element.addPropertyListener(this);
            return element;
        }
        else if (segment == 1)
        {
            TableElementEnum.Option<ForgeDirection>[] optionList = TableDirections.getDirectionOptions(ForgeDirection.VALID_DIRECTIONS);

            TableElementEnum element = new TableElementEnum<>("side", "Side", directionFromPath(mazePath), optionList);
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("side".equals(element.getID()))
        {
            MazePath path = pathFromDirection((ForgeDirection) element.getPropertyValue(), mazePath.sourceRoom.coordinates);
            mazePath.pathDimension = path.pathDimension;
            mazePath.pathGoesUp = path.pathGoesUp;
        }
        else
        {
            int index = Integer.valueOf(element.getID().substring(3));
            mazePath.sourceRoom.coordinates[index] = (int) element.getPropertyValue();
        }
    }

    public static ForgeDirection directionFromPath(MazePath path)
    {
        switch (path.pathDimension)
        {
            case 0:
                return path.pathGoesUp ? ForgeDirection.EAST : ForgeDirection.WEST;
            case 1:
                return path.pathGoesUp ? ForgeDirection.UP : ForgeDirection.DOWN;
            case 2:
                return path.pathGoesUp ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
        }

        return null;
    }

    public static MazePath pathFromDirection(ForgeDirection side, int[] room)
    {
        int pathDim = side.offsetX != 0 ? 0 : side.offsetY != 0 ? 1 : side.offsetZ != 0 ? 2 : -1;
        int offset = side.offsetX + side.offsetY + side.offsetZ;

        return new MazePath(pathDim, offset > 0, room[0], room[1], room[2]);
    }
}
