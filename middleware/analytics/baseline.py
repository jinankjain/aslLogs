import numpy as np
import matplotlib.pyplot as plt
import statistics

num_test = 5.0

def plot_throughput():
	tps = []
	std_err = []
	for j in range(1, 129):
		file_name = "file{}.log".format(j)
		temp = 0.0
		std_temp = []
		for i in range(1, 6):
			folder_name = "micro{}/".format(i)
			complete_path = folder_name + file_name
			f = open(complete_path,'r')
			iterf = iter(f)
			last_line = ""
			for line in iterf:
				last_line = line
			data = int(last_line.split()[6])
			std_temp.append(data)
			temp += data
		std_err.append(statistics.stdev(std_temp))
		temp = temp/num_test
		tps.append(temp)
	x = range(1, 129)
	plt.title('Baseline Experiment')
	plt.ylabel('Throughput')
	plt.xlabel('Number of Clients')
	plt.grid(True)
	plt.errorbar(x, tps, std_err, marker='^')
	plt.savefig('base_throughput.png')
	plt.close()

def plot_total_avg():
	avg = []
	std_err = []
	for j in range(1, 129):
		file_name = "file{}.log".format(j)
		temp_avg = 0.0
		temp_std = 0.0
		for i in range(1, 6):
			folder_name = "micro{}/".format(i)
			complete_path = folder_name + file_name
			f = open(complete_path,'r')
			iterf = iter(f)
			last_line = ""
			for line in iterf:
				last_line = line.split()
				if(last_line and last_line[0] == "Total" and len(last_line)>2):
					for k in range(5):
						line = next(iterf)
						line = line.split()
						if k == 2:
							temp_avg += float(line[1])
						elif k == 4:
							temp_std += float(line[1])
		temp_avg = temp_avg/num_test
		temp_std = temp_std/num_test
		avg.append(temp_avg)
		std_err.append(temp_avg)

	x = range(1, 129)
	plt.title('Baseline Experiment')
	plt.ylabel('Average Response Time in us')
	plt.xlabel('Number of Clients')
	plt.grid(True)
	plt.errorbar(x, avg, std_err, marker='^')
	plt.savefig('base_avg.png')
	plt.close()
	

plot_throughput()
plot_total_avg()

