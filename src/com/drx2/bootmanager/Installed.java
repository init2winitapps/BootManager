package com.drx2.bootmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.drx2.bootmanager.services.BootManagerService;
import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.CustomProgressDialog;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.drx2.bootmanager.utilities.Utilities;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class Installed extends ListActivity {
	 
	ShellCommand s = new ShellCommand();
	Utilities u = new Utilities();
	private List<String> item = null;
	private List<String> path = null;
	private String root = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/";
	private String current = null;
	private String slot;
	private String installtype;
	//String bootimg;
	Button installbutton;
	private static final String PREFS_DEVICE = "DeviceInfo";
	String systemsize;
	String datasize;
	String cachesize;
	String ext = "ext2";
	Context context;
	SharedPreferences colors;
	SharedPreferences shared;
	File currentfile;
	static public ArrayList<String> queue;
	static public ArrayList<Boolean> kernel;
	static public ArrayList<String> bootimg;
	static int queueTotal = -1;
	static boolean wipesystem = false;
	static boolean wipedata = false;
	static boolean wipecache = false;
	static boolean kernelinqueue = false;
	static ArrayAdapter adapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    context = getApplicationContext();
		shared = PreferenceManager.getDefaultSharedPreferences(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    if(shared.getBoolean("themePref", false) == true){
	    	setTheme(android.R.style.Theme_Black_NoTitleBar);
	    }else{
	    	setTheme(android.R.style.Theme_Light_NoTitleBar);
	    }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.installed);
        colors = getSharedPreferences(PREFS_DEVICE, 0);
        installbutton=(Button)findViewById(R.id.installbutton);
        Bundle extras = getIntent().getExtras();
        slot = extras.getString("slot");
        installtype = extras.getString("installtype");
        LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);
		mainLayout.startAnimation(slideDown);
        mainLayout.setVisibility(LinearLayout.GONE);
        if(getIntent().getStringExtra("root") != null){
        	root = getIntent().getStringExtra("root");
        }
        if(Utilities.device().equals("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana")){
        	root = u.getExternalDirectory().toString();
        }
        try{
        	getDir(root);
        	File directory = new File(root);
        	File[] contents = directory.listFiles();
        	if (contents.length == 0) {
        		TextView t = new TextView(this); 
        		t=(TextView)findViewById(R.id.empty); 
        		t.setText(R.string.emptyFol);
        	}
        }catch (Exception e) {
        	Toast.makeText(Installed.this, R.string.nMount, Toast.LENGTH_LONG).show();
        }
        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	    actionBar.setTitle(current);
	    actionBar.setOnTitleClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	if(Utilities.device().equals("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana")){
	        		if(current.contains("/sdcard-ext")){
	        			root = Environment.getExternalStorageDirectory().toString();
	        			getDir(root);
	        		}else{
	        			root = Environment.getExternalStorageDirectory() + "-ext";
	        			getDir(root);
	        		}
	        	}
	        }
	    });
	    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	    LinearLayout l = (LinearLayout) findViewById(R.id.bottombar);
        int startcolor = colors.getInt("buttonStart", context.getResources().getColor(R.color.buttonStart));
		int endcolor = colors.getInt("buttonEnd", context.getResources().getColor(R.color.buttonEnd));
		int[] color = {startcolor, endcolor};
		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color);
		l.setBackgroundDrawable(d);
		queue = new ArrayList<String>();
		kernel = new ArrayList<Boolean>();
		bootimg = new ArrayList<String>();
		installbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				if(installtype!=null){
					if(installtype.contains("rom")){
						LinearLayout mainLayout=(LinearLayout)findViewById(R.id.bottombar);
						Animation slideUp = AnimationUtils.loadAnimation(Installed.this, R.anim.slide_down_out);
						mainLayout.startAnimation(slideUp);
						mainLayout.setVisibility(LinearLayout.GONE);
						queue.add(currentfile.getAbsolutePath());
						if(ifkernel(currentfile.getAbsolutePath())){
							CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
							builder.setTitle("Kernel or Rom?")
							.setMessage("Is this zip a kernel?")
							.setCancelable(true)
							.setPositiveButton("Kernel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									kernel.add(true);
									kernelinqueue=true;
									queueTotal = queueTotal + 1;
									if(Utilities.device().contains("shadow")||Utilities.device().contains("droid2")||Utilities.device().contains("droid2we")||Utilities.device().contains("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana")){
										bootimg.add("2ndInit");
										queueDialog();
									} else {
										queueDialog();
									}
								}
							})
							.setNegativeButton("Rom", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									kernel.add(false);
									queueTotal = queueTotal + 1;
									if(Utilities.device().contains("shadow")||Utilities.device().contains("droid2")||Utilities.device().contains("droid2we")||Utilities.device().contains("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana")){
										bootimg.add("2ndInit");
										queueDialog();
									} else {
										multipleBootsTask mbt = new multipleBootsTask();
										mbt.execute();
									}
								}
							}).show();
						}else{
							kernel.add(false);
							queueTotal = queueTotal + 1;
							if(Utilities.device().contains("shadow")||Utilities.device().contains("droid2")||Utilities.device().contains("droid2we")||Utilities.device().contains("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana")){
								bootimg.add("2ndInit");
								queueDialog();
							} else {
								multipleBootsTask mbt = new multipleBootsTask();
								mbt.execute();
							}
						}
						
					}
				}
			}
    	});
    }
	    
    private class Home implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_actionbar_home;
	    }
	    @Override
	    public void performAction(View view) {
		        getDir(root);
	    }
	}
	    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (findViewById(R.id.bottombar).isShown()){
        		LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
        		Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);
	  			mainLayout.startAnimation(slideDown);
  		      	mainLayout.setVisibility(LinearLayout.GONE);
  		      	ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	  			actionBar.setTitle(current);
			}else{
				if(!(current.equals("/mnt/sdcard") || current.equals(root))){
	        		File cur = new File(current);
	        		getDir(cur.getParentFile().toString());
	        	}else{
	        		Installed.this.finish();
	        		overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	        	}	  
			}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	    
    private void getDir(String dirPath){
    	item = new ArrayList<String>();
    	path = new ArrayList<String>();
     
    	File f = new File(dirPath);
    	File[] files = f.listFiles();
     
    	for(int i=0; i < files.length; i++){
    		File file = files[i];
    		//Problem was adding the path without the /queueDialog();
			
    		//path.add(file.getPath());
    		if(file.isDirectory()){
    			path.add(file.getPath()+"/");
    			item.add(file.getName() + "/");
    		}else if(file.getName().endsWith("zip")){
    			path.add(file.getPath());
    			item.add(file.getName());
    		}else{
    			//do nothing
    		}
    	}
	     
	    current = dirPath;
	     
	    ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(current);
		if(!(current.equals("/mnt/sdcard") || current.equals(root))){
			actionBar.removeAllActions();
			actionBar.addAction(new Home(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));	
     	}else{
     		actionBar.removeAllActions();
     	}
		
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
		setListAdapter(fileList);
		class IgnoreCaseComparator implements Comparator<String> {
			public int compare(String strA, String strB) {
				return strA.compareToIgnoreCase(strB);
			}
		}
		IgnoreCaseComparator icc = new IgnoreCaseComparator();
		java.util.Collections.sort(path,icc);
		java.util.Collections.sort(item,icc);
	}

    private boolean ifkernel(String string){
		try {
			ZipFile zipfile = new ZipFile(string);
			Enumeration<?> e = zipfile.entries();
	        while(e.hasMoreElements()){
	        	ZipEntry ze = (ZipEntry)e.nextElement();
	        	if(ze.getName().toLowerCase().endsWith("zimage")){
	        		return true;
	        	}
	        }
		} catch (ZipException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	return false;
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(path.get(position));
		if (!file.isDirectory()){
			if(file.toString().endsWith("zip")){
				LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
				Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
	  			mainLayout.startAnimation(slideUp);
				mainLayout.setVisibility(LinearLayout.VISIBLE);
				currentfile = file;
				ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	  		  	actionBar.setTitle("Install " + file.getName() + "to "+slot+"?");
			}else{
				Toast.makeText(Installed.this, file.getName() + " isn't a ROM", Toast.LENGTH_LONG).show();
			}
		}else{
			if(file.canRead()){
				getDir(path.get(position));
		  		if (findViewById(R.id.bottombar).isShown()){
		  			LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
		  			Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);
		  			mainLayout.startAnimation(slideDown);
		  			mainLayout.setVisibility(LinearLayout.GONE);
		  			ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		  			actionBar.setTitle(current);
		  		}
			}else{
				   Toast.makeText(Installed.this, file.getName() + " can't be read!", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void spaceCheck(String slot){
		u.log("checking free space");
        SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
    	systemsize=settings.getString("systemsize", "");
    	datasize=settings.getString("datasize", "");
    	cachesize=settings.getString("cachesize", ""); 
        int a = Integer.parseInt(systemsize);
        int b = Integer.parseInt(datasize);
        int c = Integer.parseInt(cachesize); 
        int sum = a + b + c;
        StatFs stat = new StatFs((u.getExternalDirectory()+"/BootManager/rom1").toString());
        double sdAvailSize = (double)stat.getAvailableBlocks() *(double)stat.getBlockSize();
        double gigaAvailable = sdAvailSize / 1048576;
        if(gigaAvailable < sum){
        	u.log("Only "+gigaAvailable+" space free");
        	CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
        	builder.setTitle("Low On Space!")
        		   .setMessage(R.string.lowspace)
        	       .setCancelable(true)
        	       .setNegativeButton(R.string.okay, new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                Installed.this.finish();
        	           }
        	       }).show();
        }else{
        	Dialog(slot);
        }
	}
	
	private void OWDialog(final String slot){
		CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
		builder.setTitle("Overwrite?")
			   .setMessage("ROM already installed to " + slot + " if you wish to overwrite make sure to wipe system, cache and data")
		       .setCancelable(true)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
						if(kernelinqueue || wipesystem == true || wipedata == true || wipecache == true){
							extDialog(slot);
						} else {
							startinstallservice(slot);
				    	    Intent i = new Intent(Installed.this, Install.class);
							startActivity(i);
							Installed.this.finish();
						}
		           }
		       })
		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   wipesystem = false;
		        	   wipedata = false;
		        	   wipecache = false;
		        	   queueTotal=-1;
		        	   kernelinqueue = false;
		        	   bootimg = new ArrayList<String>();
		        	   queue = new ArrayList<String>();
		        	   kernel = new ArrayList<Boolean>();
		        	   dialog.cancel();
		        	   Toast.makeText(context, "Queue has been reset!!", Toast.LENGTH_LONG).show();
		        	   dialog.cancel();
		           }
		       }).show();
	}
	
	private void Dialog(final String slot){
		CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
		builder.setTitle("Install to " + slot + "?")
			   .setMessage("Are you sure you want to install queue to " + slot)
		       .setCancelable(true)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	    extDialog(slot);
		           }
		       })
		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   wipesystem = false;
		        	   wipedata = false;
		        	   wipecache = false;
		        	   queueTotal=-1;
		        	   kernelinqueue = false;
		        	   bootimg = new ArrayList<String>();
		        	   queue = new ArrayList<String>();
		        	   kernel = new ArrayList<Boolean>();
		        	   dialog.cancel();
		        	   Toast.makeText(context, "Queue has been reset!!", Toast.LENGTH_LONG).show();
		        	   dialog.cancel();
		           }
		       }).show();
	}
	
	private void extDialog(final String slot){
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
		String board=settings.getString("device", "");
		if(board.contains("shadow")||board.contains("droid2")||board.contains("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
			if(board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
				ext="ext4";
			} else {
				ext="ext3";
			}
			startinstallservice(slot);
    	    Intent i = new Intent(Installed.this, Install.class);
			startActivity(i);
			Installed.this.finish();
		} else if(board.contains("tuna")){
			ext="ext4";
			startinstallservice(slot);
    	    Intent i = new Intent(Installed.this, Install.class);
			startActivity(i);
			Installed.this.finish();
		} else {
			String fsmessage = getString(R.string.filesystem);
			CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
			builder.setTitle("Which Filesystem for "+slot)
				   .setMessage(fsmessage+checkfilesystems())
			       .setCancelable(true)
			       .setPositiveButton("Ext4", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	    u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/log.txt");
			        	    ext="ext4";
			        	    startinstallservice(slot);
			        	    Intent i = new Intent(Installed.this, Install.class);
			    			startActivity(i);
			    			Installed.this.finish();
			        	    u.log("Chose ext4 format");
			        	}
			       })
			       .setNegativeButton("Ext2", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                ext="ext2";
			                startinstallservice(slot);
			        	    Intent i = new Intent(Installed.this, Install.class);
			    			startActivity(i);
			    			Installed.this.finish();
			                u.log("chose ext2 format");
			           }
			       }).show();
		}
	}
	
	class multipleBootsTask extends AsyncTask<Void, Void, Void> {
		ArrayList<CharSequence> bootimgs;
		CustomProgressDialog p;
		
		public multipleBootsTask() {
			this.p=new CustomProgressDialog(context);
			this.p = CustomProgressDialog.show(Installed.this, "Verifying Zip", "Please wait ...", true,true);
			
        }

		@Override
		protected Void doInBackground(Void... arg0) {
			try{
				SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
				String board=settings.getString("device", "");
				bootimgs = new ArrayList<CharSequence>();
				ZipFile zipfile = new ZipFile(queue.get(queueTotal));
				Enumeration<?> e = zipfile.entries();
	            while(e.hasMoreElements()){
	            	ZipEntry ze = (ZipEntry)e.nextElement();
	            	if(board.contains("vigor")){
						if(ze.getName().endsWith(".img")||ze.getName().endsWith("PH98IMG.zip")){
							bootimgs.add(ze.toString());
						}
					} else {
						if(ze.getName().endsWith(".img")){
							bootimgs.add(ze.toString());
						}
					}
	            }
			 }catch(Exception e){
			    	System.out.println(e.toString());
			        e.printStackTrace();
			 }
			 return null;
		}
		
		@Override 
		protected void onPostExecute(Void unused) {
			if(p!=null)p.dismiss();
	        if(bootimgs.size()==0){
	        	bootimg.add("none");
	        	queueDialog();
	        } else if(bootimgs.size()==1){
	        	//continue as normal
	        	bootimg.add(bootimgs.get(0).toString());
	        	u.log("Boot"+bootimgs.get(0).toString());
	        	queueDialog();
	        } else if(bootimgs.size() > 1){
	        	//multiple boot.imgs have user select
	        	CharSequence[] boots = new CharSequence[bootimgs.size()];
	        	boots=bootimgs.toArray(boots);
	        	final CharSequence[] boots2 = boots;
	        	CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
	        	builder.setTitle("Choose Boot.img");
	        	builder.setItems(boots2, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	bootimg.add((String) boots2[item]);
				    	CustomDialog.Builder builder2 = new CustomDialog.Builder(Installed.this);
						builder2.setTitle("Install?")
							   .setMessage("Install rom using "+bootimg+"?")
						       .setCancelable(true)
						       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						        	   queueDialog();
						           }
						       })
						       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						           public void onClick(DialogInterface dialog, int id) {
						        	   //Reset queue
						        	   wipesystem = false;
						        	   wipedata = false;
						        	   wipecache = false;
						        	   queueTotal=-1;
						        	   kernelinqueue = false;
						        	   bootimg = new ArrayList<String>();
						        	   queue = new ArrayList<String>();
						        	   kernel = new ArrayList<Boolean>();
						        	   dialog.cancel();
						        	   Toast.makeText(context, "Queue has been reset!!", Toast.LENGTH_LONG).show();
						           }
						       }).show();
				    }
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//Reset queue
			        	   wipesystem = false;
			        	   wipedata = false;
			        	   wipecache = false;
			        	   queueTotal=-1;
			        	   kernelinqueue = false;
			        	   bootimg = new ArrayList<String>();
			        	   queue = new ArrayList<String>();
			        	   kernel = new ArrayList<Boolean>();
			        	   dialog.cancel();
			        	   Toast.makeText(context, "Queue has been reset!!", Toast.LENGTH_LONG).show();
					}
				}).show();
	        }
	   
		}
	}
	
	private void queueDialog(){
		LayoutInflater qDialog = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View inflate = qDialog.inflate(R.layout.queuedialog, null);
		final CheckBox wsystem = (CheckBox) inflate.findViewById(R.id.checkBox1);
		final CheckBox wdata = (CheckBox) inflate.findViewById(R.id.checkBox2);
		final CheckBox wcache = (CheckBox) inflate.findViewById(R.id.checkBox3);
		if(shared.getBoolean("themePref", false) == true){
			TextView wipes = (TextView)inflate.findViewById(R.id.wipey);
			wipes.setTextColor(0xffffffff);
			wsystem.setTextColor(0xffffffff);
			wdata.setTextColor(0xffffffff);
			wcache.setTextColor(0xffffffff);
		}
		CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
		builder.setTitle("Install Queue")
			   .setContentView(inflate)
		       .setCancelable(false)//we have cancel button so changing this to false?
		       .setOnCancelListener(new OnCancelListener() {
		    	   public void onCancel(DialogInterface dialog) {
		    		  //commented this out cause just using the cancel button to reset things
		    		}
		       })
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id) {
		    		   wipesystem = wsystem.isChecked();
		    		   wipedata = wdata.isChecked();
		    		   wipecache = wcache.isChecked();
		    		   if(new File(u.getExternalDirectory() + "/BootManager/" + slot + "/system.img").exists()){
	    				   OWDialog(slot);
	    			   }else{
	    				   spaceCheck(slot);	
	    			   }
		    	   }
		       })
		       .setNeutralButton(R.string.addROM, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   wipesystem = wsystem.isChecked();
		        	   wipedata = wdata.isChecked();
		        	   wipecache = wcache.isChecked();
		        	   if(getIntent().getStringExtra("root") != null){
		        		   root = getIntent().getStringExtra("root");
		        	   }
		               if(Utilities.device().equals("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana")){
		            	   root = u.getExternalDirectory().toString();
		               }
		               getDir(root);
		               dialog.dismiss();
		           }
		       })
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   wipesystem = false;
		        	   wipedata = false;
		        	   wipecache = false;
		        	   queueTotal=-1;
		        	   kernelinqueue = false;
		        	   bootimg = new ArrayList<String>();
		        	   queue = new ArrayList<String>();
		        	   kernel = new ArrayList<Boolean>();
		               dialog.cancel();
		               Toast.makeText(context, "Queue has been reset!!", Toast.LENGTH_LONG).show();
		           }
		       });
			   ListView cList = (ListView) inflate.findViewById(R.id.queueDlist);
			   ArrayList<String> queueName = new ArrayList<String>();
			   for(String s: queue){
				   queueName.add(new File(s).getName());
			   }
			   adapter = new ArrayAdapter<String>(Installed.this, android.R.layout.simple_list_item_1, queueName);
			   cList.setAdapter(adapter);
			   //cList.setOnItemLongClickListener(longListen);
			   wsystem.setChecked(wipesystem);
			   wdata.setChecked(wipedata);
			   wcache.setChecked(wipecache);
		       builder.show();
	}
	
	OnItemLongClickListener longListen = new OnItemLongClickListener (){
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
			CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
			builder.setTitle("Remove From Queue")
				   .setMessage("Are you sure you want to remove " + queue.get(position) + "?")
			       .setCancelable(true)
			       .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   //TODO make sure this works haven't checked at all yet
			        	   queue.remove(position);
			        	   kernel.remove(position);
			        	   bootimg.remove(position);
			        	   queueTotal = queueTotal - 1;
			           }
			       })
			       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       }).show();
			return true;
		}
	};
	
	private String checkfilesystems(){
		String string;
		Boolean ext2=false;
		Boolean ext4=false;
		CommandResult cf = s.su.runWaitFor(context.getFilesDir().getAbsolutePath()+"/busybox cat /proc/filesystems");
		if(cf.stdout!=null){
			if(cf.stdout.contains("ext2")){
				ext2=true;
			}
			if(cf.stdout.contains("ext4")){
				ext4=true;
			}
		}
		if(ext2 && ext4){
			string=getString(R.string.e2_e4_support);
		} else if(ext2 && !ext4){
			string=getString(R.string.e2_support);
		} else if(!ext2 && ext4){
			string=getString(R.string.e4_support);
		} else {
			string=getString(R.string.no_fs_support); 
		}
		return string;
	}
	
	private void startinstallservice(String slot){
		Intent stopService  = new Intent(this, BootManagerService.class);
		stopService(stopService);
		Intent service = new Intent(this, BootManagerService.class);
		service.putExtra("ext", ext);
		if(wipesystem == true){
			service.putExtra("wipesystem", true);
		} else {
			service.putExtra("wipesystem", false);
		}
		if(wipedata == true){
			service.putExtra("wipedata", true);
		} else {
			service.putExtra("wipedata", false);
		}
		if(wipecache == true){
			service.putExtra("wipecache", true);
		} else {
			service.putExtra("wipecache", false);
		}
		service.putExtra("kernel", false);
		service.putExtra("slot", slot);
		startService(service);
	}
	
	private void startKernelinstallservice(String slot){
		Intent stopService  = new Intent(this, BootManagerService.class);
		stopService(stopService);
		Intent service = new Intent(this, BootManagerService.class);
		service.putExtra("ext", ext);
		service.putExtra("wipesystem", false);
		service.putExtra("wipedata", false);
		service.putExtra("wipecache", false);
		service.putExtra("kernel", true);
		service.putExtra("slot", slot);
		startService(service);
	}
	
	private void KernelextDialog(final String slot){
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
		String board=settings.getString("device", "");
		if(board.contains("tuna")){
			ext="ext4";
    	    u.log("Chose ext4 format");
    	    startKernelinstallservice(slot);
    	    Intent i = new Intent(Installed.this, Install.class);
			startActivity(i);
			Installed.this.finish();
		} else {
			CustomDialog.Builder builder = new CustomDialog.Builder(Installed.this);
			builder.setTitle("Which Filesystem for Kernel")
				   .setMessage(R.string.filesystem)
			       .setCancelable(true)
			       .setPositiveButton("Ext4", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	    u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/log.txt");
			        	    ext="ext4";
			        	    u.log("Chose ext4 format");
			        	    startKernelinstallservice(slot);
			        	    Intent i = new Intent(Installed.this, Install.class);
							startActivity(i);
							Installed.this.finish();
			        	}
			       })
			       .setNegativeButton("Ext2", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                ext="ext2";
			                u.log("chose ext2 format");
			                startKernelinstallservice(slot);
			                Intent i = new Intent(Installed.this, Install.class);
							startActivity(i);
							Installed.this.finish();
			        }
			       }).show();
		}
	}
}