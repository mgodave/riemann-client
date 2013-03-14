set datafile separator ','
set title "Mean Ack Rate"
set xlabel "Time (sec)"
set ylabel "Average (acks/sec)"
set term png size 1600, 400 crop
set output "acks.png"
plot "acks.csv" using 1:4 with lines
