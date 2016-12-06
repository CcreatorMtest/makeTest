package alexpavlov.maketest;

import android.app.ActionBar.LayoutParams;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class QuestActivity extends Activity {
	// Тип вопроса
	enum Types {ONE, MULTI, TEXT};
	enum States {QUESTION, EXPLANATION};
	
	// динамическое добавление
	LinearLayout llMain;
	TextView tV;
	Button btnAns;
	EditText edText;
	
	String[] answers;
	String explanation;
	boolean[] rAns; // для мульти-варианта
	boolean[] curAns;
	String[] rAnsStr; // для вводимых данных
	String dirName = "default";
	String[] courses;
	
	Types type = Types.ONE;
	States state = States.QUESTION;
	String toShow = explanation;
	
	OnClickListener oclRdBtn1, oclRdBtn2, oclChb;
	String courseName = "";
	
	// Работа с базой данных
	DBHelper dbHelper;
	String[] dbQuestions;
	String[] dbTypes;
	int dbIndex = 0;
	int preNumOfQuestions; // получаем из MainActivity
	// для подсчета результата
	int numOfRightAnswers = 0;
	// на случай, если вопросов недостаточно
	int numOfQuestions = 0;
	static final String LOG_TAG = "MyLog";
	
	// файл- журнал загрузок, названия курсов
    void writeToFile() {
    	final String FILE_NAME = "courses.txt";
        Log.d(LOG_TAG, "write to file " + FILE_NAME);
 
        try {
        	String str = courseName + (char)7;
            FileOutputStream outputStream = openFileOutput(FILE_NAME, MODE_APPEND);
            outputStream.write(str.getBytes());
            outputStream.close();
 
        } catch (Exception e) {
            Log.d(LOG_TAG, "Произошла ошибка при записи");
        }
    }
    
    // загрузка случайных вопросов из существующей базы
    void getQuestions()
    {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
	  Cursor c = db.query("mytable",
                new String[] { "*" }, "courseName = ?" , 
                new String[] { courseName }, null, null,  "RANDOM() limit " + preNumOfQuestions);
	  if (!c.moveToFirst())
	    return;
	  dbTypes = new String[preNumOfQuestions];
	  dbQuestions = new String[preNumOfQuestions];
      do
      {
        dbTypes[dbIndex] = c.getString(c.getColumnIndex("type"));
        dbQuestions[dbIndex++] = c.getString(c.getColumnIndex("question"));
      } while (c.moveToNext() && dbIndex < preNumOfQuestions);
     
	  c.close();
	  db.close();
	  numOfQuestions = dbIndex;
	  if (numOfQuestions < preNumOfQuestions)
	    Toast.makeText(getApplicationContext(), "Всего будет " + numOfQuestions
	    		+ " вопросов, это полное количество вопросов в данном курсе", Toast.LENGTH_LONG).show();
	  dbIndex = 0;
    }
    
    // вывод вопросов на экран
	void askQuestion()
	{
		switch(dbTypes[dbIndex])
        {
        case "s":
        	type = Types.ONE;
        	break;
        case "d":
        	type = Types.MULTI;
        	break;
        case "a":
        	type = Types.TEXT;
        	break;
        }
	
		char c = (char)7;
		String[] quest = dbQuestions[dbIndex].split(c + "");
        
		String taskName = quest[0];
        Toast.makeText(this, "Название задачи: " + taskName, Toast.LENGTH_SHORT).show();
    	
        int questSize = quest.length;
	      
		tV.setText(quest[1]);
		RadioGroup radioGroup = new RadioGroup(this); 
	    llMain.addView(tV);
	    
	    switch (type)
	    {
	    case ONE:
	    	answers = new String[questSize - 3];
	    	for (int i = 2; i < questSize - 1; i++)
	    	  answers[i-2] = quest[i];
	    	int rightAnswer = (int)Integer.valueOf(quest[questSize-1]);
	    	explanation = quest[1] + "\nПравильный ответ: " + answers[rightAnswer];
	    	RadioButton[] RbArray = new RadioButton[answers.length];
	    	for (int i = 0; i < answers.length; i++)
	    	{
	    		RbArray[i] = new RadioButton(this); 
		    	RbArray[i].setText(answers[i]); 
		    	RbArray[i].setId(i);
		    	radioGroup.addView(RbArray[i]); 
	    	}
	    	for (int i = 0; i < answers.length; i++)
	        {
	          if (i == rightAnswer)
	        	  RbArray[i].setOnClickListener(oclRdBtn1);
	          else
	        	  RbArray[i].setOnClickListener(oclRdBtn2);
	        }
	    	llMain.addView(radioGroup);
	    	
	    	btnAns.setEnabled(false);
	    	break;
	    case MULTI:
	    	answers = new String[questSize - 3];
	    	for (int i = 2; i < questSize - 1; i++)
	    	  answers[i-2] = quest[i];
	    	int len = questSize - 3;
	    	rAns = new boolean[len];
	    	explanation = quest[1];
	    	
	    	if (!quest[questSize-1].equals("-"))
	    	{
	    		explanation += "\nПравильные ответы: ";
		    	for (int i = 0; i < quest[questSize-1].length(); i++)
		    	{
		    		String s = "" + quest[questSize-1].charAt(i);
		    		int x = (int)Integer.valueOf(s);
		    		rAns[x] = true;
		    	}
	    	
		    	for (int i = 0; i < quest[questSize-1].length(); i++)
		    	{
		    		String s = "" + quest[questSize-1].charAt(i);
		    		int x = (int)Integer.valueOf(s);
		    		explanation += (answers[x] + " ");
		    	}
	    	}
	    	else
	    	  explanation += "\nСреди ответов нет правильного";
	    	CheckBox[] ChbArray = new CheckBox[answers.length];
	    	
	    	curAns = new boolean[answers.length];
	    	for (int i = 0; i < answers.length; i++)
	    	{
	    		ChbArray[i] = new CheckBox(this); 
	    		ChbArray[i].setText(answers[i]); 
	    		ChbArray[i].setId(i);
	    		ChbArray[i].setOnClickListener(oclChb);
	    		llMain.addView(ChbArray[i]); 
	    	}
	    	break;
	    case TEXT:
	    	rAnsStr = new String[questSize - 1];
	    	for (int i = 2; i < questSize; i++)
	    	  rAnsStr[i-2] = quest[i];
	    	explanation = quest[1] + "\nПример правильного ответа: " + rAnsStr[0];
	    	edText = new EditText(this);
	    	llMain.addView(edText);
	    	break;
	    }    
	    llMain.addView(btnAns);    
	}
	
	// чтение вопросов из выбранного файла, запись в базу
	boolean readDB ()
	{
		try {
			StringBuffer fileData = new StringBuffer();
	        BufferedReader reader = new BufferedReader(
	                new FileReader(dirName));
	   
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1){
	            String readData = String.valueOf(buf, 0, numRead);
	            fileData.append(readData);
	        }
	        
	        reader.close();
	        String strTask = new String(fileData.toString());
      
	    	if (strTask.isEmpty())
	    	{
	    	  Toast.makeText(getApplicationContext(), "Файл пуст",
			    		  Toast.LENGTH_SHORT).show();
	    	  return false;
	    	}
				
	    	int i = 0;
	    	courseName = "";
	    	char c = (char)8;
	    	do 
	    	{
	    	  courseName += strTask.charAt(i);
	    	} while (strTask.charAt(i++) != c);
	    	courseName = courseName.substring(0, i - 1);
	    	strTask = strTask.substring(i);
	    	
		    	
	    	String[] quest = strTask.split(c + "");
	    	
	    	int questSize = quest.length;
	    	
	    	ContentValues cv = new ContentValues();
	   	    final SQLiteDatabase db = dbHelper.getWritableDatabase();
	   	    
	   	    db.delete("mytable", "courseName = ?", new String[]{courseName});
	   	   
	    	for (i = 0; i < questSize; i++)
	    	{  
	    		cv.put("courseName", courseName);
		   	    cv.put("type", quest[i].charAt(0) + "");
		   	    cv.put("question", quest[i].substring(1));
		   	    db.insert("mytable", null, cv);
	    	}
	    	
       } catch (FileNotFoundException e) {
         e.printStackTrace(); }
         catch (IOException e) {
	         e.printStackTrace(); }
		return true;
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question);
		Intent intent = getIntent();
		String connection = intent.getStringExtra("type");
	    dirName = intent.getStringExtra("dirName");
	    preNumOfQuestions = Integer.parseInt(intent.getStringExtra("number"));
	    llMain = (LinearLayout) findViewById(R.id.llMain);
	    tV = new TextView(this);
	    tV.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    tV.setMovementMethod(new ScrollingMovementMethod());   
	    
	    dbHelper = new DBHelper(this);
	    btnAns = new Button(this);
	    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
	    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    btnAns.setLayoutParams(lParams);
	    btnAns.setText("Далее");
	    // была произведена загрузка нового курса
	    if (connection.equals("show"))
	    {
	    	readDB();
	    	writeToFile();
	    }
	    // выбран существующий
	    else
	      courseName = dirName;
	    
	    OnClickListener oclBtn = new OnClickListener() {
            @Override
            public void onClick(View v) {
            	switch (state)
            	{
            	case QUESTION:
	                llMain.removeAllViews();
	                llMain.addView(tV);
	                llMain.addView(btnAns);
	                if (type == Types.ONE)
	                {
	                  if (toShow.equals("Правильно! =)\n"))
	                	numOfRightAnswers++;
	                }
	                if (type == Types.MULTI)
	                {
	                  boolean right = true;
	                  for (int i = 0; i < answers.length; i++)
	                	 if (rAns[i] != curAns[i])
	                	 {
	                	   right = false;
	                	   break;
	                	 }
	                  if (right)
	                  {
	                	  toShow = "Правильно! =)\n";
	                	  numOfRightAnswers++;
	                  }
	                  else
	                	  toShow = "Неправильно! =(\nВопрос: " + explanation;
	                }
	                if (type == Types.TEXT)
	                {
	                  String answer = edText.getText().toString();
		               
	                  
	                  boolean right = false;
	                  for (int i = 0; i < rAnsStr.length; i++)
	                	 if (answer.equals(rAnsStr[i]))
	                	 {
	                	   right = true;
	                	   break;
	                	 }
	                  if (right)
	                  {
	                	  toShow = "Правильно! =)\n";
	                	  numOfRightAnswers++;
	                  }
	                  else
	                	  toShow = "Неправильно! =(\nВопрос: " + explanation;
	                   
	                }  
	                tV.setText(toShow);
	                state = States.EXPLANATION;
	                dbIndex++;
	                break;
            	case EXPLANATION:
            		llMain.removeAllViews();
            		if (dbIndex < numOfQuestions)
            		{
            		  state = States.QUESTION;
            		  askQuestion();
            		}
            		else
            		{
            		  tV.setText("Тест завершен! Верных ответов: " + numOfRightAnswers + "/" + numOfQuestions);
            		  llMain.addView(tV);
            		  Button btnAgain = new Button(getApplicationContext());
            		  btnAgain.setText("Повторить");
            		  llMain.addView(btnAgain);
            		  btnAgain.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							  dbIndex = 0;
							  numOfQuestions = 0;
							  numOfRightAnswers = 0;
							  getQuestions();
							  state = States.QUESTION;
							  llMain.removeAllViews();
		            		  askQuestion();
						}
					});
            		}
            		break;
            	
            	}
            }
	    };
        oclRdBtn1 = new OnClickListener() {
            @Override
            public void onClick(View v) {
            	btnAns.setEnabled(true);
            	toShow = "Правильно! =)\n";
            }
        };
        oclRdBtn2 = new OnClickListener() {
            @Override
            public void onClick(View v) {
            	btnAns.setEnabled(true);
                toShow = "Неправильно! =(\nВопрос: " + explanation;
            }
        };
        
        oclChb = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                  curAns[v.getId()] = true;
                } else {
                	curAns[v.getId()] = false;
                }
            }
        };
        btnAns.setOnClickListener(oclBtn);
        getQuestions();
        askQuestion();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	  protected void onDestroy() {
		dbHelper.close();
	    super.onDestroy();
	   }
	
	class DBHelper extends SQLiteOpenHelper {
		 
	    public DBHelper(Context context) {
	      super(context, "myDB", null, 1);
	    }
	 
	    @Override
	    public void onCreate(SQLiteDatabase db) {
	      Log.d("MyLogs", "--- onCreate database ---");
	     
	      db.execSQL(("create table " + "mytable" + " ("
	          + "id integer primary key autoincrement,"
	    	  + "courseName text,"
	          + "type text,"
	          + "question text" + ");"));
	    }
	 
	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	 
	    }
	  }
	 
}