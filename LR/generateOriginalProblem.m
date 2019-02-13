%Description: This function generates a uniformly random LR problem.
%
%Input:       A 1 * 2 dimensional matrix, i.e. dimension = [m n]
%Output:      A structure 'lr' containing all information about the LR problem.
%             lr.dimension: size of Aeq
%             lr.sigma:     standard deviation of the Gaussian noise
%             lr.beta:      real regression coefficients
%             lr.betahat:   computed regression coefficients, i.e.
%             beta_hat = (X^T * X) ^ -1 * X^T * yhat
%             lr.y:         real value for y, i.e. y = X * beta
%             lr.yhat:      noised value
%             lr.time:      time taken to solve this LR by Matlab
%
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.10.25 at CUHK

function lr = generateOriginalProblem (dimension)

lr.dimension = dimension;
lr.sigma     = 0.01;

lr.beta      = 2 * rand( dimension(2), 1 ) - 1;   %[-1, 1]
lr.X         = 2 * rand( dimension ) - 1;
lr.y         = lr.X * lr.beta;
lr.yhat      = lr.y + lr.sigma * randn( dimension(1), 1 );          %dimension(1) = m

tic
lr.betahat = regress(lr.yhat, lr.X);
lr.time = toc;