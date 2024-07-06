package com.unsoldriceball.adventurenote.note_system;

import com.unsoldriceball.adventurenote.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.*;




public class ANNoteBuilder
{
    private static final String SECTION = "\u00A7";
    public static final String TAGNAME_TITLE = "title";
    public static final String TAGNAME_AUTHOR = "author";
    public static final String TAGNAME_CONTENTS = "pages";
    //本の著者名の前にこれをEnumANNoteTypeのordinalの数だけ追加することで、本を識別する。
    public static final String TAGVALUE_AUTHOR_KEY = SECTION + "l" + SECTION + "e" + SECTION + "r" + SECTION + "o" + SECTION + "r";
    public static final String TAGVALUE_PREFIX_UUID = SECTION + "0_UU";
    public static final String TAGVALUE_SUFFIX_UUID = "ID_";
    public static final String TAGVALUE_PREFIX_PROGRESS = "#";
    private static final String STRING_NEWLINE = "\n";
    private static final String STRING_SPLITTER = "/";
    private static final String STRING_PERCENTAGE = "%";
    private static final String TEMPSTRING_JUMPPAGE_NUM = "%JUMPPAGE_NUMBER%";  //AdventureNote内で、文字のクリック時のページの移動先を設定するために、一時的に設置される文字列。最終的に数値で置き換えられる。また、mod名とセットで使用される。
    private static final int MAXLINE_INPAGE = 14;
    private static final int FIRSTPAGE_CONTENTS_TABLE = 2;     //目次の最初のページ。




    //新規AdventureNoteを作成する関数。
    public static ItemStack createBlankNotes(EnumANNoteType type)
    {
        final ItemStack _note_temp = new ItemStack(Items.WRITTEN_BOOK);
        final NBTTagCompound _tag = new NBTTagCompound();

        _tag.setString(TAGNAME_TITLE, buildNoteTitle(type, null));
        _tag.setString(TAGNAME_AUTHOR, buildNoteAuthorName(type, null));

        _note_temp.setTagCompound(_tag);
        return _note_temp;
    }



    //AdventureNoteのItemStackを作成する関数。
    public static ItemStack createNote(EnumANNoteType type, EntityPlayer p)
    {
        final ItemStack _note_temp = new ItemStack(Items.WRITTEN_BOOK);
        final NBTTagCompound _tag = new NBTTagCompound();

        _tag.setString(TAGNAME_TITLE, buildNoteTitle(type, p));
        _tag.setString(TAGNAME_AUTHOR, buildNoteAuthorName(type, p));
        _tag.setTag(TAGNAME_CONTENTS, createPages(type, p.getUniqueID()));

        _note_temp.setTagCompound(_tag);
        return _note_temp;
    }




    public static NBTTagList createPages(EnumANNoteType type, UUID uuid_p)
    {
        NBTTagList _pages = new NBTTagList();

        List<String> _contents_advnote = buildPages(type, uuid_p);
        for (String __s : _contents_advnote)
        {
            _pages.appendTag(new NBTTagString(__s));
        }
        return _pages;
    }



    //実際に進捗状況を表示するページを構築する。
    //内容->目次->最初のページの順で作成される。
    private static List<String> buildPages(EnumANNoteType type, UUID uuid_p)
    {
        final String _DOUBLE_NEW_LINE = STRING_NEWLINE + STRING_NEWLINE;
        final String _TEMPTEXT_PAGE = "#%#PAGE#%#";     //後の処理で各modのページ数に置換される。
        final String _TEMPTEXT_PAGE_PREFIX = ANConfig.c_Texts.prefixInNote_modPageIndex + "(";
        final String _TEMPTEXT_PAGE_SUFFIX = ")";
        final String _TEMPTEXT_PAGE_BUILT = _TEMPTEXT_PAGE_PREFIX + _TEMPTEXT_PAGE + _TEMPTEXT_PAGE_SUFFIX;
        final String _TEMPTEXT_SPLIT_MODNAME_AND_ELEMENTS = "%SPLIT%";      //この文字列でsplit()を実行する。
        final String _TEMPTEXT_LEACH_LENCAP = "%LEACH_LENCAP%";     //対象としたテキストがconfigで設定されている文字数制限を超えていることをシステムに対して示すために使う。
        final Map<Integer, String> _DATA_REGISTRY = getSorted_f_registered_data(type);
        final ArrayList<Integer> _DATA_REGISTRY_KEYS = new ArrayList<>(_DATA_REGISTRY.keySet());
        final ArrayList<Integer> _DATA_PLAYER = ANJsonEditor.convertIDtoClassName(ANJsonEditor.getValue_Json(uuid_p, type.name()), type);
        final ArrayList<String> _CONTENTS = new ArrayList<>();      //1つのindexに1つのページデータが格納される。
        final Map<String, int[]> _MODS = new LinkedHashMap<>();    //modが最初に登場したページ番号と、要したページの枚数、要素の数を保存していく。
        final int _MODS_INDEX_FIRSTPAGE = 0;
        final int _MODS_INDEX_ELEMENTS_AMOUNT = 1;
        final int _MODS_INDEX_UNLOCKEDELEMENTS_AMOUNT = 2;
        final int _MODS_ARRAYSIZE = 3;

        StringBuilder _str_builder = new StringBuilder();
        int _line_now = 1;
        String _name_mod_nowtarget = "";

        //ANDataCollector.f_registered_datas.get(type)内の全のKeyをループする。ここにはclassのcanonicalNameか、DimensionIDが格納されている。
        for (Integer __hash : _DATA_REGISTRY_KEYS)
        {
            //現在作業している行が、1ページの最大行数を超えていた場合(ページを追加する。)。
            if (_line_now > MAXLINE_INPAGE)
            {
                _CONTENTS.add(convertContentsPage_toJson(_str_builder, _name_mod_nowtarget, _TEMPTEXT_SPLIT_MODNAME_AND_ELEMENTS));
                _str_builder = new StringBuilder();
                _line_now = 1;
            }

            //__sのmod名を取得する(dimensionにmod名は無い。)。
            String __name_mod_thisloop = dataName_To_DisplayName(getModName_FromID(_DATA_REGISTRY.get(__hash)), true, _TEMPTEXT_LEACH_LENCAP);
            if (__name_mod_thisloop.endsWith(_TEMPTEXT_LEACH_LENCAP))    //configで設定されており、文字数制限に達している印が含まれている場合。
            {
                //このifが成立する場合、改行が発生するが、
                //mod名の下は常に1行空いているので_line_now++は行わなくても見た目上問題ない。(この文字数制限処理自体が見た目のための処理なのでこれで良い。)
                __name_mod_thisloop = __name_mod_thisloop.replace(_TEMPTEXT_LEACH_LENCAP, "");
            }

            //mod名が存在した場合。
            if (!__name_mod_thisloop.isEmpty())
            {
                //一番初めのループだった場合。あるいは、
                //「現在の__sのmodと、ひとつ前の__sのmod」が一致していない場合。(あるいはひとつ前の__sのmodが存在しない。)
                if (_name_mod_nowtarget.isEmpty() || !__name_mod_thisloop.equals(_name_mod_nowtarget))
                {
                    //「作業している行が1行目でない」場合。(要はそのmodの要素が、MAXLINE_INPAGEとぴったりではない行の段階でなくなった場合。)
                    if (_line_now != 1)
                    {
                        _CONTENTS.add(convertContentsPage_toJson(_str_builder, _name_mod_nowtarget, _TEMPTEXT_SPLIT_MODNAME_AND_ELEMENTS));
                        _str_builder = new StringBuilder();
                        _line_now = 1;
                    }

                    //作業の対象となるmodを新しいmodに切り替える。
                    _name_mod_nowtarget = __name_mod_thisloop;

                    int[] __mods_data = new int[_MODS_ARRAYSIZE];
                    __mods_data[_MODS_INDEX_FIRSTPAGE] = _CONTENTS.size() + 1;       //_CONTENTS.size()の開始は0から。ページ数は1から開始したいので+1をする。
                    _MODS.put(__name_mod_thisloop, __mods_data);     //ここで今後_modname_nowに使用する配列をputしておく。(そうすれば今後は配列を読み取ってputするだけでよくなる。)
                }
                //作業している行が1行目の場合。
                if (_line_now == 1)
                {
                    //一行目にmod名を挿入する。
                    _str_builder.append(ANConfig.c_Texts.prefixInNote_modName);
                    _str_builder.append(__name_mod_thisloop);
                    _str_builder.append(_TEMPTEXT_PAGE_BUILT);
                    _str_builder.append(_TEMPTEXT_SPLIT_MODNAME_AND_ELEMENTS);
                    _str_builder.append(_DOUBLE_NEW_LINE);
                    _line_now += 2;
                }
                _MODS.get(_name_mod_nowtarget)[_MODS_INDEX_ELEMENTS_AMOUNT]++;
                //プレイヤーが__sをアンロック済みだった場合(個数を数えておく。)。
                if (_DATA_PLAYER.contains(__hash))
                {
                    _MODS.get(_name_mod_nowtarget)[_MODS_INDEX_UNLOCKEDELEMENTS_AMOUNT]++;
                }
            }
            //プレイヤーが__sをアンロック済みだった場合とそうでない場合(文字を装飾する。)。
            if (_DATA_PLAYER.contains(__hash))
            {
                _str_builder.append(ANConfig.c_Texts.prefixInNote_element_unlocked);
            }
            else
            {
                _str_builder.append(ANConfig.c_Texts.prefixInNote_element_locked);
            }

            String __name_element = getDisplayName_fromData(type, __hash, _TEMPTEXT_LEACH_LENCAP); //__sを実際にプレイヤーに表示する形式にしたもの。elementの名前の由来は、modに含まれる要素であることから。
            if (__name_element.contains(_TEMPTEXT_LEACH_LENCAP))    //configで設定されており、文字数制限に達している印が含まれている場合。
            {
                __name_element = __name_element.replace(_TEMPTEXT_LEACH_LENCAP, "");
                //このifが成立する場合は、改行が発生するので、作業している行を1つ進めることで、このループの要素に対して計2行を割り当てる。
                _line_now++;
            }
            //configで隠すように設定されている場合は、解除していない要素名を隠す。
            if (ANConfig.c_Systems.hideLockedName && !_DATA_PLAYER.contains(__hash))
            {
                __name_element = __name_element.replaceAll("\\S", ANConfig.c_Texts.string_hideLockedName);
            }
            _str_builder.append(__name_element);
            _str_builder.append(STRING_NEWLINE);

            //最後のループだった場合は、MAXLINE_INPAGEまで行が達していなくてもその時点でページとして配列に追加する。
            if (_DATA_REGISTRY_KEYS.get(_DATA_REGISTRY_KEYS.size() - 1).equals(__hash))
            {
                _CONTENTS.add(convertContentsPage_toJson(_str_builder, _name_mod_nowtarget, _TEMPTEXT_SPLIT_MODNAME_AND_ELEMENTS));
                //ここでforの処理終了。
            }
            else
            {
                _line_now++;
            }
        }

        //dimensionにはmod名が含まれないので、処理が異なる。(ここで処理終了。)
        if (type == EnumANNoteType.DIMENSIONS)
        {
            final List<String> _FIRSTPAGE = buildFirstPage(type, _DATA_REGISTRY.size(), _DATA_PLAYER.size());
            _FIRSTPAGE.addAll(_CONTENTS);
            return _FIRSTPAGE;
        }
        else
        {
            //目次と最初のページを作成。目次は、indexの0に各modが何ページ目なのかを記録したList、1に実際の目次データを格納している。
            final List<String> _FIRSTPAGE = buildFirstPage(type, _DATA_REGISTRY.size(), _DATA_PLAYER.size());
            final ArrayList<ArrayList<String>> _CONTENT_TABLE =  buildContentsTable(_MODS, _MODS_INDEX_FIRSTPAGE, _MODS_INDEX_ELEMENTS_AMOUNT, _MODS_INDEX_UNLOCKEDELEMENTS_AMOUNT);

            //_TEMPTEXT_PAGEと、_TEMPTEXT_JUMPPAGE_NUMを置換してく。
            final List<String> _FINALLY_CONTENTS = new ArrayList<>();
            final List<String> _NAMES_MOD = new ArrayList<>(_MODS.keySet());    //各modの名前が格納されている。
            final List<String> _MODSPOS_IN_CONTENTSTABLE = new ArrayList<>(_CONTENT_TABLE.get(0));  //何ページ目に各modの目次があるか。_NAMES_MODとindexが対応。
            final Map<String, Integer> _MOD_PAGE_AMOUNT = new LinkedHashMap<>();      //各modの最大ページ数が格納される。
            final String _TEMPTEXT_MAXPAGE = "%MAXPAGE%";

            int _mod_count = 0;         //処理したmodの個数を数える。
            int _mod_page_count = 1;    //同じmodが何回続いているかを記録する。
            StringBuilder _str_builder2 = new StringBuilder();     //一応宣言を分ける。名前の数字に意味はない。
            String _modname_now2 = _NAMES_MOD.get(_mod_count);      //一応宣言を分ける。名前の数字に意味はない。
            String _key_mod_pageamount = _TEMPTEXT_MAXPAGE + _modname_now2 + _TEMPTEXT_MAXPAGE;     //この後の処理で各modの最大ページ数に置換される。

            for (String __s : _CONTENTS)
            {
                //__s(ループ中のページ)にmod名+_TEMPTEXT_PAGE_BUILTが含まれなかった場合。(ページごとに必ずmod名とそれに連なる仮のページ数表記が含まれる。)
                if (!__s.contains(_modname_now2 + _TEMPTEXT_PAGE_BUILT))
                {
                    _mod_count++;
                    _mod_page_count = 1;
                    _modname_now2 = _NAMES_MOD.get(_mod_count);
                    _key_mod_pageamount = _TEMPTEXT_MAXPAGE + _modname_now2 + _TEMPTEXT_MAXPAGE;
                }
                //_TEMPTEXT_PAGE用の処理。
                _str_builder2.append(_mod_page_count);
                _str_builder2.append(STRING_SPLITTER);
                _str_builder2.append(_key_mod_pageamount);

                //_TEMPTEXT_JUMPPAGE_NUM用の処理。
                //目次の何ページ目に_modname_now2があるのか調べる。
                final String __MODPOS_IN_CONTENTSTABLE = _MODSPOS_IN_CONTENTSTABLE.get(_mod_count);

                //置換していく。
                String __replaced_string = __s.replace(_TEMPTEXT_PAGE, _str_builder2);
                __replaced_string = __replaced_string.replace(TEMPSTRING_JUMPPAGE_NUM + _modname_now2, __MODPOS_IN_CONTENTSTABLE);

                _FINALLY_CONTENTS.add(__replaced_string);

                _str_builder2 = new StringBuilder();
                _mod_page_count++;

                //始まりの数字を0にするために-1をする。
                _MOD_PAGE_AMOUNT.put(_key_mod_pageamount, _mod_page_count - 1);
            }

            //だんだんコード書くのがめんどくさくなってきた。ごり押しで書いてる。もっと賢いやり方が頭に浮かんでるけどもうめんどくさい。
            //最大ページ数をreplace()で書き込んでいく。
            final ArrayList<String> _KEYS_MODPAGEAMOUNT = new ArrayList<>(_MOD_PAGE_AMOUNT.keySet());   //mod名が入っている。
            int _loop_count = 0;
            int _mod_count3 = 0; //一応宣言を分ける。名前の数字に意味はない。
            String _key_now = _KEYS_MODPAGEAMOUNT.get(_mod_count3);

            for (String __s : _FINALLY_CONTENTS)
            {
                //_TEMPTEXT_MAXPAGE+現在作業しているmod名+_TEMPTEXT_MAXPAGEが現在のページに含まれなかった場合。
                if (!__s.contains(_key_now))
                {
                    _mod_count3++;
                    _key_now = _KEYS_MODPAGEAMOUNT.get(_mod_count3);
                }
                final String __STRING_REPLACE = __s.replace(_key_now, String.valueOf(_MOD_PAGE_AMOUNT.get(_key_now)));
                _FINALLY_CONTENTS.set(_loop_count, __STRING_REPLACE);
                _loop_count++;
            }

            //最初のページ、目次、メインページを結合していく。
            _CONTENT_TABLE.get(1).addAll(_FINALLY_CONTENTS);
            _FIRSTPAGE.addAll(_CONTENT_TABLE.get(1));

            return _FIRSTPAGE;
        }
    }



    //目次を作成する関数。
    //戻り値のindex0に_MOD_POS, 1に_RESULTが入る。
    private static ArrayList<ArrayList<String>> buildContentsTable(Map<String, int[]> mods, int mods_index_firstpage, int mods_index_elementsamount, int mods_index_unlockedelementsamount)
    {
        final String _STRING_PREFIX_PROGRESS = "(";
        final String _STRING_SUFFIX_PROGRESS = ")";
        final ArrayList<String> _NAMES_MOD = new ArrayList<>(mods.keySet());

        final ArrayList<String> _MOD_POS = new ArrayList<>(); //各modの目次が何ページ目にあるかを記録する。indexを_NAMES_MODと対応させる。
        final ArrayList<String> _RESULT = new ArrayList<>();
        StringBuilder _str_builder = new StringBuilder();
        StringBuilder _str_builder_perline = new StringBuilder();
        int _line_now = 1;

        for(String __s : _NAMES_MOD)
        {
            _str_builder_perline.append(__s);

            //進捗情報を後ろに付け加える。
            final int __AMOUNT_ELEMENT = mods.get(__s)[mods_index_elementsamount];
            final int __AMOUNT_UNLOCKEDELEMENT = mods.get(__s)[mods_index_unlockedelementsamount];

            //modの要素をすべて解除しているかどうかで文字の色を変える。
            if (__AMOUNT_ELEMENT == __AMOUNT_UNLOCKEDELEMENT)
            {
                _str_builder_perline.append(ANConfig.c_Texts.prefixInNote_modProgress_completed);
            }
            else
            {
                _str_builder_perline.append(ANConfig.c_Texts.prefixInNote_modProgress_notCompleted);
            }
            _str_builder_perline.append(_STRING_PREFIX_PROGRESS);
            if (ANConfig.c_Systems.progress_usePercentage)
            {
                float __percentage = (float) __AMOUNT_UNLOCKEDELEMENT / __AMOUNT_ELEMENT;
                //小数点第2まで残したい。
                __percentage = Math.round(__percentage * 100.0f) / 100.0f;
                _str_builder_perline.append(__percentage);
                _str_builder_perline.append(STRING_PERCENTAGE);
            }
            else
            {
                _str_builder_perline.append(__AMOUNT_UNLOCKEDELEMENT);
                _str_builder_perline.append(STRING_SPLITTER);
                _str_builder_perline.append(__AMOUNT_ELEMENT);
            }
            _str_builder_perline.append(_STRING_SUFFIX_PROGRESS);

            //クリックでmodの最初のページに飛べるようにするために、後で置換するための文字列(_TEMP_JUMPPAGE_NUM+mod名)を一旦入れておく。
            _str_builder_perline = new StringBuilder(toJson_BookText(String.valueOf(_str_builder_perline), TEMPSTRING_JUMPPAGE_NUM + __s));

            //メインページから飛んでこれるようにするため(_TEMP_JUMPPAGE_NUMを置換するため)に、このmodが何ページ目かを記録する。
            _MOD_POS.add(String.valueOf(FIRSTPAGE_CONTENTS_TABLE + _RESULT.size()));

            //最後のループだった場合。
            if (_NAMES_MOD.get(_NAMES_MOD.size() - 1).equals(__s))
            {
                _str_builder.append(_str_builder_perline);
                _RESULT.add("[" + _str_builder + "]");
            }
            //そうでなかった場合は、作業する行を1つ進め、ページの最大行数に達した場合はページを追加する。
            else
            {
                _line_now++;
                _str_builder.append(_str_builder_perline);
                _str_builder_perline = new StringBuilder();
                if (_line_now > MAXLINE_INPAGE)
                {
                    _RESULT.add("[" + _str_builder + "]");
                    _str_builder = new StringBuilder();
                    _line_now = 1;
                }
                else
                {
                    _str_builder.append(",");   //Jsonの{}同士を繋ぐ。
                    _str_builder_perline.append(STRING_NEWLINE);
                }
            }
        }

        final int _RESULT_SIZE = _RESULT.size();    //目次の総ページ数。

        //_TEMP_JUMPPAGE_NUMを置換していく。
        for (String __s2 : _NAMES_MOD)
        {
            final String __TARGET = TEMPSTRING_JUMPPAGE_NUM + __s2;

            int __i = 0;    //下の処理で、_RESULTのループ回数が入る。
            for (String ___result : _RESULT)
            {
                if (___result.contains(__TARGET))
                {
                    final String __MODPAGE = String.valueOf(mods.get(__s2)[mods_index_firstpage] + _RESULT_SIZE + 1);  //AdventureNoteには最初のページが存在するので+1。
                    _RESULT.set(__i, ___result.replace(__TARGET, __MODPAGE));
                    break;
                }
                __i++;
            }
        }
        final ArrayList<ArrayList<String>> _RESULT2 = new ArrayList<>();
        _RESULT2.add(_MOD_POS);
        _RESULT2.add(_RESULT);
        return _RESULT2;
    }



    private static List<String> buildFirstPage(EnumANNoteType type, int data_size_all, int data_size_player)
    {
        final StringBuilder _STR_BUILDER = new StringBuilder();
        _STR_BUILDER.append(STRING_NEWLINE);
        _STR_BUILDER.append(readConfig_firstpageTitle(type));
        _STR_BUILDER.append(STRING_NEWLINE);
        _STR_BUILDER.append(STRING_NEWLINE);
        //全ての要素を解放済みの場合は色を変える。
        if (data_size_player == data_size_all)
        {
            _STR_BUILDER.append(ANConfig.c_Texts.prefixInNote_modProgress_completed);
        }
        else
        {
            _STR_BUILDER.append(ANConfig.c_Texts.prefixInNote_modProgress_notCompleted);
        }

        if (ANConfig.c_Systems.progress_usePercentage)
        {
            float _percentage = (float) data_size_player / data_size_all;
            //小数点第2まで残したい。
            _percentage = Math.round(_percentage * 100.0f) / 100.0f;
            _STR_BUILDER.append(_percentage);
            _STR_BUILDER.append(STRING_PERCENTAGE);
        }
        else
        {
            _STR_BUILDER.append(data_size_player);
            _STR_BUILDER.append(STRING_SPLITTER);
            _STR_BUILDER.append(data_size_all);
        }
        return new ArrayList<>(Collections.singletonList(toJson_BookText(String.valueOf(_STR_BUILDER), null)));
    }



    private static String readConfig_firstpageTitle(EnumANNoteType type)
    {
        switch (type)
        {
            case MOBS:
                return ANConfig.c_Texts.noteFirstPageTitle_Mob;
            case BOSSES:
                return ANConfig.c_Texts.noteFirstPageTitle_Boss;
            case BIOMES:
                return ANConfig.c_Texts.noteFirstPageTitle_Biome;
            case DIMENSIONS:
                return ANConfig.c_Texts.noteFirstPageTitle_Dimension;
        }
        return "";
    }



    private static String toJson_BookText(String s, String change_page)
    {
        final StringBuilder _STR_BUILDER = new StringBuilder();
        _STR_BUILDER.append("{\"text\":\"");
        _STR_BUILDER.append(s);
        _STR_BUILDER.append("\"");
        //change_pageが設定されている場合は、クリック時に指定のページに飛ばすようにする。(nullで無効。)
        if (change_page != null)
        {
            _STR_BUILDER.insert(0, "\"\",");    //なぜかこの文字列が必要。Minecraftが、jsonの配列の開始を認識できるようになるとかならないとか。
            _STR_BUILDER.append(",\"clickEvent\":{\"action\":\"change_page\",\"value\":");
            _STR_BUILDER.append(change_page);
            _STR_BUILDER.append("}");

            //さらにconfigが有効な場合には行先のページ数を表示する。
            if (ANConfig.c_Systems.showHoverText_pageTo)
            {
                _STR_BUILDER.append(",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"");
                _STR_BUILDER.append(SECTION);
                _STR_BUILDER.append("7-> p.");
                _STR_BUILDER.append(change_page);
                _STR_BUILDER.append("\"}");
            }
        }
        _STR_BUILDER.append("}");
        return String.valueOf(_STR_BUILDER);
    }



    //_CONTENTSに追加する予定のStringBuilderを、必要に応じてmod名をクリックで目次に飛べるようにした上で、Json化する関数。
    //実際にクリックで飛べるようになるのはtemptext_jumppage_numを後々の処理で置換してから。
    private static String convertContentsPage_toJson(StringBuilder target, String modname, String temptext_split)
    {
        final String _TARGET = String.valueOf(target);

        //mod名が含まれているかどうか(dimensionなどの場合は含まれない。)
        if (modname.isEmpty())
        {
            return toJson_BookText(_TARGET, null);
        }
        else
        {
            final String[] _TARGET_SPLITTED = _TARGET.split(temptext_split);
            final String _MODNAME = toJson_BookText(_TARGET_SPLITTED[0], TEMPSTRING_JUMPPAGE_NUM + modname);
            final String _ELEMENTS = toJson_BookText(_TARGET_SPLITTED[1], null);

            return "[" + _MODNAME + "," + _ELEMENTS + "]"; //","でJsonの{}同士を繋ぐ。
        }
    }



    //Noteのタイトルを返す関数。
    private static String buildNoteTitle(EnumANNoteType type, EntityPlayer p)
    {
        String _name = "";
        switch (type)
        {
            case MOBS:
                _name = ANConfig.c_Texts.noteName_Mob;
                break;
            case BOSSES:
                _name = ANConfig.c_Texts.noteName_Boss;
                break;
            case BIOMES:
                _name = ANConfig.c_Texts.noteName_Biome;
                break;
            case DIMENSIONS:
                _name = ANConfig.c_Texts.noteName_Dimension;
        }
        if (p != null)
        {
            _name = _name.replaceAll("%player%", p.getDisplayNameString());
        }
        else
        {
            //createBlankNoteではこちらの処理が実行される。
            _name = _name.replaceAll("%player%", "someone");
        }
        return _name;
    }



    //Noteの著者名を生成する関数。
    //_TAGVALUE_AUTHOR_KEYを名前の前に入れることで本を識別できるようにする。
    //また、最後にプレイヤーのデータの総数を記入することで、本の状態が最新かどうかの比較を容易にする。
    private static String buildNoteAuthorName(EnumANNoteType type, EntityPlayer p)
    {
        final int _LOOP_COUNT = type.ordinal();
        StringBuilder result = new StringBuilder();

        for(int i = 0; i <= _LOOP_COUNT; i++)
        {
            result.append(ANNoteBuilder.TAGVALUE_AUTHOR_KEY);
        }

        result.append(ANConfig.c_Texts.name_author);
        if (p != null)
        {
            //createBlankNote()では以下の処理を実行しない。
            result.append(TAGVALUE_PREFIX_UUID).append(p.getUniqueID()).append(TAGVALUE_SUFFIX_UUID);

            if (ANMain.UPDATER_PER_PLAYER.containsKey(p.getUniqueID()))
            {
                final ANNoteUpdater _UPDATER = ANMain.UPDATER_PER_PLAYER.get(p.getUniqueID());
                result.append(TAGVALUE_PREFIX_PROGRESS).append(_UPDATER.note_progress.get(type));
            }
        }
        return String.valueOf(result);
    }



    //className、あるいはdimensionIDから、表示名を取得する関数。
    private static String getDisplayName_fromData(EnumANNoteType type, Integer hash, String temptext_leach_lencap)
    {
        String _target = "";
        switch (type)
        {
            case MOBS:
            case BOSSES:
                _target = ANDataCollector.f_entities_name.get(hash);
                break;
            case BIOMES:
                _target = ANDataCollector.f_biomes_instance.get(hash).getBiomeName();
                break;
            case DIMENSIONS:
                //dimensionだけ名前の取得方法がそもそも他と異なる。
                final String _KEY = ANDataCollector.f_registered_datas.get(type).get(hash);

                if (_KEY != null)
                {
                    _target = dataName_To_DisplayName(_KEY, false, temptext_leach_lencap);
                }
                break;
        }
        return applyLenCap(_target, false, temptext_leach_lencap);
    }



    //各種IDを、実際の表示名「みたいに」する関数。
    private static String dataName_To_DisplayName(String s, boolean is_modname, String temptext_leach_lencap)
    {
        final int _LEN_STRING = s.length();
        StringBuilder _string_target = new StringBuilder(s);
        //アンダーバーを検出したときにtrueになり、次の文字を大文字にする。
        boolean space_detected = false;

        for(int i = 0; i <= _LEN_STRING - 1; i++)
        {
            final String _CHAR_NOW_TARGET = String.valueOf(_string_target.charAt(i));

            //一文字目は大文字にする。また、spaceDetectedがtrueでも大文字にする。
            if (i == 0 || space_detected)
            {
                _string_target.replace(i, i + 1, _CHAR_NOW_TARGET.toUpperCase());
                space_detected = false;
            }
            //アンダーバーを検知したら、スペースに置換してspaceDetectedをtrueにする。
            if (_CHAR_NOW_TARGET.equals("_"))
            {
                _string_target.replace(i, i + 1, " ");
                space_detected = true;
            }
        }

        return applyLenCap(String.valueOf(_string_target), is_modname, temptext_leach_lencap);
    }


    //configに応じて文字数制限の処理を行う関数。
    private static String applyLenCap(String target, boolean is_modname, String temptext_leach_lencap)
    {
        String _target = target;
        final int _LENGTH_CAP;

        //内容物が英数字だった場合。
        if (_target.matches("[a-zA-Z_0-9]+"))
        {
            _LENGTH_CAP = is_modname ? ANConfig.c_Systems.modName_lengthCap_english : ANConfig.c_Systems.elementName_lengthCap_english;
        }
        else
        {
            _LENGTH_CAP = is_modname ? ANConfig.c_Systems.modName_lengthCap_other : ANConfig.c_Systems.elementName_lengthCap_other;
        }

        if (_target.length() > _LENGTH_CAP)
        {
            //空に設定されていた場合は改行で対応する。
            if (ANConfig.c_Texts.string_lengthCap_omit.isEmpty())
            {
                _target = _target + temptext_leach_lencap;
            }
            else
            {
                _target = _target.substring(0, _LENGTH_CAP) + ANConfig.c_Texts.string_lengthCap_omit;
            }
        }

        return _target;
    }



    //IDからmod名を返す簡単な関数。
    private static String getModName_FromID(String s)
    {
        final String _TARGET = ":"; //この文字でmod名と表示名が分割されている。

        if (s.contains(_TARGET))
        {
            return s.split(_TARGET)[0];
        }
        else
        {
            //dimensionなどはこちらの処理が実行される。
            return "";
        }
    }



    //valueの名前順でANDataCollector.f_registered_datas.get(type)のデータをソートする。
    private static Map<Integer, String> getSorted_f_registered_data(EnumANNoteType type)
    {
        final Map<Integer, String> _DATA = new HashMap<>(ANDataCollector.f_registered_datas.get(type));
        final ArrayList<Integer> _KEYS = new ArrayList<>(_DATA.keySet());
        final ArrayList<String> _VALUES = new ArrayList<>(_DATA.values());
        final ArrayList<String> _VALUES_SORTED = new ArrayList<>(_DATA.values());
        Collections.sort(_VALUES_SORTED);

        final ArrayList<Integer> _SORTED_INDEX = new ArrayList<>();
        final Map<Integer, String> _SORTED_DATA = new LinkedHashMap<>();


        for (String __s : _VALUES_SORTED)
        {
            _SORTED_INDEX.add(_VALUES.indexOf(__s));
        }

        for (Integer __i : _SORTED_INDEX)
        {
            _SORTED_DATA.put(_KEYS.get(__i), _VALUES.get(__i));
        }

        return _SORTED_DATA;
    }
}
