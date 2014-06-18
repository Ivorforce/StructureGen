/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.random;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 17.06.14.
 */
public class Artifact
{
    private static List<String> objectTypes = Arrays.asList("tool", "eye", "board", "weapon", "toy", "memory", "relic", "orb", "block", "device", "killer", "eraser", "diminisher", "supporter", "spreader", "eater", "devourer", "helper", "teacher", "inventor", "partner", "remedy", "destination", "constructor", "prophet", "stone", "artifact", "scroll");
    private static List<String> traits = Arrays.asList("ominous", "odd", "uncontrollable", "catastrophical", "silent", "furious", "banned", "secret", "unknown", "popular", "forgotten", "lost", "heroic", "famous", "colossal", "mad", "wise", "uncontrollable", "glorious", "unprecedented", "unbelievable", "incredible", "lesser", "greater", "striking", "Red", "Gold", "Silver", "Blue", "Marine", "Glowing", "Crimson", "Violet", "White", "Black", "Sinister");
    private static List<String> powers = Arrays.asList("tears", "darkness", "light", "fire", "gold", "luck", "cold", "wealth", "fury", "magic", "nature", "beasts");

    private String objectType;
    private String trait;
    private String power;
    private String uniqueName;

    public Artifact(String objectType, String trait, String power, String uniqueName)
    {
        this.objectType = objectType;
        this.trait = trait;
        this.power = power;
        this.uniqueName = uniqueName;
    }

    private static Artifact randomArtifact(Random random)
    {
        return randomArtifact(random, getRandomElementFrom(objectTypes, random));
    }

    private static Artifact randomArtifact(Random random, String objectType)
    {
        String trait = random.nextFloat() < 0.2f ? getRandomElementFrom(traits, random) : null;
        String power = random.nextFloat() < 0.8f ? getRandomElementFrom(powers, random) : null;
        String uniqueName = random.nextFloat() < 0.1f ? Person.chaoticName(random, random.nextBoolean()) : null;

        return new Artifact(objectType, trait, power, uniqueName);
    }

    private static <O> O getRandomElementFrom(List<O> list, Random random)
    {
        return list.get(random.nextInt(list.size()));
    }

    private static String firstCharUppercase(String name)
    {
        return Character.toString(name.charAt(0)).toUpperCase() + name.substring(1);
    }

    public String getObjectType()
    {
        return objectType;
    }

    public String getTrait()
    {
        return trait;
    }

    public String getPower()
    {
        return power;
    }

    public String getUniqueName()
    {
        return uniqueName;
    }

    public String getFullName()
    {
        StringBuilder builder = new StringBuilder();

        if (uniqueName != null)
        {
            builder.append(firstCharUppercase(trait)).append(", the ");
        }
        else
        {
            builder.append("the ");
        }

        if (trait != null)
        {
            builder.append(firstCharUppercase(trait)).append(' ');
        }

        builder.append(objectType);

        if (power != null)
        {
            builder.append(" of ").append(firstCharUppercase(power));
        }

        if (uniqueName != null)
        {
            builder.append(",");
        }

        return builder.toString();
    }
}
