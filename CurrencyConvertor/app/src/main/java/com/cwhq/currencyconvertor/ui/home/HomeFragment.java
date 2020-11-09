package com.cwhq.currencyconvertor.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.cwhq.currencyconvertor.MainActivity;
import com.cwhq.currencyconvertor.R;
import com.cwhq.currencyconvertor.database.DBHelper;
import com.cwhq.currencyconvertor.models.CurrencyData;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private static String MY_PREFS_NAME ="last_convert";
    public static String App_Lang ="ENGLISH";
    private static String[] CurrencyArray;
    private static String FromCurrency;
    private static Double baseAmount = Double.parseDouble("1");
    private static ArrayList<String> ToCurrencies = new ArrayList<String>();
    private static ArrayList<CurrencyData> CurrencyData = new ArrayList<CurrencyData>();
    private static ArrayList<String> FavouritePairs = new ArrayList<String>();

    private ProgressBar progressBar;
    private EditText editTextAmount;
    private Button buttonFromCurrency, buttonToCurrency;
    private ListView RateList;


    private View root;
    private DBHelper dbHelper;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        root = inflater.inflate(R.layout.fragment_home, container, false);
        dbHelper = new DBHelper(this.getContext());
        FavouritePairs = dbHelper.getAllFavouritePairs();

        progressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
        editTextAmount = (EditText) root.findViewById(R.id.edittext_amountfrom);
        buttonFromCurrency = (Button) root.findViewById(R.id.button_fromcurrency);
        buttonToCurrency = (Button) root.findViewById(R.id.button_tocurrency);
        RateList =(ListView) root.findViewById(R.id.list_rates);

        ArrayAdapter<String> adapter;
        CurrencyArray = getResources().getStringArray(R.array.currencies);


        SharedPreferences prefs = this.getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        App_Lang = prefs.getString("Lang","ENGLISH");
        String Last_FromCurrency = prefs.getString("FromCurrency", "USD");
        String Last_ToCurrencies = prefs.getString("ToCurrencies", "EUR");
        Float Last_Amount = prefs.getFloat("Amount", 1);

        if(App_Lang!=MainActivity.Lang){
            MainActivity.Lang =App_Lang;
            if(App_Lang.equals("ENGLISH")){
                setLocale("en");
            }
            if(App_Lang.equals("CHINESE")){
                setLocale("zh");
            }
        }
        editTextAmount.setText(Last_Amount.toString());
        FromCurrency = Last_FromCurrency;
        for (String Currency : Last_ToCurrencies.split(",")) {
            if(!ToCurrencies.contains(Currency)) {
                ToCurrencies.add(Currency);
            }
        }

        buttonFromCurrency.setText(FromCurrency);

        progressBar.setVisibility(View.GONE);

        convertCurrency();

        editTextAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String Amount = editTextAmount.getText().toString();
                if (Amount.equals("")){Amount="1.0";}
                if(Double.parseDouble(Amount)==Double.parseDouble("0")){
                    Amount = "1";
                    editTextAmount.setText("1.0");
                }
                baseAmount = Double.parseDouble(Amount);
                updateValues();
            }
        });


        buttonFromCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                builder.setTitle(R.string.dialog_base_currency_title);

                int checkedItem = 0; //this will checked the item when user open the dialog
                builder.setSingleChoiceItems(CurrencyArray, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FromCurrency = CurrencyArray[which];
                        buttonFromCurrency.setText(FromCurrency);
                        Toast.makeText(root.getContext(), CurrencyArray[which] + getString(R.string.dialog_base_currency_toast), Toast.LENGTH_LONG).show();
                    }
                });

                builder.setPositiveButton(R.string.dialog_done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(ToCurrencies.size()>0) {
                            convertCurrency();
                            dialog.dismiss();
                        }else{
                            Snackbar.make(root, getString(R.string.dialog_base_currency_snackbar), Snackbar.LENGTH_LONG)
                                    .setAction(getString(R.string.dialog_close), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    })
                                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                                    .show();
                        }
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        buttonToCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                builder.setTitle(R.string.dialog_target_currency_title);
                boolean[] checkedItems = new boolean[CurrencyArray.length];
                for (int i =0; i<CurrencyArray.length;i++) {
                    checkedItems[i] = (ToCurrencies.contains(CurrencyArray[i]) ? true : false);
                }
                builder.setMultiChoiceItems(CurrencyArray, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(!isChecked){
                            if(ToCurrencies.contains(CurrencyArray[which])){
                                ToCurrencies.remove(CurrencyArray[which]);
                                Toast.makeText(root.getContext(),  CurrencyArray[which] + getString(R.string.dialog_target_currency_toast_removed) , Toast.LENGTH_LONG).show();

                            }
                        }else{
                            if(!ToCurrencies.contains(CurrencyArray[which])) {
                                ToCurrencies.add(CurrencyArray[which]);
                            }
                            Toast.makeText(root.getContext(),  CurrencyArray[which] + getString(R.string.dialog_target_currency_toast_selected) , Toast.LENGTH_LONG).show();
                        }

                    }
                });

                builder.setPositiveButton(R.string.dialog_done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(ToCurrencies.size()>0) {
                            convertCurrency();
                            dialog.dismiss();
                        }else{
                            Snackbar.make(root, getString(R.string.dialog_target_currency_snackbar), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.dialog_close, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    })
                                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                                    .show();
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();



            }
        });


        return root;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.top_menu_home, menu);
        return;
    }

    public void setLocale(String localeName) {
            Locale myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(getActivity(), MainActivity.class);
            startActivity(refresh);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_lang:
                AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                builder.setTitle(R.string.select_language);

                final String[] langArray = new String[]{"ENGLISH", "CHINESE"};
                int checkedItem = 0; //this will checked the item when user open the dialog
                builder.setSingleChoiceItems(langArray, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App_Lang = langArray[which];
                        if(App_Lang.equals("ENGLISH")){
                            setLocale("en");
                        }
                        if(App_Lang.equals("CHINESE")){
                            setLocale("zh");
                        }
                        saveSharedPrefs();

                      }
                });

                builder.setPositiveButton(R.string.dialog_done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Snackbar.make(root, getString(R.string.language_changed), Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.dialog_close), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                                .show();
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;

            case R.id.menu_help:
                AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getActivity());
                helpBuilder.setTitle(R.string.help_menu);

                TextView tv = new TextView(getActivity());
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setText(Html.fromHtml(getString(R.string.help_text_home)));

                LinearLayout lyt = new LinearLayout(getActivity());

                final float scale = getContext().getResources().getDisplayMetrics().density;
                int padding = (int) (20 * scale + 0.5f);
                lyt.setPadding(padding, padding, padding, padding);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lyt.setLayoutParams(params);
                lyt.setLayoutParams(params);
                lyt.addView(tv);

                helpBuilder.setView(lyt);
                helpBuilder.setPositiveButton(R.string.dialog_done, null);
                helpBuilder.show();

                return true;
        }
        return onOptionsItemSelected(item);
    }



    private void updateValues(){
         ArrayList<CurrencyData> TempCurrencyData = new ArrayList<CurrencyData>();

        for(CurrencyData currencyData : CurrencyData){
            TempCurrencyData.add(new CurrencyData(currencyData.getName(),currencyData.getRate(),currencyData.getRate()*baseAmount));
        }
        CurrencyData = new ArrayList<CurrencyData>();
        CurrencyData = TempCurrencyData;
        saveSharedPrefs();
        showConvertedRates();
    }


    private void convertCurrency(){
        String Amount = editTextAmount.getText().toString();
        if (Amount.equals("")){Amount="1.0";}
        if(Double.parseDouble(Amount)==Double.parseDouble("0")){
            Amount = "1";
            editTextAmount.setText("1.0");
        }
        baseAmount = Double.parseDouble(Amount);


        if(!ToCurrencies.contains(FromCurrency)) {
            progressBar.setVisibility(View.VISIBLE);
            CurrencyData = new ArrayList<CurrencyData>();
            saveSharedPrefs();
            String path = "https://api.exchangeratesapi.io/latest?base=" + FromCurrency + "&symbols=" + android.text.TextUtils.join(",", ToCurrencies);
            new AsyncGetRates().execute(path);
        }else{
            showError(getString(R.string.convert_same_currency_error));
        }
    }

    public void showConvertedRates(){
        RateList.setAdapter(null);
        CustomRateAdapter adapter = new CustomRateAdapter();
        RateList.setAdapter(adapter);
    }

    private void addToFavourite(String pair){

        if(FavouritePairs.contains(pair)){
            dbHelper.deleteFavouritePair(pair);
            Toast.makeText(root.getContext(), pair + getString(R.string.add_to_fav_removed), Toast.LENGTH_LONG).show();

        }else {
            dbHelper.insertPair(pair);
            Toast.makeText(root.getContext(), pair + getString(R.string.add_to_fav_added), Toast.LENGTH_LONG).show();

        }

        FavouritePairs = dbHelper.getAllFavouritePairs();
    }

    public void saveSharedPrefs(){
        SharedPreferences.Editor editor = this.getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("Lang",App_Lang);
        editor.putString("FromCurrency", FromCurrency);
        editor.putString("ToCurrencies", android.text.TextUtils.join(",", ToCurrencies));
        editor.putFloat("Amount",Float.parseFloat(baseAmount.toString()));
        editor.apply();
    }

    private void showError(String msg){
        Snackbar.make(root, msg, Snackbar.LENGTH_LONG)
                .setAction(R.string.dialog_close, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                .show();
    }

    private void showCurrencyInfo(CurrencyData currencyData){
        new AlertDialog.Builder(root.getContext())
                .setTitle(R.string.currency_rate)
                .setMessage(getString(R.string.currency) + currencyData.getName() + getString(R.string.rate) + currencyData.getRate() + getString(R.string.value) + currencyData.getValue()  )

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public class CustomRateAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return CurrencyData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override

        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.row_currency,null);

            RelativeLayout Row = (RelativeLayout)view.findViewById(R.id.row_currency);
            TextView Name = (TextView)view.findViewById(R.id.text_currency);
            TextView Rate = (TextView)view.findViewById(R.id.text_rate);
            TextView Value = (TextView)view.findViewById(R.id.text_value);
            final ImageButton AddToFavourite = (ImageButton)view.findViewById(R.id.button_addtofavourite);

            Name.setText(CurrencyData.get(i).getName());
            Rate.setText(Double.toString(CurrencyData.get(i).getRate()));
            Value.setText(Double.toString(CurrencyData.get(i).getValue()));

            final String thisPair = FromCurrency + "-" + CurrencyData.get(i).getName();
            if(FavouritePairs.contains(thisPair)){
                AddToFavourite.setImageResource(R.drawable.ic_favourite);
            }else{
                AddToFavourite.setImageResource(R.drawable.ic_not_favourite);
            }

            AddToFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(FavouritePairs.contains(thisPair)){
                        AddToFavourite.setImageResource(R.drawable.ic_not_favourite);
                    }else{
                        AddToFavourite.setImageResource(R.drawable.ic_favourite);
                    }
                    addToFavourite(thisPair);

                }
            });

            Row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCurrencyInfo(CurrencyData.get(i));
                }
            });
            return view;

        }
    }


    public class AsyncGetRates extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            CurrencyData = new ArrayList<CurrencyData>();
            progressBar.setVisibility(View.GONE);
            if(s!= null && !s.equals("error")) {
                try {
                    JSONObject rates = new JSONObject(s);
                    for (String Currency : ToCurrencies) {
                        CurrencyData.add(new CurrencyData(Currency, rates.getDouble(Currency), rates.getDouble(Currency) * baseAmount));
                    }
                    showConvertedRates();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                showError(getString(R.string.error_try_again));
            }


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CurrencyData = new ArrayList<CurrencyData>();
        }

        @Override
        public String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                if(!finalJson.contains("error")) {
                    JSONObject jsonObject = new JSONObject(finalJson);
                    JSONObject rates = jsonObject.getJSONObject("rates");
                    return rates.toString();
                }else{
                    return "error";
                }




            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}