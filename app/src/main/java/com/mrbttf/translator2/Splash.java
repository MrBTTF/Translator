package com.mrbttf.translator2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

/**
 * Created by MrBTTF on 19.05.2016.
 */
public class Splash extends Activity
{

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);

        LoadingData loadingData = new LoadingData(this);
        loadingData.execute();
    }

    private class LoadingData extends AsyncTask<Void, Void, Boolean>
    {
        Context context;

        public LoadingData(Context context)
        {
            this.context=context;
        }


        @Override
        protected Boolean doInBackground(Void... params)
        {
            try {
                Translator.loadDict(context);
            } catch (IOException e) {
                Log.d(MainActivity.G_LOG,e.getMessage());
                return false;
            }
            finally {
                return true;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(result) {
                Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }

        }

    }
}
