This program is used to find out the number of zeros of an LP solution in a statistical way. The LP is randomly generated  and the number of zeros is caculated by averaging the outcome of many problem instances. 

To run this program in Matlab, do the followings:
1. copy all '.m' files to the working directory of Matlab.
2. type 'test' in the command window.

To change the dimension of the test, just modify the array in the 'test.m' file and run again.

The design of the program is as follows:
1. 'numberOfZeros.m' is a function to evaluate the number of zeros in an LP solution with a specified dimension as input. The output is an averag of the individual solutions of many problem instances. 
2. 'test.m' is the main program. It calls 'numberOfZeros.m' on different dimensions to get the final result.