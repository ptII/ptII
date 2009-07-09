/*
@Copyright (c) 2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

						PT_COPYRIGHT_VERSION_2
						COPYRIGHTENDKEY


*/
///////////////////////////////////////////////////////
/// \file   cclient.c
///
/// \brief  Simple simulation program to illustrate
///         implementation of a client.
///
/// \author Michael Wetter,
///         Simulation Research Group, 
///         LBNL,
///         MWetter@lbl.gov
///
/// \date   2007-12-01
///
/// This file is a simple simulation program written 
/// in C to illustrate how to implement a client.
/// The program simulates two rooms, each represented
/// by a first order ordinary differential equation
/// that describes the time rate of change of the
/// room temperature.
/// Input to the room model is the control signal
/// for a heater. The control signal is obtained from
/// Ptolemy II. Output of the model is the room 
/// temperature, which is sent to Ptolemy II.
/// The differential equation is solved using an 
/// explicit Euler integration.
///
///////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
//#include <unistd.h> // for sleep 
#include "utilSocket.h"

//////////////////////////////////////////////////////
/// Main function
int main(int argc, char *argv[]){
  //////////////////////////////////////////////////////
  // Declare variables for the socket communication
  // File name used to get the port number
  const char *const simCfgFilNam = "socket.cfg";
  // client error flag
  const int cliErrFla = -1;
  // Flags to exchange the status of the simulation program 
  // and of the middleware.
  int flaWri = 0;
  int flaRea = 0;
  // Number of variables to be exchanged
  const int nDblWri = 2;
  const int nIntWri = 0;
  const int nBooWri = 0;
  int nDblRea, nIntRea, nBooRea;
  // Number of rooms
  int nRoo =2;
  // Arrays that contain the variables to be exchanged
  double dblValWri[2];
  int intValWri[1]; // zero array's not allowed for MS compiler
  int booValWri[1]; // zero array's not allowed for MS compiler
  double dblValRea[2];
  int intValRea[1]; // zero array's not allowed for MS compiler
  int booValRea[1]; // zero array's not allowed for MS compiler
  int i, sockfd, retVal;
  // set simulation time step
  double delTim;

  //////////////////////////////////////////////////////
  // Declare variables of the room model
  double simTimWri = 0;
  double simTimRea = 0;
  double TIni   = 10;
  double tau    = 2*3600;
  double Q0Hea  = 100;
  double UA     = Q0Hea / 20;
  double TOut   = 5;
  double C[]    = {tau*UA, 2*tau*UA};
  double TRoo[] = {TIni, TIni};

  double y[]    = {0, 0};
  //////////////////////////////////////////////////////
  if (argc <= 1) {
    printf("Usage: %s simulation_timestep_in_seconds\n", argv[0]);
    return(1);
  }
  delTim = atof(argv[1]);
  fprintf(stderr,"Simulation model has time step %8.5g\n", delTim);
  /////////////////////////////////////////////////////////////
  // Establish the client socket
  sockfd = establishclientsocket(simCfgFilNam);
  if (sockfd < 0){
    fprintf(stderr,"Error: Failed to obtain socket file descriptor. sockfd=%d.\n", sockfd);
    exit((sockfd)+100);
  }

  /////////////////////////////////////////////////////////////
  // Simulation loop
  while(1){
    /////////////////////////////////////////////////////////////
    // assign values to be exchanged
    for(i=0; i < nDblWri; i++)
      dblValWri[i]=TRoo[i];
    for(i=0; i < nIntWri; i++)
      intValWri[i]=0;
    for(i=0; i < nBooWri; i++)
      booValWri[i]=1;

    /////////////////////////////////////////////////////////////
    // Exchange values
    retVal = exchangewithsocket(&sockfd, &flaWri, &flaRea,
				&nDblWri, &nIntWri, &nBooWri,
				&nDblRea, &nIntRea, &nBooRea,
				&simTimWri,
				dblValWri, intValWri, booValWri,
				&simTimRea,
				dblValRea, intValRea, booValRea);
    /////////////////////////////////////////////////////////////
    // Check flags
    if (retVal < 0){
      sendclienterror(&sockfd, &cliErrFla);
      printf("Simulator received value %d when reading from socket. Exit simulation.\n", retVal);
      closeipc(&sockfd);
      exit((retVal)+100);
    }

    if (flaRea == 1){
      printf("Simulator received end of simulation signal from server. Exit simulation.\n");
      closeipc(&sockfd);
      exit(0);
    }

    if (flaRea != 0){
      printf("Simulator received flag = %d from server. Exit simulation.\n", flaRea);
      closeipc(&sockfd);
      exit(1);
    }
    /////////////////////////////////////////////////////////////
    // No flags found that require the simulation to terminate. 
    // Assign exchanged variables
    for(i=0; i < nRoo; i++)
      y[i] = dblValRea[i];

    /////////////////////////////////////////////////////////////
    // Having obtained y_k, we compute the new state x_k+1 = f(y_k)
    // This is the actual simulation of the client, such as an
    // EnergyPlus time step
    for(i=0; i < nRoo; i++)
      TRoo[i] = TRoo[i] + delTim/C[i] * ( UA * (TOut-TRoo[i] ) + Q0Hea * y[i] );
    simTimWri += delTim; // advance simulation time
  } // end of simulation loop
}

