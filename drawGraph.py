import numpy as np
import matplotlib.pyplot as plt
import statistics
import math
import sys
import os

num_test = 5.0

def get_throughput(server_number, low, high, threads, clients):
	tps = []
	file_name = "memslapthreads{}clients{}.log".format(threads, clients)
	server_name = "memslapFinal{}".format(server_number+1)
	file_path = os.path.join(server_name, file_name)
	f = open(file_path,'r')
	iterf = iter(f)
	for line in iterf:
		check_for_get = line.split()
		if len(check_for_get)>0:
			if check_for_get[0] == "Get":
				line = next(iterf)
				period_info = next(iterf)
				total_info = next(iterf)
				total_info_arr = total_info.split()
				period_info_arr = period_info.split()
				if int(total_info_arr[1]) > low and int(total_info_arr[1]) < high:
					tps.append(int(period_info_arr[3]))

	return tps

def get_response_time(server_number, low, high, threads, clients):
	response_time_avg = []
	response_time_std = []
	file_name = "memslapthreads{}clients{}.log".format(threads, clients)
	server_name = "memslapFinal{}".format(server_number+1)
	file_path = os.path.join(server_name, file_name)
	f = open(file_path,'r')
	iterf = iter(f)
	for line in iterf:
		check_for_get = line.split()
		if len(check_for_get)>0:
			if check_for_get[0] == "Get":
				line = next(iterf)
				period_info = next(iterf)
				total_info = next(iterf)
				total_info_arr = total_info.split()
				period_info_arr = period_info.split()
				if int(total_info_arr[1]) > low and int(total_info_arr[1]) < high:
					response_time_avg.append(float(period_info_arr[8]))
					response_time_std.append(float(period_info_arr[9]))

	return response_time_avg, response_time_std

thread_set = [10, 20, 30, 40, 50]
client_sets = [40, 80, 120, 160, 200, 240, 280, 320, 360, 400]
step_size = 40
sol_throughput = []

def plot_throughput(low, high, h):
	p = 0
	print "value of h {}".format(h)
	for t in thread_set:
		client_set = [0,0]
		for j in range(0, len(client_sets)):
			tps = []
			for k in range(0, 59):
				tps.append(0)
			for i in (0, len(client_set)-1):
				client_set[i] = client_set[i] + step_size
				temp = get_throughput(i, low, high, t, client_set[i])
				for k in range(0,59):
					tps[k] = tps[k] + temp[k]
			print "Client: {} Threads: {}, tps: {}".format(client_set[i], t, statistics.mean(tps))
			if(h == 0):
				sol_throughput.append([statistics.mean(tps), statistics.stdev(tps)**2])
			else:
				sol_throughput[p][0] += statistics.mean(tps)
				sol_throughput[p][1] += float((statistics.stdev(tps)**2))
			p+=1

sol_response_time = []
def plot_response_time(low, high, h):
	p = 0
	print "value of h {}".format(h)
	for t in thread_set:
		client_set = [0,0]
		for j in range(0, len(client_sets)):
			avg_response_time = []
			std_response_time = []
			for k in range(0, 59):
				avg_response_time.append(0)
				std_response_time.append(0)
			for i in (0, len(client_set)-1):
				client_set[i] = client_set[i] + step_size
				avg, std = get_response_time(i, low, high, t, client_set[i])
				for k in range(0,59):
					avg_response_time[k] = avg_response_time[k] + avg[k]
					std_response_time[k] = std[k]**2 + std_response_time[k]
			avg_response_time[:] = [x / len(client_set) for x in avg_response_time]
			std_response_time[:] = [math.sqrt(x) / len(client_set) for x in std_response_time]
			print "Client: {} Threads: {}, avg_response_time: {}".format(client_set[i], t, statistics.mean(avg_response_time))
			stdev = 0
			for std in std_response_time:
				stdev = std**2 + stdev
			stdev = stdev/len(std_response_time)
			if(h == 0):
				sol_response_time.append([statistics.mean(avg_response_time), stdev])
			else:
				sol_response_time[p][0] += statistics.mean(avg_response_time)
				sol_response_time[p][1] += stdev
			p+=1

			# for j in range(0, 2):
			# 	client_set[j] = client_set[j] + step_size
			# 	clients = 0
			# 	tps = []
			# 	for k in range(0,60):
			# 		tps.append(0)
			# 	for k in range(0, 2):
			# 		clients = clients + client_set[k]
			# 	for k in range(0, 2):
			# 		if client_set[k] > 0:
			# 			server_name = "memslapFinal{}".format(k+1)
			# 			for i in range(0, 60):
			# 				temp = get_throughput(server_name, low, high, t, clients)
			# 				tps[k]+=temp[k]
			# 	#print "Clients: {} Threads: {} Average: {}".format(clients, t, statistics.mean(tps))
			# 	if h == 0:
			# 		sol_throughput.append([statistics.mean(tps), statistics.stdev(tps)**2])
			# 	else:
			# 		sol_throughput[p][0] += statistics.mean(tps)
			# 		sol_throughput[p][1] += float((statistics.stdev(tps)**2))
			# 	p+=1


low = 0
high = 60
h = 0
while high<=240:
	# print ""
	#print "Low: {} High: {}".format(low, high)
	plot_throughput(low, high, h)
	low+=60
	high+=60
	h+=1

for i in range(0, 50):
	sol_throughput[i][0] = sol_throughput[i][0]/num_test
	sol_throughput[i][1] = math.sqrt(sol_throughput[i][0])/num_test


for i in range (0,5):
	x = []
	error = []
	for j in range(0, 10):
		x.append(sol_throughput[i*10+j][0])
		error.append(sol_throughput[i*10+j][1])
	plt.errorbar(client_sets, x, error, marker='^', label="Thread-{}".format(thread_set[i]))


# low = 0
# high = 60
# h = 0
# while high<=240:
# 	# print ""
# 	#print "Low: {} High: {}".format(low, high)
# 	plot_response_time(low, high, h)
# 	low+=60
# 	high+=60
# 	h+=1

# for i in range(0, 50):
# 	sol_response_time[i][0] = sol_response_time[i][0]/num_test
# 	sol_response_time[i][1] = math.sqrt(sol_response_time[i][1])/num_test


# for i in range (0,5):
# 	x = []
# 	error = []
# 	for j in range(0, 10):
# 		x.append(sol_response_time[i*10+j][0])
# 		error.append(sol_response_time[i*10+j][1])
# 	plt.errorbar(client_sets, x, error, marker='^', label="Thread-{}".format(thread_set[i]))

# x_values = []
# for i in range (0, len(client_sets)+3):
# 	x_values.append(i*40)

# plt.xticks(x_values)
# plt.title('Response Time')
# plt.xlabel('Number of Clients')
# plt.ylabel('Response Time (us)')
# plt.legend(loc=4)
# plt.show()

x_values = []
y_values = []
for i in range (0, len(client_sets)+2):
	x_values.append(i*40)

y_values = []
for i in range (0, 11):
	y_values.append(i*2000)

plt.xticks(x_values)
plt.yticks(y_values)
plt.title('Throughput')
plt.xlabel('Number of Clients')
plt.ylabel('Throughput in (ops/s)')
plt.legend(loc=4)
plt.show()
