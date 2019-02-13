%Description: This function first randomly generates a LE problem, then
%             encrypts this problem. At the same time, compute the time solving these LE
%             problems.
%Input:       problem size, i.e. if dimension = [n n], the 'A' is a n * n matrix.
%Output:      the performance of the proposed method
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.08.30 at CUHK

function performance = evaluateLE(dimension)

loops = 20;
%performance: n, n, t_original, t_cloud, t_custermer, asymmetric
%            speedup,cloud efficiency
performance     = zeros(1, 7);
tempPerformance = zeros(loops, 3);

for i = 1 : loops
    le = generateOriginalProblemLE (dimension);
    tempPerformance(i, 1) = le.time; %t_original
    
    sk.D1 = sparse ( diag( rand(dimension(1), 1) ) );
    sk.D2 = sparse ( diag( rand(dimension(1), 1) ) );
    
    %problem transformation
    tic
    newle = generateNewProblemLE (le, sk);
    tempPerformance(i, 3) = toc; %t_customer
    
    %cloud server solving the problem
    tic;
    newle.x  = newle.A \ newle.b;
    tempPerformance(i, 2) = toc;
    newle.time = tempPerformance(i, 2);
end

performance(1:2) = dimension;
performance(3:5) = mean(tempPerformance);
performance(6)   = performance(3) / performance(5);
performance(7)   = performance(3) / performance(4);