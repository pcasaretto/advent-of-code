(ns solution
  (:require [clojure.string :as str]))

(defn parse-input [input]
  (mapv vec (str/split-lines input)))

(defn find-start [grid]
  (first (for [r (range (count grid))
               c (range (count (first grid)))
               :when (= \S (get-in grid [r c]))]
           [r c])))

(defn solve-part1 [input]
  (let [grid (parse-input input)
        rows (count grid)
        cols (count (first grid))
        [start-r start-c] (find-start grid)]
    (loop [beams #{[start-r start-c]}
           splits 0]
      (if (empty? beams)
        splits
        (let [;; Move all beams down one row
              moved (map (fn [[r c]] [(inc r) c]) beams)
              ;; Partition into in-bounds and out-of-bounds
              in-bounds (filter (fn [[r c]] (< r rows)) moved)
              ;; Check each beam for splitter
              {hit true, no-hit false}
              (group-by (fn [[r c]] (= \^ (get-in grid [r c]))) in-bounds)
              ;; Count splits (dedupe first since merged beams = one split)
              hit-set (set hit)
              split-count (count hit-set)
              ;; Create new beams from splits
              new-beams (for [[r c] hit-set
                              dc [-1 1]
                              :let [nc (+ c dc)]
                              :when (and (>= nc 0) (< nc cols))]
                          [r nc])
              ;; Combine non-split beams with new split beams
              next-beams (into (set no-hit) new-beams)]
          (recur next-beams (+ splits split-count)))))))

(defn solve-part2 [input]
  (let [grid (parse-input input)
        rows (count grid)
        cols (count (first grid))
        [start-r start-c] (find-start grid)]
    (loop [beams {[start-r start-c] 1}  ; position -> timeline count
           total 0]
      (if (empty? beams)
        total
        (let [;; Move all beams down, accumulating counts
              moved (reduce-kv
                      (fn [m [r c] cnt]
                        (update m [(inc r) c] (fnil + 0) cnt))
                      {}
                      beams)
              ;; Count exited timelines
              exited-count (reduce-kv
                            (fn [sum [r _] cnt]
                              (if (>= r rows) (+ sum cnt) sum))
                            0
                            moved)
              in-bounds (into {} (filter (fn [[[r _] _]] (< r rows)) moved))
              ;; Process splitters: each timeline splits into 2
              next-beams (reduce-kv
                           (fn [m [r c] cnt]
                             (if (= \^ (get-in grid [r c]))
                               (-> m
                                   (update [r (dec c)] (fnil + 0) cnt)
                                   (update [r (inc c)] (fnil + 0) cnt))
                               (update m [r c] (fnil + 0) cnt)))
                           {}
                           in-bounds)
              ;; Filter beams in column bounds
              valid-beams (into {} (filter (fn [[[_ c] _]]
                                            (and (>= c 0) (< c cols)))
                                          next-beams))]
          (recur valid-beams (+ total exited-count)))))))

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
