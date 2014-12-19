package com.app.jieyangchen.onlinemusicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager extends SQLiteOpenHelper {
	private static String DATABASE_NAME="music.db";
	private String TABLE_NAME="music_list";
	public String _ID="_id";
	public String _TITLE="_title";
	public String _ARTIST="_artist";
	public String _ALBUM="_album";
	public String _DURATION="_duration";
	public String _DATA="_data";
	public String _DISPLAY_NAME="_display_name";
	public String _COMPOSER="_composer";
	public String _SIZE="_size";
	public String _YEAR="_year";
	public String _RATE="_rate";
	private SQLiteDatabase db = null;
	public DBManager(Context context) {
	
		super(context, DATABASE_NAME, null, 1);
		db=getWritableDatabase();
	//	onCreate(db);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.d("TEST","oncreate");
		//db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		String create="create table if not exists "+TABLE_NAME+"("+_ID+" integer primary key autoincrement,"+
													_TITLE+" text,"+
													_ARTIST+" text,"+
													_ALBUM+" text," +
													_YEAR+" text," +
												//	_DURATION+" text,"+
													_DATA+" text,"+
												//	_DISPLAY_NAME+" text,"+
												//	_COMPOSER+" text,"+
												//	_SIZE+" text,"+
													_RATE+" integer"+");";
		db.execSQL(create);
		//db.delete(TABLE_NAME, null, null);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.d("TEST","upgrade");
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		
		onCreate(db);
		
	}
	
	public void deleteDB(){
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
	}
	public void addMusic(Cursor c){
		db=getWritableDatabase();
		if(db==null){
			Log.d("TEST","get db error");
		}
		if(c==null){
			Log.d("TEST","cursor error");
		}
		c.moveToFirst();
		ContentValues v=null;
		
		for(int i=0;i<c.getCount();i++){
		

			InsertNew(c);
		
		c.moveToNext();
	//	}
		}
	}
	public void deleteMusicByPath(String path){
		db=getWritableDatabase();
		
		db.delete(TABLE_NAME, _DATA+"=\""+path+"\"",null);
	}
	
	
	public Cursor getMusicByPath(String path){
		Log.d("TEST","get-music-by-path:"+path);
		db=getWritableDatabase();
		Cursor c=db.query(TABLE_NAME, null, _DATA+"=\""+path+"\"", null, null, null, null);
		c.moveToFirst();
		
		//Log.d("TEST","get-path:"+path+"   rate:"+c.getInt(6));
		return c;
		
	}
	
	public void InsertNew(Cursor c){
		db=getWritableDatabase();
		
	
		
		int result=db.query(TABLE_NAME, null, _DATA+"=\""+c.getString(5).substring(4)+"\"", null, null, null, null).getCount();
			ContentValues v=new ContentValues();
			v.put(_TITLE,c.getString(1));
			v.put(_ARTIST,c.getString(2));
			v.put(_ALBUM,c.getString(3));
			v.put(_YEAR,c.getString(4));
			v.put(_DATA,c.getString(5).substring(4));
			
			if(result==0){
				Log.d("TEST","new-music:"+c.getString(5).substring(4));
				v.put(_RATE,0);
			
			if(db.insertOrThrow(TABLE_NAME, null, v)!=-1)
				Log.d("TEST","Insert OK:"+c.getString(1));
			else Log.d("TEST","Insert error");
			
			
		}
			else if(result==1){
				Log.d("TEST","update- OK:"+c.getString(1));
				db.update(TABLE_NAME, v, _DATA+"=\""+c.getString(5).substring(4)+"\"", null);
			}
		
	}
	
	
	public void setMusicRate(String path,int rate){
		db=getWritableDatabase();
		ContentValues v=new ContentValues();
		v.put("_RATE",rate);
		
		int i=db.update(TABLE_NAME, v, _DATA+"=\""+path+"\"", null);
		Log.d("TEST","path:"+path+"  rating:"+rate+"  result:"+i);
	}
	public SQLiteDatabase getDB(){
		db=getWritableDatabase();
		return db;
	}
	
	public Cursor getAll(){
		db=getWritableDatabase();
		Cursor c=db.query(TABLE_NAME, null,null, null, null, null, null);
		return c;
	}
	public Cursor getAllArtist(){
		db=getWritableDatabase();
		Cursor c=db.query(TABLE_NAME,new String[]{"DISTINCT "+_ARTIST} ,null, null, null, null, null);
		return c;
	}
	
	public Cursor getAllAlbum(){
		db=getWritableDatabase();
		Cursor c=db.query(TABLE_NAME,new String[]{"DISTINCT "+_ALBUM} ,null, null, null, null, null);
		return c;
	}
	
	
	
	public Cursor getMusicByArtist(String artist){
		db=getWritableDatabase();
		
		return db.query(TABLE_NAME, null, _ARTIST+"=\""+artist+"\"", null, null, null, null);
		
	}
	
	public Cursor getMusicByAlbum(String album){
		db=getWritableDatabase();
		
		return db.query(TABLE_NAME, null, _ALBUM+"=\""+album+"\"", null, null, null, null);
		
	}
	
	public Cursor getFavoriteMusic(){
		db=getWritableDatabase();
		
		return db.query(TABLE_NAME, null, _RATE+"="+10, null, null, null, null);
		
	}
	
	public void closeDB(){
		db=getWritableDatabase();
		db.close();
	}
}
