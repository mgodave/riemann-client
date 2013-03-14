#!/bin/bash

PREFIX="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

(

	cd $*
	gnuplot < $PREFIX/acks.plot
	gnuplot < $PREFIX/rtt.plot
	gnuplot < $PREFIX/throughput.plot
	gnuplot < $PREFIX/unacked.plot

	cat <<EOF > plots.html

<html>
	<body>
		<img src="acks.png"/><p/>
		<img src="rtt.png"/><p/>
		<img src="throughput.png"/><p/>
		<img src="unacked.png"/><p/>
	</body>
</html>

EOF

)