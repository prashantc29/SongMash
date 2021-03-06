package com.songmash.songmash;

import com.songmash.songmash.battlemanager.BattleManager;
import com.songmash.songmash.constants.StringConstants;
import com.songmash.songmash.datastructure.Battle;
import com.songmash.songmash.filesystem.NoMp3sFoundException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static BattleManager battleManager = null;
	private static int battleNum = 1; 
	private static boolean noMp3sPresent = false;
	private Battle currentBattle = null;
	private ProgressDialog pd;
	private Activity currentActivity = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Async task to initialize battlemanager which involves reading all the mp3 files from the SD card. 
		AsyncTask<Void, Void, Void> battleManagerInitializerTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(currentActivity);
				pd.setTitle(getString(R.string.mp3Read));
				pd.setMessage(getString(R.string.pleaseWait));
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				try {
					battleManager = BattleManager.getInstance();
				} catch (NoMp3sFoundException e) {
					noMp3sPresent = true;
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if (pd!=null) {
					pd.dismiss();
				}
				if(noMp3sPresent) {
					// If no mp3s found start no Mp3s found activity.
					Intent intent = new Intent(getApplicationContext(), NoMp3Activity.class);
				    startActivity(intent);
				}else {				
					nextBattle();
				}
			}
			
		};
		
		battleManagerInitializerTask.execute((Void[])null);
		
		// View ranked button activity starts the RankDisplay activity
		final Button viewRankedListButton = (Button)findViewById(R.id.button3);
		viewRankedListButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Start rankDisplayActivity
				Intent intent = new Intent(getApplicationContext(), RankDisplayActivity.class);
			    startActivity(intent);

				
			}
		});
		
		// Update rating for song1 when button1 is pressed
		final Button button1 = (Button)findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(currentBattle != null) {
					battleManager.updateRating(currentBattle.getSong1(), currentBattle.getSong2(), 100, 0);
				}
				nextBattle();
			}
		});
		
		// Update the rating for song2 when button2 is pressed.
		final Button button2 = (Button)findViewById(R.id.button2);
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(currentBattle != null) {
					battleManager.updateRating(currentBattle.getSong1(), currentBattle.getSong2(), 0, 100);
				}
				nextBattle();
			}
		});
		
	}

	private void nextBattle() {
		if(noMp3sPresent) {
			return;
		}
		Battle battle = battleManager.nextBattle();
		if(battle == null) {
			// Start rankDisplayActivity
			Intent intent = new Intent(getApplicationContext(), RankDisplayActivity.class);
			intent.putExtra(StringConstants.ARE_BATTLES_DONE, true);
		    startActivity(intent);
		    return;
		}
		currentBattle = battle;
		TextView song1View = (TextView)findViewById(R.id.Song1);
		TextView song2View = (TextView)findViewById(R.id.Song2);
		song1View.setText(battleManager.getSong(battle.getSong1()).getTitle());
		song2View.setText(battleManager.getSong(battle.getSong2()).getTitle());
		// Set the header
		TextView header = (TextView)findViewById(R.id.header);
		header.setText(getString(R.string.battle) + " " + battleNum);
		battleNum++;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
