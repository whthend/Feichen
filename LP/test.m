%Description: This program evaluates a secure LP outsourcing scheme and its improvement scheme proposed by CHEN Fei et al..
%Author:      CHEN Fei, chenfeiorange@163.com
%Date:        2012.08.28 at CUHK

dimensions = [50 100; 100 200; 200 400; 400 800;
              800 1600];
          
rand('twister',5489);
results = zeros(size(dimensions, 1) , 7);

for i = 1 : size(dimensions, 1)
    results(i, :) = evaluate( dimensions(i, :) );
end

format shortG
disp('m, n, t_original, t_cloud, t_custermer, asymmetric speedup,cloud efficiency');
results