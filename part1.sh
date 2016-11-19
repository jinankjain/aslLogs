#!/bin/bash

echo -e "Running test for Part 1 of Milestone 2:"

# Number of thread varies in step size of 30 from 30 to 120
# Number of client varies in step size of 40 from 40 to 400 client
## Number of threads in memaslap is 8 because maximum cores on the machine are 8 so you cannot achieve more than that
# Right now keeping threads equal to client in middleware

#IP Address of all the servers
memcachedServers[0]=jjain@jjainjinankjain1.westeurope.cloudapp.azure.com
memcachedServers[1]=jjain@jjainjinankjain10.westeurope.cloudapp.azure.com
memcachedServers[2]=jjain@jjainjinankjain3.westeurope.cloudapp.azure.com
memcachedServers[3]=jjain@jjainjinankjain7.westeurope.cloudapp.azure.com
memcachedServers[4]=jjain@jjainjinankjain6.westeurope.cloudapp.azure.com

#Command for running memaslap
runmemaslap="./memaslap -s 127.0.0.1:9000 -T 64 -c 64 -o1 -w 1k -S 1s -t 360s -F smallvalue.cfg"
runmemcached="memcached -p 11212 -t 1"
## ssh into all the machines and run memcached server on then
for i in "${memcachedServers[@]}";
do
    ssh $i tmux send -t foo "memcached" SPACE "-p" SPACE "11212" SPACE "-t" SPACE "1" ENTER
done

memaslap[0]=jjain@jjainjinankjain4.westeurope.cloudapp.azure.com
memaslap[1]=jjain@jjainjinankjain2.westeurope.cloudapp.azure.com

client[0]=0
client[1]=0
client[2]=0
stepsize=20

middleware=jjain@jjainjinankjain11.westeurope.cloudapp.azure.com
## Run middleware
threads[0]=10
threads[1]=20
threads[2]=30
threads[3]=40
threads[4]=50

for p in "${threads[@]}";
do
    client[0]=20
    client[1]=20
    for i in `seq 0 9`;
    do
        ssh $middleware tmux send -t foo "java" SPACE "-jar" SPACE "dist/middleware-jjain.jar" SPACE "-l" SPACE "10.0.0.10" SPACE "-p" SPACE "8000" SPACE "-t" SPACE $p SPACE "-r" SPACE "1" SPACE "-m" SPACE "10.0.0.6:11212" SPACE "10.0.0.14:11212" SPACE "10.0.0.7:11212" SPACE "10.0.0.11:11212" SPACE "10.0.0.5:11212" SPACE "\>" SPACE "../../middlwarethread"$p"clients"$clientnum".log" ENTER
        pssh -h pssh-hosts -l jjain tmux send -t foo "./memaslap" SPACE "-s" SPACE "10.0.0.10:8000" SPACE "-T" SPACE ${client[0]} SPACE "-c" SPACE ${client[0]} SPACE "-o1" SPACE "-w" SPACE "1k" SPACE "-S" SPACE "1s" SPACE "-t" SPACE "360s" SPACE "-F" SPACE "new.cfg" SPACE "\>" SPACE "../../memslapthreads"$p"clients"$clientnum".log" ENTER 
        sleep 500
        ## Restart Memcached Server
        for k in "${memcachedServers[@]}";
        do
            echo -e "Flushing memcached servers"
            echo $k
            ssh $k tmux new-window -t foo
            ssh $k tmux send -t foo "killall" SPACE "-9" SPACE "memcached" ENTER
            ssh $k tmux send -t foo "memcached" SPACE "-p" SPACE "11212" SPACE "-t" SPACE "1" ENTER
        done
        echo -e "Flushing Middleware"
        ssh $middleware tmux new-window -t foo
        ssh $middleware tmux send -t foo "killall" SPACE "-9" SPACE "java" ENTER
        ssh $middleware tmux send -t foo "cd" SPACE "asl-fall16-project/middleware/" ENTER
        for j in `seq 0 1`;
        do
            client[$j]=`expr "${client[$j]}" + "$stepsize"`
        done
    done
done
