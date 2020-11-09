package com.cwhq.currencyconvertor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Currency_Convertor_DB";
    public static final String FAVORITE_TABLE_NAME = "FavouritePairs";
    public static final String FAVORITE_TABLE_COLUMN = "pair";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + FAVORITE_TABLE_NAME  + "(" + FAVORITE_TABLE_COLUMN +" text primary key)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FAVORITE_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertPair (String pair) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("pair", pair);

        db.insert( FAVORITE_TABLE_NAME, null, contentValues);
        return true;
    }


    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FAVORITE_TABLE_NAME);
        return numRows;
    }


    public Integer deleteFavouritePair (String pair ) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FAVORITE_TABLE_NAME,
                "pair = ? ",
                new String[] { pair });
    }

    public ArrayList<String> getAllFavouritePairs() {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM "+ FAVORITE_TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(FAVORITE_TABLE_COLUMN)));
            res.moveToNext();
        }
        return array_list;
    }
}
