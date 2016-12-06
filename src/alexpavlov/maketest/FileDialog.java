package alexpavlov.maketest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileDialog 
{
	private String sdDir = "";
	private Context context;
	private TextView filePathView;
	private String fileNameSelected = "";
	private String myDir = "";
	private List<String> subDirectories = null;
	private FDListener myFDListener = null;
	private ArrayAdapter<String> listAdapter = null;

	public interface FDListener 
	{
		public void onChosenDir(String chosenDir);
	}

	public FileDialog(Context c, FDListener fdListener)
	{
		context = c;
		sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		myFDListener = fdListener;

		try
		{
			sdDir = new File(sdDir).getCanonicalPath();
		}
		catch (IOException ioe)
		{
		}
	}

	public void chooseFile()
	{
		// Initial directory is sdcard directory
		if (myDir.equals(""))
		  chooseFile(sdDir);
		else
		  chooseFile(myDir);
	}

	public void chooseFile(String dir)
	{
		File dirFile = new File(dir);
		if (!dirFile.exists() || ! dirFile.isDirectory())
			dir = sdDir;

		try
		{
			dir = new File(dir).getCanonicalPath();
		}
		catch (IOException ioe)
		{
			return;
		}

		myDir = dir;
		subDirectories = getDirectories(dir);

		class fileDialogOnClickListener implements DialogInterface.OnClickListener
		{
			public void onClick(DialogInterface dialog, int item) 
			{
				String m_dir_old = myDir;
				String selected = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
				if (selected.charAt(selected.length() - 1) == '/')
				  selected = selected.substring(0, selected.length() - 1);
				
				if (selected.equals("<-"))
				   myDir = myDir.substring(0, myDir.lastIndexOf("/"));
				else
				  myDir += "/" + selected;
				
				fileNameSelected = "";
				
				if ((new File(myDir).isFile()))
				{
					myDir = m_dir_old;
					fileNameSelected = selected;
				}
				
				updateDirectory();
			}
		}

		AlertDialog.Builder dialogBuilder = createDialog(dir, subDirectories, new fileDialogOnClickListener());

		dialogBuilder.setPositiveButton("OK", new OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if (myFDListener != null)
				  myFDListener.onChosenDir(myDir + "/" + fileNameSelected);	
			}
		}).setNegativeButton("Выход", null);

		final AlertDialog dirsDialog = dialogBuilder.create();
		dirsDialog.show();
	}
	
	private List<String> getDirectories(String dir)
	{
		List<String> dirs = new ArrayList<String>();
		try
		{
			File dirFile = new File(dir);
			
			if (!myDir.equals(sdDir)) 
			  dirs.add("<-");
			
			if (!dirFile.exists() || ! dirFile.isDirectory())
			  return dirs;

			for (File file : dirFile.listFiles()) 	
			  dirs.add( file.getName() );
		}
		catch (Exception e)	{}

		Collections.sort(dirs, new Comparator<String>()
		{	
			public int compare(String o1, String o2) 
			{
				return o1.compareTo(o2);
			}
		});
		return dirs;
	}
	

	private AlertDialog.Builder createDialog(String title, List<String> listItems,
			DialogInterface.OnClickListener onClickListener)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOrientation(LinearLayout.VERTICAL);
		
		filePathView = new TextView(context);
		filePathView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		filePathView.setBackgroundColor(Color.rgb(20, 20, 20)); 
		filePathView.setTextColor(Color.WHITE);
		filePathView.setGravity(Gravity.CENTER_VERTICAL);
		filePathView.setText(title);
		filePathView.setTextSize((float) 20.0);
		titleLayout.addView(filePathView);
	
		
		dialogBuilder.setView(titleLayout);
		listAdapter = createListAdapter(listItems);
		dialogBuilder.setSingleChoiceItems(listAdapter, -1, onClickListener);
		dialogBuilder.setCancelable(false);
		return dialogBuilder;
	}

	private void updateDirectory()
	{
		subDirectories.clear();
		subDirectories.addAll( getDirectories(myDir) );
		filePathView.setText(myDir + "/" + fileNameSelected);
		listAdapter.notifyDataSetChanged();
	}

	private ArrayAdapter<String> createListAdapter(List<String> items)
	{
		return new ArrayAdapter<String>(context, android.R.layout.select_dialog_item, android.R.id.text1, items)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent) 
			{
				View v = super.getView(position, convertView, parent);
				if (v instanceof TextView)
				{
					TextView tv = (TextView) v;
					tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
				}
				return v;
			}
		};
	}
} 
