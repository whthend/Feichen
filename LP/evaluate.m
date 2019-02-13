%Description: This function first randomly generates a LP problem, then
%             encrypts this problem. At the same time, compute the time solving these LP
%             problems.
%Input:       problem size, i.e. if dimension = [m n], the Aeq is a m * n
%             matrix.
%Output:      the performance of the proposed method
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.08.28 at CUHK

function performance = evaluate(dimension)

loops = 20;
%performance: m, n, t_original, t_cloud, t_custermer, asymmetric
%            speedup,cloud efficiency
performance     = zeros(1, 7);
tempPerformance = zeros(loops, 3);

for i = 1 : loops
    lp = generateOriginalProblem (dimension);
    tempPerformance(i, 1) = lp.time; %t_original
    
    %key generation
    sk.Q = rand( dimension(1), dimension(1) );
%     while (det(sk.Q) ==0)
%         sk.Q = rand( dimension(1), dimension(1) );
%     end
    sk.M = diag( abs(rand(dimension(2), 1)) );
    
    %problem transformation
    tic
    newlp = generateNewProblem (lp, sk);
    tempPerformance(i, 3) = toc; %t_customer
    
    %cloud server solving the problem
    tic;
    [newlp.x,newlp.fval,newlp.exitflag]  = linprog(newlp.f, newlp.A, newlp.b, newlp.Aeq, newlp.beq, newlp.lb);
    tempPerformance(i, 2) = toc;
    
    if newlp.exitflag == -2
        disp('No feasible point was found.');
    elseif newlp.exitflag == -3
        disp('Problem is unbounded.');
    end 
end

performance(1:2) = dimension;
performance(3:5) = mean(tempPerformance);
performance(6)   = performance(3) / performance(5);
performance(7)   = performance(3) / performance(4);