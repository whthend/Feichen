%Description: This function first randomly generates an LR problem, then
%             encrypts this problem. At the same time, compute the time
%             solving the LR problem.
%
%Input:       problem size, i.e. if dimension = [m n]
%Output:      the performance of the proposed method
%
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.10.25 at CUHK

function performance = evaluate(dimension)

loops = 20;
%performance: m, n, t_original, t_cloud, t_custermer, asymmetric
%            speedup,cloud efficiency
performance     = zeros(1, 7);
tempPerformance = zeros(loops, 3);

for i = 1 : loops
    lr = generateOriginalProblem (dimension);
    tempPerformance(i, 1) = lr.time;        %t_original
    
    %key generation
    sk.key = 2 * rand() - 1;
    sk.A = diag ( ones(dimension(1), 1) * sk.key );   %A: m by m orthognal matrix; A^T * A = sk.key^2 * I
    sk.D = diag ( 2 * rand( dimension(2), 1 ) - 1 );            %D: n by n diagnal matrix
    
    %problem transformation
    newlr = generateNewProblem (lr, sk);
    tempPerformance(i, 3) = newlr.time;            %t_customer
    
    %cloud server solving the problem
    tic;
    newlr.betahat = regress(newlr.yhat, newlr.X);
    tempPerformance(i, 2) = toc;            %t_cloud

%correctness check, i.e. comparing the 'betahat' value
%     tempBeta = sk.D * newlr.betahat;
%     tempy = lr.X * tempBeta;
%     lr.yhat - tempy

%     tempBeta = sk.D * newlr.betahat
%     lr.beta
    
end

performance(1:2) = dimension;
performance(3:5) = mean(tempPerformance);
performance(6)   = performance(3) / performance(5);
performance(7)   = performance(3) / performance(4);