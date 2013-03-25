#!/data/data/com.drx2.bootmanager.lite/files/busybox sh 
board=$1
rom=$2
sdcardblock=$3
storage=$4
bb=$5
bbpath=$6
sdcardreal=$7
ext=$8
key=$9
pass=1

echo "Free" 


$bb mkdir /data/local/tmp/boot.img-ramdisk
cd /data/local/tmp/boot.img-ramdisk
$bb gzip -dc /data/local/tmp/boot.img-ramdisk.gz | $bb cpio -i
cd /
if [ "$ext" == "ext4" ]; then
	if [ "$board" == "sholes" ]; then
		$bb sed -i 's:mount.*\/system.*: :' /data/local/tmp/boot.img-ramdisk/init.rc
		$bb sed -i 's:mount.*\/data.*: :' /data/local/tmp/boot.img-ramdisk/init.rc
		$bb awk '/mount yaffs2 mtd@cache \/cache nosuid nodev/ && n == 0 { sub(/mount yaffs2 mtd@cache \/cache nosuid nodev/, "insmod /sbin/mbcache.ko\n    insmod /sbin/jbd2.ko\n    insmod /sbin/ext4.ko\n    devwait '$sdcardblock'\n    mount vfat '$sdcardblock' /'$storage'\n    chmod 0771 /system\n    mount ext4 loop@/'$storage'/BootManager/'$rom'/system.img /system noatime nodiratime \n    mount ext4 loop@/'$storage'/BootManager/'$rom'/data.img /data noatime nodiratime\n    mount ext4 loop@/'$storage'/BootManager/'$rom'/cache.img /cache noatime nodiratime\n    exec '$bbpath' mount --bind /sdcard/BootManager/'$rom'/.android_secure mnt/secure/asec\n    chown system system /data\n    chmod 0771 /data\n    chown system cache /cache\n    chmod 0770 /cache"); ++n } { print }' /data/local/tmp/boot.img-ramdisk/init.rc > /data/local/tmp/boot.img-ramdisk/newinit.rc
	$bb cp /data/local/ext4.ko /data/local/tmp/boot.img-ramdisk/sbin/ext4.ko	
	else
		if [ "$storage" == "emmc" ]; then
		$bb awk '/mount yaffs2 mtd@cache \/cache nosuid nodev/ && n == 0 { sub(/mount yaffs2 mtd@cache \/cache nosuid nodev/, "umount /data\n    umount /system\n    export PHONE_STORAGE /mnt/emmc\n    mount rootfs rootfs / remount\n    mkdir /mnt/emmc 0771 system system\n    symlink /mnt/emmc /emmc\n    mount rootfs rootfs / ro remount\n    devwait '$sdcardblock'\n    mount vfat '$sdcardblock' /'$storage'\n    mount vfat '$sdcardreal' /sdcard\n    insmod /'$storage'/BootManager/'$rom'/jbd2.ko\n    insmod /'$storage'/BootManager/'$rom'/ext4.ko\n    chmod 0771 /system\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext4 -o noatime,nodiratime,commit=19,barrier=0,nobh,nouser_xattr,errors=continue,nodev,rw /'$storage'/BootManager/'"$rom"'/system.img /system\n    exec /system/bin/logwrapper /sbin/e2fsck -p /dev/block/loop0\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext4 -o noatime,nodiratime,nosuid,nodev,commit=19,barrier=0,nobh,nouser_xattr,errors=continue,rw /'$storage'/BootManager/'"$rom"'/data.img /data\n    exec /system/bin/logwrapper /sbin/e2fsck -p /dev/block/loop1\n    exec /'$storage'/BootManager/'$rom'/busybox mount --bind /emmc/BootManager/'$rom'/.android_secure mnt/secure/asec\n    chown system system /data\n    chmod 0771 /data\n    umount \/cache\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext4 -o noatime,nodiratime,nosuid,nodev,commit=19,barrier=0,nobh,nouser_xattr,errors=continue,rw /'$storage'/BootManager/'"$rom"'/cache.img /cache\n    exec /system/bin/logwrapper /sbin/e2fsck -p /dev/block/loop2\n    chown system cache /cache\n    chmod 0770 /cache"); ++n } { print }' /data/local/tmp/boot.img-ramdisk/init.rc > /data/local/tmp/boot.img-ramdisk/newinit.rc
		else
		$bb awk '/mount yaffs2 mtd@cache \/cache nosuid nodev/ && n == 0 { sub(/mount yaffs2 mtd@cache \/cache nosuid nodev/, "umount /data\n    umount /system\n    devwait '$sdcardblock'\n    mount vfat '$sdcardblock' /'$storage'\n    insmod /'$storage'/BootManager/'$rom'/jbd2.ko\n    insmod /'$storage'/BootManager/'$rom'/ext4.ko\n    chmod 0771 /system\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext4 -o noatime,nodiratime,commit=19,barrier=0,nobh,nouser_xattr,errors=continue,nodev,rw /'$storage'/BootManager/'"$rom"'/system.img /system\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext4 -o noatime,nodiratime,nosuid,nodev,commit=19,barrier=0,nobh,nouser_xattr,errors=continue,rw /'$storage'/BootManager/'"$rom"'/data.img /data\n    exec /'$storage'/BootManager/'$rom'/busybox mount --bind /sdcard/BootManager/'$rom'/.android_secure mnt/secure/asec\n    chown system system /data\n    chmod 0771 /data\n    umount \/cache\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext4 -o noatime,nodiratime,nosuid,nodev,commit=19,barrier=0,nobh,nouser_xattr,errors=continue,rw /'$storage'/BootManager/'"$rom"'/cache.img /cache\n    chown system cache /cache\n    chmod 0770 /cache"); ++n } { print }' /data/local/tmp/boot.img-ramdisk/init.rc > /data/local/tmp/boot.img-ramdisk/newinit.rc
		fi
		$bb cp /data/local/tmp/system/lib/modules/ext4.ko /$storage/BootManager/$rom/ext4.ko
		$bb cp /data/local/tmp/system/lib/modules/jbd2.ko /$storage/BootManager/$rom/jbd2.ko
	fi
else
	if [ "$board" == "sholes" ]; then
		$bb sed -i 's:mount.*\/system.*: :' /data/local/tmp/boot.img-ramdisk/init.rc
		$bb sed -i 's:mount.*\/data.*: :' /data/local/tmp/boot.img-ramdisk/init.rc
		$bb awk '/mount yaffs2 mtd@cache \/cache nosuid nodev/ && n == 0 { sub(/mount yaffs2 mtd@cache \/cache nosuid nodev/, "insmod /sbin/mbcache.ko\n    insmod /sbin/ext2.ko\n    devwait '$sdcardblock'\n    mount vfat '$sdcardblock' /'$storage'\n    chmod 0771 /system\n    mount ext2 loop@/'$storage'/BootManager/'$rom'/system.img /system noatime nodiratime \n    mount ext2 loop@/'$storage'/BootManager/'$rom'/data.img /data noatime nodiratime\n    mount ext2 loop@/'$storage'/BootManager/'$rom'/cache.img /cache noatime nodiratime\n    exec '$bbpath' mount --bind /sdcard/BootManager/'$rom'/.android_secure mnt/secure/asec\n    chown system system /data\n    chmod 0771 /data\n    chown system cache /cache\n    chmod 0770 /cache"); ++n } { print }' /data/local/tmp/boot.img-ramdisk/init.rc > /data/local/tmp/boot.img-ramdisk/newinit.rc
	$bb cp /data/local/tmp/ext2.ko /data/local/tmp/boot.img-ramdisk/sbin/ext2.ko
	$bb cp /data/local/tmp/mbcache.ko /data/local/tmp/boot.img-ramdisk/sbin/mbcache.ko
	else
		if [ "$storage" == "emmc" ]; then
		$bb awk '/mount yaffs2 mtd@cache \/cache nosuid nodev/ && n == 0 { sub(/mount yaffs2 mtd@cache \/cache nosuid nodev/, "umount /data\n    umount /system\n    export PHONE_STORAGE /mnt/emmc\n    mount rootfs rootfs / remount\n    mkdir /mnt/emmc 0771 system system\n    symlink /mnt/emmc /emmc\n    mount rootfs rootfs / ro remount\n    devwait '$sdcardblock'\n    mount vfat '$sdcardblock' /'$storage'\n    mount vfat '$sdcardreal' /sdcard\n    chmod 0771 /system\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext2 -o rw /'$storage'/BootManager/'"$rom"'/system.img /system\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext2 -o rw /'$storage'/BootManager/'"$rom"'/data.img /data\n    exec /'$storage'/BootManager/'$rom'/busybox mount --bind /emmc/BootManager/'$rom'/.android_secure mnt/secure/asec\n    chown system system /data\n    chmod 0771 /data\n    umount \/cache\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext2 -o rw /'$storage'/BootManager/'"$rom"'/cache.img /cache\n    exec /system/bin/logwrapper /sbin/e2fsck -p /dev/block/loop2\n    chown system cache /cache\n    chmod 0770 /cache"); ++n } { print }' /data/local/tmp/boot.img-ramdisk/init.rc > /data/local/tmp/boot.img-ramdisk/newinit.rc
		else
		$bb awk '/mount yaffs2 mtd@cache \/cache nosuid nodev/ && n == 0 { sub(/mount yaffs2 mtd@cache \/cache nosuid nodev/, "umount /data\n    umount /system\n    devwait '$sdcardblock'\n    mount vfat '$sdcardblock' /'$storage'\n    chmod 0771 /system\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext2 -o rw /'$storage'/BootManager/'"$rom"'/system.img /system\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext2 -o rw /'$storage'/BootManager/'"$rom"'/data.img /data\n    exec /'$storage'/BootManager/'$rom'/busybox mount --bind /sdcard/BootManager/'$rom'/.android_secure mnt/secure/asec\n    chown system system /data\n    chmod 0771 /data\n    umount \/cache\n    exec /'$storage'/BootManager/'$rom'/busybox mount -t ext2 -o rw /'$storage'/BootManager/'"$rom"'/cache.img /cache\n    chown system cache /cache\n    chmod 0770 /cache"); ++n } { print }' /data/local/tmp/boot.img-ramdisk/init.rc > /data/local/tmp/boot.img-ramdisk/newinit.rc
		fi
	fi
fi
$bb rm /data/local/tmp/boot.img-ramdisk/init.rc
$bb mv /data/local/tmp/boot.img-ramdisk/newinit.rc /data/local/tmp/boot.img-ramdisk/init.rc

$bb sed -i 's:on boot:    chmod 0777 /system\n\non boot:' /data/local/tmp/boot.img-ramdisk/init.$board.rc
$bb sed -i 's:mount.*\/system: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc
$bb sed -i 's:mount.*\/data: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc
$bb sed -i 's:mount.*\/cache: :' /data/local/tmp/boot.img-ramdisk/init.$board.rc

if [ "$board" == "vivow" ]; then
	$bb sed -i 's:mount.*\/system: :' /data/local/tmp/boot.img-ramdisk/init.vivow_ct.rc
	$bb sed -i 's:mount.*\/data: :' /data/local/tmp/boot.img-ramdisk/init.vivow_ct.rc
	$bb sed -i 's:mount.*\/cache: :' /data/local/tmp/boot.img-ramdisk/init.vivow_ct.rc
fi

if [ "$board" == "tegra" ]; then
	$bb echo "g2x detected"
	$bb sed -i 's:mount.*\/system: :' /data/local/tmp/boot.img-ramdisk/init.p999.rc
	$bb sed -i 's:mount.*\/data: :' /data/local/tmp/boot.img-ramdisk/init.p999.rc
	$bb sed -i 's:mount.*\/cache: :' /data/local/tmp/boot.img-ramdisk/init.p999.rc
fi

cd /data/local/tmp/boot.img-ramdisk
$bb find . | $bb cpio -o -H newc | $bb gzip > /data/local/tmp/newramdisk.cpio.gz
$bb rm /data/local/tmp/boot.img-ramdisk.gz
$bb mv /data/local/tmp/newramdisk.cpio.gz /data/local/tmp/boot.img-ramdisk.gz
$bb mv /data/local/tmp/boot.img-zImage /data/local/tmp/zImage
	if [ "$board" == "tegra" ]; then
	$bb echo \#!/system/bin/sh > /data/local/tmp/createnewboot.sh
	$bb echo /data/local/tmp/mkbootimg --kernel /data/local/tmp/zImage --ramdisk /data/local/tmp/boot.img-ramdisk.gz --base $($bb cat /data/local/tmp/boot.img-base) --output /data/local/tmp/newboot.img >> /data/local/tmp/createnewboot.sh
	else
	$bb echo \#!/system/bin/sh > /data/local/tmp/createnewboot.sh
	$bb echo /data/local/tmp/mkbootimg --kernel /data/local/tmp/zImage --ramdisk /data/local/tmp/boot.img-ramdisk.gz --cmdline \"$($bb cat /data/local/tmp/boot.img-cmdline)\" --base $($bb cat /data/local/tmp/boot.img-base) --output /data/local/tmp/newboot.img >> /data/local/tmp/createnewboot.sh
	fi
$bb chmod 777 /data/local/tmp/createnewboot.sh
/data/local/tmp/createnewboot.sh
$bb cp /data/local/tmp/newboot.img /sdcard/BootManager/$rom/boot.img

return
