// Methods for interfacing clients using BSD sockets.
#define NDEBUG 1
/*
********************************************************************
Copyright Notice
----------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy). All rights reserved.

If you have questions about your rights to use or distribute this
software, please contact Berkeley Lab's Technology Transfer Department
at TTD@lbl.gov

NOTICE.  This software was developed under partial funding from the U.S.
Department of Energy.  As such, the U.S. Government has been granted for
itself and others acting on its behalf a paid-up, nonexclusive,
irrevocable, worldwide license in the Software to reproduce, prepare
derivative works, and perform publicly and display publicly.  Beginning
five (5) years after the date permission to assert copyright is obtained
from the U.S. Department of Energy, and subject to any subsequent five
(5) year renewals, the U.S. Government is granted for itself and others
acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
license in the Software to reproduce, prepare derivative works,
distribute copies to the public, perform publicly and display publicly,
and to permit others to do so.


Modified BSD License agreement
------------------------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the
      distribution.
   3. Neither the name of the University of California, Lawrence
      Berkeley National Laboratory, U.S. Dept. of Energy nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes,
patches, or upgrades to the features, functionality or performance of
the source code ("Enhancements") to anyone; however, if you choose to
make your Enhancements available either publicly, or directly to
Lawrence Berkeley National Laboratory, without imposing a separate
written license agreement for such Enhancements, then you hereby grant
the following license: a non-exclusive, royalty-free perpetual license
to install, use, modify, prepare derivative works, incorporate into
other computer software, distribute, and sublicense such enhancements or
derivative works thereof, in binary and source code form.

********************************************************************
*/

///////////////////////////////////////////////////////
/// \file   utilSocket.c
///
/// \brief  Methods for interfacing clients
///         using BSD sockets.
///
/// \author Michael Wetter,
///         Simulation Research Group, 
///         LBNL,
///         MWetter@lbl.gov
///
/// \date   2007-12-01
///
/// \version $Id$
///
/// This file provides methods that allow clients to
/// establish a socket connection. Clients typically call
/// the method \c establishclientsocket()
/// once, and then call the method 
/// \c exchangewithsocket() in each time step.
///
/// \sa establishclientsocket
/// \sa exchangewithsocket
///
///////////////////////////////////////////////////////
#include "utilSocket.h"

////////////////////////////////////////////////////////////////
/// Appends a character array to another character array.
///
/// The array size of \c buffer may be extended by this function
/// to prevent a buffer overflow. If \c realloc fails to allocate
/// new memory, then this function calls \c perror(...) and
/// returns \c EXIT_FAILURE.
///
///\param buffer The buffer to which the character array will be added.
///\param toAdd The character array that will be appended to \c buffer
///\param bufLen The length of the character array \c buffer. This parameter will
///              be set to the new size of \c buffer if memory was reallocated.
///\return 0 if no error occurred.
int save_append(char* *buffer, const char *toAdd, int *bufLen){
  const int size = 1024;
  const int nNewCha = strlen(toAdd);
  const int nBufCha = strlen(*buffer);
  // reallocate memory if needed
  if ( *bufLen < nNewCha + nBufCha + 1){
    *bufLen = *bufLen + size * (((nNewCha + nBufCha) / size)+1); 
    *buffer = realloc(*buffer, *bufLen);
    if (*buffer == NULL) {
      perror("Realloc failed in save_append.");
#ifdef NDEBUG
      fprintf(f1, "Realloc failed in save_append.\n");
#endif
      return EXIT_FAILURE;
    }
  }
  // append toAdd to buffer
  strcpy(*buffer + strlen(*buffer), toAdd);
  return 0;
}

////////////////////////////////////////////////////////////////
/// Assembles the buffer that will be exchanged through the IPC.
///
///\param flag The communication flag.
///\param nDbl The number of double values.
///\param nInt The number of integer values.
///\param nBoo The number of boolean values.
///\param dblVal The array that stores the double values.
///\param intVal The array that stores the integer values.
///\param booVal The array that stores the boolean values.
///\param buffer The buffer into which the values will be written.
///\param bufLen The buffer length prior and after the call.
///\return 0 if no error occurred.
int assembleBuffer(int flag,
		    int nDbl, int nInt, int nBoo,
		    double curSimTim,
		    double dblVal[], int intVal[], int booVal[],
		    char* *buffer, int *bufLen)
{
  int i;
  int retVal;
  const int ver = 1;
  char temCha[1024]; // temporary character array
  memset((char*) *buffer, '\0', *bufLen);
  // Set up how many values will be in buffer
  // This is an internally used version number to make update
  // of the format possible later without braking old versions
  sprintf(temCha, "%d ", ver);
  retVal = save_append(buffer, temCha, bufLen);
  if ( retVal != 0 ) return retVal;
  sprintf(temCha, "%d ", flag);
  retVal = save_append(buffer, temCha, bufLen);
  if ( retVal != 0 ) return retVal;
  if ( flag == 0 ){
    // Only process data if the flag is zero.
    sprintf(temCha, "%d ", nDbl);
    retVal = save_append(buffer, temCha, bufLen);
    if ( retVal != 0 ) return retVal;
    sprintf(temCha, "%d ", nInt);
    retVal = save_append(buffer, temCha, bufLen);
    if ( retVal != 0 ) return retVal;
    sprintf(temCha, "%d ", nBoo);
    retVal = save_append(buffer, temCha, bufLen);
    if ( retVal != 0 ) return retVal;
    sprintf(temCha,"%20.15e ", curSimTim); 
    retVal = save_append(buffer, temCha, bufLen);
    if ( retVal != 0 ) return retVal;
    // add values to buffer
    for(i = 0; i < nDbl; i++){
      sprintf(temCha,"%20.15e ", dblVal[i]); 
      retVal = save_append(buffer, temCha, bufLen);
      if ( retVal != 0 ) return retVal;
    }
    for(i = 0; i < nInt; i++){
      sprintf(temCha,"%d ", intVal[i]); 
      retVal = save_append(buffer, temCha, bufLen);
      if ( retVal != 0 ) return retVal;
    }
    for(i = 0; i < nBoo; i++){
      sprintf(temCha,"%d ", booVal[i]); 
      retVal = save_append(buffer, temCha, bufLen);
      if ( retVal != 0 ) return retVal;
    }
  }
  // For the Java server to read the line, the line 
  // needs to be terminated with '\n'
  sprintf(temCha,"\n");
  retVal = save_append(buffer, temCha, bufLen);
  if ( retVal != 0 ) return retVal;
  // No error, return 0
  return 0;
}

/////////////////////////////////////////////////////////////////
/// Gets an integer and does the required error checking.
///
///\param nptr Pointer to character buffer that contains the number.
///\param endptr After return, this variable contains a pointer to the 
///            character after the last character of the number.
///\param base Base for the integer.
///\param The value contained in the character buffer.
///\return 0 if no error occurred.
int getIntCheckError(const char *nptr, char **endptr, const int base,
		      int* val){
  errno = 0; // must reset errno to 0
  long results = strtol(nptr, endptr, base);  
  /////////////////////////////////////////////////////////////////
  // do error checking
  if ((errno == ERANGE
       && (results == LONG_MAX
	   || results == LONG_MIN))
      || (errno != 0 && results == 0)) {
    perror("strtol caused error.");
    if (strlen(nptr) < 1) {
        fprintf(stderr, "strtol() was called with a string of length less than 1. This can occur when no data is read.\n");
    } else {
        fprintf(stderr, "strtol was called with strtol(%s, %x, %d)\n", nptr, endptr, base);
    }
    return EXIT_FAILURE;
  }
  if (results > INT_MAX
      || results < INT_MIN) {
    //  64 bit problems here.  Under 64 bit linux, int is 4 bytes,
    // long is 8 bytes.  Under MacOS X, int is 4 bytes, long is 4 bytes.
    perror("strtol returned a long outside the range of an int.");
    return EXIT_FAILURE;
  }
  if (*endptr == nptr) {
    fprintf(stderr, "Error: No digits were found in getIntCheckError.\n");
    fprintf(stderr, "Further characters after number: %s\n", endptr);
    fprintf(stderr, "Sending EXIT_FAILURE = : %d\n", EXIT_FAILURE);
    return EXIT_FAILURE;
  }
  val = (int *) results;  
  return 0;
}

/////////////////////////////////////////////////////////////////
/// Gets a double and does the required error checking.
///
///\param nptr Pointer to character buffer that contains the number.
///\param endptr After return, this variable contains a pointer to the 
///            character after the last character of the number.
///\param The value contained in the character buffer.
///\return 0 if no error occurred.
int getDoubleCheckError(const char *nptr, char **endptr, 
			double* val){
  errno = 0; // must reset errno to 0
  *val = strtod(nptr, endptr);  
  /////////////////////////////////////////////////////////////////
  // do error checking
  if ((errno == ERANGE && (*val == HUGE_VAL || *val == -HUGE_VAL))
      || (errno != 0 && *val == 0)) {
    perror("strtod caused error.");
    return EXIT_FAILURE;
  }
  if (*endptr == nptr) {
    fprintf(stderr, "Error: No digits were found in getDoubleCheckError.\n");
    fprintf(stderr, "Further characters after number: %s\n", endptr);
    fprintf(stderr, "Sending EXIT_FAILURE = : %d\n", EXIT_FAILURE);
    return EXIT_FAILURE;
  }
  return 0;
}

/////////////////////////////////////////////////////////////////
/// Disassembles the buffer that has been received through the IPC.
///
///\param buffer The buffer that contains the values to be parsed.
///\param flag The communication flag.
///\param nDbl The number of double values received.
///\param nInt The number of integer values received.
///\param nBoo The number of boolean values received.
///\param dblVal The array that stores the double values.
///\param intVal The array that stores the integer values.
///\param booVal The array that stores the boolean values.
///\return 0 if no error occurred.
int disassembleBuffer(const char* buffer,
		      int *fla,
		      int *nDbl, int *nInt, int *nBoo,
		      double *curSimTim,
		      double dblVal[], int intVal[], int booVal[])
{
  const int base = 10;
  char *endptr = 0;
  int retVal;    // return value
  //////////////////////////////////////////////////////
  // Get first few integers to set up dictionaray
  int ver, i;
  // set number of received values to zero to ensure that
  // if retVal != 0, we have the values initialized
  *nDbl = 0;
  *nInt = 0;
  *nBoo = 0;
  *curSimTim = 0;
  // version number
  retVal = getIntCheckError(buffer, &endptr, base, &ver);
  if ( retVal )
    return retVal;
  //////////////////////////////////////////////////////
  // communication flag
  retVal = getIntCheckError(endptr, &endptr, base, fla);
  if ( retVal )
    return retVal;
  // number of doubles, integers and booleans
  retVal = getIntCheckError(endptr, &endptr, base, nDbl);
  if ( retVal ) 
    return retVal;
  retVal = getIntCheckError(endptr, &endptr, base, nInt);
  if ( retVal ) 
    return retVal;
  retVal = getIntCheckError(endptr, &endptr, base, nBoo);
  if ( retVal ) 
    return retVal;
  // current simulation time
  retVal = getDoubleCheckError(endptr, &endptr, curSimTim);
  if ( retVal ) {
#ifdef NDEBUG
    fprintf(f1, "Error while getting the current simulation time.\n");
#endif
    return retVal;
  }
  //////////////////////////////////////////////////////
  // Get doubles
  for(i=0; i < *nDbl; i++){
    retVal = getDoubleCheckError(endptr, &endptr, &dblVal[i]);
    if ( retVal ) {
#ifdef NDEBUG
    fprintf(f1, "Error while getting double %d of %d.\n", i, *nDbl);
#endif
      return retVal;
    }
  }
  //////////////////////////////////////////////////////
  // Get integers
  for(i=0; i < *nInt; i++){
    retVal = getIntCheckError(endptr, &endptr, base, &intVal[i]);
    if ( retVal ){
#ifdef NDEBUG
    fprintf(f1, "Error while getting integer %d of %d.\n", i, *nInt);
#endif
      return retVal;
    }
  }
  //////////////////////////////////////////////////////
  // Get boolean
  for(i=0; i < *nBoo; i++){
    retVal = getIntCheckError(endptr, &endptr, base, &booVal[i]);
    if ( retVal ){
#ifdef NDEBUG
    fprintf(f1, "Error while getting boolean %d of %d.\n", i, *nBoo);
#endif
      return retVal;
    }
  }
  return retVal;
}

/////////////////////////////////////////////////////////////////////
/// Gets the port number for the BSD socket communication.
///
/// This method parses the xml file for the socket number.
/// \param docname Name of xml file.
/// \return the socket port number if successful, or -1 if an error occured.
int getsocketportnumber(const char *const docname) {
  int retVal;
  char *xPat = "//ipc/socket";
  char *atrNam = "port";
  char *res;
  res = malloc(BUFFER_LENGTH);
  if (res == NULL) {
    perror("Realloc failed in getsocketportnumber.");
#ifdef NDEBUG
    fprintf(f1, "Realloc failed in getsocketportnumber.\n");
#endif
    return -1;
  }
  if (0 == getxmlvalue(docname, xPat, atrNam, res))
    retVal = atoi((char*)res);
  else
    retVal = -1;
  free(res);
  return retVal;
}
/////////////////////////////////////////////////////////////////////
/// Gets the hostname for the BSD socket communication.
///
/// This method parses the xml file for the socket host name.
/// \param docname Name of xml file.
/// \param hostname The hostname will be written to this argument.
/// \return 0 if successful, or -1 if an error occured.
int getsockethost(const char *const docname, char *const hostname) {
  char *xPat = "//ipc/socket";
  char *atrNam = "hostname";
  int r = getxmlvalue(docname, xPat, atrNam, hostname);
  return r;
}

//////////////////////////////////////////////////////////////////
/// Establishes a connection to the socket.
///
/// This method establishes the client socket.
///
/// \param docname Name of xml file that contains the socket information.
/// \return The socket file descripter, or a negative value if an error occured.
int establishclientsocket(const char *const docname){
  int portNo, retVal, sockfd;
  char *hostname;
  char *serverIP;

#ifdef _MSC_VER /************* Windows specific code ********/
  struct hostent* FAR server;
  WSADATA wsaData;
  WORD wVersionRequested;
  //  int PASCAL FAR sockWin;
  //  char sockWinChar[20];
#else  /************* End of Windows specific code *******/
  struct hostent *server;
#endif
  //++  char* ip = "127.0.0.1";
  //++  char* port;
  //++  struct addrinfo aiHints;
  //++  struct addrinfo *aiList = NULL;

  //  struct addrinfo *server;

  struct sockaddr_in serAdd;
  const int arg=1; // 1: true
#ifdef NDEBUG
  if (f1 == NULL)
    f1 = fopen ("utilSocket.log", "w");
  if (f1 == NULL){
    fprintf(stderr, "Cannot open file %s\n", "utilSocket.log");
    return -1;
  }
  else
    fprintf(f1, "utilSocket: Establishing socket based on file %s.\n", docname);
#endif
  hostname = malloc(BUFFER_LENGTH);
  if (hostname == NULL) {
    perror("Realloc failed in establishclientsocket.");
#ifdef NDEBUG
    fprintf(f1, "Realloc failed in establishclientsocket.\n");
#endif
    return -1;
  }

  //////////////////////////////////////////////////////
  // get the socket port number
  portNo = getsocketportnumber(docname);
  if ( portNo < 0 ){
#ifdef NDEBUG  
    fprintf(f1, "ERROR: Could not obtain socket port number. Return value = %d.\n", portNo);
#endif
    return portNo;
  }
#ifdef NDEBUG  
    fprintf(f1, "Socket port number = %d.\n", portNo);
#endif
  //////////////////////////////////////////////////////
  // get the socket host name
  retVal = getsockethost(docname, hostname);
  if ( retVal < 0 ){
#ifdef NDEBUG  
    fprintf(f1, "ERROR: Could not obtain socket hostname. Return value = %d.\n", retVal);
#endif
    return retVal;
  }
  // open socket
#ifdef _MSC_VER /************* Windows specific code ********/
    wVersionRequested = MAKEWORD( 2, 2 );
  retVal = WSAStartup( wVersionRequested, &wsaData );
  if ( retVal != 0 ) {
   /* Tell the user that we could not find a usable */
   /* WinSock DLL.                                  */
#ifdef NDEBUG  
    fprintf(f1, "ERROR: Could not find a usable WinSock DLL.\n");
    fprintf(f1, "WSAGetLastError = %d\n", WSAGetLastError());
#endif
    return -1;
  }
  /* Confirm that the WinSock DLL supports 2.2.*/
  /* Note that if the DLL supports versions greater    */
  /* than 2.2 in addition to 2.2, it will still return */
  /* 2.2 in wVersion since that is the version we      */
  /* requested.                                        */
  if ( LOBYTE( wsaData.wVersion ) != 2 ||
       HIBYTE( wsaData.wVersion ) != 2 ) {
    /* Tell the user that we could not find a usable */
    /* WinSock DLL.                                  */
#ifdef NDEBUG  
    fprintf(f1, "ERROR: Could not find a usable WinSock DLL for requested version.\n");
#endif
    WSACleanup( );
    return -1; 
  }
#ifdef NDEBUG  
  fprintf(f1, "WinSock DLL is acceptable.\n");
#endif
  /* The WinSock DLL is acceptable. Proceed. */
#endif /************* End of Windows specific code *******/

  sockfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
  if( sockfd < 0){
#ifdef NDEBUG
    fprintf(f1, "ERROR opening socket. sockfd = %d.\n", sockfd);
#endif
    return sockfd;
  }
#ifdef NDEBUG
  fprintf(f1, "Socket opened, sockfd = %d.\n", sockfd);
#endif
  if( setsockopt(sockfd, SOL_SOCKET, SO_KEEPALIVE, (char *)&arg, sizeof(arg)) != 0){
#ifdef NDEBUG
        fprintf(f1, "ERROR setting socket option keep alive.\n");
        fprintf(f1, "Error flag errno = %d.\n", errno);
#endif
        return -1;
      }
  
  if (sockfd < 0){
#ifdef NDEBUG
    fprintf(f1, "ERROR opening socket\n");
#endif
    fprintf(stderr, "Returning from establish... with sockfd= %d\n", sockfd);
    return sockfd;
  }
  //////////////////////////////////////////////////////
  // establish server address
  server = gethostbyname(hostname);
  free(hostname);
  
  if (server == NULL) {
#ifdef NDEBUG
    fprintf(f1,"ERROR, no such host\n");
#endif
    return -1;
  }
  serverIP = (char*)inet_ntoa(*(struct in_addr *)*server->h_addr_list);
  memset((char *) &serAdd, '\0', sizeof(serAdd));
  serAdd.sin_family = AF_INET;
  // FIXME: inet_addr() is obsolete.
  serAdd.sin_addr.s_addr = inet_addr(serverIP);
#ifdef NDEBUG
  fprintf(f1, "Debugging: utilSocket.c: serverIP: %d, inet_addr: %d\n",
	 serverIP,   serAdd.sin_addr.s_addr);
#endif
  serAdd.sin_port = htons(portNo);
  //////////////////////////////////////////////////////
  // establish connection
  //retVal = connect(sockfd, (void *)&serAdd, sizeof(serAdd));
  retVal = connect(sockfd, (const struct sockaddr*)&serAdd, sizeof(serAdd));
  if ( retVal < 0){
#ifdef NDEBUG
#ifdef _MSC_VER
    fprintf(f1, "ERROR when connecting to socket: WSAGetLastError = %d\n", WSAGetLastError());
#else
    fprintf(f1, "ERROR when connecting to socket: %s\n",  strerror(errno));
#endif
#endif
    return retVal;
  }
  return sockfd;
}

/////////////////////////////////////////////////////////////////
/// Writes data to the socket.
///
/// Clients can call this method to write data to the socket.
///\param sockfd Socket file descripter
///\param flaWri Communication flag to write to the socket stream.
///\param nDblWri Number of double values to write.
///\param nIntWri Number of integer values to write.
///\param nBooWri Number of boolean values to write.
///\param curSimTim Current simulation time in seconds.
///\param dblValWri Double values to write.
///\param intlValWri Integer values to write.
///\param boolValWri Boolean values to write.
///\sa int establishclientsocket(uint16_t *portNo)
///\return The exit value of \c send, or a negative value if an error occured.
int writetosocket(const int *sockfd, 
		  const int *flaWri,
		  const int *nDblWri, const int *nIntWri, const int *nBooWri,
		  double *curSimTim,
		  double dblValWri[], int intValWri[], int booValWri[])
{
  int retVal;
  // buffer used to exchange data
  char *buffer; 
  int bufLen = BUFFER_LENGTH;

#ifdef NDEBUG
  if (f1 == NULL) // open file
    f1 = fopen ("utilSocket.log", "w");
  if (f1 == NULL){
    fprintf(stderr, "Cannot open file %s\n", "utilSocket.log");
    return -1;
  }
#endif

  /////////////////////////////////////////////////////
  // make sure that the socketFD is valid
if (*sockfd < 0 ){
    fprintf(stderr, "ERROR: Called write to socket with negative socket number.\n");
    fprintf(stderr, "       sockfd : %d\n",  *sockfd);
#ifdef NDEBUG
    fprintf(f1, "ERROR: Called write to socket with negative socket number.\n");
    fprintf(f1, "       sockfd : %d\n",  *sockfd);
    fflush(f1);
#endif
    return -1; // return a negative value in case of an error
  }

  /////////////////////////////////////////////////////
  // allocate storage for buffer
#ifdef NDEBUG
  fprintf(f1, "Assembling buffer.\n", *sockfd);
#endif
  buffer = malloc(bufLen);
  if (buffer == NULL) {
    perror("Realloc failed in writetosocket.");
#ifdef NDEBUG
    fprintf(f1, "Realloc failed in writetosocket.\n");
#endif
    return -1;
  }
  //////////////////////////////////////////////////////
  // copy arguments to buffer
  retVal = assembleBuffer(*flaWri, *nDblWri, *nIntWri, *nBooWri, 
			  *curSimTim,
			  dblValWri, intValWri, booValWri, 
			  &buffer, &bufLen);
  if (retVal != 0 ){
    fprintf(stderr, "ERROR: Failed to allocate memory for buffer before writing to socket.\n");
    fprintf(stderr, "       retVal : %d\n",  retVal);
    fprintf(stderr, "       Message: %s\n",  strerror(errno));
#ifdef NDEBUG
    fprintf(f1, "ERROR: Failed to allocate memory for buffer before writing to socket.\n");
    fprintf(f1, "       retVal : %d\n",  retVal);
    fprintf(f1, "       Message: %s\n",  strerror(errno));
    fflush(f1);
#endif
    free(buffer);
    return -1; // return a negative value in case of an error
  }
  //////////////////////////////////////////////////////
  // write to socket
#ifdef NDEBUG
    fprintf(f1, "Write to socket with fd = %d\n", *sockfd);
    fprintf(f1, "Buffer        = %s\n", buffer);
#endif

#ifdef _MSC_VER
    retVal = send(*sockfd,buffer,strlen(buffer), 0);
#else
  retVal = write(*sockfd,buffer,strlen(buffer));
#endif

#ifdef NDEBUG
  if (retVal >= 0)
    fprintf(f1, "Wrote %d characters to socket.\n",  retVal);
  else
    fprintf(f1, "ERROR writing to socket: Return value = %d.\n",  retVal);
#endif
  if (retVal < 0){
#ifdef NDEBUG
#ifdef _MSC_VER
    fprintf(f1, "ERROR writing to socket: WSAGetLastError = %d\n", WSAGetLastError());
#else
    fprintf(f1, "ERROR writing to socket: %s\n",  strerror(errno));
#endif
    fflush(f1);
#endif
  }
  free(buffer);
  return retVal;
}

/////////////////////////////////////////////////////////////////
/// Writes an error flag to the socket stream.
///
/// This method should be used by clients if they experience an
/// error and need to terminate the socket connection.
///
///\param sockfd Socket file descripter
///\param flaWri should be set to a negative value.
int sendclienterror(const int *sockfd, const int *flaWri){
  int zI=0;
  double zD = 0;
  if ( *sockfd >= 0 ){
    return writetosocket(sockfd, flaWri, &zI, &zI, &zI, &zD,
			 NULL, NULL, NULL);
  }
  else
    return 0;
}

/////////////////////////////////////////////////////////////////
/// Reads data from the socket.
///
/// Clients can call this method to exchange data through the socket.
///
///\param sockfd Socket file descripter
///\param flaRea Communication flag read from the socket stream.
///\param nDblRea Number of double values to read.
///\param nIntRea Number of integer values to read.
///\param nBooRea Number of boolean values to read.
///\param curSimTim Current simulation time in seconds read from socket.
///\param dblValRea Double values read from socket.
///\param intlValRea Integer values read from socket.
///\param boolValRea Boolean values read from socket.
///\sa int establishclientsocket(uint16_t *portNo)
int readfromsocket(const int *sockfd, int *flaRea, 
		   int *nDblRea, int *nIntRea, int *nBooRea,
		   double *curSimTim,
		   double dblValRea[], int intValRea[], int booValRea[])
{
  int retVal, i;
  char inpBuf[BUFFER_LENGTH]; 
  memset(inpBuf, 0, BUFFER_LENGTH);

  /////////////////////////////////////////////////////
  // make sure that the socketFD is valid
if (*sockfd < 0 ){
    fprintf(stderr, "ERROR: Called read from socket with negative socket number.\n");
    fprintf(stderr, "       sockfd : %d\n",  *sockfd);
#ifdef NDEBUG
    fprintf(f1, "ERROR: Called read from socket with negative socket number.\n");
    fprintf(f1, "       sockfd : %d\n",  *sockfd);
    fflush(f1);
#endif
    return -1; // return a negative value in case of an error
  }

#ifdef _MSC_VER
    // MSG_WAITALL is not in the winsock2.h file, at least not on my system...
#define MSG_WAITALL 0x8 /* do not complete until packet is completely filled */
  retVal = recv(*sockfd, inpBuf, BUFFER_LENGTH-1, 0);//, MSG_OOB);
  //  retVal = recv(*sockfd, inpBuf, BUFFER_LENGTH-1, MSG_WAITALL);
#else
  retVal = read(*sockfd, inpBuf, BUFFER_LENGTH-1);
#endif

  if (retVal < 0){
#ifdef NDEBUG
#ifdef _MSC_VER
    fprintf(f1, "ERROR reading: WSAGetLastError = %d\n", WSAGetLastError());
#else
    fprintf(f1, "ERROR reading: %s\n",  strerror(errno));
#endif
    fflush(f1);
#endif
    return retVal;
  }
  //////////////////////////////////////////////////////
  // disassemble buffer and store values in function argument
#ifdef NDEBUG
  fprintf(f1, "Disassembling buffer.\n");
#endif

  retVal = disassembleBuffer(inpBuf,
			     flaRea,
			     nDblRea, nIntRea, nBooRea, 
			     curSimTim,
			     dblValRea, intValRea, booValRea);
#ifdef NDEBUG
  fprintf(f1, "Disassembled buffer.\n");
#endif
  return retVal;
}

/////////////////////////////////////////////////////////////////
/// Exchanges data with the socket.
///
/// Clients can call this method to exchange data through the socket.
///\param sockfd Socket file descripter
///\param flaWri Communication flag to write to the socket stream.
///\param flaRea Communication flag read from the socket stream.
///\param nDblWri Number of double values to write.
///\param nIntWri Number of integer values to write.
///\param nBooWri Number of boolean values to write.
///\param nDblRea Number of double values to read.
///\param nIntRea Number of integer values to read.
///\param nBooRea Number of boolean values to read.
///\param simTimWri Current simulation time in seconds to write.
///\param dblValWri Double values to write.
///\param intlValWri Integer values to write.
///\param boolValWri Boolean values to write.
///\param simTimRea Current simulation time in seconds read from socket.
///\param dblValRea Double values read from socket.
///\param intlValRea Integer values read from socket.
///\param boolValRea Boolean values read from socket.
///\sa int establishclientsocket(uint16_t *portNo)
///\return The exit value of \c send or \c read, or a negative value if an error occured.
int exchangewithsocket(const int *sockfd, 
		       const int *flaWri, int *flaRea,
		       const int *nDblWri, const int *nIntWri, const int *nBooWri,
		       int *nDblRea, int *nIntRea, int *nBooRea,
		       double *simTimWri,
		       double dblValWri[], int intValWri[], int booValWri[],
		       double *simTimRea,
		       double dblValRea[], int intValRea[], int booValRea[]){
  int retVal;
#ifdef NDEBUG
  if (f1 == NULL)
    f1 = fopen ("utilSocket.log", "w");
  if (f1 == NULL){
    fprintf(stderr, "Cannot open file %s\n", "utilSocket.log");
    return -1;
  }
  rewind(f1);
  fprintf(f1, "*** BCVTB client log file.\n", *simTimWri);
  fprintf(f1, "*************************.\n", *simTimWri);
  fprintf(f1, "Writing to socket at time = %e\n", *simTimWri);
#endif

  retVal = writetosocket(sockfd, flaWri, 
			 nDblWri, nIntWri, nBooWri,
			 simTimWri,
			 dblValWri, intValWri, booValWri);
  if ( retVal >= 0 ){
#ifdef NDEBUG
  fprintf(f1, "Reading from socket.\n");
  fflush(f1);
#endif
    retVal = readfromsocket(sockfd, flaRea,
			    nDblRea, nIntRea, nBooRea,
			    simTimRea,
			    dblValRea, intValRea, booValRea);
  }
#ifdef NDEBUG
  fprintf(f1, "Finished exchanging data with socket: simTimRea=%e, flag=%d.\n", *simTimRea, retVal);
  fflush(f1);
#endif
  return retVal;
}

///////////////////////////////////////////////////////////
/// Closes the inter process communication socket.
///
///\param sockfd Socket file descripter.
///\return The return value of the \c close function.
int closeipc(int* sockfd){
#ifdef _MSC_VER
  return closesocket(*sockfd);
#else
  return close(*sockfd);
#endif
}

#include <stdio.h>
int main( int argc, const char* argv[] )
{
  int i, retVal;
  char *buffer;
  const char* toAdd = "abcd ef";
  int bufLen=1; // just for testing, a better setting would be 1024
  int flaWri=0;
  const int nDbl=3;
  double dbl[] = {1, 2, 3};
  const int nInt = 2;
  int inte[] = {10, 20};
    
  printf( "\nHello World\n\n" );
#ifdef NDEBUG
  if (f1 == NULL)
    f1 = fopen ("utilSocket.log", "w");
#endif
  buffer = malloc(bufLen);

  for(i=0; i < 4; i++){
    retVal = assembleBuffer(flaWri, nDbl, nInt, nInt,
			  60.0,
			    dbl, inte, inte,
			  &buffer, &bufLen);

  //    save_append(buffer, toAdd, &bufLen);
    fprintf(stderr, "main: buffer        = %s.\n", buffer);
    fprintf(stderr, "main: string length = %d.\n", strlen(buffer));
  }
  printf( "\nEnd of program\n\n" );
}
