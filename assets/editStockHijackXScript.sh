#!/data/data/com.drx2.bootmanager.lite/files/busybox sh 

rom=$1
bb=$2
zip=$3
key=$4
pass=1

echo "Free" 


	$bb sed -i 's:exec /sbin/busybox mount -t ext3 -o rw /mnt/sdcard/BootManager/rom1/system.img /system:exec /sbin/busybox mount -t ext3 -o rw /mnt/sdcard/BootManager/'$rom'/system.img /system:' /data/local/tmp/hijack-boot/newboot/init.rc
	$bb sed -i 's:exec /sbin/busybox mount -t ext3 -o rw /mnt/sdcard/BootManager/rom1/data.img /data:exec /sbin/busybox mount -t ext3 -o rw /mnt/sdcard/BootManager/'$rom'/data.img /data:' /data/local/tmp/hijack-boot/newboot/init.rc
	$bb sed -i 's:exec /sbin/busybox mount -t ext3 -o rw /mnt/sdcard/BootManager/rom1/cache.img /cache:exec /sbin/busybox mount -t ext3 -o rw /mnt/sdcard/BootManager/'$rom'/cache.img /cache:' /data/local/tmp/hijack-boot/newboot/init.rc
	$bb sed -i 's:exec /sbin/busybox mount --bind /sdcard/BootManager/rom1/.android_secure mnt/secure/asec:exec /sbin/busybox mount --bind /sdcard/BootManager/'$rom'/.android_secure mnt/secure/asec:' /data/local/tmp/hijack-boot/newboot/init.rc


cd /data/local/tmp/hijack-boot
$zip -r /data/local/tmp/hijack-boot.zip *
return
