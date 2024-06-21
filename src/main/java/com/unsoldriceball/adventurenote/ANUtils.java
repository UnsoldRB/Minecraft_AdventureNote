package com.unsoldriceball.adventurenote;

import com.unsoldriceball.adventurenote.note_system.ANNoteBuilder;
import com.unsoldriceball.adventurenote.note_system.EnumANNoteType;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;




public class ANUtils
{
    //buildNoteAuthorName()で生成された著者名を利用して本を識別する。
    public static EnumANNoteType getNoteType(ItemStack item)
    {
        final String _AUTHORNAME_RAW = getNBT_Author(item);
        final String _AUTHORNAME_DISPLAY = ANConfig.c_Texts.name_author;
        final String _TAGVALUE_KEY = ANNoteBuilder.TAGVALUE_AUTHOR_KEY;

        if (_AUTHORNAME_RAW != null && _AUTHORNAME_RAW.contains(_TAGVALUE_KEY + _AUTHORNAME_DISPLAY))
        {
            final String[] _AUTHORNAME_RAW_SPLITTED = _AUTHORNAME_RAW.split(_AUTHORNAME_DISPLAY);
            final String _KEYS = _AUTHORNAME_RAW_SPLITTED[0];
            final int _LEN_TAGVALUE_KEY = _TAGVALUE_KEY.length();
            int _match_count = 0;

            //_KEYSに_TAGVALUE_KEYがいくつ含まれるかを数える。
            for (int __i = _LEN_TAGVALUE_KEY; __i <= _KEYS.length(); __i += _LEN_TAGVALUE_KEY)
            {
                if (_KEYS.substring(__i - _LEN_TAGVALUE_KEY, __i).equals(_TAGVALUE_KEY))
                {
                    _match_count++;
                }
                else
                {
                    break;
                }
            }

            final EnumANNoteType[] _ENUMS = EnumANNoteType.values();
            if (1 <= _match_count && _match_count <= _ENUMS.length)
            {
                return _ENUMS[_match_count - 1];
            }
        }
        return null;
    }



    //指定した本に記載されているprogressと、指定したprogressが等しいかを調べる。
    public static boolean isNotEqualNBT_progress(ItemStack i, int progress)
    {
        if (isWrittenBook(i))
        {
            final String _AUTHORNAME_RAW = getNBT_Author(i);
            final String _AUTHORNAME_DISPLAY = ANConfig.c_Texts.name_author;

            if (_AUTHORNAME_RAW != null && _AUTHORNAME_RAW.contains(ANNoteBuilder.TAGVALUE_AUTHOR_KEY + _AUTHORNAME_DISPLAY) && _AUTHORNAME_RAW.contains(ANNoteBuilder.TAGVALUE_PREFIX_PROGRESS))
            {
                final String[] _AUTHORNAME_RAW_SPLITTED = _AUTHORNAME_RAW.split(ANNoteBuilder.TAGVALUE_PREFIX_PROGRESS);

                return progress != Integer.parseInt(_AUTHORNAME_RAW_SPLITTED[1]);
            }
        }
        return true;
    }



    public static UUID getUUID_fromAuthor(ItemStack i)
    {
        final String _AUTHORNAME_RAW = getNBT_Author(i);
        final String _AUTHORNAME_DISPLAY = ANConfig.c_Texts.name_author;

        if (_AUTHORNAME_RAW != null && _AUTHORNAME_RAW.contains(ANNoteBuilder.TAGVALUE_AUTHOR_KEY + _AUTHORNAME_DISPLAY))
        {
            final String[] _AUTHORNAME_RAW_SPLITTED = _AUTHORNAME_RAW.split(_AUTHORNAME_DISPLAY);

            if (_AUTHORNAME_RAW_SPLITTED.length == 2)
            {
                String _uuid_string = _AUTHORNAME_RAW_SPLITTED[1].split(ANNoteBuilder.TAGVALUE_SUFFIX_UUID)[0];
                _uuid_string = _uuid_string.replace(ANNoteBuilder.TAGVALUE_PREFIX_UUID, "");
                try
                {
                    return UUID.fromString(_uuid_string);
                }
                catch (Exception exc)
                {
                    return null;
                }

            }
            else
            {
                //本をクラフト、あるいは新規で入手した場合にはnullを返す。(_AUTHORNAME_RAWに持ち主のUUIDがまだ存在していない。)
                return null;
            }
        }
        return null;
    }



    //著名済みの本から著者名を取得する。
    public static String getNBT_Author(ItemStack i)
    {
        final String _TAGNAME_AUTHOR = ANNoteBuilder.TAGNAME_AUTHOR;
        final NBTTagCompound _NBT_ITEM = i.getTagCompound();
        final int _INT_DATATYPE = 8;

        if (isWrittenBook(i) && _NBT_ITEM != null && _NBT_ITEM.hasKey(_TAGNAME_AUTHOR, _INT_DATATYPE))
        {
            return _NBT_ITEM.getString(_TAGNAME_AUTHOR);
        }
        return null;
    }




    //entityの名前がlangファイルで設定されているかを調べる。
    public static boolean hasName(Entity entity)
    {
        final String _NAME = entity.getName();
        final String _VERIFICATION_TEXT = ".name";  //名前の最後の5文字がこれなら、名前が設定されていない。

        return !_NAME.endsWith(_VERIFICATION_TEXT);
    }



    //対象が著名済みの本かどうかを返す関数。
    public static boolean isWrittenBook(ItemStack i)
    {
        return i.getItem().equals(Items.WRITTEN_BOOK);
    }
}
