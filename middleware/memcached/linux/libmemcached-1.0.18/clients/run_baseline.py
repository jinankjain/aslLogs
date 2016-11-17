import os
import sys

for i in range(1, 129):
	filename = "file{}.log".format(i)
	# command = "ls > ../../../../"+filename
	command = "./memaslap -s 10.0.0.14:9000 -T {} -c {} -o0.9 -S 1s -t 30s -F smallvalue.cfg > {}".format(i, i, filename)
	os.system(command)
