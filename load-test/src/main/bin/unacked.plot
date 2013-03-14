set datafile separator ','
set title "Un-Acked Messages"
set xlabel "Time (sec)"
set ylabel "Messages"
set term png size 1600, 400 crop
set output "unacked.png"
f(x)=m*x+c
fit f(x) "unacked.csv" using 1:2 via m,c
plot "unacked.csv" using 1:2 with lines, f(x) title 'fit'
