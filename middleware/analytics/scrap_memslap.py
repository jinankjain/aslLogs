import json
import numpy as np
import matplotlib.pyplot as plt

f = open('mem.log','r')
iterf = iter(f)
i = 0;
parsed_data = {}
parsed_data['Get'] = []
parsed_data['Set'] = []
parsed_data['Total'] = []
parsed_data['get_stats'] = []
parsed_data['set_stats'] = []
parsed_data['total_stats'] = []

optype = "Get"
data_spiting_rate = 1
for line in iterf:

	if(i==0):
		servers = line.split()
		parsed_data['servers'] = servers[2].split(",")
	elif(i==1):
		threads = line.split()
		parsed_data['threads'] = threads[2]
	elif(i==2):
		concurrency = line.split()
		parsed_data['concurrency'] = concurrency[1]
	elif(i==3):
		runtime = line.split()
		parsed_data['runtime'] = runtime[2]
	elif(i==4):
		window_size = line.split()
		parsed_data['window_size'] = window_size[2]
	elif(i==5):
		set_prop = line.split()
		parsed_data['set_prop'] = set_prop[2].split("=")[1]
	elif(i==6):
		get_prop = line.split()
		parsed_data['get_prop'] = get_prop[2].split("=")[1]
	elif(i==7):
		for j in range(int(parsed_data['runtime'][:-1])/data_spiting_rate):
			for k in range(3):
				temp_array = []
				temp_data = {}
				temp = []
				for l in range(5):
					if l == 1:
						optype = line.split()[0]
					elif l == 2:
						temp = line.split()
					elif l == 3:
						data = line.split()
						for h in range(10):
							temp_data[temp[h]] = data[h]
						temp_array.append(temp_data)
					elif l == 4:
						data = line.split()
						for h in range(10):
							temp_data[temp[h]] = data[h]
						temp_array.append(temp_data)
						parsed_data[optype].append(temp_array)
					line = next(iterf) 
						# stat_type = line.split()
						# print stat_type[0]
						# parsed_data[stat_type[0]] = {}
	elif(i==8):
		for p in range(3):
			temp_data = {}
			for j in range(11):
				if(j==0):
					temp = line.split()
					if temp:
						print temp
						if(temp[0]=='Get'):
							optype = "get_stats"
						elif(temp[0]=='Set'):
							optype = "set_stats"
						elif(temp[0]=='Total'):
							optype = "total_stats"
				elif(j>=1 and j<=4):
					temp = line.split()
					temp_data[temp[0]] = temp[1]
				elif(j==5):
					parsed_data[optype].append(temp_data)
					print temp_data
				line = next(iterf)


	i = i+1
f.close()



# example data
x = np.arange(1, 128, 1)
y = np.exp(-x)
# example error bar values that vary with x-position
error = 0.1 + 0.2 * x
# error bar values w/ different -/+ errors
lower_error = 0.4 * error
upper_error = error
asymmetric_error = [lower_error, upper_error]

fig, (ax0, ax1) = plt.subplots(nrows=2, sharex=True)
ax0.errorbar(x, y, yerr=error, fmt='-o')
ax0.set_title('variable, symmetric error')

ax1.errorbar(x, y, xerr=asymmetric_error, fmt='o')
ax1.set_title('variable, asymmetric error')
ax1.set_yscale('log')
plt.show()

#print json.dumps(parsed_data)