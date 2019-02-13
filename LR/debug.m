A = [ 3 0; 0 4];
X = [1 2; 3 4];
D = [2 0; 0 3];
y = [5; 6];

r1 = A * X * D;
y1 = A * y;

r2 = r1;
y2 = y1;

for i = 1 : size(X, 1)
    r2(i, :) = A(i, i) .* X(i, :);
end

for i = 1 : size(X, 2)
    r2(:, i) = D(i, i) .* r2(:, i);
end

for i = 1 : size(y, 1)
    y2(i) = A(i, i) * y(i);
end

disp('r1 vs. r2');
r1, r2

disp('y1 vs. y2');
y1, y2