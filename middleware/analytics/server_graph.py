import numpy as np
import matplotlib.pyplot as plt
import statistics
import math

num_test = 5.0

def plot_throughput():
	tps = []
	for jj in range(1, 4):
		file_name = "server{}.log".format(jj)
		f = open(file_name,'r')
		iterf = iter(f)
		i = 0
		if jj == 1:
			for line in iterf:
				if(i==8):
					for k in range(360):
						for p in range(3):
							for j in range(5):
								if(j==2 and p==2):
									temp = line.split()
									tps.append(int(temp[3]))
								line = next(iterf)
						
				i = i+1
		else:
			for line in iterf:
				if(i==8):
					for k in range(360):
						for p in range(3):
							for j in range(5):
								if(j==2 and p==2):
									temp = line.split()
									tps[k] = tps[k] + int(temp[3])
								line = next(iterf)
						
				i = i+1
	x = range(1, 361)
	plt.title('Stability Trace')
	plt.ylabel('Throughput')
	plt.xlabel('Time')
	plt.grid(True)
	x = np.asarray(x)
	plt.plot(10*x, tps)
	print statistics.mean(tps)
	plt.savefig('stab_throughput.png')
	plt.close()

def plot_total_avg():
	tps = []
	avg = []
	for jj in range(1, 4):
		file_name = "server{}.log".format(jj)
		f = open(file_name,'r')
		iterf = iter(f)
		i = 0
		if jj == 1:
			for line in iterf:
				if(i==8):
					for k in range(360):
						for p in range(3):
							for j in range(5):
								if(j==2 and p==2):
									temp = line.split()
									avg.append(float(temp[8]))
									tps.append(float(temp[9])*float(temp[9]))
								line = next(iterf)
						
				i = i+1
		elif jj == 2:
			for line in iterf:
				if(i==8):
					for k in range(360):
						for p in range(3):
							for j in range(5):
								if(j==2 and p==2):
									temp = line.split()
									avg[k] = avg[k] + float(temp[8])
									tps[k] = tps[k] + float(temp[9])*float(temp[9])
								line = next(iterf)
		elif jj == 3:
			for line in iterf:
				if(i==8):
					for k in range(360):
						for p in range(3):
							for j in range(5):
								if(j==2 and p==2):
									temp = line.split()
									avg[k] = avg[k] + float(temp[8])
									tps[k] = tps[k] + float(temp[9])*float(temp[9])
									avg[k] = avg[k]/3.0
									tps[k] = tps[k]/3.0
									math.sqrt(tps[k])
								line = next(iterf)
						
				i = i+1
	
	x = range(1, 361)
	plt.title('Stability Trace')
	plt.ylabel('Average Response Time in us')
	plt.xlabel('Time')
	print statistics.mean(avg)
	plt.grid(True)
	x = np.asarray(x)
	plt.errorbar(10*x, avg, tps, marker='^')
	plt.savefig('stab_avg.png')
	plt.close()


plot_throughput()
plot_total_avg()
plt.show()
