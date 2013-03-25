#!/data/data/com.drx2.bootmanager.lite/files/busybox sh 
rom=$1
bb=$2
ext=$3

echo "Free" 

$bb mkdir /data/local/tmp/boot.img-ramdisk
cd /data/local/tmp/boot.img-ramdisk
$bb gzip -dc /data/local/tmp/boot.img-ramdisk.gz | $bb cpio -i
cd /
if [ "$ext" == "ext4" ]; then
	$bb cp /data/local/tmp/system/lib/modules/ext4.ko /$storage/BootManager/$rom/ext4.ko
	$bb cp /data/local/tmp/system/lib/modules/jbd2.ko /$storage/BootManager/$rom/jbd2.ko
else
	$bb cp /data/local/tmp/system/lib/modules/ext2.ko /data/local/tmp/boot.img-ramdisk/sbin/ext2.ko
	$bb cp /data/local/tmp/system/lib/modules/mbcache.ko /data/local/tmp/boot.img-ramdisk/sbin/mbcache.ko
fi
cd /data/local/tmp/boot.img-ramdisk
$bb find . | $bb cpio -o -H newc | $bb gzip > /data/local/tmp/newramdisk.cpio.gz
$bb rm /data/local/tmp/boot.img-ramdisk.gz
$bb mv /data/local/tmp/newramdisk.cpio.gz /data/local/tmp/boot.img-ramdisk.gz
$bb echo \#!/system/bin/sh > /data/local/tmp/createnewboot.sh
$bb echo /data/local/tmp/mkbootimg --kernel /data/local/tmp/zImage --ramdisk /data/local/tmp/boot.img-ramdisk.gz --cmdline \"$($bb cat /data/local/tmp/boot.img-cmdline)\" --base $($bb cat /data/local/tmp/boot.img-base) --output /data/local/tmp/newboot.img >> /data/local/tmp/createnewboot.sh
$bb chmod 777 /data/local/tmp/createnewboot.sh
/data/local/tmp/createnewboot.sh
$bb cp /data/local/tmp/newboot.img /sdcard/BootManager/$rom/boot.img
