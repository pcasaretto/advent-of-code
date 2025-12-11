# Day 11: Reactor

## Problem Summary

We have a directed acyclic graph of devices. Data flows from device to device through connections.

**Part 1**: Count all paths from `you` to `out`

**Part 2**: Count paths from `svr` to `out` that pass through both `dac` AND `fft`

## Solution Approach

### Data Structure

```clojure
{"you" ["bbb" "ccc"],
 "bbb" ["ddd" "eee"],
 ...}
```

A map from node to list of children (outgoing edges).

### Step 1: Topological Sort

We need to process nodes in an order where children come before parents (so when we compute a parent's count, children's counts are ready).

```clojure
(defn topo-sort [graph start]
  (letfn [(visit [node visited result]
            (if (visited node)
              [visited result]              ; already seen, return unchanged
              (let [[visited result]
                    (reduce (fn [[visited result] child]
                              (visit child visited result))
                            [(conj visited node) result]
                            (get graph node []))]
                [visited (conj result node)])))]  ; add self AFTER children
    (second (visit start #{} []))))
```

**Key pattern**: Thread `[visited result]` through recursion as accumulators. Each call returns updated accumulators, no mutation needed.

**Post-order**: We add a node to result AFTER visiting all its children, so children appear before parents.

### Step 2: Part 1 - Simple Path Counting

```clojure
(defn count-paths [graph start]
  (let [order (topo-sort graph start)]
    (reduce (fn [counts node]
              (assoc counts node
                     (if (= node "out")
                       1
                       (reduce + 0 (map #(get counts % 0)
                                        (get graph node []))))))
            {}
            order)))
```

Process nodes in topo order. For each node:
- `"out"` -> 1 path (base case)
- Otherwise -> sum of children's path counts

Since children come first in topo order, their counts are already in `counts` when we need them.

### Step 3: Part 2 - Constrained Path Counting

Now we track state: which required nodes (`dac`, `fft`) have been visited.

```
State bits:
  0 = seen neither
  1 = seen dac
  2 = seen fft
  3 = seen both
```

**Key insight**: `counts[(node, s)]` = paths from `node` to `out`, entering with state `s`.

```clojure
(reduce
  (fn [counts node]
    (let [node-bit (get req-to-bit node 0)]  ; 0 if not required
      (reduce
        (fn [counts in-state]
          (let [out-state (bit-or in-state node-bit)  ; update state
                k [node in-state]                      ; key = entering state
                v (if (= node "out")
                    (if (= out-state all-bits) 1 0)   ; count if all visited
                    (reduce + 0 (map #(get counts [% out-state] 0)
                                     (get graph node []))))]
            (assoc counts k v)))
        counts
        (range n-states))))  ; try all 4 possible entering states
  {}
  order)
```

For each node, for each possible entering state:
1. Compute exiting state: `out-state = in-state | node-bit`
2. At `"out"`: return 1 if we've seen all required nodes
3. Otherwise: sum children's counts, passing them `out-state`

**Why `[node in-state]` as key?**
- We enter node with `in-state`
- We pass `out-state` to children
- Children's keys are `[child out-state]`
- This matches the flow of state through the graph

### Final Query

```clojure
(get counts ["svr" 0] 0)  ; start at svr with no bits set
```

## Complexity

- **Part 1**: O(V + E) - each node visited once
- **Part 2**: O(4 * (V + E)) - each (node, state) pair visited once, 4 possible states

## Performance

| Part | Answer | Time |
|------|--------|------|
| 1 | 603 | 0.7ms |
| 2 | 380,961,604,031,372 | 3.4ms |

Note: There are 155 quadrillion total paths from svr to out, but memoization/DP reduces computation to O(V * states).

## Key Learnings

1. **Purely functional recursion**: Thread accumulators through recursive calls instead of using mutable state
2. **Bottom-up DP**: Process in topological order so dependencies are computed first
3. **State tracking with bitmasks**: Efficient way to track which subset of nodes have been visited
4. **Key semantics matter**: In bottom-up DP, carefully consider whether keys represent entering or exiting state
