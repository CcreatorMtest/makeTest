package alexpavlov.maketest;

import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	String dirName; // для show
	String oldName; // для choose
	
	String courseDefault = "(не выбрано)";
	String[] courses = {courseDefault};
	final static String LOG_TAG = "MyLog";
	final static String fileName = "courses.txt";
	int questionNumber; // число вопросов в другое активити
	
	public void readFile() {
		String str = new String();
        Log.d(LOG_TAG, "readFile");
        
        FileInputStream stream = null;
        StringBuilder sb = new StringBuilder();
        String line;
        
        try {
            stream = openFileInput(fileName);
 
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                stream.close();
            }
            str = sb.toString();
            char c = (char)7;
            String[] strArr = str.split(c + "");
            Set<String> set = new LinkedHashSet<String>();
            set.add(courseDefault);
            
            if (str.length() > 1)
            {
            	for (int i = 0; i < strArr.length; i++)
                {
                  set.add(strArr[i]);
                }

            }
            courses = new String[set.size()];
            courses = set.toArray(new String[set.size()]);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Файла нет или произошла ошибка при чтении");
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btnShow = (Button) findViewById(R.id.btnShow);
		final TextView tV = (TextView) findViewById(R.id.tV);
		final Button btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setEnabled(false);
		Button btnChoose = (Button)findViewById(R.id.btnChoose);
		Button btnBase = (Button)findViewById(R.id.btnBase);
		final EditText eT = (EditText)findViewById(R.id.eT);
		questionNumber = 5;
		readFile();
		SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
		final TextView tVnumber = (TextView)findViewById(R.id.tVnumber);
		ArrayAdapter<String>adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
		 final Spinner spinner = (Spinner)findViewById(R.id.spinner);
	        spinner.setAdapter(adapter);
	        spinner.setEnabled(false);
	        spinner.setBackgroundColor(Color.BLACK);
	        // заголовок
	        //spinner.setPrompt("Title");
	        // выделяем элемент 
	        spinner.setSelection(0);
	        // устанавливаем обработчик нажатия
	        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
	      
	        	@Override
	      public void onItemSelected(AdapterView<?> parent, View view,
	          int position, long id) {
	        		String selectedItem = parent.getItemAtPosition(position).toString();
	                oldName = selectedItem;
	                
	                if (oldName.equals(courseDefault))
	                  btnStart.setEnabled(false);
	                else
	                  btnStart.setEnabled(true);
	      }

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
	    });

		
		btnBase.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				class DBDestroyer extends SQLiteOpenHelper {
					 
				    public DBDestroyer(Context context) {
				      super(context, "myDB", null, 1);
				    }
				 
				    @Override
				    public void onCreate(SQLiteDatabase db) {
				      Log.d("MyLogs", "удаляем базу");
				    }
				 
				    @Override
				    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				 
				    }
				  }

				if (eT.getText().toString().equals("Delete"))
				{
					eT.setText("");
					DBDestroyer destr = new DBDestroyer(getApplicationContext());
					final SQLiteDatabase db = destr.getWritableDatabase(); 
			   	    db.delete("mytable", null, null);
					if (!getApplicationContext().deleteFile(fileName))
			   	    	Log.d(LOG_TAG, "Файл не удален!");
			   	    Toast.makeText(getApplicationContext(), "База вопросов очищена", Toast.LENGTH_LONG).show();
			   	    destr.close();
			   	    btnStart.setEnabled(false);
			   	    courses = new String[1];
			   	    courses[0] = courseDefault;
			   	    spinner.setEnabled(false);
			   	    ArrayAdapter<String>adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, courses);
			        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner.setAdapter(adapter);
				}
			}
		});
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				questionNumber = (seekBar.getProgress() / 20 + 1) * 5;
				tVnumber.setText("Количество вопросов: " + questionNumber);	
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {	
			}
		});
		
			    
		btnShow.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				
				FileDialog fileDialog =  new FileDialog(MainActivity.this, new FileDialog.FDListener()
				{
					@Override
					public void onChosenDir(String chosenDir) 
					{
						File f = new File(chosenDir);
						if (f.isFile() && chosenDir.endsWith(".tst"))
						{
							btnStart.setEnabled(true);
							dirName = chosenDir;
							tV.setText("Выбранный файл с вопросами: " + dirName);
							spinner.setEnabled(false);
							spinner.setBackgroundColor(Color.BLACK);
						}
						else if (f.isDirectory())
						{
							Toast.makeText(MainActivity.this, chosenDir + " - папка", Toast.LENGTH_LONG).show();
						}
						else if (!chosenDir.endsWith(".tst"))
						{
							Toast.makeText(MainActivity.this, "Файл должен иметь расширение .tst", Toast.LENGTH_LONG).show();
						}
					}
				});
				fileDialog.chooseFile();
			}
		});
		
		
		btnStart.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				final String SHOW = "show";
				final String CHOOSE = "choose";
				
				Intent intent = new Intent(MainActivity.this, QuestActivity.class); 
				if (spinner.isEnabled() && !oldName.equals(courseDefault))
				{
					intent.putExtra("type", CHOOSE);
				    intent.putExtra("dirName", oldName);
				}
				else
				{
					intent.putExtra("type", SHOW);
				    intent.putExtra("dirName", dirName);
				    
			    }
				String s = questionNumber + "";
				Log.d("MyLogs", "Передаем" + s);
				intent.putExtra("number", s);
				startActivity(intent);
			  }
			});
		
		btnChoose.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				  spinner.setEnabled(true);
				  spinner.setBackgroundColor(Color.WHITE);
			    }	
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}