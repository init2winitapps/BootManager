#include "com_drx2_bootmanager_screenshot_ScreenShotLib.h"
#include <stdlib.h>
#include <unistd.h>

#include <fcntl.h>
#include <stdio.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <time.h>

#include <linux/fb.h>
#include <linux/kd.h>

#include "pixelflinger.h"

typedef struct {
     long filesize;
     char reserved[2];
     long headersize;
     long infoSize;
     long width;
     long depth;
     short biPlanes;
     short bits;
     long biCompression;
     long biSizeImage;
     long biXPelsPerMeter;
     long biYPelsPerMeter;
     long biClrUsed;
     long biClrImportant;
} BMPHEAD;

int ao; 
int ro; 
int go; 
int bo;
int offset;

//surface pointer
static GGLSurface gr_framebuffer[2]; 
//handler
static int gr_fb_fd = -1;
//v screen info
static struct fb_var_screeninfo vi;
//f screen info
struct fb_fix_screeninfo fi;

static void dumpinfo(struct fb_fix_screeninfo *fi,
                     struct fb_var_screeninfo *vi);


static int get_framebuffer(GGLSurface *fb, const char *framebuffer)
{
    int fd;
    void *bits;
    
    fd = open(framebuffer, O_RDWR);
    if(fd < 0) {
        //fprintf(fp, "cannot open fb0");
	exit(6);
        return -1;
	
    }

    if(ioctl(fd, FBIOGET_FSCREENINFO, &fi) < 0) {
        //fprintf(fp, "failed to get fb0 info");
	exit(7);
        return -1;
    }

    if(ioctl(fd, FBIOGET_VSCREENINFO, &vi) < 0) {
        //fprintf(fp, "failed to get fb0 info");
	exit(8);
        return -1;
    }

    //dumpinfo(&fi, &vi);

    bits = mmap(0, fi.smem_len, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if(bits == MAP_FAILED) {
        //fprintf(fp, "failed to mmap framebuffer");
	exit(9);
        return -1;
    }
	
    fb->version = sizeof(*fb);
    fb->width = vi.xres;
    fb->height = vi.yres;
    fb->stride = fi.line_length / (vi.bits_per_pixel >> 3);
    fb->data = bits;
    fb->format = GGL_PIXEL_FORMAT_RGB_565;

    fb++;
    offset = vi.xoffset * vi.bits_per_pixel;
    offset += vi.xres * vi.yoffset * vi.bits_per_pixel;
    lseek(fd, offset, SEEK_SET);
    fb->version = sizeof(*fb);
    fb->width = vi.xres;
    fb->height = vi.yres;
    fb->stride = fi.line_length / (vi.bits_per_pixel >> 3);
    fb->data = (void*) (((unsigned) bits) + vi.yres * vi.xres * 2);
    fb->format = GGL_PIXEL_FORMAT_RGB_565;
    ro = vi.red.offset;
    go = vi.green.offset;
    bo = vi.blue.offset;
    ao = vi.transp.offset;

    
    return fd;
}

static void dumpinfo(struct fb_fix_screeninfo *fi, struct fb_var_screeninfo *vi)
{
    fprintf(stderr,"vi.xres = %d\n", vi->xres);
    fprintf(stderr,"vi.yres = %d\n", vi->yres);
    fprintf(stderr,"vi.xresv = %d\n", vi->xres_virtual);
    fprintf(stderr,"vi.yresv = %d\n", vi->yres_virtual);
    fprintf(stderr,"vi.xoff = %d\n", vi->xoffset);
    fprintf(stderr,"vi.yoff = %d\n", vi->yoffset);
    fprintf(stderr, "vi.bits_per_pixel = %d\n", vi->bits_per_pixel);

    fprintf(stderr, "fi.line_length = %d\n", fi->line_length);

}

JNIEXPORT jint JNICALL Java_com_drx2_bootmanager_screenshot_ScreenShotLib_takeScreenShot
  (JNIEnv * env, jobject obj, jstring string1) {
  //get screen capture
  const char *framebuffer = (*env)->GetStringUTFChars(env, string1, 0);
  gr_fb_fd = get_framebuffer(gr_framebuffer, framebuffer);
  (*env)->ReleaseStringUTFChars(env, string1, framebuffer);
  
  if (gr_fb_fd <= 0) exit(1);

  int w = vi.xres, h = vi.yres, depth = vi.bits_per_pixel;
  	
  //convert pixel data
  uint8_t *rgb24;
  if (depth == 16)
  {
	rgb24 = (uint8_t *)malloc(w * h * 3); 
	int i = 0;
	for (;i<w*h;i++)
	{
		uint16_t pixel16 = ((uint16_t *)gr_framebuffer[0].data)[i];
		// RRRRRGGGGGGBBBBBB -> RRRRRRRRGGGGGGGGBBBBBBBB
		// in rgb24 color max is 2^8 per channel (*255/32 *255/64 *255/32)	
		rgb24[3*i+3]   = (255*(pixel16 & 0xFF000000))/ 32;	//alpha?
		rgb24[3*i+2]   = (255*(pixel16 & 0x001F))/ 32; 		//Blue
		rgb24[3*i+1]   = (255*((pixel16 & 0x07E0) >> 5))/64;	//Green
		rgb24[3*i]     = (255*((pixel16 & 0xF800) >> 11))/32; 	//Red
	}
  }
  else
  if (depth == 32)
  {
	
  	rgb24 = (uint8_t *) gr_framebuffer[0].data;
	
  }
  else 
  {
  	//free
        close(gr_fb_fd);
	exit(2);
  };
  //save RGB 24 Bitmap
  int bytes_per_pixel = 4;
  BMPHEAD bh;
  memset ((char *)&bh,0,sizeof(BMPHEAD)); // sets everything to 0 
  //bh.filesize  =   calculated size of your file (see below)
  //bh.reserved  = two zero bytes
  bh.headersize  = 54L;			// for 24 bit images
  bh.infoSize  =  0x28L;		// for 24 bit images
  bh.width     = w;			// width of image in pixels
  bh.depth     = h;			// height of image in pixels
  bh.biPlanes  =  1;			// for 24 bit images
  bh.bits      = 8 * bytes_per_pixel;	// for 24 bit images
  bh.biCompression = 0L;		// no compression
  int bytesPerLine;
  bytesPerLine = w * bytes_per_pixel;  	// for 24 bit images
  //round up to a dword boundary 
  if (bytesPerLine & 0x0003) 
  {
    	bytesPerLine |= 0x0003;
    	++bytesPerLine;
  }
  bh.filesize = bh.headersize + (long)bytesPerLine * bh.depth;
  FILE * bmpfile;
  //printf("Bytes per line : %d\n", bytesPerLine);
  bmpfile = fopen("/sdcard/BootManager/screenshot.bmp", "wb");
  if (bmpfile == NULL)
  {
	close(gr_fb_fd);
	exit(3);
  }
  fwrite("BM",1,2,bmpfile);
  fwrite((char *)&bh, 1, sizeof (bh), bmpfile);
  //fwrite(rgb24,1,w*h*3,bmpfile);
  char *linebuf;   
  linebuf = (char *) calloc(1, bytesPerLine);
  if (linebuf == NULL)
  {
     	fclose(bmpfile);
	close(gr_fb_fd);
	exit(4);
  }
  int line,x;
  for (line = h-1; line >= 0; line --)
  {
    	// fill line linebuf with the image data for that line 
	for( x =0 ; x < w; x++ )
  	{
		//We need to see if blue or red pixel is first so colors aren't off
		if (ro == 0){ //RGBA8888
   		*(linebuf+x*bytes_per_pixel+0) = *(rgb24 + (x+line*w)*bytes_per_pixel+2);
   		*(linebuf+x*bytes_per_pixel+1) = *(rgb24 + (x+line*w)*bytes_per_pixel+1);
		*(linebuf+x*bytes_per_pixel+2) = *(rgb24 + (x+line*w)*bytes_per_pixel+0);
		*(linebuf+x*bytes_per_pixel+3) = *(rgb24 + (x+line*w)*bytes_per_pixel+3);
		}

		if (bo == 0){ //BGRA8888
   		*(linebuf+x*bytes_per_pixel+0) = *(rgb24 + (x+line*w)*bytes_per_pixel+0);
   		*(linebuf+x*bytes_per_pixel+1) = *(rgb24 + (x+line*w)*bytes_per_pixel+1);
		*(linebuf+x*bytes_per_pixel+2) = *(rgb24 + (x+line*w)*bytes_per_pixel+2);
		*(linebuf+x*bytes_per_pixel+3) = *(rgb24 + (x+line*w)*bytes_per_pixel+3);
		}
  	}
	// remember that the order is BGR and if width is not a multiple
       	// of 4 then the last few bytes may be unused
	fwrite(linebuf, 1, bytesPerLine, bmpfile);
  }
  fclose(bmpfile);
  close(gr_fb_fd);
  return (0);
}



