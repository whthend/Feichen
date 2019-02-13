This program is used to evaluate the performance of the scheme for secure linear programming (LP)  outsourcing.

To run this program in Matlab, do the followings:
1. copy all '.m' files to the working directory of Matlab.
2. type 'test' in the command window.

To change the dimension of the linear programming, just modify the array in the 'test.m' file and run again.

The design of the program is as follows:
1. 'generateOriginalProblem.m' is a function to generate an LP instance randomly.
2. 'generateNewProblem.m' is a function to encrypt an LP problem.
3. 'evaluate.m' is a function to evaluate the performance of the proposed outsourcing scheme on a detailed dimension by average the running time of a few problem instances. This function also calls 'generateOriginalProblem.m' and 'generateNewProblem.m' to generate problem instances.
4. 'test.m' is the main program. It calls 'evaluate.m' on different dimensions to get the final performance result.
5. 'testRealWorld' is a program testing the performance of the protocol using a real world data set, i.e. 'AGG_3'. More information about the data set can be found in our paper or on our website.