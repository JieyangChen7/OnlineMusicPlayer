package com.app.jieyangchen.onlinemusicplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class MusicService extends Service implements
		MediaPlayer.OnCompletionListener {
	/** Called when the activity is first created. */

	private MediaPlayer mp;
	private Handler handler;
	private DBManager manager;
	private int id;
	private int type;
	private String info;
	private String playingPath;
	private String downloadPath;
	private String url;
	private String SDCard;
	private boolean isStream = false;
	private int downloadStatus = 1;
	private int isPlaying = 2;
	private boolean detect = false;
	private double precent;
	private int real;
	private int duration;
	private int current;
	private int mode = 0;
	private int DLmode = 0;
	private int downloaded = 0;
	private int total = 0;
	private String title;
	private String artist;
	private String album;
	private String year;
	private int speed;
	private int listNum = 0;
	private boolean isReady = false;
	private boolean isSingle;
	private boolean isLoop;
	private Context context;
	private String DLStatus;
	private boolean isBuffer = false;
	private int bufferMax = 0;
	private int bufferCur = 0;
	private int rating;
	Thread timeThread;
	Thread streamThread;
	Thread downloadThread;

	File download;

	@Override
    public void onCreate() {
        super.onCreate();
       Log.d("TEST","Service-Create");
       	manager=new DBManager(this);       	
       
        SDCard=Environment.getExternalStorageDirectory()+"";
        SDCard=SDCard.substring(4);
        context=this;
        IntentFilter filter = new IntentFilter();		
       
		filter.addAction("PLAY");
		filter.addAction("PAUSE");
		filter.addAction("FORWARD");
		filter.addAction("REWIND");
		filter.addAction("LAST");
		filter.addAction("NEXT");
		filter.addAction("SINGLE");
		filter.addAction("LOOP");
		filter.addAction("PROGRESS_CHANGE");
		filter.addAction("RATING");
		filter.addAction("PAUSEDL");
		filter.addAction("CANCELDL");
		filter.addAction("DELETE");
		filter.addAction("RESUMELOCAL");
		filter.addAction("RESUMEDOWNLOAD");
		
		registerReceiver(r, filter);
		
		IntentFilter SDfilter = new IntentFilter();		
		SDfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		SDfilter.addDataScheme("file");
		registerReceiver(sdr, SDfilter);
       
		
		if(timeThread!=null){
		timeThread.interrupt();
		}
		timeThread =new Thread(new updateThread());
		timeThread.start();
		if(streamThread!=null){
			streamThread.interrupt();
		}
		streamThread=new Thread(new playStream());
		streamThread.start();	
		
     
        
    }

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		resetInfo();
		if (intent.getIntExtra("isOnline", -1) == 0) {
			isStream = false;
			mode = 0;

			id = intent.getIntExtra("id", -1);
			type = intent.getIntExtra("type", -1);
			info = intent.getStringExtra("info");
			Log.d("TEST", "Service-Start-get" + id + "type:" + type);
			Cursor c = getLocalMusic(type, info, id,1);
			playingPath = c.getString(5);
			// Log.d("TEST","local-path"+path);
			refreshSD();
			playerReady();
		} else if (intent.getIntExtra("isOnline", -1) == 1) {
			isStream = true;
			// type=3;
			mode = 1;
			url = intent.getStringExtra("url");
			Log.d("TEST", "music-service-get-url:" + url);
			Log.d("TEST", "SD" + SDCard);
			if (downloadThread != null) {
				downloadThread.interrupt();
			}
			downloadThread = new Thread(new download());
			downloadThread.start();
			downloadStatus = 0;

		}

	}

	public Cursor getList(int type, String info) {
		if (type == -1) {
			Log.d("TEST", "get-type-error");
			return null;
		} else if (type == 0) {
			Cursor c = manager.getAll();
			return c;

		} else if (type == 1) {
			Cursor c = manager.getMusicByArtist(info);
			return c;

		} else if (type == 2) {
			Cursor c = manager.getMusicByAlbum(info);
			return c;
		} else if (type == 3) {
			Cursor c = manager.getFavoriteMusic();
			Log.d("TEST", "favorite:" + c.getCount());
			return c;
		}
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("TEST", "onDestroy()");
		if (timeThread != null) {
			timeThread.interrupt();
			Log.d("TEST", "onDestroy()-stop-timethread");
		}
		if (streamThread != null) {
			streamThread.interrupt();
			Log.d("TEST", "onDestroy()-stop-streamThread");
		}
		if (downloadThread != null) {
			downloadThread.interrupt();
			Log.d("TEST", "onDestroy()-stop-downloadThread");
		}

		if (mp != null) {
			mp.stop();
			mp.reset();
			mp.release();
			mp = null;
			Log.d("TEST", "onDestroy()-stop-mediaplayer");

		}

		Intent intent1 = new Intent();
		intent1.setAction("DELETE");
		sendBroadcast(intent1);

		unregisterReceiver(r);
		unregisterReceiver(sdr);
		manager.close();

	}

	class download implements Runnable {
		public void run() {

			int lastTime = 0;
			int currentDownload = 0;
			int check = 0;
			int check1 = 0;
			int check2 = 1;
			int reconnect = 0;
			DLStatus = "";
			downloadStatus = 0;
			downloaded = 0;
			total = 0;
			precent = 0;
			speed = 0;

			try {
				URL myurl = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) myurl
						.openConnection();
				if (conn.getResponseCode() == 200) {
					Log.d("TEST", "http-connect-ok");
					downloadStatus = 1;
				} else {
					Log.d("TEST", "http-connect-error");
					downloadStatus = -1;
					Thread.currentThread().interrupt();

				}
				total = conn.getContentLength();
				String[] split = url.split("/");
				String filename = split[split.length - 1];
				filename = URLDecoder.decode(filename, "UTF-8");

				Log.d("TEST", "filename:" + filename);
				downloadPath = SDCard + "/" + filename;
				download = new File(downloadPath);
				playingPath = downloadPath;
				if (download.exists()) {
					if (download.delete()) {
						Log.d("TEST", "delete-ok");

					} else
						Log.d("TEST", "delete-error");
				}
				download.createNewFile();
				InputStream in = conn.getInputStream();
				OutputStream output = new FileOutputStream(download);
				byte[] buff = new byte[1024 * 10];

				while (currentDownload != -1) {

					if (downloadStatus == 2) {
						speed = 0;
					} else if (downloadStatus == 1) {
						Log.d("TEST", "precent:" + precent + "d:" + downloaded
								+ "t:" + total + "c:" + currentDownload);
						Thread.currentThread().sleep(100);

						currentDownload = in.read(buff);
						speed = (int) ((double) (currentDownload * 10) / (double) 1024);

						if (currentDownload > 0)
							output.write(buff);
						total = conn.getContentLength();
						if (total > 0 && currentDownload > 0) {
							downloaded = downloaded + currentDownload;
							if (downloaded <= total) {
								precent = (double) downloaded / (double) total;
							}
						}

						if (precent > 0.05 && check == 0) {
							if (check2 == 0) {
								Log.d("TEST", "check2");
								if (getRealTime() - lastTime > 0) {
									mode = 1;
									DLmode = 1;
									Log.d("TEST", "mode1");
								} else if (getRealTime() - lastTime == 0
										&& lastTime != 0) {
									mode = 2;
									DLmode = 2;
									Log.d("TEST", "mode2");
								}
								refreshSD();
								playerReady();
								check2 = 1;
								check = 1;
							}

							if (check1 == 0) {
								Log.d("TEST", "check1");
								lastTime = getRealTime();
								check2 = 0;
								check1 = 1;
							}
						}
					}

				}
				output.flush();
				output.close();
				downloadStatus = 3;
				speed = 0;
				detect = false;
				Log.d("TEST", "download-done");
				Thread.currentThread().interrupt();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class updateThread implements Runnable {
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {

				sendCurrent();

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	class playStream implements Runnable {
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				if ((downloadStatus == 1 || downloadStatus == 2) && isStream
						&& isPlaying == 1
						&& real - mp.getCurrentPosition() < 2000) {
					mp.pause();
					bufferMax = real - mp.getCurrentPosition();
					Log.d("TEST", "buffering...");

					while ((downloadStatus == 1 || downloadStatus == 2)
							&& isStream && isPlaying == 1
							&& real - mp.getCurrentPosition() < 2000) {
						isBuffer = true;
						bufferCur = real - mp.getCurrentPosition();

					}
					isBuffer = false;

					mp.start();

				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

		}
	}

	// public void copyfile(File fromFile, File toFile){
	//
	// try {
	//
	// InputStream fosfrom = new FileInputStream(fromFile);
	//
	// OutputStream fosto = new FileOutputStream(toFile);
	//
	// byte bt[] = new byte[1024];
	//
	// int c;
	//
	// while ((c = fosfrom.read(bt)) > 0) {
	//
	// fosto.write(bt, 0, c);
	//
	// }
	//
	// fosfrom.close();
	// fosto.close();
	//
	// } catch (Exception e){
	//
	// }
	// }
	public void playerReady() {
		isReady = false;
		try {
			mp = new MediaPlayer();
			mp.reset();
			mp.setDataSource(playingPath);
			mp.prepare();
			isReady = true;
			Log.d("TEST", "ready");
		} catch (IllegalArgumentException e1) {
			Log.d("TEST", "player-ready-error1");
			e1.printStackTrace();
			playerReady();
		} catch (IllegalStateException e1) {
			Log.d("TEST", "player-ready-error2");
			e1.printStackTrace();
			playerReady();
		} catch (IOException e1) {
			Log.d("TEST", "player-ready-error3");
			e1.printStackTrace();
			playerReady();
		}

	}

	public void updateProgress() {

		if (mode == 0) {
			if (isReady)
				current = mp.getCurrentPosition();
			real = duration;
			if (isReady)
				duration = mp.getDuration();

//			 DLStatus="Local Music";
//			 total=1;
//			 downloaded=1;
//			 precent=1;
//			 speed=0;
		} else {
			if (mode == 1) {
				if (isReady)
					current = mp.getCurrentPosition();
				if (isReady)
					real = getRealTime();
				if (precent != 0)
					duration = (int) ((double) real / (double) precent);

			}
			if (mode == 2) {
				if (isReady)
					current = mp.getCurrentPosition();
				if (isReady)
					real = (int) ((double) mp.getDuration() * (double) precent);
				if (isReady)
					duration = mp.getDuration();
			}

		}

		// if(downloadStatus==-1){
		// DLStatus="Download Error";
		// }
		// else if(downloadStatus==0){
		// DLStatus="Start Download";
		//
		// }
		// else if(downloadStatus==1){
		// DLStatus="Downloading..."+(int)(precent*100)+"%.";
		//
		// }
		// else if(downloadStatus==2){
		// DLStatus="Download Paused..."+(int)(precent*100)+"%.";
		//
		// }
		// else if(downloadStatus==3){
		// DLStatus="Download Complete.";
		//
		// }
		// else if(downloadStatus==4){
		// DLStatus="Download Canceled.";
		//
		// }

	}

	protected BroadcastReceiver r = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d("TEST", "get:" + intent.getAction());

			if (intent.getAction().equals("PLAY")) {
				if (!isStream || precent > 0.05) {
					playerReady();
					mp.seekTo(current);
					mp.start();
					isPlaying = 1;
					detect = true;
				}
			}
			if (intent.getAction().equals("PAUSE")) {
				mp.pause();
				isPlaying = 2;
				detect = false;

			}
			if (intent.getAction().equals("FORWARD")) {

				streamSeekto(current + 5000);

			}
			if (intent.getAction().equals("REWIND")) {

				streamSeekto(current - 5000);

			}

			if (intent.getAction().equals("LAST")) {
				if (!isStream) {
					mp.stop();
					mp.reset();
					isReady = false;
					Cursor c = switchMusic(-1);
					if (c != null) {
						playingPath = c.getString(5);
						refreshSD();
						playerReady();
						if (isPlaying == 1) {
							mp.start();
						}
					} else {
						resetInfo();
						sendEndofList();
					}

				}

			}

			if (intent.getAction().equals("NEXT")) {
				if (!isStream) {
					mp.stop();
					mp.reset();
					isReady = false;
					Cursor c = switchMusic(1);
					if (c != null) {
						playingPath = c.getString(5);
						refreshSD();
						playerReady();
						if (isPlaying == 1) {
							mp.start();
						}
					} else {
						resetInfo();
						sendEndofList();
					}

				}

			}

			if (intent.getAction().equals("PROGRESS_CHANGE")) {
				int progress = intent.getIntExtra("Progress", current);
				streamSeekto(progress);
			}

			if (intent.getAction().equals("PAUSEDL")) {
				if (downloadStatus == 1)
					downloadStatus = 2;
				else if (downloadStatus == 2)
					downloadStatus = 1;
			}

			if (intent.getAction().equals("CANCELDL")) {
				if (downloadStatus == 1 || downloadStatus == 2) {
					if (downloadThread != null) {
						downloadThread.interrupt();

						File file = new File(downloadPath);
						if (file.exists()) {
							file.delete();
							Log.d("TEST", "delete-test-ok");
						}
					}
					resetInfo();
					downloadStatus = 4;
					mp.reset();
					 total=1;
					 downloaded=0;
					 precent=0;
					 speed=0;
				} else if (downloadStatus == 4) {
					downloadStatus = 1;
					downloadThread = new Thread(new download());
					downloadThread.start();

				}
			}

			if (intent.getAction().equals("DELETE")) {
				if (downloadStatus == 1 || downloadStatus == 2) {
					if (downloadThread != null) {
						downloadThread.interrupt();
						if (downloadPath != null) {
							File file = new File(downloadPath);
							Log.d("TEST", downloadPath + "is DLing,try delete");
							while (file.exists()) {
								file.delete();
								Log.d("TEST", "delete-test-ok");
							}
							manager.deleteMusicByPath(downloadPath);
						}
					}

					downloadStatus = 4;

				}
				resetInfo();
				if (mp != null) {
					mp.stop();
					mp.reset();
					// mp.release();
					// mp=null;
				}

				downloadPath = null;
			}
			if (intent.getAction().equals("RATING")) {
				rating = intent.getIntExtra("Rate", 0);
				Log.d("TEST", "set-rating:" + rating);
				manager.setMusicRate(playingPath, rating);

			}

			if (intent.getAction().equals("SINGLE")) {
				isSingle = intent.getBooleanExtra("isSingle", false);

			}

			if (intent.getAction().equals("LOOP")) {
				isLoop = intent.getBooleanExtra("isLoop", false);

			}

			if (intent.getAction().equals("RESUMELOCAL")) {

			}
			if (intent.getAction().equals("RESUMEDOWNLOAD")) {
				if (!playingPath.equals(downloadPath)) {
					mode = DLmode;
					isStream = true;
					playingPath = downloadPath;
					refreshSD();
					playerReady();
					isPlaying = 2;
				}

			}

		}

	};

	public void sendEndofList() {
		Intent intent = new Intent();
		intent.setAction("END");
		sendBroadcast(intent);
	}

	protected BroadcastReceiver sdr = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d("TEST", "get:" + intent.getAction());

			if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				Log.d("TEST", "scanner=finished");
				updateMusicInfo();
			}
		}

	};

	public int getRealTime() {
		MediaPlayer mpt = new MediaPlayer();
		mpt.reset();
		try {
			mpt.setDataSource(playingPath);
			mpt.prepare();
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		} catch (IllegalStateException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return mpt.getDuration();
	}

	public void sendCurrent() {
		updateProgress();
		Intent intent = new Intent();
		intent.setAction("Current");
		intent.putExtra("Current", current);
		intent.putExtra("Real", real);
		intent.putExtra("Duration", duration);
		intent.putExtra("isPlaying", isPlaying);
		intent.putExtra("Precent", precent);
		intent.putExtra("downloadStatus", downloadStatus);
		intent.putExtra("DLCurrent", downloaded);
		intent.putExtra("DLTotal", total);
		intent.putExtra("Title", title);
		intent.putExtra("Artist", artist);
		intent.putExtra("Album", album);
		intent.putExtra("Year", year);
		intent.putExtra("downloadPath", downloadPath);
		intent.putExtra("playingPath", playingPath);
		intent.putExtra("Speed", speed);
		intent.putExtra("isBuffer", isBuffer);
		intent.putExtra("max", bufferMax);
		intent.putExtra("cur", bufferCur);
		intent.putExtra("Rating", rating);
		intent.putExtra("isStream", isStream);
		sendBroadcast(intent);
	}

	public void resetInfo() {
		current = 0;
		real = 0;
		duration = 0;
		isPlaying = 2;
		// DLStatus="";
		// downloadStatus=0;
		// downloaded=0;
		// total=0;
		// precent=0;
		// speed=0;
		title = "";
		artist = "";
		album = "";
		year = "";
		isReady = false;

		rating = 0;
		// path="";
	}

	public void refreshSD() {
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://"
						+ Environment.getExternalStorageDirectory()
								.getAbsolutePath())));
	}

	public void updateMusicInfo() {
		if (isStream) {
			Cursor c = context.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Audio.Media._ID,
							MediaStore.Audio.Media.TITLE,
							MediaStore.Audio.Media.ARTIST,
							MediaStore.Audio.Media.ALBUM,
							MediaStore.Audio.Media.YEAR,
							MediaStore.Audio.Media.DATA

					}, null, null, null);

			Log.d("TEST", "get-" + c.getCount() + "music");
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				Log.d("TEST", "get-" + c.getString(5).substring(4) + "is "
						+ playingPath + "???");
				if (c.getString(5).substring(4).equals(playingPath)) {
					break;
				}
				c.moveToNext();
			}
			manager.InsertNew(c);

			// Log.d("TEST","ALBUM_ART:"+c.getString(6));
		}

		Cursor c1 = manager.getMusicByPath(playingPath);
		c1.moveToFirst();
		Log.d("TEST", "count:" + c1.getCount());
		if (c1.getCount() > 0) {
			title = c1.getString(1);
			artist = c1.getString(2);
			album = c1.getString(3);
			year = c1.getString(4);
			rating = c1.getInt(6);
		}

	}

	public Cursor getLocalMusic(int type, String info, int id,int avaliable) {
		if(avaliable==1){
		Cursor c = getList(type, info);
		c.moveToFirst();
		listNum = c.getCount();
		if (id < c.getCount() && id >= 0) {
			for (int i = 0; i < id; i++) {
				c.moveToNext();
			}
		}
		return c;
		}
		 else {
			 return null;
		}
		
	}

	public Cursor switchMusic(int direction) {
		// direction=-1.....Last Music
		// direction=1.....Next Music
		if (direction == 1 && isSingle && isLoop) {
			id = id;
		} else if (direction == 1 && !isSingle && isLoop) {
			if (id < listNum - 1) {
				id = id + 1;
			} else if (id == listNum - 1) {
				id = 0;
			}
		} else if (direction == 1 && !isSingle && !isLoop) {
			if (id < listNum - 1) {
				id = id + 1;
			} else if (id == listNum - 1) {
				return getLocalMusic(type, info, id,0);
			}
		} else if (direction == 1 && isSingle && !isLoop) {
			return getLocalMusic(type, info, id,0);
		}

		else if (direction == -1 && isSingle && isLoop) {
			id = id;
		} else if (direction == -1 && !isSingle && isLoop) {
			if (id > 0) {
				id = id - 1;
			} else if (id == 0) {
				id = listNum - 1;
			}
		} else if (direction == -1 && !isSingle && !isLoop) {
			if (id > 0) {
				id = id - 1;
			} else if (id == 0) {
				return getLocalMusic(type, info, id,0);
			}
		} else if (direction == -1 && isSingle && !isLoop) {
			return getLocalMusic(type, info, id,0);
		}
		return getLocalMusic(type, info, id,1);

	}

	public void streamSeekto(int pos) {
		isReady = false;
		mp.stop();
		mp.reset();

		playerReady();

		if (pos <= real && pos >= 0) {
			mp.seekTo(pos);

		} else if (pos > real) {
			mp.seekTo(real);
		} else if (pos < 0) {
			mp.seekTo(0);
		}
		if (isPlaying == 1) {
			mp.start();
		}

	}

	public void onCompletion(MediaPlayer arg0) {
		if (!isStream) {
			mp.stop();
			mp.reset();
			isReady = false;
			Cursor c = switchMusic(1);
			if (c != null) {
				playingPath = c.getString(5);
				refreshSD();
				playerReady();
				if (isPlaying == 1) {
					mp.start();
				}
			} else {
				resetInfo();
				sendEndofList();
			}

		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}