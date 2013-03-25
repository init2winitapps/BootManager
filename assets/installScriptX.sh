#!/data/data/com.drx2.bootmanager.lite/files/busybox sh 
board=$1
rom=$2
sdcardblock=$3
bb=$4
zip=$5
key=$6
ext=$7
sdcard=$8
pass=1

echo "Free" 


	$bb sed -i 's:mount.*\/system.*$:mkdir /internaldata 0771 system system\n    mount '$ext' /dev/block/mmcblk1p24 /internaldata nosuid nodev noatime nodiratime\n    exec /sbin/busybox cp sbin/recovery_mode /internaldata/.recovery_mode\n    mkdir /mnt 0775 root system\n    mkdir '$sdcard' 0000 system system\n    exec /sbin/busybox sleep 5\n    mount vfat '$sdcardblock' '$sdcard'\n    exec /sbin/busybox mount -t '$ext' -o rw '$sdcard'/BootManager/'$rom'/system.img /system:' /data/local/tmp/hijack-boot/newboot/init.$board.rc
	$bb sed -i 's:mount.*\/data.*$:exec /sbin/busybox mount -t '$ext' -o rw '$sdcard'/BootManager/'$rom'/data.img /data:' /data/local/tmp/hijack-boot/newboot/init.$board.rc
	$bb sed -i 's:mount.*\/cache.*$:exec /sbin/busybox mount -t '$ext' -o rw '$sdcard'/BootManager/'$rom'/cache.img /cache\n    exec /sbin/busybox mount --bind /sdcard/BootManager/'$rom'/.android_secure mnt/secure/asec:' /data/local/tmp/hijack-boot/newboot/init.$board.rc
$bb echo "set_perm(0, 0, 0755, \"/newboot/sbin/busybox\");" >> /data/local/tmp/hijack-boot/META-INF/com/google/android/updater-script
$bb mv /data/local/tmp/busybox /data/local/tmp/hijack-boot/newboot/sbin/busybox
$bb chmod 777 /data/local/tmp/hijack-boot/newboot/sbin/busybox
$bb mv /data/local/tmp/recovery_mode /data/local/tmp/hijack-boot/newboot/sbin/recovery_mode
cd /data/local/tmp/hijack-boot
$zip -r /data/local/tmp/hijack-boot.zip *
return
