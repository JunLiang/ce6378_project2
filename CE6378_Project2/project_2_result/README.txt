This folder contains the server and client trace files collected during our experiments. 

Under folder concurrent_write, concurrent_write_001, concurrent_write_002, concurrent_write_003, concurrent_write_004, we stored all the trace files for the concurrent write test cases (as described in our project report).

Under folder random_read_write, there are trace files for the random read/write test case.

Under folder write_quorum, there are trace files for the test case of writing quorum validation.

Under folder partition, the trace files for partition based test case are stored here.

Under folder simulate_failure, we store the trace files for simulation of crash failure of 2 servers.

file: all_nodes.cfg  -- This is the configuration file of our server nodes and client nodes.

file: simulation_server.sh -- This is the shell script that starts all the configured servers on different netxx servers

file:kill_net.sh -- This is the shell script that logs onto the netxx servers to clean up the server process. 

file:typescript -- This is the script capture of the client session.
