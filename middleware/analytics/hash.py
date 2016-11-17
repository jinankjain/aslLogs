# import matplotlib.pyplot as plt
# from numpy.random import normal
# gaussian_numbers = [143458, 142563, 142913, 142188, 142589, 142818, 143471]
# plt.hist(gaussian_numbers, bins=(1,2,3,4,5,6,7))
# plt.title("Gaussian Histogram")
# plt.xlabel("Value")
# plt.ylabel("Frequency")
# plt.show()

import random
import matplotlib.pyplot as plt
x = [143458, 142563, 142913, 142188, 142589, 142818, 143471]
xbins = range(1, len(x)+1)
#plt.hist(x, bins=xbins, color = 'blue') 
#Does not make the histogram correct. It counts the occurances of the individual counts. 

plt.hist(xbins,weights=x)#plot works but I need this in histogram format
plt.show()