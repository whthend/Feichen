%Description: This function counts the number of zeros in an LP solution in
%             a statistical manner.
%Input:       1 * 2 dimensional matrix denoting the LP parameter, i.e. dimension = [m n]
%Output:      a number showing average number of zeros of an LP solution
%             with parameters that is determined by the input
%Author:      Chen, Fei ( chenfeiorange@163.com )
%Date:        2013.03.05 at CUHK

function count = numberOfZeros (dimension)

flag = 1;  %to indicate whether the random LP problem is feasible
lp.dimension = dimension;
count = 0;
loop = 50;

for i = 1 : loop    
    
    while (flag == 1)
        lp.f         = rand( dimension(2), 1 );  
        %lp.f         = abs( rand( dimension(2), 1 ) );   %to make sure the the problem is feasible
        lp.A         = [];
        lp.b         = [];
        lp.Aeq       = 2 * rand(dimension) - 1;
        %if lp.Aeq = rand(dimension), it turns out that the randomly generated LP problem doesn't have
        %a solution
        lp.beq       = 2 * rand( dimension(1), 1 ) - 1;
        lp.lb        = zeros( dimension(2), 1 );
        
        [x,fval,exitflag]  = linprog(lp.f, lp.A, lp.b, lp.Aeq, lp.beq, lp.lb);
        
        if exitflag == 1
            flag = 0;
        elseif exitflag == -2
            disp('No feasible point was found.');
        elseif exitflag == -3
            disp('Problem is unbounded.');
        end
    end
    
    lp.x        = x;
    lp.fval     = fval;
    lp.exitflag = exitflag;
    
    temp = zeros(dimension(2), 1 );
    count = count + sum (temp == x);
end

count = count / loop;

