set datafile separator ','
set title "Throughput"
set xlabel "Time (sec)"
set ylabel "Average Throughput (bytes/sec)"
set term png size 1600, 400 crop
set output "throughput.png"
plot "throughput.csv" using 1:4 with lines
