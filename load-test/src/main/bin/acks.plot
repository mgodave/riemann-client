#set title
#set xlabel
#set ylabel
#set xrange[:]
#set yrange[:]
set datafile separator ','

set term x11 0
plot "acks.csv" using 1:4 title "Acks", \
     "sends.csv" using 1:4 title "Sends"

set term x11 1
plot "rtt.csv" using 1:4 title "rtt", \
     "rtt.csv" using 1:9 title "rtt"

set term x11 2
plot "outstanding-sends.csv" 1:2 using title "Outstanding Sends"

set term x11 3
plot "throughput-meter.csv" using 1:4 title "Throughput"

set term x11 4
plot "outstanding-sends.csv" using 1:2 title "Outstanding Sends" axes x1y1, \
     "throughput-meter.csv" using 1:4 title "Throughput" axes x1y2