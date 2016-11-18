#!/bin/bash

echo -e "Running test for Part 3 of Milestone 2:"

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
memcachedServers[5]=jjain@jjainjinankjain8.westeurope.cloudapp.azure.com
memcachedServers[6]=jjain@jjainjinankjain9.westeurope.cloudapp.azure.com


memcachedServersIP[1]="10.0.0.14:11212"
memcachedServersIP[2]="10.0.0.7:11212"
memcachedServersIP[3]="10.0.0.11:11212"
memcachedServersIP[4]="10.0.0.5:11212"
memcachedServersIP[5]="10.0.0.9:11212"
memcachedServersIP[6]="10.0.0.13:11212"
memcachedServersIP[7]="10.0.0.6:11212"

#Command for running memaslap
runmemaslap="./memaslap -s 127.0.0.1:9000 -T 64 -c 64 -o1 -w 1k -S 1s -t 360s -F smallvalue.cfg"
runmemcached="memcached -p 11212 -t 1"
## ssh into all the machines and run memcached server on then
# for i in "${memcachedServers[@]}";
# do
#     ssh $i tmux send -t foo "memcached" SPACE "-p" SPACE "11212" SPACE "-t" SPACE "1" ENTER
# done

memaslap[0]=jjain@jjainjinankjain4.westeurope.cloudapp.azure.com
memaslap[1]=jjain@jjainjinankjain2.westeurope.cloudapp.azure.com

helper=" SPACE " 

middleware=jjain@jjainjinankjain11.westeurope.cloudapp.azure.com
server_number[0]=3
server_number[1]=5
server_number[2]=7

workload[0]=1
workload[1]=4
workload[2]=7
workload[3]=10

for g in `seq 1 5`;
do
    for q in "${workload[@]}";
    do
        for i in "${server_number[@]}";
        do
            server_list=""
            for j in `seq 1 ${i}`;
            do 
                server_list+=${memcachedServersIP[$j]}$helper
            done
            for j in `seq 0 1`;
            do
                if [ $j -eq 0 ]; then
                    ssh $middleware tmux send -t foo "java" SPACE "-jar" SPACE "dist/middleware-jjain.jar" SPACE "-l" SPACE "10.0.0.10" SPACE "-p" SPACE "8000" SPACE "-t" SPACE 30 SPACE "-r" SPACE "1" SPACE "-m" SPACE ${server_list} "\>" SPACE "../../part3/repition"$g"/middlwareReplication1Server"$i".log" ENTER;
                    pssh -h pssh-hosts -l jjain tmux send -t foo "./memaslap" SPACE "-s" SPACE "10.0.0.10:8000" SPACE "-T" SPACE "200" SPACE "-c" SPACE "200" SPACE "-o1" SPACE "-S" SPACE "1s" SPACE "-t" SPACE "60s" SPACE "-F" SPACE "new"$q".cfg" SPACE "\>" SPACE "../../part3/repition"$g"/memslapReplication1Server"$i".log" ENTER ;
                    sleep 200s;
                    ## Restart Memcached Server
                    for k in "${memcachedServers[@]}";
                    do
                        echo -e "Flushing memcached servers";
                        echo $k;
                        ssh $k tmux new-window -t foo;
                        ssh $k tmux send -t foo "killall" SPACE "-9" SPACE "memcached" ENTER;
                        ssh $k tmux send -t foo "memcached" SPACE "-p" SPACE "11212" SPACE "-t" SPACE "1" ENTER;
                    done
                    echo -e "Flushing Middleware";
                    ssh $middleware tmux new-window -t foo;
                    ssh $middleware tmux send -t foo "killall" SPACE "-9" SPACE "java" ENTER;
                    ssh $middleware tmux send -t foo "cd" SPACE "asl-fall16-project/middleware/" ENTER;
                fi
                if [ $j -eq 1 ]; then
                    ssh $middleware tmux send -t foo "java" SPACE "-jar" SPACE "dist/middleware-jjain.jar" SPACE "-l" SPACE "10.0.0.10" SPACE "-p" SPACE "8000" SPACE "-t" SPACE 30 SPACE "-r" SPACE $i SPACE "-m" SPACE ${server_list} "\>" SPACE "../../part3/repition"$g"/middlwareReplication"$i"Server"$i".log" ENTER;
                    pssh -h pssh-hosts -l jjain -A tmux send -t foo "./memaslap" SPACE "-s" SPACE "10.0.0.10:8000" SPACE "-T" SPACE "200" SPACE "-c" SPACE "200" SPACE "-o1" SPACE "-S" SPACE "1s" SPACE "-t" SPACE "60s" SPACE "-F" SPACE "new"$q".cfg" SPACE "\>" SPACE "../../part3/repition"$g"/memslapReplication"$i"Server"$i".log" ENTER ;
                    sleep 200s;
                    ## Restart Memcached Server
                    for k in "${memcachedServers[@]}";
                    do
                        echo -e "Flushing memcached servers";
                        echo $k;
                        ssh $k tmux new-window -t foo;
                        ssh $k tmux send -t foo "killall" SPACE "-9" SPACE "memcached" ENTER;
                        ssh $k tmux send -t foo "memcached" SPACE "-p" SPACE "11212" SPACE "-t" SPACE "1" ENTER;
                    done
                    echo -e "Flushing Middleware";
                    ssh $middleware tmux new-window -t foo;
                    ssh $middleware tmux send -t foo "killall" SPACE "-9" SPACE "java" ENTER;
                    ssh $middleware tmux send -t foo "cd" SPACE "aslLogs/middleware/" ENTER;
                fi
            done
        done
    done
done