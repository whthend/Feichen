%Description: This function generates a uniformly random LP problem.
%Input:       A 1 * 2 dimensional matrix, i.e. dimension = [m n]
%Output:      A structure 'lp' containing all information about the LP problem.
%             lp.dimension: size of Aeq
%             lp.f:         objective function
%             lp.A:         Ax <= b
%             lp.b:         Ax <= b
%             lp.Aeq:       Aeqx <= beq
%             lp.beq:       Aeqx <= beq
%             lp.lb:        x >= lb
%             lp.time:      time taken to solve this LP by Matlab
%             lp.x:         optimal solution
%             lp.fval:      optimal objecive value
%             lp.exitflag:  to indicate whether the problem is solved
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.08.28 at CUHK

function lp = generateOriginalProblem (dimension)

flag = 1;  %to indicate whether the random LP problem is feasible
time = 0;  %to record the time for solving the LP
lp.dimension = dimension;

while (flag == 1)
    lp.f         = abs( rand( dimension(2), 1 ) );   %to make sure the the problem is feasible
    lp.A         = [];
    lp.b         = [];
    lp.Aeq       = 2 * rand(dimension) - 1;
%if lp.Aeq = rand(dimension), it turns out that the randomly generated LP problem doesn't have
%a solution
    lp.beq       = 2 * rand( dimension(1), 1 ) - 1;
    lp.lb        = zeros( dimension(2), 1 );
    
    tic;
    [x,fval,exitflag]  = linprog(lp.f, lp.A, lp.b, lp.Aeq, lp.beq, lp.lb);
    time = toc;
    
    if exitflag == 1
        flag = 0;
    elseif exitflag == -2
        disp('No feasible point was found.');
    elseif exitflag == -3
        disp('Problem is unbounded.');
    end    
end

lp.time     = time;
lp.x        = x;
lp.fval     = fval;
lp.exitflag = exitflag;
