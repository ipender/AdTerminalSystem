#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <stdint.h>
#include <termios.h>
#include <android/log.h>
#include <sys/ioctl.h>

#include "com_bupt_sensordriver_rfid.h"
#include "spidev.h"
#include "spidev_test.h"

#undef  TCSAFLUSH
#define TCSAFLUSH  TCSETSF
#ifndef _TERMIOS_H_
#define _TERMIOS_H_
#endif

int fd=0;

unsigned char UID[5], Temp[4];

int WriteRawRC(int addr, int data)
{
        int ret;
        //int fd = g_SPI_Fd;
        unsigned char  TxBuf[2];

        //bit7:MSB=0,bit6~1:addr,bit0:RFU=0
        TxBuf[0] = ((unsigned char)addr << 1)&0x7E;
        //TxBuf[0] &= 0x7E;

        TxBuf[1] = (unsigned char)data;

        ret = write(fd, TxBuf, 2);
        if (ret < 0)
                printf("spi:SPI Write error\n");

        usleep(10);

        return ret;
}

unsigned char ReadRawRC(int addr)
{
        int ret;
        //int fd = g_SPI_Fd;
        unsigned char  ReData;
        unsigned char  Address;

        Address  = (unsigned char)addr << 1;
        Address |= (1 << 7);
        Address &= ~(1 << 0);

        ret = write(fd, &Address, 1);
        if (ret < 0)
                printf("spi:SPI Write error\n");

        usleep(100);

        ret = read(fd, &ReData, 1);
        if (ret < 0)
                printf("spi:SPI Read error\n");

        return ReData;
}

void SetBitMask(unsigned char reg,unsigned char mask)
{
        char tmp = 0x0;

        tmp = ReadRawRC(reg) | mask;

        WriteRawRC(reg,tmp | mask);
}

void ClearBitMask(unsigned char reg, unsigned char mask)
{
        char tmp = 0x0;

        tmp = ReadRawRC(reg)&(~mask);

        WriteRawRC(reg, tmp);  // clear bit mask
}

int rc522_init()
{
        int ret;
        char version = 0;

        //reset
        WriteRawRC(CommandReg, PCD_RESETPHASE);
        usleep(10);
        WriteRawRC(ModeReg, 0x3D);
        WriteRawRC(TReloadRegL, 30);
        WriteRawRC(TReloadRegH, 0);
        WriteRawRC(TModeReg, 0x8D);
        WriteRawRC(TPrescalerReg, 0x3E);

        version = ReadRawRC(VersionReg);
        printf("Chip Version: 0x%x\n", version);
        usleep(50000);

        return 0;
}

void PcdAntennaOn()
{
        unsigned char i;

        WriteRawRC(TxASKReg, 0x40);
        usleep(20);

        i = ReadRawRC(TxControlReg);
        if(!(i&0x03))
                SetBitMask(TxControlReg, 0x03);

        i = ReadRawRC(TxASKReg);
}

char PcdComMF522(unsigned char Command, unsigned char *pInData,
                                                unsigned char InLenByte, unsigned char *pOutData,
                                                unsigned int  *pOutLenBit)
{
        char status = MI_ERR;
        unsigned char irqEn  = 0x00;
        unsigned char waitFor = 0x00;
        unsigned char lastBits;
        unsigned char n;
        unsigned int  i;

        switch (Command)
        {
                case PCD_AUTHENT:
                        irqEn   = 0x12;
                          waitFor = 0x10;
                          break;
                case PCD_TRANSCEIVE:
                        irqEn   = 0x77;
                        waitFor = 0x30;
                        break;
                default:
                        break;
        }

        WriteRawRC(ComIEnReg, irqEn|0x80);
        ClearBitMask(ComIrqReg, 0x80);
        WriteRawRC(CommandReg, PCD_IDLE);
        SetBitMask(FIFOLevelReg, 0x80); // Çå¿ÕFIFO
        for(i=0; i<InLenByte; i++)
                WriteRawRC(FIFODataReg, pInData[i]); // Êý¾ÝÐ´ÈëFIFO

        WriteRawRC(CommandReg, Command); // ÃüÁîÐ´ÈëÃüÁî¼Ä´æÆ÷

        if(Command == PCD_TRANSCEIVE)
                SetBitMask(BitFramingReg,0x80); // ¿ªÊ¼·¢ËÍ

        i = 6000; //¸ù¾ÝÊ±ÖÓÆµÂÊµ÷Õû£¬²Ù×÷M1¿¨×î´óµÈ´ýÊ±¼ä25ms
        do
        {
                n = ReadRawRC(ComIrqReg);
                i--;
        }
        while((i!=0)&&!(n&0x01)&&!(n&waitFor));

        ClearBitMask(BitFramingReg, 0x80);

        if(i!=0)
        {
                if(!(ReadRawRC(ErrorReg) & 0x1B))
                {
                        status = MI_OK;
                        if (n&irqEn&0x01)
                                status = MI_NOTAGERR;
                        if(Command == PCD_TRANSCEIVE)
                        {
                                n = ReadRawRC(FIFOLevelReg);

                                lastBits = ReadRawRC(ControlReg) & 0x07;
                                if(lastBits)
                                        *pOutLenBit = (n-1)*8 + lastBits;
                                else
                                        *pOutLenBit = n*8;

                                if(n == 0)
                                        n = 1;
                                if(n>MAXRLEN)
                                        n = MAXRLEN;

                                for (i=0; i<n; i++)
                                        pOutData[i] = ReadRawRC(FIFODataReg);
                        }
                }
                else
                {
                        status = MI_ERR;
                }
        }

        SetBitMask(ControlReg, 0x80);// stop timer now
        WriteRawRC(CommandReg, PCD_IDLE);

        return status;
}

char PcdRequest(unsigned char req_code, unsigned char *pTagType)
{
        char status;
        unsigned int  unLen;
        unsigned char ucComMF522Buf[MAXRLEN];

        ClearBitMask(Status2Reg, 0x08);
        WriteRawRC(BitFramingReg, 0x07);
        SetBitMask(TxControlReg, 0x03);

        ucComMF522Buf[0] = req_code;

        status = PcdComMF522(PCD_TRANSCEIVE, ucComMF522Buf,
                                                1, ucComMF522Buf, &unLen);

        if ((status == MI_OK) && (unLen == 0x10))
        {
                *pTagType     = ucComMF522Buf[0];
                *(pTagType+1) = ucComMF522Buf[1];
        }
        else
        {
                status = MI_ERR;
        }

        return status;
}

char PcdAnticoll(unsigned char *pSnr)
{
        char status;
        unsigned char i, snr_check = 0;
        unsigned int  unLen;
        unsigned char ucComMF522Buf[MAXRLEN];

        ClearBitMask(Status2Reg, 0x08);
        WriteRawRC(BitFramingReg, 0x00);
        ClearBitMask(CollReg, 0x80);

        ucComMF522Buf[0] = PICC_ANTICOLL1;
        ucComMF522Buf[1] = 0x20;

        status = PcdComMF522(PCD_TRANSCEIVE, ucComMF522Buf,
                                                                2, ucComMF522Buf, &unLen);

        if(status == MI_OK)
        {
                for (i=0; i<4; i++)
                {
                        *(pSnr+i)  = ucComMF522Buf[i];
                        snr_check ^= ucComMF522Buf[i];
                }
                if (snr_check != ucComMF522Buf[i])
                {
                        status = MI_ERR;
                }
        }

        SetBitMask(CollReg,0x80);

        return status;
}

int Auto_Reader(void)
{
        int i = 0;
        unsigned long num = 0;

	memset(UID, 0, 5);

        //while(1)
        {
                if(PcdRequest(0x52,Temp) == MI_OK)
                {
                        if(Temp[0]==0x04 && Temp[1]==0x00)
                                printf("MFOne-S50\n");
                        else if(Temp[0]==0x02 && Temp[1]==0x00)
                                printf("MFOne-S70\n");
                        else if(Temp[0]==0x44 && Temp[1]==0x00)
                                printf("MF-UltraLight\n");
                        else if(Temp[0]==0x08 && Temp[1]==0x00)
                                printf("MF-Pro\n");
                        else if(Temp[0]==0x44 && Temp[1]==0x03)
                                printf("MF Desire\n");
                        else
                                printf("Unknown\n");

                        if(PcdAnticoll(UID) == MI_OK)
                        {
                                printf("Card Id is(%d):", num++);
#if 1
                                //for(i=0; i<4; i++)
                                        __android_log_print(ANDROID_LOG_INFO, "rfid id:","%x%x%x%x", 
								UID[0], UID[1],UID[2],UID[3]);
#else                   
                                tochar(UID[0]);
                                tochar(UID[1]);
                                tochar(UID[2]);
                                tochar(UID[3]);
#endif
                                printf("\n");

                                PcdRequest(0x52,Temp);//clear

				return 4;//return lenth
                        }
                        else
                        {
                                printf("no serial num read\n");
                        }
                }
                else
                {
                        printf("No Card!\n");
                }

                //usleep(300000);
		return 0;
        }
}

/*
 * Class:     com_topeet_myproject_rfid
 * Method:    Open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_rfid_Open
  (JNIEnv *env, jobject obj)
  {
	if(fd<=0)fd = open("/dev/rc522", O_RDWR|O_NDELAY|O_NOCTTY);
	if(fd <=0 )
		__android_log_print(ANDROID_LOG_INFO, "rfid", "open /dev/rc522 Error");
	else
	{
		 __android_log_print(ANDROID_LOG_INFO, "rfid", "open /dev/rc522 Sucess fd=%d",fd);
		
		rc522_init();

		PcdAntennaOn();
	}
  }

/*
 * Class:     com_topeet_myproject_rfid
 * Method:    Close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_rfid_Close
  (JNIEnv *env, jobject obj)
  {
	if(fd > 0)close(fd);
  }

/*
 * Class:     com_topeet_myproject_rfid
 * Method:    Ioctl
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_bupt_sensordriver_rfid_Ioctl
  (JNIEnv *env, jobject obj, jint num, jint en)
  {
  	ioctl(fd, en, num);
  }
  
/*
 * Class:     com_topeet_myproject_rfid
 * Method:    Read
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_com_bupt_sensordriver_rfid_Read
  (JNIEnv *env, jobject obj)
 {
		unsigned char buffer[512];
		int BufToJava[512];
		int len=0, i=0;
		
		memset(buffer,0,sizeof(buffer));
		memset(BufToJava,0,sizeof(BufToJava));
		
		len=read(fd, buffer, 512);
	
		if(len<=0)return NULL;	
	
		for(i=0;i<len;i++)
		{
			printf("%x", buffer[i]);
			BufToJava[i] = buffer[i];
		}
		jintArray array = (*env)-> NewIntArray(env, len); 
		(*env)->SetIntArrayRegion(env,array, 0, len, BufToJava);
	
		return array;	
  }
  
/*
 * Class:     com_topeet_myproject_rfid
 * Method:    ReadCardNum
 * Signature: ()[I
 */
JNIEXPORT jbyteArray JNICALL Java_com_bupt_sensordriver_rfid_ReadCardNum
  (JNIEnv *env, jobject obj)
 {
                unsigned char buffer[512];
                int BufToJava[512];
                int len=0, i=0;
#if 0
                memset(buffer,0,sizeof(buffer));
                memset(BufToJava,0,sizeof(BufToJava));

                len=read(fd, buffer, 512);

                if(len<=0)return NULL;

                for(i=0;i<len;i++)
                {
                        printf("%x", buffer[i]);
                        BufToJava[i] = buffer[i];
                }
#else
		len = Auto_Reader();
		if(len<=0)return NULL;

		__android_log_print(ANDROID_LOG_INFO, "rfid id:","%x%x%x%x",
                                                                UID[0], UID[1],UID[2],UID[3]);
                for(i=0;i<len;i++)
                {
                        //printf("%x", UID[i]);
		
                        BufToJava[i] = UID[i];
                }
#endif
                jbyteArray array = (*env)-> NewByteArray(env, len);
                (*env)->SetByteArrayRegion(env,array, 0, len, UID);

		__android_log_print(ANDROID_LOG_INFO, "rfid num:","%x%x%x%x",
                                                                UID[0], UID[1],UID[2],UID[3]);
                return array;
  }

