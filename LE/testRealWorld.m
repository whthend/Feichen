%Description: This program evaluates the performance of the protocol using
%             real world data sets.
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2013.11.11 at CUHK

load bp_400
A = Problem.A;
rand('twister',5489);
dimension = size(A);

b = rand (size(A, 2), 1);
tic
x = A \ b;
time = toc;

le.A    = A;
le.b    = b;
le.x    = x;
le.time = time;

performance     = zeros(1, 7);
tempPerformance = zeros(1, 3);

tempPerformance(1, 1) = le.time; %t_original

sk.D1 = sparse ( diag( rand(dimension(1), 1) ) );
sk.D2 = sparse ( diag( rand(dimension(1), 1) ) );

newle = le;
%problem transformation
tic
newle.A     = sk.D1 * le.A * sk.D2;
newle.b     = sk.D1 * le.b;
tempPerformance(1, 3) = toc; %t_customer

%cloud server solving the problem
tic;
newle.x  = newle.A \ newle.b;
tempPerformance(1, 2) = toc;
newle.time = tempPerformance(1, 2);

performance(1:2) = dimension;
performance(3:5) = tempPerformance;
performance(6)   = performance(3) / performance(5);
performance(7)   = performance(3) / performance(4);

format shortG
disp('m, n, t_original, t_cloud, t_custermer, asymmetric speedup,cloud efficiency');
performance