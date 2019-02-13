%Description: This program evaluates the performance of our protocol using
%             a real world tese case.
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2013.11.11 at CUHK

load BNL1

flag = 1;  %to indicate whether the random LP problem is feasible
time = 0;  %to record the time for solving the LP
dimension = size(A);

lp.f         = c;
lp.A         = [];
lp.b         = [];
lp.Aeq       = A;
lp.beq       = b;
lp.lb        = lbounds;
scale = 0.25 * max(b);

tic;
[x,fval,exitflag]  = linprog(lp.f, lp.A, lp.b, lp.Aeq, lp.beq, lp.lb);
time = toc;

if exitflag == 1
    flag = 0;
elseif exitflag == -2
    disp('Origianal Problem: No feasible point was found.');
elseif exitflag == -3
    disp('Origianal Problem: Problem is unbounded.');
end

lp.time     = time;
lp.x        = x;
lp.fval     = fval;
lp.exitflag = exitflag;

%problem tranformation and performance evaluation
performance     = zeros(1, 7);
tempPerformance = zeros(1, 3);
tempPerformance(1, 1) = lp.time; %t_original
sk.Q = scale * rand( dimension(1), dimension(1) );
sk.M = scale * diag( abs(rand(dimension(2), 1)) );

newlp       = lp;
tic
newlp.f     = (sk.M)' * lp.f;
newlp.Aeq   = sk.Q * lp.Aeq * sk.M;
newlp.beq   = sk.Q * lp.beq;
tempPerformance(1, 3) = toc; %t_customer

%cloud server solving the problem
tic;
[newlp.x,newlp.fval,newlp.exitflag]  = linprog(newlp.f, newlp.A, newlp.b, newlp.Aeq, newlp.beq, newlp.lb);
tempPerformance(1, 2) = toc;

if newlp.exitflag == -2
    disp('Transformed Problem: No feasible point was found.');
elseif newlp.exitflag == -3
    disp('Transformed Problem: Problem is unbounded.');
end

performance(1:2) = dimension;
performance(3:5) = tempPerformance;
performance(6)   = performance(3) / performance(5);
performance(7)   = performance(3) / performance(4);

format shortG
disp('m, n, t_original, t_cloud, t_custermer, asymmetric speedup,cloud efficiency');
performance