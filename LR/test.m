%Description: This program evaluates a secure LR outsourcing scheme
%
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.10.25 at CUHK

dimensions = [50 30; 100 60; 200 120; 400 240; 800 480; 1600 1000; 4000 3000];
          
rand('twister',5489);
results = zeros(size(dimensions, 1) , 7);

for i = 1 : size(dimensions, 1)
    results(i, :) = evaluate( dimensions(i, :) );
end

format shortG
disp('m, n, t_original, t_cloud, t_custermer, asymmetric speedup,cloud efficiency');
results