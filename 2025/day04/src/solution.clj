(ns solution
  (:require [clojure.string :as str]))

(defn parse-input [input]
  (->> (str/split-lines input)
       (mapv vec)))

(defn neighbors [[r c]]
  (for [dr [-1 0 1]
        dc [-1 0 1]
        :when (not (and (zero? dr) (zero? dc)))]
    [(+ r dr) (+ c dc)]))

(defn roll? [grid [r c]]
  (= \@ (get-in grid [r c])))

(defn accessible? [grid pos]
  (and (roll? grid pos)
       (< (count (filter #(roll? grid %) (neighbors pos))) 4)))

(defn all-positions [grid]
  (let [rows (count grid)
        cols (count (first grid))]
    (for [r (range rows)
          c (range cols)]
      [r c])))

(defn solve-part1 [input]
  (let [grid (parse-input input)]
    (->> (all-positions grid)
         (filter #(accessible? grid %))
         count)))

(defn find-accessible [grid]
  (->> (all-positions grid)
       (filter #(accessible? grid %))
       set))

(defn remove-rolls [grid positions]
  (reduce (fn [g pos] (assoc-in g pos \.))
          grid
          positions))

(defn solve-part2 [input]
  (loop [grid (parse-input input)
         total 0]
    (let [accessible (find-accessible grid)]
      (if (empty? accessible)
        total
        (recur (remove-rolls grid accessible)
               (+ total (count accessible)))))))

(defn solve-part2-optimized [input]
  (let [grid (parse-input input)
        rolls (set (filter #(roll? grid %) (all-positions grid)))]
    (loop [rolls rolls
           queue (filter #(< (count (filter rolls (neighbors %))) 4) rolls)
           total 0]
      (if (empty? queue)
        total
        (let [;; Remove all currently accessible from rolls
              accessible (set (filter #(and (rolls %)
                                            (< (count (filter rolls (neighbors %))) 4))
                                      queue))
              rolls' (reduce disj rolls accessible)
              ;; Only re-check neighbors of removed rolls
              to-check (filter rolls' (mapcat neighbors accessible))]
          (recur rolls' to-check (+ total (count accessible))))))))

(defmacro timed [expr]
  `(let [start# (System/nanoTime)
         result# ~expr
         ms# (/ (- (System/nanoTime) start#) 1e6)]
     [result# ms#]))

(defn -main []
  (let [input (slurp *in*)
        [r1 t1] (timed (solve-part2 input))
        [r2 t2] (timed (solve-part2-optimized input))]
    (println "Part 1:" (solve-part1 input))
    (println (format "Part 2: %d (%.2fms)" r1 t1))
    (println (format "Part 2 (optimized): %d (%.2fms)" r2 t2))))

(when (= *file* (System/getProperty "babashka.file"))
  (-main))
