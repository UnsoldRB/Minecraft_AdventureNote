package com.unsoldriceball.adventurenote;

import com.unsoldriceball.adventurenote.note_system.ANNoteBuilder;
import com.unsoldriceball.adventurenote.note_system.EnumANNoteType;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static com.unsoldriceball.adventurenote.ANMain.ID_MOD;

public class ANRecipes
{
    private final static Ingredient _R_BOOK = Ingredient.fromItem(Items.WRITABLE_BOOK);
    private final static Ingredient _R_LAPIS = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 4));




    //レシピの登録を行う関数。
    public static void registerRecipe(NonNullList<Ingredient> ingredients, EnumANNoteType type)
    {
        final IRecipe _RECIPE = new ShapelessRecipes(ID_MOD, ANNoteBuilder.createBlankNotes(type), ingredients);
        _RECIPE.setRegistryName(new ResourceLocation(ID_MOD, type.name().toLowerCase()));
        ForgeRegistries.RECIPES.register(_RECIPE);
    }



    //以下、レシピの定義を行う関数。
    //全てANMainから呼び出される。
    public static void recipe_NORMALENTITY()
    {
        NonNullList<Ingredient> _recipe_input = NonNullList.create();
        _recipe_input.add(_R_BOOK);
        _recipe_input.add(_R_LAPIS);
        _recipe_input.add(Ingredient.fromItem(Items.WOODEN_SWORD));
        registerRecipe(_recipe_input, EnumANNoteType.MOBS);
    }



    public static void recipe_BOSSENTITY()
    {
        NonNullList<Ingredient> _recipe_input = NonNullList.create();
        _recipe_input.add(_R_BOOK);
        _recipe_input.add(_R_LAPIS);
        _recipe_input.add(Ingredient.fromItem(Items.STONE_SWORD));
        _recipe_input.add(Ingredient.fromItem(Items.SHIELD));
        registerRecipe(_recipe_input, EnumANNoteType.BOSSES);
    }



    public static void recipe_BIOME()
    {
        NonNullList<Ingredient> _recipe_input = NonNullList.create();
        _recipe_input.add(_R_BOOK);
        _recipe_input.add(_R_LAPIS);
        _recipe_input.add(Ingredient.fromItem(Items.LEATHER_BOOTS));
        _recipe_input.add(Ingredient.fromItem(Items.BOAT));
        registerRecipe(_recipe_input, EnumANNoteType.BIOMES);
    }



    public static void recipe_DIMENSION()
    {
        NonNullList<Ingredient> _recipe_input = NonNullList.create();
        _recipe_input.add(_R_BOOK);
        _recipe_input.add(_R_LAPIS);
        _recipe_input.add(Ingredient.fromItem(Items.IRON_BOOTS));
        _recipe_input.add(Ingredient.fromItem(Items.ENDER_PEARL));
        registerRecipe(_recipe_input, EnumANNoteType.DIMENSIONS);
    }
}
