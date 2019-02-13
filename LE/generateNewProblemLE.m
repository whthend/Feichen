%Description: This function encrypts a LE problem to a new LE problem.
%Input:      'le' - a struture containing the LE problem
%            'sk' - a struture containing encryption key
%             le.exitflag:  to indicate whether the problem is solved
%Output:      A new le problem 'newle'.
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.08.30 at CUHK

function newle = generateNewProblemLE (le, sk)

newle       = le;

newle.A     = sk.D1 * le.A * sk.D2;
newle.b     = sk.D1 * le.b;