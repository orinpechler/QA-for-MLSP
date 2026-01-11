# QA-for-MLSP
Implementation of the methods from the paper "Toward Quantum Annealing for Multi-League Sports Scheduling"

### Abstract
This paper introduces the use of quantum annealing for the Multi-League Scheduling Problem, under the main assumption that all leagues contain the same even number of teams. In this problem, a schedule of matches has to be found for several leagues consisting of multiple teams and clubs, a particularly relevant issue in amateur and youth sports. For this scheduling problem, the main goal is to develop a so-called QUBO formulation, which is the main type of formulation for a quantum annealer. Four different techniques are used to develop such QUBOs. These are then solved for various instances using D-Wave’s current Advantage System. The technique called domain-wall encoding is found to outperform the other three implemented techniques in terms of solution quality, providing empirical support for this approach. However, this technique also has the highest running time, whereas the relatively new technique called unbalanced penalization achieves the lowest running time, with a solution quality that is only marginally worse than that of domain-wall encoding. Although currently quantum annealing does not perform as well as the classical approaches, it is expected that in the future quantum computers will become a superior alternative.

### Link to paper
https://link.springer.com/chapter/10.1007/978-3-031-94263-1_11
