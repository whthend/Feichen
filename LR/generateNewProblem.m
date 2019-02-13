%Description: This function encrypts a LR problem to a new LR problem.
%
%Input:      'lr' - a struture containing the LR problem
%             lr.dimension: size of X
%             lr.sigma:     standard deviation of the Gaussian noise
%             lr.beta:      real regression coefficients
%             lr.betahat:   computed regression coefficients, i.e.
%             beta_hat = (X^T * X) ^ -1 * X^T * yhat
%             lr.y:         real value for y, i.e. y = X * beta
%             lr.yhat:      noised value
%             lr.time:      time taken to solve this LR by Matlab
%
%Output:      A new LR problem 'newlr'.
%
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.10.25 at CUHK

function newlr = generateNewProblem (lr, sk)

newlr       = lr;

tic

% newlr.X     = sk.A * lr.X * sk.D;
% newlr.yhat  = sk.A * lr.yhat;

for i = 1 : size(lr.X, 1)
    newlr.X(i, :) = sk.A(i, i) * lr.X(i, :);
end

for i = 1 : size(lr.X, 2)
    newlr.X(:, i) = sk.D(i, i) * newlr.X(:, i);
end

for i = 1 : size(lr.yhat, 1)
    newlr.yhat(i) = sk.A(i, i) * lr.yhat(i);
end

newlr.time  = toc;