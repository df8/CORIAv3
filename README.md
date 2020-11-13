# CORIA v3.1

CORIA (Connectivity Risk Analyzer) is a framework for analyzing network connectivity weaknesses on graphs with millions of vertices and edges.

This repository reflects the continued development of CORIA v3.0, which was initially implemented from scratch by Sebastian Gross in 2017. [See his repository.](https://github.com/bigbasti/CORIAv3)

## Supported Algorithms
1. All-Pairs Shortest Paths (APSP) based on Breadth-First Search (BFS)
2. Node Degree (NDEG)
3. Average Neighbour Degree (AND)
4. Iterated Average Neighbour Degree (IAND)
5. Local Clustering Coefficient (CLCO)
6. Betweenness Centrality (BC)
7. Average Shortest Path Length (ASPL)
8. Eccentricity (ECC)
9. Unified Risk Score (URS)
10. Connectivity Risk Classification (CRC)
11. Graph Diameter
12. Graph Density
13. Min-Max Normalisation

## Implemented Calculation Environments
This list is sorted in ascending order by the observed performance with the first library being the slowest.
1. Java-based `GraphStream` (CORIA API by Sebastian Gross)
2. SQL-Query-based `MariaDB v10.5+` implementation (CORIA API by David Fradin) 
3. Python-based `NetworkX` (CORIA API by Sebastian Gross, updated by David Fradin)
4. CUDA C++ with Cython/Python wrappers using `RAPIDS cuGraph` and `RAPIDS cuDF` (CORIA API by David Fradin)
 

## Changelog from v3.0 to v3.1
- Added CUDA-based implementations of metrics running computationally intensive calculations on a GPU.
    - [NEW] Added an approximated implementation of Betweenness Centrality with parameter `k`
- Improved performance of metrics execution:
    - We reduced memory footprint during metric calculations by eliminating the handling of entire datasets in memory. After calculating a metric, we read and process the results file only one line at a time and immediately write that data chunk into the database.
    - We also reduced the memory footprint by keeping node and edge objects in memory only for the duration of a (transactional) operation.
    - We reduced usage of loop operators such as `List.filter()`. We use database lookups on indexed columns instead.
- Migrated from a REST API to a GraphQL API.
- Migrated from manually written/hard-coded SQL statements to the object-relational mapping solution _Hibernate_ and Spring Data JPA (Java Persistence API). 
Metric implementations in SQL are however implemented using native SQL queries (see `DatasetRepository.java`). 
- Migrated from MySQL v5 to MariaDB v10.5.x to use newer window functions such as [`MEDIAN()`](https://mariadb.com/kb/en/median/).
- Remodelled the relational database layout to comply with the third normal form (3NF) 
- Replaced the AngularJS 1.x frontend with a more up-to-date and data-centric ReactJS frontend based on [React-Admin](https://github.com/marmelab/react-admin)
- Removed support for Redis database due to the lack of support by _Hibernate_ to run MariaDB and Redis together in a single Java Spring project. 
- Fixed warnings appearing from Maven package upgrades and the upgrade to JDK version 11
- Reduced repetitive code (DRY principle)
- Shifted Java package name from `com.bigbasti.coria` to `com.coria.v3` to reflect that the project is now the result of work of more than one developer.
- Corrected typos in comments

## Build from Source
To build CORIA from sources please follow these steps. 

Note: We tested this instruction on Ubuntu 18.04 LTS, Windows 10 Build 2004 and Mac OS X 10.15 Catalina. `RAPIDS` currently supports only Linux, hence for the full set of features we recommend Ubuntu 18.04.

1. Download and install the Java JDK 11 or newer
2. Download and install Maven
3. Install the proprietary NVIDIA display drivers and the newest NVIDIA CUDA Toolkit [We tested on NVIDIA display driver v450.51 and cuda-toolkit 11.0.3]
3. Download and unpack the sources
4. Open terminal in the root directory of the sources
5. Run `mvn clean install`
6. Now open the directory `<unzipped directory>/coria-api/target/` and find the archive `coria.war`
6. Deploy this file to the server of your choice (see below) [We tested this project on Apache Tomcat v9.0.37]

## Deploy the Application

Note: We tested this instruction on Ubuntu 18.04 LTS, Windows 10 Build 2004 and Mac OS X 10.15 Catalina. `RAPIDS` currently supports only Linux, hence for the full set of features we recommend Ubuntu 18.04.

1. Install Ubuntu OS. [We tested on Ubuntu 18.04 (Bionic Beaver)].
2. Install Anaconda or Miniconda [We tested on Conda v4.8.2 and Python 3.7.6]
2. Install MariaDB database server v10.5 or higher and create a DB e.g. named 'coria'. [We tested on mariadb-server-10.5]
3. Install a Java `*.war` application server of your choice [We tested this project on Apache Tomcat v9.0.37]
4. Install the proprietary NVIDIA display drivers for Ubuntu and the NVIDIA CUDA Toolkit [We tested on NVIDIA display driver v450.51 and cuda-toolkit 11.0.3]
5. Build the `coria.war` file according to instructions in the section _Build from Source_.
6. Deploy the `coria.war` file into the server. In Tomcat 9, you can use the Tomcat admin page to upload the `coria.war` file. 
We recommend to install CORIA as the root level application such that you can use it at `http://localhost:8080` instead of `http://localhost:8080/coria`.
6. Your webserver will extract `coria.war` into the application directory. Find the `application.properties` file in the unpacked application directory i.e. `WEB-INF\classes\`
7. Configure the `application.properties` lines according to your setup, especially credentials to DB and the path to a local writable directory
8. Run server and navigate to `http://localhost:8080`

## Suggestions to improve for future developers
- The data exchange between CORIA backend and the range of external libraries currently poses a bottleneck due to lots of I/O operation.
The exchange in v3.1 works this way:
    1. User requests a metric calculation.
    2. We **read** the entire graph dataset from the database (which is stored on a drive) into RAM.
    3. We **write** the entire dataset from RAM into a text file stored on a drive
    4. The metric library now launches in an external process and **reads** the whole text file into RAM.
    5. The metric runs its calculations and finishes by **writing** the results from RAM into a text file stored on a drive.    
    6. The backend now **reads** the results from drive back into RAM and validates it.
    7. Finally, the backend **writes** the results to the database (drive).

    Depending on the I/O bandwidth of your drive (HDD, SSD, eMMC, ...) reading and writing a larger dataset (e.g. 149+ MB) multiple times may cause a bottleneck. 
    One option to tackle this is to use the `ramfs` and `tmpfs` in-memory filesystems available in modern Linux distributions. 
    Memory based file systems tend to be orders of magnitude faster than persistent drives.
    Additionally, we suggest improving the performance of the used MariaDB database engine through configuration. 
    - Redis is an in-memory database engine by design, hence I recommend preferring Redis in cases where overall calculation time is crucial. 
    - In MariaDB, it is possible to increase the `query_cache` size parameter, thus creating a sort of hot and cold hybrid storage which should improve I/O speeds.
- CUDA allows running kernels (programs) on multiple graphic cards in parallel, enabling even higher calculation speeds. 
This requires changes in algorithm design and further benchmarks to optimally utilize the multi-level parallel architecture. As of the time of writing this (Sept 2020), cuGraph developers were already working on multi-GPU enabled algorithms.
- The current design runs CORIA metrics one at a time. It would be worth testing how a system behaves with two different GPU metrics being computed in parallel.  

## Issues, Questions
If you have trouble building or installing the application or find any bugs, please feel free to open an issue on Github.

## References
1. Gross, Sebastian (2017). Development of a modular software framework for the analysis of network connectivity risks based on network graphs [Bachelor’s thesis]. Hochschule für Telekommunikation Leipzig (FH).

## My supplementary publication
- Fradin, David Alexander (2020). Accelerating the Analysis of Network Connectivity Risks: Development of High-Performance Software Modules on the GPU [Master’s thesis]. Humboldt-Universität zu Berlin.