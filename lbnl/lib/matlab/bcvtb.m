function [methodinfo,structs,enuminfo,ThunkLibName]=bcvtb
%BCVTB Create structures to define interfaces found in 'matlabSocket'.

%This function was generated by loadlibrary.m parser version 1.1.6.29 on Thu Jul  9 13:46:33 2009
%perl options:'matlabSocket.i -outfile=bcvtb.m'
ival={cell(1,0)}; % change 0 to the actual number of functions to preallocate the data.
structs=[];enuminfo=[];fcnNum=1;
fcns=struct('name',ival,'calltype',ival,'LHS',ival,'RHS',ival,'alias',ival);
ThunkLibName=[];
% int establishclientsocket ( const char * const docname ); 
fcns.name{fcnNum}='establishclientsocket'; fcns.calltype{fcnNum}='cdecl'; fcns.LHS{fcnNum}='int32'; fcns.RHS{fcnNum}={'cstring'};fcnNum=fcnNum+1;
% int sendclienterror ( const int * sockfd , const int * flaWri ); 
fcns.name{fcnNum}='sendclienterror'; fcns.calltype{fcnNum}='cdecl'; fcns.LHS{fcnNum}='int32'; fcns.RHS{fcnNum}={'int32Ptr', 'int32Ptr'};fcnNum=fcnNum+1;
% int exchangewithsocket ( const int * sockfd , const int * flaWri , int * flaRea , const int * nDblWri , const int * nIntWri , const int * nBooWri , int * nDblRea , int * nIntRea , int * nBooRea , double * simTimWri , double dblValWri [], int intValWri [], int booValWri [], double * simTimRea , double dblValRea [], int intValRea [], int booValRea []); 
fcns.name{fcnNum}='exchangewithsocket'; fcns.calltype{fcnNum}='cdecl'; fcns.LHS{fcnNum}='int32'; fcns.RHS{fcnNum}={'int32Ptr', 'int32Ptr', 'int32Ptr', 'int32Ptr', 'int32Ptr', 'int32Ptr', 'int32Ptr', 'int32Ptr', 'int32Ptr', 'doublePtr', 'doublePtr', 'int32Ptr', 'int32Ptr', 'doublePtr', 'doublePtr', 'int32Ptr', 'int32Ptr'};fcnNum=fcnNum+1;
% int closeipc ( int * sockfd ); 
fcns.name{fcnNum}='closeipc'; fcns.calltype{fcnNum}='cdecl'; fcns.LHS{fcnNum}='int32'; fcns.RHS{fcnNum}={'int32Ptr'};fcnNum=fcnNum+1;
methodinfo=fcns;