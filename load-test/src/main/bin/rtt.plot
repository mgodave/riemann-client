set datafile separator ','
set title "RTT"
set xlabel "Time (sec)"
set ylabel "Average RTT (sec)"
set term png size 1600, 400 crop
set output "rtt.png"
plot "rtt.csv" using 1:4 with lines
