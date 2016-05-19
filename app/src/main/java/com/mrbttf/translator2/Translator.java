package com.mrbttf.translator2;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by MrBTTF on 19.05.2016.
 */
public class Translator
{
    private HashMap<String,String> engFraMap;
    private HashMap<String,String> fraEngMap;

    Context context;

    public Translator(Context context)
    {
        this.context = context;
        engFraMap = new HashMap<>();
        fraEngMap = new HashMap<>();
    }

    public void loadDict()
    {
        AssetManager am = context.getAssets();
        InputStream is;
        try
        {
            is = am.open("dict.txt");

            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);

            String line1;
            String line2;
            while ((line1 = br.readLine()) != null)
            {
                line2 = br.readLine();
                fraEngMap.put(line1,line2);
                engFraMap.put(line2,line1);
            }

            br.close();
            isr.close();
            is.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String translateText(String text, boolean engFra)
    {
        HashMap<String,String> curMap;
        if(engFra)
            curMap=engFraMap;
        else
            curMap=fraEngMap;

        if(text.isEmpty()) return "";

        String[] words = text.split(" ");

        StringBuilder translation = new StringBuilder();
        for (String word : words)
        {

            String p="";
            if(isPunctuation(word.charAt(word.length()-1)))
            {
                p= String.valueOf(word.charAt(word.length()-1));
                word =word.substring(0, word.length() -1);
            }

            if(!curMap.containsKey(word.toLowerCase()))
            {
                translation.append(word + p + " ");
                continue;
            }

            StringBuilder trword = new StringBuilder(curMap.get(word.toLowerCase()).toLowerCase());
            if(Character.isUpperCase(word.charAt(0)))
            {
                trword.setCharAt(0,Character.toUpperCase(trword.charAt(0)));
            }

            translation.append(trword + p + " ");
        }


        return translation.substring(0, translation.length() - 1);
    }

    private boolean isPunctuation(char c) {
        return c == ','
                || c == '.'
                || c == '!'
                || c == '?'
                || c == ':'
                || c == ';' ;
    }
}
