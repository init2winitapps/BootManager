package com.drx2.bootmanager.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;

public class Utilities {
	
	/**========================== Variables =====================**/
	private static Process rt;
	
	//DO NOT PUT / AT THE BEGINNING WHEN DOWNLOADING IT'S THERE ALREADY
	//Live URL (V3.0)
	public static String url = new String("http://bootmanager.gflam.com/bootmanager/V3.0/");
	
	public final String BASE_URL = url;
	//Test URL
	//public static String url = new String(tpyrced("aHR0cDovL3Rlc3QuaW5pdDJ3aW5pdGFwcHMuY29tL2Jvb3RtYW5hZ2VyLw=="));
	//public final String BASE_URL = url;
	Context context;
	
	public final static String device(){
		return android.os.Build.BOARD;
	}
	
	public final File getExternalDirectory(){
		if(device().contains("vigor")||device().contains("ruby")||device().contains("holiday")){
			File useExternal = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete");
			if(useExternal.exists())
				if(readFirstLineOfFile(useExternal.getAbsolutePath()).contains("external"))
					return new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/ext_sd");
				else
					return Environment.getExternalStorageDirectory().getAbsoluteFile();
			else
				return Environment.getExternalStorageDirectory().getAbsoluteFile();
		} else if(device().contains("spyder")||device().contains("maserati")||device().contains("targa")||device().contains("solana")){
			File useExternal = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete");
			if(useExternal.exists())
				if(readFirstLineOfFile(useExternal.getAbsolutePath()).contains("external"))
					return new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"-ext");
				else
					return Environment.getExternalStorageDirectory().getAbsoluteFile();
			else
				return Environment.getExternalStorageDirectory().getAbsoluteFile();
		} else if(device().equals("tuna")){
			if(new File("/bootmanager/media").exists()){
				if(!new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/phoneRom/boot.img").exists()){
					if(new File("/bootmanager/media/BootManager/phoneRom/boot.img").exists()){
						return new File("/bootmanager/media");
					} else {
						return Environment.getExternalStorageDirectory().getAbsoluteFile();
					}
				} else {
					return Environment.getExternalStorageDirectory().getAbsoluteFile();
				}
			} else {
				return Environment.getExternalStorageDirectory().getAbsoluteFile();
			}
		}else{
			return Environment.getExternalStorageDirectory().getAbsoluteFile();
		}
	}
	
	/**========================== Methods =====================**/
	
	public static String readFirstLineOfFile(String filename) {
        BufferedReader br;
        String output = null;

        try {
            br = new BufferedReader(new FileReader(filename), 512);
            try {
                output = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
           
        }
        if(output==null)output="null";
        return output;
    }
	
	/** Error Dialog **/
	public void errorDialog(Context context, String title, String message){
		CustomDialog.Builder builder = new CustomDialog.Builder(context);
		builder.setTitle(title)
			   .setMessage(message)
		       .setCancelable(false)
		       .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       }).show();
	}
	
	/** Mover **/
	public void moveFile(String src, String dest){
	    File file = new File(src);
	    File dir = new File(dest);
	    boolean success = file.renameTo(new File(dir, file.getName()));
	    if (!success) {
	        
	    }
	}
	
	public static FileInputStream rootFileInputStream(String file){
		FileInputStream is = null;
		Process process;
		try {
            Runtime rt = Runtime.getRuntime();
            process = rt.exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream()); 
            os.writeBytes("cat '"+file+"'\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            //process.waitFor();
        } catch (IOException e) {
        	return is;
        //} catch (InterruptedException e) {
        	//return is;
        }
		return (FileInputStream) process.getInputStream();
	}
	
	/** Copier **/
	public void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);
	    
	    byte[] buf = new byte[4096];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
	
	/** Zipper **/
	public void zipper(String[] filenames, String zipfile){
		byte[] buf = new byte[4096];
		try {
		    String outFilename = zipfile;
		    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
		    for (int i=0; i<filenames.length; i++) {
		        FileInputStream in = new FileInputStream(filenames[i]);
		        File file = new File(filenames[i]);
		        out.putNextEntry(new ZipEntry(file.getName()));
		        int len;
		        while ((len = in.read(buf)) > 0) {
		            out.write(buf, 0, len);
		        }
		        out.closeEntry();
		        in.close();
		    }
		    out.close();
		} catch (IOException e) {
		}

	}
	
	/** Fix Permission **/
	public void fixPerm(){
    	execCommand("busybox mount -o rw,remount /system");
    	execCommand("busybox chmod 644 /data/app/*");
    	execCommand("busybox chmod 644 /data/app-private/*");
    	execCommand("busybox chmod 644 /system/app/*");
    	execCommand("busybox chmod 751 /data/data/*");
    	execCommand("busybox chmod 777 /data/data/*/*");
    	execCommand("busybox chmod 775 /data/data/*/lib");
    	execCommand("busybox chmod 771 /data/data/*/shared_prefs");
    	execCommand("busybox chmod 771 /data/data/*/databases");
    	execCommand("busybox chmod 771 /data/data/*/cache");
    	execCommand("busybox chmod 775 /data/data/cache");
		execCommand("busybox chmod 555 /mnt/asec/*");
		execCommand("busybox chmod 555 /mnt/asec/*/*");
		execCommand("buysbox chmod 075 /mnt/secure/*");
    	execCommand("busybox mount -o ro,remount -t yaffs2 /dev/block/mtdblock3 /system");
    }
	
	/** Run Su Command **/
	public Boolean execCommand(String command) 
    {
		Process process;
        try {
            Runtime rt = Runtime.getRuntime();
            process = rt.exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream()); 
            os.writeBytes(command + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            os.close();
        } catch (IOException e) {
        	return false;
        } catch (InterruptedException e) {
        	return false;
        }
        return true; 
    }
	
	public Boolean dontwait(String command) 
    {
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream()); 
            os.writeBytes(command + "\n");
            os.flush();
            //os.writeBytes("exit\n");
            //os.flush();
        } catch (IOException e) {
        	return false;
        }
        return true; 
    }
	
	/** Downloader **/
	public boolean downloadUtil(Context context, String DOWNLOAD_URL, String OUTPUT_NAME)
	{
		try {
			log(OUTPUT_NAME);
			BufferedInputStream in = new BufferedInputStream(new URL(DOWNLOAD_URL).openStream());
			FileOutputStream fos = new FileOutputStream(OUTPUT_NAME);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 4096);
			byte[] b = new byte[4096];
			int data = 0;
			while((data = in.read(b, 0, 4096)) >= 0) {
				bos.write(b, 0, data);
			}
			bos.close();
			in.close();
		} catch (IOException e) {
			log(e.toString());
			return false;
		} 
		return true;
	}
	
	public void downloadUtil2(Context context, String DOWNLOAD_URL, String OUTPUT_NAME) {
		log(OUTPUT_NAME);
		ShellCommand s = new ShellCommand();
		CommandResult r = s.su.runWaitFor(context.getFilesDir().getAbsolutePath()+"/wget "+DOWNLOAD_URL+" -O "+OUTPUT_NAME);
		if(r.stdout!=null){
			log(r.stdout);
		}
		if(r.stderr!=null){
			log(r.stderr);
		}
	}	
	
	/** Request Root **/
	public boolean requestRoot()
	{
		try {
			rt = Runtime.getRuntime().exec("su");
			DataOutputStream dos = new DataOutputStream(rt.getOutputStream());
			dos.writeBytes("exit\n");
			dos.flush();
			try {
				rt.waitFor();
			} catch (InterruptedException e) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	/** Unzipper 2 **/
	public void unzip(String src, String dest, String filestoextract, Context context){
		final int BUFFER_SIZE = 4096;
		File dir = new File("/data/local/tmp/system");
		File dirpar = new File("/data/local/tmp");
		BufferedOutputStream bufferedOutputStream = null;
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(src); 
			ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
			ZipEntry zipEntry;
			while ((zipEntry = zipInputStream.getNextEntry()) != null){
				String zipEntryName = zipEntry.getName();
				File file = new File(dest + zipEntryName);
				if((zipEntry.getName()).contains(filestoextract)) {
					if(!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
						File directory = file.getParentFile(); 
						if(!directory.equals("/data/local/tmp") || !directory.equals("/data/local")){
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory);
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+directory);
							//System.out.println("setting permissions on "+directory+" folder");
						}
						if(!directory.getParentFile().equals("/data/local/tmp") || !directory.getParentFile().equals("/data/local")){
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory.getParentFile());
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+directory.getParentFile());
							//System.out.println("setting permissions on "+directory.getParentFile()+" folder");
						}
						if(!directory.getParentFile().getParentFile().equals("/data/local/tmp") || !directory.getParentFile().getParentFile().equals("/data/local")){
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory.getParentFile().getParentFile());
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+directory.getParentFile().getParentFile());
							//System.out.println("setting permissions on "+directory.getParentFile().getParentFile()+" folder");
						}
						if(directory.getParentFile().exists()){
							if(!(dirpar.equals(directory.getParentFile()))){
								execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory.getParentFile());
								execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+directory.getParentFile());
								dirpar=directory.getParentFile();
							}
						}
					}else{
						File directory = file.getParentFile();
						if(!(directory.equals(dir))){
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory);
							dir=directory;
						}
					}
					if (file.exists()){
		        
					}else{
						if(zipEntry.isDirectory()){ 
							file.mkdirs();
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/"+zipEntry);
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /data/local/tmp/"+zipEntry);
						}else{	
							byte buffer[] = new byte[BUFFER_SIZE];
							FileOutputStream fileOutputStream = new FileOutputStream(file);
							bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
							int count;
							while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
								bufferedOutputStream.write(buffer, 0, count);
							}
							//System.out.println("unizipping "+file);
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+file);
							execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 755 "+file);
							bufferedOutputStream.flush();
							bufferedOutputStream.close(); 
						}
					}
				} 
			}
			zipInputStream.close();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void zipper2(String dir2zip, String outFileName, String romslot){
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFileName));
			zipDir(dir2zip, zos, romslot);
			zos.flush();
			zos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void zipDir(String dir2zip, ZipOutputStream zos, String romslot) { 
		byte[] readBuffer = new byte[4096];
		int bytesin = 0;
	    try  { 
	    	
	        File  zipDir = new File(dir2zip); 
	        //get a listing of the directory content 
	        String[] dirList = zipDir.list(); 
	        
	        //loop through dirList, and zip the files 
	        for(int i=0; i<dirList.length; i++) { 
	            File f = new File(zipDir, dirList[i]);
	            if(f.isDirectory())  { 
	                //if the File object is a directory, call this 
	                //function again to add its content recursively 
	            	String filePath = f.getPath(); 
	            	zipDir(filePath, zos, romslot); 
	                //loop again 
	            continue; 
	            } 
	            //if we reached here, the File object f was not 
	            //a directory 
	            //create a FileInputStream on top of f 
	        FileInputStream fis = new FileInputStream(f); 
	            //create a new zip entry 
	        String file = (f.getPath().substring(romslot.length()));
	        ZipEntry anEntry = new ZipEntry(file); 
	        zos.putNextEntry(anEntry); 
	        
	        //System.out.println(file);
	        
	            //now write the content of the file to the ZipOutputStream 
	        while ((bytesin = fis.read(readBuffer)) > 0) {
	            zos.write(readBuffer, 0, bytesin);
	        }
	           //close the Stream 
	           zos.flush();
	           zos.closeEntry();
	           fis.close();
	    } 
	} 
	catch(Exception e) 
		{ 
	   	System.out.println(e);//handle exception 
		}
	}
	
	public boolean appInstalledOrNot(Context context, String uri){
		PackageManager pm = context.getPackageManager();
		boolean app_installed = false;
		try{
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		}catch (PackageManager.NameNotFoundException e){
			app_installed = false;
		}
        return app_installed ;
    }
	
	//called from getMD5()
	public static byte[] createChecksum(String filename) throws Exception {
		InputStream fis =  new FileInputStream(filename);
		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
			fis.close();
			return complete.digest();
	}

	//Call this to get md5
	public static String getMD5(String filename) throws Exception {
		byte[] b = createChecksum(filename);
		String result = "";
		for (int i=0; i < b.length; i++) {
			result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}
		
	public void log(String message){
		File log = new File(getExternalDirectory()+"/BootManager/log.txt");
		System.out.println(message);
		try {
			if(!log.exists()){
				log.createNewFile();
			}
			FileWriter out = new FileWriter(log, true);
			out.write(message+"\n");
			out.flush();
			if(message.contains("closelogfile")){
				out.write("closing log");
				out.close();
			}
		}catch (IOException e){
            e.printStackTrace();
        }
    }
		
	public boolean checkbinarys(String file, String md5){
		try {
			if(!Utilities.getMD5(file).matches(md5)) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
		
	public boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
		
	public void fixfilesystem(String rom, Context context){
		fixThread threadwipe = new fixThread(rom, context);
		threadwipe.start();
	}
	public class fixThread extends Thread{
		String rom;
		Context context;
		CustomProgressDialog cpd = null;
		public fixThread(String ROM, Context context){
			this.rom = ROM;
			this.context = context;
			cpd = CustomProgressDialog.show(context, "Fixing filesystems", "Please wait ...", true, false);
		}
		@Override
		public void run() {
			try {
				//cpd = CustomProgressDialog.show(context, "Fixing filesystems", "Please wait ...", true, false);
				ShellCommand s = new ShellCommand();
				log("Attempting to fix corrupted filesystem");
				CommandResult fixsys=s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/e2fsck -y " + getExternalDirectory() + "/BootManager/" + rom + "/cache.img");
				if(fixsys.stderr!=null){
					log(fixsys.stderr);
				}
				if(fixsys.stdout!=null){
					log(fixsys.stdout);
				}
				CommandResult fixsys2=s.su.runWaitFor("/e2fsck -y " + getExternalDirectory() + "/BootManager/" + rom + "/cache.img");
				if(fixsys2.stderr!=null){
					log(fixsys2.stderr);
				}
				if(fixsys2.stdout!=null){
					log(fixsys2.stdout);
				}
				CommandResult fixdata=s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/e2fsck -y " + getExternalDirectory() + "/BootManager/" + rom + "/data.img");
				if(fixdata.stderr!=null){
					log(fixdata.stderr);
				}
				if(fixdata.stdout!=null){
					log(fixdata.stdout);
				}
				CommandResult fixdata2=s.su.runWaitFor("/e2fsck -y " + getExternalDirectory() + "/BootManager/" + rom + "/data.img");
				if(fixdata2.stderr!=null){
					log(fixdata2.stderr);
				}
				if(fixdata2.stdout!=null){
					log(fixdata2.stdout);
				}
				CommandResult fixcache=s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/e2fsck -y " + getExternalDirectory() + "/BootManager/" + rom + "/system.img");
				if(fixcache.stderr!=null){
					log(fixcache.stderr);
				}
				if(fixcache.stdout!=null){
					log(fixcache.stdout);
				}
				CommandResult fixcache2=s.su.runWaitFor("/e2fsck -y " + getExternalDirectory() + "/BootManager/" + rom + "/system.img");
				if(fixcache2.stderr!=null){
					log(fixcache2.stderr);
				}
				if(fixcache2.stdout!=null){
					log(fixcache2.stdout);
				}
			}finally{
				if(cpd!=null)
					cpd.dismiss();
   		   	}
		}
	}
	
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] {" Bytes", "KB", "MB", "GB", "TB"};
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + units[digitGroups];
	}
	
    public boolean checkIfNumber(String in) {
    	try {
    		Integer.parseInt(in);
    	} catch (NumberFormatException ex) {
    		return false;
    	}
    	return true;
    }
    
    public void writeFile(String file, String message){
    	  try {
    		  Writer output = null;
    		  output = new BufferedWriter(new FileWriter(new File(file)));
    		  output.write(message);
    		  output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}