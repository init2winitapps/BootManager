#!/data/data/com.drx2.bootmanager.lite/files/busybox sh 
board=$1
rom=$2
sdcardblock=$3
storage=$4
bb=$5
bbpath=$6
ext_sdblock=$7
ext=$8
key=$9
pass=1

echo "Free"

$bb mkdir /data/local/tmp/boot.img-ramdisk
cd /data/local/tmp/boot.img-ramdisk
$bb gzip -dc /data/local/tmp/boot.img-ramdisk.gz | $bb cpio -i
cd /
if [ "$ext" == "ext4" ]; then
	$bb sed -i 's:\#mount.*\/system.*$: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:\#mount.*\/data.*$: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:\#mount.*\/cache.*$: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:mount.*\/system.*$:mkdir /mnt 0775 root system\n    mkdir /mnt/sdcard 0000 system system\n    exec /sbin/busybox sleep 10\n    mount vfat '$sdcardblock' /mnt/sdcard\n    mount vfat '$ext_sdblock' '$storage'\n    chmod 0771 /system\n    exec /sbin/busybox mount -t ext4 -o rw '$storage'/BootManager/'$rom'/system.img /system:' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:mount.*\/data.*$:exec /sbin/busybox mount -t ext4 -o rw '$storage'/BootManager/'$rom'/data.img /data:' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:mount.*\/cache.*$:exec /sbin/busybox mount -t ext4 -o rw '$storage'/BootManager/'$rom'/cache.img /cache\n    mkdir /mnt 0775 root system\n    mkdir /mnt/secure 0700 root root\n    mkdir /mnt/secure/asec  0700 root root\n    exec /sbin/busybox mount --bind '$storage'/BootManager/'$rom'/.android_secure mnt/secure/asec:' /data/local/tmp/boot.img-ramdisk/init.$board.rc
else
	$bb sed -i 's:\#mount.*\/system.*$: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:\#mount.*\/data.*$: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:\#mount.*\/cache.*$: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:mount.*\/system.*$:mkdir /mnt 0775 root system\n    mkdir /mnt/sdcard 0000 system system\n    exec /sbin/busybox sleep 10\n    mount vfat '$sdcardblock' /mnt/sdcard\n    mount vfat '$ext_sdblock' '$storage'\n    chmod 0771 /system\n    exec /sbin/busybox mount -t ext2 -o rw '$storage'/BootManager/'$rom'/system.img /system:' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:mount.*\/data.*$:exec /sbin/busybox mount -t ext2 -o rw '$storage'/BootManager/'$rom'/data.img /data:' /data/local/tmp/boot.img-ramdisk/init.$board.rc
	$bb sed -i 's:mount.*\/cache.*$:exec /sbin/busybox mount -t ext2 -o rw '$storage'/BootManager/'$rom'/cache.img /cache\n    mkdir /mnt 0775 root system\n    mkdir /mnt/secure 0700 root root\n    mkdir /mnt/secure/asec  0700 root root\n    exec /sbin/busybox mount --bind '$storage'/BootManager/'$rom'/.android_secure mnt/secure/asec:' /data/local/tmp/boot.img-ramdisk/init.$board.rc	
fi

$bb sed -i 's:on boot:    chmod 0777 /system\n\non boot:' /data/local/tmp/boot.img-ramdisk/init.$board.rc

$bb mv /data/local/tmp/busybox /data/local/tmp/boot.img-ramdisk/sbin/busybox
$bb chmod 777 /data/local/tmp/boot.img-ramdisk/sbin/busybox
cd /data/local/tmp/boot.img-ramdisk
$bb find . | $bb cpio -o -H newc | $bb gzip > /data/local/tmp/newramdisk.cpio.gz
$bb rm /data/local/tmp/boot.img-ramdisk.gz
$bb mv /data/local/tmp/newramdisk.cpio.gz /data/local/tmp/boot.img-ramdisk.gz
$bb mv /data/local/tmp/boot.img-zImage /data/local/tmp/zImage
$bb echo \#!/system/bin/sh > /data/local/tmp/createnewboot.sh
$bb echo /data/local/tmp/mkbootimg --kernel /data/local/tmp/zImage --ramdisk /data/local/tmp/boot.img-ramdisk.gz --cmdline \"$($bb cat /data/local/tmp/boot.img-cmdline)\" --base $($bb cat /data/local/tmp/boot.img-base) --output /data/local/tmp/newboot.img >> /data/local/tmp/createnewboot.sh
$bb chmod 777 /data/local/tmp/createnewboot.sh
/data/local/tmp/createnewboot.sh
$bb cp /data/local/tmp/newboot.img $storage/BootManager/$rom/boot.img


