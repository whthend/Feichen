%Description: This function encrypts a LP problem to a new LP problem.
%Input:      'lp' - a struture containing the LP problem
%            'sk' - a struture containing encryption key
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
%Output:      A new LP problem 'newlp'.
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.08.28 at CUHK

function newlp = generateNewProblem (lp, sk)

newlp       = lp;

newlp.f     = (sk.M)' * lp.f;
newlp.Aeq   = sk.Q * lp.Aeq * sk.M;
newlp.beq   = sk.Q * lp.beq;