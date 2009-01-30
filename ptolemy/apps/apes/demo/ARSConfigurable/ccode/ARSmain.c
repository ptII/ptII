/*
 * File: ert_main.c
 *
 * Real-Time Workshop code generated for Simulink model carModel.
 *
 * Model version                        : 1.0
 * Real-Time Workshop file version      : 6.0  (R14)  05-May-2004
 * Real-Time Workshop file generated on : Tue Jan 20 15:04:51 2009
 * TLC version                          : 6.0 (Apr 27 2004)
 * C source code generated on           : Tue Jan 20 15:04:51 2009
 *
 * You can customize this banner by specifying a different template.
 */

#include <stdio.h>                      /* This ert_main.c example uses printf/fflush */
#include <stdlib.h>
#include "carModel_ert_rtw/carModel.h"                   /* Model's header file */
#include "dynamicsControll_ert_rtw/dynamicsControll.h" 
#include "motorController_ert_rtw/motorController.h" 
#include "rtwtypes.h"                   /* MathWorks types */
#include "OSEKCodeWrapper.h"
#include "ARS_OSEK.h"
#include "ARSmain.h"

enum OSEKTasks
{
	appDispatcherTask, dynamicsControllerTask, motorControllerTask
};

enum OSEKEvents
{
	appDispatcherEvent = 0x01, motorControllerEvent=0x02

};

/*****************************************************************************/
static boolean_T OverrunFlag = 0;
double *speed, *front_angle, *rear_angle;
int simStep=0;
int inputSize=0;
bool isExe=FALSE;
bool appRunning=FALSE;

/*****************************************************************************/

int_T main(int_T argc, const char_T *argv[])
{

FILE *outf;
int i;

isExe=TRUE;
if (appStartup()!=0){
	exit(1);
}

  for(simStep=0; simStep < inputSize; simStep++){
//	  printf("Step %d ...\n",step_cnt);

	  if(simStep%5 == 0){
		  dynamicsControll_step();
	  }
	  motorController_step();
	  carModel_step();
	  rear_angle[simStep]=carModel_Y.rearangle;
  }


  /* Terminate model */
  carModel_terminate();
  motorController_terminate();
  dynamicsControll_terminate();

  outf = fopen("output_data.txt","wt");
  if (fopen == NULL){
	  printf("Error opening the output file. Exiting... \n");
	  exit(1);
  }
  
  for (i=0;i<inputSize;i++){
	  fprintf(outf,"%lf\n",rear_angle[i]);
  }

  fclose(outf);

  printf("Done!\n");

  return 0;
}
/*****************************************************************************/

int appStartup(){

	int i;
	FILE *inf;


  printf("Reading model inputs...\n");
  fflush(NULL);

  inf = fopen("input_data.txt","r");
  if (inf == NULL){
	  printf("Error opening the input file. Exiting... \n");
	  return 1;
  }
  fscanf(inf,"%d", &inputSize);
  printf("Input size: %d\n",inputSize);
  speed = (double *)malloc(inputSize*sizeof(double));
  front_angle = (double *)malloc(inputSize*sizeof(double));
  rear_angle = (double *)malloc(inputSize*sizeof(double));

  if((speed == NULL)||(front_angle==NULL)||(rear_angle==NULL)){
	  printf("Memory allocation error. Exiting... \n");
	  return 1;
  }

  for(i=0;i<inputSize;i++){
	  fscanf(inf,"%lf",&(speed[i]));
  }

  for(i=0;i<inputSize;i++){
	  fscanf(inf,"%lf",&(front_angle[i]));
  }
  fclose(inf);

  /* Initialize model */
  dynamicsControll_initialize(1);
  motorController_initialize(1);
  carModel_initialize(1);
  appRunning=TRUE;
  if(!isExe){
	  ActivateTask(motorControllerTask);
	  ActivateTask(appDispatcherTask);
  }
  return 0;
}
/*****************************************************************************/

/* This should be connected to a trigger with the base rate */
/* OSEK task activated at startup */
void appDispatcher(){

	while (appRunning){
		WaitEvent(appDispatcherEvent);

	  if(simStep%5 == 0){
		  ActivateTask(dynamicsControllerTask);
	  }

	  SetEvent(motorControllerTask, motorControllerEvent);
	  carModel_step();
	  simStep++;
	}

	  TerminateTask();

}
/*****************************************************************************/

/* OSEK task activated at startup */

void motorController(){

	while (appRunning){
		WaitEvent(motorControllerEvent);
		motorController_step();
	}
	TerminateTask();	
}
/*****************************************************************************/

/* Interrupt service routine */

void dispatcherIRS(){

	if (appRunning){
		SetEvent(appDispatcherTask, appDispatcherEvent);
	}
	TerminateTask();
}
/*****************************************************************************/
