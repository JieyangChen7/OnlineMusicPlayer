package com.app.jieyangchen.onlinemusicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


import com.app.jieyangchen.onlinemusicplayer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class List extends Activity {
	private DBManager manager;
	private ListView list;
	private TextView title;
	private static final int ITEM1 = Menu.FIRST;
	private static final int ITEM2 = Menu.FIRST+1;
	private static final int ITEM3 = Menu.FIRST+2;
	private String _WELCOME="Welcome！";
	private String _LONG_CLICK_TO_DELETE="Long press to delete";
	private String _DELETE_SUCCESSFULLY="Deleted";
	private String _DELETE_CONFIRM="Are you sure?";
	private String _NEGATIVE="Cancel";
	private String _POSITIVE="OK";
	private String _ALL_MUSIC="All Music";
	private String _ALL_MUSIC_DESCRIPTION="Show all the music on SD card by name";
	private String _ALL_ARTIST="All Artist";
	private String _ALL_ARTIST_DESCRIPTION="Show all the music on SD card by artist";
	private String _ALL_ALBUM="All Album";
	private String _ALL_ALBUM_DESCRIPTION="Show all the music on SD card by album";
	private String _MY_FAVORITE="My Favorites";
	private String _MY_FAVORITE_DESCRIPTION="Show all the top rated music";
	private String _ONLINE_MUSIC="Online Music";
	private String _ONLINE_MUSIC_DESCRIPTION="Online downloading and streaming";
	private String _REFRESH_DATABASE="Update Music Library";
	private String _NOW_PLAYING="Now Playing";
	private String _QUIT="Quit";
	private String _MAIN_MENU_TITLE="Main Menu";
	private String _ALL_MUSIC_TITLE="All Music";
	private String _ALL_ARTIST_TITLE="All Artist";
	private String _ALL_ALBUM_TITLE="All Album";
	private String _MY_FAVORITE_TITLE="My Favorite";
	private String _ARTIST_TITLE1="Artist:";
	private String _ALBUM_TITLE1="Album:";
	private String _ARTIST_TITLE2="'s music";
	private String _ALBUM_TITLE2="'s music";
	
	
	
	SimpleAdapter listAdapter;
	private Context context;
	private int type;
	private String info;
	private String downloadPath="";
	private String playingPath="";
	private int downloadStatus=0;
	private boolean isStream;
	private int isPlaying=0;
	ArrayList<HashMap<String, String>> listData=null;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Toast.makeText(this, _WELCOME, Toast.LENGTH_LONG).show();
		Log.d("TEST","list-start");
		setContentView(R.layout.list);
        Log.d("TEST","list-start-1");
		title=(TextView)findViewById(R.id.titleText);
        Log.d("TEST","list-start-2");
		list=(ListView)findViewById(R.id.list);
		context=this;
		Intent  intent=this.getIntent();
        Log.d("TEST","list-start-3");
		int type1=intent.getIntExtra("type", -1);
        Log.d("TEST","list-start-4");
		String info1;
		Refresh();
        Log.d("TEST","list-start-5");

		if(type1!=-1){
			info1=intent.getStringExtra("info");
			
			if(type1==0){
				Log.d("TEST","list-start-type0");
				type=0;
				getAllMusic();
				title.setText(_ALL_MUSIC_TITLE);
			}
			else if(type1==1){
				Cursor c1=manager.getMusicByArtist(info1);
				
				type=1;
				info=info1;
				MusicList(c1);
				title.setText(_ARTIST_TITLE1+info+_ARTIST_TITLE2);
			}
			else if(type1==2){
				Cursor c1=manager.getMusicByAlbum(info1);
				type=2;
				info=info1;
				MusicList(c1);
				title.setText(_ALBUM_TITLE1+info+_ALBUM_TITLE2);
			}
			else if(type==3){
	    		Cursor c1=manager.getFavoriteMusic();
	    		type=3;
	    		info=info1;
	    		MusicList(c1);
	    		title.setText(_MY_FAVORITE_TITLE);
	    	}
			else if(type1==4){
				mainMenu();
				type=-1;
			}
			
		}
		else{
		
		mainMenu();
		type=-1;
		}
		
		
		
		
		IntentFilter SDfilter = new IntentFilter();		
		SDfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		SDfilter.addDataScheme("file");
		registerReceiver(sdr, SDfilter);
		
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("Current");
		registerReceiver(r, filter);
		
	}
	
	
	 protected BroadcastReceiver sdr=new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Log.d("TEST","get:"+intent.getAction());
				
				if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)){
					Log.d("TEST","scanner=finished");
					manager.addMusic(ScanSD());
				}
			}
	    	
	    };
	    
	    
	    
	    protected BroadcastReceiver r=new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals("Current")){
				//	path=intent.getStringExtra("Path");
					downloadPath=intent.getStringExtra("downloadPath");
					playingPath=intent.getStringExtra("playingPath");
					downloadStatus=intent.getIntExtra("downloadStatus",0);
					isStream=intent.getBooleanExtra("isStream", false);
					isPlaying=intent.getIntExtra("isPlaying", 2);
					
				}
			}
	    };
	
	public void Refresh(){
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
		manager=new DBManager(context);
		manager.onCreate(manager.getDB());
	//	manager.addMusic(ScanSD());
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		Log.d("TEST","menu-created-ok");
		menu.add(0,ITEM1,0,_REFRESH_DATABASE).setIcon(android.R.drawable.ic_popup_sync);		
		menu.add(0,ITEM2,0,_NOW_PLAYING).setIcon(android.R.drawable.ic_menu_upload_you_tube);
		menu.add(0,ITEM3,0,_QUIT).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case ITEM1:
				manager.deleteDB();
			//	manager.onCreate(manager.getDB());
				Refresh();
				break;
			case ITEM2:
				Intent intent = new Intent();
				intent.setClass(List.this, MusicUI.class);
				intent.putExtra("resume", 1);
				startActivity(intent);
				unregisterReceiver(r);
				finish();
				break;
			case ITEM3:
				Intent intent1 = new Intent();
				intent1.setAction("com.cjy.media.Music_Service");
				stopService(intent1);
				manager.closeDB();
				finish();
				break;
		}
		
		
		return true;
	}
	
	
	public Cursor ScanSD(){
		Cursor c = this.getContentResolver()
				.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						new String[]{
						MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.YEAR,		
						MediaStore.Audio.Media.DATA,
						},
						null, null, null);
		Log.d("TEST","num"+c.getCount());
		c.moveToFirst();

		return c;
	}
	
	public void MusicList(Cursor c) {
		Toast.makeText(this, _LONG_CLICK_TO_DELETE, Toast.LENGTH_LONG).show();
		Log.d("TEST","music-list-num"+c.getCount());
		listData = new ArrayList<HashMap<String, String>>();
			c.moveToFirst();
			HashMap<String, String> map;
			
			for (int i = 0; i < c.getCount(); i++) {
				map = new HashMap<String, String>();
				
				map.put("id", c.getInt(0)+"");
				String Extra="";
				
				if(c.getString(5).equals(downloadPath)&&c.getString(5).equals(playingPath)){
					map.put("Title", c.getString(1)+"(Playing&Downloading)");
				}
					
				else if(c.getString(5).equals(playingPath)){
					map.put("Title", c.getString(1)+"(Playing)");
				}
				
				
				
				else if(c.getString(5).equals(downloadPath)){
					map.put("Title", c.getString(1)+"(Downloading)");
				}
				else{
					map.put("Title", c.getString(1));
				}
				map.put("Artist", c.getString(2));
				listData.add(map);
				Log.d("TEST","list add:"+c.getString(1));
				c.moveToNext();
			}
			
			listAdapter = new SimpleAdapter(List.this,
					listData,
					R.layout.item,
					new String[]{"Title","Artist"},
					new int[]{R.id.Title,R.id.Artist});	
		list.setAdapter(listAdapter);
		list.setOnItemClickListener(musicListListener);
		list.setOnItemLongClickListener(longClick);
		Log.d("TEST","list done");
	}
	
OnItemLongClickListener longClick=new OnItemLongClickListener(){
		

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				final int arg2, long arg3) {
			// TODO Auto-generated method stub
			
			
			
			
			
			DialogInterface.OnClickListener confirmListener =new DialogInterface.OnClickListener() {
				
			    	
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(type==0||info!=""){
							Log.d("TEST","long-clicked");
							Cursor c=getLocalMusic(type,info,arg2);
							manager.deleteMusicByPath(c.getString(5));
							Log.d("TEST","delete-test:"+c.getString(5));
							
							
							if(c.getString(5).equals(downloadPath)){
								Intent intent1=new Intent();
								intent1.setAction("DELETE");
								sendBroadcast(intent1);
							}
							else{
								File file=new File(c.getString(5));
								if(file.exists()){
									file.delete();
								}
							}
							
							MusicList(getList(type,info));
							
							Toast.makeText(context, _DELETE_SUCCESSFULLY, Toast.LENGTH_LONG).show();
							
						}
							
					
				}
			};
			
			AlertDialog show = new AlertDialog.Builder(context)
			
		    .setMessage(_DELETE_CONFIRM)
		    .setNegativeButton(_NEGATIVE, null)
		    .setPositiveButton(_POSITIVE, confirmListener)
			
		    .show();

		

			
			
			
			MusicList(getList(type,info));
			
			return true;
		}
	};
	
	
	public Cursor getLocalMusic(int type, String info, int id){
    	Cursor c=getList(type,info);
		c.moveToFirst();	
		if(id<c.getCount()){
		for(int i=0;i<id;i++){
			c.moveToNext();
		}
		}
		else
			c=null;
		
		return c;
    }
	
	public Cursor getList(int type, String info){
    	if(type==-1){
    		Log.d("TEST","get-type-error");
    		return null;
    	}
    	else if(type==0){
    		Cursor c=manager.getAll();
    		return c;
    		
    	}
    	else if(type==1){
    		Cursor c=manager.getMusicByArtist(info);
    		return c;
    		
    	}
    	else if(type==2){
    		Cursor c=manager.getMusicByAlbum(info);
    		return c;
    	}
    	else if(type==3){
    		Cursor c=manager.getFavoriteMusic();
    		return c;
    	}
    	return null;
    }
	
	
	public void mainMenu(){
		title.setText(_MAIN_MENU_TITLE);
		type=-1;
		listData = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		map = new HashMap<String, String>();
		map.put("Choice", _ALL_MUSIC);
		map.put("Description", _ALL_MUSIC_DESCRIPTION);
		listData.add(map);
		map = new HashMap<String, String>();
		map.put("Choice", _ALL_ARTIST);
		map.put("Description", _ALL_ARTIST_DESCRIPTION);
		listData.add(map);
		map = new HashMap<String, String>();
		map.put("Choice", _ALL_ALBUM);
		map.put("Description", _ALL_ALBUM_DESCRIPTION);
		listData.add(map);
		map = new HashMap<String, String>();
		map.put("Choice", _MY_FAVORITE);
		map.put("Description", _MY_FAVORITE_DESCRIPTION);
		listData.add(map);
		map = new HashMap<String, String>();
		map.put("Choice", _ONLINE_MUSIC);
		map.put("Description", _ONLINE_MUSIC_DESCRIPTION);
		listData.add(map);
		
		listAdapter = new SimpleAdapter(List.this,
				listData,
				R.layout.item,
				new String[]{"Choice","Description"},
				new int[]{R.id.Title,R.id.Artist});	
	list.setAdapter(listAdapter);
	list.setOnItemClickListener( mainMenuListener);
	
	}
	
	
	private void getAllMusic(){
		title.setText(_ALL_MUSIC_TITLE);
		type=0;
		info="";
		
		Cursor c = manager.getAll();
		Log.d("TEST","get-all-music:"+c.getCount());
		MusicList(c);
		
	}
	
private void getFaviroteMusic(){
	title.setText(_MY_FAVORITE_TITLE);
		type=3;
		info="";
		
		Cursor c = manager.getFavoriteMusic();
		Log.d("TEST","get-getFavoriteMusic-music:"+c.getCount());
		MusicList(c);
		
	}
	
	
	public void getAllArtist(){
		title.setText(_ALL_ARTIST_TITLE);
		type=1;
		listData = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		info="";
		Cursor c = manager.getAllArtist();
		c.moveToFirst();
		Log.d("TEST","artist-count:"+c.getCount());
		for(int i=0;i<c.getCount();i++){
			map = new HashMap<String, String>();
			map.put("Artist",c.getString(0));
			listData.add(map);
			c.moveToNext();
		}
		listAdapter = new SimpleAdapter(List.this,
				listData,
				R.layout.item,
				new String[]{"Artist"},
				new int[]{R.id.Title});	
	list.setAdapter(listAdapter);
	list.setOnItemClickListener( allArtistListener);
		
	}
	
	public void getAllAlbum(){
		title.setText(_ALL_ALBUM_TITLE);
		type=2;
		listData = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map;
		info="";
		Cursor c = manager.getAllAlbum();
		c.moveToFirst();
		Log.d("TEST","album-count:"+c.getCount());
		for(int i=0;i<c.getCount();i++){
			map = new HashMap<String, String>();
			map.put("Album",c.getString(0));
			listData.add(map);
			c.moveToNext();
		}
		listAdapter = new SimpleAdapter(List.this,
				listData,
				R.layout.item,
				new String[]{"Album"},
				new int[]{R.id.Title});	
	list.setAdapter(listAdapter);
	list.setOnItemClickListener( allAlbumListener);
	}

	OnItemClickListener mainMenuListener =new OnItemClickListener(){

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
		switch (arg2){
		case 0:
			getAllMusic();
			
			break;
		case 1:
			getAllArtist();
			break;		
		case 2:
			getAllAlbum();
			break;
			
		case 3:
			getFaviroteMusic();
			break;
			
		case 4:
			Intent intent1=new Intent();
			intent1.setAction("DELETE");
			sendBroadcast(intent1);
			Intent intent = new Intent();
			intent.setClass(List.this, SearchWeb.class);
			startActivity(intent);
			manager.close();
			unregisterReceiver(r);
			finish();
			break;
		
		}
		
		}
		
	};
	OnItemClickListener musicListListener= new OnItemClickListener(){

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {			
			
			Cursor c=getLocalMusic(type,info,arg2);
			Log.d("TEST", "need-resume:"+c.getString(5)+"???"+playingPath);
			
			
			if(c.getString(5).equals(downloadPath)){
				Intent intent = new Intent();
				intent.setClass(List.this, MusicUI.class);
				intent.putExtra("resume", 2);
				startActivity(intent);
				unregisterReceiver(r);
				finish();
			}
			else if(c.getString(5).equals(playingPath)){
				Log.d("TEST", "LOCAL-resume");
				Intent intent = new Intent();
				intent.setClass(List.this, MusicUI.class);
				intent.putExtra("resume", 1);
				startActivity(intent);
				unregisterReceiver(r);
				finish();
			}
			else{
//				Intent intent1=new Intent();
//				intent1.setAction("CANCELDL");
//				sendBroadcast(intent1);
//			
			Intent intent = new Intent();
			intent.setClass(List.this, MusicUI.class);
			intent.putExtra("type", type);
			intent.putExtra("info", info);
			intent.putExtra("id", arg2);
			intent.putExtra("isOnline", 0);
			startActivity(intent);
			unregisterReceiver(r);
			finish();
			}
		}
		
	};
	
	
	
	
	
	
	OnItemClickListener allArtistListener= new OnItemClickListener(){

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Cursor c = manager.getAllArtist();
			c.moveToFirst();
			for(int i=0;i<arg2;i++){
				c.moveToNext();
			}
			Cursor c1=manager.getMusicByArtist(c.getString(0));
			Log.d("TEST",c.getString(0)+":"+c1.getCount());
			type=1;
			info=c.getString(0);
			MusicList(c1);
			title.setText(_ARTIST_TITLE1+info+_ARTIST_TITLE2);
		}
		
	};
	
	OnItemClickListener allAlbumListener= new OnItemClickListener(){

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Cursor c = manager.getAllAlbum();
			c.moveToFirst();
			for(int i=0;i<arg2;i++){
				c.moveToNext();
			}
			Cursor c1=manager.getMusicByAlbum(c.getString(0));
			Log.d("TEST",c.getString(0)+":"+c1.getCount());
			type=2;
			
			info=c.getString(0);
			MusicList(c1);
			title.setText(_ALBUM_TITLE1+info+_ALBUM_TITLE2);
		}
		
	};
	
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == event.KEYCODE_BACK) {
				Log.d("TEST","BACK_PRESSED");
					if(type==0){
						Log.d("TEST","type=0-return");
					mainMenu();
					}
					else if(type==1){
						Log.d("TEST","type=1-return");
						if(info==""){
							mainMenu();
						}
						else{
							getAllArtist();
						}
					}
					else if(type==2){
						Log.d("TEST","type=2-return");
						if(info==""){
							mainMenu();
						}
						else{
							getAllAlbum();
						}
					}
					
					if(type==3){
						Log.d("TEST","type=0-return");
					mainMenu();
					}
					
					
				return true;
			}
			
			return false;
	    }
	
	
	
}
