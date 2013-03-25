#!/data/data/com.drx2.bootmanager.lite/files/busybox sh 

board=$1
rom=$2
bb=$3
key=$4
pass=1

echo "Free" 

#edit script here
$bb mkdir /data/local/tmp/boot.img-ramdisk
cd /data/local/tmp/boot.img-ramdisk
$bb gzip -dc /data/local/tmp/boot.img-ramdisk.gz | $bb cpio -i
cd /
#edit mount lines here
#use only ext4? stock kernel has support so probably don't need ext2 at all

$bb sed -i 's:mount.*\/system.*$:export EXTERNAL_STORAGE /mnt/sdcard\n    mkdir /mnt 0775 root system\n    mkdir /mnt/sdcard 0000 system system\n    symlink /mnt/sdcard /sdcard\n    mount ext4 /dev/block/platform/s3c-sdhci.0/by-name/media /mnt/sdcard\n    chmod 0771 /system\n    mount ext4 loop@/mnt/sdcard/BootManager/'$rom'/system.img /system:' /data/local/tmp/boot.img-ramdisk/init.$board.rc

$bb sed -i 's:mount.*\/cache.*$::' /data/local/tmp/boot.img-ramdisk/init.$board.rc

$bb sed -i 's:mount.*\/data.*$:mount ext4 loop@/mnt/sdcard/BootManager/'$rom'/data.img /data\n    mount ext4 loop@/mnt/sdcard/BootManager/'$rom'/cache.img /cache:' /data/local/tmp/boot.img-ramdisk/init.$board.rc


#repack boot.img
cd /data/local/tmp/boot.img-ramdisk
$bb find . | $bb cpio -o -H newc | $bb gzip > /data/local/tmp/newramdisk.cpio.gz
$bb rm /data/local/tmp/boot.img-ramdisk.gz
$bb mv /data/local/tmp/newramdisk.cpio.gz /data/local/tmp/boot.img-ramdisk.gz
$bb mv /data/local/tmp/boot.img-zImage /data/local/tmp/zImage
$bb echo \#!/system/bin/sh > /data/local/tmp/createnewboot.sh
$bb echo /data/local/tmp/mkbootimg --kernel /data/local/tmp/zImage --ramdisk /data/local/tmp/boot.img-ramdisk.gz --cmdline \"$($bb cat /data/local/tmp/boot.img-cmdline)\" --base $($bb cat /data/local/tmp/boot.img-base) --output /data/local/tmp/newboot.img >> /data/local/tmp/createnewboot.sh
$bb chmod 777 /data/local/tmp/createnewboot.sh
/data/local/tmp/createnewboot.sh
$bb cp /data/local/tmp/newboot.img /sdcard/BootManager/$rom/boot.img


