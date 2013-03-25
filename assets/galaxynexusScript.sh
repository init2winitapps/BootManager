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

$bb sed -i 's:mount.*\/system.*$:mkdir bootmanager 0771 system system\n        mount ext4 /dev/block/platform/omap/omap_hsmmc.0/by-name/userdata /bootmanager wait noatime nosuid nodev nomblk_io_submit,errors=panic\n        mount ext4 loop@/bootmanager/media/BootManager/'$rom'/system.img /system:' /data/local/tmp/boot.img-ramdisk/init.$board.rc

$bb sed -i 's:mount.*\/data.*$:mount ext4 loop@/bootmanager/media/BootManager/'$rom'/data.img /data:' /data/local/tmp/boot.img-ramdisk/init.$board.rc

$bb sed -i 's:mount.*\/cache.*$:mount ext4 loop@/bootmanager/media/BootManager/'$rom'/cache.img /cache\n        mkdir /mnt 0775 root system\n        mkdir /mnt/secure 0700 root root\n        mkdir /mnt/secure/asec  0700 root root\n        mkdir /data/media 0775 media_rw media_rw\n        chown media_rw media_rw /data/media\n        mkdir /data/misc/wifi 0770 wifi wifi\n        mkdir /data/misc/wifi/sockets 0770 wifi wifi\n        mkdir /data/misc/dhcp 0770 dhcp dhcp\n        chown dhcp dhcp /data/misc/dhcp\n        mkdir /data/smc 0770 drmrpc drmrpc\n        chown drmrpc drmrpc /data/smc/counter.bin\n        chown drmrpc drmrpc /data/smc/storage.bin\n        chown drmrpc drmrpc /data/smc/system.bin\n        mkdir /data/misc/camera 0770 media media\n        mkdir /data/misc/camera/R5_MVEN003_LD2_ND0_IR0_SH0_FL1_SVEN003_DCCID1044 0770 media media\n        setprop vold.post_fs_data_done 1:' /data/local/tmp/boot.img-ramdisk/init.$board.rc

$bb sed -i 's:service sdcard /system/bin/sdcard /data/media 1023 1023:service sdcard /system/bin/sdcard /bootmanager/media 1023 1023:' /data/local/tmp/boot.img-ramdisk/init.$board.rc


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


