import numpy as np
import matplotlib.pyplot as plt
from matplotlib import mlab
import statistics
import math
import sys
import os

#Parse Middleware Logs

def pareseMiddleware(threads, clients):
	time_middleware_array = []
	time_queue_array = []
	time_server_array = []

	file_name = "middlwarethread{}clients{}.log".format(threads, clients)
	folder_name = "middlewareFinal"
	file_path = os.path.join(folder_name, file_name)
	f = open(file_path,'r')
	iterf = iter(f)
	for line in iterf:
		check_for_get = line.split()
		if len(check_for_get)>0:
			if check_for_get[0] == "Get":
				time_middleware_info = next(iterf)
				time_queue_info = next(iterf)
				time_server_info = next(iterf)

				time_middleware = time_middleware_info.split()
				time_queue = time_queue_info.split()
				time_server = time_server_info.split()

				if(len(time_middleware)>1 and len(time_queue)>1 and len(time_server)>1):
					if(time_middleware[1] == "Middleware:" and time_server[1] == "Server:" and time_queue[1] == "Queue:"):
						time_server_array.append(float(time_server[2]))
						time_queue_array.append(float(time_queue[2]))
						time_middleware_array.append(float(time_middleware[2]))

	return time_middleware_array, time_queue_array, time_server_array

t_middleware, t_queue, t_server = pareseMiddleware(30, 400)
nano = 1000000000
t_middleware[:] = [x / nano for x in t_middleware]
t_queue[:] = [x / nano for x in t_queue]
t_server[:] = [x / nano for x in t_server]

d = np.sort(t_server)

# Percentile values
p = np.array([0.0, 25.0, 50.0, 75.0, 95.0, 100.0])

perc = mlab.prctile(d, p=p)

plt.plot(d)
# Place red dots on the percentiles
plt.plot((len(d)-1) * p/100., perc, 'ro')

# Set tick locations and labels
plt.xticks((len(d)-1) * p/100., map(str, p))

plt.title('Percentile for Server Time')
plt.xlabel('Percentile')
plt.ylabel('Time in s')

plt.show()