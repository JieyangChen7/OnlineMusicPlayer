package com.app.jieyangchen.onlinemusicplayer;

import java.io.File;
import java.io.IOException;


import com.app.jieyangchen.onlinemusicplayer.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

public class MusicUI extends Activity {
	private String _PAUSE_DOWNLOAD="暂停下载";
	private String _RESUME_DOWNLOAD="恢复下载";
	private String _CANCEL_DOWNLOAD="取消下载";
	private String _RESTART_DOWNLOAD="重新下载";
	private String _UNAVALIABLE="不可用";
	private String _MUSIC_LOADED="音乐已加载：";
	private String _DOWNLOAD_FILE="文件：";
	private String _DOWNLOAD_SPEED="下载速度：";
	private String _BUFFERING="已缓冲：";
	private String _DOWNLOAD_STARTING="下载即将开始";
	private String _DOWNLOADING="正在下载...";
	private String _DOWNLOADING_PAUSED="下载已暂停...";
	private String _DOWNLOAD_CANCELED="下载已取消";
	private String _DOWNLOAD_COMPLETED="下载已完成";
	private String _DOWNLOAD_ERROR="下载错误";		
	private String _END_OF_LIST="已达到列表尾";
	private String _ALL="全部";
	private String _SINGLE="单曲";
	private String _LOOP="循环";
	private String _NO_LOOP="单次";
	private String DLStatusText="";
	
	private ImageButton playButton;
//	private Button playButton;
	private ImageButton rewindButton;
	private ImageButton forwardButton;
	private ImageButton lastButton;
	private ImageButton nextButton;
	private ToggleButton single;
	private ToggleButton loop;
	private Button pauseDLButton;
	private Button cancelDLButton;
	private AudioManager mAudioManager = null;
	private SeekBar seekbar;
	private TextView currentTime;
	private TextView durationTime;
	private TextView titleText;
	private TextView artistText;
	private TextView albumText;
	private TextView buffer;
	private TextView DLStatus;
	private TextView pathText;
	private TextView yearText;
	private TextView speedText;
	private TextView allText;
	private TextView loopText;
	private RatingBar ratingbar;
	
	private TextView musicLoadedText;
	private ProgressBar progressbar;
	private ProgressBar bufferbar;
	
	private int dur=0;
	private int type=-1;
	private String info="";
	private String path;
	private boolean isPlaying=false;
	private Context context; 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		context=this;
	
		isPlaying=false;
		playButton= (ImageButton)findViewById(R.id.playButton);
		
		rewindButton = (ImageButton)findViewById(R.id.rewindButton);
		forwardButton = (ImageButton)findViewById(R.id.forwardButton);
		lastButton = (ImageButton)findViewById(R.id.lastButton);
		nextButton = (ImageButton)findViewById(R.id.nextButton);
		
		single = (ToggleButton)findViewById(R.id.Single);
		loop = (ToggleButton)findViewById(R.id.Loop);
		allText=(TextView)findViewById(R.id.textView14);
		loopText=(TextView)findViewById(R.id.textView15);
		
		
		pauseDLButton=(Button)findViewById(R.id.button2);
		cancelDLButton=(Button)findViewById(R.id.button5);
		progressbar=(ProgressBar)findViewById(R.id.progressBar1);
		bufferbar=(ProgressBar)findViewById(R.id.progressBar2);
		bufferbar.setVisibility(ProgressBar.INVISIBLE);
	
		
		seekbar= (SeekBar)findViewById(R.id.seekBar1);
		
		ratingbar= (RatingBar)findViewById(R.id.ratingBar1);
		ratingbar.setMax(10);
		currentTime= (TextView)findViewById(R.id.textView1);
		durationTime= (TextView)findViewById(R.id.textView2);
		titleText=(TextView)findViewById(R.id.textView3);
		artistText=(TextView)findViewById(R.id.textView4);
		albumText=(TextView)findViewById(R.id.textView5);
		buffer=(TextView)findViewById(R.id.textView6);
		DLStatus=(TextView)findViewById(R.id.textView7);
		yearText=(TextView)findViewById(R.id.textView8);
		musicLoadedText=(TextView)findViewById(R.id.textView9);
		speedText=(TextView)findViewById(R.id.textView10);
		pathText=(TextView)findViewById(R.id.textView11);
		buffer.setVisibility(TextView.INVISIBLE);
		allText.setText(_SINGLE);
		loopText.setText(_NO_LOOP);
		single.setChecked(false);
		loop.setChecked(false);
		
	//	mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
	//	maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//ªÒµ√◊Ó¥Û“Ù¡ø  
	//	currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
			
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				if(fromUser){
				Log.d("TEST","ratingbar:"+ratingbar.getProgress());
				Intent intent=new Intent();
				intent.setAction("RATING");
				intent.putExtra("Rate",ratingbar.getProgress() );
				sendBroadcast(intent);
				}
			}
		
			
		});
		
		Intent intent1=this.getIntent();
		if(intent1.getIntExtra("resume", -1)==1){
			
			Intent intent=new Intent();
			intent.setAction("RESUMELOCAL");
			sendBroadcast(intent);
		}
		else if(intent1.getIntExtra("resume", -1)==2){
			
			Intent intent=new Intent();
			intent.setAction("RESUMEDOWNLOAD");
			sendBroadcast(intent);
		}
		else{if(intent1.getIntExtra("isOnline", -1)==0){
			int i=intent1.getIntExtra("id", -1);
			Log.d("TEST","MUSIC-UI-get"+i);
			Log.d("TEST","try to start Service");
			Intent intent= new Intent();
			intent.putExtra("isOnline", 0);
			type=intent1.getIntExtra("type", -1);
			info=intent1.getStringExtra("info");
			intent.putExtra("type",type );
			intent.putExtra("info",info );
			intent.putExtra("id", i);
			intent.setAction("com.app.jieyangchen.onlinemusicplayer.Music_Service");
			startService(intent);
			Log.d("TEST","start Service done");
		
		}
		else if(intent1.getIntExtra("isOnline", -1)==1){
			Log.d("TEST","MUSIC_UI:"+intent1.getStringExtra("url"));
			type=4;
			Intent intent= new Intent();
			intent.putExtra("isOnline", 1);
			intent.putExtra("url",intent1.getStringExtra("url"));
			intent.setAction("com.app.jieyangchen.onlinemusicplayer.Music_Service");
			startService(intent);
			Log.d("TEST","start Service done");
		}
		}
		
		IntentFilter filter = new IntentFilter();
		
		
		filter.addAction("Current");
		filter.addAction("END");
		registerReceiver(r, filter);
	
		
		
		
		
		
		playButton.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Intent intent1=new Intent();
				if(!isPlaying){
					isPlaying=true;
				//	playButton.setText("Pause");
					playButton.setImageResource(android.R.drawable.ic_media_pause);
					intent1.setAction("PLAY");
					Log.d("TEST","button-play");
				}
				else{
					isPlaying=false;
				//	playButton.setText("Play");
					playButton.setImageResource(android.R.drawable.ic_media_play);
					intent1.setAction("PAUSE");
					Log.d("TEST","button-pause");
				}
					
				
				sendBroadcast(intent1);
				
			}

		});
	
		
		
		rewindButton.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Intent intent1=new Intent();
				intent1.setAction("REWIND");
				sendBroadcast(intent1);
				
			}

		});
		
		forwardButton.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Intent intent1=new Intent();
				intent1.setAction("FORWARD");
				sendBroadcast(intent1);
				
			}

		});
		
//		stopButton.setOnClickListener(new View.OnClickListener() {
//			
//			public void onClick(View v) {
//				Intent intent1=new Intent();
//				intent1.setAction("STOP");
//				sendBroadcast(intent1);
//				
//			}
//		});
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					//seekBarChange(progress);
				}	
			}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) {
				seekBarChange(seekBar.getProgress());
			}
		});
		
		
		pauseDLButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent1=new Intent();
				intent1.setAction("PAUSEDL");
				sendBroadcast(intent1);
				
			}
		});
		
		
		cancelDLButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent1=new Intent();
				intent1.setAction("CANCELDL");
				sendBroadcast(intent1);
				
				}
			});
		
		lastButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent1=new Intent();
				intent1.setAction("LAST");
				sendBroadcast(intent1);

				
			}
		});
		
		
		nextButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent1=new Intent();
				intent1.setAction("NEXT");
				sendBroadcast(intent1);

				
			}
		});
		
		
		single.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				
					Intent intent1=new Intent();
					intent1.setAction("SINGLE");
					intent1.putExtra("isSingle", isChecked);
					sendBroadcast(intent1);
					if(isChecked){
						allText.setText(_SINGLE);
				}
					else{
						allText.setText(_ALL);
					}
				
				
			}
		});
		
		
		loop.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
			
					Intent intent1=new Intent();
					intent1.setAction("LOOP");
					intent1.putExtra("isLoop", isChecked);
					sendBroadcast(intent1);	
					if(isChecked){
						loopText.setText(_LOOP);
					}
					else{
						loopText.setText(_NO_LOOP);
					}
				
				
			}
		});
		
		
		
		}
	

	
	
	protected BroadcastReceiver r=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("MusicInfo")){
				
				titleText.setText(intent.getStringExtra("Title"));
				artistText.setText(intent.getStringExtra("Artist"));
				albumText.setText(intent.getStringExtra("Album"));
				
				seekbar.setProgress(0);
				seekbar.setSecondaryProgress(0);
				progressbar.setProgress(0);
			}
			if(intent.getAction().equals("Current")){
				int current=intent.getExtras().getInt("Current");
				int duration=intent.getExtras().getInt("Duration");
				int real=intent.getExtras().getInt("Real");
				int downloadStatus=intent.getIntExtra("downloadStatus", 0);
				boolean isStream=intent.getBooleanExtra("isStream", false);
				double precent=intent.getDoubleExtra("Precent", 0);
				Log.d("TEST","UI-current:"+current);
				currentTime.setText(toTime(current));
				durationTime.setText(toTime(duration));
				seekbar.setMax(duration);
				seekbar.setProgress(current);
				seekbar.setSecondaryProgress(real);
				if(intent.getIntExtra("isPlaying",1)==1){
					isPlaying=true;
					//playButton.setText("Pause");
					playButton.setImageResource(android.R.drawable.ic_media_pause);
				}else{
					isPlaying=false;
				//	playButton.setText("Play");
					playButton.setImageResource(android.R.drawable.ic_media_play);
				}
				progressbar.setMax(intent.getIntExtra("DLTotal", 0));
				progressbar.setProgress(intent.getIntExtra("DLCurrent", 0));
				
				
				
				  if(downloadStatus==-1){
			   			DLStatusText=_DOWNLOAD_ERROR;
			   		}
			   		else if(downloadStatus==0){
			   			DLStatusText=_DOWNLOAD_STARTING;
			   			
			   		}
			   		else if(downloadStatus==1){
			   			DLStatusText=_DOWNLOADING+(int)(precent*100)+"%.";
			   			
			   		}
			   		else if(downloadStatus==2){
			   			DLStatusText=_DOWNLOADING_PAUSED+(int)(precent*100)+"%.";
			   			
			   		}
			   		else if(downloadStatus==3){
			   			DLStatusText=_DOWNLOAD_COMPLETED;
			   			
			   		}
			   		else if(downloadStatus==4){
			   			DLStatusText=_DOWNLOAD_CANCELED;
			   			
			   		}
				DLStatus.setText(DLStatusText);
				
				
				
				if(downloadStatus==1){
					pauseDLButton.setText(_PAUSE_DOWNLOAD);
					cancelDLButton.setText(_CANCEL_DOWNLOAD);
					pauseDLButton.setClickable(true);
					cancelDLButton.setClickable(true);
				}
				else if(downloadStatus==2){
					pauseDLButton.setText(_RESUME_DOWNLOAD);
					cancelDLButton.setText(_CANCEL_DOWNLOAD);
					pauseDLButton.setClickable(true);
					cancelDLButton.setClickable(true);
				}
				else if(downloadStatus==3){
					pauseDLButton.setText(_UNAVALIABLE);
					cancelDLButton.setText(_UNAVALIABLE);
					cancelDLButton.setClickable(false);
					pauseDLButton.setClickable(false);
				}
				else if(downloadStatus==4){
					pauseDLButton.setText(_UNAVALIABLE);
					cancelDLButton.setText(_RESTART_DOWNLOAD);
					pauseDLButton.setClickable(false);
				}
				titleText.setText(intent.getStringExtra("Title"));
				artistText.setText(intent.getStringExtra("Artist"));
				albumText.setText(intent.getStringExtra("Album"));
				yearText.setText(intent.getStringExtra("Year"));
				musicLoadedText.setText(_MUSIC_LOADED+toTime(real)+".");
				path=intent.getStringExtra("downloadPath");
				pathText.setText(_DOWNLOAD_FILE+path);
				speedText.setText(_DOWNLOAD_SPEED+intent.getIntExtra("Speed",0)+"KB/s");
				ratingbar.setProgress(intent.getIntExtra("Rating",0));
				if(intent.getBooleanExtra("isBuffer", false)){
					bufferbar.setVisibility(ProgressBar.VISIBLE);
					buffer.setVisibility(TextView.VISIBLE);
					Log.d("TEST","buffering-ui");
					double max=intent.getIntExtra("max", 0);
					double cur=intent.getIntExtra("cur", 0);
					Log.d("TEST","max:"+max+"cur:"+cur);
					int buffprecent=(int)(cur/max);
					buffer.setText(_BUFFERING+buffprecent*100+"%.");
				}else{
					bufferbar.setVisibility(ProgressBar.INVISIBLE);
					buffer.setVisibility(TextView.INVISIBLE);
					Log.d("TEST","buffering-done-ui");
				}
			}
			
			
			if(intent.getAction().equals("END")){
				Toast.makeText(context, _END_OF_LIST, Toast.LENGTH_LONG).show();
				
			
			}
		}
	};
	
	
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == event.KEYCODE_BACK) {
			//	context.unregisterReceiver(r);
				Intent intent = new Intent();
				intent.setClass(this, List.class);
				intent.putExtra("type", type);
				intent.putExtra("info", info);
			//	intent.putExtra("path", path);
				startActivity(intent);
				unregisterReceiver(r);
				finish();
			}
			return true;
	    }
	
	public void seekBarChange(int progress){
		Intent intent = new Intent();
		intent.setAction("PROGRESS_CHANGE");
		intent.putExtra("Progress", progress);
		sendBroadcast(intent);
	}
	
	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}
}
