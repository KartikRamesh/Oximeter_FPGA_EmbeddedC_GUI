
#include <stdio.h>
#include <string.h>
#include <math.h>
#include "platform.h"
#include "xil_printf.h"
#include "xparameters.h"
#include "xscugic.h"
#include "xil_exception.h"
#include "xscutimer.h"
#include "sleep.h"
#include "poxivpmn_cfg.h"


// ---- interrupt controller -----
static XScuGic  Intc;					// interrupt controller instance
static XScuGic_Config  *IntcConfig;		// configuration instance

// ---- scu timer -----
static XScuTimer  pTimer;				// private Timer instance
static XScuTimer_Config  *pTimerConfig;	// configuration instance


static volatile unsigned int Led_Output;
static volatile unsigned int ISR_Count;
static volatile unsigned int ISR_CountP;


/*
 * ------------------------------------------------------------
 * Send config data to ADC and read conversion result (AD7887)
 * ------------------------------------------------------------
 */
static double ReadADC(void) //
{
	int k;
	unsigned int SpiDone;
	double ADC_read =0;
	POXIREG(2) = SPI_Conf_ADC;// ADC CS & Length is 16bits

	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));

	//Disable the on-chip reference
	POXIREG(3) = ADC_SCh_PMode2;// Control register

	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	ADC_read = (double)(0xFFFF & rcvData);


	return ADC_read;// Conversion result is coded in 16 bits*/

}
/*
 * ------------------------------------------------------------
 * ADC Off (AD7887)
 * ------------------------------------------------------------
 */
static void ADCOff(void) //
{
	int k;
	unsigned int SpiDone;
	POXIREG(2) = SPI_Conf_ADC;// ADC CS & Length is 16bits

	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));

	//Disable the on-chip reference
	POXIREG(3) = 0x00200000;// bit num 5
	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	print("ADC Off: The onchip reference disable!\n\r");
}
/*
 * ------------------------------------------------------------
 * DAC Initialize (AD5624R_5644R_5664R)
 * ------------------------------------------------------------
 */
static void InitDAC(void) //
{
	int k;
	unsigned int SpiDone;
	POXIREG(2) = SPI_Conf_DAC;// DAC CS & Length is 24bits
	//Reset DAC
	POXIREG(3) = 0x00280001;
	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	print("DAC reset!\n\r");

	//Power Up DAC
	POXIREG(3) = 0x0020000F;
	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	print("DAC Power Up!\n\r");

	//Internal reference setup (on/off )
	POXIREG(3) = 0x00380001;
	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	print("DAC Internal reference setup!\n\r");

	//LDAC register setup
	POXIREG(3) = 0x00300000;
	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	print("DAC LDAC register setup!\n\r");

}
/*
 * ------------------------------------------------------------
 * DAC Off (AD5624R_5644R_5664R)
 * ------------------------------------------------------------
 */
static void DACOff(void) //
{
	int k;
	unsigned int SpiDone;
	POXIREG(2) = SPI_Conf_DAC;// DAC CS & Length is 24bits
	//Reset DAC
	POXIREG(3) = 0x00280001;
	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	print("DAC reset!\n\r");

	//Power Up DAC
	POXIREG(3) = 0x0020001F;
	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	print("DAC Power Down!\n\r");
}
/*
 * ------------------------------------------------------------
 * DAC MOSI Write (AD5624R_5644R_5664R)
 * ------------------------------------------------------------
 */
static void WriteDAC(int channel, int value) //
{
	int k;
	unsigned int SpiDone;
	//xil_printf("Write DAC channel [%d] with value : %x\n\r", channel, value);
	POXIREG(2) = SPI_Conf_DAC;// DAC CS & Length is 24bits

	if((channel ==0) | (channel ==1) | (channel ==2) | (channel ==3) | (channel ==4)){
		k = 0;
		do {
			SpiDone = POXIREG(1);
			k++;
		} while (((SpiDone & 0x01) == 0) && (k < 10000));

		//Write to and update DAC channel n is 3
		POXIREG(3) = (3<<19) | (channel<<16) | ((value & 0x0FFF)<<4);//input shift register is 24bits

		k = 0;
		do {
			SpiDone = POXIREG(1);
			k++;
		} while (((SpiDone & 0x01) == 0) && (k < 10000));


	}
	else{
		//xil_printf(" *** invalid DAC channel: %d\n\r", channel);
	}
}
/*
 * ------------------------------------------------------------
 * PGA MOSI Write (MC_1789213)
 * ------------------------------------------------------------
 */
static void WritePGA(unsigned int gain) //
{
	int k;
	unsigned int SpiDone;
	int gain_cmd=0; //gain 1 default
	InitDAC();

	POXIREG(2) = SPI_Conf_PGA;// PGA CS & cpha=1 & Length is 16bits
	switch(gain)
	{
		case 1: gain_cmd = 0;break;
		case 2: gain_cmd = 1;break;
		case 4: gain_cmd = 2;break;
		case 5: gain_cmd = 3;break;
		case 8: gain_cmd = 4;break;
		case 10: gain_cmd = 5;break;
		case 16: gain_cmd = 6;break;
		case 32: gain_cmd = 7;break;
		default: gain_cmd = 4;
	}
	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));

	//Write to register is 2
	POXIREG(3) = (2<<21) + (gain_cmd<<8);//Instruction register is 8bits and Gain register is 8bits

	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));


}
/*
 * ------------------------------------------------------------
 * PGA Off (MC_1789213)
 * ------------------------------------------------------------
 */
static void PGAOff(void) //
{
	int k;
	unsigned int SpiDone;

	print(" PGA Shutdown Mode...\n");
	POXIREG(2) = SPI_Conf_PGA;// PGA CS & cpha=1 & Length is 16bits

	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));

	//Write to register is 2
	POXIREG(3) = 1<<21 ;//PGA enters Shutdown Mode as soon as a full 16-bit word is sent and CS is raised

	k = 0;
	do {
		SpiDone = POXIREG(1);
		k++;
	} while (((SpiDone & 0x01) == 0) && (k < 10000));
	print(" ...PGA Off!\n");

}

/*
 * ---------------------------------------------------------------------------------
 * Filter Initialization
 * ---------------------------------------------------------------------------------
 */
static Filterobj HP_f,LP_f;
void POxiFilter_Init(void)
{
	HP_f.b0 = BH_0;
	HP_f.b1 = BH_1;
	HP_f.b2 = BH_2;
	HP_f.a1 = AH_1;
	HP_f.a2 = AH_2;
	HP_f.u_k1 = 0.0;
	HP_f.u_k2 = 0.0;
	HP_f.y_k1 = 0.0;
	HP_f.y_k2 = 0.0;
	HP_f.y_out = 0.0;
	LP_f.b0 = BL_0;
	LP_f.b1 = BL_1;
	LP_f.b2 = BL_2;
	LP_f.a1 = AL_1;
	LP_f.a2 = AL_2;
	LP_f.u_k1 = 0.0;
	LP_f.u_k2 = 0.0;
	LP_f.y_k1 = 0.0;
	LP_f.y_k2 = 0.0;
	LP_f.y_out = 0.0;
}
/*
 * ---------------------------------------------------------------------------------
 * Filter for read Samples: High pass and Low pass (0.3 Hz to 6 Hz)respectively
 * ---------------------------------------------------------------------------------
 */
static double POxiFilter(double sample) //
{
	HP_f.y_out = HP_f.b0*sample + HP_f.b1*HP_f.u_k1 + HP_f.b2*HP_f.u_k2
	            - HP_f.a1*HP_f.y_k1 - HP_f.a2* HP_f.y_k2;
	HP_f.u_k2 = HP_f.u_k1;
	HP_f.u_k1 = sample;
	HP_f.y_k2 = HP_f.y_k1;
	HP_f.y_k1 = HP_f.y_out;


	LP_f.y_out = LP_f.b0*HP_f.y_out + LP_f.b1*LP_f.u_k1 + LP_f.b2*LP_f.u_k2
	            - LP_f.a1*LP_f.y_k1 - LP_f.a2* LP_f.y_k2;
	LP_f.u_k2 = LP_f.u_k1;
	LP_f.u_k1 = HP_f.y_out;
	LP_f.y_k2 = LP_f.y_k1;
	LP_f.y_k1 = LP_f.y_out;

	return LP_f.y_out;
}

/*
 * ------------------------------------------------------------
 * Interrupt handler (ZYNQ private timer)
 * Tis is the function to be called when the interrupt occurs.
 * ------------------------------------------------------------
 */
static int FinalPulse=0, FindMAX=1, FindMIN=0;
static int Red_samples_index = 0, IRed_samples_index = 0;
static volatile int InterruptsIndex = 0, MaxSampleIndex = 0, nextSamplesIndex = 0, lastSamplesIndex = 0;
static volatile int InterruptsfromMax[Maxprate] = {0};
static float InterruptsMaxAverage = 0, Nxt_InterruptsMaxAverage = 0;
static double FrADCSamples[numSamples],rADCSamples[numSamples],FirADCSamples[numSamples],irADCSamples[numSamples];
static double animatedled, MinSample=65535;
static int MaxSample[Maxprate] = {0};
static int pulseis=0, pulsecnt=0,MaxDebug_1 = 0, MaxDebug_2 = 0, MaxDebug_3 = 0;
static void TimerIntrHandler(void *CallBackRef)
{
	static double SampleValue = 0;
	static double filteredSample =0;
	XScuTimer *TimerInstance = (XScuTimer *)CallBackRef;

	XScuTimer_ClearInterruptStatus(TimerInstance);
	if(ISR_CountP<(int)InterruptsMaxAverage+1)
	{
		if(InterruptsMaxAverage!=0){
			animatedled = ISR_CountP*(MaxAnimatedLED/InterruptsMaxAverage);
		}else{
			animatedled = 0;
		}
		WriteDAC(CH_C, (int)animatedled);
		ISR_CountP++;
	}
	else{
		ISR_CountP=0;
		InterruptsMaxAverage = Nxt_InterruptsMaxAverage;//load new pulse rate
		FinalPulse = FindPulseRate();
		printf("§%1.0f\n",(float)FinalPulse);
	}
	//At each interruption, we will turn ON/OFF red and ired LEDs separately and read the value measured from ADC
	if(InterruptsIndex==65535){
		InterruptsIndex=0;
	}else{
		InterruptsIndex++;
	}
	switch(ISR_Count)
	{
		case 0:
			rd_iron = rdON; // red LED is turned ON
			ISR_Count++;
			break;
		case 1:
			SampleValue = ReadADC();
			rd_iron = ird_rdOFF;
			filteredSample = POxiFilter(SampleValue);
			if (Red_samples_index < numSamples)
			{
				FrADCSamples[Red_samples_index] = filteredSample;
				MaxDebug_3 = MaxDebug_2;//debug
				MaxDebug_2 = MaxDebug_1;//debug
				MaxDebug_1 = filteredSample;//debug
				if((MaxDebug_3 == MaxDebug_2)&(MaxDebug_3 == MaxDebug_1)){
					if(pulsecnt<3){
						pulsecnt++;
						pulseis=1;
					}else{
						pulseis=0;
					}
				}else{
					pulseis=1;
					pulsecnt=0;
				}
				printf("~%f\n",filteredSample);
				rADCSamples[Red_samples_index] = SampleValue;
				Red_samples_index++;
			}else{Red_samples_index = 0;}
			ISR_Count++;
			break;
		case 2:
			rd_iron = irdON; // ired LED is turned ON
			ISR_Count++;
			break;
		case 3:
			SampleValue = ReadADC();
			rd_iron = ird_rdOFF;
			filteredSample = POxiFilter(SampleValue);

			if (IRed_samples_index < numSamples)
			{
				FirADCSamples[IRed_samples_index] = filteredSample;
				printf("/%f\n",filteredSample);
				irADCSamples[IRed_samples_index] = SampleValue;
				IRed_samples_index++;
				if (MaxSampleIndex<Maxprate)
				{
					if (FindMAX==1){
						if (filteredSample>MaxSample[MaxSampleIndex])
						{
							MaxSample[MaxSampleIndex] = filteredSample;
							nextSamplesIndex=0;
						}
						else
						{
							nextSamplesIndex++;
							if(nextSamplesIndex>10)// to be sure that this was the correct maximum
							{
								FindMAX=0;FindMIN=1;
								InterruptsfromMax[MaxSampleIndex]=InterruptsIndex-10;//first case will be skipped
								MaxSampleIndex ++;
								InterruptsIndex=0;
								nextSamplesIndex=0;
								if(MaxSampleIndex==Maxprate)MaxSampleIndex=1;
								MaxSample[MaxSampleIndex]=-65535;//reset Maximum
							}
						}
					}else if(FindMIN==1){
						if (filteredSample<MinSample)
						{
							MinSample = filteredSample;
							nextSamplesIndex=0;
						}
						else
						{
							nextSamplesIndex++;
							if(nextSamplesIndex>5)// to be sure that this was the correct minimum
							{
								FindMAX=1;FindMIN=0;
								nextSamplesIndex=0;
								MinSample=65535;//reset Minimum
							}
						}
					}else{;}

				}else{MaxSampleIndex=0;}

			}else{IRed_samples_index = 0;}
			ISR_Count = 0;
			break;
		default:;
	}
}
/*
 * ------------------------------------------------------------
 * Calculating the Pulse Rate
 * ------------------------------------------------------------
 */

static int FindPulseRate(void){
static double pul=0;
Nxt_InterruptsMaxAverage = (InterruptsfromMax[1]+InterruptsfromMax[2]+InterruptsfromMax[3])/3;
	if(Nxt_InterruptsMaxAverage==0){
		return 0;
	}else if(pulseis==0){
		InterruptsfromMax[1]=0;
		InterruptsfromMax[2]=0;
		InterruptsfromMax[3]=0;
		Nxt_InterruptsMaxAverage=0;
		return 0;
	}else{
		pul= (float)(InterruptFreq/Nxt_InterruptsMaxAverage)*60;
		return (int)pul;
	}
}

static void HelpPrint(void){
	print(" x ----> Exit POxi!\n");
	print(" gain ----> set PGA gain\n");
	print(" pgaoff ----> go to Shutdown Mode!\n");
	print(" initdac ----> initialize DAC!\n");
	print(" dacoff ----> Power down DAC!\n");
	print(" rlight ----> RD_Light_Intensity!\n");
	print(" irlight ----> IR_Light_Intensity!\n");
	print(" amb ----> Set ambient light intensity\n");
	print(" adcoff ----> disable the on-chip reference!\n");
	print(" rdon ----> turn ON rlight!\n");
	print(" iron ----> turn ON irlight!\n");
	print(" off ----> turn OFF lights!\n");
	print(" measure ----> start measuring and save values!\n");
	print(" readreg ----> read slave registers!\n");
	print(" start ----> enable timer interrupt!\n");
	print(" stop ----> disable timer interrupt!\n");
	print(" wrLED ----> Write animated LED for debug!\n");
	print(" rm ----> reset measuring!\n");
	print(" help ----> print help!\n");
	print(">>\n");
}
static int isr_run;

static void startisr(void)
{
	if (isr_run == 0){
		// start scu timer
		print(" * start timer...\n\r");
		XScuTimer_Start(&pTimer);
		isr_run = 1;
	}
	else{
		// scu timer is already started
		print(" * scu timer is already started...\n\r");
	}
	xil_printf("ISR count: %d\n\r", ISR_Count);
}
static void stopisr(void)
{
	if (isr_run == 1)
	{
		// stop scu timer
		xil_printf(" * index sample %d...\n\r", IRed_samples_index);
		print(" * stop timer...\n\r");
		XScuTimer_Stop(&pTimer);
		isr_run = 0;
	}
	else
	{
		//scu timer is already stopped
		print(" * scu timer is already stopped...\n\r");
	}
	xil_printf("ISR count: %d\n\r", ISR_Count);
}
int main()
{
	int  do_continue;
	int entry, entrycase,incorrect=0;
	char cmd_buf[CMDBUF_SIZE];


    init_platform();
    print("--- Pulse Oximeter V2.0 ESD Students ---\n\r");
    ISR_Count = 0;
    ISR_CountP = 0;

    print(" * initialize exceptions...\n\r");
    Xil_ExceptionInit();

    print(" * lookup config GIC...\n\r");
    IntcConfig = XScuGic_LookupConfig(XPAR_SCUGIC_0_DEVICE_ID);
    print(" * initialize GIC...\n\r");
    XScuGic_CfgInitialize(&Intc, IntcConfig, IntcConfig->CpuBaseAddress);

	// Connect the interrupt controller interrupt handler to the hardware
    print(" * connect interrupt controller handler...\n\r");
	Xil_ExceptionRegisterHandler(XIL_EXCEPTION_ID_IRQ_INT,
				(Xil_ExceptionHandler)XScuGic_InterruptHandler, &Intc);

    print(" * lookup config scu timer...\n\r");
    pTimerConfig = XScuTimer_LookupConfig(XPAR_XSCUTIMER_0_DEVICE_ID);
    print(" * initialize scu timer...\n\r");
    XScuTimer_CfgInitialize(&pTimer, pTimerConfig, pTimerConfig->BaseAddr);
    print(" * Enable Auto reload mode...\n\r");
	XScuTimer_EnableAutoReload(&pTimer);
    print(" * load scu timer...\n\r");
    XScuTimer_LoadTimer(&pTimer, TIMER_LOAD_VALUE);

    print(" * set up timer interrupt...\n\r");
    XScuGic_Connect(&Intc, XPAR_SCUTIMER_INTR, (Xil_ExceptionHandler)TimerIntrHandler,
    				(void *)&pTimer);
    print(" * enable interrupt for timer at GIC...\n\r");
    XScuGic_Enable(&Intc, XPAR_SCUTIMER_INTR);
    print(" * enable interrupt on timer...\n\r");
    XScuTimer_EnableInterrupt(&pTimer);

	// Enable interrupts in the Processor.
    print(" * enable processor interrupts...\n\r");
	Xil_ExceptionEnable();

	// scu timer not started
    print(" * scu timer not started...\n\r");
    isr_run = 0;

	////////////////////////////////////////////////////////////////////
	HelpPrint();

	do_continue = 1;
	entrycase = 1;

	do
	{
		switch(entrycase)
        {
        case 1: fflush(stdout);
				fgets(cmd_buf, CMDBUF_SIZE, stdin);
				cmd_buf[CMDBUF_SIZE-1] = '\0';
				entrycase = 2;
                break;
        case 2: entrycase = 3;
				if (cmd_buf[0] == 'x')
				{
					printf(" Exiting Poxi Application!. . .\n");
					incorrect = 0;
					do_continue = 0;
				}
				else if (!strncmp(cmd_buf, "gain", 4))
				{
					printf("Enter PGA gain value:\n 1\n 2\n 4\n 5\n 8\n 10\n 16\n 32\n");
					scanf("%d",&entry);
					WritePGA(entry);
					printf("PGA gain set to: %d\n",entry);
					POxiFilter_Init();
					incorrect = 0;

				}
				else if (!strncmp(cmd_buf, "pgaoff", 6))
				{
					PGAOff();
					printf("PGA set to shutdown Mode\n");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "initdac", 7))
				{
					InitDAC();
					printf("DAC is Initialized\n");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "dacoff", 6))
				{
					DACOff();
					printf("DAC has been shutdown\n");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "adcoff", 6))
				{
					ADCOff();
					printf("ADC has been shutdown\n");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "rdon", 4))
				{
					rd_iron = rdON;
					printf("RED LED is ON\n");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "iron", 4))
				{
					rd_iron = irdON;
					printf("IR Light is ON\n");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "off", 3))
				{
					rd_iron = ird_rdOFF;
					printf("LEDs are turned OFF\n");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "rlight", 6))
				{
					print("Enter Intensity for Red LED: \n\r");
					scanf("%d",&entry);
					WriteDAC(CH_A, entry);
					printf("Intensity for RED LED set to %d\n",entry);
					incorrect = 0;

				}
				else if (!strncmp(cmd_buf, "irlight", 7))
				{
					print("- Enter Intensity for IR LED: \n\r");
					scanf("%d",&entry);
					WriteDAC(CH_B, entry);
					printf("Intensity for IR LED set to %d\n",entry);
					incorrect = 0;

				}
				else if (!strncmp(cmd_buf, "wrLED", 5))
				{
					print("- Enter Intensity of animated LED: \n\r");
					scanf("%d",&entry);
					WriteDAC(CH_C, entry);
					printf("Intensity of animated LED %d\n",entry);
					incorrect = 0;

				}
				else if (!strncmp(cmd_buf, "amb", 3))
				{
					print("Enter value for amb: \n\r");
					scanf("%d",&entry);
					WriteDAC(CH_D, entry);
					printf("Ambience set to: %d\n",entry);
					incorrect = 0;

				}
				else if (!strncmp(cmd_buf, "measure", 7))
				{
					if(isr_run==0)
					{
						print("Measurement is about to start!\n\rEnter Stop to stop Measuring.");
						XScuTimer_Start(&pTimer);
						isr_run=1;
					}
					else{
						print("Interrupt is already started!!");
					}

					entrycase = 1;
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "start", 5))
				{
					POxiFilter_Init();
					startisr();
					incorrect = 0;

				}
				else if (!strncmp(cmd_buf, "stop", 4))
				{
					stopisr();
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "readreg", 7))
				{
					print(" reg    content (hex)\n\r");
					print("---------------------\n\r");
					for (int kk = 0; kk < 4; kk++) {
						xil_printf("   %d    %x\n\r", kk, (unsigned int)POXIREG(kk));
					}
					print("---------------------\n\r");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "rm", 2))
				{
				    usleep(20000);
					Red_samples_index = 0;
					IRed_samples_index = 0;
					MaxSampleIndex = 0;
					nextSamplesIndex = 0;
					InterruptsIndex=0;
					ISR_Count = 0;
					ISR_CountP = 0;
					pulsecnt =0;
					rd_iron = ird_rdOFF;
					WriteDAC(CH_C, 0);
					MaxSample[0] = 0;MaxSample[1] = 0;MaxSample[2] = 0;MaxSample[4] = 0;
					MinSample=65535;
					InterruptsfromMax[0] = 0;InterruptsfromMax[1] = 0;InterruptsfromMax[2] = 0;InterruptsfromMax[3] = 0;InterruptsfromMax[4] = 0;
					print("- Measurement reset! \n\r");
					incorrect = 0;
				}
				else if (!strncmp(cmd_buf, "help", 4))
				{
					HelpPrint();
					incorrect = 0;
				}
				else if(incorrect != 0)
				{
					printf("** Incorrect Command ***\n");
				}
				else{;}
                break;
        default: entrycase = 1;
        }
	} while (do_continue);

	////////////////////////////////////////////////////////////////////

    print("shutting down...\n\r");
    stopisr();
    // Clear outputs
	Xil_ExceptionDisable();
    XScuTimer_DisableInterrupt(&pTimer);
    XScuGic_Disable(&Intc, XPAR_SCUTIMER_INTR);
    PGAOff();
    ADCOff();
	DACOff();
    usleep(20000);

    print("Thank you for using Pulse Oximeter V2.0 ESD Students.\n\r");
    cleanup_platform();
    return 0;
}
