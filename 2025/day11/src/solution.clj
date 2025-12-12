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

;; Part 1: Simple path counting
(def count-paths-memo
  (memoize
    (fn [graph node]
      (if (= node "out")
        1
        (reduce + 0 (map #(count-paths-memo graph %) (get graph node [])))))))

(defn solve-part1 [input]
  (let [graph (parse-input input)]
    (count-paths-memo graph "you")))

;; Part 2: Path counting with required nodes
(def count-paths-through-memo
  (memoize
    (fn [graph required node visited]
      (let [visited (if (required node) (conj visited node) visited)]
        (if (= node "out")
          (if (= visited required) 1 0)
          (reduce + 0 (map #(count-paths-through-memo graph required % visited)
                           (get graph node []))))))))

(defn solve-part2 [input]
  (let [graph (parse-input input)]
    (count-paths-through-memo graph #{"dac" "fft"} "svr" #{})))

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
