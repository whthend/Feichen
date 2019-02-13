%Description: This function generates a uniformly random linear equation (LE) solving problem.
%Input:       A 1 * 2 dimensional matrix, i.e. dimension = [n n]
%Output:      A structure 'le' containing all information about the le problem.
%             le.dimension: size of A
%             le.A:         Ax = b
%             le.b:         Ax = b
%             le.x:         Ax = b
%             le.time:      time taken to solve this LE by Matlab
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.08.30 at CUHK

function le = generateOriginalProblemLE (dimension)


le.dimension = dimension;

A = 2 * rand(dimension) - 1;
b = 2 * rand(dimension) - 1;
tic
x = A \ b;
time = toc;

le.A    = A;
le.b    = b;
le.x    = x;
le.time = time;
