from fabric.api import *

run("memcached -p 11212 -t 1")
run("memcached -p 11214 -t 1")
run("memcached -p 11213 -t 1")