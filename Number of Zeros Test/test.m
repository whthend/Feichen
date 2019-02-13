%Description: This program finds out the number of zeros in an LP solution
%             in a statisfical way.
%Author:      Chen, Fei ( chenfeiorange@163.com )
%Date:        2013.03.05 at CUHK

dimensions  = [50 100; 100 200; 200 400; 400 800];             

%dimensions = [50 100; 100 200; 200 400; 400 800;
%              800 1600; 1000 2000; 3200 3840];
          
count       = zeros(size(dimensions, 1) , 1);

for i = 1 : size(dimensions, 1)
    count(i) = numberOfZeros( dimensions(i, :) );
end

disp('number of zeros for different dimensions are as follows:');
[dimensions count]