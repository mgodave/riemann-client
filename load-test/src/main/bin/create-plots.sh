#!/bin/bash

PREFIX="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

(

	cd $*
	gnuplot < $PREFIX/acks.plot
	gnuplot < $PREFIX/rtt.plot
	gnuplot < $PREFIX/throughput.plot
	gnuplot < $PREFIX/unacked.plot

)