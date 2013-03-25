package com.drx2.bootmanager.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;

import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;

public class PhoneRomSetup {
	CustomProgressDialog sp = null;
	String filesystem = null;

	public void setupPR(final String board, final Context context, final String boot){
		CustomDialog.Builder builder = new CustomDialog.Builder(context);
		builder.setTitle("Warning")
		.setMessage(R.string.phoneWarn)
		.setCancelable(true)
		//Toggle setIcon to show or not show the app's icon in Dialogs
		.setIcon(R.drawable.icon, true)
		.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				sp = CustomProgressDialog.show(context, "Setting Up Phone ROM", "Please wait ...", true, false);
				spThread sp = new spThread(board, context, boot);
				sp.start();
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();
	}



	public class spThread extends Thread{
		Context context;
		String board;
		String boot;
		Utilities u = new Utilities();
		ShellCommand s = new ShellCommand();

		spThread(String board, Context context, String boot){
			this.context=context;
			this.board=board;
			this.boot=boot;
		}
		@Override
		public void run() {
			if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.equals("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
				boolean secondinit = false;
				try {
					if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.equals("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
						File bootzip = new File("/system/etc/hijack-boot.zip");
						if(bootzip.exists()){
							secondinit=true;
							if(board.equals("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
								if(!(new File(Environment.getExternalStorageDirectory() + "-ext/BootManager").exists())){
									new File(Environment.getExternalStorageDirectory() + "-ext/BootManager").mkdir();
								}
								if(!(new File(Environment.getExternalStorageDirectory() + "-ext/BootManager/phoneRom").exists())){
									new File(Environment.getExternalStorageDirectory() + "-ext/BootManager/phoneRom").mkdir();
								}
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+bootzip+" "+Environment.getExternalStorageDirectory()+"-ext/BootManager/phoneRom/hijack-boot.zip");
								u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip "+ Environment.getExternalStorageDirectory() +"-ext/BootManager/phoneRom/update.zip");
								u.execCommand("echo Phone Rom > "+Environment.getExternalStorageDirectory()+"-ext/BootManager/phoneRom/name");
							}else{
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+bootzip+" "+u.getExternalDirectory()+"/BootManager/phoneRom/hijack-boot.zip");
								u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip "+ u.getExternalDirectory()+"/BootManager/phoneRom/update.zip");
								u.execCommand("echo Phone Rom > "+u.getExternalDirectory()+"/BootManager/phoneRom/name");
							}
						} else {
							((Activity) context).runOnUiThread(new Runnable() {                
								public void run() {
									CustomDialog.Builder builder2 = new CustomDialog.Builder(context);
									builder2.setTitle("Oops....")
									.setIcon(R.drawable.oops, true)
									.setMessage(R.string.requires_secondinit)
									.setCancelable(false)
									.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
											//finish();
										}
									}).show();	
								}
							});
						}
					}
				} finally {
					if(secondinit){
						File hijack = null;
						if(board.equals("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
							hijack = new File(Environment.getExternalStorageDirectory()+"-ext/BootManager/phoneRom/hijack-boot.zip");
						}else{
							hijack = new File(u.getExternalDirectory()+"/BootManager/phoneRom/hijack-boot.zip");
						}
						if(hijack.exists()){
							sp.dismiss();
							((Activity) context).runOnUiThread(new Runnable() {                
								public void run() {
									CustomDialog.Builder builder2 = new CustomDialog.Builder(context);
									builder2.setTitle("Success!!!")
									.setMessage(R.string.Xphonesucc)
									.setCancelable(false)
									.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									}).show();
								}
							});
						} else {
							sp.dismiss();
							((Activity) context).runOnUiThread(new Runnable() {                
								public void run() {
									CustomDialog.Builder builder2 = new CustomDialog.Builder(context);
									builder2.setTitle("Failed to Setup phone rom")
									.setMessage(R.string.xphonefail)
									.setCancelable(false)
									.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									}).show();
								}
							});
						}
					}
				}
			} else {
				try {
					if(board.equals("sholes")){
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox dd if=/dev/block/mtdblock2 of=" + u.getExternalDirectory() + "/BootManager/phoneRom/boot.img");
					}else if(board.equals("aloha")||board.equals("buzz")){
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/morebinarys/dump_image boot " + u.getExternalDirectory() + "/BootManager/phoneRom/boot.img");
					} else if(board.equals("tuna")){
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/dump_image /dev/block/mmcblk0p7 "+u.getExternalDirectory()+"/BootManager/phoneRom/boot.img");
					} else if(board.equals("herring")){
						CommandResult phstp = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox dd if=/dev/mtd/mtd2 of="+u.getExternalDirectory()+"/BootManager/phoneRom/boot.img");
						if(phstp.stdout!=null){
							u.log(phstp.stdout);
						}
						if(phstp.stderr!=null){
							u.log(phstp.stderr);
						}
					} else if(board.equals("tegra")||board.equals("otter")){
						if(boot!=null){
							CommandResult phstp = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/dump_image "+boot+" "+u.getExternalDirectory()+"/BootManager/phoneRom/boot.img");
							if(phstp.stdout!=null){
								u.log(phstp.stdout);
							}
							if(phstp.stderr!=null){
								u.log(phstp.stderr);
							}
						}
					}else{
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/dump_image boot "+u.getExternalDirectory()+"/BootManager/phoneRom/boot.img");
					}
					if(!board.equals("vigor")){
						if(!new File(u.getExternalDirectory()+"/BootManager/phoneRom/update.zip").exists()) {
							u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip "+u.getExternalDirectory()+"/BootManager/phoneRom/update.zip");
						}
						if(!new File(u.getExternalDirectory()+"/BootManager/phoneRom/update.zip").exists()) {
							if(board.equalsIgnoreCase("aloha")){
								u.downloadUtil(context, u.BASE_URL + "devices/aloha/BootManager-phone-signed.zip", u.getExternalDirectory()+"/BootManager/phoneRom/update.zip");
							} else {
								u.downloadUtil(context, u.BASE_URL + "files/BootManager-phone-signed.zip", u.getExternalDirectory()+"/BootManager/phoneRom/update.zip");
							}
						}
					}
					u.execCommand("echo Phone Rom > "+u.getExternalDirectory()+"/BootManager/phoneRom/name");
					if(new File(u.getExternalDirectory()+"/BootManager/phoneRom/boot.img").exists()){
						int count = 0;
						while(!(new File(u.getExternalDirectory()+"/BootManager/phoneRom/boot.img").length() > 0)){
							count++;
							if(count > 5){
								break;
							}
							if(board.equals("sholes")){
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox dd if=/dev/block/mtdblock2 of=" + u.getExternalDirectory() + "/BootManager/phoneRom/boot.img");
							}else if(board.equals("aloha")){
								CommandResult dumpboot = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/morebinarys/dump_image boot " + u.getExternalDirectory() + "/BootManager/phoneRom/boot.img");
								if(dumpboot.stderr!=null){
									u.log(dumpboot.stderr);
								}
								if(dumpboot.stdout!=null){
									u.log(dumpboot.stdout);
								}
							} else if(board.equals("tuna")){
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/dump_image /dev/block/mmcblk0p7 "+u.getExternalDirectory()+"/BootManager/phoneRom/boot.img");
							} else if(board.equals("herring")){
								CommandResult phstp = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox dd if=/dev/mtd/mtd2 of="+u.getExternalDirectory()+"/BootManager/phoneRom/boot.img");
								if(phstp.stdout!=null){
									u.log(phstp.stdout);
								}
								if(phstp.stderr!=null){
									u.log(phstp.stderr);
								}
							} else if(board.equals("tegra")||board.equals("otter")){
								if(boot!=null){
									CommandResult phstp = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/dump_image "+boot+" "+u.getExternalDirectory()+"/BootManager/phoneRom/boot.img");
									if(phstp.stdout!=null){
										u.log(phstp.stdout);
									}
									if(phstp.stderr!=null){
										u.log(phstp.stderr);
									}
								}
							}else{
								CommandResult dumpboot = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/dump_image boot /data/local/tmp/boot.img");
								if(dumpboot.stderr!=null){
									u.log(dumpboot.stderr);
								}
								if(dumpboot.stdout!=null){
									u.log(dumpboot.stdout);
								}
								u.execCommand(context.getFilesDir().getAbsolutePath() + "busybox mv /data/local/tmp/boot.img "+u.getExternalDirectory()+"/BootManager/phoneRom/boot.img");
								u.execCommand(context.getFilesDir().getAbsolutePath() + "busybox rm /data/local/tmp/boot.img");
							}
						}
						if(board.equals("vigor")){
							addBootimgtoUpdateRezound();
						}
						sp.dismiss();
						((Activity) context).runOnUiThread(new Runnable() {                
							public void run() {
								CustomDialog.Builder builder2 = new CustomDialog.Builder(context);
								builder2.setTitle("Success!!!")
								.setMessage(R.string.phoneSucc)
								.setCancelable(false)
								.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								}).show();
							}
						});
					} else {
						sp.dismiss();
						((Activity) context).runOnUiThread(new Runnable() {                
							public void run() {
								CustomDialog.Builder builder2 = new CustomDialog.Builder(context);
								builder2.setTitle("Failed to back up boot.img")
								.setMessage(R.string.phoneFail)
								.setCancelable(false)
								.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								}).show();
							}
						});
					}
				}finally{

				}
			}
		}
	}

	public void factorReset(String board, final Context context, final String slot){
		if(board.contains("shadow")||board.contains("droid2")||board.contains("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
			filesystem="mke2fs -F -T ext3 -b 4096 -m 0 ";
		} else {
			CustomDialog.Builder builder = new CustomDialog.Builder(context);
			builder.setTitle("Which Filesystem for "+slot)
			.setMessage(R.string.filesystem)
			.setCancelable(true)
			.setPositiveButton("Ext4", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					filesystem="mke2fs -F -T ext4 -b 4096 -E stride=64,stripe-width=64 -O ^has_journal,extent,^huge_file -m 0 ";
					sp = CustomProgressDialog.show(context, "Factory resetting "+slot, "Please wait ...", true, false);
					frThread fr = new frThread(filesystem, context, slot);
					fr.start();
				}
			})
			.setNegativeButton("Ext2", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					filesystem="mke2fs -F -b 4096 -m 0 ";
					sp = CustomProgressDialog.show(context, "Factory resetting "+slot, "Please wait ...", true, false);
					frThread fr = new frThread(filesystem, context, slot);
					fr.start();
				}
			}).show();
		}

	}

	public class frThread extends Thread{
		Context context;
		String filesystem;
		String slot;
		Utilities u = new Utilities();
		ShellCommand s = new ShellCommand();

		frThread(String filesystem, Context context, String slot){
			this.context=context;
			this.filesystem=filesystem;
			this.slot=slot;
		}
		@Override
		public void run() {
			try {
				u.log("Wiping data.img");
				CommandResult datafs = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/"+filesystem  + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
				if(datafs.stdout!=null){
					u.log(datafs.stdout);
				}
				if(datafs.stderr!=null){
					u.log(datafs.stderr);
				}
				if((datafs.exit_value)!=0){
					CommandResult datafs2 = s.su.runWaitFor(filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
					if(datafs2.stdout!=null){
						u.log(datafs2.stdout);
					}
					if(datafs2.stderr!=null){
						u.log(datafs2.stderr);
					}
					if((datafs2.exit_value)!=0){
						CommandResult datafs3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/morebinarys/"+filesystem+ u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
						if((datafs3.exit_value)!=0){
							if(datafs3.stdout!=null){
								u.log(datafs3.stdout);
							}
							if(datafs3.stderr!=null){
								u.log(datafs3.stderr);
							}
						}
					}
				}
				u.log("Wiping cache.img");
				CommandResult cachefs = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/"+filesystem  + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
				if(cachefs.stdout!=null){
					u.log(cachefs.stdout);
				}
				if(cachefs.stderr!=null){
					u.log(cachefs.stderr);
				}
				if((cachefs.exit_value)!=0){
					CommandResult cachefs2 = s.su.runWaitFor(filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
					if(cachefs2.stdout!=null){
						u.log(cachefs2.stdout);
					}
					if(cachefs2.stderr!=null){
						u.log(cachefs2.stderr);
					}
					if((cachefs2.exit_value)!=0){
						CommandResult cachefs3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/morebinarys/"+filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
						if((cachefs3.exit_value)!=0){
							if(cachefs3.stdout!=null){
								u.log(cachefs3.stdout);
							}
							if(cachefs3.stderr!=null){
								u.log(cachefs3.stderr);
							}
						}
					}
				}
			} finally {
				sp.dismiss();
			}

		}
	}

	private void addBootimgtoUpdateRezound(){
		Utilities u = new Utilities();
		File updatezip = new File(u.getExternalDirectory()+"/BootManager/phoneRom/update.zip");
		if(updatezip.exists())updatezip.delete();
		try{
			byte[] buffer = new byte[4096];
			ZipInputStream zin = new ZipInputStream(new FileInputStream(new File(u.getExternalDirectory()+"/BootManager/.zips/BootImageFlasherRezound.zip")));
			FileInputStream bootis = new FileInputStream(new File(u.getExternalDirectory()+"/BootManager/phoneRom/boot.img"));
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(updatezip));
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null){
				out.putNextEntry(new ZipEntry(ze));
				for(int read = zin.read(buffer); read > -1; read = zin.read(buffer)){
					out.write(buffer, 0, read);
				}
				out.closeEntry();
			}
			ZipEntry bootentry = new ZipEntry("kernel/boot.img");
			out.putNextEntry(bootentry);
			for(int read = bootis.read(buffer); read > -1; read = bootis.read(buffer)){
				out.write(buffer, 0, read);
			}
			out.closeEntry();
			bootis.close();

			out.close();
			zin.close();
		}catch(Exception e){
			u.log(e.toString());
			e.printStackTrace();
		}

	}
}