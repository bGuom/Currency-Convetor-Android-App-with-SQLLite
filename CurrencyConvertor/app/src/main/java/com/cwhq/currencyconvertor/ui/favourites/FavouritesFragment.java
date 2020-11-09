package com.cwhq.currencyconvertor.ui.favourites;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class FavouritesFragment extends Fragment {

    private static String MY_PREFS_NAME ="favourite_page";
    private static ArrayList<String> FavouritePairs = new ArrayList<String>();

   private ProgressBar progressBar;
    private ListView FavouriteList;

    private View root;
    private DBHelper dbHelper;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        root = inflater.inflate(R.layout.fragment_favourites, container, false);

        dbHelper = new DBHelper(this.getContext());
        FavouritePairs = dbHelper.getAllFavouritePairs();

        progressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
        FavouriteList =(ListView) root.findViewById(R.id.list_favourites);

        showFavoritePairs();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.top_menu, menu);
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                AlertDialog.Builder helpBuilder = new AlertDialog.Builder(getActivity());
                helpBuilder.setTitle(R.string.help_menu);

                TextView tv = new TextView(getActivity());
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setText(Html.fromHtml(getString(R.string.help_text_fav)));

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


    public void showFavoritePairs(){
        //if(FavouritePairs.size()==0){showError("No Favourite Items to Show");}
        FavouriteList.setAdapter(null);
        CustomRateAdapter adapter = new CustomRateAdapter();
        FavouriteList.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }

    private void removeFavourite(String pair){

        if(FavouritePairs.contains(pair)){
            dbHelper.deleteFavouritePair(pair);
        }
        Toast.makeText(root.getContext(), pair + getString(R.string.dialog_target_currency_toast_removed), Toast.LENGTH_LONG).show();

        FavouritePairs = dbHelper.getAllFavouritePairs();
        showFavoritePairs();
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

    private void showPairInfo(String pair){
        new AlertDialog.Builder(root.getContext())
                .setTitle(R.string.favourite_currency_pair)
                .setMessage(getString(R.string.base_currency) + pair.split("-")[0] +  getString(R.string.target_currency) + pair.split("-")[1]  )

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
            return FavouritePairs.size();
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
            view = getLayoutInflater().inflate(R.layout.row_favourite,null);

            LinearLayout Row = (LinearLayout)view.findViewById(R.id.row_favourite_pair);
            TextView From = (TextView)view.findViewById(R.id.text_from);
            TextView To = (TextView)view.findViewById(R.id.text_to);
            final ImageButton RemoveFavourite = (ImageButton)view.findViewById(R.id.button_delete);

            From.setText(FavouritePairs.get(i).split("-")[0]);
            To.setText(FavouritePairs.get(i).split("-")[1]);

            RemoveFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeFavourite(FavouritePairs.get(i));

                }
            });

            Row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPairInfo(FavouritePairs.get(i));
                }
            });

            return view;

        }
    }
}