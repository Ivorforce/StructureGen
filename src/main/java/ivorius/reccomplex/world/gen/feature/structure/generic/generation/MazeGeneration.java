/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.generation;

import com.google.gson.*;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.gentypes.TableDataSourceMazeGeneration;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.*;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Created by lukas on 07.10.14.
 */
public class MazeGeneration extends GenerationType implements WeightedSelector.Item
{
    private static Gson gson = createGson();

    public String mazeID;
    public Double weight;

    public SavedMazeComponent mazeComponent;

    public MazeGeneration()
    {
        this(null, null, "", new SavedMazeComponent(ConnectorStrategy.DEFAULT_WALL));
        mazeComponent.rooms.addAll(Selection.zeroSelection(3));
    }

    public MazeGeneration(@Nullable String id, Double weight, String mazeID, SavedMazeComponent mazeComponent)
    {
        super(id != null ? id : randomID(MazeGeneration.class));
        this.weight = weight;
        this.mazeID = mazeID;
        this.mazeComponent = mazeComponent;
    }

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(MazeGeneration.class, new Serializer());
        builder.registerTypeAdapter(SavedMazeComponent.class, new SavedMazeComponent.Serializer());
        builder.registerTypeAdapter(MazeRoom.class, new SavedMazeComponent.RoomSerializer());
        builder.registerTypeAdapter(SavedMazeReachability.class, new SavedMazeReachability.Serializer());
        builder.registerTypeAdapter(SavedMazePath.class, new SavedMazePath.Serializer());
        builder.registerTypeAdapter(SavedMazePathConnection.class, new SavedMazePathConnection.Serializer());
        builder.registerTypeAdapter(SavedMazePathConnection.ConditionalConnector.class, new SavedMazePathConnection.ConditionalConnector.Serializer());

        return builder.create();
    }

    public static Gson getGson()
    {
        return gson;
    }

    public String getMazeID()
    {
        return mazeID;
    }

    public void setMazeID(String mazeID)
    {
        this.mazeID = mazeID;
    }

    @Override
    public double getWeight()
    {
        return weight != null ? weight : 1.0;
    }

    public boolean hasDefaultWeight()
    {
        return weight == null;
    }

    @Override
    public String displayString()
    {
        return IvTranslations.format("reccomplex.generationInfo.mazeComponent.title", mazeID);
    }

    @Nullable
    @Override
    public Placer placer()
    {
        return null;
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceMazeGeneration(navigator, delegate, this);
    }

    public static class Serializer implements JsonSerializer<MazeGeneration>, JsonDeserializer<MazeGeneration>
    {
        @Override
        public MazeGeneration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "MazeGenerationInfo");

            String id = JsonUtils.getString(jsonObject, "id", null);

            String mazeID = JsonUtils.getString(jsonObject, "mazeID");

            JsonObject componentJson = JsonUtils.getJsonObject(jsonObject, "component", new JsonObject());

            Double weight = jsonObject.has("weight") ? JsonUtils.getDouble(jsonObject, "weight") : null;
            if (weight == null) // Legacy, weight was in SavedMazeComponent's JSON
            {
                if (componentJson.has("weightD"))
                    weight = JsonUtils.getDouble(componentJson, "weightD");
                else if (componentJson.has("weight"))
                    weight = JsonUtils.getInt(componentJson, "weight") * 0.01; // 100 was default
            }

            SavedMazeComponent mazeComponent = gson.fromJson(componentJson, SavedMazeComponent.class);

            return new MazeGeneration(id, weight, mazeID, mazeComponent);
        }

        @Override
        public JsonElement serialize(MazeGeneration src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", src.id);

            if (src.weight != null)
                jsonObject.addProperty("weight", src.weight);

            jsonObject.addProperty("mazeID", src.mazeID);
            jsonObject.add("component", gson.toJsonTree(src.mazeComponent));

            return jsonObject;
        }
    }
}
