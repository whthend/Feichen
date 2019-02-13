This program is used to evaluate the performance of the scheme for secure linear equation (LE) solving outsourcing.

To run this program in Matlab, do the followings:
1. copy all '.m' files to the working directory of Matlab.
2. type 'testLE' in the command window.

To change the dimension of the linear equation, just modify the array in the 'testLE.m' file and run again.

The design of the program is as follows:
1. 'generateOriginalProblemLE.m' is a function to generate an LE instance randomly.
2. 'generateNewProblemLE.m' is a function to encrypt an LE problem.
3. 'evaluateLE.m' is a function to evaluate the performance of the proposed outsourcing scheme on a detailed dimension by average the running time of a few problem instances. This function also calls 'generateOriginalProblemLE.m' and 'generateNewProblemLE.m' to generate problem instances.
4. 'testLE.m' is the main program. It calls 'evaluateLE.m' on different dimensions to get the final performance result.
5. 'testRealWorld' is a program testing the performance of the protocol using a real world data set, i.e. 'bp_400'. More information about the data set can be found in our paper or on our website.
