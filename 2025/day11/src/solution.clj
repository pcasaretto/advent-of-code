(ns solution
  (:require [clojure.string :as str]))

(defn parse-line [line]
  (let [[from tos] (str/split line #": ")]
    [from (str/split tos #" ")]))

(defn parse-input [input]
  (->> (str/split-lines input)
       (filter #(not (str/blank? %)))
       (map parse-line)
       (into {})))

(defn topo-sort
  "Topological sort via DFS post-order (purely functional)"
  [graph start]
  (letfn [(visit [node visited result]
            (if (visited node)
              [visited result]
              (let [[visited result]
                    (reduce (fn [[visited result] child]
                              (visit child visited result))
                            [(conj visited node) result]
                            (get graph node []))]
                [visited (conj result node)])))]
    (second (visit start #{} []))))

(defn count-paths
  "Count paths using bottom-up DP on topologically sorted nodes"
  [graph start]
  (let [order (topo-sort graph start)]
    (reduce (fn [counts node]
              (assoc counts node
                     (if (= node "out")
                       1
                       (reduce + 0 (map #(get counts % 0) (get graph node []))))))
            {}
            order)))

(defn solve-part1 [input]
  (let [graph (parse-input input)
        counts (count-paths graph "you")]
    (get counts "you" 0)))

(defn count-paths-through
  "Count paths from start to 'out' that visit all required nodes"
  [graph required start]
  (let [req-to-bit (into {} (map-indexed (fn [i n] [n (bit-shift-left 1 i)]) required))
        all-bits (dec (bit-shift-left 1 (count required)))
        n-states (bit-shift-left 1 (count required))
        order (topo-sort graph start)]
    (reduce
      (fn [counts node]
        (let [node-bit (get req-to-bit node 0)]
          (reduce
            (fn [counts in-state]
              (let [out-state (bit-or in-state node-bit)
                    k [node in-state]  ; key is (node, ENTERING state)
                    v (if (= node "out")
                        (if (= out-state all-bits) 1 0)
                        ;; children receive out-state (state after this node)
                        (reduce + 0 (map #(get counts [% out-state] 0)
                                         (get graph node []))))]
                (assoc counts k v)))
            counts
            (range n-states))))
      {}
      order)))

(defn solve-part2 [input]
  (let [graph (parse-input input)
        counts (count-paths-through graph ["dac" "fft"] "svr")]
    (get counts ["svr" 0] 0)))

(defmacro timed [expr]
  `(let [start# (System/nanoTime)
         result# ~expr
         ms# (/ (- (System/nanoTime) start#) 1e6)]
     [result# ms#]))

(defn -main []
  (let [input (slurp *in*)
        [r1 t1] (timed (solve-part1 input))
        [r2 t2] (timed (solve-part2 input))]
    (println (format "Part 1: %d (%.2fms)" r1 t1))
    (println (format "Part 2: %d (%.2fms)" r2 t2))))

(when (= *file* (System/getProperty "babashka.file"))
  (-main))
